package com.innospots.nexus.console.auth.service;

/**
 * 已签发认证令牌对携带的作用域。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tokenType       IDENTITY 或 BUSINESS
 * @param tenantId        BUSINESS 令牌上的租户
 * @param tenantMemberId  BUSINESS 令牌上的租户成员
 * @param workspaceId     作用域 BUSINESS 令牌上的活跃工作区
 * @param projectId       作用域 BUSINESS 令牌上的活跃项目
 */
public record AuthSessionScope(
        String tokenType,
        String tenantId,
        String tenantMemberId,
        String workspaceId,
        String projectId
) {
}
