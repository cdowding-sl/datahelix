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

public abstract class TypedRestrictionDTO extends TypedConstraintDTO
{
    public final String field;
    public final FieldType fieldType;
    public final ConstraintType type;

    protected TypedRestrictionDTO(String field, FieldType fieldType, ConstraintType type)
    {
        this.field = field;
        this.fieldType = fieldType;
        this.type = type;
    }
}
