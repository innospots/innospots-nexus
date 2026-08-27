package com.innospots.nexus.kernel.workspace.domain.entity;

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
 * Tenant-scoped collaboration workspace. Isolation fields come from
 * {@link TenantBaseEntity}; this is not a Project entity.
 */
@Getter
@Setter
@Entity
@Table(name = WorkspaceEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_workspace_tenant_code", columnList = "tenant_id,workspace_code", unique = true)
})
@TableName(WorkspaceEntity.TABLE_NAME)
public class WorkspaceEntity extends TenantBaseEntity {

    public static final String TABLE_NAME = "nx_workspace";

    /**
     * Workspace identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String workspaceId;

    @Override
    public String idPrefix() {
        return "wks";
    }

    /**
     * Display name.
     */
    @Column(length = 128, nullable = false)
    private String workspaceName;

    /**
     * Tenant-unique workspace code.
     */
    @Column(length = 64, nullable = false)
    private String workspaceCode;

    /**
     * Optional description.
     */
    @Column(length = 512)
    private String description;

    /**
     * Lifecycle status.
     */
    @Column(length = 32, nullable = false)
    private String status;
}
