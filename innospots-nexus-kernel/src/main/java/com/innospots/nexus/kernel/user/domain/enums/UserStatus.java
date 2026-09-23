package com.innospots.nexus.kernel.user.domain.enums;

/**
 * 用户生命周期状态。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum UserStatus {

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
    LOCKED,

    /**
     * 等待激活或验证。
     */
    PENDING
}
