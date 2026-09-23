package com.innospots.nexus.console.role.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * 控制台角色；通过 {@code ownerType} / {@code ownerId} / {@code securityRealm} 表达归属与可见性。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.domain.identity.RoleSnapshot
 */
@Getter
@Setter
@Entity
@Table(name = RoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_owner_code",
                columnList = "owner_type,owner_id,security_realm,role_code", unique = true),
        @Index(name = "idx_nx_role_owner_status", columnList = "owner_type,owner_id,status"),
        @Index(name = "idx_nx_role_name", columnList = "role_name"),
        @Index(name = "idx_nx_role_realm", columnList = "security_realm")
})
@TableName(RoleEntity.TABLE_NAME)
public class RoleEntity extends BaseEntity {

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

    @Column(length = 32, nullable = false)
    private String ownerType;

    @Column(length = 32)
    private String ownerId;

    @Column(length = 32, nullable = false)
    private String securityRealm;

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
