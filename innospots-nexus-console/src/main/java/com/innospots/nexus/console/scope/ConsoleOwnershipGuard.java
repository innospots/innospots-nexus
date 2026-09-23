package com.innospots.nexus.console.scope;

/**
 * 控制台 API 入口校验会话是否满足数据归属层级要求。
 */
public final class ConsoleOwnershipGuard {

    private ConsoleOwnershipGuard() {
    }

    public static ConsoleOwnership requireTenantScope() {
        return ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.TENANT);
    }

    public static ConsoleOwnership requireWorkspaceScope() {
        return ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.WORKSPACE);
    }
}
