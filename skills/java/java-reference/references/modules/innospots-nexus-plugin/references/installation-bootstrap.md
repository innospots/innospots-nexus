# 包 `installation.bootstrap`

## `PluginHostBootstrap`

**类型：** class（final）

应用宿主启用插件子系统的唯一入口。固定执行 classpath 发现、安装事实对账、
运行时创建与 eligible 插件启动。

### 方法

#### `enable(PluginHostBootstrapRequest request) → PluginInstallationManager`

创建并启动安装管理器；调用方持有返回值，并在宿主关闭时调用
`PluginInstallationManager.close()`。请求为空或任一阶段失败时抛出 `NexusException`。

## `PluginHostBootstrapRequest`

**类型：** record

宿主启用插件子系统所需的全部依赖。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `installationDao` | `PluginInstallationDao` | 安装表 DAO |
| `runtimeConfig` | `PluginRuntimeConfig` | 插件运行时配置 |
| `installationConfig` | `PluginInstallationConfig` | 自动安装与安装策略 |
| `contributionDecoders` | `PluginContributionDecoderRegistry` | YAML Contribution 解码器表 |
| `contributionHandlers` | `List<PluginContributionHandler<?>>` | 运行时 Contribution Handler |
| `contributionSnapshotters` | `PluginContributionSnapshotterRegistry` | 安装快照序列化器表 |
| `pluginClassLoader` | `ClassLoader` | 显式发现类加载器；为空时依次回退到运行时配置、线程上下文与定义类加载器 |

### 构造方法

#### `PluginHostBootstrapRequest(...)`

除 `pluginClassLoader` 外的依赖均必填；Handler 列表不得为 `null` 或包含空元素。
违反约束时抛出 `PluginStatusCode.PLUGIN_CONFIG_INVALID`。
