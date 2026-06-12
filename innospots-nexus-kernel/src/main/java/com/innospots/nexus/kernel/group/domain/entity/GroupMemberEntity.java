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
 * Single group ownership record for a project user.
 * <p>
 * A leader is always a member and is represented by the {@code leader} flag.
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = GroupMemberEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_group_member_project_user",
                columnList = "project_id,user_id", unique = true),
        @Index(name = "idx_nx_group_member_project_group",
                columnList = "project_id,group_id"),
        @Index(name = "idx_nx_group_member_project_group_leader",
                columnList = "project_id,group_id,leader")
})
@TableName(GroupMemberEntity.TABLE_NAME)
public class GroupMemberEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_group_member";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String groupMemberId;

    @Override
    public String idPrefix() {
        return "gmb";
    }

    @Column(length = 32, nullable = false)
    private String groupId;

    @Column(length = 32, nullable = false)
    private String userId;

    @Column(nullable = false)
    private Boolean leader;
}
