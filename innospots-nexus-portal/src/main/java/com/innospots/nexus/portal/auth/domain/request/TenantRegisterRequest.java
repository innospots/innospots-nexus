package com.innospots.nexus.portal.auth.domain.request;

/**
 * 租户域身份注册。不创建 TenantMember。
 *
 * @param userName           唯一登录名
 * @param displayName        可选显示名称
 * @param email              可选 email
 * @param mobile             可选 mobile
 * @param region             可选 region such as CN
 * @param timeZone           可选 IANA 时区
 * @param language           可选界面语言，例如 zh-CN
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
