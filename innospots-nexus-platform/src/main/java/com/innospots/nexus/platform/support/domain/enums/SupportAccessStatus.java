package com.innospots.nexus.platform.support.domain.enums;

/**
 * 平台支持访问授权进入租户的生命周期。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum SupportAccessStatus {

    /**
     * 等待租户管理员审批。
     */
    PENDING,

    /**
     * 当前可用至 {@code expireAt}。
     */
    ACTIVE,

    /**
     * 已过期。
     */
    EXPIRED,

    /**
     * 已显式撤销。
     */
    REVOKED
}
