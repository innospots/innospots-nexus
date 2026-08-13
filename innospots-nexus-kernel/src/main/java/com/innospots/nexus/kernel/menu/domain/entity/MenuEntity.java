package com.innospots.nexus.kernel.menu.domain.entity;

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
 * Project-scoped persistence entity for management-console menu nodes.
 */
@Getter
@Setter
@Entity
@Table(name = MenuEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_menu_project_key", columnList = "project_id,menu_key", unique = true),
        @Index(name = "idx_nx_menu_project_parent_order", columnList = "project_id,parent_id,sort_order"),
        @Index(name = "idx_nx_menu_project_status_visible", columnList = "project_id,status,visible")
})
@TableName(MenuEntity.TABLE_NAME)
public class MenuEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_menu";

    /**
     * Menu node identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String menuId;

    @Override
    public String idPrefix() {
        return "mnu";
    }

    /**
     * Optional parent menu identifier.
     */
    @Column(length = 32)
    private String parentId;

    /**
     * Stable project-unique menu key.
     */
    @Column(length = 64, nullable = false)
    private String menuKey;

    /**
     * Display name.
     */
    @Column(length = 128, nullable = false)
    private String menuName;

    /**
     * Menu node type.
     */
    @Column(length = 32, nullable = false)
    private String menuType;

    /**
     * Frontend route path.
     */
    @Column(length = 256)
    private String routePath;

    /**
     * Frontend component key.
     */
    @Column(length = 128)
    private String componentKey;

    /**
     * Redirect target route path.
     */
    @Column(length = 256)
    private String redirectPath;

    /**
     * External URL for external-link menus.
     */
    @Column(length = 512)
    private String externalUrl;

    /**
     * Menu icon key.
     */
    @Column(length = 128)
    private String icon;

    /**
     * Browser target used when opening the destination.
     */
    @Column(length = 32, nullable = false)
    private String openMode;

    /**
     * Whether the menu is visible.
     */
    @Column(nullable = false)
    private Boolean visible;

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
     * Whether the menu is protected.
     */
    @Column(nullable = false)
    private Boolean builtIn;
}
