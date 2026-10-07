# 贡献与 console@1

| 包 / 类型 | 说明 |
|-----------|------|
| `contribution.*` | 贡献模型、decoder/snapshotter 注册表 |
| `contribution.console.*` | `ConsolePluginContributionHandler`、UI 规范声明 |
| `contribution.console.ConsolePluginContributionSnapshotter` | 贡献快照 |

控制台 catalog **表**与同步 REST 归属 **innospots-nexus-console**；plugin 只产出解码后的贡献与 Page 元数据。

## `ConsoleModuleDeclaration` 要点

| 字段 | 说明 |
|------|------|
| `domainKey` | Page DSL classpath 第一段：`ui-pages/{domainKey}/{moduleKey}/...` |
| `moduleKey` | 模块键 |
| `pages` | `UiSpecPageDeclaration`（`pageKey`、`pagePath`、可选嵌套 `children`） |
| `menuTree` | `MenuDeclaration`（目录或页面入口；页面节点绑定 `pageKey`） |

`pageKey` 须与 Page DSL `page.id` 及 YAML 文件名一致；路径推荐 `/page/{domainKey}/{moduleKey}/{pageKey}`（`ConsoleModuleDescriptor.pagePath`）。

## 内置 entry 组装（console 模块）

**innospots-nexus-console** 的 `ConsoleModuleEntrySupport` 从 `ConsoleModuleDescriptor` / `ConsoleEntryPluginDescriptor` 生成上述结构（支持**多模块、多页面、多顶层菜单**）。规范索引：[console-entry-and-pages.md](../../console-entry-and-pages.md)。

包级参考：`contribution.md`、`contribution-console.md` 及各子包 `contribution-console-ui-spec-*.md`。
