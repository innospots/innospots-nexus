# 控制台 Entry、插件配置与 ModuleDescriptors

跨 `java:reference` / `java:design` / `java:develop` 的路由索引：`console@2` Entry
组装、插件配置接入、`entryPageKey`、console catalog 同步与 platform 多模块聚合。

## 文档地图

| 主题 | 权威文档 |
|------|----------|
| **插件配置 schema、来源优先级、`SecretValue`** | [`java:develop` → plugin-entry-and-configuration.md](../../java-develop/references/plugin-entry-and-configuration.md#2-声明插件配置) |
| **`ConsoleContributions` / `ConsoleContributeNode`** | [plugin → contribution-console-entry-assembly.md](modules/innospots-nexus-plugin/references/contribution-console-entry-assembly.md) |
| **`console@2` 类型快照** | [plugin → contribution-console.md](modules/innospots-nexus-plugin/references/contribution-console.md) |
| **console 内置 entry** | [console → entry.md](modules/innospots-nexus-console/references/entry.md) |
| **platform 多模块聚合** | [`java:develop` → plugin-entry-and-configuration.md §5](../../java-develop/references/plugin-entry-and-configuration.md#5-平台多模块聚合) |
| **管理 REST 前缀** | [console → endpoint-contracts.md](modules/innospots-nexus-console/references/endpoint-contracts.md) |
| **目录树与权限 API** | [console → catalog-permission.md](modules/innospots-nexus-console/references/catalog-permission.md) |

## 三层模型

```text
plugin.yaml / PluginDefinition
  ├─ spec.config → ConfigDefinition（配置 schema，不含生产密钥）
  └─ spec.contributions[]
       └─ console@2（ConsolePluginContribution）
            ├─ domainKey
            └─ modules[] → ConsoleContributeNode（MODULE）
                  └─ children[] → MENU 或嵌套 MODULE
                        │
                        ▼
console: ConsoleCatalogSyncService
  → nx_console_catalog_resource（MODULE / MENU）
```

| 层 | 当前 API | 不应包含 |
|----|----------|----------|
| 配置 schema | `ConfigDefinition` / `ConfigItemDefinition` | 菜单、`entryPageKey`、生产密钥 |
| 配置值 | `PluginRuntimeConfig` → `ConfigurationManager` → `PluginConfig` | 插件 JAR 内生产配置 |
| Entry 声明 | `ConsolePluginContribution` / `ConsoleContributeNode` | REST 实现、数据库查询、Spring Bean |

`entryPageKey` 是稳定的入口身份，不是配置项，也不要求宿主打包某种页面运行时资源。
前端或外部页面工程按自身契约消费该键；console 只负责目录、授权与导航。

## Java Entry 组装

**plugin 模块**提供两个公开入口：

- `ConsoleContributeNode`：`module(...)`、`menu(...)`、`singlePageModule(...)`
- `ConsoleContributions`：`entryPlugin(...)`、`discoveryTags(...)`

```java
String domainKey = "reports";
String entryPageKey = "reports-reports-main";

ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
        "reports", 10, displayName, description, entryPageKey, "chart");

PluginDefinition definition = ConsoleContributions.entryPlugin(
        "com.example.reports.console",
        version,
        ConsoleContributions.discoveryTags(
                "com.example.reports.console",
                domainKey,
                List.of("reports")),
        displayName,
        description,
        domainKey,
        List.of(module));
```

多 MENU 使用 `ConsoleContributeNode.module(...)` 并在 `children` 放 `menu(...)`。
单插件多 MODULE 直接把多个 MODULE 传入 `entryPlugin(...)`。

规则：

- `entryPageKey` 使用稳定、可读的小写 kebab-case 字面量，例如
  `reports-reports-main`、`platform-user-main`。
- 推荐格式为 `{domainKey}-{moduleKey}-{pageSuffix}`，但运行时不得依赖字符串拆分；
  它是不透明稳定键。
- MODULE 必须有 `moduleKey`，并且有 `entryPageKey` 或非空 `children`，二者不能同时存在。
- MENU 没有 `moduleKey`，必须有 `entryPageKey`。
- `domainKey`、`moduleKey`、权限组名使用小写 kebab-case。

## Platform 聚合

`innospots-nexus-platform` 的 `platform.entry` 使用四个窄类型：

| 类型 | 职责 |
|------|------|
| `PlatformModuleDescriptors` | 集中声明 `ORGANIZATION`、`USERS` 等 MODULE 常量及 `CONSOLE_MODULES` 顺序 |
| `PlatformEntryPluginDescriptor` | 绑定 platform pluginId、显示文本和 MODULE 列表；拒绝空列表 |
| `PlatformModuleEntrySupport` | 调用 `ConsoleContributions.entryPlugin(...)` |
| `PlatformConsoleEntryPlugin` | `Plugin.definition()` 返回上述组装结果 |

```java
public final class PlatformModuleDescriptors {

    public static final ConsoleContributeNode USERS = pageModule(
            "user",
            20,
            displayName,
            description,
            "platform-user-main",
            "team");

    public static final List<ConsoleContributeNode> CONSOLE_MODULES = List.of(
            ORGANIZATION,
            MENU,
            USERS,
            DICTIONARY,
            ROLE,
            PERMISSION,
            LOGGER,
            SETTINGS);
}
```

```java
PluginDefinition definition = PlatformModuleEntrySupport.definition(
        PlatformEntryPluginDescriptor.platformConsole());
```

`PlatformEntryPluginDescriptor` 不是 plugin 模块的通用容器。新增跨模块通用描述符前，
必须有至少两个真实消费者；单个 platform 聚合继续使用当前 record。

## 内置 Entry

| 宿主 | 必选插件 ID | 入口页键 |
|------|-------------|----------|
| **portal / console 宿主** | `BuiltinConsoleEntryPlugins.REQUIRED_PLUGIN_IDS`（当前仅 `com.innospots.nexus.console.plugin-management`） | `nexus-plugin-main` |
| **platform 宿主** | `BuiltinPlatformEntryPlugins.REQUIRED_PLUGIN_IDS`（`com.innospots.nexus.platform.console`） | 各 MODULE 的稳定 `entryPageKey`，例如 `platform-user-main` |

Entry 只声明目录入口与 `entryPageKey`，不会创建页面资源，也不负责前端路由实现。

## 注册与测试

1. `Plugin` 实现放入所属模块的 `entry` 包。
2. 注册 `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin`。
3. 仅当宿主必须启用时加入对应 `Builtin*EntryPlugins.REQUIRED_PLUGIN_IDS`。
4. 安装/启用后由 console catalog 同步读取活动 Contribution。

| 测试 | 断言 |
|------|------|
| `ConsoleContributionsTest` | 单页、多 MENU、多 MODULE、发现标签 |
| `BuiltinConsoleEntryPluginsTest` / `ConsoleModuleEntryPluginsTest` | 内置 ID、SPI、稳定 `entryPageKey` |
| `PlatformConsoleEntryPluginTest` | 聚合顺序、realm 标签、MODULE/MENU 形态 |
| 配置测试 | default → host → source → env → system → runtime；未知键/密文拒绝 |

## 易混概念

| 名称 | 说明 |
|------|------|
| **`menu:` 资源键** | catalog 中 MENU 节点（`menu:{moduleKey}.{entryPageKey}`），与租户 `nx_menu` 表无关 |
| **`module:` 资源键** | catalog 中 MODULE 节点（`module:{moduleKey}`） |
| **侧栏导航 API** | `GET /api/d/nexus/navigation/menus` |
| **权限配置树** | `GET /api/d/nexus/catalog/tree`（MODULE → MENU / 嵌套 MODULE） |

## 模块归属

- **plugin：** `console@2` 模型、解码、Entry 组装、Contribution Handler/Snapshotter。
- **console：** 内置 plugin-management Entry、catalog 同步、权限与导航 API。
- **platform：** `PlatformModuleDescriptors` + `PlatformConsoleEntryPlugin`。

完整归属见 [module-ownership.md](module-ownership.md)。
