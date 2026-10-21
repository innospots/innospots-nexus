# 控制台 entry 插件与 Page 页面键

跨 `java:reference` / `java:design` / `java:develop` 的**路由索引**：内置 entry 组装、`pageKey` 规范、classpath 布局与 catalog 同步分工。实现以代码为准；细节见各模块 `references/`。

## 文档地图

| 主题 | 权威文档 |
|------|----------|
| **console@2 贡献树与 Java 组装** | [innospots-nexus-plugin → contribution-console-entry-assembly.md](modules/innospots-nexus-plugin/references/contribution-console-entry-assembly.md) |
| **本模块内置 entry ID / 路由** | [innospots-nexus-console → entry.md](modules/innospots-nexus-console/references/entry.md) |
| **Platform 聚合 entry** | `innospots-nexus-platform` 包 `platform.entry`（`PlatformConsoleEntryPlugin`） |
| **Page DSL YAML 路径与 `parentPageKey`** | [innospots-nexus-plugin-ui-spec → workflow-and-layout.md](modules/innospots-nexus-plugin-ui-spec/references/workflow-and-layout.md) |
| **`page.id` 与 DSL 顶层字段** | [plugin-ui-spec → yaml-document.md](modules/innospots-nexus-plugin-ui-spec/references/yaml-document.md) |
| **`console@2` 运行时类型** | [innospots-nexus-plugin → contribution-console-overview.md](modules/innospots-nexus-plugin/references/contribution-console-overview.md) |
| **管理 REST 前缀** | [console → endpoint-contracts.md](modules/innospots-nexus-console/references/endpoint-contracts.md)（`ConsoleConstant.API_PREFIX` = `/api/d/nexus`） |
| **目录树与权限 API** | [console → catalog-permission.md](modules/innospots-nexus-console/references/catalog-permission.md) |

## 分层职责

```text
plugin.yaml / PluginDefinition
  └─ console@2（ConsolePluginContribution）
        ├─ domainKey
        └─ modules[] → ConsoleContributeNode（MODULE）
              └─ children[] → ConsoleContributeNode（MENU：entryPageKey、可选 icon）
                    │
                    ▼
classpath: ui-pages/{domainKey}/{moduleKey}/{pageKey}.yaml  →  Page DSL（page.id = pageKey）
                    │
                    ▼
console: ConsoleCatalogSyncService  →  nx_console_catalog_resource（MODULE / MENU / PAGE 树）
```

| 层 | 谁声明「有哪些页面」 | 谁声明「侧栏菜单」 |
|----|----------------------|-------------------|
| **plugin** `ConsoleContributeNode` / YAML `modules[]` | MENU 的 `entryPageKey`（复合 pageKey）；子页由 YAML `parentPageKey` 与 catalog 发现 | MODULE 下 MENU 子节点（递归 `children`） |
| **console** `entry` 包 | 仅模块级：`BuiltinConsoleEntryPlugins`、`ConsolePageRoutes` | 内置 SPI：`PluginManagementEntryPlugin` |

**组装归属 plugin 模块**（middleware-free、可被 platform/console 共用）：

- `ConsoleContributeNode` — 贡献树节点 + 静态工厂（`module` / `menu` / `singlePageModule`）
- `ConsoleContributions` — `entryPlugin(...)` 与 `discoveryTags(...)`（`DISCOVERY_TAG_*` 常量）

默认权限分组等为包内 `ConsolePermissionDefaults`，不暴露为 public API。

**console 模块**仅保留 `BuiltinConsoleEntryPlugins`、`ConsolePageRoutes` 与 `PluginManagementEntryPlugin`。

## 内置 entry 现状（本仓库）

| 宿主 | 必选插件 ID | 说明 |
|------|-------------|------|
| **portal / console 宿主** | `BuiltinConsoleEntryPlugins.REQUIRED_PLUGIN_IDS` | 当前仅 `com.innospots.nexus.console.plugin-management`（`PluginManagementEntryPlugin`） |
| **platform 宿主** | `BuiltinPlatformEntryPlugins.REQUIRED_PLUGIN_IDS` | `com.innospots.nexus.platform.console`（单插件多 MODULE，`ui-pages/platform/**`） |

`innospots-nexus-console` 的 classpath 仍含 `ui-pages/nexus/{menu,dictionary,...}/` 等 YAML，供后续 entry 或其它模块贡献；**未**再声明对应 `pluginId` 常量或 `*EntryPlugin`。

## `pageKey` 速查

| 项 | 规则 |
|----|------|
| 格式 | 复合键 `{domainKey}-{moduleKey}-{pageSuffix}`（`PageDslPageRef.encode`）；后缀段 kebab-case，≤ 128 |
| 一致性 | MENU `entryPageKey` = YAML `page.id` = 文件名 `{pageKey}.yaml` |
| 内置 nexus 域 | `ConsolePageRoutes.BUILTIN_DOMAIN_KEY`（`nexus`） |
| 授权名 | catalog 访问项 `grantName` = MENU `entryPageKey` |
| 前端路由 | `/page/{domainKey}/{moduleKey}/{pageKey}`（`ConsolePageRoutes.pagePath`） |

## 易混概念

| 名称 | 说明 |
|------|------|
| **`MenuEntryPlugin`（历史）** | 已移除；菜单管理页 YAML 仍在 `ui-pages/nexus/menu/`，需新 entry 或 portal 贡献时再注册 |
| **`menu:` 资源键** | catalog 中 MENU 节点（`menu:{moduleKey}.{entryPageKey}`），与租户 `nx_menu` 表无关 |
| **侧栏导航 API** | `GET /api/d/nexus/navigation/menus`（授权后组装） |
| **权限配置树** | `GET /api/d/nexus/catalog/tree`（MODULE → PAGE 嵌套；不含 MENU 节点） |

## 模块归属（摘要）

- **plugin：** `console@2` 模型、解码、entry 组装工厂、Page DSL 解析类型（Java 部分在 console）
- **console：** 内置 `PluginManagementEntryPlugin`、`ConsolePageRoutes`、catalog 同步与 REST
- **platform：** `PlatformModuleDescriptors` + `PlatformConsoleEntryPlugin`（聚合 platform 模块 entry）

完整归属见 [module-ownership.md](module-ownership.md)。
