# 包 `catalog.service`

## ConsoleCatalogService

**类型：** class

从宿主级目录索引组装权限设置树。

### 方法

#### `tree() → List<CatalogNodeVo>`
- **说明：** 创建目录树查询服务。 / public ConsoleCatalogService(ConsoleCatalogResourceDao resourceDao) { if (resourceDao == null) { throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "resourceDao is required"); } this.resourceDao = resourceDao; } /** 返回已启用的插件目录资源树。
- **返回：** 根节点列表


## ConsoleCatalogSyncService

**类型：** class

将已激活 Console Contribution 和 PageDsl 同步为宿主级目录索引。

### 方法

#### `sync() → CatalogSyncResult`
- **说明：** 创建 Console 目录同步服务。 / public ConsoleCatalogSyncService( ConsoleCatalogResourceDao resourceDao, ConsoleContributionCatalog contributionCatalog, PageDslLoader pageDslLoader ) { this.resourceDao = require(resourceDao, "resourceDao"); this.contributionCatalog = require(contributionCatalog, "contributionCatalog"); this.pageDslLoader = require(pageDslLoader, "pageDslLoader"); } /** 同步当前宿主全部 ACTIVE 插件贡献的模块、菜单、页面、action 和 datasource。
- **返回：** 本次创建、更新和禁用的资源数量
