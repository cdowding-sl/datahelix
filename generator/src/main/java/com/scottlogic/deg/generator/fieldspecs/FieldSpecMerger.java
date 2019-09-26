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

package com.scottlogic.deg.generator.fieldspecs;

import com.scottlogic.deg.common.profile.Types;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.generator.fieldspecs.whitelist.WeightedElement;
import com.scottlogic.deg.generator.restrictions.StringRestrictionsMerger;
import com.scottlogic.deg.generator.restrictions.linear.LinearRestrictionsMerger;
import com.scottlogic.deg.generator.utils.SetUtils;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Returns a FieldSpec that permits only data permitted by all of its inputs
 */
public class FieldSpecMerger {
    private final RestrictionsMergeOperation restrictionMergeOperation =
        new RestrictionsMergeOperation(new LinearRestrictionsMerger(), new StringRestrictionsMerger());

    /**
     * Null parameters are permitted, and are synonymous with an empty FieldSpec
     * <p>
     * Returning an empty Optional conveys that the fields were unmergeable.
     */
    public Optional<FieldSpec> merge(FieldSpec left, FieldSpec right) {
        if (hasSet(left) && hasSet(right)) {
            return mergeSets(left, right);
        }
        if (hasSet(left)) {
            return combineSetWithRestrictions(left, right);
        }
        if (hasSet(right)) {
            return combineSetWithRestrictions(right, left);
        }
        return combineRestrictions(left, right);
    }

    private static WeightedElement<Object> mergeElements(WeightedElement<Object> left,
                                                         WeightedElement<Object> right) {
        return new WeightedElement<>(left.element(), left.weight() + right.weight());
    }

    private Optional<FieldSpec> mergeSets(FieldSpec left, FieldSpec right) {
        DistributedList<Object> set = new DistributedList<>(left.getWhitelist().distributedSet().stream()
            .flatMap(leftHolder -> right.getWhitelist().distributedSet().stream()
                .filter(rightHolder -> elementsEqual(leftHolder, rightHolder))
                .map(rightHolder -> mergeElements(leftHolder, rightHolder)))
            .collect(Collectors.toList()));

        return addNullable(left, right, setRestriction(left.getType(), set));
    }

    private static <T> boolean elementsEqual(WeightedElement<T> left, WeightedElement<T> right) {
        return left.element().equals(right.element());
    }

    private Optional<FieldSpec> combineSetWithRestrictions(FieldSpec set, FieldSpec restrictions) {
        DistributedList<Object> newSet = new DistributedList<>(
            set.getWhitelist().distributedSet().stream()
                .filter(holder -> restrictions.permits(holder.element()))
                .collect(Collectors.toList()));

        return addNullable(set, restrictions, setRestriction(set.getType(), newSet));
    }

    private Optional<FieldSpec> addNullable(FieldSpec left, FieldSpec right, FieldSpec newFieldSpec) {
        if (isNullable(left, right)) {
            return Optional.of(newFieldSpec);
        }

        if (noAllowedValues(newFieldSpec)) {
            return Optional.empty();
        }

        return Optional.of(newFieldSpec.withNotNull());
    }

    private boolean noAllowedValues(FieldSpec fieldSpec) {
        return (fieldSpec.getWhitelist() != null && fieldSpec.getWhitelist().isEmpty());
    }

    private FieldSpec setRestriction(Types type, DistributedList<Object> set) {
        return FieldSpec.fromType(type).withWhitelist(set);
    }

    private boolean hasSet(FieldSpec fieldSpec) {
        return fieldSpec.getWhitelist() != null;
    }

    private boolean isNullable(FieldSpec left, FieldSpec right) {
        return left.isNullable() && right.isNullable();
    }

    private Optional<FieldSpec> combineRestrictions(FieldSpec left, FieldSpec right) {
        FieldSpec merged = restrictionMergeOperation.applyMergeOperation(left, right);

        merged = merged.withBlacklist(SetUtils.union(left.getBlacklist(), right.getBlacklist()));

        return addNullable(left, right, merged);
    }
}
