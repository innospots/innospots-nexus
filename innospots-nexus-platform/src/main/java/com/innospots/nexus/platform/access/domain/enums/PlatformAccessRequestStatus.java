package com.innospots.nexus.platform.access.domain.enums;

/**
 * 平台访问申请状态。
 */
public enum PlatformAccessRequestStatus {

    /**
     * 待管理员审批。
     */
    PENDING_APPROVAL,

    /**
     * 已通过（通常伴随邀请单）。
     */
    APPROVED,

    /**
     * 已拒绝。
     */
    REJECTED
}
