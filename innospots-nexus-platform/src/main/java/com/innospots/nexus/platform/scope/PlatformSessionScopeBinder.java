package com.innospots.nexus.platform.scope;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.console.scope.service.SessionScopeTlc;

/**
 * 运营平台会话绑定：仅同步身份与令牌中的作用域标识，不加载租户 / 工作区 / 项目快照。
 */
public class PlatformSessionScopeBinder implements SessionScopeBinder {

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
}
