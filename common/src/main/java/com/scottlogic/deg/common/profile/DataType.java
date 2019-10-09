package com.scottlogic.deg.common.profile;

public enum DataType
{
    DECIMAL("decimal", GenericDataType.NUMERIC),
    INTEGER( "integer", GenericDataType.NUMERIC),
    ISIN("ISIN", GenericDataType.STRING),
    SEDOL("SEDOL", GenericDataType.STRING),
    CUSIP("CUSIP", GenericDataType.STRING),
    RIC("RIC", GenericDataType.STRING),
    FIRST_NAME("firstname", GenericDataType.STRING),
    LAST_NAME("lastname", GenericDataType.STRING),
    FULL_NAME("fullname", GenericDataType.STRING),
    STRING("string", GenericDataType.STRING),
    DATE_TIME("datetime", GenericDataType.DATETIME);

    private final String name;
    private final GenericDataType genericDataType;

    DataType(String name, GenericDataType genericDataType)
    {
        this.name = name;
        this.genericDataType = genericDataType;
    }

    public String getName() {
        return name;
    }
    public GenericDataType getGenericDataType()
    {
        return genericDataType;
    }

    public static DataType fromName(String name){
        switch (name)
        {
            case "decimal": return DECIMAL;
            case "integer": return INTEGER;
            case "ISIN": return ISIN;
            case "SEDOL": return SEDOL;
            case "CUSIP": return CUSIP;
            case "RIC": return RIC;
            case "firstname": return FIRST_NAME;
            case "lastname": return LAST_NAME;
            case "fullname": return FULL_NAME;
            case "string": return STRING;
            case "datetime": return DATE_TIME;
            default:
                throw new IllegalStateException("No data types with name " + name);
        }
    }
}
