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

    @TableId(type = IdType.INPUT)
    @Id
    @Column(length = 32, nullable = false)
    private String credentialId;
    @Column(length = 32, nullable = false)
    private String userId;
    @Column(length = 256, nullable = false)
    private String passwordHash;
    @Column(length = 128, nullable = false)
    private String passwordSalt;
    @Column(length = 64, nullable = false)
    private String passwordAlgorithm;
    @Column(nullable = false)
    private Integer passwordVersion;
    @Column(nullable = false)
    private Boolean forceReset;
    @Column(nullable = false)
    private Integer failedAttempts;
    @Column
    private LocalDateTime lockedUntil;
    @Column
    private LocalDateTime expiredAt;
}
