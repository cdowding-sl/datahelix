package com.scottlogic.deg.profile.reader.atomic;

import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraintdetail.ParsedDateGranularity;
import com.scottlogic.deg.common.profile.constraintdetail.ParsedGranularity;
import com.scottlogic.deg.common.profile.constraints.Constraint;
import com.scottlogic.deg.common.profile.constraints.atomic.*;
import com.scottlogic.deg.common.util.NumberUtils;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.profile.dtos.constraints.PredicateConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.AfterConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.AfterOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.BeforeConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.BeforeOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.EqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.GranularToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.InMapConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.InSetConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.GreaterThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.GreaterThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.LessThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.LessThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.textual.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConstraintFactory
{
    private final FileReader fileReader;

    @Inject
    public ConstraintFactory(FileReader fileReader)
    {
        this.fileReader = fileReader;
    }

    public Constraint create(PredicateConstraintDTO dto, Field field)
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
                return new IsAfterConstantDateTimeConstraint(field, ConstraintReaderHelpers.parseDate(((AfterConstraintDTO)dto).value));
            case AFTER_OR_AT:
                return new IsAfterOrEqualToConstantDateTimeConstraint(field, ConstraintReaderHelpers.parseDate(((AfterOrAtConstraintDTO)dto).value));
            case BEFORE:
                return new IsBeforeConstantDateTimeConstraint(field, ConstraintReaderHelpers.parseDate(((BeforeConstraintDTO)dto).value));
            case BEFORE_OR_AT:
                return new IsBeforeOrEqualToConstantDateTimeConstraint(field, ConstraintReaderHelpers.parseDate(((BeforeOrAtConstraintDTO)dto).value));
            case GRANULAR_TO:
                GranularToConstraintDTO granularToConstraintDTO = (GranularToConstraintDTO)dto;
                return granularToConstraintDTO.value instanceof Number
                        ? new IsGranularToNumericConstraint(field, ParsedGranularity.parse(granularToConstraintDTO.value))
                        : new IsGranularToDateConstraint(field, ParsedDateGranularity.parse((String) granularToConstraintDTO.value));
            default:
                throw new IllegalArgumentException("constraint type not found");
        }
    }

    private static int integer(Object value) {
        return NumberUtils.coerceToBigDecimal(value).intValueExact();
    }

    private static Pattern pattern(Object value) {
        return value instanceof Pattern ? (Pattern) value : Pattern.compile((String) value);
    }
}
