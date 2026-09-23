package com.innospots.nexus.platform.user.domain.enums;

/**
 * 平台用户生命周期状态。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum PlatformUserStatus {

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
