package com.innospots.nexus.portal.workspace.domain.entity;

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

/**
 * 租户作用域协作工作区。隔离字段来自
 * {@link TenantBaseEntity}；这不是 Project 实体。
 *
 * @author Smars
 * @date 2026/09/13
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
     * 工作区标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String workspaceId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "wks";
    }

    /**
     * 显示名称。
     */
    @Column(length = 128, nullable = false)
    private String workspaceName;

    /**
     * 租户内唯一 workspace code。
     */
    @Column(length = 64, nullable = false)
    private String workspaceCode;

    /**
     * 可选描述。
     */
    @Column(length = 512)
    private String description;

    /**
     * 生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;
}
