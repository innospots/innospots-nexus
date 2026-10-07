package com.innospots.nexus.base.domain.field;

/**
 * 具有特定 {@link FieldValueType}、必填标志与可选默认值的参数字段。其作用域自动设为 {@link FieldScope#PARAMETER}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DomainField
 */
public class ParamField extends DomainField {

    private boolean required;
    private Object defaultValue;
    private final FieldValueType fieldValueType;

    private ParamField(String code, FieldValueType valueType) {
        super(code, code, valueTypeName(valueType));
        this.fieldValueType = valueType;
        scope(FieldScope.PARAMETER);
    }

    public static ParamField of(String code, FieldValueType valueType) {
        return new ParamField(code, valueType);
    }

    public boolean required() {
        return required;
    }

    public FieldValueType fieldValueType() {
        return fieldValueType;
    }

    public ParamField required(boolean required) {
        this.required = required;
        return this;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public ParamField defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    private static String valueTypeName(FieldValueType valueType) {
        if (valueType == null) {
            return null;
        }
        return valueType.javaType().getSimpleName();
    }
}
