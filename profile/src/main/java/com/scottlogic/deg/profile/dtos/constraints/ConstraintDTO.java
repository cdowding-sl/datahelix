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

package com.scottlogic.deg.profile.dtos.constraints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.scottlogic.deg.profile.dtos.constraints.chronological.*;
import com.scottlogic.deg.profile.dtos.constraints.general.*;
import com.scottlogic.deg.profile.dtos.constraints.numerical.*;
import com.scottlogic.deg.profile.dtos.constraints.textual.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "is")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EqualToConstraintDTO.class, name = "equalTo"),
        @JsonSubTypes.Type(value = InSetConstraintDTO.class, name = "inSet"),
        @JsonSubTypes.Type(value = NullConstraintDTO.class, name = "null"),
        @JsonSubTypes.Type(value = GranularToConstraintDTO.class, name = "granularTo"),
        @JsonSubTypes.Type(value = MatchingRegexConstraintDTO.class, name = "matchingRegex"),
        @JsonSubTypes.Type(value = ContainingRegexConstraintDTO.class, name = "containingRegex"),
        @JsonSubTypes.Type(value = OfLengthConstraintDTO.class, name = "ofLength"),
        @JsonSubTypes.Type(value = LongerThanConstraintDTO.class, name = "longerThan"),
        @JsonSubTypes.Type(value = ShorterThanConstraintDTO.class, name = "shorterThan"),
        @JsonSubTypes.Type(value = GreaterThanConstraintDTO.class, name = "greaterThan"),
        @JsonSubTypes.Type(value = GreaterThanOrEqualToConstraintDTO.class, name = "greaterThanOrEqualTo"),
        @JsonSubTypes.Type(value = LessThanConstraintDTO.class, name = "lessThan"),
        @JsonSubTypes.Type(value = LessThanOrEqualToConstraintDTO.class, name = "lessThanOrEqualTo"),
        @JsonSubTypes.Type(value = AfterConstraintDTO.class, name = "after"),
        @JsonSubTypes.Type(value = AfterOrAtConstraintDTO.class, name = "afterOrAt"),
        @JsonSubTypes.Type(value = BeforeConstraintDTO.class, name = "before"),
        @JsonSubTypes.Type(value = BeforeOrAtConstraintDTO.class, name = "beforeOrAt"),
})
public abstract class ConstraintDTO
{
    private final ConstraintType type;
    public String field;

    protected ConstraintDTO(ConstraintType type)
    {
        this.type = type;
    }

    public ConstraintType getType()
    {
        return type;
    }

    public abstract boolean hasDependency();
}
