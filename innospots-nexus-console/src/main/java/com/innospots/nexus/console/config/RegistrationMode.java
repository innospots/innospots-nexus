package com.innospots.nexus.console.config;

/**
 * 平台自助注册方式（部署配置项 {@code nexus.console.registration.mode}，互斥）。
 *
 * @author Smars
 * @date 2026/10/18
 * @see RegistrationConfig#mode
 * @see com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode
 */
public enum RegistrationMode {

    /** 完全开放注册（前端 {@code /register/open}，后端 OPEN 注册 API）。 */
    OPEN,

    /** 邀请码 / 邀请链接注册（{@code /register/invite} 等；管理员邮件邀请不受此枚举单独限制）。 */
    INVITE,

    /** 注册待管理员审核（{@code /register/approval}）。 */
    APPROVAL,

    /** 关闭所有自助注册公开入口。 */
    CLOSED
}
