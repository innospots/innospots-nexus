# 包 `installation.bootstrap`

## PluginHostBootstrap

**类型：** class

应用宿主启用插件子系统的唯一入口。

### 方法

#### `enable(PluginHostBootstrapRequest request) → PluginInstallationManager`
- **说明：** 完成插件子系统启用：发现 → 对账 → 启动 eligible 插件。
- **参数：**
  - `request` — 宿主注入的全部依赖
- **返回：** 已启动的安装管理器；关闭时调用 PluginInstallationManager


## PluginHostBootstrapRequest

**类型：** record

应用宿主启用插件子系统所需的全部依赖。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `installationDao` | `PluginInstallationDao` | 安装表 DAO |
| `runtimeConfig` | `PluginRuntimeConfig` | 运行时配置 |
| `installationConfig` | `PluginInstallationConfig` | 安装策略（含是否自动安装） |
| `contributionDecoders` | `PluginContributionDecoderRegistry` | YAML Contribution 解码器表 |
| `contributionHandlers` | `List<PluginContributionHandler<?>>` | 运行时 Contribution Handler 列表 |
| `contributionSnapshotters` | `PluginContributionSnapshotterRegistry` | 安装快照序列化器表 |
| `pluginClassLoader` | `ClassLoader` | 发现用的类加载器；为 null 时回退到运行时配置或当前线程 CL |
