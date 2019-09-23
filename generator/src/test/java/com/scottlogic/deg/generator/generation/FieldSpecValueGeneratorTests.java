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

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.atomic.IsOfTypeConstraint.Types;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.whitelist.FrequencyDistributedSet;
import com.scottlogic.deg.generator.generation.databags.DataBagValue;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.restrictions.*;
import com.scottlogic.deg.generator.utils.JavaUtilRandomNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.deg.generator.config.detail.DataGenerationType.*;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.*;
import static com.scottlogic.deg.common.profile.FieldBuilder.createField;

class FieldSpecValueGeneratorTests {

    @Test
    void generate_fieldSpecMustContainRestrictionNullAndSetRestrictionsHasValues_returnsDataBagsWithValuesInSetRestrictions() {
        FieldSpec fieldSpec = FieldSpec.Empty.withNotNull()
            .withWhitelist(
                (FrequencyDistributedSet.uniform(
                    new HashSet<>(
                        Arrays.asList(10, 20, 30)))));
        FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
            INTERESTING,
            new StandardFieldValueSourceEvaluator(),
            new JavaUtilRandomNumberGenerator());

        final Set<DataBagValue> result = fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

        Set<DataBagValue> expectedDataBags = fieldSpec.getWhitelist().set()
            .stream()
            .map(DataBagValue::new)
            .collect(Collectors.toSet());

        assertThat(result, sameBeanAs(expectedDataBags));
    }

    @Test
    void generate_fieldSpecMustContainRestrictionNullAndNumericRestrictionApplied_returnsExpectedDataBagsForNumericRestriction() {
        FieldSpec fieldSpec = FieldSpec.Empty
            .withNumericRestrictions(
                new NumericRestrictions() {{
                    min = new NumericLimit<>(new BigDecimal(10), false);
                    max = new NumericLimit<>(new BigDecimal(30), false);
                }});
        FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
            INTERESTING,
            new StandardFieldValueSourceEvaluator(),
            new JavaUtilRandomNumberGenerator());

        final Set<DataBagValue> result =
            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

        Set<DataBagValue> expectedDataBags = new HashSet<>(
            Arrays.asList(
                new DataBagValue(
                    new BigDecimal("10.00000000000000000001")
                ),
                new DataBagValue(
                    new BigDecimal("10.00000000000000000002")
                ),
                new DataBagValue(
                    new BigDecimal("29.99999999999999999998")
                ),
                new DataBagValue(
                    new BigDecimal("29.99999999999999999999")
                ),
                new DataBagValue(null)
            )
        );

        assertThat(result, sameBeanAs(expectedDataBags));
    }

    @Nested
    class GetDataValuesTests {
        FieldValueSourceEvaluator fieldValueSourceEvaluator;
        FieldValueSource fieldValueSource;
        JavaUtilRandomNumberGenerator randomNumberGenerator;

        @BeforeEach
        void beforeEach() {
            fieldValueSourceEvaluator = mock(FieldValueSourceEvaluator.class);
            fieldValueSource = mock(FieldValueSource.class);

            randomNumberGenerator = mock(JavaUtilRandomNumberGenerator.class);

            List<FieldValueSource> list = new ArrayList<>();
            list.add(fieldValueSource);

            when(fieldValueSourceEvaluator.getFieldValueSources(any())).thenReturn(list);
            when(fieldValueSource.generateAllValues()).thenReturn(Stream.empty());
            when(fieldValueSource.generateInterestingValues()).thenReturn(Stream.empty());
            when(fieldValueSource.generateRandomValues(randomNumberGenerator)).thenReturn(Stream.empty());
        }

        @Test
        void generateRandom_uniqueFieldSpec_returnsAllValues() {
            FieldSpec fieldSpec = FieldSpec.Empty.withNotNull();

            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                RANDOM,
                fieldValueSourceEvaluator,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(new Field(null, Types.STRING, true, null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateInterestingValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateRandom_notUniqueFieldSpec_returnsRandomValues() {
            FieldSpec fieldSpec = FieldSpec.Empty.withNotNull();

            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                RANDOM,
                fieldValueSourceEvaluator,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(0)).generateAllValues();
            verify(fieldValueSource, times(0)).generateInterestingValues();
            verify(fieldValueSource, times(1)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateInteresting_uniqueFieldSpec_returnsAllValues() {
            FieldSpec fieldSpec = FieldSpec.Empty.withNotNull();

            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                INTERESTING,
                fieldValueSourceEvaluator,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(new Field(null, Types.STRING, true, null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateInterestingValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateInteresting_notUniqueFieldSpec_returnsInterestingValues() {
            FieldSpec fieldSpec = FieldSpec.Empty.withNotNull();

            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                INTERESTING,
                fieldValueSourceEvaluator,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(0)).generateAllValues();
            verify(fieldValueSource, times(1)).generateInterestingValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateSequential_uniqueFieldSpec_returnsAllValues() {
            FieldSpec fieldSpec = FieldSpec.Empty.withNotNull();

            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                FULL_SEQUENTIAL,
                fieldValueSourceEvaluator,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateInterestingValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateSequential_notUniqueFieldSpec_returnsAllValues() {
            FieldSpec fieldSpec = FieldSpec.Empty.withNotNull();

            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                FULL_SEQUENTIAL,
                fieldValueSourceEvaluator,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateInterestingValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }
    }
}
