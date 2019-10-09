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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scottlogic.deg.common.profile.ConstraintType;
import com.scottlogic.deg.profile.InvalidProfileException;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.AllOfConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.AnyOfConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.ConditionalConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.NotConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.AfterConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.AfterOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.BeforeConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.chronological.BeforeOrAtConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.*;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.GreaterThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.GreaterThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.LessThanConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.numerical.LessThanOrEqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.textual.*;

import java.io.IOException;

@JsonSerialize(using = ConstraintDTO.ConstraintSerializer.class)
@JsonDeserialize(using = ConstraintDTO.ConstraintDeserializer.class)
public abstract class ConstraintDTO
{
    private final ConstraintType type;

    protected ConstraintDTO(ConstraintType type)
    {
        this.type = type;
    }

    public ConstraintType getType()
    {
        return type;
    }

    static class ConstraintSerializer extends JsonSerializer<ConstraintDTO>
    {
        @Override
        public void serialize(ConstraintDTO value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeStartObject(value);
            switch (value.getType())
            {
                case NOT:
                    gen.writeObjectField(value.getType().getName(), ((NotConstraintDTO)value).constraint);
                    break;
                case ANY_OF:
                    gen.writeObjectField(value.getType().getName(), ((AnyOfConstraintDTO)value).constraints);
                    break;
                case ALL_OF:
                    gen.writeObjectField(value.getType().getName(), ((AllOfConstraintDTO)value).constraints);
                    break;
                default:
                    gen.writeObjectField(value.getType().getName(), value);
            }
            gen.writeEndObject();
        }
    }
    static class ConstraintDeserializer extends JsonDeserializer<ConstraintDTO>
    {
        @Override
        public ConstraintDTO deserialize(JsonParser p, DeserializationContext context) throws IOException
        {
            ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
            ObjectNode object = objectMapper.readTree(p);

            if(object.has(ConstraintType.EQUAL_TO.getName())) return objectMapper.treeToValue(object.get(ConstraintType.EQUAL_TO.getName()), EqualToConstraintDTO.class);
            if(object.has(ConstraintType.IN_SET.getName())) return objectMapper.treeToValue(object.get(ConstraintType.IN_SET.getName()), InSetConstraintDTO.class);
            if(object.has(ConstraintType.IN_MAP.getName())) return objectMapper.treeToValue(object.get(ConstraintType.IN_MAP.getName()), InMapConstraintDTO.class);
            if(object.has(ConstraintType.NULL.getName())) return objectMapper.treeToValue(object.get(ConstraintType.NULL.getName()), NullConstraintDTO.class);
            if(object.has(ConstraintType.GRANULAR_TO.getName())) return objectMapper.treeToValue(object.get(ConstraintType.GRANULAR_TO.getName()), GranularToConstraintDTO.class);
            if(object.has(ConstraintType.MATCHES_REGEX.getName())) return objectMapper.treeToValue(object.get(ConstraintType.MATCHES_REGEX.getName()), MatchesRegexConstraintDTO.class);
            if(object.has(ConstraintType.CONTAINS_REGEX.getName())) return objectMapper.treeToValue(object.get(ConstraintType.CONTAINS_REGEX.getName()), ContainsRegexConstraintDTO.class);
            if(object.has(ConstraintType.OF_LENGTH.getName())) return objectMapper.treeToValue(object.get(ConstraintType.OF_LENGTH.getName()), OfLengthConstraintDTO.class);
            if(object.has(ConstraintType.LONGER_THAN.getName())) return objectMapper.treeToValue(object.get(ConstraintType.LONGER_THAN.getName()), LongerThanConstraintDTO.class);
            if(object.has(ConstraintType.SHORTER_THAN.getName())) return objectMapper.treeToValue(object.get(ConstraintType.SHORTER_THAN.getName()), ShorterThanConstraintDTO.class);
            if(object.has(ConstraintType.GREATER_THAN.getName())) return objectMapper.treeToValue(object.get(ConstraintType.GREATER_THAN.getName()), GreaterThanConstraintDTO.class);
            if(object.has(ConstraintType.GREATER_THAN_OR_EQUAL_TO.getName())) return objectMapper.treeToValue(object.get(ConstraintType.GREATER_THAN_OR_EQUAL_TO.getName()), GreaterThanOrEqualToConstraintDTO.class);
            if(object.has(ConstraintType.LESS_THAN.getName())) return objectMapper.treeToValue(object.get(ConstraintType.LESS_THAN.getName()), LessThanConstraintDTO.class);
            if(object.has(ConstraintType.LESS_THAN_OR_EQUAL_TO.getName())) return objectMapper.treeToValue(object.get(ConstraintType.LESS_THAN_OR_EQUAL_TO.getName()), LessThanOrEqualToConstraintDTO.class);
            if(object.has(ConstraintType.AFTER.getName())) return objectMapper.treeToValue(object.get(ConstraintType.AFTER.getName()), AfterConstraintDTO.class);
            if(object.has(ConstraintType.AFTER_OR_AT.getName())) return objectMapper.treeToValue(object.get(ConstraintType.AFTER_OR_AT.getName()), AfterOrAtConstraintDTO.class);
            if(object.has(ConstraintType.BEFORE.getName())) return objectMapper.treeToValue(object.get(ConstraintType.BEFORE.getName()), BeforeConstraintDTO.class);
            if(object.has(ConstraintType.BEFORE_OR_AT.getName())) return objectMapper.treeToValue(object.get(ConstraintType.BEFORE_OR_AT.getName()), BeforeOrAtConstraintDTO.class);
            if(object.has(ConstraintType.NOT.getName())) return objectMapper.treeToValue(object, NotConstraintDTO.class);
            if(object.has(ConstraintType.ANY_OF.getName())) return objectMapper.treeToValue(object, AnyOfConstraintDTO.class);
            if(object.has(ConstraintType.ALL_OF.getName())) return objectMapper.treeToValue(object, AllOfConstraintDTO.class);
            if(object.has(ConstraintType.CONDITION.getName())) return objectMapper.treeToValue(object.get(ConstraintType.CONDITION.getName()), ConditionalConstraintDTO.class);

            throw new InvalidProfileException("The constraint json object doesn't contain any of the expected keywords " + object);
        }
    }
}
