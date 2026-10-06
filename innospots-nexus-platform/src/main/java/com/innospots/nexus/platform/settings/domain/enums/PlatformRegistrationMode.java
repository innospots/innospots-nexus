package com.innospots.nexus.platform.settings.domain.enums;

/**
 * 平台自助注册模式（三选一，互斥）。
 *
 * <p>持久化于 {@code platform.registration.mode}；仅一种模式的公开注册 API 在同一时刻可用。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService
 */
public enum PlatformRegistrationMode {

    /** 完全开放注册：OTP 通过后立即 {@code ACTIVE}。 */
    OPEN,

    /** 邀请注册：注册页邀请码；管理员邀请链接不受此枚举单独限制。 */
    INVITE,

    /** 注册待审核：注册完成 {@code PENDING_APPROVAL}，审批通过后 {@code ACTIVE}。 */
    APPROVAL
}
