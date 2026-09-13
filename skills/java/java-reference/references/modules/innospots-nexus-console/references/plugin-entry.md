# 插件管理与控制台入口插件

## 插件管理 API

`PluginManagementEndpoint` 委托给 **core** 的 `PluginInstallationManager`：

| 操作 | 行为 |
|-----------|----------|
| `list` / `get` | `PluginManagementView` → 通过 MapStruct 转换器转为 `PluginManagementVo` |
| `install` | 安装 + 启动 |
| `enable` / `disable` | 生命周期切换；可选目录同步 |
| `retry` | 重启 FAILED 插件 |

错误使用 plugin 模块的 `PluginStatusCode`。无卸载/删除契约。

依赖方向：console → core 插件安装 API；console **不**定义贡献格式。

## 内置入口插件

`BuiltinConsoleEntryPlugins.REQUIRED_PLUGIN_IDS`：

| 常量 | 插件 ID |
|----------|-----------|
| `MENU` | `com.innospots.nexus.console.menu` |
| `DICTIONARY` | `com.innospots.nexus.console.dictionary` |
| `LOGGER` | `com.innospots.nexus.console.logger` |
| `PERMISSION` | `com.innospots.nexus.console.permission` |
| `ROLE` | `com.innospots.nexus.console.role` |
| `PLUGIN_MANAGEMENT` | `com.innospots.nexus.console.plugin-management` |

每个 `*EntryPlugin` 实现 `com.innospots.nexus.core.plugin.Plugin`，
并为插件运行时注册控制台模块元数据。

## ConsoleModuleDescriptor

单个内置模块的不可变 record：

- `pluginId`、`moduleKey`、`pageKey`、`pagePath`
- `menuKey`、`menuIcon`、`orderIndex`
- `displayName`、`description`、`pageTitle`（`I18nObject`）

`ConsoleModuleEntrySupport` 从描述符构建贡献载荷。
`mainPageKey(moduleKey)` → `{moduleKey}-main`。

## 宿主集成

1. 随应用交付上述六个 ID 的内置入口 JAR 或 classpath 插件。
2. 通过 `/console/plugins` 或启动安装器安装/启用。
3. 运行目录同步，使权限树包含模块页面。
4. 在用户看到模块之前，通过 `GrantManagementEndpoint` 为角色授权。

Page DSL 文件和 `console@1` 贡献模式保留在 **innospots-nexus-plugin** 中。
