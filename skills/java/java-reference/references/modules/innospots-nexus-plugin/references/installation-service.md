# 包 `installation.service`

## PluginInstallationManager

**类型：** class

Core 插件安装管理器。

### 方法

#### `reconcile() → void`
- **说明：** 创建使用有效发现报告的安装管理器。
- **参数：**
  - `repository` — 安装事实仓储
  - `runtimeFactory` — 运行时工厂
  - `installationConfig` — 安装行为配置
  - `discoveryReport` — 已完成发现的报告

#### `reconcile(PluginDiscoveryReport report) → void`
- **说明：** 替换发现报告并执行一次全量对账。
- **参数：**
  - `report` — 新的发现报告

#### `start() → void`
- **说明：** 对账后创建唯一运行时并启动已安装且期望启用的插件。 重复调用在运行时已存在时保持幂等。 / public void start()

#### `capabilities() → CapabilityManager`
- **说明：** 返回已启动运行时的 Capability 查询边界。
- **返回：** 活动 Capability 管理器

#### `plugins() → List<PluginManagementView>`
- **说明：** 查询全部插件的持久化事实和当前运行快照。
- **返回：** 聚合管理视图列表

#### `plugin(String pluginId) → Optional<PluginManagementView>`
- **说明：** 查询一个插件的聚合管理视图。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 管理视图；安装事实不存在时为空

#### `installAndStart(String pluginId) → PluginManagementView`
- **说明：** 安装并启动一个当前存在于有效 Catalog 的插件。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 更新后的管理视图

#### `enable(String pluginId) → PluginManagementView`
- **说明：** 启用一个已安装且当前存在的插件。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 更新后的管理视图

#### `disable(String pluginId) → PluginManagementView`
- **说明：** 先提交停用意图，再停止当前运行实例。 停止失败不会恢复启用意图。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 更新后的管理视图

#### `retryStart(String pluginId) → PluginManagementView`
- **说明：** 重试已安装、期望启用且当前失败的插件。 不修改安装意图。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 更新后的管理视图

#### `close() → void`
- **说明：** 关闭此管理器拥有的唯一 PluginManager。 重复关闭保持幂等。 / public void close()


## PluginRuntimeFactory

**类型：** class

Core 运行时工厂，集中组装唯一的 PluginManager，不访问数据库或 Console。

### 方法

#### `create(PluginCatalog catalog, Set<String> eligiblePluginIds) → PluginManager`
- **说明：** 创建带宿主运行参数和 Contribution Handler 的运行时工厂。
- **参数：**
  - `baseConfig` — 宿主运行时基础配置
  - `handlers` — 按类型注册的 Contribution Handler 列表
  - `snapshotters` — 宿主注册的 Contribution 快照器表
  - `catalog` — 已通过全局校验的插件目录
  - `eligiblePluginIds` — 已安装且期望启用的插件身份
- **返回：** 独立 PluginManager

#### `snapshotters() → PluginContributionSnapshotterRegistry`
- **说明：** 返回工厂使用的安全 Contribution 快照器。
- **返回：** 不可变快照器注册表

#### `handlers() → List<PluginContributionHandler<?>>`
- **说明：** 返回当前工厂配置的 Contribution Handler 快照。
- **返回：** 不可变 Handler 列表
