package com.innospots.nexus.platform.invite.domain.enums;

/**
 * 邀请交付方式。
 */
public enum PlatformInviteDeliveryMode {

    /**
     * 在线邮件/短信通知（经 OTP 事件投递）。
     */
    ONLINE,

    /**
     * 仅生成链接与邀请码，由运营线下抄送。
     */
    OFFLINE
}
