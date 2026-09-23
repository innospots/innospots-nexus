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
 * 运维域登录身份。平台用户由管理员创建，
 * 而非公开自助注册。
 *
 * @author Smars
 * @date 2026/09/13
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
     * 平台域 user 标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String platformUserId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "pus";
    }

    /**
     * 平台域唯一登录名。
     */
    @Column(length = 64, nullable = false)
    private String loginName;

    /**
     * 运维控制台展示的显示名称。
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
     * 以 {@link PlatformUserStatus} 名称持久化的生命周期状态。
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
}
