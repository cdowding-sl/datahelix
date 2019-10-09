package com.scottlogic.deg.generator.profile.constraints.atomic;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.restrictions.StringRestrictionsFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public class NotMatchesRegexConstraint implements AtomicConstraint {
    public final Field field;
    public final Pattern regex;

    public NotMatchesRegexConstraint(Field field, Pattern regex) {
        this.field = field;
        this.regex = regex;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public AtomicConstraint negate() {
        return new MatchesRegexConstraint(field, regex);
    }

    @Override
    public FieldSpec toFieldSpec() {
        return FieldSpec.fromRestriction(StringRestrictionsFactory.forStringMatching(regex, true));
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o instanceof ViolatedAtomicConstraint) {
            return o.equals(this);
        }
        if (o == null || getClass() != o.getClass()) return false;
        NotMatchesRegexConstraint constraint = (NotMatchesRegexConstraint) o;
        return Objects.equals(field, constraint.field) && Objects.equals(regex.toString(), constraint.regex.toString());
    }

    @Override
    public int hashCode(){
        return Objects.hash(field, regex.toString());
    }

    @Override
    public String toString(){ return String.format("`%s` NOT matches /%s/", field.name, regex); }
}
