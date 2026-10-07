# 包 `lifecycle`

## ManagedPlugin

**类型：** class

持有一个插件实例、Provider、上下文、资源以及原子生命周期转换。 生命周期转换串行执行，但未知插件代码不会在本对象的生命周期锁内执行。

### 方法

#### `start() → void`
- **说明：** 为一个已发现插件创建生命周期管理包装器。
- **参数：**
  - `discovered` — 已发现插件
  - `config` — 每次启动使用的配置快照
  - `registry` — Capability 注册表
  - `eventBus` — 插件事件总线
  - `configSupplier` — 插件共享配置解析器
  - `providerConfigSupplier` — Provider 私有配置解析器
  - `contributionHandlers` — 按类型索引的 Contribution Handler 列表

#### `stop() → void`
- **说明：** 原子撤出 Capability，再销毁 Provider、插件状态和资源。 / public void stop()

#### `waiting(Map<CapabilityKey, DependencyResolution> dependencies) → void`
- **说明：** 将插件标记为等待所需 Capability。
- **参数：**
  - `dependencies` — 当前声明快照的依赖诊断

#### `dependencies(Map<CapabilityKey, DependencyResolution> dependencies) → void`
- **说明：** 更新依赖诊断，但不改变生命周期状态。
- **参数：**
  - `dependencies` — 当前声明快照的依赖诊断

#### `info() → PluginRuntimeInfo`
- **说明：** 返回不含运行时对象和配置值的不可变运行快照。
- **返回：** 当前插件运行诊断

#### `availability() → PluginAvailability`
- **说明：** 返回 Capability 与 Contribution 共用的可用性门控。
- **返回：** 插件可用性门控

#### `definition() → PluginDefinition`
- **说明：** 返回不可变的插件定义快照。
- **返回：** 发现时缓存的插件定义


## PluginAvailability

**类型：** class

Capability 与 Contribution 共用的原子可用性门控。

### 方法

#### `isActive() → boolean`
- **说明：** 返回当前是否允许外部访问插件资源。
- **返回：** 门控已激活时 true

#### `generation() → long`
- **说明：** 返回当前资源代次。
- **返回：** 最近一次成功激活后的代次；未激活时为 0

#### `activate() → synchronized long`
- **说明：** 将本次已提交资源切换为可见。 重复激活保持幂等，不递增代次。
- **返回：** 激活后的资源代次

#### `deactivate() → synchronized void`
- **说明：** 关闭门控并使旧代次失效。 重复关闭保持幂等，不递减代次。 / public synchronized void deactivate()

#### `requireActive() → void`
- **说明：** 校验当前门控仍然有效。 / public void requireActive()


## PluginAvailabilityIndex

**类型：** class

将插件标识映射到运行时可用性门控，供 Capability 查询过滤未激活 Provider。

### 方法

#### `register(String pluginId, PluginAvailability availability) → void`
- **说明：** 登记一个插件的可用性门控。
- **参数：**
  - `pluginId` — 稳定的插件标识
  - `availability` — 插件运行时门控

#### `unregister(String pluginId) → void`
- **说明：** 移除一个插件的可用性门控。
- **参数：**
  - `pluginId` — 稳定的插件标识

#### `isVisible(String pluginId) → boolean`
- **说明：** 判断指定插件的 Capability 是否对外可见。
- **参数：**
  - `pluginId` — 稳定的插件标识
- **返回：** 门控已激活时 true；未登记或门控未激活时 false


## PluginRuntimeInfo

**类型：** record

不可变且已脱敏的运行快照，不保留运行时对象或配置值。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | — |
| `name` | `String` | — |
| `version` | `String` | — |
| `implementationClass` | `String` | — |
| `state` | `PluginState` | — |
| `phase` | `String` | — |
| `tags` | `Tags` | — |
| `providedCapabilities` | `List<CapabilityKey>` | — |
| `requirements` | `List<CapabilityRequirement>` | — |
| `dependencies` | `Map<CapabilityKey, DependencyResolution>` | — |
| `discoveredAt` | `Instant` | — |
| `startedAt` | `Instant` | — |
| `lastError` | `String` | — |

### 方法

#### `pluginId() → String`
- **说明：** 返回语义更明确的插件稳定身份别名。 public String pluginId()

#### `displayName() → String`
- **说明：** 返回插件展示名称别名。 public String displayName()


## PluginState

**类型：** enum

对外暴露的插件粗粒度生命周期状态。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `DISCOVERED` | — |
| `DESCRIBED` | — |
| `WAITING` | — |
| `STARTING` | — |
| `ACTIVE` | — |
| `STOPPING` | — |
| `STOPPED` | — |
| `FAILED` | — |
