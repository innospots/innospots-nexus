# 包 `catalog.service`

## `ConsoleCatalogService`

**类型：** class（final）

从宿主级目录索引组装 MODULE / MENU 权限目录。

### 构造方法

#### `ConsoleCatalogService(ConsoleCatalogResourceDao resourceDao, EndpointGrantManager endpointGrantManager)`

注入目录 DAO 与 endpoint grant 聚合器；`resourceDao` 为空时抛出配置状态码异常。

### 方法

#### `tree() → List<CatalogNodeVo>`

**已废弃；** 委托 `permissionCatalog()`。

#### `permissionCatalog() → List<CatalogNodeVo>`

返回启用的 MODULE 根节点及其 MENU / 嵌套 MODULE 子树。叶子 MODULE 的 `grants`
合并访问项与 `EndpointGrantManager.moduleCatalog(moduleKey)` 的操作项。

#### `grantResourceKeys() → Set<String>`

递归收集目录中全部可持久化授权资源键。

## `CatalogPermissionCatalogAssembler`

**类型：** class（final）

把 catalog 表中的 MODULE/MENU 与 `EndpointGrant` 操作项组装为 `CatalogModuleGrantVo`。

## `ConsoleCatalogSyncService`

**类型：** class（final）

将已激活 `console@2` 贡献同步为宿主级目录索引；当前只写 MODULE / MENU。

### 构造方法

#### `ConsoleCatalogSyncService(ConsoleCatalogResourceDao resourceDao, ConsoleContributionCatalog contributionCatalog)`

注入目录 DAO 与活动贡献目录；任一为空时抛出异常。

### 方法

#### `sync() → CatalogSyncResult`

创建缺失资源、更新变化资源，并禁用不再存在的旧资源，返回计数快照。
