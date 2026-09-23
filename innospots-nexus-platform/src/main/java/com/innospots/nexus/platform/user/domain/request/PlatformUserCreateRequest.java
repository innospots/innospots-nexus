package com.innospots.nexus.platform.user.domain.request;

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
 * @param encryptedPassword 前端加密密码载荷
 */
public record PlatformUserCreateRequest(
        String loginName,
        String displayName,
        String email,
        String mobile,
        String employeeNo,
        String encryptedPassword
) {
}
