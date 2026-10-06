package com.innospots.nexus.platform.invite.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformInvitePreviewVo", description = "邀请令牌预览（匿名）")
public record PlatformInvitePreviewVo(
        @Schema(description = "掩码后的邮箱")
        String maskedEmail,
        @Schema(description = "掩码后的手机")
        String maskedMobile,
        @Schema(description = "预置登录名")
        String loginName,
        @Schema(description = "过期时间")
        LocalDateTime expiresAt,
        @Schema(description = "状态")
        String status
) {
}
