package com.innospots.nexus.portal.user.domain.request;

import java.time.LocalDateTime;

/**
 * 使用外部 OAuth 身份注册租户域用户的请求对象。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userName            唯一登录用户名
 * @param displayName         界面展示的显示名称
 * @param email               邮箱地址
 * @param mobile              手机号
 * @param region              地区偏好，例如 CN
 * @param timeZone            IANA 时区，例如 Asia/Shanghai
 * @param language            界面语言，例如 zh-CN
 * @param provider            OAuth 提供方编码
 * @param providerSubject     提供方侧稳定主体标识符
 * @param providerAccount     提供方侧账号名
 * @param providerDisplayName 提供方侧显示名称
 * @param providerEmail       提供方侧邮箱
 * @param providerAvatarUrl   提供方侧头像 URL
 * @param accessTokenKey      访问令牌的安全存储键
 * @param refreshTokenKey     刷新令牌的安全存储键
 * @param tokenExpiresAt      访问令牌过期时间
 */
public record UserOauthRegisterRequest(
        String userName,
        String displayName,
        String email,
        String mobile,
        String region,
        String timeZone,
        String language,
        String provider,
        String providerSubject,
        String providerAccount,
        String providerDisplayName,
        String providerEmail,
        String providerAvatarUrl,
        String accessTokenKey,
        String refreshTokenKey,
        LocalDateTime tokenExpiresAt
) {
}
