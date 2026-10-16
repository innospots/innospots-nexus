package com.innospots.nexus.platform.organization.domain.entity;

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
 * 平台侧登记的租户管理员，与 {@code nx_tenant_user} 通过邮箱或用户 ID 关联。
 */
@Getter
@Setter
@Entity
@Table(name = TenantAdminEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_pl_tenant_admin_email", columnList = "tenant_id,email", unique = true),
        @Index(name = "idx_nx_pl_tenant_admin_tenant", columnList = "tenant_id")
})
@TableName(TenantAdminEntity.TABLE_NAME)
public class TenantAdminEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_pl_tenant_admin";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantAdminId;

    @Override
    public String idPrefix() {
        return "tad";
    }

    @Column(length = 32, nullable = false)
    private String tenantId;

    @Column(length = 128, nullable = false)
    private String email;

    /**
     * 以 {@link com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole} 名称持久化。
     */
    @Column(length = 32, nullable = false)
    private String adminRole;

    @Column(length = 32)
    private String tenantUserId;

    @Column(length = 128)
    private String displayName;
}
