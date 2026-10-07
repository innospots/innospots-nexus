package com.innospots.nexus.base.domain.condition;

/**
 * 用于连接多个 {@link Factor} 条件的逻辑组合符。每种关系在 SQL 与脚本上下文中各有符号。
 *
 * @author Smars
 * @date 2026/09/13
 * @see Factor
 * @see Mode
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

    /**
     * 返回指定模式下的关系符号。
     *
     * @param mode 输出模式
     * @return 关系符号
     */
    public String symbol(Mode mode) {
        return mode == Mode.DB ? dbSymbol : scriptSymbol;
    }

    /**
     * 返回 SQL 关系符号。
     *
     * @return SQL 符号
     */
    public String dbSymbol() {
        return dbSymbol;
    }

    /**
     * 返回脚本关系符号。
     *
     * @return 脚本符号
     */
    public String scriptSymbol() {
        return scriptSymbol;
    }
}
