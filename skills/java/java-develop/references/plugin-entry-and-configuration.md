# 插件配置、Entry 与 ModuleDescriptors

本文解决三类容易混在一起的问题：

1. 插件需要哪些配置、配置值从哪里来、运行时怎样读取。
2. 怎样把 console MODULE/MENU 声明组装成 `PluginDefinition`。
3. 多模块插件怎样通过 `ModuleDescriptors` 与 `EntryPluginDescriptor` 集中管理。

## 1. 三层模型

| 层 | 声明内容 | 当前类型 | 不应包含 |
|----|----------|----------|----------|
| 配置 schema | 键、类型、必填、默认值、密文 | `ConfigDefinition` / YAML `spec.config` | 生产密钥、租户运行时值 |
| 配置值 | 宿主提供的扁平键值 | `PluginRuntimeConfig` → `ConfigurationManager` → `PluginConfig` | 插件 JAR 内的生产配置 |
| 入口声明 | domain、MODULE、MENU、entryPageKey、权限分组 | `ConsolePluginContribution` / `ConsoleContributeNode` | REST 实现、数据库查询、Spring Bean |

入口声明不是插件配置。不要把 `moduleKey`、`entryPageKey`、菜单顺序写进
`ConfigDefinition`。

## 2. 声明插件配置

### Java

```java
PluginDefinition definition = PluginDefinition.builder("com.example.reports")
        .name("Reports")
        .version("1.0.0")
        .config(ConfigDefinition.builder()
                .string("endpoint").required().end()
                .integer("timeoutSeconds").defaultValue("30").end()
                .uri("callbackUrl").required().end()
                .enumeration("mode", "fast", "safe").defaultValue("safe").end()
                .secret("apiToken").required().end()
                .build())
        .contribute(contribution)
        .build();
```

### YAML

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.reports
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    en: Reports
    zh-CN: 报表
  config:
    - key: endpoint
      type: URI
      required: true
    - key: timeoutSeconds
      type: INTEGER
      default: 30
    - key: apiToken
      type: SECRET
      required: true
  contributions:
    - type: console
      majorVersion: 2
      domainKey: reports
      displayName:
        en: Reports
        zh-CN: 报表
      modules:
        - moduleKey: reports
          displayName:
            en: Reports
            zh-CN: 报表
          description:
            en: Report management.
            zh-CN: 报表管理。
          entryPageKey: reports-reports-main
          icon: chart
```

当前编译器拒绝 `spec.capabilities` 与 `spec.requirements`；新插件使用
`spec.contributions`，且至少要声明一份 Contribution。console Contribution 的
`modules` 也必须非空；YAML 的 `default` 字段对应 Java `defaultValue(...)`。

配置约束：

- key 使用小写驼峰，最长 128 字符。
- `SECRET` 禁止默认值，运行时通过 `SecretValue` 访问。
- `ENUM` 必须声明 `enumValues`；其它类型不得声明。
- `URI` 必须是绝对 URI。
- `DURATION` 使用 ISO-8601，例如 `PT30S`。
- 未在 schema 中声明的 `plugins.<pluginId>.*` 键会拒绝解析，而不是静默忽略。

## 3. 接入运行时值

宿主构造 `PluginRuntimeConfig`：

```java
PluginRuntimeConfig runtimeConfig = new PluginRuntimeConfig(
        Set.of("com.example.reports"),
        Set.of(),
        Map.of(
                "plugins.com.example.reports.endpoint", "https://reports.example.com",
                "plugins.com.example.reports.apiToken", resolvedToken),
        List.of(new DatabaseConfigSource(configRepository)),
        Map.of("plugins.com.example.reports.timeoutSeconds", "45"),
        pluginClassLoader);
```

配置来源优先级从低到高：

```text
schema default
→ hostConfig
→ ConfigSource（列表顺序，后者覆盖前者）
→ NEXUS_PLUGIN_* 环境变量
→ JVM 系统属性
→ runtimeVariables
```

完整键与命名空间：

```text
plugins.<pluginId>.<localKey>
```

环境变量名由 `ConfigurationManager.environmentName(pluginId, localKey)` 生成。
同一 classpath 快照中若两个逻辑键映射到同一环境变量名，启动应失败。

`ConfigSource`：

- 由宿主实现，不由插件 JAR 提供。
- 每次插件 start 时调用一次 `values()`。
- 必须返回不可变快照。
- 一次启动周期内 `PluginConfig` 不再刷新；热更新需要重启插件或重建运行时。

插件读取：

```java
@Override
public void initialize(PluginContext context) {
    URI endpoint = context.config().getUri("callbackUrl", null);
    int timeout = context.config().getInt("timeoutSeconds", 30);
    try (SecretValue token = context.config().requireSecret("apiToken")) {
        token.use(chars -> configureClient(endpoint, timeout, chars));
    }
}
```

不要输出 `SecretValue`、把密钥写进日志，或把明文密钥放进 `plugin.yaml`。

## 4. 组装一个 Entry 插件

### 单页 MODULE

```java
String domainKey = "reports";
String entryPageKey = "reports-reports-main";

ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
        "reports",
        10,
        I18nObject.of("en", "Reports", "zh-CN", "报表"),
        I18nObject.of("en", "Report management.", "zh-CN", "报表管理。"),
        entryPageKey,
        "chart");

PluginDefinition definition = ConsoleContributions.entryPlugin(
        "com.example.reports.console",
        "1.0.0",
        ConsoleContributions.discoveryTags(
                "com.example.reports.console",
                domainKey,
                List.of("reports")),
        I18nObject.of("en", "Reports", "zh-CN", "报表"),
        I18nObject.of("en", "Report management.", "zh-CN", "报表管理。"),
        domainKey,
        List.of(module));
