# 包 `catalog.domain.vo`

## CatalogNodeVo

**类型：** record

权限设置页的插件功能树节点。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `String` | 资源主键 |
| `ownerPluginId` | `String` | 来源插件 |
| `moduleKey` | `String` | 模块 key |
| `resourceType` | `CatalogResourceType` | 资源类型 |
| `resourceKey` | `String` | 稳定资源 key |
| `pageKey` | `String` | 页面标识 |
| `routePath` | `String` | 页面路由 |
| `displayName` | `String` | 展示名称 |
| `sortOrder` | `Integer` | 同级排序 |
| `children` | `List<CatalogNodeVo>` | 子节点 |

### 构造方法

#### `CatalogNodeVo()`
