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

package com.scottlogic.deg.generator.fieldspecs.whitelist;

import com.scottlogic.deg.generator.utils.RandomNumberGenerator;
import com.scottlogic.deg.generator.utils.SetUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistributedListTest {

    @Test
    public void testEmptyIsEmpty() {
        DistributedList<String> empty = DistributedList.empty();
        DistributedList<String> manualEmpty = new DistributedList<>(Collections.emptyList());

        assertEquals(manualEmpty, empty);
    }

    @Test
    public void testNullSetIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DistributedList<>(Collections.singletonList(null)));
    }

    @Test
    public void testUniformGeneratesUniformDistribution() {
        final double uniformWeight = 10.0D;
        WeightedElement<String> first = new WeightedElement<>("first", uniformWeight);
        WeightedElement<String> second = new WeightedElement<>("second", uniformWeight);
        WeightedElement<String> third = new WeightedElement<>("third", uniformWeight);

        List<WeightedElement<String>> weightedElements = SetUtils.ListOf(first, second, third);

        DistributedList<String> manualSet = new DistributedList<>(weightedElements);

        List<String> elements = SetUtils.ListOf("first", "second", "third");
        DistributedList<String> uniformSet = DistributedList.uniform(elements);

        assertEquals(manualSet, uniformSet);
    }

    private DistributedList<String> prepareTwoElementSet() {
        List<WeightedElement<String>> holders = Stream.of("first", "second", "third", "fourth")
            .map(WeightedElement::withDefaultWeight)
            .collect(Collectors.toList());
        return new DistributedList<>(holders);
    }

    @Test
    public void testRandomPick() {
        DistributedList<String> set = prepareTwoElementSet();

        String firstValue = set.pickRandomly(mockOfRandom(0.0D));
        String otherFirstValue = set.pickRandomly(mockOfRandom(0.24D));
        String secondValue = set.pickRandomly(mockOfRandom(0.25D));
        String otherSecondValue = set.pickRandomly(mockOfRandom(0.49D));
        String thirdValue = set.pickRandomly(mockOfRandom(0.5D));
        String otherThirdValue = set.pickRandomly(mockOfRandom(0.74D));
        String fourthValue = set.pickRandomly(mockOfRandom(0.75D));
        String otherFourthValue = set.pickRandomly(mockOfRandom(0.99D));

        assertEquals(firstValue, otherFirstValue);
        assertEquals(secondValue, otherSecondValue);
        assertEquals(thirdValue, otherThirdValue);
        assertEquals(fourthValue, otherFourthValue);
    }

    private static RandomNumberGenerator mockOfRandom(double value) {
        RandomNumberGenerator generator = mock(RandomNumberGenerator.class);
        when(generator.nextDouble(0.0D, 1.0D)).thenReturn(value);
        return generator;
    }
}
