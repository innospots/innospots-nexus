package com.innospots.nexus.platform.user.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 运维 API 返回的平台用户概要。
 *
 * @author Smars
 * @date 2026/09/13
 * @param platformUserId platform-realm user 标识符
 * @param loginName      唯一登录名
 * @param displayName    显示名称
 * @param email          邮箱地址
 * @param mobile         手机号
 * @param employeeNo     内部员工编号
 * @param status         生命周期状态
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
        String status
) {
}
