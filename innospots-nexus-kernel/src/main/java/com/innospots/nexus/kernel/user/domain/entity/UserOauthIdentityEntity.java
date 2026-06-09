package com.innospots.nexus.kernel.user.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.entity.BaseEntity;

/**
 * OAuth identity binding for a platform user and one external identity
 * provider subject.
 */
@Getter
@Setter
@Entity
@Table(name = UserOauthIdentityEntity.TABLE_NAME)
@TableName(UserOauthIdentityEntity.TABLE_NAME)
public class UserOauthIdentityEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_user_oauth";

    @TableId(type = IdType.ASSIGN_ID)
    @Id
    @Column(nullable = false)
    private Long identityId;
    @Column(nullable = false)
    private Long userId;
    @Column(length = 64, nullable = false)
    private String provider;
    @Column(length = 256, nullable = false)
    private String providerSubject;
    @Column(length = 128)
    private String providerAccount;
    @Column(length = 128)
    private String providerDisplayName;
    @Column(length = 128)
    private String providerEmail;
    @Column(length = 512)
    private String providerAvatarUrl;
    @Column(length = 256)
    private String accessTokenKey;
    @Column(length = 256)
    private String refreshTokenKey;
    @Column
    private LocalDateTime tokenExpiresAt;
}
