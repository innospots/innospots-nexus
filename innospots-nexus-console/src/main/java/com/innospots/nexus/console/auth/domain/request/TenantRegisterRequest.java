package com.innospots.nexus.console.auth.domain.request;

/**
 * 租户域身份注册。不创建 TenantMember。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userName           唯一登录名
 * @param displayName        可选 显示名称
 * @param email              可选 email
 * @param mobile             可选 mobile
 * @param region             可选 region such as CN
 * @param timeZone           可选 IANA 时区
 * @param language           可选 界面语言，例如 zh-CN
 * @param encryptedPassword  前端加密密码
 */
public record TenantRegisterRequest(
        String userName,
        String displayName,
        String email,
        String mobile,
        String region,
        String timeZone,
        String language,
        String encryptedPassword
) {
}
