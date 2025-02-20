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

package com.scottlogic.deg.profile.dto;

import com.scottlogic.deg.common.ValidationException;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

import javax.json.stream.JsonParser;
import java.io.*;
import java.util.*;

/**
 * Used to validate a DataHelix Profile JSON file.
 * <p>
 * Checks that the profile JSON file is valid against the DataHelix Profile Schema (datahelix.schema.json)
 */
public class ProfileSchemaValidatorLeadPony implements ProfileSchemaValidator {
    private List<String> profileJsonLines;

    @Override
    public void validateProfile(String profile, String schema) {
        List<String> errorMessages = new ArrayList<>();

        profileJsonLines = Arrays.asList(profile.split(System.lineSeparator()));

        JsonValidationService service = JsonValidationService.newInstance();

        // Reads the JSON schema
        JsonSchema jsonSchema = service.readSchema(new ByteArrayInputStream(schema.getBytes()));

        // Problem handler which will print problems found.
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = ProblemHandler.collectingTo(problems);

        // We have to step over the profile otherwise it is not checked against the schema.
        try (JsonParser parser = service.createParser(new ByteArrayInputStream(profile.getBytes()), jsonSchema, handler)) {
            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();
                // Do something useful here
            }
        }

        //Add all of the problems as error messages
        if(!problems.isEmpty()) {
            TreeMap<Integer, String> problemDictionary = new TreeMap<>();
            extractProblems(problems, problemDictionary);
            errorMessages.addAll(formatProblems(problemDictionary));
        }

        if(!errorMessages.isEmpty()) {
            errorMessages.add(0,
                "Error(s) occurred during schema validation." +
                    "\nFor full details try opening the profile in a json schema-enabled IDE." +
                    "\nSee https://github.com/finos/datahelix/blob/master/docs/GettingStarted.md#Profile-Validation\n");

            throw new ValidationException(errorMessages);
        }
    }

    private void extractProblems(List<Problem> problems, TreeMap<Integer, String> problemDictionary) {
        for (Problem problem : problems) {
            extractProblem(problem, problemDictionary);
        }
    }

    private void extractProblem(Problem problem, TreeMap<Integer, String> problemDictionary) {
        if (!problem.hasBranches()) {
            int lineNumber = (int)problem.getLocation().getLineNumber();
            String formattedMessage = "- " + problem.getMessage() + "\n";
            problemDictionary.merge(lineNumber, formattedMessage, String::concat);

            return;
        }
        extractProblems(problem.getBranch(0), problemDictionary);
    }

    private List<String> formatProblems(TreeMap<Integer, String> problemDictionary) {
        List<String> outputList = new ArrayList<>();
        String messageFormat = "Problem found at line %d\n... %s ...\nSuggested fix:\n%s";

        problemDictionary.forEach(
            (lineNo, messages)
                -> outputList.add(
                String.format(
                    messageFormat,
                    lineNo,
                    profileJsonLines.get(lineNo - 1).trim(),
                    messages
                )
            )
        );

        return outputList;
    }
}
