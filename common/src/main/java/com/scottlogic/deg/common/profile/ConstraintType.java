package com.scottlogic.deg.common.profile;

public enum ConstraintType
{
    EQUAL_TO("equalTo"),
    IN_SET("inSet"),
    IN_MAP("inMap"),
    NULL("null"),
    GRANULAR_TO("granularTo"),
    MATCHES_REGEX("matchingRegex"),
    CONTAINS_REGEX("containingRegex"),
    OF_LENGTH("ofLength"),
    LONGER_THAN("longerThan"),
    SHORTER_THAN("shorterThan"),
    GREATER_THAN("greaterThan"),
    GREATER_THAN_OR_EQUAL_TO("greaterThanOrEqualTo"),
    LESS_THAN("lessThan"),
    LESS_THAN_OR_EQUAL_TO("lessThanOrEqualTo"),
    AFTER("after"),
    AFTER_OR_AT("afterOrAt"),
    BEFORE("before"),
    BEFORE_OR_AT("beforeOrAt"),
    NOT("not"),
    ANY_OF("anyOf"),
    ALL_OF("allOf"),
    CONDITION("condition");

    private final String name;

    ConstraintType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
