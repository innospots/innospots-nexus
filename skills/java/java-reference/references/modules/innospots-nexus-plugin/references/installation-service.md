# 包 `installation.service`

## `PluginInstallationManager`

**类型：** class（final，implements `AutoCloseable`）

协调安装事实与当前 JVM 运行时；持有该实例创建的唯一 `PluginManager`。

### 构造方法

#### `PluginInstallationManager(PluginInstallationRepository repository, PluginRuntimeFactory runtimeFactory, PluginInstallationConfig installationConfig, PluginDiscoveryReport discoveryReport)`

### 方法

- `reconcile()` / `reconcile(PluginDiscoveryReport report)`：登记有效插件、更新 MISSING，
  运行时活跃时禁止对账。
- `start()`：对账后创建运行时并启动 installed + desiredEnabled + PRESENT 的插件。
- `plugins() → List<PluginManagementView>`
- `plugin(String pluginId) → Optional<PluginManagementView>`
- `installAndStart(String pluginId) → PluginManagementView`
- `enable(String pluginId) → PluginManagementView`
- `disable(String pluginId) → PluginManagementView`：先写入停用意图，再停止运行时。
- `retryStart(String pluginId) → PluginManagementView`：仅允许 `FAILED` 状态。
- `close()`：关闭运行时，幂等。

## `PluginRuntimeFactory`

**类型：** class（final）

### 构造方法

#### `PluginRuntimeFactory(PluginRuntimeConfig baseConfig, List<PluginContributionHandler<?>> handlers, PluginContributionSnapshotterRegistry snapshotters)`

### 方法

- `create(PluginCatalog catalog, Set<String> eligiblePluginIds) → PluginManager`：
  将未 eligible 的已发现插件加入 disabled，并只保留 eligible 范围内的 required。
- `handlers() → List<PluginContributionHandler<?>>`
- `snapshotters() → PluginContributionSnapshotterRegistry`

创建前校验每个 Contribution 都有 Handler 与 Snapshotter。
