package com.innospots.nexus.console.auth.domain.request;

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
public record AuthLoginRequest(
        String login,
        String encryptedPassword,
        String captchaClientKey,
        String captchaCode
) {
}
