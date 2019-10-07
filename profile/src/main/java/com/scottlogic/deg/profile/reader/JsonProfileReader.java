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
import com.google.inject.name.Named;
import com.scottlogic.deg.common.profile.*;
import com.scottlogic.deg.common.profile.constraints.Constraint;
import com.scottlogic.deg.profile.dtos.ProfileDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.NullConstraintDTO;
import com.scottlogic.deg.profile.serialisation.ProfileSerialiser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JsonProfileReader is responsible for reading and validating a profile from a path to a profile JSON file.
 * It returns a Profile object for consumption by a generator
 */
public class JsonProfileReader implements ProfileReader
{
    private final File profileFile;
    private final ConstraintReader constraintReader;

    @Inject
    public JsonProfileReader(@Named("config:profileFile") File profileFile, ConstraintReader constraintReader)
    {
        this.profileFile = profileFile;
        this.constraintReader = constraintReader;
    }

    public Profile read() throws IOException
    {
        byte[] encoded = Files.readAllBytes(profileFile.toPath());
        String profileJson = new String(encoded, StandardCharsets.UTF_8);
        return read(profileJson);
    }

    public Profile read(String profileJson) throws IOException
    {
        ProfileDTO profileDTO = new ProfileSerialiser().deserialise(profileJson);
        if (profileDTO.fields == null) throw new InvalidProfileException("Profile is invalid: 'fields' have not been defined.");
        if (profileDTO.rules == null) throw new InvalidProfileException("Profile is invalid: 'rules' have not been defined.");

        ProfileFields profileFields = new ProfileFields(profileDTO.fields.stream()
                .map(fieldDTO -> new Field(fieldDTO.name, fieldDTO.getDataType(), fieldDTO.unique, fieldDTO.formatting, false))
                .collect(Collectors.toList()));

        List<String> rulesWithEmptyConstraints = profileDTO.rules.stream().filter(r -> r.constraints.isEmpty()).map(r -> r.rule).collect(Collectors.toList());
        if(!rulesWithEmptyConstraints.isEmpty())
        {
            throw new InvalidProfileException("Profile is invalid: unable to find 'constraints' for rule(s): " + String.join(",", rulesWithEmptyConstraints));
        }

        Collection<Rule> rules = profileDTO.rules.stream()
                .map(r -> new Rule(new RuleInformation(r.rule), constraintReader.readMany(r.constraints, profileFields)))
                .collect(Collectors.toList());

        Collection<Constraint> nullableConstraints = profileDTO.fields.stream()
                .filter(fieldDTO -> !fieldDTO.nullable)
                .map(fieldDTO -> constraintReader.read(new NullConstraintDTO(), profileFields).negate())
                .collect(Collectors.toList());

        if (!nullableConstraints.isEmpty())
        {
            rules.add(new Rule(new RuleInformation("nullable-rules"), nullableConstraints));
        }
        return new Profile(profileFields, rules, profileDTO.description);
    }
}
