package com.innospots.nexus.base.domain.condition;

/**
 * The target output mode for condition statements.
 * <ul>
 *   <li>{@link #DB} — SQL-compatible expressions</li>
 *   <li>{@link #SCRIPT} — script/expression language expressions</li>
 *   <li>{@link #JAVA} — Java Boolean expressions (same as SCRIPT)</li>
 * </ul>
 */
public enum Mode {
    DB,
    SCRIPT,
    JAVA
}
