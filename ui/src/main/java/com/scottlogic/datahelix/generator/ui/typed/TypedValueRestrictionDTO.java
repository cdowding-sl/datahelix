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
package com.scottlogic.datahelix.generator.ui.typed;

import com.scottlogic.datahelix.generator.ui.ConstraintType;
import com.scottlogic.deg.common.profile.FieldType;

public class TypedValueRestrictionDTO<T> extends TypedRestrictionDTO
{
    public final T value;

    public TypedValueRestrictionDTO(String field, FieldType fieldType, ConstraintType type, T value)
    {
        super(field, fieldType, type);
        this.value = value;
    }
}
