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
 * 租户域登录身份。租户成员关系在 {@code nx_tenant_member}，
 * 而非本表。
 *
 * @author Smars
 * @date 2026/09/13
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
     * 租户域用户标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantUserId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "tus";
    }

    /**
     * 租户域唯一登录用户名。
     */
    @Column(length = 64, nullable = false)
    private String userName;

    /**
     * 显示名称; empty values fall back to {@code userName} in UI。
     */
    @Column(length = 128)
    private String displayName;

    /**
     * 邮箱地址；存在时唯一。
     */
    @Column(length = 128)
    private String email;

    /**
     * 手机号；存在时唯一。
     */
    @Column(length = 32)
    private String mobile;

    /**
     * 地区偏好，例如 CN 或 US。
     */
    @Column(length = 32)
    private String region;

    /**
     * IANA 时区，例如 Asia/Shanghai。
     */
    @Column(length = 64)
    private String timeZone;

    /**
     * 界面语言，例如 zh-CN。
     */
    @Column(length = 32)
    private String language;

    /**
     * 头像存储键。
     */
    @Column(length = 256)
    private String avatarKey;

    /**
     * 原始注册来源。
     */
    @Column(length = 32, nullable = false)
    private String registerSource;

    /**
     * 生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * 邮箱是否已验证。
     */
    @Column(nullable = false)
    private Boolean emailVerified;

    /**
     * 手机号是否已验证。
     */
    @Column(nullable = false)
    private Boolean mobileVerified;

    /**
     * 上次成功登录时间。
     */
    @Column
    private LocalDateTime lastLoginTime;

    /**
     * 上次成功登录 IP 地址。
     */
    @Column(length = 64)
    private String lastLoginIp;
}
