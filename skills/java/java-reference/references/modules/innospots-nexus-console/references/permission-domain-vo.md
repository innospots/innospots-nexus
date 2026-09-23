# 包 `permission.domain.vo`

## PermissionResourceSyncVo

**类型：** record

显式同步扩展和 UiSpec 权限目录后的处理结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `createdResources` | `int` | 新创建的资源数量。 |
| `updatedResources` | `int` | 元数据发生变化并被更新的资源数量。 |
| `disabledResources` | `int` | 当前来源中已不存在、被标记为禁用的资源数量。 |

## PermissionResourceVo

**类型：** record

面向管理端和前端的权限资源目录视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `String` | 资源记录主键。 |
| `ownerPluginId` | `String` | 来源插件稳定身份。 |
| `moduleKey` | `String` | 所属模块 key。 |
| `resourceType` | `CatalogResourceType` | 资源类型。 |
| `resourceKey` | `String` | 稳定资源 key。 |
| `parentResourceId` | `String` | 资源父节点主键。 |
| `pageKey` | `String` | 资源所属或引用的页面 key。 |
| `datasourceKey` | `String` | datasource 在页面内的 key。 |
| `routePath` | `String` | 页面路由。 |
| `requestMethod` | `String` | datasource 的 HTTP 方法。 |
| `requestUrl` | `String` | datasource 的 HTTP 路径模板。 |
| `displayName` | `String` | 目录展示名称。 |
| `sortOrder` | `Integer` | 同级排序值。 |
| `status` | `String` | 资源状态。 |

### 方法

#### `from(ConsoleCatalogResourceEntity entity) → PermissionResourceVo`

- **说明：** 从持久化目录记录创建接口视图。
- **参数：**
  - `entity` — 持久化资源记录
- **返回：** 资源目录视图
