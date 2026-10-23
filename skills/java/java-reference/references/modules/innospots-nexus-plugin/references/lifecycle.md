# 包 `lifecycle`

## `ManagedPlugin`

**类型：** class（final）

持有一个插件实例、配置供应器、事件总线、资源作用域和已准备 Contribution，执行
`DESCRIBED → STARTING → ACTIVE → STOPPING → STOPPED/FAILED` 生命周期转换。

### 构造方法

#### `ManagedPlugin(DiscoveredPlugin discovered, Supplier<PluginConfig> configSupplier, DefaultPluginEventBus eventBus, List<PluginContributionHandler<?>> contributionHandlers)`

### 方法

- `start()`：解析配置、准备全部 Contribution、调用插件 `initialize/start`、按顺序
  `stage/commit`，最后激活可用性门控并发布 `PluginStartedEvent`。
- `stop()`：先关闭可用性门控，再逆序回滚/关闭 Contribution、调用插件 `stop`、关闭资源作用域。
- `info() → PluginRuntimeInfo`：返回脱敏运行快照。
- `availability() → PluginAvailability`：返回 Contribution 可见性门控。
- `definition() → PluginDefinition`：返回发现时缓存的不可变定义。

启动与停止失败会执行回滚并记录脱敏错误码，不把异常对象写入运行快照。

## `PluginAvailability`

**类型：** class（final）

Contribution 对外可见性的原子门控。

| 方法 | 说明 |
|------|------|
| `isActive() → boolean` | 当前是否允许外部访问 |
| `generation() → long` | 最近成功激活代次 |
| `activate() → long` | 幂等激活并返回代次 |
| `deactivate()` | 幂等失活 |
| `requireActive()` | 未激活时抛 `NexusException`（`CAPABILITY_NOT_FOUND`） |

## `PluginAvailabilityIndex`

**类型：** class（final）

维护 pluginId → `PluginAvailability`，供宿主查询插件资源是否可见。

| 方法 | 说明 |
|------|------|
| `register(String pluginId, PluginAvailability availability)` | 登记；重复登记失败 |
| `unregister(String pluginId)` | 移除 |
| `isVisible(String pluginId) → boolean` | 门控已激活时返回 `true` |

## `PluginRuntimeInfo`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 插件标识 |
| `name` | `String` | 默认语言显示名称 |
| `version` | `String` | 插件版本 |
| `implementationClass` | `String` | `Plugin` 实现类名 |
| `state` | `PluginState` | 生命周期状态 |
| `phase` | `String` | 当前/最近失败阶段 |
| `tags` | `Tags` | 插件标签 |
| `discoveredAt` | `Instant` | 发现时间 |
| `startedAt` | `Instant` | 最近启动时间 |
| `lastError` | `String` | 最近脱敏错误码 |

另提供 `pluginId()` 与 `displayName()` 别名。

## `PluginState`

**类型：** enum

对外暴露的插件粗粒度生命周期状态。当前 `DefaultPluginManager` 不实现依赖等待调度；
正常启动路径从 `DESCRIBED` 进入。

| 常量 | 说明 |
|------|------|
| `DISCOVERED` | 已在 Catalog 中发现，尚未完成定义编译或预检 |
| `DESCRIBED` | 定义已编译并通过预检，尚未进入启动队列 |
| `WAITING` | 等待依赖满足或宿主允许启动 |
| `STARTING` | 正在执行启动事务 |
| `ACTIVE` | 启动成功且对外可用 |
| `STOPPING` | 正在执行停止事务 |
| `STOPPED` | 已停止且资源作用域已释放 |
| `FAILED` | 启动或停止失败；是否重试由上层策略决定 |
