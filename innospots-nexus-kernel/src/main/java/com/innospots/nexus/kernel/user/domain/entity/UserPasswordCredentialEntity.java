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
 * Password credential persistence entity for users registered with local
 * credentials or later granted local-password login.
 */
@Getter
@Setter
@Entity
@Table(name = UserPasswordCredentialEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_user_password_user_id", columnList = "user_id", unique = true)
})
@TableName(UserPasswordCredentialEntity.TABLE_NAME)
public class UserPasswordCredentialEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_user_password";

    /**
     * Password credential identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String credentialId;

    @Override
    public String idPrefix() {
        return "upc";
    }

    /**
     * User identifier.
     */
    @Column(length = 32, nullable = false)
    private String userId;

    /**
     * Hashed password value.
     */
    @Column(length = 256, nullable = false)
    private String passwordHash;

    /**
     * Salt used when hashing the password.
     */
    @Column(length = 128, nullable = false)
    private String passwordSalt;

    /**
     * Hash algorithm name.
     */
    @Column(length = 64, nullable = false)
    private String passwordAlgorithm;

    /**
     * Version of the hashed password.
     */
    @Column(nullable = false)
    private Integer passwordVersion;

    /**
     * Whether the user must reset the password on next login.
     */
    @Column(nullable = false)
    private Boolean forceReset;

    /**
     * Consecutive failed login attempts.
     */
    @Column(nullable = false)
    private Integer failedAttempts;

    /**
     * Time until which the credential is locked.
     */
    @Column
    private LocalDateTime lockedUntil;

    /**
     * Credential expiry time.
     */
    @Column
    private LocalDateTime expiredAt;
}
