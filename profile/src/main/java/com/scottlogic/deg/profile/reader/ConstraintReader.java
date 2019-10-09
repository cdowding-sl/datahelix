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
import com.scottlogic.deg.common.profile.constraintdetail.ParsedDateGranularity;
import com.scottlogic.deg.common.profile.constraintdetail.ParsedGranularity;
import com.scottlogic.deg.common.profile.constraints.Constraint;
import com.scottlogic.deg.common.profile.constraints.atomic.*;
import com.scottlogic.deg.common.profile.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.OrConstraint;
import com.scottlogic.deg.common.util.NumberUtils;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.AllOfConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.AnyOfConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.ConditionalConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.GrammaticalConstraintDTO;
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

import java.math.BigDecimal;
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
import java.util.Optional;
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
                .collect(Collectors.toSet());
    }


    public Constraint read(ConstraintDTO dto, ProfileFields profileFields)
    {
        if (dto == null) throw new InvalidProfileException("Constraint is null");
        if (dto instanceof PredicateConstraintDTO)
        {
            PredicateConstraintDTO predicateConstraintDTO = (PredicateConstraintDTO) dto;
            if (predicateConstraintDTO.hasDependency()) return null;
            switch (predicateConstraintDTO.getType())
            {
                case EQUAL_TO:
                    Field equalToField = profileFields.getByName(predicateConstraintDTO.field);
                    EqualToConstraintDTO equalToConstraintDTO = (EqualToConstraintDTO) predicateConstraintDTO;
                    switch (equalToField.type.getGenericDataType())
                    {
                        case DATETIME:
                            return new EqualToConstraint(equalToField, parseDate((String) equalToConstraintDTO.value));
                        case NUMERIC:
                            return new EqualToConstraint(equalToField, NumberUtils.coerceToBigDecimal(equalToConstraintDTO.value));
                        default:
                            return new EqualToConstraint(equalToField, equalToConstraintDTO.value);
                    }
                case IN_SET:
                    InSetConstraintDTO inSetConstraintDTO = (InSetConstraintDTO) predicateConstraintDTO;
                    Field inSetField = profileFields.getByName(predicateConstraintDTO.field);
                    return new IsInSetConstraint(inSetField, inSetConstraintDTO.file != null
                            ? fileReader.setFromFile(inSetConstraintDTO.file)
                            : DistributedList.uniform(inSetConstraintDTO.values.stream().distinct()
                            .map(value ->
                            {
                                switch (inSetField.type.getGenericDataType())
                                {
                                    case DATETIME:
                                        return parseDate((String) value);
                                    case NUMERIC:
                                        return NumberUtils.coerceToBigDecimal(value);
                                    default:
                                        return value;
                                }
                            })
                            .collect(Collectors.toList())));
                case IN_MAP:
                    InMapConstraintDTO inMapConstraintDTO = (InMapConstraintDTO) predicateConstraintDTO;
                    return new IsInMapConstraint(profileFields.getByName(predicateConstraintDTO.field), fileReader.listFromMapFile(inMapConstraintDTO.file, inMapConstraintDTO.key));
                case NULL:
                    return new IsNullConstraint(profileFields.getByName(predicateConstraintDTO.field));
                case MATCHES_REGEX:
                    return new MatchesRegexConstraint(profileFields.getByName(predicateConstraintDTO.field), pattern(((MatchesRegexConstraintDTO) predicateConstraintDTO).value));
                case CONTAINS_REGEX:
                    return new ContainsRegexConstraint(profileFields.getByName(predicateConstraintDTO.field), pattern(((ContainsRegexConstraintDTO) predicateConstraintDTO).value));
                case OF_LENGTH:
                    return new StringHasLengthConstraint(profileFields.getByName(predicateConstraintDTO.field), integer(((OfLengthConstraintDTO) predicateConstraintDTO).value));
                case SHORTER_THAN:
                    return new IsStringShorterThanConstraint(profileFields.getByName(predicateConstraintDTO.field), integer(((ShorterThanConstraintDTO) predicateConstraintDTO).value));
                case LONGER_THAN:
                    return new IsStringLongerThanConstraint(profileFields.getByName(predicateConstraintDTO.field), integer(((LongerThanConstraintDTO) predicateConstraintDTO).value));
                case GREATER_THAN:
                    return new IsGreaterThanConstantConstraint(profileFields.getByName(predicateConstraintDTO.field), NumberUtils.coerceToBigDecimal(((GreaterThanConstraintDTO) predicateConstraintDTO).value));
                case GREATER_THAN_OR_EQUAL_TO:
                    return new IsGreaterThanOrEqualToConstantConstraint(profileFields.getByName(predicateConstraintDTO.field), NumberUtils.coerceToBigDecimal(((GreaterThanOrEqualToConstraintDTO) predicateConstraintDTO).value));
                case LESS_THAN:
                    return new IsLessThanConstantConstraint(profileFields.getByName(predicateConstraintDTO.field), NumberUtils.coerceToBigDecimal(((LessThanConstraintDTO) predicateConstraintDTO).value));
                case LESS_THAN_OR_EQUAL_TO:
                    return new IsLessThanOrEqualToConstantConstraint(profileFields.getByName(predicateConstraintDTO.field), NumberUtils.coerceToBigDecimal(((LessThanOrEqualToConstraintDTO) predicateConstraintDTO).value));
                case AFTER:
                    return new IsAfterConstantDateTimeConstraint(profileFields.getByName(predicateConstraintDTO.field), parseDate(((AfterConstraintDTO) predicateConstraintDTO).value));
                case AFTER_OR_AT:
                    return new IsAfterOrEqualToConstantDateTimeConstraint(profileFields.getByName(predicateConstraintDTO.field), parseDate(((AfterOrAtConstraintDTO) predicateConstraintDTO).value));
                case BEFORE:
                    return new IsBeforeConstantDateTimeConstraint(profileFields.getByName(predicateConstraintDTO.field), parseDate(((BeforeConstraintDTO) predicateConstraintDTO).value));
                case BEFORE_OR_AT:
                    return new IsBeforeOrEqualToConstantDateTimeConstraint(profileFields.getByName(predicateConstraintDTO.field), parseDate(((BeforeOrAtConstraintDTO) predicateConstraintDTO).value));
                case GRANULAR_TO:
                    GranularToConstraintDTO granularToConstraintDTO = (GranularToConstraintDTO) predicateConstraintDTO;
                    return granularToConstraintDTO.value instanceof Number
                            ? new IsGranularToNumericConstraint(profileFields.getByName(predicateConstraintDTO.field), ParsedGranularity.parse(granularToConstraintDTO.value))
                            : new IsGranularToDateConstraint(profileFields.getByName(predicateConstraintDTO.field), ParsedDateGranularity.parse((String) granularToConstraintDTO.value));
                case NOT:
                    return read(((NotConstraintDTO) predicateConstraintDTO).constraint, profileFields).negate();
                default:
                    throw new InvalidProfileException("Predicate constraint type not found: " + predicateConstraintDTO);
            }
        }
        if (dto instanceof GrammaticalConstraintDTO)
        {
            GrammaticalConstraintDTO grammaticalConstraintDTO = (GrammaticalConstraintDTO) dto;
            switch (grammaticalConstraintDTO.getType())
            {
                case ALL_OF:
                    return new AndConstraint(readMany(((AllOfConstraintDTO) grammaticalConstraintDTO).constraints, profileFields));
                case ANY_OF:
                    return new OrConstraint(readMany(((AnyOfConstraintDTO) grammaticalConstraintDTO).constraints, profileFields));
                case CONDITION:
                    ConditionalConstraintDTO conditionalConstraintDTO = (ConditionalConstraintDTO) grammaticalConstraintDTO;
                    Constraint ifConstraint = read(conditionalConstraintDTO.ifConstraint, profileFields);
                    Constraint thenConstraint = read(conditionalConstraintDTO.thenConstraint, profileFields);
                    Constraint elseConstraint = conditionalConstraintDTO.elseConstraint == null ? null
                            : read(conditionalConstraintDTO.elseConstraint, profileFields);
                    return new ConditionalConstraint(ifConstraint, thenConstraint, elseConstraint);
                default:
                    throw new InvalidProfileException("Grammatical constraint type not found: " + grammaticalConstraintDTO);
            }
        }
        throw new InvalidProfileException("Constraint type not found: " + dto);
    }

    public Optional<Constraint> readType(Field field)
    {
        switch (field.type) {
            case INTEGER:
                return Optional.of(new IsGranularToNumericConstraint(field, new ParsedGranularity(BigDecimal.ONE)));
            case ISIN:
                return Optional.of(new MatchesStandardConstraint(field, StandardConstraintTypes.ISIN));
            case SEDOL:
                return Optional.of(new MatchesStandardConstraint(field, StandardConstraintTypes.SEDOL));
            case CUSIP:
                return Optional.of(new MatchesStandardConstraint(field, StandardConstraintTypes.CUSIP));
            case RIC:
                return Optional.of(new MatchesStandardConstraint(field, StandardConstraintTypes.RIC));
            case FIRST_NAME:
                return Optional.of(new IsInSetConstraint(field, NameRetriever.loadNamesFromFile(NameConstraintTypes.FIRST)));
            case LAST_NAME:
                return Optional.of(new IsInSetConstraint(field, NameRetriever.loadNamesFromFile(NameConstraintTypes.LAST)));
            case FULL_NAME:
                return Optional.of(new IsInSetConstraint(field, NameRetriever.loadNamesFromFile(NameConstraintTypes.FULL)));
            default:
                return Optional.empty();
        }
    }

    private TemporalAdjusterGenerator getOffsetUnit(EqualToConstraintDTO dto)
    {
        String offsetUnitUpperCase = dto.offsetUnit.toUpperCase();
        boolean workingDay = offsetUnitUpperCase.equals("WORKING DAYS");
        return new TemporalAdjusterGenerator(Enum.valueOf(ChronoUnit.class, workingDay ? "DAYS" : offsetUnitUpperCase), workingDay);
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
        } catch (DateTimeParseException exception)
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
