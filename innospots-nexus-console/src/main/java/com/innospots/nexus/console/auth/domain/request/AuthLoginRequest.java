package com.innospots.nexus.console.auth.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 两个域共用的密码登录载荷。
 *
 * @author Smars
 * @date 2026/09/13
 * @param login              user_name、email 或 mobile
 * @param encryptedPassword  前端加密密码
 * @param captchaClientKey   登录图形码发放时的客户端键；策略关闭时可空
 * @param captchaCode        用户输入的图形验证码；策略关闭时可空
 */
@Schema(name = "AuthLoginRequest", description = "密码登录请求")
public record AuthLoginRequest(
        @Schema(description = "用户名、邮箱或手机号", required = true, examples = {"admin"})
        String login,
        @Schema(description = "前端加密密码", required = true)
        String encryptedPassword,
        @Schema(description = "图形验证码客户端键；策略关闭时可空")
        String captchaClientKey,
        @Schema(description = "图形验证码；策略关闭时可空")
        String captchaCode
) {
}
