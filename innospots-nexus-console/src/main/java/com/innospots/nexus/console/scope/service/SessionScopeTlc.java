package com.innospots.nexus.console.scope.service;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
/**
 * 会话 TLC / {@link SessionContext} 绑定的共享辅助方法。
 */
public final class SessionScopeTlc {

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
    }

    public static void bindAuthUser(AuthUser user) {
        TLC.securityRealm(user.realm().name());
        TLC.userName(user.loginName());
        TLC.put(TLC.USER_ID, user.userId());
        if (user.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(user.userId());
        }
    }

    public static void applyClaimsIdentity(TokenClaims claims) {
        TLC.securityRealm(claims.realm().name());
        TLC.put(TLC.USER_ID, claims.userId());
        if (claims.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(claims.userId());
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
    }

}
