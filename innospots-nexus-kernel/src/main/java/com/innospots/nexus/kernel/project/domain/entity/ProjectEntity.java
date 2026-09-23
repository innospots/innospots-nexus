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

import com.innospots.nexus.kernel.persistence.entity.TenantWorkspaceBaseEntity;

/**
 * 工作区作用域内的业务项目. Projects provide isolation under a shared workspace。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@Entity
@Table(name = ProjectEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_project_workspace_code", columnList = "workspace_id,project_code", unique = true)
})
@TableName(ProjectEntity.TABLE_NAME)
public class ProjectEntity extends TenantWorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_project";

    /**
     * 项目标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String projectId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "prj";
    }

    /**
     * 显示名称。
     */
    @Column(length = 128, nullable = false)
    private String projectName;

    /**
     * 工作区内唯一 project code。
     */
    @Column(length = 64, nullable = false)
    private String projectCode;

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
