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


import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.common.profile.DateTimeGranularity;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.FieldType;
import com.scottlogic.deg.common.profile.NumericGranularity;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.generator.profile.Profile;
import com.scottlogic.deg.generator.profile.Rule;
import com.scottlogic.deg.generator.profile.constraints.Constraint;
import com.scottlogic.deg.generator.profile.constraints.atomic.*;
import com.scottlogic.deg.generator.profile.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.generator.profile.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.generator.profile.constraints.grammatical.OrConstraint;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import static com.scottlogic.deg.common.util.Defaults.DEFAULT_DATE_FORMATTING;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;




public class JsonProfileReaderTests {

    private DistributedList<Object> inSetReaderReturnValue = DistributedList.singleton("test");
    private DistributedList<String> fromFileReaderReturnValue = DistributedList.singleton("test");

    private class MockFromFileReader extends FileReader {

        MockFromFileReader() {
            super("");
        }

        @Override
        public DistributedList<Object> setFromFile(String file)
        {
            return inSetReaderReturnValue;
        }

        @Override
        public DistributedList<String> listFromMapFile(String file, String Key)
        {
            return fromFileReaderReturnValue;
        }

    }

    private final String schemaVersion = "\"0.7\"";
    private String json;

    private JsonProfileReader jsonProfileReader = new JsonProfileReader(null, new ConstraintReader(new AtomicConstraintReader(new MockFromFileReader())));



    private void givenJson(String json) {
        this.json = json;
    }

    private Profile getResultingProfile() throws IOException {
        return jsonProfileReader.read(json);
    }
    
    private void expectValidationException(String message) {
        Throwable exception = Assertions.assertThrows(ValidationException.class, this::getResultingProfile);
        Assertions.assertEquals(message, exception.getMessage());
    }

    private void expectRules(Consumer<Rule>... ruleAssertions) throws IOException {
        expectMany(this.getResultingProfile().getRules(), ruleAssertions);
    }

    private Consumer<Rule> ruleWithDescription(String expectedDescription) {
        return rule -> Assert.assertThat(rule.getDescription(), equalTo(expectedDescription));
    }

    private Consumer<Rule> ruleWithConstraints(Consumer<Constraint>... constraintAsserters) {
        return rule -> expectMany(rule.getConstraints(), constraintAsserters);
    }

    private <T> Consumer<Constraint> typedConstraint(Class<T> constraintType, Consumer<T> asserter) {
        return constraint -> {
            Assert.assertThat(constraint, instanceOf(constraintType));

            asserter.accept((T) constraint);
        };
    }

    private Consumer<Field> fieldWithName(String expectedName) {
        return field -> Assert.assertThat(field.getName(), equalTo(expectedName));
    }

    private void expectFields(Consumer<Field>... fieldAssertions) throws IOException {
        expectMany(this.getResultingProfile().getFields(), fieldAssertions);
    }

    /**
     * Given a set I1, I2, I3... and some consumers A1, A2, A3..., run A1(I1), A2(I2), A3(I3)...
     * This lets us make assertions about each entry in a sequence
     */
    private <T> void expectMany(
            Iterable<T> assertionTargets,
            Consumer<T>... perItemAssertions) {

        Iterator<T> aIterator = assertionTargets.iterator();
        Iterator<Consumer<T>> bIterator = Arrays.asList(perItemAssertions).iterator();

        while (aIterator.hasNext() && bIterator.hasNext()) {
            bIterator.next().accept(aIterator.next());
        }

        if (aIterator.hasNext() || bIterator.hasNext())
            Assert.fail("Sequences had different numbers of elements");
    }


