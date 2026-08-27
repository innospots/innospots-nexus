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
 * Time-bounded grant allowing a platform user to access one tenant.
 *
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
     * Support-access grant identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String grantId;

    @Override
    public String idPrefix() {
        return "sag";
    }

    /**
     * Tenant being accessed.
     */
    @Column(length = 32, nullable = false)
    private String tenantId;

    /**
     * Platform user receiving access.
     */
    @Column(length = 32, nullable = false)
    private String platformUserId;

    /**
     * Business reason for the grant.
     */
    @Column(length = 512, nullable = false)
    private String reason;

    /**
     * Tenant-admin account that approved the grant.
     */
    @Column(length = 32)
    private String approvedBy;

    /**
     * Absolute expiry time.
     */
    @Column(nullable = false)
    private LocalDateTime expireAt;

    /**
     * Lifecycle status persisted as {@link SupportAccessStatus} name.
     */
    @Column(length = 32, nullable = false)
    private String status;
}
