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

package com.scottlogic.deg.profile.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collection;

@JsonDeserialize(using = RuleDeserializer.class)
@JsonSerialize(using = RuleSerializer.class)
public class RuleDTO {
    public String rule;
    public Collection<ConstraintDTO> constraints;

    public RuleDTO() {}

    public RuleDTO(String rule, Collection<ConstraintDTO> constraints){
        this.rule = rule;
        this.constraints = constraints;
    }
}
