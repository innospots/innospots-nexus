# 插件管理与内置入口

- **plugin**：`PluginManagementEndpoint` → `PluginInstallationManager`（plugin 模块）
- **entry**：`BuiltinConsoleEntryPlugins`、`ConsoleEntryPluginDescriptor`、`ConsoleModuleDescriptor`、`ConsoleModuleEntrySupport`、各 `*EntryPlugin`
- **openapi**：`NexusConsoleOpenApiDefinition`、`OpenApiScalarDocumentation`

## 内置 entry 与 pageKey

六个 `*EntryPlugin` 各贡献**一个** `console@1` 模块（一插件一模块）。共用 `ConsoleModuleEntrySupport` 组装：

| 能力 | API |
|------|-----|
| 单模块（现状） | `definition(ConsoleModuleDescriptor)` |
| 多模块合一插件 | `definition(ConsoleEntryPluginDescriptor)` |
| 多页面 | `additionalPageKeys` + 对应 YAML |
| 多顶层菜单 | `menuEntries`（`ConsoleMenuItemDescriptor`） |

入口页键默认为 **`{moduleKey}-main`**，须与 Page DSL `page.id` 及 `ui-pages/nexus/{moduleKey}/{pageKey}.yaml` 一致。

**规范全文：** [console-entry-and-pages.md](../../../console-entry-and-pages.md)、[entry.md → 入口页与 pageKey 规范](entry.md#入口页与-pagekey-规范)  
**菜单 entry 示例：** [menu-entry.md](menu-entry.md)

包级参考：`plugin-*.md`、`entry.md`、`openapi*.md`。
