package com.innospots.nexus.portal.user.domain.request;

/**
 * 使用本地密码凭证注册租户域用户的请求对象。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userName          唯一登录用户名
 * @param displayName       界面展示的显示名称
 * @param email             邮箱地址
 * @param mobile            手机号
 * @param region            地区偏好，例如 CN
 * @param timeZone          IANA 时区，例如 Asia/Shanghai
 * @param language          界面语言，例如 zh-CN
 * @param encryptedPassword 前端加密密码载荷
 */
public record UserPasswordRegisterRequest(
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
