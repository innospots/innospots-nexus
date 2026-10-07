package com.innospots.nexus.console.menu.domain.entity;

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
 * 管理控制台菜单节点；默认 WORKSPACE 归属。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@Entity
@Table(name = MenuEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_menu_owner_key",
                columnList = "owner_type,owner_id,security_realm,menu_key", unique = true),
        @Index(name = "idx_nx_menu_owner_parent_order", columnList = "owner_type,owner_id,parent_id,sort_order"),
        @Index(name = "idx_nx_menu_owner_status_visible", columnList = "owner_type,owner_id,status,visible"),
        @Index(name = "idx_nx_menu_realm", columnList = "security_realm")
})
@TableName(MenuEntity.TABLE_NAME)
public class MenuEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_menu";

    /**
     * 菜单节点标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String menuId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "mnu";
    }

    /**
     * 可选父菜单标识符。
     */
    @Column(length = 32)
    private String parentId;

    /**
     * 项目内唯一的稳定菜单键。
     */
    @Column(length = 64, nullable = false)
    private String menuKey;

    /**
     * 显示名称。
     */
    @Column(length = 128, nullable = false)
    private String menuName;

    /**
     * 菜单节点类型。
     */
    @Column(length = 32, nullable = false)
    private String menuType;

    /**
     * 前端路由路径。
     */
    @Column(length = 256)
    private String routePath;

    /**
     * 前端组件键。
     */
    @Column(length = 128)
    private String componentKey;

    /**
     * 重定向目标路由路径。
     */
    @Column(length = 256)
    private String redirectPath;

    /**
     * 外链菜单的外部 URL。
     */
    @Column(length = 512)
    private String externalUrl;

    /**
     * 菜单图标键。
     */
    @Column(length = 128)
    private String icon;

    /**
     * 打开目标时使用的浏览器目标。
     */
    @Column(length = 32, nullable = false)
    private String openMode;

    /**
     * 菜单是否可见。
     */
    @Column(nullable = false)
    private Boolean visible;

    /**
     * 生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * 同级显示顺序。
     */
    @Column(nullable = false)
    private Integer sortOrder;

    /**
     * 菜单是否为受保护菜单。
     */
    @Column(nullable = false)
    private Boolean builtIn;
}
