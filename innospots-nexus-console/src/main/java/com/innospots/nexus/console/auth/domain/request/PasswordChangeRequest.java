package com.innospots.nexus.console.auth.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 已认证用户的密码修改。
 *
 * @author Smars
 * @date 2026/09/13
 * @param oldEncryptedPassword 当前前端加密密码
 * @param newEncryptedPassword 期望的前端加密密码
 */
@Schema(name = "PasswordChangeRequest", description = "修改密码请求")
public record PasswordChangeRequest(
        @Schema(description = "当前前端加密密码", required = true)
        String oldEncryptedPassword,
        @Schema(description = "新前端加密密码", required = true)
        String newEncryptedPassword
) {
}
