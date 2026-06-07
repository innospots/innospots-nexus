package com.innospots.nexus.base.domain.condition;

/**
 * Factory that selects the appropriate {@link IFactorStatement} implementation
 * based on the target {@link Mode}.
 */
public final class FactorStatementBuilder {

    private FactorStatementBuilder() {
    }

    public static IFactorStatement build(Mode mode) {
        return switch (mode) {
            case DB -> new DatabaseFactorStatement();
            case SCRIPT, JAVA -> new ScriptFactorStatement();
        };
    }
}
