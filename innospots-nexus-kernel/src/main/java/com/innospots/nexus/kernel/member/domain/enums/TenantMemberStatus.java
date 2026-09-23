package com.innospots.nexus.kernel.member.domain.enums;

/**
 * 租户成员关系的生命周期状态。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum TenantMemberStatus {

    /**
     * 成员可访问租户。
     */
    ACTIVE,

    /**
     * 成员被阻断访问租户。
     */
    DISABLED,

    /**
     * 邀请或加入尚未完成。
     */
    PENDING
}
