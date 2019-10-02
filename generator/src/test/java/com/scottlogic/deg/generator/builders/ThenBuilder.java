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

package com.scottlogic.deg.generator.builders;

import com.scottlogic.deg.generator.profile.constraints.Constraint;

public class ThenBuilder {
    private final Constraint ifCondition;

    ThenBuilder(BaseConstraintBuilder<? extends Constraint> builder) {
        this.ifCondition = builder.build();
    }

    private ThenBuilder(Constraint ifCondition) {
        this.ifCondition = ifCondition;
    }

    public ElseBuilder withThen(BaseConstraintBuilder<? extends Constraint> builder) {
        return new ElseBuilder(ifCondition, builder);
    }

    public ThenBuilder negate() {
        return new ThenBuilder(ifCondition.negate());
    }
}
