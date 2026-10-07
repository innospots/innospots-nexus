# 包 `menu.entry`

## 与运行时「菜单域」的区别

| 概念 | 类 / 表 | 作用 |
|------|---------|------|
| **菜单管理 entry 插件** | `MenuEntryPlugin` | 向插件运行时贡献 `console@1`：**模块 `menu`、入口页 `menu-main`、一条内置侧栏项** |
| **租户导航菜单数据** | `MenuEntity` / `nx_menu` | 工作区侧栏树；由 portal 域 CRUD（console 无 `/api/nexus/menus` REST） |
| **授权后导航 API** | `NavigationMenuAssembler`、`NavigationMenuEndpoint` | 按权限组装当前用户可见侧栏 |

`MenuEntryPlugin` **不**写入 `nx_menu`；它只注册「菜单管理」这一控制台功能模块的 Page DSL 与 catalog 资源。

---

## MenuEntryPlugin

**类型：** class

贡献「菜单管理」入口页的内置 entry 插件。实现 `Plugin`；`definition()` 委托 `ConsoleModuleEntrySupport`。

### 描述符（与 `pageKey` 规范）

| 字段 | 值 |
|------|-----|
| `pluginId` | `BuiltinConsoleEntryPlugins.MENU`（`com.innospots.nexus.console.menu`） |
| `domainKey` | `ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY`（`nexus`） |
| `moduleKey` | `menu` |
| `entryPageKey` | `menu-main`（`MenuEntryPlugin.ENTRY_PAGE_KEY` 显式常量） |
| `menuKey` | `menu-main`（与入口页键相同） |
| `menuIcon` | `menu` |
| `orderIndex` | `10` |

### Page DSL

```text
innospots-nexus-console/src/main/resources/ui-pages/nexus/menu/menu-main.yaml
```

YAML 必填：`page.id: menu-main`（与 `entryPageKey`、文件名一致）。

### 生成的 `console@1` 形状（概念）

- **pages：** 一条 `UiSpecPageDeclaration("menu-main", "/page/nexus/menu/menu-main", [])`
- **menuTree：** 一条 `MenuDeclaration.page("menu-main", pageTitle, "menu", 10, "menu-main")`

完整 `pageKey` 命名与三层一致性见 [`entry.md` → 入口页与 pageKey 规范](entry.md#入口页与-pagekey-规范)。

### 方法

#### `definition() → PluginDefinition`

- **说明：** 返回上述贡献的不可变插件定义；版本与标签见 `BuiltinConsoleEntryPlugins`。

### 新增类似内置 entry 的模式

1. 在 `com.innospots.nexus.console.<domain>.entry` 增加 `*EntryPlugin`。
2. 在 `BuiltinConsoleEntryPlugins` 增加 `pluginId` 常量（若需纳入 `REQUIRED_PLUGIN_IDS`）。
3. `ConsoleModuleDescriptor.builtin(pluginId, domainKey, moduleKey, entryPageKey, …)` 或 `ConsoleModuleEntrySupport.definition(ConsoleEntryPluginDescriptor.of(...))`（多模块捆绑时）。
4. 添加 `ui-pages/nexus/{moduleKey}/{moduleKey}-main.yaml`，`page.id` 为 `{moduleKey}-main`；多页/多菜单见 [entry.md](entry.md)。
5. 测试可参考 `ConsoleModuleEntryPluginsTest`、`ConsoleModuleEntrySupportTest`、`BuiltinConsoleEntryPluginsTest`。

总索引：[console-entry-and-pages.md](../../../console-entry-and-pages.md)。
