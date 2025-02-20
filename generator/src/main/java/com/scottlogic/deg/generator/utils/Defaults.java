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

package com.scottlogic.deg.generator.utils;

import com.scottlogic.deg.generator.restrictions.linear.Limit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static com.scottlogic.deg.common.util.Defaults.*;

public class Defaults {

    public static final Limit<BigDecimal> NUMERIC_MAX_LIMIT = new Limit<>(NUMERIC_MAX, true);
    public static final Limit<BigDecimal> NUMERIC_MIN_LIMIT= new Limit<>(NUMERIC_MIN, true);

    public static final Limit<OffsetDateTime> DATETIME_MAX_LIMIT = new Limit<>(ISO_MAX_DATE, true);
    public static final Limit<OffsetDateTime> DATETIME_MIN_LIMIT = new Limit<>(ISO_MIN_DATE, true);

}
