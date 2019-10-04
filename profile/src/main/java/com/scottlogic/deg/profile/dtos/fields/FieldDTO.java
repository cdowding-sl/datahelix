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

package com.scottlogic.deg.profile.dtos.fields;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.scottlogic.deg.common.profile.DataType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumericalFieldDTO.class, name = "decimal"),
        @JsonSubTypes.Type(value = NumericalFieldDTO.class, name = "integer"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "ISIN"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "SEDOL"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "CUSIP"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "RIC"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "firstname"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "lastname"),
        @JsonSubTypes.Type(value = TextualFieldDTO.class, name = "fullname"),
        @JsonSubTypes.Type(value = DateTimeFieldDTO.class, name = "datetime")
})
public abstract class FieldDTO
{
    private final DataType dataType;
    public String name;
    public String formatting;
    public boolean unique;
    public boolean nullable = true;

    FieldDTO(DataType dataType)
    {
        this.dataType = dataType;
    }

    public DataType getDataType()
    {
        return dataType;
    }
}
