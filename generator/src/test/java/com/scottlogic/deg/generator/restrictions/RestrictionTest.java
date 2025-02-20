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

package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.restrictions.linear.Limit;
import com.scottlogic.deg.generator.restrictions.linear.LinearRestrictions;
import com.scottlogic.deg.generator.restrictions.linear.LinearRestrictionsFactory;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class RestrictionTest {

    @Test
    public void shouldFilterNumeric() {
        LinearRestrictions<BigDecimal> restriction = LinearRestrictionsFactory.createNumericRestrictions(
            new Limit<>(new BigDecimal("5"), true),
            new Limit<>(new BigDecimal("10"), false));

        Assert.assertThat(restriction.match(BigDecimal.valueOf(4)), Is.is(false));
        Assert.assertThat(restriction.match(BigDecimal.valueOf(5)), Is.is(true));
        Assert.assertThat(restriction.match(BigDecimal.valueOf(9)), Is.is(true));
        Assert.assertThat(restriction.match(BigDecimal.valueOf(10)), Is.is(false));
    }

    @Test
    public void shouldFilterString() {
        StringRestrictions restriction = new StringRestrictionsFactory().forStringMatching(Pattern.compile("H(i|ello) World"), false);

        Assert.assertThat(restriction.match("Hello World"), Is.is(true));
        Assert.assertThat(restriction.match("Hi World"), Is.is(true));
        Assert.assertThat(restriction.match("Goodbye"), Is.is(false));

        Assert.assertThat(restriction.match("5"), Is.is(false));
    }

}
