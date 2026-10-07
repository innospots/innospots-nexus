# 包 `permission.domain.vo`

## PermissionResourceSyncVo

**类型：** record

显式同步扩展和 UiSpec 权限目录后的处理结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `createdResources` | `/**
         * 新创建的资源数量。
         */
        int` | — |
| `updatedResources` | `/**
         * 元数据发生变化并被更新的资源数量。
         */
        int` | — |
| `disabledResources` | `/**
         * 当前来源中已不存在、被标记为禁用的资源数量。
         */
        int` | — |


## PermissionResourceVo

**类型：** record

面向管理端和前端的权限资源目录视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `/**
         * 资源记录主键。
         */
        String` | — |
| `ownerPluginId` | `/**
         * 来源插件稳定身份。
         */
        String` | — |
| `moduleKey` | `/**
         * 所属模块 key。
         */
        String` | — |
| `resourceType` | `/**
         * 资源类型。
         */
        CatalogResourceType` | — |
| `resourceKey` | `/**
         * 稳定资源 key。
         */
        String` | — |
| `parentResourceId` | `/**
         * 资源父节点主键。
         */
        String` | — |
| `pageKey` | `/**
         * 资源所属或引用的页面 key。
         */
        String` | — |
| `datasourceKey` | `/**
         * datasource 在页面内的 key。
         */
        String` | — |
| `routePath` | `/**
         * 页面路由。
         */
        String` | — |
| `requestMethod` | `/**
         * datasource 的 HTTP 方法。
         */
        String` | — |
| `requestUrl` | `/**
         * datasource 的 HTTP 路径模板。
         */
        String` | — |
| `displayName` | `/**
         * 目录展示名称。
         */
        String` | — |
| `sortOrder` | `/**
         * 同级排序值。
         */
        Integer` | — |
| `status` | `/**
         * 资源状态。
         */
        String` | — |

### 方法

#### `from(ConsoleCatalogResourceEntity entity) → PermissionResourceVo`
- **说明：** 资源记录主键。 / String resourceId, /** 来源插件稳定身份。 / String ownerPluginId, /** 所属模块 key。 / String moduleKey, /** 资源类型。 / CatalogResourceType resourceType, /** 稳定资源 key。 / String resourceKey, /** 资源父节点主键。 / String parentResourceId, /** 资源所属或引用的页面 key。 / String pageKey, /** datasource 在页面内的 key。 / String datasourceKey, /** 页面路由。 / String routePath, /** datasource 的 HTTP 方法。 / String requestMethod, /** datasource 的 HTTP 路径模板。 / String requestUrl, /** 目录展示名称。 / String displayName, /** 同级排序值。 / Integer sortOrder, /** 资源状态。 / String status ) { /** 从持久化目录记录创建接口视图。
- **参数：**
  - `entity` — 持久化资源记录
- **返回：** 资源目录视图
