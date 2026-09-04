package com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity;

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
 * 宿主级 Console 插件贡献目录索引。
 *
 * <p>由 ACTIVE 插件 Contribution 与 UiSpec 同步生成；授权关系在 console 的
 * {@code nx_permission_grant} 中按 workspace 维护。</p>
 */
@Getter
@Setter
@Entity
@Table(name = ConsoleCatalogResourceEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_console_catalog_resource_key",
                columnList = "resource_key", unique = true),
        @Index(name = "idx_nx_console_catalog_resource_source",
                columnList = "owner_plugin_id,module_key,resource_type,status"),
        @Index(name = "idx_nx_console_catalog_resource_parent",
                columnList = "parent_resource_id,sort_order"),
        @Index(name = "idx_nx_console_catalog_resource_request",
                columnList = "page_key,request_method,request_url"),
        @Index(name = "idx_nx_console_catalog_resource_realm", columnList = "security_realm")
})
@TableName(ConsoleCatalogResourceEntity.TABLE_NAME)
public class ConsoleCatalogResourceEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_console_catalog_resource";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String resourceId;

    @Column(name = "owner_plugin_id", length = 256, nullable = false)
    private String ownerPluginId;

    @Column(length = 128, nullable = false)
    private String moduleKey;

    @Column(length = 32, nullable = false)
    private String resourceType;

    @Column(length = 256, nullable = false)
    private String resourceKey;

    @Column(length = 32)
    private String parentResourceId;

    @Column(length = 256)
    private String pageKey;

    @Column(length = 128)
    private String datasourceKey;

    @Column(length = 512)
    private String routePath;

    @Column(length = 16)
    private String requestMethod;

    @Column(length = 512)
    private String requestUrl;

    @Column(length = 256)
    private String displayName;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(length = 32, nullable = false)
    private String securityRealm;

    @Override
    public String idPrefix() {
        return "ccr";
    }
}
