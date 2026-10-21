# 菜单管理与 entry（历史说明）

> **状态：** 独立的 `MenuEntryPlugin` 与 `BuiltinConsoleEntryPlugins.MENU` 已移除。菜单管理 Page DSL 仍位于 `ui-pages/nexus/menu/nexus-menu-main.yaml`；注册进 catalog 需新增 `Plugin` 实现或由 portal 贡献 `console@2` 模块。

## 概念区分

| 概念 | 说明 |
|------|------|
| **菜单管理 UI 模块** | 控制台功能模块（`moduleKey=menu`、入口页 `nexus-menu-main`） |
| **租户菜单数据** | `nx_menu` 与 menu 域 REST（portal）；与 entry 贡献无关 |
| **catalog MENU 节点** | `console@2` MODULE 下 MENU 子节点，资源键 `menu:{moduleKey}.{entryPageKey}` |

## 若重新提供菜单管理 entry

1. 实现 `Plugin`，`definition()` 返回 `ConsoleEntryPlugins.define(...)`（见 [contribution-console-entry-assembly.md](../../innospots-nexus-plugin/references/contribution-console-entry-assembly.md)）。
2. 在本模块或 portal 的 `Builtin*EntryPlugins` 增加 **真实存在** 的 `pluginId` 常量；仅当纳入宿主必选列表时加入 `REQUIRED_PLUGIN_IDS`。
3. 使用 `PageDslPageRef.encode("nexus", "menu", "main")` 作为 `entryPageKey`。
4. 注册 `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin`。
5. 测试参考 `ConsoleModuleEntryPluginsTest`、`BuiltinConsoleEntryPluginsTest`。

`pageKey` 命名与路由见 [entry.md → 入口页与 pageKey 规范](entry.md#入口页与-pagekey-规范)。
