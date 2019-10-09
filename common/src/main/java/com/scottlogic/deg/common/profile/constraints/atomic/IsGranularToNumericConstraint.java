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

package com.scottlogic.deg.common.profile.constraints.atomic;

import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraintdetail.NumericGranularity;


import java.util.Objects;

import static com.scottlogic.deg.common.util.Defaults.NUMERIC_MAX;
import static com.scottlogic.deg.common.util.Defaults.NUMERIC_MIN;

public class IsGranularToNumericConstraint implements AtomicConstraint {
    public final Field field;
    public final NumericGranularity granularity;

    public IsGranularToNumericConstraint(Field field, NumericGranularity granularity) {
        if(field == null)
            throw new IllegalArgumentException("field must not be null");
        if(granularity == null)
            throw new IllegalArgumentException("granularity must not be null");

        this.granularity = granularity;
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public AtomicConstraint negate() {
        throw new ValidationException("Numeric Granularity cannot be negated or used in if statements");
    }

    @Override
    public FieldSpec toFieldSpec() {
        return FieldSpec.fromRestriction(new LinearRestrictions(NUMERIC_MIN, NUMERIC_MAX, granularity));
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o instanceof ViolatedAtomicConstraint) {
            return o.equals(this);
        }
        if (o == null || getClass() != o.getClass()) return false;
        IsGranularToNumericConstraint constraint = (IsGranularToNumericConstraint) o;
        return Objects.equals(field, constraint.field) && Objects.equals(granularity, constraint.granularity);
    }

    @Override
    public int hashCode(){
        return Objects.hash(field, granularity);
    }

    @Override
    public String toString() {
        return String.format("%s granular to %s", field.name, granularity);
    }
}
