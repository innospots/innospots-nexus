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
 * Project-scoped role persistence entity for management authorization.
 *
 * @see com.innospots.nexus.base.domain.identity.RoleInfo
 */
@Getter
@Setter
@Entity
@Table(name = RoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_project_code", columnList = "project_id,role_code", unique = true),
        @Index(name = "idx_nx_role_project_status", columnList = "project_id,status"),
        @Index(name = "idx_nx_role_name", columnList = "role_name")
})
@TableName(RoleEntity.TABLE_NAME)
public class RoleEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_role";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String roleId;

    @Override
    public String idPrefix() {
        return "rol";
    }

    @Column(length = 64, nullable = false)
    private String roleName;

    @Column(length = 64, nullable = false)
    private String roleCode;

    @Column(length = 256)
    private String description;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean builtIn;

    @Column(nullable = false)
    private Boolean administrator;
}
