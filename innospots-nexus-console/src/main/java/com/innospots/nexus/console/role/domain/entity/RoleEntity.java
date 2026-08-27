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

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

/**
 * Console-scoped role persistence entity. Ownership is PLATFORM, TENANT, or WORKSPACE.
 *
 * @see com.innospots.nexus.base.domain.identity.RoleInfo
 */
@Getter
@Setter
@Entity
@Table(name = RoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_owner_code", columnList = "owner_type,owner_id,role_code", unique = true),
        @Index(name = "idx_nx_role_workspace_status", columnList = "workspace_id,status"),
        @Index(name = "idx_nx_role_name", columnList = "role_name"),
        @Index(name = "idx_nx_role_realm", columnList = "security_realm")
})
@TableName(RoleEntity.TABLE_NAME)
public class RoleEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_role";

    /**
     * Role identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String roleId;

    @Override
    public String idPrefix() {
        return "rol";
    }

    /**
     * Display name.
     */
    @Column(length = 64, nullable = false)
    private String roleName;

    /**
     * Stable unique role code within the owner.
     */
    @Column(length = 64, nullable = false)
    private String roleCode;

    /**
     * Ownership layer: PLATFORM, TENANT, or WORKSPACE.
     */
    @Column(length = 32, nullable = false)
    private String ownerType;

    /**
     * Owner identifier matching {@code ownerType}; empty for PLATFORM.
     */
    @Column(length = 32)
    private String ownerId;

    /**
     * Security realm: PLATFORM or TENANT.
     */
    @Column(length = 32, nullable = false)
    private String securityRealm;

    /**
     * Optional description.
     */
    @Column(length = 256)
    private String description;

    /**
     * Lifecycle status.
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * Sibling display order.
     */
    @Column(nullable = false)
    private Integer sortOrder;

    /**
     * Whether the role is protected.
     */
    @Column(nullable = false)
    private Boolean builtIn;

    /**
     * Whether the role grants administrator privileges.
     */
    @Column(nullable = false)
    private Boolean administrator;
}
