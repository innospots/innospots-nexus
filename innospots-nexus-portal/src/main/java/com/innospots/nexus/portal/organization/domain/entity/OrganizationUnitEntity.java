package com.innospots.nexus.portal.organization.domain.entity;

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

import com.innospots.nexus.portal.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.portal.organization.domain.enums.OrganizationUnitType;

/**
 * 租户内部组织单元，不是 {@code nx_enterprise} 企业档案实体。
 *
 * @author Smars
 * @date 2026/09/13
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
     * 组织单元标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String unitId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "org";
    }

    /**
     * 父单元标识符；树根部为 null。
     */
    @Column(length = 32)
    private String parentId;

    /**
     * 租户内唯一 unit code。
     */
    @Column(length = 64, nullable = false)
    private String unitCode;

    /**
     * 显示名称。
     */
    @Column(length = 128, nullable = false)
    private String unitName;

    /**
     * 以 {@link OrganizationUnitType} 名称持久化的节点类型。
     */
    @Column(length = 32, nullable = false)
    private String unitType;

    /**
     * 同级显示顺序。
     */
    @Column(nullable = false)
    private Integer sortOrder;

    /**
     * 生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;
}
