package com.innospots.nexus.kernel.organization.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.kernel.organization.domain.enums.OrganizationUnitType;

/**
 * Tenant-internal organization unit. This is not {@code nx_enterprise}.
 *
 * @see OrganizationUnitType
 */
@Getter
@Setter
@Entity
@Table(name = OrganizationUnitEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_organization_unit_code", columnList = "tenant_id,unit_code", unique = true),
        @Index(name = "idx_nx_organization_unit_parent", columnList = "tenant_id,parent_id,sort_order")
})
@TableName(OrganizationUnitEntity.TABLE_NAME)
public class OrganizationUnitEntity extends TenantBaseEntity {

    public static final String TABLE_NAME = "nx_organization_unit";

    /**
     * Organization unit identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String unitId;

    @Override
    public String idPrefix() {
        return "org";
    }

    /**
     * Parent unit identifier; null for the tree root.
     */
    @Column(length = 32)
    private String parentId;

    /**
     * Tenant-unique unit code.
     */
    @Column(length = 64, nullable = false)
    private String unitCode;

    /**
     * Display name.
     */
    @Column(length = 128, nullable = false)
    private String unitName;

    /**
     * Node type persisted as {@link OrganizationUnitType} name.
     */
    @Column(length = 32, nullable = false)
    private String unitType;

    /**
     * Sibling display order.
     */
    @Column(nullable = false)
    private Integer sortOrder;

    /**
     * Lifecycle status.
     */
    @Column(length = 32, nullable = false)
    private String status;
}
