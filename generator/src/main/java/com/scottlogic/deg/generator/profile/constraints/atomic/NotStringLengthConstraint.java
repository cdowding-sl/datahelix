package com.scottlogic.deg.generator.profile.constraints.atomic;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.HelixStringLength;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecFactory;
import com.scottlogic.deg.generator.restrictions.StringRestrictionsFactory;

import java.util.Objects;

public class NotStringLengthConstraint implements AtomicConstraint {
    public final Field field;
    public final HelixStringLength referenceValue;

    public NotStringLengthConstraint(Field field, HelixStringLength referenceValue) {
        this.referenceValue = referenceValue;
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public AtomicConstraint negate() {
        return new StringHasLengthConstraint(field, referenceValue);
    }

    @Override
    public FieldSpec toFieldSpec() {
        return FieldSpecFactory.fromRestriction(StringRestrictionsFactory.forLength(referenceValue.getValue(), true));
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o instanceof ViolatedAtomicConstraint) {
            return o.equals(this);
        }
        if (o == null || getClass() != o.getClass()) return false;
        NotStringLengthConstraint constraint = (NotStringLengthConstraint) o;
        return Objects.equals(field, constraint.field) && Objects.equals(referenceValue, constraint.referenceValue);
    }

    @Override
    public int hashCode(){
        return Objects.hash(field, referenceValue);
    }

    @Override
    public String toString() { return String.format("`%s` length = %s", field.getName(), referenceValue); }
}
