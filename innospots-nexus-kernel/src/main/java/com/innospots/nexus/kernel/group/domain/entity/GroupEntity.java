package com.innospots.nexus.kernel.group.domain.entity;

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

import com.innospots.nexus.core.entity.ProjectBaseEntity;

/**
 * Project-scoped hierarchical user group.
 */
@Getter
@Setter
@Entity
@Table(name = GroupEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_group_project_code", columnList = "project_id,group_code", unique = true),
        @Index(name = "idx_nx_group_project_parent_order", columnList = "project_id,parent_id,sort_order"),
        @Index(name = "idx_nx_group_project_status", columnList = "project_id,status")
})
@TableName(GroupEntity.TABLE_NAME)
public class GroupEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_group";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String groupId;

    @Override
    public String idPrefix() {
        return "grp";
    }

    @Column(length = 32)
    private String parentId;

    @Column(length = 64, nullable = false)
    private String groupCode;

    @Column(length = 128, nullable = false)
    private String groupName;

    @Column(length = 256)
    private String description;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean builtIn;
}
