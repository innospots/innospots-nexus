# 包 `runtime`

## `DefaultPluginManager`

**类型：** class（final，implements `PluginManager`）

轻量插件运行时。每个实例独立持有已发现目录、Contribution Handler、事件总线与运行状态；
管理操作短暂串行化，插件生命周期代码不在该管理锁内执行。

### 静态工厂

#### `create(PluginRuntimeConfig config, PluginCatalog catalog, List<PluginContributionHandler<?>> contributionHandlers) → DefaultPluginManager`

构造时完成必选插件、Contribution Handler、配置 schema 与 `ManagedPlugin` 预检装配。

### 方法

- `start()`：按目录顺序启动未禁用插件；任一必选插件未激活时回滚本次已启动插件并失败。
- `start(String pluginId)`：启动指定插件。
- `stop(String pluginId)`：停止活动的指定插件；非活动状态直接返回。
- `plugins() → List<PluginRuntimeInfo>`：按 pluginId 排序返回不可变运行快照。
- `plugin(String pluginId) → Optional<PluginRuntimeInfo>`：查询单个运行快照。
- `close()`：按实际启动顺序逆序停止插件并释放运行时引用；关闭后不可复用。

## `PluginManager`

**类型：** interface（extends `AutoCloseable`）

宿主侧实例级插件运行时入口，只提供批量/单项启停、快照查询与关闭。

## `PluginRuntimeConfig`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `requiredPluginIds` | `Set<String>` | 启动时必须激活的插件 |
| `disabledPluginIds` | `Set<String>` | 自动启动时排除的插件 |
| `hostConfig` | `Map<String, String>` | 扁平化宿主静态配置 |
| `configSources` | `List<ConfigSource>` | 每次插件启动解析时调用的动态来源 |
| `runtimeVariables` | `Map<String, String>` | 最高优先级运行时覆盖 |
| `pluginClassLoader` | `ClassLoader` | ServiceLoader 类加载器；`null` 时使用回退值 |

必选与禁用集合不得重叠。另提供省略 `configSources` 的五参数兼容构造器。

### 方法

#### `resolvedClassLoader(ClassLoader fallback) → ClassLoader`

未配置插件类加载器时返回 `fallback`。
