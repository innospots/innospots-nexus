package com.innospots.nexus.console.user.admin.domain.entity;

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

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;
import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * 后台管理登录身份。
 *
 * <p>由管理员创建，非公开自助注册。通过 {@link #securityRealm} 区分该身份归属的
 * 安全域：</p>
 * <ul>
 *   <li>{@link SecurityRealm#PLATFORM}：运营平台（admin）自身的后台账号；</li>
 *   <li>{@link SecurityRealm#TENANT}：租户/用户侧（portal）的后台账号，
 *       用于让 portal 用户登录后访问后台管理功能。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see AdminUserStatus
 * @see SecurityRealm
 */
@Getter
@Setter
@Entity
@Table(name = AdminUserEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_admin_user_login_name", columnList = "login_name", unique = true),
        @Index(name = "idx_nx_admin_user_status", columnList = "status"),
        @Index(name = "idx_nx_admin_user_realm", columnList = "security_realm")
})
@TableName(AdminUserEntity.TABLE_NAME)
public class AdminUserEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_admin_user";

    /**
     * 后台管理用户标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String adminUserId;

    /**
     * 返回主键前缀。
     *
     * @return 主键前缀
     */
    @Override
    public String idPrefix() {
        return "ADM";
    }

    /**
     * 该身份归属的安全域：{@code PLATFORM}（运营平台）或 {@code TENANT}（租户/用户侧）。
     */
    @Column(length = 32, nullable = false)
    private String securityRealm;

    /**
     * 唯一登录名。
     */
    @Column(length = 64, nullable = false)
    private String loginName;

    /**
     * 管理控制台展示的显示名称。
     */
    @Column(length = 128)
    private String displayName;

    /**
     * 邮箱地址。
     */
    @Column(length = 128)
    private String email;

    /**
     * 手机号。
     */
    @Column(length = 32)
    private String mobile;

    /**
     * 内部员工编号。
     */
    @Column(length = 64)
    private String employeeNo;

    /**
     * 以 {@link AdminUserStatus} 名称持久化的生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;

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

    /**
     * 返回该身份的安全域枚举，无法识别时回退 {@link SecurityRealm#PLATFORM}。
     *
     * @return 安全域
     */
    public SecurityRealm realm() {
        if (securityRealm == null || securityRealm.isBlank()) {
            return SecurityRealm.PLATFORM;
        }
        for (SecurityRealm candidate : SecurityRealm.values()) {
            if (candidate.name().equalsIgnoreCase(securityRealm.trim())) {
                return candidate;
            }
        }
        return SecurityRealm.PLATFORM;
    }
}
