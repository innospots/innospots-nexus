package com.innospots.nexus.base.domain.condition;

/**
 * Comparison operators used in filter conditions. Each operator carries
 * separate symbols for SQL ({@code dbSymbol}) and script ({@code scriptSymbol})
 * contexts.
 */
public enum Operator {
    GREATER(">", ">"),
    GREATER_EQUAL(">=", ">="),
    LESS_EQUAL("<=", "<="),
    LESS("<", "<"),
    EQUAL("=", "=="),
    NOT_EQUAL("!=", "!="),
    IN("in", "in"),
    NOT_IN("not in", "notin"),
    LIKE("like", "like"),
    IS_NULL("is null", "=="),
    IS_NOT_NULL("is not null", "!="),
    BETWEEN("between", "between"),
    HAS_VALUE("has value", "!=");

    private final String dbSymbol;
    private final String scriptSymbol;

    Operator(String dbSymbol, String scriptSymbol) {
        this.dbSymbol = dbSymbol;
        this.scriptSymbol = scriptSymbol;
    }

    public String symbol(Mode mode) {
        return mode == Mode.DB ? dbSymbol : scriptSymbol;
    }

    public String dbSymbol() {
        return dbSymbol;
    }

    public String scriptSymbol() {
        return scriptSymbol;
    }

    public boolean isRange() {
        return this == BETWEEN
                || this == GREATER
                || this == GREATER_EQUAL
                || this == LESS
                || this == LESS_EQUAL;
    }
}
