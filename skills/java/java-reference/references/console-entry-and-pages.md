# 控制台 entry 插件与 Page 页面键

跨 `java:reference` / `java:design` / `java:develop` 的**路由索引**：内置 entry 组装、`pageKey` 规范、classpath 布局与 catalog 同步分工。实现以代码为准；细节见各模块 `references/`。

## 文档地图

| 主题 | 权威文档 |
|------|----------|
| **`pageKey` / 入口页 / 多模块组装** | [innospots-nexus-console → entry.md](modules/innospots-nexus-console/references/entry.md) |
| **Page DSL YAML 路径与 `parentPageKey`** | [innospots-nexus-plugin-ui-spec → workflow-and-layout.md](modules/innospots-nexus-plugin-ui-spec/references/workflow-and-layout.md) |
| **`page.id` 与 DSL 顶层字段** | [plugin-ui-spec → yaml-document.md](modules/innospots-nexus-plugin-ui-spec/references/yaml-document.md) |
| **`console@1` Java 类型** | [innospots-nexus-plugin → contribution-console.md](modules/innospots-nexus-plugin/references/contribution-console.md) |
| **菜单 entry 示例（`MenuEntryPlugin`）** | [console → menu-entry.md](modules/innospots-nexus-console/references/menu-entry.md) |
| **管理 REST 前缀** | [console → endpoint-contracts.md](modules/innospots-nexus-console/references/endpoint-contracts.md)（`ConsoleConstant.API_PREFIX` = `/api/d/nexus`） |
| **目录树与权限 API** | [console → catalog-permission.md](modules/innospots-nexus-console/references/catalog-permission.md) |

## 分层职责

```text
plugin.yaml / PluginDefinition
  └─ console@1（ConsolePluginContribution）
        ├─ domainKey + moduleKey + pages[].pageKey / pagePath
        └─ menuTree（MenuDeclaration）
              │
              ▼
classpath: ui-pages/{domainKey}/{moduleKey}/{pageKey}.yaml  →  Page DSL（page.id = pageKey）
              │
              ▼
console: ConsoleCatalogSyncService  →  nx_console_catalog_resource（MODULE / PAGE 树）
```

| 层 | 谁声明「有哪些页面」 | 谁声明「页面父子关系」 |
|----|----------------------|------------------------|
| 贡献 / entry 组装 | `UiSpecPageDeclaration` 或 `ConsoleModuleDescriptor.allPageKeys()` | — |
| Page DSL | — | `page.parentPageKey`（可选） |

内置 entry 通过 **`ConsoleModuleEntrySupport`** 生成 `console@1`：

- **单模块：** `definition(ConsoleModuleDescriptor)`
- **多模块：** `definition(ConsoleEntryPluginDescriptor)`（一个 `pluginId`，多个 `ConsoleModuleDescriptor`）
- **多页面：** `additionalPageKeys` + 各 YAML
- **多顶层菜单：** `menuEntries`（`ConsoleMenuItemDescriptor` 列表）；为空时沿用单条 `MenuDeclaration.page`

六个 `*EntryPlugin` 仍为「一插件一模块」；捆绑多模块时使用同一 `pluginId` 的 `ConsoleEntryPluginDescriptor.of(...)`。

`ConsoleModuleDescriptor.builtin(pluginId, domainKey, moduleKey, entryPageKey, …)` 中 `domainKey` 仍由调用方显式传入；六个内置 entry 使用 `BUILTIN_DOMAIN_KEY`（`nexus`）仅为本模块默认常量，其它产品域传各自 `domainKey`。入口页键使用**复合 pageKey**（`compositePageKey(domainKey, moduleKey, pageSuffix)`，见 `PageDslPageRef`）。自定义 `menuKey` 时用 `ConsoleModuleDescriptor.of(...)`。

## `pageKey` 速查

| 项 | 规则 |
|----|------|
| 格式 | `[a-z][a-z0-9]*(?:-[a-z0-9]+)*`，≤ 128 字符 |
| 一致性 | `console@1.pageKey` = YAML `page.id` = 文件名 `{pageKey}.yaml` |
| 内置入口页 | 各 `*EntryPlugin` 内经 `compositePageKey(domainKey, moduleKey, pageSuffix)` 声明（如 `nexus-menu-main`），经 `builtin(..., entryPageKey, ...)` 传入 |
| `domainKey` | 与 `console@1`、`ui-pages/{domainKey}/...` 一致；内置六个 entry 使用 `BUILTIN_DOMAIN_KEY`（`nexus`），**非 API 强制** |
| 前端路由 | `/page/{domainKey}/{moduleKey}/{pageKey}`（`ConsoleModuleDescriptor.pagePath`） |

## 易混概念

| 名称 | 说明 |
|------|------|
| **`MenuEntryPlugin`** | 贡献「菜单管理」**功能模块**的 entry，不是租户 `nx_menu` 数据 |
| **`menuKey`（console@1）** | 贡献内菜单节点稳定键；内置常与 `entryPageKey` 相同 |
| **侧栏导航 API** | `GET /api/d/nexus/navigation/menus`（授权后组装，与 entry 贡献不同） |
| **权限配置树** | `GET /api/d/nexus/catalog/tree`（MODULE → PAGE → 子 PAGE，不含 MENU 节点） |

## 模块归属（摘要）

- **plugin：** 贡献解码、Page DSL 解析/校验、`console@1` 模型
- **console：** 内置 `*EntryPlugin`、`ConsoleModuleEntrySupport`、catalog 同步与 REST
- **不得**在 console 重复定义 contribution 约束（归属 plugin）

完整归属见 [module-ownership.md](module-ownership.md)。
