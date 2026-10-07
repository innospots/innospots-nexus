# 包 `contribution`

## PluginContribution

**类型：** interface

插件向宿主提交的一类不可变静态扩展声明。


## PluginContributionContext

**类型：** record

Contribution Handler 使用的只读插件上下文。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `owner` | `ProviderRef` | 声明 Contribution 的插件身份 |
| `config` | `PluginConfig` | 插件共享只读配置 |
| `availability` | `PluginAvailability` | 插件与 Contribution 共用的可用性门控 |


## PluginContributionDecoder

**类型：** interface

将已完成结构校验的通用 YAML 字段解码为具体 Contribution。


## PluginContributionDecoderRegistry

**类型：** class

宿主显式注册的通用 Contribution Decoder 表。

### 方法

#### `builder() → Builder`
- **说明：** 创建空注册表构建器。
- **返回：** 新的可变构建器

#### `find(PluginContributionType<?> type) → Optional<PluginContributionDecoder<?>>`
- **说明：** 按类型查询 Decoder。
- **参数：**
  - `type` — Contribution 类型标识
- **返回：** 已注册 Decoder；未注册时返回空 Optional

#### `snapshot() → Map<PluginContributionType<?>, PluginContributionDecoder<?>>`
- **说明：** 返回不可变 Decoder 快照。
- **返回：** 类型到 Decoder 的不可变映射

#### `build() → PluginContributionDecoderRegistry`
- **说明：** 构建不可变注册表。
- **返回：** 冻结后的注册表快照


## PluginContributionEntry

**类型：** record

将 Contribution 与声明它的插件绑定，供 Handler 做全局校验。


## PluginContributionHandler

**类型：** interface

负责一种 Contribution 全局校验和事务准备的宿主处理器。


## PluginContributionSnapshotter

**类型：** interface

将 Contribution 转为不含类、Handler 或 Secret 的安全快照。


## PluginContributionSnapshotterRegistry

**类型：** class

宿主显式注册的安全 Contribution 快照器表。

### 方法

#### `builder() → Builder`
- **说明：** 创建注册表构建器。
- **返回：** 新的可变构建器

#### `find(PluginContributionType<?> type) → Optional<PluginContributionSnapshotter<?>>`
- **说明：** 查找类型对应的快照器。
- **参数：**
  - `type` — Contribution 类型标识
- **返回：** 已注册快照器；未注册时返回空 Optional

#### `snapshot() → Map<PluginContributionType<?>, PluginContributionSnapshotter<?>>`
- **说明：** 返回不可变快照器快照。
- **返回：** 类型到快照器的不可变映射

#### `build() → PluginContributionSnapshotterRegistry`
- **说明：** 构建不可变注册表。
- **返回：** 冻结后的注册表快照


## PluginContributionType

**类型：** record

Contribution 的稳定类型标识，例如 console@1。

### 方法

#### `toString() → String`
- **说明：** / public PluginContributionType { if (name == null || !NAME_PATTERN.matcher(name).matches() || majorVersion < 1) { throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID, "invalid plugin contribution type: " + name + "@" + majorVersion); } } /** 返回人类可读的 type@major 标识。 public String toString()


## PreparedPluginContribution

**类型：** interface

一个已准备但尚未对外可见的 Contribution 事务句柄。
