package com.innospots.nexus.kernel.scope.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.scope.TenantScope;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.console.scope.service.SessionScopeTlc;
import com.innospots.nexus.kernel.scope.KernelProjectScopeDirectory;
import com.innospots.nexus.kernel.scope.KernelTenantScopeDirectory;
import com.innospots.nexus.kernel.scope.KernelWorkspaceScopeDirectory;

/**
 * 租户域会话绑定：通过 kernel 作用域目录解析并填充快照。
 */
@RequiredArgsConstructor
public class KernelSessionScopeBinder implements SessionScopeBinder {

    private final KernelTenantScopeDirectory tenantScopeDirectory;
    private final KernelWorkspaceScopeDirectory workspaceScopeDirectory;
    private final KernelProjectScopeDirectory projectScopeDirectory;

    @Override
    public void bindAfterAuth(AuthUser user, AuthSessionScope scope) {
        Checks.notNull(user, "user");
        Checks.notNull(scope, "scope");
        SessionScopeTlc.bindAuthUser(user);
        bindScope(scope);
    }

    @Override
    public void bindScope(AuthSessionScope scope) {
        Checks.notNull(scope, "scope");
        SessionScopeTlc.applyScopeIds(scope);
        if (scope.tenantId() != null && !scope.tenantId().isBlank()) {
            tenantScopeDirectory.findByTenantId(scope.tenantId())
                    .ifPresent(this::bindTenantScope);
        }
        KernelScopeSnapshots.bindWorkspaceAndProject(scope, workspaceScopeDirectory, projectScopeDirectory);
    }

    @Override
    public void bindFromClaims(TokenClaims claims) {
        Checks.notNull(claims, "claims");
        SessionScopeTlc.applyClaimsIdentity(claims);
        bindScope(new AuthSessionScope(
                claims.tokenType(),
                claims.tenantId(),
                claims.tenantMemberId(),
                claims.workspaceId(),
                claims.projectId()));
    }

    @Override
    public void clear() {
        SessionScopeTlc.clear();
    }

    private void bindTenantScope(TenantScope tenantScope) {
        SessionContext.bindTenant(tenantScope.tenant(), tenantScope.organization());
    }
}
