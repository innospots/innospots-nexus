package com.innospots.nexus.console.permission.domain.entity;

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
 * 由扩展和 UiSpec 生成的规范化权限目录记录。
 *
 * <p>扩展声明和 UiSpec 才是资源事实源，本实体只是项目内用于检索、展示、授权和请求匹配的索引。
 * 它不保存完整页面定义，也不替代 UiSpec。</p>
 */
@Getter
@Setter
@Entity
@Table(name = PermissionResourceEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_permission_resource_project_key",
                columnList = "workspace_id,resource_key", unique = true),
        @Index(name = "idx_nx_permission_resource_source",
                columnList = "workspace_id,extension_key,module_key,resource_type,status"),
        @Index(name = "idx_nx_permission_resource_parent",
                columnList = "workspace_id,parent_resource_id,sort_order"),
        @Index(name = "idx_nx_permission_resource_request",
                columnList = "workspace_id,page_key,request_method,request_url"),
        @Index(name = "idx_nx_permission_resource_realm", columnList = "security_realm")
})
@TableName(PermissionResourceEntity.TABLE_NAME)
public class PermissionResourceEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_permission_resource";

    /** 资源记录主键。 */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String resourceId;

    /** 提供该资源的扩展 key。 */
    @Column(length = 128, nullable = false)
    private String extensionKey;

    /** 资源所属模块 key。 */
    @Column(length = 128, nullable = false)
    private String moduleKey;

    /** 持久化的资源类型，对应 PermissionResourceType。 */
    @Column(length = 32, nullable = false)
    private String resourceType;

    /** 项目内稳定且可复用的资源 key。 */
    @Column(length = 256, nullable = false)
    private String resourceKey;

    /** 资源归属树的父资源；菜单到页面的导航关系通过 pageKey 表示。 */
    @Column(length = 32)
    private String parentResourceId;

    /** 完整页面标识，格式为 moduleKey.pageKey。 */
    @Column(length = 256)
    private String pageKey;

    /** DATASOURCE 资源所属页面中的 datasource key。 */
    @Column(length = 128)
    private String datasourceKey;

    /** PAGE 资源对应的页面路由。 */
    @Column(length = 512)
    private String routePath;

    /** DATASOURCE 资源对应的 HTTP 方法。 */
    @Column(length = 16)
    private String requestMethod;

    /** DATASOURCE 资源对应的 HTTP 路径模板。 */
    @Column(length = 512)
    private String requestUrl;

    /** 从扩展或 UiSpec 元数据复制的单值展示名称。 */
    @Column(length = 256)
    private String displayName;

    /** 同级资源排序值。 */
    @Column(nullable = false)
    private Integer sortOrder;

    /** 目录记录生命周期状态。 */
    @Column(length = 32, nullable = false)
    private String status;

    /** 安全域：PLATFORM 或 TENANT。 */
    @Column(length = 32, nullable = false)
    private String securityRealm;

    /** 返回权限资源主键使用的 ID 前缀。 */
    @Override
    public String idPrefix() {
        return "prs";
    }
}
