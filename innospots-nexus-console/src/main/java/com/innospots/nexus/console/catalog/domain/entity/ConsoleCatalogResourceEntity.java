package com.innospots.nexus.console.catalog.domain.entity;

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
 * 宿主级 Console 插件贡献目录索引（console@2）。
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

    @Column(name = "domain_key", length = 128, nullable = false)
    private String domainKey;

    @Column(length = 128, nullable = false)
    private String moduleKey;

    @Column(length = 32, nullable = false)
    private String resourceType;

    @Column(length = 256, nullable = false)
    private String resourceKey;

    @Column(length = 32)
    private String parentResourceId;

    @Column(name = "entry_page_key", length = 256)
    private String entryPageKey;

    @Column(name = "display_name", columnDefinition = "TEXT")
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(length = 128)
    private String icon;

    @Column(name = "view_groups", columnDefinition = "TEXT")
    private String viewGroups;

    @Column(name = "operation_groups", columnDefinition = "TEXT")
    private String operationGroups;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(length = 32, nullable = false)
    private String securityRealm;

    @Override
    public String idPrefix() {
        return "ccr";
    }
}
