package com.innospots.nexus.console.auth.domain.model;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 加密进访问或刷新令牌的紧凑声明。
 *
 * <p>令牌只承载<b>身份</b>与<b>安全域</b>：谁登录了、属于哪个租户、租户成员关系是什么。
 * 工作区与项目属于「当前操作上下文」，每次请求都可能变化，因此不入令牌，
 * 由客户端状态（cookie）声明并在请求期重新校验（见
 * {@code WorkspaceScopeResolver}）。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @param realm          PLATFORM 或 TENANT
 * @param purpose        ACCESS 或 REFRESH
 * @param tokenType      IDENTITY 或 BUSINESS
 * @param userId         platform 或 tenant user 标识符
 * @param tenantId       租户域令牌上的租户
 * @param tenantMemberId 租户域令牌上的租户成员
 * @param expiresAt      epoch 秒级过期时间
 */
public record TokenClaims(
        SecurityRealm realm,
        String purpose,
        String tokenType,
        String userId,
        String tenantId,
        String tenantMemberId,
        long expiresAt
) {
}
