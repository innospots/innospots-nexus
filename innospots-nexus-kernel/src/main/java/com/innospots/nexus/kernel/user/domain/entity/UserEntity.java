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
 * User persistence entity for registration profile and lifecycle state.
 * <p>Passwords are intentionally stored in {@link UserPasswordCredentialEntity}
 * so OAuth-only users can exist without local password material.</p>
 *
 * @see UserPasswordCredentialEntity
 * @see UserOauthIdentityEntity
 */
@Getter
@Setter
@Entity
@Table(name = UserEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_user_user_name", columnList = "user_name", unique = true),
        @Index(name = "idx_nx_user_real_name", columnList = "real_name"),
        @Index(name = "idx_nx_user_email", columnList = "email"),
        @Index(name = "idx_nx_user_mobile", columnList = "mobile"),
        @Index(name = "idx_nx_user_status", columnList = "status")
})
@TableName(UserEntity.TABLE_NAME)
public class UserEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_user";

    /**
     * User identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String userId;

    @Override
    public String idPrefix() {
        return "usr";
    }

    /**
     * Unique login user name.
     */
    @Column(length = 64, nullable = false)
    private String userName;

    /**
     * Display name shown in the UI.
     */
    @Column(length = 128)
    private String displayName;

    /**
     * Legal or real-world name when provided.
     */
    @Column(length = 128)
    private String realName;

    /**
     * Email address.
     */
    @Column(length = 128)
    private String email;

    /**
     * Mobile phone number.
     */
    @Column(length = 32)
    private String mobile;

    /**
     * Avatar storage key.
     */
    @Column(length = 256)
    private String avatarKey;

    /**
     * Preferred locale.
     */
    @Column(length = 32)
    private String locale;

    /**
     * Preferred time zone.
     */
    @Column(length = 64)
    private String timeZone;

    /**
     * Original registration source.
     */
    @Column(length = 32, nullable = false)
    private String registerSource;

    /**
     * Lifecycle status.
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * Whether the email address is verified.
     */
    @Column(nullable = false)
    private Boolean emailVerified;

    /**
     * Whether the mobile number is verified.
     */
    @Column(nullable = false)
    private Boolean mobileVerified;

    /**
     * Last successful login time.
     */
    @Column
    private LocalDateTime lastLoginTime;

    /**
     * Last successful login IP address.
     */
    @Column(length = 64)
    private String lastLoginIp;
}
