# Java SPI 插件手册

## 1. 最小结构

```
my-plugin.jar
├── META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
│   └── com.example.myplugin.MyPlugin
└── com/example/myplugin/
    ├── MyPlugin.java
    └── WeComSender.java          # CapabilityProvider 实现
```

SPI 文件内容为实现类全限定名（一行一个）。

## 2. 实现 Plugin

```java
public final class MyPlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("com.example.message-wecom")
                .name("WeCom Messaging")
                .description("Send messages via WeCom")
                .version("1.0.0")
                .tags(Tags.of("vendor", "wecom"))
                .config(ConfigDefinition.builder()
                        .string("corpId").required().end()
                        .secret("secret").required().end()
                        .end())
                .provide(
                        MESSAGE_SENDER,
                        "wecom",
                        Tags.of("channel", "wecom"),
                        ConfigDefinition.builder()
                                .string("agentId").required().end()
                                .end(),
                        WeComSender::new)
                .require(OTHER_CAPABILITY, true)
                .build();
    }

    @Override
    public void initialize(PluginContext context) {
        // 读取插件级配置：context.config()
    }

    @Override
    public void start() {
        // 插件级启动逻辑（Provider 已 initialize 之后）
    }

    @Override
    public void stop() {
        // 插件级停止逻辑（资源作用域关闭之前）
    }
}
```

## 3. Builder 常用 API

| 方法 | 说明 |
|------|------|
| `builder(pluginId)` | 必填；反向域名 |
| `name` / `description` | 展示文案（`I18nObject` 或单语言） |
| `version` | 语义版本字符串 |
| `tags(Tags)` | 插件级路由标签，与 Provider 标签合并 |
| `config(ConfigDefinition)` | 插件共享配置 schema |
| `provide(type, factory)` | 最简 Capability 声明 |
| `provide(type, providerId, tags, config, factory)` | 完整 Capability 声明 |
| `require(type, required)` | Capability 依赖 |
| `require(type, required, tags)` | 带标签约束的依赖 |
| `contribute(PluginContribution)` | 添加 `console@1` 等静态贡献声明 |

`provide` 的 `factory` 类型为 `CapabilityProviderFactory`，常用方法引用 `WeComSender::new`（要求 public 无参构造）。

## 4. 声明 `console@1` 页面与菜单

Java SPI 插件可以直接构造 `ConsolePluginContribution`，把控制台模块、页面树和菜单树挂到
`PluginDefinition.Builder.contribute(...)`：

```java
import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;

public final class ConsolePlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("com.example.sales-console")
                .name("Sales Console")
                .version("1.0.0")
                .contribute(new ConsolePluginContribution(List.of(
                        new ConsoleModuleDeclaration(
                                "sales",
                                I18nObject.of("zh-CN", "销售"),
                                I18nObject.of("zh-CN", "销售管理"),
                                List.of(new UiSpecPageDeclaration(
                                        "order-list",
                                        "/sales/orders",
                                        List.of(new UiSpecPageDeclaration(
                                                "order-detail",
                                                "/sales/orders/{orderId}",
                                                List.of())))),
                                List.of(MenuDeclaration.directory(
                                        "sales-root",
                                        I18nObject.of("zh-CN", "销售"),
                                        "shopping-cart",
                                        10,
                                        List.of(MenuDeclaration.page(
                                                "order-list",
                                                I18nObject.of("zh-CN", "订单列表"),
                                                "list",
                                                10,
                                                "order-list")))))))
                .build();
    }
}
```

声明要点：

- `ConsolePluginContribution` 的 `modules` 不能为空。
- `ConsoleModuleDeclaration.pages` 必填，表达页面树；`menuTree` 可选，表达静态导航。
- `UiSpecPageDeclaration.pageKey` 必须和 `ui-spec/<moduleKey>/<pageKey>.yaml` 中的
  `pageInfo.pageId` 对齐。
- `MenuDeclaration` 只能二选一：目录节点使用 `children`，页面入口使用 `pageKey`。
- 带必填路径变量的页面，例如 `/sales/orders/{orderId}`，可以是合法 PAGE，但不能直接做静态菜单入口。

## 5. 实现 CapabilityProvider

```java
public final class WeComSender implements MessageSender {

    @Override
    public void initialize(CapabilityProviderContext context) {
        String corpId = context.config().require("corpId");
        String agentId = context.providerConfig().require("agentId");
        try (SecretValue secret = context.config().requireSecret("secret")) {
            secret.use(chars -> { /* 使用密文 */ return null; });
        }
    }

    @Override
    public void send(String target, String body) {
        // ...
    }

    @Override
    public void destroy() {
        // 释放连接等
    }
}
```

## 6. 约束

- `definition()` 必须无副作用、可重复调用；发现阶段只会调用一次。
- `pluginId`、`providerId` 格式见 [04-yaml-plugin.md](04-yaml-plugin.md) 标识符一节（与 YAML 相同规则）。
- 同一插件内 `providerId` 不能重复。
- V1 不支持在 Java 定义里写 `exposures` 或远程 bind。
- Java `console@1` 声明中的页面与菜单规则，与 YAML `contributions` 完全一致。
- 页面树负责表达领域归属；菜单树负责表达导航结构，二者不要互相代替。

## 7. 本地验证

```java
ClassLoader cl = ...; // 含插件 JAR
var report = new ClasspathPluginDiscovery(cl, decoders).discoverReport();
assertThat(report.rejectedDefinitions()).isEmpty();
```

参考测试：`ClasspathPluginDiscoveryTest`、`ManagedPluginTest`、`PluginConsoleAssemblyTest`。
