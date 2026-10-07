# 包 `discovery`

## ClasspathPluginDiscovery

**类型：** class

通过 Java SPI 和全部 plugin.yaml 资源发现插件，并统一编译为 Plugin。


## DiscoveredPlugin

**类型：** record

一个已发现的插件实例及其缓存的不可变定义。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `plugin` | `Plugin` | 由 ServiceLoader 创建的插件实例 |
| `definition` | `PluginDefinition` | 只读取一次的插件定义快照 |
| `discoveredAt` | `Instant` | 发现时间 |
| `source` | `PluginSource` | 声明来源元数据 |


## ManifestPlugin

**类型：** class

将 YAML 编译定义适配为唯一 Plugin SPI，避免创建第二套运行时。

### 方法

#### `definition() → PluginDefinition`
- **说明：** 创建 YAML 插件适配器。
- **参数：**
  - `definition` — 已编译的插件定义
- **返回：** 插件定义快照


## PluginCatalog

**类型：** class

一个插件运行时使用的不可变发现快照。

### 方法

#### `of(List<DiscoveredPlugin> plugins) → PluginCatalog`
- **说明：** 根据发现列表创建目录，并执行全局 pluginId 与 Capability API 冲突校验。
- **参数：**
  - `plugins` — 已发现的插件实例和定义
- **返回：** 按插件标识排序的不可变目录

#### `plugins() → List<DiscoveredPlugin>`
- **说明：** 返回按插件标识确定性排序的不可变发现列表。 public List plugins()

#### `plugin(String pluginId) → Optional<DiscoveredPlugin>`
- **说明：** 按稳定标识查找一个已发现插件。
- **参数：**
  - `pluginId` — 稳定的插件标识
- **返回：** 匹配的已发现插件；未找到时返回空 Optional

#### `definitions() → List<PluginDefinition>`
- **说明：** 返回用于诊断和预检校验的不可变插件定义快照。
- **返回：** 按发现顺序排列的插件定义列表


## PluginDefinitionCompiler

**类型：** class

将 YAML 纯数据模型编译为无副作用的运行时 PluginDefinition。

### 方法

#### `registerDeclaredTypes(PluginManifest manifest) → void`
- **说明：** 创建只支持 Capability Java binding 的编译器。
- **参数：**
  - `capabilityTypes` — 发现阶段累积的 Capability 类型表
  - `classLoader` — 加载实现类的类加载器
  - `contributionDecoders` — 宿主注册的 Contribution Decoder 表
  - `manifest` — 已解析的插件清单

#### `compile(PluginManifest manifest, PluginSource source) → PluginDefinition`
- **说明：** 编译一个 YAML 插件定义。
- **参数：**
  - `manifest` — 已解析的插件清单
  - `source` — 声明来源元数据
- **返回：** 无副作用的运行时插件定义


## PluginDiscoveryReport

**类型：** record

表示发现阶段成功目录与被拒绝定义的分离结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `validCatalog` | `PluginCatalog` | 通过全局校验的插件目录 |
| `rejectedDefinitions` | `List<RejectedPluginDefinition>` | 被拒绝定义的不可变诊断列表；null 视为空列表 |


## RejectedPluginDefinition

**类型：** record

单个插件定义被拒绝时保留的来源和安全诊断。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `source` | `PluginSource` | 被拒绝定义的来源元数据 |
| `claimedPluginId` | `String` | 声明的插件标识；未知时可为空 |
| `diagnostics` | `List<String>` | 脱敏后的拒绝原因列表；null 视为空列表 |
