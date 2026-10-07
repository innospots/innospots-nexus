package com.innospots.nexus.platform.user.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 管理员创建带本地密码的平台用户请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param loginName         平台域唯一登录名
 * @param displayName       在运营控制台展示的显示名称
 * @param email             邮箱地址
 * @param mobile            手机号
 * @param employeeNo        内部员工编号
 * @param roleCodes         默认 PLATFORM 角色编码，逗号分隔；空则跳过
 * @param encryptedPassword 前端加密密码载荷
 */
@Schema(name = "PlatformUserCreateRequest", description = "创建平台用户请求")
public record PlatformUserCreateRequest(
        @Schema(description = "平台域唯一登录名", required = true, minLength = 2, maxLength = 64)
        String loginName,
        @Schema(description = "显示名称", maxLength = 128)
        String displayName,
        @Schema(description = "邮箱地址", format = "email")
        String email,
        @Schema(description = "手机号")
        String mobile,
        @Schema(description = "内部员工编号")
        String employeeNo,
        @Schema(description = "默认角色编码，逗号分隔")
        String roleCodes,
        @Schema(description = "前端加密密码", required = true)
        String encryptedPassword
) {
}
