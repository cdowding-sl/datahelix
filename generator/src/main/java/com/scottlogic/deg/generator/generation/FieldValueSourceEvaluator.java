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

package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.common.profile.DataType;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.*;
import com.scottlogic.deg.generator.generation.fieldvaluesources.datetime.DateTimeFieldValueSource;
import com.scottlogic.deg.generator.generation.string.generators.RegexStringGenerator;
import com.scottlogic.deg.generator.generation.string.generators.StringGenerator;
import com.scottlogic.deg.generator.restrictions.*;
import com.scottlogic.deg.generator.restrictions.linear.LinearRestrictions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static com.scottlogic.deg.common.util.Defaults.*;
import static com.scottlogic.deg.generator.restrictions.linear.LinearRestrictionsFactory.createDateTimeRestrictions;
import static com.scottlogic.deg.generator.restrictions.linear.LinearRestrictionsFactory.createNumericRestrictions;
import static com.scottlogic.deg.generator.utils.Defaults.*;

public class FieldValueSourceEvaluator {
    private static final FieldValueSource NULL_ONLY_SOURCE = new NullOnlySource();

    public FieldValueSource getFieldValueSources(DataType type, FieldSpec fieldSpec){

        Optional<FieldValueSource> source = getSource(type, fieldSpec);

        if (!fieldSpec.isNullable()){
            return source
                .orElseThrow(() -> new UnsupportedOperationException("Cannot get fieldValueSource for contradictory fieldspec"));
        }

        if (!source.isPresent()){
            return NULL_ONLY_SOURCE;
        }

        return new NullAppendingValueSource(source.get());
    }

    private Optional<FieldValueSource> getSource(DataType type, FieldSpec fieldSpec) {
        if (fieldSpec.getWhitelist() != null){
            if (fieldSpec.getWhitelist().isEmpty()){
                return Optional.empty();
            }

            return Optional.of(new CannedValuesFieldValueSource(fieldSpec.getWhitelist()));
        }

        return Optional.of(getRestrictionSource(type, fieldSpec));
    }

    private FieldValueSource getRestrictionSource(DataType type, FieldSpec fieldSpec) {
        switch (type.getGenericDataType()) {
            case DATETIME:
                return getDateTimeSource(fieldSpec);
            case STRING:
                return getStringSource(fieldSpec);
            case NUMERIC:
                return getNumericSource(fieldSpec);
                default:
                    throw new UnsupportedOperationException("unexpected type");
        }
    }

    private FieldValueSource getNumericSource(FieldSpec fieldSpec) {
        LinearRestrictions<BigDecimal> restrictions =
            fieldSpec.getRestrictions() == null
                ? new LinearRestrictions<>(NUMERIC_MIN, NUMERIC_MAX, DEFAULT_NUMERIC_GRANULARITY)
                : (LinearRestrictions<BigDecimal>) fieldSpec.getRestrictions();

        return new RealNumberFieldValueSource(restrictions, fieldSpec.getBlacklist());
    }

    private FieldValueSource getStringSource(FieldSpec fieldSpec) {
        StringRestrictions stringRestrictions =
            fieldSpec.getRestrictions() == null
                ? new StringRestrictionsFactory().forMaxLength(1000)
                : (StringRestrictions) fieldSpec.getRestrictions();

        StringGenerator generator = stringRestrictions.createGenerator();
        if (!fieldSpec.getBlacklist().isEmpty()) {
            RegexStringGenerator blacklistGenerator = RegexStringGenerator.createFromBlacklist(fieldSpec.getBlacklist());
            return generator.intersect(blacklistGenerator);
        }

        return generator;
    }

    private FieldValueSource getDateTimeSource(FieldSpec fieldSpec) {
        LinearRestrictions<OffsetDateTime> restrictions =
            fieldSpec.getRestrictions() == null
                ? createDateTimeRestrictions(DATETIME_MIN_LIMIT, DATETIME_MAX_LIMIT)
                : (LinearRestrictions<OffsetDateTime>) fieldSpec.getRestrictions();

        return new DateTimeFieldValueSource(restrictions, fieldSpec.getBlacklist());
    }
}
