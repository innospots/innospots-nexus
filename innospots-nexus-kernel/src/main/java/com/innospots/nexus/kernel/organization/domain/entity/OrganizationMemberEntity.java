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

/**
 * Association between an organization unit and a tenant member.
 */
@Getter
@Setter
@Entity
@Table(name = OrganizationMemberEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_organization_member",
                columnList = "tenant_id,unit_id,tenant_member_id", unique = true)
})
@TableName(OrganizationMemberEntity.TABLE_NAME)
public class OrganizationMemberEntity extends TenantBaseEntity {

    public static final String TABLE_NAME = "nx_organization_member";

    /**
     * Association identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String organizationMemberId;

    @Override
    public String idPrefix() {
        return "ogm";
    }

    /**
     * Organization unit identifier.
     */
    @Column(length = 32, nullable = false)
    private String unitId;

    /**
     * Tenant membership identifier.
     */
    @Column(length = 32, nullable = false)
    private String tenantMemberId;
}
