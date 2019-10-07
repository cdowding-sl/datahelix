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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.scottlogic.deg.common.profile.ConstraintType;

import java.util.Collection;

@JsonDeserialize(as = GrammaticalConstraintDTO.class)
public class GrammaticalConstraintDTO extends ConstraintDTO
{
    public PredicateConstraintDTO not;
    public Collection<ConstraintDTO> anyOf;
    public Collection<ConstraintDTO> allOf;
    @JsonProperty("if")
    public ConstraintDTO if_;
    public ConstraintDTO then;
    @JsonProperty("else")
    public ConstraintDTO else_;

    public GrammaticalConstraintDTO()
    {
        super(ConstraintType.GRAMMATICAL);
    }
}
