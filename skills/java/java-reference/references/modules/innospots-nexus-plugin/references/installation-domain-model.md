# 包 `installation.domain.model`

## `PluginDefinitionSnapshot`

**类型：** record

可持久化的插件静态摘要，不包含 Class、Factory、Handler、配置值或 Secret。

| 组件 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 插件标识 |
| `version` | `String` | 插件版本 |
| `apiVersion` | `int` | 插件 API 主版本 |
| `sourceType` | `String` | 来源类型文本 |
| `sourceLocation` | `String` | 来源位置 |
| `capabilities` | `List<CapabilitySnapshot>` | 兼容旧快照的数据槽；当前 Mapper 写空列表 |
| `contributions` | `List<Map<String, Object>>` | 安全 Contribution 摘要 |

### `CapabilitySnapshot`

**类型：** record

`type`、`majorVersion`、`providerId`、`tags`、`config`。当前 catalog v2 产生的新快照不使用
该嵌套类型。

### `ConfigItemSnapshot`

**类型：** record

`key`、`type`、`required`、`secret`。

## `PluginDefinitionSnapshotMapper`

**类型：** class（final）

### 静态方法

- `from(PluginDefinition definition, PluginSource source) → PluginDefinitionSnapshot`
- `from(PluginDefinition definition, PluginSource source, PluginContributionSnapshotterRegistry snapshotters)`
- `toJson(PluginDefinitionSnapshot snapshot) → String`
- `fromJson(String json) → PluginDefinitionSnapshot`

无 Snapshotter 的重载使用空注册表；含 Contribution 的定义必须为每种类型注册 Snapshotter。

## `PluginInstallation`

**类型：** record

安装事实与管理员意图的不可变模型。

| 组件 | 类型 | 说明 |
|------|------|------|
| `installationId` | `String` | 安装记录主键 |
| `pluginId` | `String` | 插件标识 |
| `pluginVersion` | `String` | 已安装/最近发现版本 |
| `sourceType` | `PluginSourceType` | 来源类型 |
| `sourceLocation` | `String` | 来源位置 |
| `presence` | `PluginPresence` | PRESENT / MISSING |
| `installed` | `boolean` | 是否已安装 |
| `desiredEnabled` | `boolean` | 管理员是否期望启用 |
| `definitionSnapshot` | `String` | 脱敏 JSON 摘要 |
| `lastRuntimeState` | `String` | 最近持久化运行状态 |
| `lastError` | `String` | 最近持久化错误 |
| `firstDiscoveredAt` | `LocalDateTime` | 首次发现 |
| `lastDiscoveredAt` | `LocalDateTime` | 最近发现 |
| `installedAt` | `LocalDateTime` | 安装时间 |
| `enabledAt` | `LocalDateTime` | 最近启用 |
| `disabledAt` | `LocalDateTime` | 最近禁用 |
| `missingAt` | `LocalDateTime` | 标记缺失时间 |

未安装插件不能设置 `desiredEnabled = true`。

## `PluginManagementView`

**类型：** record

安装事实与可选运行时信息的组合视图。构造时要求安装事实和运行时容器均非 `null`。

| 组件 | 类型 | 说明 |
|------|------|------|
| `installation` | `PluginInstallation` | 持久化安装事实与管理员意图 |
| `runtime` | `Optional<PluginRuntimeInfo>` | 当前运行时快照；未运行时为空 |

### 方法

- `pluginId() → String`
- `pluginName() → String`
- `runtimeState() → String`
- `lastError() → String`
- `runtimePhase() → String`
- `runtimeDiscoveredAt() → Instant`
- `runtimeStartedAt() → Instant`
