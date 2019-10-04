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

package com.scottlogic.deg.profile.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.scottlogic.deg.profile.services.UndefinedValue;

import java.util.Collection;

@JsonPropertyOrder({ "field", "is", "value", "file", "key", "if", "then", "else" })
public class ConstraintDTO
{
    public static final Object undefined = new UndefinedValue();
    public Object is = undefined;
    public String field;
    public Object value;
    public String otherField;
    public Integer offset;
    public String offsetUnit;
    public Collection<Object> values;
    public String file;
    public String key;
    public ConstraintDTO not;
    public Collection<ConstraintDTO> anyOf;
    public Collection<ConstraintDTO> allOf;
    @JsonProperty("if")
    public ConstraintDTO if_;
    public ConstraintDTO then;
    @JsonProperty("else")
    public ConstraintDTO else_;

    @Override
    public String toString() {
        return "ConstraintDTO{" +
            "is=" + is +
            ", field='" + field + '\'' +
            ", value=" + value +
            ", values=" + values +
            ", not=" + not +
            ", anyOf=" + anyOf +
            ", allOf=" + allOf +
            ", if_=" + if_ +
            ", then=" + then +
            ", else_=" + else_ +
            '}';
    }
}
