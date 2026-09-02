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
| `contribute(PluginContribution)` | 如 `console@1` |

`provide` 的 `factory` 类型为 `CapabilityProviderFactory`，常用方法引用 `WeComSender::new`（要求 public 无参构造）。

## 4. 实现 CapabilityProvider

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

## 5. 约束

- `definition()` 必须无副作用、可重复调用；发现阶段只会调用一次。
- `pluginId`、`providerId` 格式见 [04-yaml-plugin.md](04-yaml-plugin.md) 标识符一节（与 YAML 相同规则）。
- 同一插件内 `providerId` 不能重复。
- V1 不支持在 Java 定义里写 `exposures` 或远程 bind。

## 6. 本地验证

```java
ClassLoader cl = ...; // 含插件 JAR
var report = new ClasspathPluginDiscovery(cl, decoders).discoverReport();
assertThat(report.rejectedDefinitions()).isEmpty();
```

参考测试：`ClasspathPluginDiscoveryTest`、`ManagedPluginTest`。
