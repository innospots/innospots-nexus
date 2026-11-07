package com.innospots.nexus.console.scope.service;

import com.innospots.nexus.base.domain.scope.TenantScope;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.scope.directory.PortalTenantScopeDirectory;
import com.innospots.nexus.console.scope.domain.session.SessionScopeBinder;
import com.innospots.nexus.console.scope.domain.session.SessionScopeTlc;

/**
 * 默认会话作用域绑定实现。
 *
 * <p>令牌只含身份维度，因此这里只处理身份与租户；工作区与项目由请求期的
 * {@link com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeSessionFilter}
 * 依据客户端状态绑定。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
public class DefaultSessionScopeBinder implements SessionScopeBinder {

    private final PortalTenantScopeDirectory tenantScopeDirectory;

    /**
     * 创建绑定器。
     *
     * @param tenantScopeDirectory 租户作用域目录
     */
    public DefaultSessionScopeBinder(PortalTenantScopeDirectory tenantScopeDirectory) {
        this.tenantScopeDirectory = tenantScopeDirectory;
    }

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
    }

    @Override
    public void bindFromClaims(TokenClaims claims) {
        Checks.notNull(claims, "claims");
        SessionScopeTlc.applyClaimsIdentity(claims);
        bindScope(new AuthSessionScope(
                claims.tokenType(),
                claims.tenantId(),
                claims.tenantMemberId()));
    }

    @Override
    public void clear() {
        SessionScopeTlc.clear();
    }

    private void bindTenantScope(TenantScope tenantScope) {
        SessionContext.bindTenant(tenantScope.tenant(), tenantScope.organization());
    }
}
