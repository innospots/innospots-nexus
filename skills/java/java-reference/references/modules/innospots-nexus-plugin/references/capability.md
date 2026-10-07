# 包 `capability`

## CapabilityKey

**类型：** record

Capability API 主版本的稳定逻辑身份。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 小写点分 Capability 名称 |
| `majorVersion` | `int` | 正整数形式的 API 主版本 |


## CapabilityManager

**类型：** interface

用于选择活动 Capability Provider 的类型安全读取边界。


## CapabilityRegistration

**类型：** record

插件启动成功后才发布的不可变 Provider 注册记录。

### 方法

#### `pluginId() → String`
- **说明：** 校验注册身份、路由标签和运行时 Provider 类型。 / public CapabilityRegistration { if (type == null || provider == null || providerRef == null || tags == null) { throw NexusException.build( PluginStatusCode.CAPABILITY_TYPE_MISMATCH, "capability registration fields are required"); } if (!type.api().isInstance(provider)) { throw NexusException.build( PluginStatusCode.CAPABILITY_TYPE_MISMATCH, "provider does not implement capability API: " + type.key()); } } /** 兼容旧的构造顺序，新的调用方应直接传入 ProviderRef。
- **参数：**
  - `type` — Capability 身份和 Java API
  - `provider` — Provider 实例
  - `pluginId` — 所属插件标识
  - `tags` — 合并后的路由标签


## CapabilityRegistry

**类型：** class

通过写时复制向并发读者提供完整不可变快照的 Capability 注册表。

### 方法

#### `registerAll(List<CapabilityRegistration<?>> registrations) → synchronized void`
- **说明：** 使用默认路由创建注册表；不启用可用性门控（单元测试场景）。
- **参数：**
  - `defaultRoutes` — 查询未指定标签时使用的路由标签
  - `availabilityIndex` — 插件可用性索引；为 null 时不做门控（单元测试）
  - `registrations` — 作为一个快照发布的 Provider 注册记录

#### `unregisterPlugin(String pluginId) → synchronized void`
- **说明：** 原子移除一个插件拥有的全部注册记录。
- **参数：**
  - `pluginId` — 稳定的归属插件标识

#### `contains(CapabilityKey key) → boolean`
- **说明：** 返回指定 Capability 是否至少存在一个活动 Provider。
- **参数：**
  - `key` — Capability 逻辑身份
- **返回：** 是否存在至少一个已注册 Provider

#### `contains(CapabilityKey key, Tags requiredTags) → boolean`
- **说明：** 判断当前活动注册中是否存在匹配标签的 Provider。
- **参数：**
  - `key` — Capability 逻辑身份
  - `requiredTags` — 必须全部匹配的路由标签
- **返回：** 是否存在至少一个匹配 Provider

#### `snapshot() → Map<CapabilityKey, List<CapabilityRegistration<?>>>`
- **说明：** 返回用于依赖解析和诊断计算的不可变注册快照。
- **返回：** Capability 到注册记录的不可变快照


## CapabilityRouter

**类型：** class

应用显式标签、配置默认路由和唯一 Provider 回退，不依赖注册顺序选择 Provider。

### 方法

#### `select(CapabilityType<T> type,
            Tags requiredTags,
            List<CapabilityRegistration<T>> registrations) → CapabilityRegistration<T>`
- **说明：** 使用不可变默认路由创建路由器。
- **参数：**
  - `defaultRoutes` — 调用方未提供显式标签时使用的路由标签
  - `type` — 请求的 Capability 类型
  - `requiredTags` — 显式路由标签；null 表示使用配置的默认路由
  - `registrations` — 请求 Capability 的活动注册记录
  - `<T>` — Provider 契约类型
- **返回：** 选中的注册记录；没有匹配 Provider 时返回 null


## CapabilityType

**类型：** record

Capability 逻辑身份与 Java API 之间的类型安全关联。

### 方法

#### `of(String name,
            int majorVersion,
            Class<T> api) → CapabilityType<T>`
- **说明：** 校验 Capability 标识和 API 契约。 public CapabilityType { if (key == null || api == null || !api.isInterface() || !CapabilityProvider.class.isAssignableFrom(api)) { throw NexusException.build( PluginStatusCode.PLUGIN_DEFINITION_INVALID, "capability API must be an interface extending CapabilityProvider"); } } /** 创建一个 Capability 类型。
- **参数：**
  - `name` — Capability 名称
  - `majorVersion` — API 主版本
  - `api` — 共享的 Java API
  - `<T>` — Provider 契约类型
- **返回：** 已校验的 Capability 类型


## CapabilityTypeRegistry

**类型：** class

Capability 逻辑身份到 Java API 的映射表，不注册 Provider 实现类。

### 方法

#### `builder() → Builder`
- **说明：** 创建注册表构建器。
- **返回：** 新的可变构建器

#### `find(CapabilityKey key) → Optional<CapabilityType<?>>`
- **说明：** 按 type@major 查询已注册 API。
- **参数：**
  - `key` — Capability 逻辑身份
- **返回：** 已注册类型；未注册时返回空 Optional

#### `find(String name, int majorVersion) → Optional<CapabilityType<?>>`
- **说明：** 按名称和主版本查询已注册 API。
- **参数：**
  - `name` — 小写点分 Capability 名称
  - `majorVersion` — 正整数形式的 API 主版本
- **返回：** 已注册类型；未注册时返回空 Optional

#### `snapshot() → Map<CapabilityKey, CapabilityType<?>>`
- **说明：** 返回不可变的完整注册快照。
- **返回：** Capability 身份到类型的不可变映射

#### `registerFrom(PluginDefinition definition) → Builder`
- **说明：** 从插件定义登记其声明的全部 Capability 类型。
- **参数：**
  - `definition` — 已校验的插件定义
- **返回：** 当前构建器

#### `register(CapabilityType<T> type) → Builder`
- **说明：** 登记一个 Capability 类型，重复 key 但 API 不同会被拒绝。
- **参数：**
  - `type` — 要登记的 Capability 类型
- **返回：** 当前构建器

#### `build() → CapabilityTypeRegistry`
- **说明：** 构建不可变注册表。
- **返回：** 冻结后的注册表快照


## ProviderRef

**类型：** record

插件内 Provider 的稳定身份，由所属插件标识和插件内唯一编号组成。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 所属插件的反向域名标识 |
| `providerId` | `String` | 插件内全局唯一的 Provider 标识 |


## Tag

**类型：** record

一个不可变的路由标签属性。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 标签名称 |
| `value` | `String` | 标签值 |


## Tags

**类型：** class

不可变且按名称排序的路由标签。

### 方法

#### `empty() → Tags`
- **说明：** 返回用于无约束查询的空标签集合。
- **返回：** 共享的空标签实例

#### `of(String name, String value) → Tags`
- **说明：** 创建包含一个属性的标签集合。
- **参数：**
  - `name` — 标签名
  - `value` — 标签值
- **返回：** 新标签集合

#### `from(Map<String, String> values) → Tags`
- **说明：** 根据属性映射创建标签集合。
- **参数：**
  - `values` — 标签映射；空映射返回 {@link #empty()}
- **返回：** 新标签集合

#### `merge(Tags pluginTags, Tags providerTags) → Tags`
- **说明：** 合并插件级与 Provider 级标签。
- **参数：**
  - `pluginTags` — 插件默认标签
  - `providerTags` — Provider 标签
- **返回：** 合并后的不可变标签集合

#### `and(String name, String value) → Tags`
- **说明：** 返回新增一个无冲突属性后的标签集合。
- **参数：**
  - `name` — 标签名
  - `value` — 标签值
- **返回：** 包含新属性的不可变副本

#### `get(String name) → Optional<String>`
- **说明：** 查找指定标签值。
- **参数：**
  - `name` — 标签名
- **返回：** 标签值；不存在时为空

#### `matches(Tags required) → boolean`
- **说明：** 判断当前 Provider 标签是否包含请求的全部标签。
- **参数：**
  - `required` — 请求侧必需标签
- **返回：** 全部包含时 true；required 为 null 时 false

#### `asMap() → Map<String, String>`
- **说明：** 返回不可变标签映射。
- **返回：** 按稳定顺序排列的标签快照

#### `isEmpty() → boolean`
- **说明：** 返回当前是否没有任何标签。
- **返回：** 无标签时 true
