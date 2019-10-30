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

package com.scottlogic.deg.generator.fieldspecs.relations;

import com.scottlogic.deg.common.profile.*;
import com.scottlogic.deg.common.util.defaults.DateTimeDefaults;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecFactory;
import com.scottlogic.deg.generator.generation.databags.DataBagValue;
import com.scottlogic.deg.generator.restrictions.linear.LinearRestrictions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static com.scottlogic.deg.common.util.Defaults.ISO_MAX_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AfterDateRelationTest {

    private final Field a = new Field("a", SpecificFieldType.DATETIME, false, "", false, false);
    private final Field b = new Field("b", SpecificFieldType.DATETIME, false, "", false, false);

    @Test
    public void testReduceToFieldSpec_withNotNull_reducesToSpec() {
        FieldSpecRelations afterDateRelations = new AfterRelation(a, b, true, DateTimeDefaults.get());
        OffsetDateTime value = OffsetDateTime.of(2000,
            1,
            1,
            0,
            0,
            0,
            0,
            ZoneOffset.UTC);
        DataBagValue generatedValue = new DataBagValue(value);

        FieldSpec result = afterDateRelations.createModifierFromOtherValue(generatedValue);

        FieldSpec expected = FieldSpecFactory.fromRestriction(new LinearRestrictions<>(value, ISO_MAX_DATE, new DateTimeGranularity(ChronoUnit.MILLIS)));
        assertEquals(expected, result);
    }

    @Test
    public void testReduceToFieldSpec_withNotNullExclusive_reducesToSpec() {
        FieldSpecRelations afterDateRelations = new AfterRelation(a, b, false, DateTimeDefaults.get());
        OffsetDateTime value = OffsetDateTime.of(2000,
            1,
            1,
            0,
            0,
            0,
            0,
            ZoneOffset.UTC);
        DataBagValue generatedValue = new DataBagValue(value);

        FieldSpec result = afterDateRelations.createModifierFromOtherValue(generatedValue);

        Granularity<OffsetDateTime> granularity = new DateTimeGranularity(ChronoUnit.MILLIS);
        FieldSpec expected = FieldSpecFactory.fromRestriction(new LinearRestrictions<>(granularity.getNext(value), ISO_MAX_DATE, new DateTimeGranularity(ChronoUnit.MILLIS)));
        assertEquals(expected, result);
    }

    @Test
    public void testReduceToFieldSpec_withNull_reducesToSpec() {
        FieldSpecRelations afterDateRelations = new AfterRelation(a, b, true, DateTimeDefaults.get());
        OffsetDateTime value = null;
        DataBagValue generatedValue = new DataBagValue(value);

        FieldSpec result = afterDateRelations.createModifierFromOtherValue(generatedValue);

        FieldSpec expected = FieldSpecFactory.fromType(FieldType.DATETIME);
        assertEquals(expected, result);
    }

}
