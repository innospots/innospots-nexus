package com.innospots.nexus.console.auth.domain.vo;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 登录、注册、刷新或租户选择后返回的令牌对。
 *
 * @author Smars
 * @date 2026/09/13
 * @param realm           PLATFORM 或 TENANT
 * @param tokenType       IDENTITY 或 BUSINESS
 * @param accessToken     访问令牌
 * @param refreshToken    刷新令牌
 * @param tenantId        在 TENANT 业务令牌上设置
 * @param tenantMemberId  在 TENANT 业务令牌上设置
 * @param workspaceId     工作区活跃时设置
 * @param projectId       项目活跃时设置
 */
public record AuthTokenVo(
        SecurityRealm realm,
        String tokenType,
        String accessToken,
        String refreshToken,
        String tenantId,
        String tenantMemberId,
        String workspaceId,
        String projectId
) {
}
