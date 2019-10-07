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
import com.scottlogic.deg.profile.dtos.constraints.PredicateConstraintDTO;
import com.scottlogic.deg.common.profile.ConstraintType;
import com.scottlogic.deg.profile.dtos.constraints.GrammaticalConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.EqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.general.NullConstraintDTO;
import com.scottlogic.deg.profile.dtos.fields.FieldDTO;
import com.scottlogic.deg.profile.reader.atomic.ConstraintFactory;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


public class ConstraintReader
{
    private final ConstraintFactory constraintFactory;

    @Inject
    public ConstraintReader(ConstraintFactory constraintFactory)
    {
        this.constraintFactory = constraintFactory;
    }

    Set<Constraint> readMany(Collection<ConstraintDTO> constraints, ProfileFields fields)
    {
        return constraints.stream()
                .map(subConstraintDto -> read(subConstraintDto, fields))
                .filter(constraint -> !(constraint instanceof RemoveFromTree))
                .collect(Collectors.toSet());
    }

    Constraint readNullConstraint(FieldDTO fieldDTO, ProfileFields profileFields){
        return constraintFactory.create(new NullConstraintDTO(), profileFields.getByName(fieldDTO.name)).negate();
    }

    public Constraint read(ConstraintDTO dto, ProfileFields fields)
    {
        if (dto == null) throw new InvalidProfileException("Constraint is null");
        ConstraintType dtoType = dto.getType();
        if (dtoType == ConstraintType.GRAMMATICAL) return readGrammatical((GrammaticalConstraintDTO) dto, fields);
        PredicateConstraintDTO predicateConstraintDTO = (PredicateConstraintDTO) dto;
        if (predicateConstraintDTO.hasDependency()) return readDelayed((EqualToConstraintDTO) dto, fields);
        Field field = fields.getByName(predicateConstraintDTO.field);
        return constraintFactory.create(predicateConstraintDTO, field);
    }

    private Constraint readGrammatical(GrammaticalConstraintDTO dto, ProfileFields fields)
    {
        if (dto.not != null) return read(dto.not, fields).negate();
        if (dto.allOf != null) return new AndConstraint(readMany(dto.allOf, fields));
        if (dto.anyOf != null) return new OrConstraint(readMany(dto.anyOf, fields));
        if (dto.if_ != null && dto.then != null)
        {
            Constraint ifConstraint = read(dto.if_, fields);
            Constraint thenConstraint = read(dto.then, fields);
            Constraint elseConstraint = dto.else_ == null ? null : read(dto.else_, fields);
            return new ConditionalConstraint(ifConstraint, thenConstraint, elseConstraint);
        }
        throw new InvalidProfileException("Couldn't interpret constraint");
    }

    private DelayedAtomicConstraint readDelayed(EqualToConstraintDTO dto, ProfileFields fields)
    {
        return new DelayedDateAtomicConstraint(
                fields.getByName(dto.field),
                AtomicConstraintType.IS_EQUAL_TO_CONSTANT,
                fields.getByName(dto.otherField),
                getOffsetUnit(dto),
                dto.offset);
    }

    private TemporalAdjusterGenerator getOffsetUnit(EqualToConstraintDTO dto)
    {
        String offsetUnitUpperCase = dto.offsetUnit.toUpperCase();
        boolean workingDay = offsetUnitUpperCase.equals("WORKING DAYS");
        return new TemporalAdjusterGenerator(Enum.valueOf(ChronoUnit.class, workingDay ? "DAYS" : offsetUnitUpperCase), workingDay);
    }
}
