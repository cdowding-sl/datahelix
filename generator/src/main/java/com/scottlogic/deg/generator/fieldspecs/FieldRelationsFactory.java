package com.scottlogic.deg.profile.reader.atomic;

import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.generator.profile.DataType;
import com.scottlogic.deg.generator.profile.Field;
import com.scottlogic.deg.generator.profile.ProfileFields;
import com.scottlogic.deg.generator.profile.constraintdetail.AtomicConstraintType;
import com.scottlogic.deg.generator.profile.constraintdetail.Granularity;
import com.scottlogic.deg.generator.fieldspecs.relations.*;

public class RelationsFactory {
    public static FieldSpecRelations create(ConstraintDTO dto, ProfileFields fields){
        Field main = fields.getByName(dto.field);
        Field other = fields.getByName(dto.otherField);

        if (main.type != other.type){
            throw new ValidationException("Field " + main.name + " cannot be related to other field " + other.name);
        }

        Granularity offsetGranularity = getOffsetUnit(main.type, dto.offsetUnit);

        switch (AtomicConstraintType.fromText((String) dto.is)) {
            case IS_EQUAL_TO_CONSTANT:
                if (offsetGranularity != null){
                    return new EqualToOffsetRelation(main, other, offsetGranularity, dto.offset);
                }
                return new EqualToRelation(main, other);

            case IS_AFTER_CONSTANT_DATE_TIME:
                return new AfterRelation(main, other, false, DateTimeDefaults.get());
            case IS_AFTER_OR_EQUAL_TO_CONSTANT_DATE_TIME:
                return new AfterRelation(main, other, true, DateTimeDefaults.get());
            case IS_BEFORE_CONSTANT_DATE_TIME:
                return new BeforeRelation(main, other, false, DateTimeDefaults.get());
            case IS_BEFORE_OR_EQUAL_TO_CONSTANT_DATE_TIME:
                return new BeforeRelation(main, other, true, DateTimeDefaults.get());

            case IS_GREATER_THAN_CONSTANT:
                return new AfterRelation(main, other, false, NumericDefaults.get());
            case IS_GREATER_THAN_OR_EQUAL_TO_CONSTANT:
                return new AfterRelation(main, other, true, NumericDefaults.get());
            case IS_LESS_THAN_CONSTANT:
                return new BeforeRelation(main, other, false, NumericDefaults.get());
            case IS_LESS_THAN_OR_EQUAL_TO_CONSTANT:
                return new BeforeRelation(main, other, true, NumericDefaults.get());
        }

        throw new ValidationException(dto.is + "cannot be used with OtherValue)");
    }

    private static Granularity getOffsetUnit(DataType type, String offsetUnit) {
        if (offsetUnit == null) return null;

        switch (type) {
            case NUMERIC:
                return GranularityFactory.create(offsetUnit);
            case DATETIME:
                return getDateTimeGranularity(offsetUnit);
            default:
                return null;
        }
    }
}