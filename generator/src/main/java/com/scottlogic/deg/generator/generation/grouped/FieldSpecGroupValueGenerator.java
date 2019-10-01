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


package com.scottlogic.deg.generator.generation.grouped;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.util.FlatMappingSpliterator;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecGroup;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecMerger;
import com.scottlogic.deg.generator.fieldspecs.relations.FieldSpecRelations;
import com.scottlogic.deg.generator.fieldspecs.relations.InMapRelation;
import com.scottlogic.deg.generator.generation.FieldPair;
import com.scottlogic.deg.generator.generation.FieldSpecValueGenerator;
import com.scottlogic.deg.generator.generation.databags.*;
import com.scottlogic.deg.generator.restrictions.linear.Limit;
import com.scottlogic.deg.generator.restrictions.linear.LinearRestrictionsFactory;
import com.scottlogic.deg.generator.utils.SetUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.deg.generator.generation.grouped.FieldSpecGroupDateHelper.adjustBoundsOfDate;

public class FieldSpecGroupValueGenerator {

    private final FieldSpecValueGenerator underlyingGenerator;

    public FieldSpecGroupValueGenerator(FieldSpecValueGenerator underlyingGenerator) {
        this.underlyingGenerator = underlyingGenerator;
    }

    public Stream<DataBag> generate(FieldSpecGroup group) {
        if(group.relations().stream().allMatch(fieldSpecRelations -> fieldSpecRelations instanceof InMapRelation)) {
            return getInMapDataBagStream(group);
        } else {
            Field first = SetUtils.firstIteratorElement(group.fieldSpecs().keySet());

            FieldSpecGroup groupRespectingFirstField = initialAdjustments(first, group);
            FieldSpec firstSpec = groupRespectingFirstField.fieldSpecs().get(first);

            Stream<DataBag> firstDataBagValues = underlyingGenerator.generate(first, firstSpec)
                .map(value -> toDataBag(first, value));

            return createRemainingDataBags(firstDataBagValues, first, groupRespectingFirstField);
        }
    }

    private Stream<DataBag> getInMapDataBagStream(FieldSpecGroup group) {
        Set<Field> controllers = group.relations().stream()
            .map(FieldSpecRelations::main)
            .collect(Collectors.toSet());

        if(controllers.size() > 1) {
            throw new UnsupportedOperationException("related inMap contraints are not supported");
        }

        Field controller = (Field) controllers.toArray()[0];
        FieldSpec controllerSpec = getInMapControllerFieldSpec(controller, new ArrayList<>(group.relations()), group.fieldSpecs());

        //output data
        Stream<DataBag> index = underlyingGenerator.generate(controller, controllerSpec)
            .map(value -> toDataBag(controller, value));

        // TODO work out how nullable is meant to work here
        return index.map(indexes ->
            group.relations().stream()
                .collect(Collectors.toMap(
                        FieldSpecRelations::other,
                        fieldSpecRelations ->
                            new DataBagValue(
                                ((InMapRelation) fieldSpecRelations)
                                    .getUnderlyingList()
                                    .distributedSet()
                                    .get(((BigDecimal) indexes.getDataBagValue(controller).getValue()).intValue())
                                    .element())
                       ))
            ).map(DataBag::new);
    }

    private FieldSpec getInMapControllerFieldSpec(Field controller, ArrayList<FieldSpecRelations> relations, Map<Field, FieldSpec> fieldSpecMap) {
        FieldSpec controllerSpec = fieldSpecMap.get(controller);

        int setSize = ((InMapRelation)relations.get(0)).getUnderlyingList().distributedSet().size();

        controllerSpec = controllerSpec.withRestrictions(LinearRestrictionsFactory.createNumericRestrictions(
            new Limit<>(new BigDecimal(0), true),
            new Limit<>(new BigDecimal(setSize), false),
            0
        ));

        for (FieldSpecRelations relation: relations) {
            InMapRelation rel = (InMapRelation)relation;
            FieldSpec testing = fieldSpecMap.get(rel.other());
            for (int i = 0; i < setSize; i++) {
                if (controllerSpec.getBlacklist().contains(new BigDecimal(i))) {
                    continue;
                }
                String testingElement = rel.getUnderlyingList().distributedSet().get(i).element();
                if (!testing.permits(testingElement)) {
                    Set<Object> newBlackList = new HashSet<>(controllerSpec.getBlacklist());
                    newBlackList.add(new BigDecimal(i));
                    controllerSpec = controllerSpec.withBlacklist(newBlackList);
                }
            }
        }
        return controllerSpec;
    }

    private static DataBag toDataBag(Field field, DataBagValue value) {
        Map<Field, DataBagValue> map = new HashMap<>();
        map.put(field, value);
        return new DataBag(map);
    }

    private static FieldSpecGroup initialAdjustments(Field first, FieldSpecGroup group) {
        checkOnlyPairwiseRelationsExist(group.relations());

        Map<Field, FieldSpec> mutatingSpecs = new HashMap<>(group.fieldSpecs());

        for (FieldSpecRelations relation : group.relations()) {
            FieldSpec merged = createMergedSpecFromRelation(first, relation, group)
                .orElseThrow(() -> new IllegalStateException("Failed to merge field specs in related fields"));
            mutatingSpecs.replace(first, merged);
        }

        return new FieldSpecGroup(mutatingSpecs, group.relations());
    }

