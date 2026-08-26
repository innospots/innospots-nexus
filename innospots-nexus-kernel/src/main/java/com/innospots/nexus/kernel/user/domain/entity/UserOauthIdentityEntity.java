package com.innospots.nexus.kernel.user.domain.entity;

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

import com.innospots.nexus.core.entity.BaseEntity;

/**
 * OAuth identity binding for a platform user and one external identity
 * provider subject.
 */
@Getter
@Setter
@Entity
@Table(name = UserOauthIdentityEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_user_oauth_user_id", columnList = "user_id"),
        @Index(name = "uk_nx_user_oauth_provider_subject", columnList = "provider, provider_subject", unique = true)
})
@TableName(UserOauthIdentityEntity.TABLE_NAME)
public class UserOauthIdentityEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_user_oauth";

    /**
     * OAuth identity binding identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String identityId;

    @Override
    public String idPrefix() {
        return "uoi";
    }

    /**
     * User identifier.
     */
    @Column(length = 32, nullable = false)
    private String userId;

    /**
     * External identity provider name.
     */
    @Column(length = 64, nullable = false)
    private String provider;

    /**
     * Provider-side unique subject identifier.
     */
    @Column(length = 256, nullable = false)
    private String providerSubject;

    /**
     * Provider-side account handle.
     */
    @Column(length = 128)
    private String providerAccount;

    /**
     * Display name reported by the provider.
     */
    @Column(length = 128)
    private String providerDisplayName;

    /**
     * Email address reported by the provider.
     */
    @Column(length = 128)
    private String providerEmail;

    /**
     * Avatar URL reported by the provider.
     */
    @Column(length = 512)
    private String providerAvatarUrl;

    /**
     * Storage key of the provider access token.
     */
    @Column(length = 256)
    private String accessTokenKey;

    /**
     * Storage key of the provider refresh token.
     */
    @Column(length = 256)
    private String refreshTokenKey;

    /**
     * Access token expiry time.
     */
    @Column
    private LocalDateTime tokenExpiresAt;
}
