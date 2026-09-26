# 包 `installation.domain.model`

## PluginDefinitionSnapshot

**类型：** record

可持久化的插件静态摘要，不包含 Factory、Class、Handler、配置值或 Secret。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 稳定的插件标识 |
| `version` | `String` | 插件版本 |
| `apiVersion` | `int` | 插件 API 主版本 |
| `sourceType` | `String` | 定义来源类型文本 |
| `sourceLocation` | `String` | 定义来源位置 |
| `capabilities` | `List<CapabilitySnapshot>` | Capability 静态身份摘要列表 |
| `contributions` | `List<Map<String, Object>>` | 通用 Contribution 安全摘要列表 |

### 方法

#### `CapabilitySnapshot(String type,
            int majorVersion,
            String providerId,
            Map<String, String> tags,
            List<ConfigItemSnapshot> config) → record`
- **说明：** Capability 静态身份摘要。
- **参数：**
  - `type` — Capability 名称
  - `majorVersion` — API 主版本
  - `providerId` — Provider 标识
  - `tags` — 合并后的路由标签
  - `config` — 配置 schema 摘要

#### `ConfigItemSnapshot(String key, String type, boolean required, boolean secret) → record`
- **说明：** 配置 schema 的安全摘要。
- **参数：**
  - `key` — 配置键
  - `type` — 配置类型名称
  - `required` — 是否必填
  - `secret` — 是否敏感


## PluginDefinitionSnapshotMapper

**类型：** class

在运行时定义与持久化摘要之间执行显式、安全映射。

### 方法

#### `from(PluginDefinition definition, PluginSource source) → PluginDefinitionSnapshot`
- **说明：** 创建不含敏感配置值的定义快照。
- **参数：**
  - `definition` — 运行时插件定义
  - `source` — 声明来源元数据
- **返回：** 可持久化的安全定义快照

#### `from(PluginDefinition definition,
            PluginSource source,
            PluginContributionSnapshotterRegistry snapshotters) → PluginDefinitionSnapshot`
- **说明：** 使用宿主注册的快照器生成 Contribution 摘要。
- **参数：**
  - `definition` — 运行时插件定义
  - `source` — 声明来源元数据
  - `snapshotters` — 宿主注册的 Contribution 快照器表
- **返回：** 可持久化的安全定义快照

#### `toJson(PluginDefinitionSnapshot snapshot) → String`
- **说明：** 序列化安全快照为 JSON。
- **参数：**
  - `snapshot` — 待序列化的定义快照
- **返回：** JSON 文本

#### `fromJson(String json) → PluginDefinitionSnapshot`
- **说明：** 从 JSON 恢复安全快照。
- **参数：**
  - `json` — 定义快照 JSON 文本
- **返回：** 反序列化后的定义快照


## PluginInstallation

**类型：** record

插件安装事实与管理员意图的不可变领域模型。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `installationId` | `String` | 安装记录主键 |
| `pluginId` | `String` | 稳定的插件标识 |
| `pluginVersion` | `String` | 已安装或最近发现的插件版本 |
| `sourceType` | `PluginSourceType` | 定义来源类型 |
| `sourceLocation` | `String` | 定义来源位置 |
| `presence` | `PluginPresence` | 当前有效目录中的存在性 |
| `installed` | `boolean` | 是否已执行安装动作 |
| `desiredEnabled` | `boolean` | 管理员是否期望启用 |
| `definitionSnapshot` | `String` | 脱敏后的定义 JSON 快照 |
| `lastRuntimeState` | `String` | 最近持久化的运行状态 |
| `lastError` | `String` | 最近持久化的脱敏错误 |
| `firstDiscoveredAt` | `LocalDateTime` | 首次发现时间 |
| `lastDiscoveredAt` | `LocalDateTime` | 最近一次发现时间 |
| `installedAt` | `LocalDateTime` | 安装时间；未安装时为 null |
| `enabledAt` | `LocalDateTime` | 最近一次启用时间 |
| `disabledAt` | `LocalDateTime` | 最近一次禁用时间 |
| `missingAt` | `LocalDateTime` | 标记为缺失的时间 |


## PluginManagementView

**类型：** record

安装事实、管理员意图与当前 JVM 运行事实的聚合只读视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `installation` | `PluginInstallation` | 持久化的安装事实 |
| `runtime` | `Optional<PluginRuntimeInfo>` | 当前 JVM 中的运行快照；未加载运行时时为空 |

### 方法

#### `pluginId() → String`
- **说明：** / public PluginManagementView { if (installation == null || runtime == null) { throw NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED, "plugin management view requires installation and runtime values"); } runtime = runtime.isEmpty() ? Optional.empty() : Optional.of(runtime.orElseThrow()); } /** 返回稳定插件身份。
- **返回：** 插件标识

#### `runtimeState() → String`
- **说明：** 返回持久化的最近运行状态；运行时尚未创建时为空。
- **返回：** 当前或最近持久化的运行状态名称

#### `lastError() → String`
- **说明：** 返回持久化或运行时诊断中的最近错误。
- **返回：** 脱敏后的最近错误；无错误时可能为 null

#### `runtimePhase() → String`
- **说明：** 返回当前运行阶段。
- **返回：** 运行时阶段；未加载运行时时为 null

#### `runtimeDiscoveredAt() → Instant`
- **说明：** 返回当前运行时记录的发现时间。
- **返回：** 运行时发现时间；未加载运行时时为 null

#### `runtimeStartedAt() → Instant`
- **说明：** 返回当前运行时记录的启动时间。
- **返回：** 运行时启动时间；未启动或未加载运行时时为 null
