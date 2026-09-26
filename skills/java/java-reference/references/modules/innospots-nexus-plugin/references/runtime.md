# 包 `runtime`

## DefaultPluginManager

**类型：** class

轻量级依赖感知插件运行时，使用一把串行生命周期锁，不持有全局单例状态。

### 方法

#### `create(PluginRuntimeConfig config,
            PluginCatalog catalog,
            List<PluginContributionHandler<?>> contributionHandlers) → DefaultPluginManager`
- **说明：** 根据已发现目录创建运行时管理器，并在构造时完成预检与 ManagedPlugin 装配。
- **参数：**
  - `config` — 宿主运行时配置
  - `catalog` — 已完成全局校验的发现目录
  - `contributionHandlers` — 按类型注册的 Contribution Handler 列表
- **返回：** 独立的插件管理器实例

#### `start() → void`
- **说明：** 以依赖感知的多轮方式发现并启动符合条件的插件。 / public void start()

#### `start(String pluginId) → void`
- **说明：** 解析当前依赖诊断后启动指定插件。
- **参数：**
  - `pluginId` — 稳定的插件标识

#### `stop(String pluginId) → void`
- **说明：** 检查不会移除活动依赖的最后一个 Provider 后停止指定活动插件。
- **参数：**
  - `pluginId` — 稳定的插件标识

#### `plugins() → List<PluginRuntimeInfo>`
- **说明：** 返回当前全部插件运行快照，并按标识排序。
- **返回：** 不可变运行快照列表

#### `plugin(String pluginId) → Optional<PluginRuntimeInfo>`
- **说明：** 查找一个当前插件运行快照。
- **参数：**
  - `pluginId` — 稳定的插件标识
- **返回：** 匹配的快照；未发现时返回空 Optional

#### `capabilities() → CapabilityManager`
- **说明：** 返回此管理器的活动 Capability 查询边界。
- **返回：** 活动 Capability 管理器

#### `close() → void`
- **说明：** 按启动顺序逆序停止活动插件并释放运行时引用。 / public void close()


## PluginManager

**类型：** interface

宿主侧插件运行时入口，负责依赖感知生命周期、诊断和 Capability 查询。


## PluginRuntimeConfig

**类型：** record

一个独立插件管理器实例使用的不可变宿主配置。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `requiredPluginIds` | `Set<String>` | 宿主启动必须激活的插件 |
| `disabledPluginIds` | `Set<String>` | 排除在自动启动之外的插件 |
| `hostConfig` | `Map<String, String>` | 扁平化宿主静态配置 |
| `configSources` | `List<ConfigSource>` | 宿主动态配置来源；在每次插件启动解析时调用 |
| `runtimeVariables` | `Map<String, String>` | 优先级最高的运行时覆盖值 |
| `defaultRoutes` | `Map<CapabilityKey, Tags>` | 按 Capability 标识配置的默认标签 |
| `pluginClassLoader` | `ClassLoader` | ServiceLoader 使用的类加载器；为 null 时使用 Core 类加载器 |

### 方法

#### `resolvedClassLoader(ClassLoader fallback) → ClassLoader`
- **说明：** 使用空动态配置来源的兼容构造形式。 / public PluginRuntimeConfig( Set requiredPluginIds, Set disabledPluginIds, Map hostConfig, Map runtimeVariables, Map defaultRoutes, ClassLoader pluginClassLoader ) { this(requiredPluginIds, disabledPluginIds, hostConfig, List.of(), runtimeVariables, defaultRoutes, pluginClassLoader); } /** / public PluginRuntimeConfig { requiredPluginIds = immutablePluginIds(requiredPluginIds, "required"); disabledPluginIds = immutablePluginIds(disabledPluginIds, "disabled"); hostConfig = immutableStringMap(hostConfig, "host configuration"); configSources = immutableConfigSources(configSources); runtimeVariables = immutableStringMap(runtimeVariables, "runtime variables"); defaultRoutes = immutableRoutes(defaultRoutes); if (!java.util.Collections.disjoint(requiredPluginIds, disabledPluginIds)) { throw NexusException.build( PluginStatusCode.PLUGIN_CONFIG_INVALID, "required and disabled plugin ids must not overlap"); } } /** 返回已配置的类加载器；未配置时返回传入的运行时回退类加载器。
- **参数：**
  - `fallback` — 未配置插件专用类加载器时使用的回退类加载器
- **返回：** 实际使用的插件类加载器
