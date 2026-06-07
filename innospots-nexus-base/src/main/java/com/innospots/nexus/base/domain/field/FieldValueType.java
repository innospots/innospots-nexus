package com.innospots.nexus.base.domain.field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Supported value types for domain fields. Each type maps to a Java
 * {@link Class} and provides a {@link #convert(Object)} method for
 * string-to-typed-value coercion.
 */
public enum FieldValueType {
    STRING(String.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    DECIMAL(BigDecimal.class),
    BOOLEAN(Boolean.class),
    DATE(LocalDate.class),
    TIME(LocalTime.class),
    DATE_TIME(LocalDateTime.class),
    OBJECT(Object.class),
    ARRAY(Object[].class);

    private final Class<?> javaType;

    FieldValueType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> javaType() {
        return javaType;
    }

    /**
     * Converts a raw value (typically a string) to the target Java type.
     * If the value is already an instance of the target type, it is returned as-is.
     *
     * @param value the raw input value
     * @return the converted typed value, or null if input is null
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public Object convert(Object value) {
        if (value == null) {
            return null;
        }
        if (javaType.isInstance(value)) {
            return value;
        }
        String text = String.valueOf(value);
        return switch (this) {
            case STRING -> text;
            case INTEGER -> Integer.valueOf(text);
            case LONG -> Long.valueOf(text);
            case DOUBLE -> Double.valueOf(text);
            case DECIMAL -> new BigDecimal(text);
            case BOOLEAN -> Boolean.valueOf(text);
            case DATE -> LocalDate.parse(text);
            case TIME -> LocalTime.parse(text);
            case DATE_TIME -> LocalDateTime.parse(text);
            // OBJECT and ARRAY pass through without conversion
            case OBJECT, ARRAY -> value;
        };
    }
}
