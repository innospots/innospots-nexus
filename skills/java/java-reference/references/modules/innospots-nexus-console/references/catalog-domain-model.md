# 包 `catalog.domain.model`

## CatalogGrantKeys

**类型：** class（final，静态工具）

生成与解析 Console catalog 授权项键，格式为 `grantName:domainKey.moduleKey`。

### 方法

#### `of(String grantName, String domainKey, String moduleKey) → String`（static）
- **说明：** 组装授权键；任一段为空时返回 null

#### `viewKey(ConsoleCatalogResourceEntity resource) → String`（static）
- **说明：** 使用资源的 `entryPageKey`、`domainKey`、`moduleKey` 生成页面访问键

## CatalogSyncResult

**类型：** record

Console 目录显式同步结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `createdResources` | `int` | — |
| `updatedResources` | `int` | — |
| `disabledResources` | `int` | — |
