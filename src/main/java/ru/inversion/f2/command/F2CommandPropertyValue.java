package ru.inversion.f2.command;

import ru.inversion.utils.converter.TypeConverter;

// Under=Yes
public final class F2CommandPropertyValue {

    private final F2CommandProperty property;
    private final Object value;

    private final String rawName;
    private final String rawValue;

    /** */
    public F2CommandPropertyValue (
        F2CommandProperty property,
        String rawName,
        String rawValue,
        Object value
    )
    {
        this.property = property;
        this.rawName  = rawName;
        this.rawValue = rawValue;
        this.value    = value;
    }

    /** */
    public F2CommandProperty property() {
        return property;
    }

    /** */
    public String rawName() {
        return rawName;
    }

    /** */
    public String rawValue() {
        return rawValue;
    }

    /** */
    public Object value() {
        return value;
    }

    /** */
    public boolean known() {
        return property != null;
    }

    /** */
    public <T> T valueAs( Class<T> clazz, T defValue )
    {
        return value instanceof F2CommandCall
                ? (T) value
                : TypeConverter.convert( value, clazz );
    }
}