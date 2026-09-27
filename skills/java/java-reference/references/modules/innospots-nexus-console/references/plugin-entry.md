# 插件管理与内置入口

- **plugin**：`PluginManagementEndpoint` → `PluginInstallationManager`（plugin 模块）
- **entry**：`BuiltinConsoleEntryPlugins`、`ConsoleModuleDescriptor`、`ConsoleModuleEntrySupport`、各 `*EntryPlugin`
- **openapi**：`NexusConsoleOpenApiDefinition`、`OpenApiScalarDocumentation`

## 内置 entry 与 pageKey

六个 `*EntryPlugin` 共用 `ConsoleModuleEntrySupport` 组装 `console@1`。入口页键默认为 **`{moduleKey}-main`**，须与 Page DSL `page.id` 及 `ui-pages/nexus/{moduleKey}/{pageKey}.yaml` 文件名一致。

**规范全文：** [`entry.md` → 入口页与 pageKey 规范](entry.md#入口页与-pagekey-规范)  
**菜单 entry 示例：** [`menu-entry.md`](menu-entry.md)

包级参考：`plugin-*.md`、`entry.md`、`openapi*.md`。
