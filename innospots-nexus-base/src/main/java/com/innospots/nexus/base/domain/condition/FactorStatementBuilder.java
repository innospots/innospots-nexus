package com.innospots.nexus.base.domain.condition;

/**
 * 根据目标 {@link Mode} 选择合适 {@link IFactorStatement} 实现的工厂。
 *
 * @author Smars
 * @date 2026/09/13
 * @see IFactorStatement
 * @see DatabaseFactorStatement
 * @see ScriptFactorStatement
 */
public final class FactorStatementBuilder {

    private FactorStatementBuilder() {
    }

    /**
     * 按模式构建因子语句渲染器。
     *
     * @param mode 输出模式
     * @return 因子语句渲染器
     */
    public static IFactorStatement build(Mode mode) {
        return switch (mode) {
            case DB -> new DatabaseFactorStatement();
            case SCRIPT, JAVA -> new ScriptFactorStatement();
        };
    }
}
