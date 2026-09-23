package com.innospots.nexus.platform.support.domain.entity;

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
import com.innospots.nexus.platform.support.domain.enums.SupportAccessStatus;

/**
 * 允许平台用户访问单个租户的时间限定授权。
 *
 * @author Smars
 * @date 2026/09/13
 * @see SupportAccessStatus
 */
@Getter
@Setter
@Entity
@Table(name = SupportAccessGrantEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_support_access_tenant", columnList = "tenant_id"),
        @Index(name = "idx_nx_support_access_user", columnList = "platform_user_id")
})
@TableName(SupportAccessGrantEntity.TABLE_NAME)
public class SupportAccessGrantEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_support_access_grant";

    /**
     * 支持访问授权标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String grantId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "sag";
    }

    /**
     * 被访问的租户。
     */
    @Column(length = 32, nullable = false)
    private String tenantId;

    /**
     * 获得访问权限的平台用户。
     */
    @Column(length = 32, nullable = false)
    private String platformUserId;

    /**
     * 授权的业务原因。
     */
    @Column(length = 512, nullable = false)
    private String reason;

    /**
     * 批准该授权的租户管理员账号。
     */
    @Column(length = 32)
    private String approvedBy;

    /**
     * 绝对过期时间。
     */
    @Column(nullable = false)
    private LocalDateTime expireAt;

    /**
     * 以 {@link SupportAccessStatus} 名称持久化的生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;
}
