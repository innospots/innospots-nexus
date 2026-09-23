package com.innospots.nexus.base.domain.condition;

/**
 * 过滤条件中使用的比较运算符。每个运算符在 SQL（{@code dbSymbol}）与脚本（{@code scriptSymbol}）上下文中携带独立符号。
 *
 * @author Smars
 * @date 2026/09/13
 * @see Mode
 * @see Factor
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

    /**
     * 返回指定模式下的运算符符号。
     *
     * @param mode 输出模式
     * @return 运算符符号
     */
    public String symbol(Mode mode) {
        return mode == Mode.DB ? dbSymbol : scriptSymbol;
    }

    /**
     * 返回 SQL 运算符符号。
     *
     * @return SQL 符号
     */
    public String dbSymbol() {
        return dbSymbol;
    }

    /**
     * 返回脚本运算符符号。
     *
     * @return 脚本符号
     */
    public String scriptSymbol() {
        return scriptSymbol;
    }

    /**
     * 判断是否为范围比较运算符。
     *
     * @return 是范围运算符时返回 {@code true}
     */
    public boolean isRange() {
        return this == BETWEEN
                || this == GREATER
                || this == GREATER_EQUAL
                || this == LESS
                || this == LESS_EQUAL;
    }
}
