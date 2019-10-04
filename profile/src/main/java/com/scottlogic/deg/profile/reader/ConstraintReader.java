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

package com.scottlogic.deg.profile.reader;

import com.google.inject.Inject;
import com.scottlogic.deg.common.date.TemporalAdjusterGenerator;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.Constraint;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.common.profile.constraints.delayed.DelayedAtomicConstraint;
import com.scottlogic.deg.common.profile.constraints.delayed.DelayedDateAtomicConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.OrConstraint;
import com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintType;
import com.scottlogic.deg.profile.dtos.constraints.GrammaticalConstraintDTO;
import com.scottlogic.deg.profile.reader.atomic.AtomicConstraintValueReader;
import com.scottlogic.deg.profile.reader.atomic.AtomicConstraintFactory;
import com.scottlogic.deg.profile.reader.atomic.ConstraintValueValidator;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstraintReader
{
    private final AtomicConstraintValueReader atomicConstraintValueReader;

    @Inject
    public ConstraintReader(AtomicConstraintValueReader atomicConstraintValueReader)
    {
        this.atomicConstraintValueReader = atomicConstraintValueReader;
    }

    Set<Constraint> getSubConstraints(ProfileFields fields, Collection<ConstraintDTO> constraints)
    {
        return constraints.stream()
                .map(subConstraintDto -> apply(subConstraintDto, fields))
                .filter(constraint -> !(constraint instanceof RemoveFromTree))
                .collect(Collectors.toSet());
    }

    public Constraint apply(ConstraintDTO dto, ProfileFields fields)
    {
        if (dto == null) throw new InvalidProfileException("Constraint is null");
        ConstraintType dtoType = dto.getType();
        if (dtoType == ConstraintType.GRAMMATICAL) return getGrammaticalSubConstraints((GrammaticalConstraintDTO) dto, fields);
        if (dto.hasDependency()) return createDelayedDateAtomicConstraint(dto, fields);
        Field field = fields.getByName(dto.field);
        Object value = atomicConstraintValueReader.getValue(dto, field.type);
        ConstraintValueValidator.validate(field, dtoType, value);
        return AtomicConstraintFactory.create(dtoType, field, value);
    }

    private Constraint getGrammaticalSubConstraints(GrammaticalConstraintDTO dto, ProfileFields fields)
    {
        if (dto.not != null) return this.apply(dto.not, fields).negate();

        if (dto.allOf != null)
        {
            if (dto.allOf.isEmpty())
            {
                throw new InvalidProfileException("AllOf must contain at least one constraint.");
                return new AndConstraint(getSubConstraints(fields, dto.allOf));
            }

            if (dto.anyOf != null) return new OrConstraint(getSubConstraints(fields, dto.anyOf));
            if (dto.if_ != null)
            {
                return new ConditionalConstraint(
                        this.apply(dto.if_,fields),
                        this.apply(dto.then, fields),
                        dto.else_ != null ? this.apply(dto.else_, fields) : null);
            }

            throw new InvalidProfileException("Couldn't interpret constraint");
        }
    }

    private DelayedAtomicConstraint createDelayedDateAtomicConstraint(ConstraintDTO dto, ProfileFields fields)
    {
        return new DelayedDateAtomicConstraint(
                fields.getByName(dto.field),
                AtomicConstraintType.fromText((String) dto.is),
                fields.getByName(dto.otherField),
                getOffsetUnit(dto),
                dto.offset);
    }

    private TemporalAdjusterGenerator getOffsetUnit(ConstraintDTO dto)
    {
        if (dto.offsetUnit == null)
        {
            return null;
        }
        String offsetUnitUpperCase = dto.offsetUnit.toUpperCase();
        boolean workingDay = offsetUnitUpperCase.equals("WORKING DAYS");
        return new TemporalAdjusterGenerator(
                ChronoUnit.valueOf(ChronoUnit.class, workingDay ? "DAYS" : offsetUnitUpperCase),
                workingDay);
    }
}
