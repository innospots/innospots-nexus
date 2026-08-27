package com.innospots.nexus.console.permission.domain.entity;

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
 * 角色或组织单元对单个权限资源的授权记录。
 *
 * <p>记录存在即表示授权生效，撤销授权时删除记录。datasource 的附加查询条件与授权记录
 * 一起保存，当前阶段只负责存取，不在权限领域内执行查询拼接。</p>
 */
@Getter
@Setter
@Entity
@Table(name = PermissionGrantEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_permission_grant_subject_resource",
                columnList = "workspace_id,subject_type,subject_id,resource_id", unique = true),
        @Index(name = "idx_nx_permission_grant_subject",
                columnList = "workspace_id,subject_type,subject_id"),
        @Index(name = "idx_nx_permission_grant_resource",
                columnList = "workspace_id,resource_id"),
        @Index(name = "idx_nx_permission_grant_realm", columnList = "security_realm")
})
@TableName(PermissionGrantEntity.TABLE_NAME)
public class PermissionGrantEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_permission_grant";

    /** 授权记录主键。 */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String grantId;

    /** 授权主体类型，取值为 ROLE 或 ORG_UNIT。 */
    @Column(length = 32, nullable = false)
    private String subjectType;

    /** 授权主体 ID，具体含义由 subjectType 决定。 */
    @Column(length = 32, nullable = false)
    private String subjectId;

    /** 被授权的资源主键。 */
    @Column(length = 32, nullable = false)
    private String resourceId;

    /**
     * 管理端配置的 datasource 附加查询条件。
     * 当前按不透明文本保存和返回，后续由数据访问适配器解释；不允许将 SQL、脚本或可执行代码写入此处。
     */
    @Column(columnDefinition = "text")
    private String constraintDefinition;

    /** 安全域：PLATFORM 或 TENANT。 */
    @Column(length = 32, nullable = false)
    private String securityRealm;

    /** 返回授权记录主键使用的 ID 前缀。 */
    @Override
    public String idPrefix() {
        return "pgr";
    }
}
