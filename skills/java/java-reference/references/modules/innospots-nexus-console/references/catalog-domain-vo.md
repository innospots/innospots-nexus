# 包 `catalog.domain.vo`

## `CatalogModuleGrantVo`

**类型：** record

MODULE 节点上的可授权面。

| 名称 | 类型 | 说明 |
|------|------|------|
| `viewItems` | `List<CatalogViewItemVo>` | 页面访问项 |
| `operationGroups` | `List<CatalogPermissionGroupVo>` | 操作项分组 |

## `CatalogNodeVo`

**类型：** record

权限设置页的 MODULE / MENU 递归节点。

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `String` | 资源主键 |
| `resourceKey` | `String` | 稳定资源键 |
| `resourceType` | `CatalogResourceType` | `MODULE` 或 `MENU` |
| `ownerPluginId` | `String` | 来源插件 |
| `domainKey` | `String` | 领域键 |
| `moduleKey` | `String` | MODULE 键 |
| `displayName` | `I18nObject` | 本地化名称 |
| `description` | `I18nObject` | 本地化描述 |
| `orderIndex` | `Integer` | 同级排序 |
| `entryPageKey` | `String` | 叶子 MODULE 或 MENU 的稳定入口键 |
| `icon` | `String` | 入口图标 |
| `children` | `List<CatalogNodeVo>` | 子节点 |
| `grants` | `CatalogModuleGrantVo` | MODULE 授权面；MENU 为 `null` |

## `CatalogPermissionGroupVo`

**类型：** record

操作侧权限分组。

| 名称 | 类型 | 说明 |
|------|------|------|
| `group` | `String` | 分组键 |
| `displayName` | `I18nObject` | 本地化分组名 |
| `orderIndex` | `Integer` | 排序 |
| `items` | `List<CatalogViewItemVo>` | 组内操作项 |

## `CatalogViewItemVo`

**类型：** record

可勾选的页面访问或操作授权项。

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceKey` | `String` | 持久化授权资源键 |
| `grantName` | `String` | 对外授权名 |
| `displayName` | `I18nObject` | 本地化名称 |
| `description` | `I18nObject` | 本地化描述 |
| `orderIndex` | `Integer` | 排序 |
