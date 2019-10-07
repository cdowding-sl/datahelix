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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scottlogic.deg.common.profile.ConstraintType;

import java.io.IOException;

@JsonDeserialize(using = ConstraintDTO.ConstraintDeserializer.class)
public abstract class ConstraintDTO
{
    private final ConstraintType type;

    ConstraintDTO(ConstraintType type)
    {
        this.type = type;
    }

    public ConstraintType getType()
    {
        return type;
    }

    static class ConstraintDeserializer extends JsonDeserializer<ConstraintDTO>
    {
        @Override
        public ConstraintDTO deserialize(JsonParser p, DeserializationContext context) throws IOException
        {
            ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
            ObjectNode object = objectMapper.readTree(p);
            return object.has("is")
                    ? objectMapper.readerFor(PredicateConstraintDTO.class).readValue(object.toString())
                    : objectMapper.treeToValue(object, GrammaticalConstraintDTO.class);
        }
    }
}
