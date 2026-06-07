package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;

/**
 * Strategy interface for rendering a {@link Factor} into a mode-specific
 * expression string (SQL, script, or Java). Implementations handle value
 * quoting, type conversion, and operator-specific formatting.
 *
 * @see DatabaseFactorStatement
 * @see ScriptFactorStatement
 * @see FactorStatementBuilder
 */
public interface IFactorStatement {

    Object normalizeValue(Object value, FieldValueType valueType, Operator operator);

    default Object normalizeValue(Object value, FieldValueType valueType) {
        return normalizeValue(value, valueType, null);
    }

    default Object normalizeValue(Object value) {
        return normalizeValue(value, FieldValueType.STRING, null);
    }

    Object normalizeValue(Factor factor);

    String statement(Factor factor);
}
