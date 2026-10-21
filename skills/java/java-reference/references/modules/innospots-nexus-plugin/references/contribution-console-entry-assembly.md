# console@2 entry 组装（Java）

在 **innospots-nexus-plugin** 包 `contribution.console` 中组装 classpath entry 插件。规范见 [plugin-dsl-spec.md](../../../../../../innospots-nexus-plugin/docs/plugin/design/plugin-dsl-spec.md)。

**路由索引：** [console-entry-and-pages.md](../../console-entry-and-pages.md)

## 对外 API（编写 entry 时只需记住两个入口）

| 入口 | 职责 |
|------|------|
| **`ConsoleContributeNode`** | 领域节点 + 静态工厂 `module` / `menu` / `singlePageModule` |
| **`ConsoleContributions`** | `entryPlugin(...)` 生成 `PluginDefinition`；`discoveryTags(...)` 与 `DISCOVERY_TAG_*` 常量 |

包内实现（不对外）：`ConsoleI18n`、`ConsolePermissionDefaults`。

运行时 SPI（宿主 CDI 注册，非 entry 作者 API）：`ConsolePluginContributionDecoder`、`ConsolePluginContributionHandler`、`ConsolePluginContributionSnapshotter`、`ConsoleContributionCatalog`。

## 类型关系

```text
ConsoleContributions.entryPlugin(...)
  └─ ConsolePluginContribution（console@2）
        └─ modules: List<ConsoleContributeNode>   // MODULE
              └─ children: MENU（entryPageKey）
```

## 示例

```java
String domain = "nexus";
String pageKey = PageDslPageRef.encode(domain, "plugin", "main");

ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
        "plugin", 60, displayName, description, pageKey, "appstore");

PluginDefinition definition = ConsoleContributions.entryPlugin(
        pluginId,
        version,
        ConsoleContributions.discoveryTags(pluginId, domain, List.of("plugin")),
        displayName,
        description,
        domain,
        List.of(module));
```

多 MODULE / 多 MENU：对 `ConsoleContributeNode.module(...)` 与 `ConsoleContributeNode.menu(...)` 组合后传入 `entryPlugin` 的 `modules` 列表。Platform 见 `PlatformModuleDescriptors.CONSOLE_MODULES` 与 `PlatformConsoleEntryPlugin`。

## 测试

| 模块 | 测试类 |
|------|--------|
| plugin | `ConsoleContributionsTest` |
| console | `BuiltinConsoleEntryPluginsTest`、`ConsoleModuleEntryPluginsTest` |
| platform | `PlatformConsoleEntryPluginTest` |

## 相关

- [contribution-console-overview.md](contribution-console-overview.md)
- [innospots-nexus-console → entry.md](../../innospots-nexus-console/references/entry.md)
