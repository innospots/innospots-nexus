# 贡献与 console@2

| 包 / 类型 | 说明 |
|-----------|------|
| `contribution.*` | 贡献模型、decoder/snapshotter 注册表 |
| `contribution.console.*` | `ConsolePluginContribution`（`console@2`）、`ConsoleContributeNode`、Handler、解码器 |
| `contribution.console.ConsolePluginContributionSnapshotter` | 贡献快照 |

控制台 catalog **表**与同步 REST 归属 **innospots-nexus-console**；plugin 产出解码后的
MODULE/MENU 贡献。

## `ConsolePluginContribution` 要点

| 字段 | 说明 |
|------|------|
| `domainKey` | 控制台领域键，例如 `nexus`、`platform` |
| `modules` | 顶层 `ConsoleContributeNode` 列表（均为 MODULE） |
| `displayName` / `description` / `orderIndex` | 贡献级展示与排序 |

每个 MODULE 节点：

| 字段 | 说明 |
|------|------|
| `moduleKey` | 模块键 |
| `viewGroups` / `operationGroups` | 权限分组（须含 `default`） |
| `children` | MENU 子节点：稳定 `entryPageKey`、可选 `icon` 与权限分组 |

`entryPageKey` 是不透明稳定键。推荐使用
`{domainKey}-{moduleKey}-{pageSuffix}` 字面量，但运行时不得依赖拆分；console 侧
`ConsolePageRoutes.pagePath` 提供通用导航路径。

## Java entry 组装

**plugin 模块**提供工厂，见 [contribution-console-entry-assembly.md](contribution-console-entry-assembly.md)：

- `ConsoleContributeNode`（工厂）、`ConsoleContributions`（entry 插件门面）

**console** 保留 `BuiltinConsoleEntryPlugins`、`ConsolePageRoutes` 与
`PluginManagementEntryPlugin`。**platform** 使用 `PlatformModuleDescriptors` +
`PlatformConsoleEntryPlugin`。

插件 `spec.config`、宿主来源优先级、`SecretValue`、`ModuleDescriptors` 与
`EntryPluginDescriptor` 的实现规范见
[`java:develop` → plugin-entry-and-configuration.md](../../../../../java-develop/references/plugin-entry-and-configuration.md)。

规范索引：[console-entry-and-configuration.md](../../../console-entry-and-configuration.md)。

包级参考：[`contribution.md`](contribution.md)、
[`contribution-console.md`](contribution-console.md) 与
[`contribution-console-entry-assembly.md`](contribution-console-entry-assembly.md)。
