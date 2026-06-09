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
@Table(name = UserEntity.TABLE_NAME)
@TableName(UserEntity.TABLE_NAME)
public class UserEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_user";

    @TableId(type = IdType.ASSIGN_ID)
    @Id
    @Column(nullable = false)
    private Long userId;
    @Column(length = 64, nullable = false)
    private String userName;
    @Column(length = 128)
    private String displayName;
    @Column(length = 128)
    private String realName;
    @Column(length = 128)
    private String email;
    @Column(length = 32)
    private String mobile;
    @Column(length = 256)
    private String avatarKey;
    @Column(length = 32)
    private String locale;
    @Column(length = 64)
    private String timeZone;
    @Column(length = 32, nullable = false)
    private String registerSource;
    @Column(length = 32, nullable = false)
    private String status;
    @Column(nullable = false)
    private Boolean emailVerified;
    @Column(nullable = false)
    private Boolean mobileVerified;
    @Column
    private LocalDateTime lastLoginTime;
    @Column(length = 64)
    private String lastLoginIp;
}
