package com.innospots.nexus.platform.user.domain.enums;

/**
 * 平台用户生命周期状态（持久化为 {@code nx_pl_user.status} 名称）。
 *
 * <p>仅 {@link #ACTIVE} 可通过 {@link com.innospots.nexus.platform.auth.adapter.PlatformUserDirectory} 登录。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity
 */
public enum PlatformUserStatus {

    /**
     * 账号已创建或邀请已发出，尚未完成激活（例如未设密或未接受邀请）。
     */
    PENDING_ACTIVATION,

    /**
     * 待审核注册已完成设密，管理员审批通过前不可登录。
     */
    PENDING_APPROVAL,

    /**
     * 正常运营状态。
     */
    ACTIVE,

    /**
     * 被管理员禁用。
     */
    DISABLED,

    /**
     * 因策略或多次失败被临时锁定。
     */
    LOCKED
}