```

### 多 MENU MODULE

```java
ConsoleContributeNode module = ConsoleContributeNode.module(
        "settings",
        10,
        I18nObject.of("en", "Settings", "zh-CN", "设置"),
        I18nObject.of("en", "Workspace settings.", "zh-CN", "工作空间设置。"),
        null,
        null,
        List.of(
                ConsoleContributeNode.menu(
                        I18nObject.of("en", "General", "zh-CN", "常规"),
                        10,
                        "nexus-settings-main",
                        "setting"),
                ConsoleContributeNode.menu(
                        I18nObject.of("en", "Advanced", "zh-CN", "高级"),
                        20,
                        "nexus-settings-advanced",
                        "tool")));
```

规则：

- MODULE 必须有 `moduleKey`，并且有 `entryPageKey` 或非空 `children`，二者不能同时存在。
- MENU 没有 `moduleKey`，必须有 `entryPageKey`；当前叶子 MENU 不再挂子节点。
- `domainKey` / `moduleKey` / `group` / `entryPageKey` 使用小写 kebab-case。
- 默认 `viewGroups` / `operationGroups` 为 `default`，自定义时必须显式包含 `default`。
- `entryPageKey` 是稳定不透明键；推荐 `{domainKey}-{moduleKey}-{pageSuffix}` 字面量，
  但运行时不得依赖字符串拆分。

## 5. 平台多模块聚合

当前平台的聚合模式由三个窄类型组成：

```text
PlatformModuleDescriptors
  → 集中声明每个 MODULE 与 CONSOLE_MODULES 顺序

PlatformEntryPluginDescriptor
  → 绑定 pluginId、显示文本和 modules

PlatformModuleEntrySupport
  → 调用 ConsoleContributions.entryPlugin(...)

PlatformConsoleEntryPlugin
  → 实现 Plugin，definition() 返回组装结果
```

### ModuleDescriptors

```java
public final class PlatformModuleDescriptors {

    public static final ConsoleContributeNode USERS = pageModule(
            "user", 20, displayName, description, "main", "team");

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

适用场景：

- 一个 Maven 模块拥有多个管理 MODULE。
- MODULE 顺序、图标、`entryPageKey` 需要在同一处审阅。
- 多个测试需要引用同一批模块常量。

### EntryPluginDescriptor

当前具体类型是 `PlatformEntryPluginDescriptor`：

```java
public record PlatformEntryPluginDescriptor(
        String pluginId,
        I18nObject displayName,
        I18nObject description,
        List<ConsoleContributeNode> modules
) {
    public PlatformEntryPluginDescriptor {
        modules = modules == null ? List.of() : List.copyOf(modules);
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("modules must not be empty");
        }
    }
}
```

```java
PluginDefinition definition =
        PlatformModuleEntrySupport.definition(
                PlatformEntryPluginDescriptor.platformConsole());
```

它不是通用容器：当前值对象使用普通 `IllegalArgumentException` 校验空模块，且只在
platform entry 装配层使用。新增跨模块通用基类之前，先确认至少有两个真实消费者。

## 6. 注册与启停

1. `Plugin` 实现放入所属模块的 `entry` 包。
2. 在 `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin`
   注册实现类全限定名。
3. 把稳定 pluginId 放入该模块的 `Builtin*EntryPlugins.REQUIRED_PLUGIN_IDS`（仅当宿主必须启用）。
4. 安装/启用后由 console 的 catalog 同步任务读取活动贡献。

`ConsoleContributions.discoveryTags(...)` 会生成：

- `kind=entry`
- `domain={domainKey}`
- `module={sortedModuleKeys}`；拼接后超过 64 字符时回退为 pluginId 末段

platform 还会追加 `realm=platform`。标签只用于发现与路由，不替代 pluginId。

## 7. 必须补的测试

| 测试 | 断言 |
|------|------|
| 配置 schema 测试 | 键唯一、必填、默认值、SECRET 无默认值 |
| 配置优先级测试 | default → host → source → env → system → runtime |
| 拒绝路径测试 | 未知键、缺失必填、非法 URI/ENUM、环境变量冲突 |
| 密文测试 | `toString()` 不泄漏，`use()` 后可关闭 |
| entry 结构测试 | MODULE/MENU 形态、排序、默认分组 |
| entry 身份测试 | `entryPageKey` 稳定、唯一且符合小写 kebab-case |
| SPI 注册测试 | `META-INF/services` 包含入口类 |
| 多模块聚合测试 | `CONSOLE_MODULES` 顺序与插件贡献一致 |

## 8. 反模式

| 反模式 | 替代 |
|--------|------|
| 在 `ConfigDefinition` 中声明菜单或 `entryPageKey` | 放进 `ConsoleContributeNode` |
| 在插件 JAR 中写生产密钥 | 宿主 `ConfigSource` / `runtimeVariables` |
| 用 `System.getenv()` 在插件内自行读配置 | 统一走 `PluginContext.config()` |
| 每个 MODULE 新建一个插件 | 同一领域优先用一个聚合 entry |
| 使用已移除的历史 Entry 门面或旧描述符类型 | 继续使用当前 `ConsoleContributions`、`ConsoleContributeNode`、`PlatformModuleDescriptors` 与 `PlatformEntryPluginDescriptor` |
| 依赖 `EndpointGrant.group()` 分组 | 当前运行时只使用 `default` 组 |
| 只在 `REQUIRED_PLUGIN_IDS` 增加 ID | 同时实现 `Plugin` 并注册 ServiceLoader |
