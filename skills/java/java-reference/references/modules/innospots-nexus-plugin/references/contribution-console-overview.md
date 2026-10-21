# 贡献与 console@2

| 包 / 类型 | 说明 |
|-----------|------|
| `contribution.*` | 贡献模型、decoder/snapshotter 注册表 |
| `contribution.console.*` | `ConsolePluginContribution`（`console@2`）、`ConsoleContributeNode`、Handler、解码器 |
| `contribution.console.ConsolePluginContributionSnapshotter` | 贡献快照 |

控制台 catalog **表**与同步 REST 归属 **innospots-nexus-console**；plugin 产出解码后的贡献与 Page 元数据。

## `ConsolePluginContribution` 要点

| 字段 | 说明 |
|------|------|
| `domainKey` | Page DSL classpath 第一段：`ui-pages/{domainKey}/{moduleKey}/...` |
| `modules` | 顶层 `ConsoleContributeNode` 列表（均为 MODULE） |
| `displayName` / `description` / `orderIndex` | 贡献级展示与排序 |

每个 MODULE 节点：

| 字段 | 说明 |
|------|------|
| `moduleKey` | 模块键 |
| `viewGroups` / `operationGroups` | 权限分组（须含 `default`） |
| `children` | MENU 子节点：`entryPageKey`、可选 `icon`；访问分组见 Page DSL `grantGroup` |

`pageKey`（MENU 的 `entryPageKey`）须与 Page DSL `page.id` 及 YAML 文件名一致；前端路由推荐 `/page/{domainKey}/{moduleKey}/{pageKey}`（console 侧 `ConsolePageRoutes.pagePath`）。

## Java entry 组装

**plugin 模块**提供工厂，见 [contribution-console-entry-assembly.md](contribution-console-entry-assembly.md)：

- `ConsoleContributeNode`（工厂）、`ConsoleContributions`（entry 插件门面）

**console** 仅保留 `BuiltinConsoleEntryPlugins`、`ConsolePageRoutes` 与 `PluginManagementEntryPlugin`。**platform** 使用 `PlatformModuleDescriptors` + `PlatformConsoleEntryPlugin`。

规范索引：[console-entry-and-pages.md](../../console-entry-and-pages.md)。

包级参考：`contribution.md`、`contribution-console.md`（类型快照）、各 `contribution-console-ui-spec-*.md`。
