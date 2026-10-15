package com.innospots.nexus.platform.scope;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipLevel;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 运营平台控制台会话边界：仅 {@link ConsoleSessionScope#PLATFORM} 且归属为 PLATFORM。
 */
public final class PlatformConsoleSession {

    private PlatformConsoleSession() {
    }

    /**
     * 校验会话并返回 PLATFORM 归属（无租户/工作区快照）。
     */
    public static ConsoleOwnership requireOwnership() {
        requirePlatformScope();
        ConsoleOwnership ownership = ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        if (ownership.ownerType() != RoleOwnerType.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        return ownership;
    }

    private static void requirePlatformScope() {
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }
}
