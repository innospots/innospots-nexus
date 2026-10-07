package com.innospots.nexus.platform.invite.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteStatus;

/**
 * 平台用户邀请单。
 */
@Getter
@Setter
@Entity
@Table(name = PlatformInviteEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_pl_invite_token", columnList = "invite_token", unique = true),
        @Index(name = "uk_nx_pl_invite_code", columnList = "invite_code", unique = true),
        @Index(name = "idx_nx_pl_invite_status", columnList = "status"),
        @Index(name = "idx_nx_pl_invite_expires", columnList = "expires_at")
})
@TableName(PlatformInviteEntity.TABLE_NAME)
public class PlatformInviteEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_pl_invite";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String inviteId;

    @Override
    public String idPrefix() {
        return "piv";
    }

    @Column(length = 64, nullable = false)
    private String inviteToken;

    @Column(length = 32, nullable = false)
    private String inviteCode;

    @Column(length = 128)
    private String email;

    @Column(length = 32)
    private String mobile;

    @Column(length = 64)
    private String loginName;

    @Column(length = 512)
    private String defaultRoleCodes;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(length = 32, nullable = false)
    private String deliveryMode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime acceptedAt;

    @Column(length = 32)
    private String platformUserId;

    @Column
    private LocalDateTime revokedAt;

    @Column(nullable = false)
    private int codeFailedAttempts;

    public PlatformInviteStatus statusEnum() {
        return PlatformInviteStatus.valueOf(status);
    }

    public PlatformInviteDeliveryMode deliveryModeEnum() {
        return PlatformInviteDeliveryMode.valueOf(deliveryMode);
    }
}
