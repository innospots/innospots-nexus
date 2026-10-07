package com.innospots.nexus.base.domain.condition;

/**
 * 条件语句的目标输出模式。
 * <ul>
 *   <li>{@link #DB} — SQL 兼容表达式</li>
 *   <li>{@link #SCRIPT} — 脚本/表达式语言</li>
 *   <li>{@link #JAVA} — Java 布尔表达式（与 SCRIPT 相同）</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see IFactorStatement
 */
public enum Mode {
    /** 数据库 SQL 模式 */
    DB,
    /** 脚本表达式模式 */
    SCRIPT,
    /** Java 表达式模式 */
    JAVA
}
