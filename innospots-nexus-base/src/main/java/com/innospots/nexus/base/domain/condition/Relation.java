package com.innospots.nexus.base.domain.condition;

/**
 * Logical combinators for joining multiple {@link Factor} conditions.
 * Each relation has a SQL symbol and a script symbol.
 */
public enum Relation {
    AND(" and ", " && "),
    OR(" or ", " || ");

    private final String dbSymbol;
    private final String scriptSymbol;

    Relation(String dbSymbol, String scriptSymbol) {
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
}
