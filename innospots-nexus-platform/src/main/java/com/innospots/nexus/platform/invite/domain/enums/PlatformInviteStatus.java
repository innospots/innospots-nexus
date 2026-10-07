package com.innospots.nexus.platform.invite.domain.enums;

/**
 * 平台邀请单生命周期状态。
 */
public enum PlatformInviteStatus {

    /**
     * 待受邀人接受。
     */
    PENDING,

    /**
     * 已完成开通。
     */
    ACCEPTED,

    /**
     * 管理员撤销。
     */
    REVOKED,

    /**
     * 超过有效期。
     */
    EXPIRED
}
