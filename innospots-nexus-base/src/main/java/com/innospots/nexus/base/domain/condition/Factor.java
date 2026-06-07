package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * A single filter criterion composed of a field code, an operator, a value,
 * and an optional value type.
 * <p>Values prefixed with {@code ${...}} or {@code %{...}} are treated as
 * placeholders resolved at runtime against an input map. The {@link #value(Map)}
 * method performs this resolution.</p>
 */
@Getter
public class Factor {

    private static final String PLACEHOLDER_PREFIX_1 = "${";
    private static final String PLACEHOLDER_PREFIX_2 = "%{";
    private static final String PLACEHOLDER_SUFFIX = "}";

    private String name;
    private final String code;
    private final Operator operator;
    private final Object value;
    private final FieldValueType valueType;

    /**
     * Constructs a factor with explicit value type.
     *
     * @param code      field identifier
     * @param operator  comparison operator
     * @param value     comparison value (may be a placeholder string like "${key}")
     * @param valueType the explicit value type for formatting/quoting
     */
    public Factor(String code, Operator operator, Object value, FieldValueType valueType) {
        this.code = code;
        this.operator = operator;
        this.value = value;
        this.valueType = valueType;
    }

    /**
     * Creates a factor by inferring the value type from the Java type of value.
     *
     * @return a new Factor with auto-detected value type
     */
    public static Factor of(String code, Operator operator, Object value) {
        return new Factor(code, operator, value, inferType(value));
    }

    /**
     * Creates a factor with an explicitly specified value type.
     */
    public static Factor of(String code, Operator operator, Object value, FieldValueType valueType) {
        return new Factor(code, operator, value, valueType);
    }

    public String name() {
        return name;
    }

    public Factor name(String name) {
        this.name = name;
        return this;
    }

    public String code() {
        return code;
    }

    public Operator operator() {
        return operator;
    }

    public Object value() {
        return value;
    }

    public FieldValueType valueType() {
        return valueType;
    }

    /**
     * Resolves the factor value against a runtime input map.
     * <ul>
     *   <li>Placeholder strings ({@code ${key}} or {@code %{key}}) extract the
     *       corresponding entry from the input map.</li>
     *   <li>Values of type {@link FieldValueType#OBJECT} treat the raw value
     *       itself as a key into the input map.</li>
     *   <li>All other values are returned as-is.</li>
     * </ul>
     *
     * @param input the runtime key-value input map
     * @return the resolved value, or null if input is absent
     */
    public Object value(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        // Resolve placeholder syntax: ${key} or %{key} -> lookup input map
        if (value instanceof String str &&
                (str.startsWith(PLACEHOLDER_PREFIX_1) || str.startsWith(PLACEHOLDER_PREFIX_2))) {
            return input.get(str.substring(2, str.length() - 1));
        }
        // OBJECT type values are treated as dynamic field references
        if (valueType == FieldValueType.OBJECT) {
            return input.get(value);
        }
        return value;
    }

    /**
     * Extracts the placeholder key from a placeholder value string.
     * For example, {@code "${userId}"} returns {@code "userId"}.
     * If the value is not a placeholder, returns the string representation.
     */
    public String valueKey() {
        if (value instanceof String str &&
                (str.startsWith(PLACEHOLDER_PREFIX_1) || str.startsWith(PLACEHOLDER_PREFIX_2))) {
            return str.substring(2, str.length() - 1);
        }
        return String.valueOf(value);
    }

    /**
     * Checks if this factor has any null essential properties (operator, valueType, or code).
     */
    public boolean checkNull() {
        return operator == null || valueType == null || code == null;
    }

    /**
     * Checks if any factor in the given list has null essential properties.
     */
    public static boolean checkNull(List<Factor> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return list.stream().anyMatch(Factor::checkNull);
    }

    /**
     * Infers the {@link FieldValueType} from the Java runtime type of value.
     */
    private static FieldValueType inferType(Object value) {
        return switch (value) {
            case Integer i -> FieldValueType.INTEGER;
            case Long l -> FieldValueType.LONG;
            case Double v -> FieldValueType.DOUBLE;
            case Boolean b -> FieldValueType.BOOLEAN;
            case null -> FieldValueType.STRING;
            case List<?> l -> FieldValueType.ARRAY;
            default -> FieldValueType.STRING;
        };
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", operator=" + operator +
                ", value=" + value +
                ", valueType=" + valueType +
                '}';
    }
}
