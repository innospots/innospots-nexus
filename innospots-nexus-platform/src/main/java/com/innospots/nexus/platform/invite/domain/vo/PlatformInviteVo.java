package com.innospots.nexus.platform.invite.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformInviteVo", description = "平台邀请单")
public record PlatformInviteVo(
        @Schema(description = "邀请 ID", required = true)
        String inviteId,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "手机")
        String mobile,
        @Schema(description = "预置登录名")
        String loginName,
        @Schema(description = "默认角色编码，逗号分隔")
        String defaultRoleCodes,
        @Schema(description = "状态")
        String status,
        @Schema(description = "交付方式")
        String deliveryMode,
        @Schema(description = "过期时间")
        LocalDateTime expiresAt,
        @Schema(description = "邀请码（管理员可见）")
        String inviteCode,
        @Schema(description = "邀请链接")
        String inviteLink,
        @Schema(description = "接受后平台用户 ID")
        String platformUserId,
        @Schema(description = "接受时间")
        LocalDateTime acceptedAt,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}
