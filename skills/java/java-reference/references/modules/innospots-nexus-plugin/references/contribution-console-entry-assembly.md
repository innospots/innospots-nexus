# console@2 Entry 组装

在 **innospots-nexus-plugin** 的 `contribution.console` 包中，把 MODULE/MENU 声明组装为
`PluginDefinition`。实现指南见
[`java:develop` → plugin-entry-and-configuration.md](../../../../../java-develop/references/plugin-entry-and-configuration.md)；
完整 YAML 规范见
[`plugin-dsl-spec.md`](../../../../../../../innospots-nexus-plugin/docs/plugin/design/plugin-dsl-spec.md)。

**路由索引：**
[console-entry-and-configuration.md](../../../console-entry-and-configuration.md)

## 对外入口

| 入口 | 职责 |
|------|------|
| `ConsoleContributeNode` | `module(...)`、`menu(...)`、`singlePageModule(...)` 工厂 |
| `ConsoleContributions` | `entryPlugin(...)` 生成 `PluginDefinition`；`discoveryTags(...)` 生成发现标签 |

包内 `ConsoleI18n`、`ConsolePermissionDefaults` 不是 public API。

运行时 SPI 由宿主注册，不是 entry 作者入口：
`ConsolePluginContributionDecoder`、`ConsolePluginContributionHandler`、
`ConsolePluginContributionSnapshotter`、`ConsoleContributionCatalog`。

## 类型关系

```text
ConsoleContributions.entryPlugin(...)
  └─ ConsolePluginContribution（console@2）
        └─ modules: List<ConsoleContributeNode>（MODULE）
              └─ children: MENU 或嵌套 MODULE
```

## 单页 MODULE

```java
String domain = "nexus";
String entryPageKey = "nexus-plugin-main";

ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
        "plugin",
        60,
        displayName,
        description,
        entryPageKey,
        "appstore");

PluginDefinition definition = ConsoleContributions.entryPlugin(
        pluginId,
        version,
        ConsoleContributions.discoveryTags(pluginId, domain, List.of("plugin")),
        displayName,
        description,
        domain,
        List.of(module));
```

`singlePageModule(...)` 生成叶子 MODULE：`entryPageKey` 非空、`children` 为空。

## 多 MENU MODULE

```java
ConsoleContributeNode module = ConsoleContributeNode.module(
        "settings",
        10,
        displayName,
        description,
        null,
        null,
        List.of(
                ConsoleContributeNode.menu(
                        generalName,
                        10,
                        "nexus-settings-main",
                        "setting"),
                ConsoleContributeNode.menu(
                        advancedName,
                        20,
                        "nexus-settings-advanced",
                        "tool")));
```

模块级静态工厂会按 `orderIndex` 排序子节点。`viewGroups` / `operationGroups` 为空时
使用包含 `default` 的默认分组。

## 单插件多 MODULE

直接把多个 MODULE 放入 `entryPlugin(..., modules)`。platform 的生产实现把该逻辑拆为：

```text
PlatformModuleDescriptors.CONSOLE_MODULES
  → PlatformEntryPluginDescriptor.platformConsole()
  → PlatformModuleEntrySupport.definition(descriptor)
  → ConsoleContributions.entryPlugin(...)
  → PlatformConsoleEntryPlugin.definition()
```

`PlatformEntryPluginDescriptor` 不是 plugin 模块的通用 API；它只在 platform entry 装配层
使用。详见
[`java:develop` → plugin-entry-and-configuration.md §5](../../../../../java-develop/references/plugin-entry-and-configuration.md#5-平台多模块聚合)。

## YAML 等价声明

YAML 入口使用 `spec.contributions[]`：

```yaml
contributions:
  - type: console
    majorVersion: 2
    domainKey: nexus
    displayName:
      en: Settings
      zh-CN: 设置
    modules:
      - moduleKey: settings
        displayName:
          en: Settings
          zh-CN: 设置
        entryPageKey: nexus-settings-main
        icon: setting
```

配置 schema 放在 `spec.config`，不得把 `moduleKey`、`entryPageKey` 或菜单顺序写进配置项。
宿主配置优先级和 `SecretValue` 用法见
[`config.md`](config.md) 与
[`java:develop` → plugin-entry-and-configuration.md §2-§3](../../../../../java-develop/references/plugin-entry-and-configuration.md#2-声明插件配置)。

## 注册与测试

1. `Plugin` 实现放在所属模块的 `entry` 包。
2. 在 `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin` 注册实现类。
3. 仅当宿主必须启用时，把 pluginId 加入对应 `Builtin*EntryPlugins.REQUIRED_PLUGIN_IDS`。

| 模块 | 测试类 |
|------|--------|
| plugin | `ConsoleContributionsTest` |
| console | `BuiltinConsoleEntryPluginsTest`、`ConsoleModuleEntryPluginsTest` |
| platform | `PlatformConsoleEntryPluginTest` |

## 相关

- [contribution-console-overview.md](contribution-console-overview.md)
- [contribution-console.md](contribution-console.md)
- [config.md](config.md)
- [innospots-nexus-console → entry.md](../../innospots-nexus-console/references/entry.md)
