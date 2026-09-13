package com.innospots.nexus.kernel.project.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

/**
 * Workspace-scoped business project. Projects provide isolation under a shared workspace.
 */
@Getter
@Setter
@Entity
@Table(name = ProjectEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_project_workspace_code", columnList = "workspace_id,project_code", unique = true)
})
@TableName(ProjectEntity.TABLE_NAME)
public class ProjectEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_project";

    /**
     * Project identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String projectId;

    @Override
    public String idPrefix() {
        return "prj";
    }

    /**
     * Display name.
     */
    @Column(length = 128, nullable = false)
    private String projectName;

    /**
     * Workspace-unique project code.
     */
    @Column(length = 64, nullable = false)
    private String projectCode;

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
