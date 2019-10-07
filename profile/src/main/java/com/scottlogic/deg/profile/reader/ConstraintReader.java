/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.profile.reader;

import com.google.inject.Inject;
import com.scottlogic.deg.common.date.TemporalAdjusterGenerator;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType;
import com.scottlogic.deg.common.profile.constraintdetail.ParsedDateGranularity;
import com.scottlogic.deg.common.profile.constraintdetail.ParsedGranularity;
import com.scottlogic.deg.common.profile.constraints.Constraint;
import com.scottlogic.deg.common.profile.constraints.atomic.*;
import com.scottlogic.deg.common.profile.constraints.delayed.DelayedAtomicConstraint;
import com.scottlogic.deg.common.profile.constraints.delayed.DelayedDateAtomicConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.OrConstraint;
import com.scottlogic.deg.common.util.NumberUtils;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.*;
import com.scottlogic.deg.profile.dtos.constraints.predicate.PredicateConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.AfterConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.AfterOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.BeforeConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.BeforeOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.*;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.GreaterThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.GreaterThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.LessThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.LessThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.textual.*;
import com.scottlogic.deg.profile.dtos.fields.FieldDTO;
import com.scottlogic.deg.profile.reader.atomic.FileReader;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConstraintReader
{
    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private final FileReader fileReader;

    @Inject
    public ConstraintReader(FileReader fileReader)
    {
        this.fileReader = fileReader;
    }

    Set<Constraint> readMany(Collection<ConstraintDTO> constraints, ProfileFields fields)
    {
        return constraints.stream()
                .map(subConstraintDto -> read(subConstraintDto, fields))
                .filter(constraint -> !(constraint instanceof RemoveFromTree))
                .collect(Collectors.toSet());
    }


    public Constraint read(ConstraintDTO dto, ProfileFields fields)
    {
        if (dto == null) throw new InvalidProfileException("Constraint is null");
        if (dto instanceof GrammaticalConstraintDTO) return readGrammaticalConstraint((GrammaticalConstraintDTO) dto, fields);
        PredicateConstraintDTO predicateConstraintDTO = (PredicateConstraintDTO) dto;
        if (predicateConstraintDTO.hasDependency()) return readDelayed((EqualToConstraintDTO) dto, fields);
        return readPredicateConstraint(predicateConstraintDTO, fields.getByName(predicateConstraintDTO.field));
    }

    private DelayedAtomicConstraint readDelayed(EqualToConstraintDTO dto, ProfileFields fields)
    {
        return dto.offsetUnit != null
                ? new DelayedDateAtomicConstraint(fields.getByName(dto.field), AtomicConstraintType.IS_EQUAL_TO_CONSTANT, fields.getByName(dto.otherField), getOffsetUnit(dto), dto.offset)
                : new DelayedDateAtomicConstraint(fields.getByName(dto.field), AtomicConstraintType.IS_AFTER_CONSTANT_DATE_TIME, fields.getByName(dto.otherField));
    }

    private TemporalAdjusterGenerator getOffsetUnit(EqualToConstraintDTO dto)
    {
        String offsetUnitUpperCase = dto.offsetUnit.toUpperCase();
        boolean workingDay = offsetUnitUpperCase.equals("WORKING DAYS");
        return new TemporalAdjusterGenerator(Enum.valueOf(ChronoUnit.class, workingDay ? "DAYS" : offsetUnitUpperCase), workingDay);
    }

    Constraint readNullConstraint(FieldDTO fieldDTO, ProfileFields profileFields)
    {
        return readPredicateConstraint(new NullConstraintDTO(), profileFields.getByName(fieldDTO.name)).negate();
    }

    private AtomicConstraint readPredicateConstraint(PredicateConstraintDTO dto, Field field)
    {
        switch (dto.getType())
        {
            case EQUAL_TO:
                return new EqualToConstraint(field, ((EqualToConstraintDTO)dto).value);
            case IN_SET:
                InSetConstraintDTO inSetConstraintDTO = (InSetConstraintDTO)dto;
                return new IsInSetConstraint(field, inSetConstraintDTO.file != null
                        ? fileReader.setFromFile(inSetConstraintDTO.file)
                        : DistributedList.uniform(inSetConstraintDTO.values.stream().distinct().collect(Collectors.toList())));
            case IN_MAP:
                InMapConstraintDTO inMapConstraintDTO = (InMapConstraintDTO) dto;
                return new IsInMapConstraint(field, fileReader.listFromMapFile(inMapConstraintDTO.file, inMapConstraintDTO.key));
            case NULL:
                return new IsNullConstraint(field);
            case MATCHES_REGEX:
                return new MatchesRegexConstraint(field, pattern(((MatchesRegexConstraintDTO)dto).value));
            case CONTAINS_REGEX:
                return new ContainsRegexConstraint(field, pattern(((ContainsRegexConstraintDTO)dto).value));
            case OF_LENGTH:
                return new StringHasLengthConstraint(field, integer(((OfLengthConstraintDTO)dto).value));
            case SHORTER_THAN:
                return new IsStringShorterThanConstraint(field, integer(((ShorterThanConstraintDTO)dto).value));
            case LONGER_THAN:
                return new IsStringLongerThanConstraint(field, integer(((LongerThanConstraintDTO)dto).value));
            case GREATER_THAN:
                return new IsGreaterThanConstantConstraint(field, NumberUtils.coerceToBigDecimal(((GreaterThanConstraintDTO)dto).value));
            case GREATER_THAN_OR_EQUAL_TO:
                return new IsGreaterThanOrEqualToConstantConstraint(field, NumberUtils.coerceToBigDecimal(((GreaterThanOrEqualToConstraintDTO)dto).value));
            case LESS_THAN:
                return new IsLessThanConstantConstraint(field, NumberUtils.coerceToBigDecimal(((LessThanConstraintDTO)dto).value));
            case LESS_THAN_OR_EQUAL_TO:
                return new IsLessThanOrEqualToConstantConstraint(field, NumberUtils.coerceToBigDecimal(((LessThanOrEqualToConstraintDTO)dto).value));
            case AFTER:
                return new IsAfterConstantDateTimeConstraint(field, parseDate(((AfterConstraintDTO)dto).value));
            case AFTER_OR_AT:
                return new IsAfterOrEqualToConstantDateTimeConstraint(field, parseDate(((AfterOrAtConstraintDTO)dto).value));
            case BEFORE:
                return new IsBeforeConstantDateTimeConstraint(field, parseDate(((BeforeConstraintDTO)dto).value));
            case BEFORE_OR_AT:
                return new IsBeforeOrEqualToConstantDateTimeConstraint(field, parseDate(((BeforeOrAtConstraintDTO)dto).value));
            case GRANULAR_TO:
                GranularToConstraintDTO granularToConstraintDTO = (GranularToConstraintDTO)dto;
                return granularToConstraintDTO.value instanceof Number
                        ? new IsGranularToNumericConstraint(field, ParsedGranularity.parse(granularToConstraintDTO.value))
                        : new IsGranularToDateConstraint(field, ParsedDateGranularity.parse((String) granularToConstraintDTO.value));
            default:
                throw new InvalidProfileException("Predicate constraint type not found: " + dto);
        }
    }

    private Constraint readGrammaticalConstraint(GrammaticalConstraintDTO dto, ProfileFields profileFields)
    {
        switch (dto.getType())
        {
            case NOT:
                return read(((NotConstraintDTO)dto).constraint, profileFields).negate();
            case ALL_OF:
                return new AndConstraint(readMany(((AllOfConstraintDTO)dto).constraints, profileFields));
            case ANY_OF:
                return new OrConstraint(readMany(((AnyOfConstraintDTO)dto).constraints, profileFields));
            case CONDITION:
                ConditionalConstraintDTO conditionalConstraintDTO = (ConditionalConstraintDTO) dto;
                Constraint ifConstraint = read(conditionalConstraintDTO.ifConstraint, profileFields);
                Constraint thenConstraint = read(conditionalConstraintDTO.thenConstraint, profileFields);
                Constraint elseConstraint = conditionalConstraintDTO.elseConstraint == null ? null
                        : read(conditionalConstraintDTO.elseConstraint, profileFields);
                return new ConditionalConstraint(ifConstraint, thenConstraint, elseConstraint);
            default:
                throw new InvalidProfileException("Grammatical constraint type not found" + dto);
        }
    }

    private static OffsetDateTime parseDate(String value)
    {
        if (value.equalsIgnoreCase("NOW")) return NOW;

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern("u-MM-dd'T'HH:mm:ss'.'SSS"))
                .optionalStart()
                .appendOffset("+HH", "Z")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
        try
        {
            TemporalAccessor temporalAccessor = formatter.parse(value);
            return temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)
                    ? OffsetDateTime.from(temporalAccessor)
                    : LocalDateTime.from(temporalAccessor).atOffset(ZoneOffset.UTC);
        }
        catch (DateTimeParseException exception)
        {
            throw new InvalidProfileException(String.format(
                    "Date string '%s' must be in ISO-8601 format: yyyy-MM-ddTHH:mm:ss.SSS[Z] between (inclusive) " +
                            "0001-01-01T00:00:00.000Z and 9999-12-31T23:59:59.999Z", value));
        }
    }

    private static int integer(Object value)
    {
        return NumberUtils.coerceToBigDecimal(value).intValueExact();
    }

    private static Pattern pattern(Object value)
    {
        return value instanceof Pattern ? (Pattern) value : Pattern.compile((String) value);
    }
}
