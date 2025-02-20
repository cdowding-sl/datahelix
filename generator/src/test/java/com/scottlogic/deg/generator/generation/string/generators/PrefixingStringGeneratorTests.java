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

package com.scottlogic.deg.generator.generation.string.generators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrefixingStringGeneratorTests {
    @Test
    void matches_withInvalidPrefix_returnsFalse() {
        String testPrefix = "BB";
        String testSuffix = "penguins";
        StringGenerator mockGenerator = mock(StringGenerator.class);
        when(mockGenerator.matches(testSuffix)).thenReturn(true);
        PrefixingStringGenerator prefixingStringGenerator = new PrefixingStringGenerator("AA", mockGenerator);
        assertFalse(prefixingStringGenerator.matches(testPrefix + testSuffix));
    }

    @Test
    void matches_withInvalidSuffix_returnsFalse() {
        String testPrefix = "AA";
        String testSuffix = "penguins";
        StringGenerator mockGenerator = mock(StringGenerator.class);
        when(mockGenerator.matches(testSuffix)).thenReturn(false);
        PrefixingStringGenerator prefixingStringGenerator = new PrefixingStringGenerator("AA", mockGenerator);
        assertFalse(prefixingStringGenerator.matches(testPrefix + testSuffix));
    }

    @Test
    void matches_withValidString_returnsTrue() {
        String testPrefix = "AA";
        String testSuffix = "penguins";
        StringGenerator mockGenerator = mock(StringGenerator.class);
        when(mockGenerator.matches(testSuffix)).thenReturn(true);
        PrefixingStringGenerator prefixingStringGenerator = new PrefixingStringGenerator("AA", mockGenerator);
        assertTrue(prefixingStringGenerator.matches(testPrefix + testSuffix));
    }
}
