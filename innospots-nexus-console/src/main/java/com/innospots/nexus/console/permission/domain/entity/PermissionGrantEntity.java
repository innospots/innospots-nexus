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

import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 角色或组织单元对单个权限资源的授权记录；归属列表示授权生效的工作区（或租户）上下文。
 */
@Getter
@Setter
@Entity
@Table(name = PermissionGrantEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_permission_grant_subject_resource",
                columnList = "owner_type,owner_id,security_realm,subject_type,subject_id,resource_id", unique = true),
        @Index(name = "idx_nx_permission_grant_subject",
                columnList = "owner_type,owner_id,subject_type,subject_id"),
        @Index(name = "idx_nx_permission_grant_resource",
                columnList = "owner_type,owner_id,resource_id"),
        @Index(name = "idx_nx_permission_grant_realm", columnList = "security_realm")
})
@TableName(PermissionGrantEntity.TABLE_NAME)
public class PermissionGrantEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_permission_grant";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String grantId;

    @Column(length = 32, nullable = false)
    private String subjectType;

    @Column(length = 32, nullable = false)
    private String subjectId;

    @Column(length = 32, nullable = false)
    private String resourceId;

    @Column(columnDefinition = "text")
    private String constraintDefinition;

    @Override
    public String idPrefix() {
        return "pgr";
    }
}
