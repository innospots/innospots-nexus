package com.innospots.nexus.console.auth.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
@Schema(name = "AuthTokenVo", description = "认证令牌对")
public record AuthTokenVo(
        @Schema(description = "安全域", required = true)
        SecurityRealm realm,
        @Schema(description = "令牌类型：IDENTITY 或 BUSINESS", required = true, examples = {"IDENTITY"})
        String tokenType,
        @Schema(description = "访问令牌", required = true)
        String accessToken,
        @Schema(description = "刷新令牌", required = true)
        String refreshToken,
        @Schema(description = "租户 ID（业务令牌）")
        String tenantId,
        @Schema(description = "租户成员 ID（业务令牌）")
        String tenantMemberId,
        @Schema(description = "工作区 ID")
        String workspaceId,
        @Schema(description = "项目 ID")
        String projectId
) {
}
