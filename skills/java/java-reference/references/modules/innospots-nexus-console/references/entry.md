# 包 `entry`

`innospots-nexus-console` 内置 Entry 身份、构件版本与通用页面导航路径。贡献树与
`PluginDefinition` 组装在 **plugin**：
[contribution-console-entry-assembly.md](../../innospots-nexus-plugin/references/contribution-console-entry-assembly.md)。

**跨技能索引：**
[console-entry-and-configuration.md](../../../console-entry-and-configuration.md)

## `BuiltinConsoleEntryPlugins`

**类型：** class（final）

### 常量

| 名称 | 值 / 说明 |
|------|-----------|
| `PLUGIN_MANAGEMENT` | `com.innospots.nexus.console.plugin-management` |
| `REQUIRED_PLUGIN_IDS` | 当前仅 `PLUGIN_MANAGEMENT` |

### 方法

#### `pluginVersion() → String`

返回 `innospots-nexus-console` JAR 的 `Implementation-Version`；缺失时返回
`0.0.0-dev`。

#### `tagsFor(String pluginId, ConsolePluginContribution contribution) → Tags`

委托 `ConsoleContributions.discoveryTags(pluginId, contribution)`。

## `ConsolePageRoutes`

**类型：** class（final）

### 常量

| 名称 | 值 | 说明 |
|------|----|------|
| `BUILTIN_DOMAIN_KEY` | `nexus` | 内置 entry 使用的领域键 |
| `PAGE_ROUTE_PREFIX` | `/page` | 通用页面导航路径前缀 |

### 方法

#### `entryPageKey(String domainKey, String moduleKey, String pageSuffix) → String`

生成推荐格式 `{domainKey}-{moduleKey}-{pageSuffix}`。调用方应把返回值视为稳定不透明键。

#### `pagePath(String domainKey, String moduleKey, String entryPageKey) → String`

生成 `/page/{domainKey}/{moduleKey}/{entryPageKey}` 导航路径。

## 入口页键规范

| 项 | 规则 |
|----|------|
| 格式 | 推荐 `{domainKey}-{moduleKey}-{pageSuffix}` |
| 稳定性 | 创建后不得因重构变化；运行时不得依赖字符串拆分 |
| 唯一性 | 同一模块内唯一，跨插件资源冲突由 Contribution Handler 拒绝 |
| 侧栏访问授权名 | 叶子 MODULE/MENU 的 `entryPageKey` |
| 页面资源 | 由外部页面工程或宿主自行管理；console 不打包页面运行时 |

## 本模块内置实现

`com.innospots.nexus.console.plugin.entry.PluginManagementEntryPlugin`

| 项 | 值 |
|----|----|
| pluginId | `BuiltinConsoleEntryPlugins.PLUGIN_MANAGEMENT` |
| domainKey | `nexus` |
| moduleKey | `plugin` |
| entryPageKey | `nexus-plugin-main` |

声明 `entryPageKey` 不会创建页面文件。没有真实 `Plugin` 实现贡献 MODULE/MENU 时，
Entry 不会进入 catalog；页面实现由外部页面工程或宿主按自身契约提供。

## 测试

- `BuiltinConsoleEntryPluginsTest`
- `ConsoleModuleEntryPluginsTest`
- `ConsoleContributionsTest`（plugin 模块）
