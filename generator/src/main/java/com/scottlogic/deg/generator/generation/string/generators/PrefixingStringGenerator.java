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

import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.generator.utils.RandomNumberGenerator;

import java.util.stream.Stream;

public class PrefixingStringGenerator implements StringGenerator {

    private final String prefix;
    private final StringGenerator innerGenerator;

    public PrefixingStringGenerator(String prefix, StringGenerator innerGenerator) {
        this.prefix = prefix;
        this.innerGenerator = innerGenerator;
    }

    @Override
    public Stream<String> generateAllValues() {
        return innerGenerator.generateAllValues()
            .map(string -> prefix + string);
    }

    @Override
    public Stream<String> generateRandomValues(RandomNumberGenerator randomNumberGenerator) {
        return innerGenerator.generateRandomValues(randomNumberGenerator)
            .map(string -> prefix + string);
    }

    @Override
    public Stream<String> generateInterestingValues() {
        return innerGenerator.generateInterestingValues()
            .map(string -> prefix + string);
    }

    @Override
    public boolean matches(String string) {
        if (string.length() < prefix.length()) {
            return false;
        }
        String candidatePrefix = string.substring(0, prefix.length());
        if (!candidatePrefix.equals(prefix)) {
            return false;
        }
        return innerGenerator.matches(string.substring(prefix.length()));
    }

    @Override
    public StringGenerator intersect(StringGenerator stringGenerator) {
        throw new ValidationException("The prefixing generator cannot be combined ");
    }
}
