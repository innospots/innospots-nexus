package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Renders a {@link Factor} into a SQL expression string.
 * <p>Values are quoted according to their {@link FieldValueType}:
 * numeric and boolean types are left unquoted; date/time types are formatted
 * with ISO patterns; strings are single-quoted with apostrophes stripped.</p>
 */
public class DatabaseFactorStatement implements IFactorStatement {

    /**
     * Normalizes a value into a SQL-safe string representation.
     * Numeric and boolean types are left unquoted; date/time types use ISO
     * formatting; strings are single-quoted with embedded apostrophes stripped.
     *
     * @param value     the raw value to normalize
     * @param valueType the expected value type
     * @param operator  the operator (unused in SQL mode, retained for interface contract)
     * @return the SQL-safe string representation
     */
    @Override
    public String normalizeValue(Object value, FieldValueType valueType, Operator operator) {
        if (value == null) {
            return "";
        }
        // Numeric and boolean -> raw without quotes
        if (valueType == FieldValueType.INTEGER || valueType == FieldValueType.LONG
                || valueType == FieldValueType.DOUBLE || valueType == FieldValueType.DECIMAL
                || valueType == FieldValueType.BOOLEAN) {
            return value.toString();
        }
        // Date types -> ISO format in single quotes
        if (valueType == FieldValueType.DATE) {
            if (value instanceof LocalDate d) {
                return "'" + d.format(DateTimeFormatter.ISO_LOCAL_DATE) + "'";
            }
            return "'" + value + "'";
        }
        if (valueType == FieldValueType.DATE_TIME) {
            if (value instanceof LocalDateTime dt) {
                // Replace ISO 'T' separator with SQL standard space
                return "'" + dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', ' ') + "'";
            }
            return "'" + value + "'";
        }
        // All other types (STRING, OBJECT, ARRAY) -> single-quoted with apostrophes escaped
        String result = value.toString().replace("'", "");
        return "'" + result + "'";
    }

    @Override
    public Object normalizeValue(Factor factor) {
        return normalizeValue(factor.value(), factor.valueType());
    }

    /**
     * Renders a single factor as a complete SQL expression like
     * {@code field = 'value'} or {@code field IN (v1, v2)}.
     */
    @Override
    public String statement(Factor factor) {
        // IN operator: expand collection to comma-separated list
        if (factor.operator() == Operator.IN) {
            if (factor.value() instanceof Collection<?> col) {
                StringBuilder sb = new StringBuilder(" IN (");
                List<String> items = new ArrayList<>();
                for (Object v : col) {
                    items.add(String.valueOf(normalizeValue(v, inferType(v))));
                }
                sb.append(String.join(",", items));
                sb.append(")");
                return factor.code() + sb;
            }
            // Single-value IN falls back to equality
            return factor.code() + " = " + normalizeValue(factor.value(), factor.valueType());
        }

        // BETWEEN: extract start and end from a 2-element collection
        if (factor.operator() == Operator.BETWEEN) {
            if (factor.value() instanceof Collection<?> col) {
                int i = 0;
                String start = null, end = null;
                for (Object v : col) {
                    if (i == 0) {
                        start = String.valueOf(normalizeValue(v, inferType(v)));
                    } else if (i == 1) {
                        end = String.valueOf(normalizeValue(v, inferType(v)));
                    }
                    i++;
                }
                return factor.code() + " BETWEEN " + start + " AND " + end;
            }
        }

        // IS NULL / IS NOT NULL have no right-hand side value
        if (factor.operator() == Operator.IS_NULL) {
            return factor.code() + " IS NULL";
        }
        if (factor.operator() == Operator.IS_NOT_NULL) {
            return factor.code() + " IS NOT NULL";
        }
        if (factor.operator() == Operator.HAS_VALUE) {
            return factor.code() + " IS NOT NULL";  // alias for IS NOT NULL
        }

        // Default: field op value (e.g. age > 18)
        return factor.code() + " " + factor.operator().dbSymbol() + " "
                + normalizeValue(factor.value(), factor.valueType());
    }

    /**
     * Infers the SQL-compatible value type from the Java runtime type.
     */
    private static FieldValueType inferType(Object value) {
        return switch (value) {
            case Integer i -> FieldValueType.INTEGER;
            case Long l -> FieldValueType.LONG;
            case Double v -> FieldValueType.DOUBLE;
            case Boolean b -> FieldValueType.BOOLEAN;
            case List<?> l -> FieldValueType.ARRAY;
            default -> FieldValueType.STRING;
        };
    }
}