    private static Optional<FieldSpec> createMergedSpecFromRelation(Field first,
                                                                    FieldSpecRelations relation,
                                                                    FieldSpecGroup group) {
        Field other = relation.main().equals(first) ? relation.other() : relation.main();

        FieldSpecMerger merger = new FieldSpecMerger();

        FieldSpec reduced = relation.inverse().reduceToRelatedFieldSpec(group.fieldSpecs().get(other));
        return merger.merge(reduced, group.fieldSpecs().get(first));
    }

    private static void checkOnlyPairwiseRelationsExist(Collection<FieldSpecRelations> relations) {
        Set<FieldPair> pairs = new HashSet<>();
        Set<Field> usedFields = new HashSet<>();
        for (FieldSpecRelations relation : relations) {
            FieldPair pair = new FieldPair(relation.main(), relation.other());
            if (!pairs.contains(pair) &&
                (usedFields.contains(relation.main()) || usedFields.contains(relation.other()))) {
                throw new UnsupportedOperationException("Using more than two fields in a related dependency"
                    + " is currently unsupported.");
            }
            pairs.add(pair);
            usedFields.add(relation.main());
            usedFields.add(relation.other());
        }
    }

    private static FieldSpecGroup adjustBounds(Field field, DataBagValue value, FieldSpecGroup group) {
        Object object = value.getValue();

        if (object instanceof OffsetDateTime) {
            return adjustBoundsOfDate(field, (OffsetDateTime) object, group);
        }

        return group;
    }


    private Stream<DataBag> createRemainingDataBags(Stream<DataBag> stream, Field field, FieldSpecGroup group) {
        Stream<DataBagGroupWrapper> initial = stream
            .map(dataBag -> new DataBagGroupWrapper(dataBag, group, underlyingGenerator))
            .map(wrapper -> adjustWrapperBounds(wrapper, field));
        Set<Field> toProcess = filterFromSet(group.fieldSpecs().keySet(), field);

        Stream<DataBagGroupWrapper> wrappedStream = recursiveMap(initial, toProcess);

        return wrappedStream.map(DataBagGroupWrapper::dataBag);
    }

    private static DataBagGroupWrapper adjustWrapperBounds(DataBagGroupWrapper wrapper, Field field) {
        DataBagValue value = wrapper.dataBag().getDataBagValue(field);
        FieldSpecGroup newGroup = adjustBounds(field, value, wrapper.group());
        return new DataBagGroupWrapper(wrapper.dataBag(), newGroup, wrapper.generator());

    }

    private static Stream<DataBagGroupWrapper> recursiveMap(Stream<DataBagGroupWrapper> wrapperStream,
                                                            Set<Field> fieldsToProcess) {
        if (fieldsToProcess.isEmpty()) {
            return wrapperStream;
        }

        Field field = SetUtils.firstIteratorElement(fieldsToProcess);

        Stream<DataBagGroupWrapper> mappedStream =
            FlatMappingSpliterator.flatMap(wrapperStream, wrapper -> acceptNextValue(wrapper, field));

        Set<Field> remainingFields = filterFromSet(fieldsToProcess, field);

        return recursiveMap(mappedStream, remainingFields);
    }

    private static <T> Set<T> filterFromSet(Set<T> original, T element) {
        return original.stream()
            .filter(f -> !f.equals(element))
            .collect(Collectors.toSet());
    }

    private static Stream<DataBagGroupWrapper> acceptNextValue(DataBagGroupWrapper wrapper, Field field) {
        if (wrapper.generator().isRandom()) {
            return Stream.of(acceptNextRandomValue(wrapper, field));
        } else {
            return acceptNextNonRandomValue(wrapper, field);
        }
    }

    private static DataBagGroupWrapper acceptNextRandomValue(DataBagGroupWrapper wrapper, Field field) {
        FieldSpecGroup group = wrapper.group();

        DataBagValue nextValue = wrapper.generator().generate(field, group.fieldSpecs().get(field)).findFirst().get();

        DataBag combined = DataBag.merge(toDataBag(field, nextValue), wrapper.dataBag());

        FieldSpecGroup newGroup = adjustBounds(field, nextValue, group);

        return new DataBagGroupWrapper(combined, newGroup, wrapper.generator());
    }

    private static Stream<DataBagGroupWrapper> acceptNextNonRandomValue(DataBagGroupWrapper wrapper, Field field) {
        return wrapper.generator()
            .generate(field, wrapper.group().fieldSpecs().get(field))
            .map(value -> getWrappedDataBag(wrapper, field, value));
    }

    private static DataBagGroupWrapper getWrappedDataBag(DataBagGroupWrapper wrapper, Field field, DataBagValue value) {
        DataBag dataBag = toDataBag(field, value);
        DataBag merged = DataBag.merge(dataBag, wrapper.dataBag());

        return new DataBagGroupWrapper(merged, adjustBounds(field, value, wrapper.group()), wrapper.generator());
    }
}
