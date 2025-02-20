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

package com.scottlogic.deg.orchestrator.violate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType;
import com.scottlogic.deg.generator.generation.GenerationConfigSource;
import com.scottlogic.deg.generator.violations.filters.ConstraintTypeViolationFilter;
import com.scottlogic.deg.generator.violations.filters.ViolationFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ViolationFiltersProvider implements Provider<List<ViolationFilter>> {
    private final ViolateConfigSource commandLine;
    private final AtomicConstraintTypeMapper mapper;

    @Inject
    public ViolationFiltersProvider(ViolateConfigSource commandLine, AtomicConstraintTypeMapper mapper) {
        this.commandLine = commandLine;
        this.mapper = mapper;
    }

    @Override
    public List<ViolationFilter> get() {
        if (commandLine.getConstraintsToNotViolate() == null){
            return Collections.emptyList();
        }

        return commandLine.getConstraintsToNotViolate().stream()
            .filter(atomicConstraint -> !atomicConstraint.equals(AtomicConstraintType.IS_OF_TYPE))
            .map(mapper::toConstraintClass)
            .map(ConstraintTypeViolationFilter::new)
            .collect(Collectors.toList());
    }

}
