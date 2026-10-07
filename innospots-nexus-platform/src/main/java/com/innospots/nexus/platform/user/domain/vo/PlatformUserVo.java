package com.innospots.nexus.platform.user.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 运营管理 API 返回的平台用户概要。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Schema(name = "PlatformUserVo", description = "平台用户概要")
public record PlatformUserVo(
        @Schema(description = "平台用户 ID", required = true)
        String platformUserId,
        @Schema(description = "登录名", required = true)
        String loginName,
        @Schema(description = "显示名称")
        String displayName,
        @Schema(description = "邮箱", format = "email")
        String email,
        @Schema(description = "手机号")
        String mobile,
        @Schema(description = "员工编号")
        String employeeNo,
        @Schema(description = "状态", examples = {"ACTIVE"})
        String status,
        @Schema(description = "上次成功登录时间")
        LocalDateTime lastLoginTime,
        @Schema(description = "上次成功登录 IP")
        String lastLoginIp,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}
