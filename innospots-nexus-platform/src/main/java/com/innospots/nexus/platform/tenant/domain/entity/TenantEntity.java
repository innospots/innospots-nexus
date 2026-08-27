package com.innospots.nexus.platform.tenant.domain.entity;

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
import com.innospots.nexus.platform.tenant.domain.enums.TenantStatus;

/**
 * Platform-owned tenant lifecycle record.
 * <p>Enterprise legal profile is stored separately in
 * {@code nx_enterprise}; this table is the ops-side tenant identity.</p>
 *
 * @see TenantStatus
 */
@Getter
@Setter
@Entity
@Table(name = TenantEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_tenant_code", columnList = "tenant_code", unique = true)
})
@TableName(TenantEntity.TABLE_NAME)
public class TenantEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_tenant";

    /**
     * Tenant identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantId;

    @Override
    public String idPrefix() {
        return "tnt";
    }

    /**
     * Display name of the tenant.
     */
    @Column(length = 128, nullable = false)
    private String tenantName;

    /**
     * Stable unique tenant code.
     */
    @Column(length = 64, nullable = false)
    private String tenantCode;

    /**
     * Lifecycle status persisted as {@link TenantStatus} name.
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * Optional commercial plan code.
     */
    @Column(length = 64)
    private String planCode;

    /**
     * Initial owner identity in the tenant-user realm.
     */
    @Column(length = 32)
    private String ownerTenantUserId;
}
