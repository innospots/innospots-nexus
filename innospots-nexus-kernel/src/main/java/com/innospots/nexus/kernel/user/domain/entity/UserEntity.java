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

import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * Tenant-realm login identity. Membership in a tenant is {@code nx_tenant_member},
 * not this table.
 */
@Getter
@Setter
@Entity
@Table(name = UserEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_tenant_user_user_name", columnList = "user_name", unique = true),
        @Index(name = "uk_nx_tenant_user_email", columnList = "email", unique = true),
        @Index(name = "uk_nx_tenant_user_mobile", columnList = "mobile", unique = true),
        @Index(name = "idx_nx_tenant_user_status", columnList = "status")
})
@TableName(UserEntity.TABLE_NAME)
public class UserEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_tenant_user";

    /**
     * Tenant-realm user identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantUserId;

    @Override
    public String idPrefix() {
        return "tus";
    }

    /**
     * Unique login user name in the tenant realm.
     */
    @Column(length = 64, nullable = false)
    private String userName;

    /**
     * Display name; empty values fall back to {@code userName} in UI.
     */
    @Column(length = 128)
    private String displayName;

    /**
     * Email address; unique when present.
     */
    @Column(length = 128)
    private String email;

    /**
     * Mobile number; unique when present.
     */
    @Column(length = 32)
    private String mobile;

    /**
     * Region preference such as CN or US.
     */
    @Column(length = 32)
    private String region;

    /**
     * IANA time zone such as Asia/Shanghai.
     */
    @Column(length = 64)
    private String timeZone;

    /**
     * UI language such as zh-CN.
     */
    @Column(length = 32)
    private String language;

    /**
     * Avatar storage key.
     */
    @Column(length = 256)
    private String avatarKey;

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
