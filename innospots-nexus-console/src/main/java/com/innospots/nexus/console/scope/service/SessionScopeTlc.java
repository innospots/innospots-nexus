package com.innospots.nexus.console.scope.service;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.StringUtils;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

/**
 * 会话 TLC / {@link SessionContext} 绑定的共享辅助方法。
 */
public final class SessionScopeTlc {

    /**
     * TLC 键：控制台会话归属层级（{@link ConsoleSessionScope} 名称）。
     */
    public static final String SESSION_SCOPE = "session.consoleScope";

    private SessionScopeTlc() {
    }

    public static void clear() {
        SessionContext.clearUser();
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.securityRealm(null);
        TLC.remove(TLC.USER_ID);
        TLC.platformUserId(null);
        TLC.remove(SESSION_SCOPE);
    }

    public static void bindAuthUser(AuthUser user) {
        TLC.securityRealm(user.realm().name());
        TLC.userName(user.loginName());
        TLC.put(TLC.USER_ID, user.userId());
        if (user.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(user.userId());
            putSessionScope(ConsoleSessionScope.PLATFORM);
        }
    }

    public static void applyClaimsIdentity(TokenClaims claims) {
        TLC.securityRealm(claims.realm().name());
        TLC.put(TLC.USER_ID, claims.userId());
        if (claims.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(claims.userId());
            putSessionScope(ConsoleSessionScope.PLATFORM);
        }
    }

    public static void applyScopeIds(AuthSessionScope scope) {
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        if (scope.tenantId() != null && !scope.tenantId().isBlank()) {
            TLC.tenantId(scope.tenantId());
            TLC.tenantMemberId(scope.tenantMemberId());
        }
        if (scope.workspaceId() != null && !scope.workspaceId().isBlank()) {
            TLC.workspaceId(scope.workspaceId());
        }
        if (scope.projectId() != null && !scope.projectId().isBlank()) {
            TLC.projectId(scope.projectId());
        }
        syncSessionScopeFromScopeIds(scope);
    }

    /**
     * 写入会话归属层级；与当前 {@link TLC#securityRealm()} 不一致的值会被规范化。
     */
    public static void putSessionScope(ConsoleSessionScope scope) {
        if (scope == null) {
            TLC.remove(SESSION_SCOPE);
            return;
        }
        ConsoleSessionScope normalized = normalizeSessionScope(scope);
        TLC.put(SESSION_SCOPE, normalized.name());
    }

    /**
     * 返回当前线程的有效会话归属层级（显式 TLC 值经规范化，未设置时按快照推断）。
     */
    public static ConsoleSessionScope sessionScope() {
        ConsoleSessionScope stored = parseSessionScope(TLC.getString(SESSION_SCOPE));
        ConsoleSessionScope candidate = stored != null ? stored : inferSessionScopeFromSnapshots();
        return normalizeSessionScope(candidate);
    }

    /**
     * 按安全域与工作区 / 租户快照推断会话层级（不读取显式 TLC 值）。
     */
    public static ConsoleSessionScope inferSessionScopeFromSnapshots() {
        if (isPlatformSecurityRealm()) {
            return ConsoleSessionScope.PLATFORM;
        }
        String workspaceId = SessionContext.workspaceId();
        if (workspaceId == null || workspaceId.isBlank()) {
            workspaceId = TLC.workspaceId();
        }
        if (workspaceId != null && !workspaceId.isBlank()) {
            return ConsoleSessionScope.WORKSPACE;
        }
        return ConsoleSessionScope.TENANT;
    }

    /**
     * 将会话层级约束到与 {@link TLC#securityRealm()} 一致。
     */
    public static ConsoleSessionScope normalizeSessionScope(ConsoleSessionScope scope) {
        if (scope == null) {
            return inferSessionScopeFromSnapshots();
        }
        if (isPlatformSecurityRealm()) {
            return ConsoleSessionScope.PLATFORM;
        }
        if (scope == ConsoleSessionScope.PLATFORM) {
            return inferSessionScopeFromSnapshots();
        }
        if (scope == ConsoleSessionScope.WORKSPACE) {
            String workspaceId = SessionContext.workspaceId();
            if (workspaceId == null || workspaceId.isBlank()) {
                workspaceId = TLC.workspaceId();
            }
            if (workspaceId == null || workspaceId.isBlank()) {
                return ConsoleSessionScope.TENANT;
            }
        }
        return scope;
    }

    private static void syncSessionScopeFromScopeIds(AuthSessionScope scope) {
        if (isPlatformSecurityRealm()) {
            putSessionScope(ConsoleSessionScope.PLATFORM);
            return;
        }
        if (scope.workspaceId() != null && !scope.workspaceId().isBlank()) {
            putSessionScope(ConsoleSessionScope.WORKSPACE);
            return;
        }
        if (scope.tenantId() != null && !scope.tenantId().isBlank()) {
            putSessionScope(ConsoleSessionScope.TENANT);
            return;
        }
        TLC.remove(SESSION_SCOPE);
    }

    private static boolean isPlatformSecurityRealm() {
        String realm = StringUtils.defaultIfBlank(TLC.securityRealm(), SecurityRealm.TENANT.name());
        return SecurityRealm.PLATFORM.name().equals(realm);
    }

    private static ConsoleSessionScope parseSessionScope(String raw) {
        return ConsoleSessionScope.parse(raw);
    }
}
