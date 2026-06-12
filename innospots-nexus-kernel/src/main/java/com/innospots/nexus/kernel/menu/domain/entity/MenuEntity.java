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

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String menuId;

    @Override
    public String idPrefix() {
        return "mnu";
    }

    @Column(length = 32)
    private String parentId;

    @Column(length = 64, nullable = false)
    private String menuKey;

    @Column(length = 128, nullable = false)
    private String menuName;

    @Column(length = 32, nullable = false)
    private String menuType;

    @Column(length = 256)
    private String routePath;

    @Column(length = 128)
    private String componentKey;

    @Column(length = 256)
    private String redirectPath;

    @Column(length = 512)
    private String externalUrl;

    @Column(length = 128)
    private String icon;

    @Column(length = 32, nullable = false)
    private String openMode;

    @Column(nullable = false)
    private Boolean visible;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean builtIn;
}
