# 插件管理与内置入口

- **plugin**：`PluginManagementEndpoint` → `PluginInstallationManager`（plugin 模块）
- **entry**：`BuiltinConsoleEntryPlugins`、`ConsolePageRoutes`、`PluginManagementEntryPlugin`
- **openapi**：`NexusConsoleOpenApiDefinition`、`OpenApiScalarDocumentation`

## 内置 entry

当前 **一个** classpath entry：`PluginManagementEntryPlugin`，通过 plugin 模块工厂组装 `console@2`：

```text
ConsoleContributeNodes.singlePageModule(...)
ConsoleEntryPlugins.define(..., ConsoleEntryTags.forEntry(...), ...)
```

| 能力 | API（plugin 包） |
|------|------------------|
| 单页 MODULE | `ConsoleContributeNodes.singlePageModule` |
| 多 MENU | `ConsoleContributeNodes.module` + `menu` |
| 单插件多 MODULE | `ConsoleEntryPlugins.define` 的 `modules` 列表（见 platform） |

`domainKey` / 复合 `entryPageKey` 须与 `ui-pages/{domainKey}/{moduleKey}/{pageKey}.yaml` 及 `page.id` 一致；nexus 域见 `ConsolePageRoutes.BUILTIN_DOMAIN_KEY`。

**规范全文：** [console-entry-and-pages.md](../../../console-entry-and-pages.md)、[entry.md](entry.md)、[contribution-console-entry-assembly.md](../../innospots-nexus-plugin/references/contribution-console-entry-assembly.md)

**测试：** `ConsoleModuleEntryPluginsTest`、`BuiltinConsoleEntryPluginsTest`；plugin 侧 `ConsoleEntryPluginsTest`。

包级参考：`plugin-*.md`、`entry.md`、`openapi*.md`。
