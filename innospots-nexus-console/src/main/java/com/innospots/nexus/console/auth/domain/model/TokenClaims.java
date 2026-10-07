package com.innospots.nexus.console.auth.domain.model;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 加密进访问或刷新令牌的紧凑声明。
 *
 * @author Smars
 * @date 2026/09/13
 * @param realm           PLATFORM 或 TENANT
 * @param purpose         ACCESS 或 REFRESH
 * @param tokenType       IDENTITY 或 BUSINESS
 * @param userId          platform or tenant user 标识符
 * @param tenantId        BUSINESS 令牌上的租户
 * @param tenantMemberId  BUSINESS 令牌上的租户成员
 * @param workspaceId     作用域 BUSINESS 令牌上的活跃工作区
 * @param projectId       作用域 BUSINESS 令牌上的活跃项目
 * @param expiresAt       epoch 秒级过期时间
 */
public record TokenClaims(
        SecurityRealm realm,
        String purpose,
        String tokenType,
        String userId,
        String tenantId,
        String tenantMemberId,
        String workspaceId,
        String projectId,
        long expiresAt
) {
}
