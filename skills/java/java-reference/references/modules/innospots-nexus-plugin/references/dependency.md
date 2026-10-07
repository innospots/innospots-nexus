# 包 `dependency`

## DependencyResolution

**类型：** record

一条 Capability 依赖声明的不可变诊断快照。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `key` | `CapabilityKey` | Capability 依赖身份 |
| `requiredTags` | `Tags` | 依赖要求的 Provider 标签 |
| `required` | `boolean` | 是否阻断插件启动 |
| `declared` | `boolean` | 已发现插件中是否有提供者 |
| `available` | `boolean` | 当前是否存在活动 Provider |
| `providerPluginIds` | `List<String>` | 已发现的 Provider 所属插件标识 |


## DependencyResolver

**类型：** class

解析已声明且当前可用的 Capability 依赖，不绑定到具体 Provider 实例。

### 方法

#### `resolve(PluginDefinition definition) → Map<CapabilityKey, DependencyResolution>`
- **说明：** 为已发现插件集合构建稳定的依赖声明索引。
- **参数：**
  - `definitions` — 已发现插件的不可变定义列表
  - `registry` — 当前活动 Capability 注册表
  - `definition` — 待解析的插件声明
- **返回：** 以 Capability 为键的依赖诊断

#### `canStart(Map<CapabilityKey, DependencyResolution> resolutions) → boolean`
- **说明：** 返回所有必需依赖当前是否都有活动 Provider。
- **参数：**
  - `resolutions` — 一个插件的依赖诊断
- **返回：** 每项必需依赖是否都可用
