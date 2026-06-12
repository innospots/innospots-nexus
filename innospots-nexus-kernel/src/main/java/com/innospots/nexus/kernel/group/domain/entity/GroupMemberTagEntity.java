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
 * Tag-name assignment for a user within the user's current group.
 */
@Getter
@Setter
@Entity
@Table(name = GroupMemberTagEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_group_member_tag_assignment",
                columnList = "project_id,group_id,user_id,tag_name", unique = true),
        @Index(name = "idx_nx_group_member_tag_project_user",
                columnList = "project_id,user_id"),
        @Index(name = "idx_nx_group_member_tag_project_name",
                columnList = "project_id,tag_name")
})
@TableName(GroupMemberTagEntity.TABLE_NAME)
public class GroupMemberTagEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_group_member_tag";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String groupMemberTagId;

    @Override
    public String idPrefix() {
        return "gmt";
    }

    @Column(length = 32, nullable = false)
    private String groupId;

    @Column(length = 32, nullable = false)
    private String userId;

    @Column(length = 64, nullable = false)
    private String tagName;
}
