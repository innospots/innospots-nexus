# 包 `permission.domain.vo`

## PermissionResourceSyncVo

**类型：** record

显式同步 `console@2` 贡献到权限目录后的计数结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `createdResources` | `int` | 新创建的目录资源数量 |
| `updatedResources` | `int` | 元数据变化并更新的资源数量 |
| `disabledResources` | `int` | 当前贡献中已不存在并被禁用的资源数量 |


## PermissionResourceVo

**类型：** record

当前主体可见的 catalog 权限资源视图。catalog v2 仅产生 MODULE 与 MENU。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `String` | 目录资源主键 |
| `ownerPluginId` | `String` | 来源插件稳定身份 |
| `domainKey` | `String` | 领域键 |
| `moduleKey` | `String` | 所属 MODULE 键 |
| `resourceType` | `CatalogResourceType` | `MODULE` 或 `MENU` |
| `resourceKey` | `String` | 稳定目录资源键 |
| `parentResourceId` | `String` | 父资源主键 |
| `entryPageKey` | `String` | 叶子 MODULE/MENU 的稳定入口键 |
| `displayName` | `String` | 已序列化的目录展示名称 |
| `sortOrder` | `Integer` | 同级排序值 |
| `status` | `String` | 资源状态 |

### 方法

#### `from(ConsoleCatalogResourceEntity entity) → PermissionResourceVo`

将 catalog 持久化记录映射为管理端视图。

- **参数：** `entity` — 持久化资源记录
- **返回：** 权限资源视图
