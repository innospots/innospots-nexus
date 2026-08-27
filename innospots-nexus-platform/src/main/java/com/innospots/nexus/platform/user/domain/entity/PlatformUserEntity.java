package com.innospots.nexus.platform.user.domain.entity;

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
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;

/**
 * Ops-domain login identity. Platform users are created by administrators,
 * not by public self-registration.
 *
 * @see PlatformUserStatus
 */
@Getter
@Setter
@Entity
@Table(name = PlatformUserEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_platform_user_login_name", columnList = "login_name", unique = true),
        @Index(name = "idx_nx_platform_user_status", columnList = "status")
})
@TableName(PlatformUserEntity.TABLE_NAME)
public class PlatformUserEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_platform_user";

    /**
     * Platform-realm user identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String platformUserId;

    @Override
    public String idPrefix() {
        return "pus";
    }

    /**
     * Unique login name in the platform realm.
     */
    @Column(length = 64, nullable = false)
    private String loginName;

    /**
     * Display name shown in the ops console.
     */
    @Column(length = 128)
    private String displayName;

    /**
     * Email address.
     */
    @Column(length = 128)
    private String email;

    /**
     * Mobile number.
     */
    @Column(length = 32)
    private String mobile;

    /**
     * Internal employee number.
     */
    @Column(length = 64)
    private String employeeNo;

    /**
     * Lifecycle status persisted as {@link PlatformUserStatus} name.
     */
    @Column(length = 32, nullable = false)
    private String status;

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
