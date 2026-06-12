package com.innospots.nexus.kernel.role.domain.entity;

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
 * Junction record linking a user to one or more roles.
 *
 * @see RoleEntity
 * @see com.innospots.nexus.kernel.user.domain.entity.UserEntity
 */
@Getter
@Setter
@Entity
@Table(name = UserRoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_user_role_project_user_role",
                columnList = "project_id,user_id,role_id", unique = true),
        @Index(name = "idx_nx_user_role_user", columnList = "user_id"),
        @Index(name = "idx_nx_user_role_role", columnList = "role_id")
})
@TableName(UserRoleEntity.TABLE_NAME)
public class UserRoleEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_user_role";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String userRoleId;

    @Override
    public String idPrefix() {
        return "uro";
    }

    @Column(length = 32, nullable = false)
    private String userId;

    @Column(length = 32, nullable = false)
    private String roleId;

    public UserRoleEntity(String userId, String roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRoleEntity() {
    }
}
