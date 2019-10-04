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

package com.scottlogic.deg.profile.dtos.constraints.general;

import com.scottlogic.deg.profile.dtos.constraints.ConstraintType;

public class EqualToConstraintDTO extends GeneralConstraintDTO
{
    public Object value;
    public String otherField;
    public int offset;
    public String offsetUnit;

    public EqualToConstraintDTO()
    {
        super(ConstraintType.EQUAL_TO);
    }

    @Override
    public boolean hasDependency()
    {
        return otherField != null;
    }
}
