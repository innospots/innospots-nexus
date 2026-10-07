# 包 `catalog.bootstrap`

## ConsoleCatalogSyncStartupTask

**类型：** class

启动后将 ACTIVE 插件贡献同步到宿主级目录索引。

### 方法

#### `name() → String`
- **说明：** 创建目录同步启动任务。 / public ConsoleCatalogSyncStartupTask(ConsoleCatalogSyncService syncService) { if (syncService == null) { throw new IllegalArgumentException("syncService is required"); } this.syncService = syncService; } /** 返回名称。
- **返回：** 操作结果

#### `order() → int`
- **说明：** 执行order。
- **返回：** 操作结果

#### `run(NexusStartupContext context) → void`
- **说明：** 执行run。
- **参数：**
  - `context` — 调用上下文
