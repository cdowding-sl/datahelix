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

package com.scottlogic.deg.generator.profile.constraintdetail;

import java.util.Objects;

public class ParsedDateGranularity {
    private final Timescale granularity;

    public ParsedDateGranularity(Timescale granularity) {
        this.granularity = granularity;
    }

    public static ParsedDateGranularity parse(String granularityExpression) {
        return new ParsedDateGranularity(Timescale.getByName(granularityExpression));
    }

    public Timescale getGranularity() {
        return granularity;
    }

    @Override
    public int hashCode(){
        return Objects.hash(granularity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (getClass() != o.getClass())
            return false;
        ParsedDateGranularity parsedDateGranularity = (ParsedDateGranularity) o;
        return Objects.equals(granularity, parsedDateGranularity.granularity);
    }



}
