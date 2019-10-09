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

package com.scottlogic.deg.orchestrator.cucumber.testframework.steps;

import com.fasterxml.jackson.core.JsonParseException;
import com.scottlogic.deg.generator.config.detail.CombinationStrategyType;
import com.scottlogic.deg.generator.config.detail.DataGenerationType;
import com.scottlogic.deg.orchestrator.cucumber.testframework.utils.CucumberGenerationMode;
import com.scottlogic.deg.orchestrator.cucumber.testframework.utils.GeneratorTestUtilities;
import com.scottlogic.deg.profile.InvalidProfileException;
import com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType;
import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.Transformer;
import io.cucumber.datatable.TableCellByTypeTransformer;

import java.util.*;
import java.util.stream.Collectors;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    private final Set<AtomicConstraintType> allOperators = new HashSet<>(Arrays.asList(AtomicConstraintType.values()));

    @Override
    public Locale locale(){
        return Locale.ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry tr) {
        this.defineDataGenerationStrategyType(tr);
        this.defineCombinationStrategyType(tr);
        this.defineOperationParameterType(tr);
        this.defineGenerationMode(tr);
        this.defineParameterType(tr,"fieldVar", "^(.+)");
        this.defineParameterType(tr,"regex", "/(.+)/$");
        tr.setDefaultDataTableCellTransformer(new DataTableCellTransformer());

        tr.defineParameterType(new ParameterType<>(
            "number",
            "([+-]?\\d+(\\.\\d+)?)",
            Number.class,
            (Transformer<Number>) value -> (Number) GeneratorTestUtilities.parseNumber(value)));

        tr.defineParameterType(new ParameterType<>(
            "boolean",
            "(true|false)$",
            Boolean.class,
            (Transformer<Boolean>) Boolean::valueOf));

        tr.defineParameterType(new ParameterType<>(
            "date",
            DateValueStep.DATE_REGEX,
            String.class,
                (Transformer<String>)value -> value));
    }

    private void defineOperationParameterType(TypeRegistry tr){
        tr.defineParameterType(new ParameterType<>(
            "operator",
            this.getHumanReadableOperationRegex(allOperators),
            String.class,
            this::extractConstraint
        ));
    }

    private void defineParameterType(TypeRegistry tr, String name, String regex) {
        tr.defineParameterType(new ParameterType<>(
            name,
            regex,
            String.class,
            (Transformer<String>)fieldName -> fieldName));
    }

    private void defineDataGenerationStrategyType(TypeRegistry tr){
        Transformer<DataGenerationType> transformer = strategyString ->
            Arrays.stream(DataGenerationType.values())
            .filter(val -> val.toString().equalsIgnoreCase(strategyString))
            .findFirst().orElse(DataGenerationType.FULL_SEQUENTIAL);

        tr.defineParameterType(new ParameterType<>(
            "generationStrategy",
            "(.*)$",
            DataGenerationType.class,
            transformer));
    }

    private void defineCombinationStrategyType(TypeRegistry tr){
        Transformer<CombinationStrategyType> transformer = strategyString ->
            Arrays.stream(CombinationStrategyType.values())
                .filter(val -> val.toString().equalsIgnoreCase(strategyString))
                .findFirst().orElse(CombinationStrategyType.PINNING);

        tr.defineParameterType(new ParameterType<>(
            "combinationStrategy",
            "(.*)$",
            CombinationStrategyType.class,
            transformer));
    }

    private void defineGenerationMode(TypeRegistry tr) {
        Transformer<CucumberGenerationMode> transformer = strategyString ->
            Arrays.stream(CucumberGenerationMode.values())
                .filter(val -> val.toString().equalsIgnoreCase(strategyString))
                .findFirst().orElse(CucumberGenerationMode.VALIDATING);

        tr.defineParameterType(new ParameterType<>(
            "generationMode",
            "(.*)$",
            CucumberGenerationMode.class,
            transformer));
    }

    private String extractConstraint(String gherkinConstraint) {
        List<String> allConstraints = Arrays.asList(gherkinConstraint.split(" "));
        return allConstraints.get(0) + allConstraints
            .stream()
            .skip(1)
            .map(value -> value.substring(0, 1).toUpperCase() + value.substring(1))
            .collect(Collectors.joining());
    }

    private String getHumanReadableOperationRegex(Set<AtomicConstraintType> types){
        return
            types.stream()
            .map(act -> act.toString().replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase())
            .collect(Collectors.joining("|", "(", ")"));
    }

    private class DataTableCellTransformer implements TableCellByTypeTransformer {
        @Override
        public <T> T transform(String value, Class<T> aClass) throws Throwable {
            try {
                return aClass.cast(GeneratorTestUtilities.parseInput(value.trim()));
            } catch (JsonParseException | InvalidProfileException e) {
                return (T)e;
            }
        }
    }
}
