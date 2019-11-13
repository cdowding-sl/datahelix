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

package com.scottlogic.datahelix.generator.ui;

import com.scottlogic.datahelix.generator.ui.dtos.*;

import java.util.Arrays;

public enum ConstraintType
{
    ALL_OF(AllOfDTO.class),
    ANY_OF(AnyOfDTO.class),
    CONDITIONAL(ConditionalDTO.class),
    CONTAINS_REGEX(ContainsRegexDTO.class),
    EQUAL_TO(EqualToDTO.class),
    GENERATOR(GeneratorDTO.class),
    GRANULAR_TO(GranularToDTO.class),
    GREATER_THAN(GreaterThanDTO.class),
    IN_FILE(InFileDTO.class),
    IN_MAP(InMapDTO.class),
    IN_SET(InSetDTO.class),
    IS_NULL(IsNullDTO.class),
    LESS_THAN(LessThanDTO.class),
    LONGER_THAN(LongerThanDTO.class),
    MATCHES_REGEX(MatchesRegexDTO.class),
    NOT(NotDTO.class),
    OF_LENGTH(OfLengthDTO.class),
    SHORTER_THAN(ShorterThanDTO.class);

    public final Class<? extends ConstraintDTO> clazz;

    ConstraintType(Class<? extends ConstraintDTO> clazz)
    {
        this.clazz = clazz;
    }

    public static ConstraintType fromClass(Class<? extends ConstraintDTO> clazz)
    {
        return Arrays.stream(values()).filter(x -> x.clazz.equals(clazz)).findFirst().orElse(null);
    }
}

