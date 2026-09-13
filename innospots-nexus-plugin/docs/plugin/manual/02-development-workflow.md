# 插件二次开发流程

## 1. 先决条件

1. 插件 JAR 在应用 **Application ClassLoader** 可见（V1 无独立插件 ClassLoader）。
2. 若声明 `console@1`，宿主需注册对应 `PluginContributionDecoder`、`Handler`、`Snapshotter`（Console 模块提供，见 [09-host-assembly.md](09-host-assembly.md)）。

## 2. 三步总览

```
① 定义 Capability API 接口并实现 Provider
        ↓
② 选择声明方式（Java SPI 或 plugin.yaml）
        ↓
③ 配置运行时参数（见 [08-configuration.md](08-configuration.md)）
```

**无需**在宿主启动代码中手动注册 `CapabilityTypeRegistry`；类型由插件声明在发现阶段自动登记。

宿主需把 `application.yml` / Nacos / 环境变量等合并为 `PluginRuntimeConfig.hostConfig()`；Core 不直接读配置文件。

## 3. 步骤说明

### ① 定义契约与实现

在共享 API 模块或插件模块中定义接口并实现：

```java
public interface MessageSender extends CapabilityProvider {
    void send(String target, String body);
}

public final class WeComMessageSender implements MessageSender {
    @Override
    public void send(String target, String body) { /* ... */ }
}
```

### ② 选择声明方式

| 方式 | 适用 | 文档 |
|------|------|------|
| Java SPI | 逻辑复杂、需编程组装定义 | [03-java-plugin.md](03-java-plugin.md) |
| YAML | 声明为主、实现类显式绑定 | [04-yaml-plugin.md](04-yaml-plugin.md) |

同一 `pluginId` 只能有一种来源，不能 Java 与 YAML 各声明一份。

**Java 插件**在 `provide` 中带上 `CapabilityType`：

```java
private static final CapabilityType<MessageSender> MESSAGE_SENDER =
        CapabilityType.of("message.sender", 1, MessageSender.class);

PluginDefinition.builder("com.example.message-wecom")
        .provide(MESSAGE_SENDER, "wecom", Tags.of("channel", "wecom"), WeComMessageSender::new)
        .build();
```

**YAML 插件**在 capability 块声明 `type`、`majorVersion`、`api`、`bind`：

```yaml
capabilities:
  - type: message.sender
    majorVersion: 1
    providerId: wecom
    api: com.example.contract.MessageSender
    bind:
      kind: java
      class: com.example.message.WeComMessageSender
```

### ③ 配置（可选）

- 在 `PluginDefinition` / YAML 中声明 `config` schema。
- 在应用配置中写入运行时值（见 [08-configuration.md](08-configuration.md)）。

## 4. 依赖其他插件的 Capability

在定义中声明 `require`，**不**写死对方 `pluginId`：

```java
PluginDefinition.builder("com.example.consumer")
    .require("message.sender", 1, true)
    .build();
```

```yaml
requirements:
  - type: message.sender
    majorVersion: 1
    required: true
```

运行时由 `DependencyResolver` 根据**已 ACTIVE** 的 Provider 判断是否可启动。

## 5. 验收清单

- [ ] `pluginId` 符合反向域名格式
- [ ] 插件内 `providerId` 全局唯一
- [ ] YAML capability 已声明 `api`；Java `provide` 已声明 `CapabilityType`
- [ ] 配置键与 schema 一致，Secret 项无默认值
- [ ] `ClasspathPluginDiscovery.discoverReport()` 中插件出现在 `validCatalog`
- [ ] 安装后 `PluginManager` 中状态为 `ACTIVE`，`CapabilityManager.find` 可解析
