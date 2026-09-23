package com.innospots.nexus.base.domain.field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 领域字段支持的值类型。每种类型映射到 Java {@link Class}，并提供 {@link #convert(Object)} 方法进行字符串到类型化值的强制转换。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DomainField
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
     * 将原始值（通常为字符串）转换为目标 Java 类型。
     * 若值已是目标类型实例，则直接返回。
     *
     * @param value 原始输入值
     * @return 转换后的类型化值，输入为 null 时返回 null
     * @throws IllegalArgumentException 字符串无法解析时
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
            // OBJECT 与 ARRAY 无对应解析规则，原样透传
            case OBJECT, ARRAY -> value;
        };
    }
}
