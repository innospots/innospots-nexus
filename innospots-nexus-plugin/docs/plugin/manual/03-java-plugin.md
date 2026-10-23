# Java SPI 插件手册

本文说明当前 Java classpath 插件的声明方式：`Plugin` SPI、配置 schema 与 `console@2`
Entry 贡献。YAML 交换格式见 [04-yaml-plugin.md](04-yaml-plugin.md)。

## 1. 最小结构

```text
my-plugin.jar
├── META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
└── com/example/myplugin/
    └── MyPlugin.java
```

SPI 文件每行写一个 `Plugin` 实现类全限定名。

## 2. 实现 Plugin

```java
public final class MyPlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("com.example.message-wecom")
                .name("WeCom Messaging")
                .version("1.0.0")
                .config(ConfigDefinition.builder()
                        .string("corpId")
                        .required()
                        .description("企业 ID")
                        .end()
                        .secret("secret")
                        .required()
                        .description("应用密钥")
                        .end()
                        .build())
                .build();
    }

    @Override
    public void initialize(PluginContext context) {
        String corpId = context.config().required("corpId");
        String secret = context.config().secret("secret");
        // 创建当前插件作用域内的资源
    }

    @Override
    public void start() {
        // 启动行为
    }

    @Override
    public void stop() {
        // 释放行为；ResourceScope 随后关闭
    }
}
```

`definition()` 必须无副作用，只返回不可变声明。配置值通过 `PluginContext.config()`
读取，不在 `definition()` 中读取环境变量或数据库。

## 3. 配置 schema

`ConfigDefinition` 只声明允许的键、类型和校验元数据：

| 方法 | 用途 |
|------|------|
| `string` / `integer` / `longNumber` / `bool` / `decimal` | 基础类型 |
| `duration` / `uri` | 结构化文本类型 |
| `enumeration(key, values...)` | 枚举值 |
| `secret(key)` | 密文；诊断与日志不得输出原值 |
| `required()` / `defaultValue(...)` / `description(...)` | 必填、默认值和展示说明 |

配置值来源与优先级、环境变量命名、密文处理见
[`java:develop` → plugin-entry-and-configuration.md](../../../../skills/java/java-develop/references/plugin-entry-and-configuration.md)。

## 4. 声明 console@2 Entry

Entry 只声明目录树与稳定 `entryPageKey`，不打包页面资源，也不实现 REST。

```java
ConsoleContributeNode reports = ConsoleContributeNode.singlePageModule(
        "reports",
        10,
        I18nObject.of("zh-CN", "报表"),
        I18nObject.of("zh-CN", "报表管理"),
        "reports-reports-main",
        "chart");

@Override
public PluginDefinition definition() {
    return PluginDefinition.builder("com.example.reports.console")
            .version("1.0.0")
            .name("Reports Console")
            .contribute(new ConsolePluginContribution(
                    "reports",
                    I18nObject.of("zh-CN", "报表"),
                    I18nObject.of("zh-CN", "报表管理"),
                    0,
                    List.of(reports)))
            .build();
}
```

规则：

- 扩展根 `modules[]` 至少一项，且只能放 MODULE。
- MODULE 必须有 `moduleKey`，并且有 `entryPageKey` 或非空 `children`，二者不能同时存在。
- MENU 没有 `moduleKey`，必须有 `entryPageKey`，且 `children` 为空。
- `domainKey`、`moduleKey`、`group`、`entryPageKey` 使用小写 kebab-case。
- `entryPageKey` 是稳定不透明键；推荐 `{domainKey}-{moduleKey}-{pageSuffix}`，运行时不得拆分解析。

需要同时声明配置和 Entry 时，使用 `PluginDefinition.builder(...).config(...).contribute(...)`；
仅声明 console Entry 时也可使用 `ConsoleContributions.entryPlugin(...)` 组装。

## 5. ModuleDescriptors 与 EntryPluginDescriptor

一个宿主模块包含多个 MODULE 时，推荐拆成四个窄类型：

| 类型 | 职责 |
|------|------|
| `XxxModuleDescriptors` | 集中声明 MODULE 常量及稳定顺序；只构造 `ConsoleContributeNode` |
| `XxxEntryPluginDescriptor` | 绑定 pluginId、显示文本和 MODULE 列表；拒绝空列表 |
| `XxxModuleEntrySupport` | 调用 `ConsoleContributions.entryPlugin(...)` |
| `XxxConsoleEntryPlugin` | 实现 `Plugin.definition()`，返回组装结果 |

platform 的现有实现可直接对照：

- `PlatformModuleDescriptors`
- `PlatformEntryPluginDescriptor`
- `PlatformModuleEntrySupport`
- `PlatformConsoleEntryPlugin`

Descriptor 不注册 Bean、不访问数据库、不构造 REST 响应。新增跨模块通用描述符前，必须有
至少两个真实消费者；单个宿主继续使用本模块的 record。

## 6. 注册与验证

1. 将 `Plugin` 实现放入所属模块的 `entry` 包。
2. 注册 `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin`。
3. 仅在宿主必须启用时加入对应 `Builtin*EntryPlugins.REQUIRED_PLUGIN_IDS`。
4. 测试 `definition()`、配置 schema、稳定 `entryPageKey` 与 MODULE/MENU 形态。
5. 每批 Java 改动后运行：

```bash
mvn -pl innospots-nexus-plugin -am clean compile
```
