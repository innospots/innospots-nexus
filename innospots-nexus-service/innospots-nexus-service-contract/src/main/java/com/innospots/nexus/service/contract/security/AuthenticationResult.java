package com.innospots.nexus.service.contract.security;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;

/**
 * 预认证上下文认证结果。{@code expiresAt} 对主机校验 mTLS 等不过期机制可为 null。
 *
 * @param principal 已认证主体
 * @param scope     已解析作用域
 * @param expiresAt 可选过期时间
 * @author Smars
 * @date 2026/09/13
 * @see SecurityProvider
 */
public record AuthenticationResult(ServicePrincipal principal, ServiceScope scope, Instant expiresAt) {

    public AuthenticationResult {
        Checks.notNull(principal, "principal");
        Checks.notNull(scope, "scope");
    }
}
