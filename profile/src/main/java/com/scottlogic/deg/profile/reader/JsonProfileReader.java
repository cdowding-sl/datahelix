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
import com.scottlogic.deg.common.profile.ConstraintType;
import com.scottlogic.deg.common.profile.DataType;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.generator.profile.Profile;
import com.scottlogic.deg.generator.profile.Rule;
import com.scottlogic.deg.generator.profile.RuleInformation;
import com.scottlogic.deg.generator.profile.constraints.Constraint;
import com.scottlogic.deg.profile.InvalidProfileException;
import com.scottlogic.deg.profile.dtos.ProfileDTO;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.AllOfConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.AnyOfConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.grammatical.ConditionalConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.InMapConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.NullConstraintDTO;
import com.scottlogic.deg.profile.serialisation.ProfileSerialiser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        List<Field> fields = profileDTO.fields.stream()
                .map(fieldDTO -> new Field(fieldDTO.name, fieldDTO.getDataType(), fieldDTO.unique, fieldDTO.formatting, false))
                .collect(Collectors.toList());

        List<Field> inMapFields = profileDTO.rules.stream()
                .flatMap(ruleDTO -> ruleDTO.constraints.stream())
                .flatMap(constraintDTO -> getInMapConstraints(profileDTO).stream())
                .map(file-> new Field(file, DataType.INTEGER,false, null,true)
                ).collect(Collectors.toList());

        fields.addAll(inMapFields);
        ProfileFields profileFields = new ProfileFields(fields);

        Collection<Rule> rules = profileDTO.rules.stream()
                .map(r -> new Rule(new RuleInformation(r.rule), constraintReader.read(r.constraints, profileFields)))
                .collect(Collectors.toList());

        Collection<Constraint> nullableConstraints = profileDTO.fields.stream()
                .filter(fieldDTO -> !fieldDTO.nullable)
                .map(fieldDTO -> constraintReader.read(new NullConstraintDTO(), profileFields).negate())
                .collect(Collectors.toList());

        Collection<Constraint> typeConstraints = profileFields.stream()
                .map(constraintReader::readType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!nullableConstraints.isEmpty())
        {
            rules.add(new Rule(new RuleInformation("nullable-rules"), nullableConstraints));
        }
        if (!typeConstraints.isEmpty())
        {
            rules.add(new Rule(new RuleInformation("type-rules"), typeConstraints));
        }
        return new Profile(profileFields, rules, profileDTO.description);


    }

    private List<String> getInMapConstraints(ProfileDTO profileDto) {
        return profileDto.rules.stream()
                .flatMap(ruleDTO -> ruleDTO.constraints.stream())
                .flatMap(constraint -> getAllAtomicConstraints(Stream.of(constraint)))
                .filter(constraintDTO -> constraintDTO.getType() == ConstraintType.IN_MAP)
                .map(constraintDTO -> ((InMapConstraintDTO)constraintDTO).file)
                .collect(Collectors.toList());
    }

    private Stream<ConstraintDTO> getAllAtomicConstraints(Stream<ConstraintDTO> constraints) {
        return constraints.flatMap(this::getUnpackedConstraintsToStream);
    }

    private Stream<ConstraintDTO> getUnpackedConstraintsToStream(ConstraintDTO constraintDTO)
    {
        switch (constraintDTO.getType())
        {
            case CONDITION:
                ConditionalConstraintDTO conditionalConstraintDTO = (ConditionalConstraintDTO) constraintDTO;
                return getAllAtomicConstraints(conditionalConstraintDTO.elseConstraint == null
                        ? Stream.of(((ConditionalConstraintDTO) constraintDTO).thenConstraint)
                        : Stream.of(((ConditionalConstraintDTO) constraintDTO).thenConstraint, ((ConditionalConstraintDTO) constraintDTO).elseConstraint));
            case ALL_OF:
                return getAllAtomicConstraints(((AllOfConstraintDTO) constraintDTO).constraints.stream());
            case ANY_OF:
                return getAllAtomicConstraints(((AnyOfConstraintDTO) constraintDTO).constraints.stream());
            default:
                return Stream.of(constraintDTO);
        }
    }
}
