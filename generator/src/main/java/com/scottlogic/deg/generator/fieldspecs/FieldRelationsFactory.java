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


package com.scottlogic.deg.generator.fieldspecs;

import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.common.profile.constraints.delayed.*;
import com.scottlogic.deg.generator.fieldspecs.relations.*;

public class FieldRelationsFactory {

   public FieldSpecRelations construct(DelayedAtomicConstraint constraint) {
       if (constraint instanceof DynamicNotConstraint) {
           throw new ValidationException("related field constraints cannot yet be negated");
       }

       switch (constraint.getUnderlyingConstraint()) {
           case IS_EQUAL_TO_CONSTANT:
               return constructEqualToDate((DelayedDateAtomicConstraint) constraint);
           case IS_BEFORE_CONSTANT_DATE_TIME:
               return constructBeforeDate((DelayedDateAtomicConstraint) constraint, false);
           case IS_BEFORE_OR_EQUAL_TO_CONSTANT_DATE_TIME:
               return constructBeforeDate((DelayedDateAtomicConstraint) constraint, true);
           case IS_AFTER_CONSTANT_DATE_TIME:
               return constructAfterDate((DelayedDateAtomicConstraint) constraint, false);
           case IS_AFTER_OR_EQUAL_TO_CONSTANT_DATE_TIME:
               return constructAfterDate((DelayedDateAtomicConstraint) constraint, true);
       }

       throw new IllegalArgumentException("Unsupported field spec relations: " + constraint.getUnderlyingConstraint());

   }

   private FieldSpecRelations constructBeforeDate(DelayedDateAtomicConstraint constraint, boolean inclusive) {
       return new BeforeDateRelation(
           constraint.getField(),
           constraint.getOtherField(),
           inclusive);
   }

   private FieldSpecRelations constructAfterDate(DelayedDateAtomicConstraint constraint, boolean inclusive) {
       return new AfterDateRelation(
           constraint.getField(),
           constraint.getOtherField(),
           inclusive);
   }

   private FieldSpecRelations constructEqualToDate(DelayedDateAtomicConstraint constraint) {
       if (constraint.getOffsetUnit() != null) {
           return new EqualToOffsetDateRelation(
               constraint.getField(),
               constraint.getOtherField(),
               constraint.getOffsetGenerator(),
               constraint.getOffsetUnit());
       } else {
           return new EqualToDateRelation(
               constraint.getField(),
               constraint.getOtherField());
       }
   }
}
