package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Renders a {@link Factor} into a script/expression language (e.g. MVEL, SpEL)
 * string. Numeric values are left unquoted; strings are single-quoted.
 * {@code IN} and {@code NOT_IN} operators use {@code include(...)} /
 * {@code notInclude(...)} helper functions; {@code LIKE} uses
 * {@code regexMatch(...)}.
 */
public class ScriptFactorStatement implements IFactorStatement {

    /**
     * Normalizes a value for script/expression language output.
     * The {@code M} suffix denotes BigDecimal literals in some expression
     * languages (e.g. MVEL); values with this suffix are passed through raw.
     *
     * @param value     the raw value to normalize
     * @param valueType the expected value type, may be null
     * @param operator  the operator, used to detect range operators
     * @return the normalized value (String for quoted, Number for unquoted)
     */
    @Override
    public Object normalizeValue(Object value, FieldValueType valueType, Operator operator) {
        if (valueType == null) {
            // Without a declared type, treat Number and BigDecimal literals as-is
            if (value instanceof Number) {
                return value;
            }
            // BigDecimal MVEL literal (e.g. 10.5M)
            if (value != null && value.toString().endsWith("M")) {
                return value;
            }
            // Range operator values are left unquoted for arithmetic comparison
            if (operator != null && operator.isRange()) {
                return value;
            }
            return "'" + value + "'";
        }
        if (valueType == FieldValueType.INTEGER || valueType == FieldValueType.LONG
                || valueType == FieldValueType.DOUBLE || valueType == FieldValueType.DECIMAL) {
            if (value instanceof Number) {
                return value;
            }
            if (value != null && value.toString().endsWith("M")) {
                return value;
            }
            return valueType.convert(value);
        }
        // Pass through BigDecimal literals and range values for non-numeric types
        if (value != null && (value.toString().endsWith("M") || (operator != null && operator.isRange()))) {
            return value;
        }
        return "'" + value + "'";
    }

    @Override
    public Object normalizeValue(Factor factor) {
        return normalizeValue(factor.value(), factor.valueType());
    }

    /**
     * Renders a single factor as a script expression.
     * <ul>
     *   <li>Comparison operators: {@code field == value}</li>
     *   <li>IN/NOT_IN: {@code include(seq.set(v1,v2), field)}</li>
     *   <li>LIKE: {@code regexMatch(pattern, field)}</li>
     *   <li>BETWEEN: {@code field >= start && field <= end}</li>
     * </ul>
     */
    @Override
    public String statement(Factor factor) {
        if (factor.operator() == null) {
            return "";
        }
        StringBuilder stmt = new StringBuilder();
        switch (factor.operator()) {
            // Null/unary checks: field == null / field != null (bare keyword, not quoted)
            case IS_NULL ->
                    stmt.append(factor.code()).append(" == null");
            case IS_NOT_NULL, HAS_VALUE ->
                    stmt.append(factor.code()).append(" != null");
            // Simple binary operators: code op normalizedValue
            case EQUAL, LESS, NOT_EQUAL, GREATER, LESS_EQUAL, GREATER_EQUAL ->
                    stmt.append(factor.code()).append(factor.operator().scriptSymbol())
                            .append(normalizeValue(factor));
            // IN -> include(seq.set(vals), field)
            case IN -> {
                if (factor.value() instanceof Collection<?> col) {
                    String vs = col.stream()
                            .map(v -> String.valueOf(normalizeValue(v, factor.valueType())))
                            .collect(Collectors.joining(","));
                    stmt.append("include(seq.set(").append(vs).append("),").append(factor.code()).append(")");
                } else {
                    stmt.append("include(seq.set(").append(normalizeValue(factor)).append("),").append(factor.code()).append(")");
                }
            }
            // NOT_IN -> notInclude(seq.set(vals), field)
            case NOT_IN -> {
                if (factor.value() instanceof Collection<?> col) {
                    String vs = col.stream()
                            .map(v -> String.valueOf(normalizeValue(v, factor.valueType())))
                            .collect(Collectors.joining(","));
                    stmt.append("notInclude(seq.set(").append(vs).append("),").append(factor.code()).append(")");
                } else {
                    stmt.append("notInclude(seq.set(").append(normalizeValue(factor)).append("),").append(factor.code()).append(")");
                }
            }
            // LIKE -> regexMatch(pattern, field)
            case LIKE -> stmt.append("regexMatch(").append(normalizeValue(factor))
                    .append(",").append(factor.code()).append(")");
            // BETWEEN -> field >= start && field <= end
            case BETWEEN -> {
                if (factor.value() instanceof Collection<?> col) {
                    int i = 0;
                    String start = null, end = null;
                    for (Object v : col) {
                        if (i == 0) {
                            start = String.valueOf(normalizeValue(v, factor.valueType()));
                        } else if (i == 1) {
                            end = String.valueOf(normalizeValue(v, factor.valueType()));
                        }
                        i++;
                    }
                    stmt.append(factor.code()).append(" >= ").append(start)
                            .append(" && ").append(factor.code()).append(" <= ").append(end);
                }
            }
        }
        return stmt.toString();
    }
}