    @Test
    public void shouldDeserialiseSingleField() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"f1\", \"type\": \"string\" } ]," +
                        "    \"rules\": []" +
                        "}");

        expectFields(
                fieldWithName("f1"));
    }

    @Test
    public void shouldDeserialiseMultipleFields() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ " +
                        "       { \"name\": \"f1\", \"type\": \"string\" }," +
                        "       { \"name\": \"f2\", \"type\": \"string\" } ]," +
                        "    \"rules\": []" +
                        "}");

        expectFields(
                fieldWithName("f1"),
                fieldWithName("f2"));
    }

    @Test
    public void shouldGiveDefaultNameToUnnamedRules() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\" , \"type\": \"string\", \"nullable\": true} ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "            { \"field\": \"foo\", \"isNull\": true } " +
                        "        ]" +
                        "      }" +
                        "    ]" +
                        "}");

        expectRules(
            ruleWithDescription("Unnamed rule"));
        expectFields(
            field -> {
                Assert.assertThat(field.getName(), equalTo("foo"));
                Assert.assertEquals(field.getType(), FieldType.STRING);
            });
    }

    @Test
    public void shouldReadNameOfNamedRules() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\", \"nullable\": true } ]," +
                        "    \"rules\": [" +
                        "        {" +
                        "           \"rule\": \"Too rule for school\"," +
                        "           \"constraints\": [" +
                        "               { \"field\": \"foo\", \"isNull\": true }" +
                        "           ]" +
                        "        }" +
                        "    ]" +
                        "}");

        expectRules(
            ruleWithDescription("Too rule for school"));
    }

    @Test
    public void shouldNotThrowIsNullWithValueNull() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\" } ]," +
                "    \"rules\": [" +
                "        {" +
                "           \"rule\": \"Too rule for school\"," +
                "           \"constraints\": [" +
                "               { \"field\": \"foo\", \"isNull\": true }" +
                "           ]" +
                "        }" +
                "    ]" +
                "}");

        Assertions.assertDoesNotThrow(
            () -> getResultingProfile());
    }

    @Test
    public void shouldNotThrowIsNullWithValuesNull() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\" } ]," +
                "    \"rules\": [" +
                "        {" +
                "           \"rule\": \"Too rule for school\"," +
                "           \"constraints\": [" +
                    "               { \"field\": \"foo\", \"isNull\": true }" +
                "           ]" +
                "        }" +
                "    ]" +
                "}");

        Assertions.assertDoesNotThrow(
            () -> getResultingProfile());
    }

    @Test
    public void shouldDeserialiseIsOfTypeConstraint() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\", \"nullable\": true } ]," +
                "    \"rules\": []" +
                "}");

        expectRules();
        expectFields(
            field -> {
                Assert.assertThat(field.getName(), equalTo("foo"));
                Assert.assertEquals(field.getType(), FieldType.STRING);
            });
    }

    @Test
    public void shouldDeserialiseIsOfTypeConstraint_whenInteger() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"integer\", \"nullable\": true } ]," +
                "    \"rules\": []" +
                "}");


        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    IsGranularToNumericConstraint.class,
                    c -> {
                        Assert.assertThat(
                            c.granularity,
                            equalTo(new NumericGranularity(0)));
                    })));
    }

    @Test
    public void shouldDeserialiseIsEqualToConstraint() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\", \"nullable\": true } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"equalTo\": \"equal\" }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");



        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    EqualToConstraint.class,
                    c -> Assert.assertThat(
                        c.value,
                        equalTo("equal")))));

    }

    @Test
    public void shouldDeserialiseFormatConstraint() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { " +
                        "           \"name\": \"foo\"," +
                        "           \"formatting\": \"%.5s\"," +
                        "           \"type\": \"string\"" +
                        "    } ]," +
                        "    \"rules\": []" +
                        "}");

        expectFields(
            field -> {
                Assert.assertThat(field.getName(), equalTo("foo"));
                Assert.assertThat(field.getFormatting(), equalTo("%.5s"));
            }
        );
    }

    @Test
    public void shouldDeserialiseIsOfLengthConstraint() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"id\", \"type\": \"string\" , \"nullable\": true} ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "        { \"field\": \"id\",  \"ofLength\": 5 }" +
                        "        ]" +
                        "      }" +
                        "    ]" +
                        "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    StringHasLengthConstraint.class,
                    c -> Assert.assertThat(c.referenceValue.getValue(), equalTo(5)))));
    }

    @Test
    public void shouldDeserialiseNotWrapper() throws IOException {
        // Arrange
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\", \"nullable\": true } ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "        { \"not\": { \"field\": \"foo\",  \"equalTo\": \"string\" } }" +
                        "        ]" +
                        "      }" +
                        "    ]" +
                        "}");

        expectRules(
                ruleWithConstraints(
                        typedConstraint(
                                NotEqualToConstraint.class,
                                c -> {
                                    Assert.assertThat(
                                            c.value,
                                            equalTo("string"));
                                })));
    }

    @Test
    public void shouldDeserialiseOrConstraint() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\", \"nullable\": true } ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "          {" +
                        "            \"anyOf\": [" +
                        "              { \"field\": \"foo\",  \"equalTo\": 1 }," +
                        "              { \"field\": \"foo\", \"isNull\": true }" +
                        "            ]" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "   ]" +
                        "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    OrConstraint.class,
                    c -> Assert.assertThat(
                        c.subConstraints.size(),
                        equalTo(2)))));
    }

    @Test
    public void shouldDeserialiseAndConstraint() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\", \"nullable\": true } ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "          {" +
                        "           \"allOf\": [" +
                        "             { \"field\": \"foo\",  \"equalTo\": 1 }," +
                        "             { \"field\": \"foo\", \"isNull\": true }" +
                        "            ]" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "    ]" +
                        "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    AndConstraint.class,
                    c -> Assert.assertThat(
                        c.getSubConstraints().size(),
                        equalTo(2)))));
    }

    @Test
    public void shouldDeserialiseIfConstraint() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\", \"nullable\": true } ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "          {" +
                        "            \"if\": { \"field\": \"foo\",  \"equalTo\": \"string\" }," +
                        "            \"then\": { \"field\": \"foo\",  \"inSet\": [ \"str!\" ] }," +
                        "            \"else\": { \"field\": \"foo\",  \"longerThan\": 3 }" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "   ]" +
                        "}");

        expectRules(
                ruleWithConstraints(
                        typedConstraint(
                                ConditionalConstraint.class,
                                c -> {
                                    Assert.assertThat(
                                            c.condition,
                                            instanceOf(EqualToConstraint.class));

                                    Assert.assertThat(
                                            c.whenConditionIsTrue,
                                            instanceOf(IsInSetConstraint.class));

                                    Assert.assertThat(
                                            c.whenConditionIsFalse,
                                            instanceOf(IsStringLongerThanConstraint.class));
                                })));
    }

    @Test
    public void shouldDeserialiseIfConstraintWithoutElse() throws IOException {
        givenJson(
                "{" +
                        "    \"schemaVersion\": " + schemaVersion + "," +
                        "    \"fields\": [ { \"name\": \"foo\", \"type\": \"string\", \"nullable\": true } ]," +
                        "    \"rules\": [" +
                        "      {" +
                        "        \"constraints\": [" +
                        "          {" +
                        "            \"if\": { \"field\": \"foo\",  \"equalTo\": \"string\" }," +
                        "            \"then\": { \"field\": \"foo\",  \"equalTo\": \"str!\" }" +
                        "          }" +
                        "        ]" +
                        "      }" +
                        "    ]" +
                        "}");

        expectRules(
                ruleWithConstraints(
                        typedConstraint(
                                ConditionalConstraint.class,
                                c -> {
                                    Assert.assertThat(
                                            c.condition,
                                            instanceOf(EqualToConstraint.class));

                                    Assert.assertThat(
                                            c.whenConditionIsTrue,
                                            instanceOf(EqualToConstraint.class));

                                    Assert.assertThat(
                                            c.whenConditionIsFalse,
                                            nullValue());
                                })));
    }

    @Test
    public void shouldDeserialiseOneAsNumericGranularToConstraint() throws IOException {
        givenJson(
            "{" +
            "    \"schemaVersion\": " + schemaVersion + "," +
            "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\", \"nullable\": true } ]," +
            "    \"rules\": [" +
            "      {" +
            "        \"constraints\": [" +
            "        { \"field\": \"foo\",  \"granularTo\": 1 }" +
            "        ]" +
            "      }" +
            "    ]" +
            "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    IsGranularToNumericConstraint.class,
                    c -> {
                        Assert.assertThat(
                            c.granularity,
                            equalTo(new NumericGranularity(0)));
                    })));
    }

    @Test
    public void shouldDeserialiseTenthAsNumericGranularToConstraint() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\", \"nullable\": true } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"granularTo\": 0.1 }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    IsGranularToNumericConstraint.class,
                    c -> {
                        Assert.assertThat(
                            c.granularity,
                            equalTo(new NumericGranularity(1)));
                    })));
    }

    @Test
    public void shouldDisregardTrailingZeroesInNumericGranularities() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\", \"nullable\": true } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"granularTo\": 0.100000000 }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    IsGranularToNumericConstraint.class,
                    c -> {
                        Assert.assertThat(
                            c.granularity,
                            equalTo(new NumericGranularity(1)));
                    })));
    }

    @Test
    public void shouldAllowValidISO8601DateTime() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"datetime\", \"nullable\": true } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"afterOrAt\": \"2019-01-01T00:00:00.000\" }," +
                "        { \"field\": \"foo\",  \"before\": \"2019-01-03T00:00:00.000\" }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectRules(
            rule -> {
                // This is different because the ordering would switch depending on if the whole file was run or just this test
                IsAfterOrEqualToConstantDateTimeConstraint isAfter = (IsAfterOrEqualToConstantDateTimeConstraint) rule.getConstraints().stream()
                    .filter(f -> f.getClass() == IsAfterOrEqualToConstantDateTimeConstraint.class)
                    .findFirst()
                    .get();
                IsBeforeConstantDateTimeConstraint isBefore = (IsBeforeConstantDateTimeConstraint) rule.getConstraints().stream()
                    .filter(f -> f.getClass() == IsBeforeConstantDateTimeConstraint.class)
                    .findFirst()
                    .get();
                Assert.assertEquals(OffsetDateTime.parse("2019-01-01T00:00:00.000Z"), isAfter.referenceValue.getValue());
                Assert.assertEquals(OffsetDateTime.parse("2019-01-03T00:00:00.000Z"), isBefore.referenceValue.getValue());
            }
        );
    }

    @Test
    public void shouldRejectGreaterThanOneNumericGranularityConstraint() {
        givenJson(
            "{" +
            "    \"schemaVersion\": " + schemaVersion + "," +
            "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\" } ]," +
            "    \"rules\": [" +
            "      {" +
            "        \"constraints\": [" +
            "        { \"field\": \"foo\",  \"granularTo\": 2 }" +
            "        ]" +
            "      }" +
            "    ]" +
            "}");

        expectValidationException("Numeric granularity must be <= 1");
    }

    @Test
    public void shouldRejectNonPowerOfTenNumericGranularityConstraint() {
        givenJson(
            "{" +
            "    \"schemaVersion\": " + schemaVersion + "," +
            "    \"fields\": [ { \"name\": \"foo\", \"type\": \"decimal\" } ]," +
            "    \"rules\": [" +
            "      {" +
            "        \"constraints\": [" +
            "        { \"field\": \"foo\",  \"granularTo\": 0.15 }" +
            "        ]" +
            "      }" +
            "    ]" +
            "}");

        expectValidationException("Numeric granularity must be fractional power of ten");
    }

    @Test
    public void shouldRejectEqualToWithNullValue() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"datetime\" } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"equalTo\": null }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectValidationException("The equalTo constraint has null value for field foo");
    }

    @Test
    public void shouldRejectLessThanWithNullValue() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"datetime\" } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"lessThan\": null }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectValidationException("The lessThan constraint has null value for field foo");
    }

    @Test
    public void shouldRejectInSetWithANullValue() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"datetime\" } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"inSet\": [ null ] }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectValidationException("HelixDateTime cannot be null");
    }

    @Test
    public void shouldRejectInSetSetToNull() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"datetime\" } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"field\": \"foo\",  \"inSet\": null }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectValidationException("The inSet constraint has null value for field foo");
    }

    @Test
    public void shouldRejectIsConstraintSetToNullForNot() {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { \"name\": \"foo\", \"type\": \"datetime\" } ]," +
                "    \"rules\": [" +
                "      {" +
                "        \"constraints\": [" +
                "        { \"not\": { \"field\": \"foo\", \"is\": null } }" +
                "        ]" +
                "      }" +
                "    ]" +
                "}");

        expectValidationException("The constraint json object node for field foo doesn't contain any of the expected keywords as properties: {\"field\":\"foo\",\"is\":null}");
    }

    @Test
    public void unique_setsFieldPropertyToTrue_whenSetToTrue() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "           \"name\": \"foo\"," +
                "           \"type\": \"integer\"," +
                "           \"unique\": true" +
                "    } ]," +
                "    \"rules\": []" +
                "}");

        expectFields(
            field -> {
                Assert.assertThat(field.getName(), equalTo("foo"));
                Assert.assertTrue(field.isUnique());
            }
        );
    }

    @Test
    public void unique_setsFieldPropertyToFalse_whenOmitted() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "           \"name\": \"foo\"," +
                "           \"type\": \"integer\"" +
                "    } ]," +
                "    \"rules\": []" +
                "}");
        expectFields(
            field -> {
                Assert.assertThat(field.getName(), equalTo("foo"));
                Assert.assertFalse(field.isUnique());
            }
        );
    }

    @Test
    public void unique_setsFieldPropertyToFalse_whenSetToFalse() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "           \"name\": \"foo\"," +
                "           \"type\": \"integer\"," +
                "           \"unique\": false" +
                "    } ]," +
                "    \"rules\": []" +
                "}");

        expectFields(
            field -> {
                Assert.assertThat(field.getName(), equalTo("foo"));
                Assert.assertFalse(field.isUnique());
            }
        );
    }

    @Test
    public void nullable_addsConstraintForField_whenSetToFalse() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"integer\"," +
                "       \"nullable\": false" +
                "    } ]," +
                "    \"rules\": []" +
                "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    NotNullConstraint.class,
                    c -> {
                        Assert.assertEquals(
                            c.getField().getName(),
                            "foo");
                    }
                )
            ),
            ruleWithDescription("type-rules")
        );
    }

    @Test
    public void nullable_DoesNotAddConstraintForField_whenSetToTrue() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"integer\"," +
                "       \"nullable\": true" +
                "    } ]," +
                "    \"rules\": []" +
                "}");

        expectRules(ruleWithDescription("type-rules"));
    }

    @Test
    public void nullable_addsConstraintForFields_whenSetToFalse() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"integer\"," +
                "       \"nullable\": false" +
                "    }, { " +
                "       \"name\": \"bar\" ," +
                "       \"type\": \"integer\"," +
                "       \"nullable\": false" +
                "    }]," +
                "    \"rules\": []" +
                "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    NotNullConstraint.class,
                    c -> {
                        Assert.assertEquals(
                            c.getField().getName(),
                            "foo");
                    }
                ),
                typedConstraint(
                    NotNullConstraint.class,
                    c -> {
                        Assert.assertEquals(
                            c.getField().getName(),
                            "bar");
                    }
                )
            ),
            ruleWithDescription("type-rules")
        );
    }

    @Test
    public void nullable_addsConstraintForFields_whenOneSetToFalse() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"integer\"," +
                "       \"nullable\": true" +
                "    }, { " +
                "       \"name\": \"bar\" ," +
                "       \"type\": \"integer\"," +
                "       \"nullable\": false" +
                "    }]," +
                "    \"rules\": []" +
                "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    NotNullConstraint.class,
                    c -> {
                        Assert.assertEquals(
                            c.getField().getName(),
                            "bar");
                    }
                )
            ),
            ruleWithDescription("type-rules")
        );
    }

    @Test
    public void type_setsFieldTypeProperty_whenSetInFieldDefinition() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"decimal\" ," +
                "       \"nullable\": \"true\"" +
                "    }, { " +
                "       \"name\": \"bar\" ," +
                "       \"type\": \"string\" ," +
                "       \"nullable\": \"true\"" +
                "    }]," +
                "    \"rules\": []" +
                "}");

        expectFields(
            field -> {
                Assert.assertThat(field.getType(), equalTo(FieldType.NUMERIC));
            },
            field -> {
                Assert.assertThat(field.getType(), equalTo(FieldType.STRING));
            }
        );
        expectRules();
    }

    @Test
    void parser_createsInternalField_whenProfileHasAnInMapConstraint() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"string\"" +
                "    }, { " +
                "       \"name\": \"bar\" ," +
                "       \"type\": \"string\"" +
                "    }]," +
                "    \"rules\": [" +
                "       {" +
                "        \"rule\": \"fooRule\"," +
                "        \"constraints\": [" +
                "           { \"field\": \"foo\", \"inMap\": \"foobar.csv\", \"key\": \"Foo\" }," +
                "           { \"field\": \"bar\", \"inMap\": \"foobar.csv\", \"key\": \"Bar\"}" +
                "          ]" +
                "       }" +
                "    ]" +
                "}");

         expectFields(
            field -> {
                Assert.assertEquals("foo", field.getName());
                Assert.assertFalse(field.isInternal());
            },
            field -> {
                Assert.assertEquals("bar", field.getName());
                Assert.assertFalse(field.isInternal());
            },
            field -> {
                Assert.assertEquals("foobar.csv", field.getName());
                Assert.assertTrue(field.isInternal());
            }
        );
    }

    @Test
    void parser_createsInternalField_whenProfileHasANestedInMapConstraint() throws IOException {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"string\"" +
                "    }, { " +
                "       \"name\": \"bar\" ," +
                "       \"type\": \"string\"" +
                "    }, { " +
                "       \"name\": \"other\" ," +
                "       \"type\": \"string\"" +
                "    }]," +
                "    \"rules\": [" +
                "       {" +
                "        \"rule\": \"fooRule\"," +
                "        \"constraints\": [" +
                "                {" +
                "                    \"if\":   { \"field\": \"other\", \"matchingRegex\": \"^[O].*\" }," +
                "                    \"then\": {" +
                "                        \"if\":   { \"field\": \"other\", \"matchingRegex\": \"^[O].*\" }," +
                "                        \"then\": { \"allOf\": [" +
                "                            { \"field\": \"foo\", \"inMap\": \"foobar.csv\", \"key\": \"Foo\"}," +
                "                            { \"field\": \"bar\", \"inMap\": \"foobar.csv\", \"key\": \"Bar\"}" +
                "                        ]}" +
                "                    }" +
                "                }" +
                "          ]" +
                "       }" +
                "    ]" +
                "}");

        expectFields(
            field -> {
                Assert.assertEquals("foo", field.getName());
                Assert.assertFalse(field.isInternal());
            },
            field -> {
                Assert.assertEquals("bar", field.getName());
                Assert.assertFalse(field.isInternal());
            },
            field -> {
                Assert.assertEquals("other", field.getName());
                Assert.assertFalse(field.isInternal());
            },
            field -> {
                Assert.assertEquals("foobar.csv", field.getName());
                Assert.assertTrue(field.isInternal());
                Assert.assertEquals(FieldType.NUMERIC, field.getType());
            }
        );
    }

    @Test
    public void formatting_withDateType_shouldSetCorrectGranularity() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"date\"," +
                "       \"nullable\": \"true\"" +
                "    }]," +
                "    \"rules\": []" +
                "}");

        expectRules(
            ruleWithConstraints(
                typedConstraint(
                    IsGranularToDateConstraint.class,
                    c -> Assert.assertThat(c.granularity, equalTo(new DateTimeGranularity(ChronoUnit.DAYS)))
                )
            )
        );
    }

    @Test
    public void formatting_withDateType_shouldSetCorrectFormatting() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"date\"" +
                "    }]," +
                "    \"rules\": []" +
                "}");

        expectFields(
            field -> Assert.assertEquals(DEFAULT_DATE_FORMATTING,field.getFormatting())
        );
    }

    @Test
    public void formatting_withDateTypeAndFormatting_shouldSetCorrectFormatting() throws IOException  {
        givenJson(
            "{" +
                "    \"schemaVersion\": " + schemaVersion + "," +
                "    \"fields\": [ { " +
                "       \"name\": \"foo\" ," +
                "       \"type\": \"date\"," +
                "       \"formatting\": \"%tD\"" +
                "    }]," +
                "    \"rules\": []" +
                "}");

        expectFields(
            field -> Assert.assertEquals("%tD",field.getFormatting())
        );
    }
}
