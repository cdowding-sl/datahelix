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
import com.scottlogic.deg.common.profile.ConstraintType;
import com.scottlogic.deg.profile.dtos.constraints.chronological.AfterConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.AfterOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.BeforeConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.chronological.BeforeOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.*;
import com.scottlogic.deg.profile.dtos.constraints.numerical.GreaterThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.GreaterThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.LessThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.numerical.LessThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.textual.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "is")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EqualToConstraintDTO.class, name = "equalTo"),
        @JsonSubTypes.Type(value = InSetConstraintDTO.class, name = "inSet"),
        @JsonSubTypes.Type(value = InMapConstraintDTO.class, name = "inMap"),
        @JsonSubTypes.Type(value = NullConstraintDTO.class, name = "null"),
        @JsonSubTypes.Type(value = GranularToConstraintDTO.class, name = "granularTo"),
        @JsonSubTypes.Type(value = MatchesRegexConstraintDTO.class, name = "matchingRegex"),
        @JsonSubTypes.Type(value = ContainsRegexConstraintDTO.class, name = "containingRegex"),
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
        @JsonSubTypes.Type(value = BeforeOrAtConstraintDTO.class, name = "beforeOrAt")
})
public abstract class PredicateConstraintDTO extends ConstraintDTO
{
    public String is;
    public String field;

    protected PredicateConstraintDTO(ConstraintType type)
    {
        super(type);
    }

    public abstract boolean hasDependency();
}
