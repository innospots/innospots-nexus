package com.innospots.nexus.console.scope.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.scope.api.ProjectScopeDirectory;
import com.innospots.nexus.console.scope.api.TenantScopeDirectory;
import com.innospots.nexus.console.scope.api.WorkspaceScopeDirectory;
import com.innospots.nexus.console.scope.domain.model.TenantScope;

/**
 * Binds auth identity and scope snapshots into {@link SessionContext}.
 */
@RequiredArgsConstructor
public class SessionScopeBinder {

    private final TenantScopeDirectory tenantScopeDirectory;
    private final WorkspaceScopeDirectory workspaceScopeDirectory;
    private final ProjectScopeDirectory projectScopeDirectory;

    /**
     * Binds identity and scope snapshots after a successful auth flow.
     *
     * @param user  authenticated user
     * @param scope issued session scope
     */
    public void bindAfterAuth(AuthUser user, AuthSessionScope scope) {
        Checks.notNull(user, "user");
        Checks.notNull(scope, "scope");
        bindAuthUser(user);
        bindScope(scope);
    }

    /**
     * Binds tenant, workspace, and project snapshots for the given session scope.
     *
     * @param scope issued session scope
     */
    public void bindScope(AuthSessionScope scope) {
        Checks.notNull(scope, "scope");
        bindSessionScope(scope);
    }

    /**
     * Rebuilds session snapshots from compact token claims.
     *
     * @param claims parsed token claims
     */
    public void bindFromClaims(TokenClaims claims) {
        Checks.notNull(claims, "claims");
        TLC.securityRealm(claims.realm().name());
        TLC.put(TLC.USER_ID, claims.userId());
        if (claims.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(claims.userId());
        }
        bindSessionScope(new AuthSessionScope(
                claims.tokenType(),
                claims.tenantId(),
                claims.tenantMemberId(),
                claims.workspaceId(),
                claims.projectId()));
    }

    /**
     * Clears all bound session snapshots and TLC scope keys.
     */
    public void clear() {
        SessionContext.clearUser();
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.securityRealm(null);
        TLC.remove(TLC.USER_ID);
        TLC.platformUserId(null);
    }

    private void bindAuthUser(AuthUser user) {
        TLC.securityRealm(user.realm().name());
        TLC.userName(user.loginName());
        TLC.put(TLC.USER_ID, user.userId());
        if (user.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(user.userId());
        }
    }

    private void bindSessionScope(AuthSessionScope scope) {
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        if (scope.tenantId() != null && !scope.tenantId().isBlank()) {
            TLC.tenantId(scope.tenantId());
            TLC.tenantMemberId(scope.tenantMemberId());
            tenantScopeDirectory.findByTenantId(scope.tenantId())
                    .ifPresent(this::bindTenantScope);
        }
        if (scope.workspaceId() != null && !scope.workspaceId().isBlank()) {
            workspaceScopeDirectory.findWorkspace(scope.tenantId(), scope.workspaceId())
                    .ifPresent(SessionContext::bindWorkspace);
        }
        if (scope.projectId() != null && !scope.projectId().isBlank()) {
            projectScopeDirectory.findProject(scope.tenantId(), scope.workspaceId(), scope.projectId())
                    .ifPresent(SessionContext::bindProject);
        }
    }

    private void bindTenantScope(TenantScope tenantScope) {
        SessionContext.bindTenant(tenantScope.tenant(), tenantScope.organization());
    }
}
