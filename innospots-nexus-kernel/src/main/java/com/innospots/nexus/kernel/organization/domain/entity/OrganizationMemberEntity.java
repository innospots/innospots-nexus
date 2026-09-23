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

import com.innospots.nexus.kernel.persistence.entity.TenantBaseEntity;

/**
 * 组织单元与租户成员之间的关联。
 *
 * @author Smars
 * @date 2026/09/13
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
     * 关联标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String organizationMemberId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "ogm";
    }

    /**
     * 组织单元标识符。
     */
    @Column(length = 32, nullable = false)
    private String unitId;

    /**
     * 租户成员关系标识符。
     */
    @Column(length = 32, nullable = false)
    private String tenantMemberId;
}
