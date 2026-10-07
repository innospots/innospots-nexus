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
     * 预留：账号尚未完成激活。当前开通路径（邀请/直创）直接写入 {@link #ACTIVE} 或 {@link #PENDING_APPROVAL}，
     * 邀请进度以 {@code nx_pl_invite} 为准。
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
     * 预留：临时锁定。MVP 不由本模块写入；账户保护由 console 凭证/登录策略承担。
     */
    LOCKED
}
