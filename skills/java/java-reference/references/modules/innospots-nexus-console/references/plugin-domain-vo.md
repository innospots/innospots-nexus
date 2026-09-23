# 包 `plugin.domain.vo`

## PluginManagementVo

**类型：** record

插件管理页面使用的正交安装事实、运行状态和来源视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | — |
| `version` | `String` | — |
| `presence` | `PluginPresence` | — |
| `installed` | `boolean` | — |
| `desiredEnabled` | `boolean` | — |
| `runtimeState` | `String` | — |
| `runtimePhase` | `String` | — |
| `sourceType` | `String` | — |
| `sourceLocation` | `String` | — |
| `lastError` | `String` | — |
| `definitionSnapshot` | `String` | — |
| `firstDiscoveredAt` | `LocalDateTime` | — |
| `lastDiscoveredAt` | `LocalDateTime` | — |
| `installedAt` | `LocalDateTime` | — |
| `enabledAt` | `LocalDateTime` | — |
| `disabledAt` | `LocalDateTime` | — |
| `missingAt` | `LocalDateTime` | — |
| `runtimeDiscoveredAt` | `Instant` | — |
| `runtimeStartedAt` | `Instant` | — |
