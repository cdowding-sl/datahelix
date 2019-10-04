package com.scottlogic.deg.common.profile;

import java.time.OffsetDateTime;
import java.util.function.Function;

public enum DataType
{
    NUMERIC(o -> o instanceof Number),
    STRING(o -> o instanceof String),
    DATETIME(o -> o instanceof OffsetDateTime);

    private final Function<Object, Boolean> isInstanceOf;

    DataType(final Function<Object, Boolean> isInstanceOf) {
        this.isInstanceOf = isInstanceOf;
    }

    public boolean isInstanceOf(Object o) {
        return isInstanceOf.apply(o);
    }
}
