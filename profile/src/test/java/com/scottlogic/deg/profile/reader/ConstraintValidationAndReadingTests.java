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

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType;
import com.scottlogic.deg.common.profile.constraints.atomic.*;
import com.scottlogic.deg.common.util.Defaults;
import com.scottlogic.deg.profile.InvalidProfileException;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.EqualToConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.InSetConstraintDTO;
import com.scottlogic.deg.profile.dtos.constraints.predicate.general.NotConstraintDTO;
import com.scottlogic.deg.profile.reader.atomic.ConstraintValueValidator;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static com.scottlogic.deg.common.profile.DataType.*;
import static com.scottlogic.deg.common.profile.FieldBuilder.createField;
import static com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType.*;
import static org.hamcrest.core.IsEqual.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConstraintValidationAndReadingTests {

    ProfileFields profileFields;

    @BeforeAll
    public void before() {
        List<Field> fields = new ArrayList<>();

        fields.add(createField("test"));

        profileFields = new ProfileFields(fields);
    }

    private static Object createDateObject(String dateStr) {
        Map<String, String> date = new HashMap<>();
        date.put("date", dateStr);

        return date;
    }

    private static Stream<Arguments> testProvider() {
        EqualToConstraintDTO stringValueDto = new EqualToConstraintDTO();
        stringValueDto.field = "test";
        stringValueDto.value = "value";

        EqualToConstraintDTO numberValueDto = new EqualToConstraintDTO();
        numberValueDto.field = "test";
        numberValueDto.value = BigDecimal.valueOf(10);

        EqualToConstraintDTO dateValueDto = new EqualToConstraintDTO();
        dateValueDto.field = "test";

        dateValueDto.value = createDateObject("2020-01-01T01:02:03.456");

        InSetConstraintDTO multipleValuesDto = new InSetConstraintDTO();
        multipleValuesDto.field = "test";
        multipleValuesDto.values = Arrays.asList("A", "B");

        EqualToConstraintDTO integerTypeValueDto = new EqualToConstraintDTO();
        integerTypeValueDto.field = "test";
        integerTypeValueDto.value = 1;

        NotConstraintDTO notValueDto = new NotConstraintDTO();
        notValueDto.field = "test";
        notValueDto.constraint = stringValueDto;

        return Stream.of(
                Arguments.of(IS_EQUAL_TO_CONSTANT, stringValueDto, EqualToConstraint.class, STRING),
                Arguments.of(IS_IN_SET, multipleValuesDto, IsInSetConstraint.class, STRING),
                Arguments.of(IS_NULL, stringValueDto, IsNullConstraint.class, STRING),
                Arguments.of(IS_GRANULAR_TO, integerTypeValueDto, IsGranularToNumericConstraint.class, NUMERIC),
                Arguments.of(MATCHES_REGEX, stringValueDto, MatchesRegexConstraint.class, STRING),
                Arguments.of(HAS_LENGTH, numberValueDto, StringHasLengthConstraint.class, STRING),
                Arguments.of(IS_STRING_LONGER_THAN, numberValueDto, IsStringLongerThanConstraint.class, STRING),
                Arguments.of(IS_STRING_SHORTER_THAN, numberValueDto, IsStringShorterThanConstraint.class, STRING),
                Arguments.of(IS_GREATER_THAN_CONSTANT, numberValueDto, IsGreaterThanConstantConstraint.class, NUMERIC),
                Arguments.of(IS_GREATER_THAN_OR_EQUAL_TO_CONSTANT, numberValueDto, IsGreaterThanOrEqualToConstantConstraint.class, NUMERIC),
                Arguments.of(IS_LESS_THAN_CONSTANT, numberValueDto, IsLessThanConstantConstraint.class, NUMERIC),
                Arguments.of(IS_LESS_THAN_OR_EQUAL_TO_CONSTANT, numberValueDto, IsLessThanOrEqualToConstantConstraint.class, NUMERIC),
                Arguments.of(IS_AFTER_CONSTANT_DATE_TIME, dateValueDto, IsAfterConstantDateTimeConstraint.class, DATETIME),
                Arguments.of(IS_AFTER_OR_EQUAL_TO_CONSTANT_DATE_TIME, dateValueDto, IsAfterOrEqualToConstantDateTimeConstraint.class, DATETIME),
                Arguments.of(IS_BEFORE_CONSTANT_DATE_TIME, dateValueDto, IsBeforeConstantDateTimeConstraint.class, DATETIME),
                Arguments.of(IS_BEFORE_CONSTANT_DATE_TIME, dateValueDto, IsBeforeConstantDateTimeConstraint.class, DATETIME),
                Arguments.of(IS_BEFORE_OR_EQUAL_TO_CONSTANT_DATE_TIME, dateValueDto, IsBeforeOrEqualToConstantDateTimeConstraint.class, DATETIME));
    }

    private static Stream<Arguments> stringLengthInvalidOperandProvider() {
        EqualToConstraintDTO numberValueDto = new EqualToConstraintDTO();
        numberValueDto.field = "test";
        numberValueDto.value = new BigDecimal(10.11);

        EqualToConstraintDTO stringValueDto = new EqualToConstraintDTO();
        stringValueDto.field = "test";
        stringValueDto.value = "value";

        EqualToConstraintDTO nullValueDto = new EqualToConstraintDTO();
        nullValueDto.field = "test";
        nullValueDto.value = null;

        return Stream.of(
            Arguments.of(HAS_LENGTH, numberValueDto),
            Arguments.of(IS_STRING_LONGER_THAN, numberValueDto),
            Arguments.of(IS_STRING_SHORTER_THAN, numberValueDto),

            Arguments.of(HAS_LENGTH, stringValueDto),
            Arguments.of(IS_STRING_LONGER_THAN, stringValueDto),
            Arguments.of(IS_STRING_SHORTER_THAN, stringValueDto),

            Arguments.of(HAS_LENGTH, nullValueDto),
            Arguments.of(IS_STRING_LONGER_THAN, nullValueDto),
            Arguments.of(IS_STRING_SHORTER_THAN, nullValueDto));
    }

    private static Stream<Arguments> ofTypeInvalidValueProvider() {
        EqualToConstraintDTO numericTypeValueDto = new EqualToConstraintDTO();
        numericTypeValueDto.field = "test";
        numericTypeValueDto.value = "numeric";

        return Stream.of(Arguments.of(AtomicConstraintType.IS_OF_TYPE, numericTypeValueDto));
    }

    private static Stream<Arguments> numericOutOfBoundsOperandProvider() {
        EqualToConstraintDTO maxValueDtoPlusOne = new EqualToConstraintDTO();
        maxValueDtoPlusOne.field = "test";
        maxValueDtoPlusOne.value = Defaults.NUMERIC_MAX.add(BigDecimal.ONE);

        EqualToConstraintDTO minValueDtoMinusOne = new EqualToConstraintDTO();
        minValueDtoMinusOne.field = "test";
        minValueDtoMinusOne.value = Defaults.NUMERIC_MIN.subtract(BigDecimal.ONE);

        return Stream.of(
            Arguments.of(IS_EQUAL_TO_CONSTANT, maxValueDtoPlusOne),
            Arguments.of(IS_IN_SET, maxValueDtoPlusOne),
            Arguments.of(IS_GREATER_THAN_OR_EQUAL_TO_CONSTANT, maxValueDtoPlusOne),
            Arguments.of(IS_GREATER_THAN_CONSTANT, maxValueDtoPlusOne),
            Arguments.of(IS_LESS_THAN_OR_EQUAL_TO_CONSTANT, maxValueDtoPlusOne),
            Arguments.of(IS_LESS_THAN_CONSTANT, maxValueDtoPlusOne),

            Arguments.of(IS_EQUAL_TO_CONSTANT, minValueDtoMinusOne),
            Arguments.of(IS_IN_SET, minValueDtoMinusOne),
            Arguments.of(IS_GREATER_THAN_OR_EQUAL_TO_CONSTANT, minValueDtoMinusOne),
            Arguments.of(IS_GREATER_THAN_CONSTANT, minValueDtoMinusOne),
            Arguments.of(IS_LESS_THAN_OR_EQUAL_TO_CONSTANT, minValueDtoMinusOne),
            Arguments.of(IS_LESS_THAN_CONSTANT, minValueDtoMinusOne)
        );
    }

    private static Stream<Arguments> stringLengthValidOperandProvider() {

        EqualToConstraintDTO integerAsDecimalDto = new EqualToConstraintDTO();
        integerAsDecimalDto.field = "test";
        integerAsDecimalDto.value = new BigDecimal(10);

        EqualToConstraintDTO integerAsIntegerDto = new EqualToConstraintDTO();
        integerAsIntegerDto.field = "test";
        integerAsIntegerDto.value = 10;

        EqualToConstraintDTO integerAsDecimalWith0Fraction = new EqualToConstraintDTO();
        integerAsDecimalWith0Fraction.field = "test";
        integerAsDecimalWith0Fraction.value = new BigDecimal(10.0);

        return Stream.of(
            Arguments.of(HAS_LENGTH, integerAsDecimalDto),
            Arguments.of(IS_STRING_LONGER_THAN, integerAsDecimalDto),
            Arguments.of(IS_STRING_SHORTER_THAN, integerAsDecimalDto),

            Arguments.of(HAS_LENGTH, integerAsIntegerDto),
            Arguments.of(IS_STRING_LONGER_THAN, integerAsIntegerDto),
            Arguments.of(IS_STRING_SHORTER_THAN, integerAsIntegerDto),

            Arguments.of(HAS_LENGTH, integerAsDecimalWith0Fraction),
            Arguments.of(IS_STRING_LONGER_THAN, integerAsDecimalWith0Fraction),
            Arguments.of(IS_STRING_SHORTER_THAN, integerAsDecimalWith0Fraction));
    }

    @DisplayName("Should fail when operators have an invalid operand")
    @ParameterizedTest(name = "{0} should be invalid")
    @MethodSource({"stringLengthInvalidOperandProvider", "ofTypeInvalidValueProvider"})
    public void testAtomicConstraintReaderWithInvalidOperands(AtomicConstraintType type, EqualToConstraintDTO dto) {
        Assertions.assertThrows(InvalidProfileException.class, () -> ConstraintValueValidator.validate(createField(dto.field), type, dto.value));
    }

    @Test
    public void testBaseConstraintReaderMapWithUnmappedOperands() {
        Assertions.assertThrows(
            InvalidProfileException.class,
            () -> ConstraintValueValidator.validate(createField("test"), IS_OF_TYPE, "garbage"));
    }

    @DisplayName("Should fail when value property is numeric and out of bounds")
    @ParameterizedTest(name = "{0} should be invalid")
    @MethodSource("numericOutOfBoundsOperandProvider")
    public void testAtomicConstraintReaderWithOutOfBoundValues(AtomicConstraintType type, EqualToConstraintDTO dto) {
        Assertions.assertThrows(InvalidProfileException.class, () ->
            ConstraintValueValidator.validate(createField(dto.field, NUMERIC), type, dto.value));
    }

    @DisplayName("Should pass when string lengths have an integer operand")
    @ParameterizedTest(name = "{0} should be valid")
    @MethodSource("stringLengthValidOperandProvider")
    public void testAtomicConstraintReaderWithValidOperands(AtomicConstraintType type, EqualToConstraintDTO dto) {
        Assertions.assertDoesNotThrow(() -> ConstraintValueValidator.validate(createField(dto.field), type, dto.value));
    }

    @Test
    public void shouldRejectDatesAt0000() {
        assertRejectedDateTimeParse("0000-01-01T00:00:00.000");
        assertRejectedDateTimeParse("0000-01-01T00:00:00.000Z");
    }

    @Test
    public void shouldRejectDatesBefore0000() {
        assertRejectedDateTimeParse("-00001-01-01T00:00:00.000");
        assertRejectedDateTimeParse("-00001-01-01T00:00:00.000Z");
    }

    @Test
    public void shouldRejectDatesAfter9999(){
        assertRejectedDateTimeParse("10000-01-01T00:00:00.000");
        assertRejectedDateTimeParse("10000-01-01T00:00:00.000Z");
    }

    @Test
    public void shouldAcceptDatesAtStartOf0001() {
        assertSuccessfulDateParse(
            "0001-01-01T00:00:00.000",
            OffsetDateTime.of(1, 1, 1, 00, 00, 00, 0, ZoneOffset.UTC));

        assertSuccessfulDateParse(
            "0001-01-01T00:00:00.000Z",
            OffsetDateTime.of(1, 1, 1, 00, 00, 00, 0, ZoneOffset.UTC));
    }

    @Test
    public void shouldAcceptDatesAtEndOf9999() {
        assertSuccessfulDateParse(
            "9999-12-31T23:59:59.999",
            OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 999000000, ZoneOffset.UTC));

        assertSuccessfulDateParse(
            "9999-12-31T23:59:59.999Z",
            OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 999000000, ZoneOffset.UTC));
    }

    @Test
    public void shouldRejectInvalidDates(){
        assertRejectedDateTimeParse("2019-02-29T00:00:00.000");
        assertRejectedDateTimeParse("2019-02-29T00:00:00.000Z");
    }

    @Test
    public void shouldRejectInvalidTimes(){
        assertRejectedDateTimeParse("2019-01-01T24:00:00.000");
        assertRejectedDateTimeParse("2019-01-01T24:00:00.000Z");
    }

    @Test
    public void shouldRejectPartiallySpecifiedDateTimes(){
        assertRejectedDateTimeParse("2010-01-01");
        assertRejectedDateTimeParse("2010-01-01T12:30:00");
    }

    @Test
    public void shouldAssumeUTCWhenOffsetNotSpecified() {
        assertSuccessfulDateParse(
            "2018-04-01T00:00:00.000",
            OffsetDateTime.of(2018, 04, 01, 00, 00, 00, 0, ZoneOffset.UTC));
    }

    @Test
    public void shouldHandleExplicitHourOffsets() {
        assertSuccessfulDateParse(
            "2018-04-01T00:00:00.000+03",
            OffsetDateTime.of(2018, 04, 01, 00, 00, 00, 0, ZoneOffset.ofHours(3)));
    }

    private void assertSuccessfulDateParse(String dateString, OffsetDateTime expectedDateTime) {
        OffsetDateTime actualDateTime = tryParseConstraintDateTimeValue(createDateObject(dateString));

        Assert.assertThat(actualDateTime, equalTo(expectedDateTime));
    }

    private void assertRejectedDateTimeParse(String dateString) {
        Assertions.assertThrows(
            InvalidProfileException.class,
            () -> tryParseConstraintDateTimeValue(createDateObject(dateString)));
    }

    private OffsetDateTime tryParseConstraintDateTimeValue(Object value) {
        EqualToConstraintDTO dateDto = new EqualToConstraintDTO();
        dateDto.field = "test";
        dateDto.value = value;
        ConstraintValueValidator.validate(createField("test", DATETIME), IS_AFTER_CONSTANT_DATE_TIME, value);

        return (OffsetDateTime)value;
    }
}
