package com.scottlogic.datahelix.generator.ui.services;/*
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scottlogic.datahelix.generator.ui.ConstraintType;
import com.scottlogic.datahelix.generator.ui.dtos.ConstraintDTO;
import com.scottlogic.datahelix.generator.ui.dtos.InvalidDTO;

import java.io.IOException;

public class ConstraintDeserialiser extends JsonDeserializer<ConstraintDTO>
{
    @Override
    public ConstraintDTO deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException
    {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        ObjectNode node = mapper.readTree(jsonParser);
        for (ConstraintType type : ConstraintType.values())
        {
            try
            {
                return mapper.treeToValue(node, type.clazz);
            } catch (Exception ignored)
            {
                //ignored
            }
        }
        return new InvalidDTO("Cannot deserialise constraint | Invalid syntax: " + node.toString());
    }
}
