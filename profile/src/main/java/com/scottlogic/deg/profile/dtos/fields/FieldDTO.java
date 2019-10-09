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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scottlogic.deg.common.profile.DataType;
import com.scottlogic.deg.profile.InvalidProfileException;

import java.io.IOException;

@JsonDeserialize(using = FieldDTO.FieldDeserializer.class)
public class FieldDTO
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

    static class FieldDeserializer extends JsonDeserializer<FieldDTO>
    {
        @Override
        public FieldDTO deserialize(JsonParser p, DeserializationContext context) throws IOException
        {
            ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
            ObjectNode object = objectMapper.readTree(p);
            switch (DataType.fromName(object.get("type").asText()))
            {
                case DECIMAL:
                    return objectMapper.treeToValue(object, DecimalFieldDTO.class);
                case INTEGER:
                    return objectMapper.treeToValue(object, IntegerFieldDTO.class);
                case ISIN:
                    return objectMapper.treeToValue(object, ISINFieldDTO.class);
                case SEDOL:
                    return objectMapper.treeToValue(object, SEDOLFieldDTO.class);
                case CUSIP:
                    return objectMapper.treeToValue(object, CUSIPFieldDTO.class);
                case RIC:
                    return objectMapper.treeToValue(object, RICFieldDTO.class);
                case FIRST_NAME:
                    return objectMapper.treeToValue(object, FirstNameFieldDTO.class);
                case LAST_NAME:
                    return objectMapper.treeToValue(object, LastNameFieldDTO.class);
                case FULL_NAME:
                    return objectMapper.treeToValue(object, FullNameFieldDTO.class);
                case STRING:
                    return objectMapper.treeToValue(object, StringFieldDTO.class);
                case DATE_TIME:
                    return objectMapper.treeToValue(object, DateTimeFieldDTO.class);
                default:
                    throw new InvalidProfileException("Unexpected data type: " + DataType.fromName(object.get("type").asText()));
            }
        }
    }
}
