# Capability 使用说明

## 1. 概念

| 概念 | 类 | 说明 |
|------|-----|------|
| Capability 类型 | `CapabilityType<T>` | 逻辑名 + 主版本 + Java API |
| Provider 身份 | `ProviderRef` | `(pluginId, providerId)` |
| 路由标签 | `Tags` | 插件级 + Provider 级合并后用于匹配 |
| 注册记录 | `CapabilityRegistration` | 启动成功后写入 `CapabilityRegistry` |
| 查询入口 | `CapabilityManager` | `require` / `find` / `findAll` |

插件**声明**能力与类型；宿主在发现阶段自动登记类型并**路由**到具体 Provider。

## 2. 类型登记（自动）

发现阶段从插件声明自动构建类型表，无需宿主手动 `register`：

- **Java 插件**：`PluginDefinition` 中 `provide` 携带的 `CapabilityType`
- **YAML 插件**：`capabilities[].api` 字段

```java
ClasspathPluginDiscovery discovery = new ClasspathPluginDiscovery(classLoader, contributionDecoders);
```

## 3. 宿主侧：默认路由（可选）

```java
PluginRuntimeConfig config = new PluginRuntimeConfig(
        requiredPluginIds,
        disabledPluginIds,
        hostConfig,
        runtimeVariables,
        Map.of(
                MESSAGE_SENDER.key(), Tags.of("channel", "wecom")
        ),
        classLoader);
```

调用方未传 Tags 时，使用上述默认路由；仍须唯一匹配，否则 `CAPABILITY_AMBIGUOUS`。

## 4. 宿主业务代码：查询 Provider

```java
@Service
class OrderNotificationService {

    private final CapabilityManager capabilities;

    OrderNotificationService(PluginManager pluginManager) {
        this.capabilities = pluginManager.capabilities();
    }

    void notify(String userId, String body) {
        MessageSender sender = capabilities.require(
                "message.sender",
                1,
                Tags.of("channel", "wecom"));
        sender.send(userId, body);
    }

    Optional<MessageSender> findOptional() {
        return capabilities.find("message.sender", 1, Tags.empty());
    }
}
```

| 方法 | 行为 |
|------|------|
| `require(name, major, tags)` | 按逻辑名解析类型；无匹配或歧义 → 异常 |
| `find(name, major, tags)` | 无匹配 → `Optional.empty()` |
| `require(type, tags)` / `find(type, tags)` | 编译期类型安全重载 |
| `findAll(name, major)` / `findAll(type)` | 返回该类型全部 **可见** Provider |

**路由规则**（`CapabilityRouter`）：

1. 请求 Tags 是 Provider Tags 的**子集**则匹配；
2. 请求 Tags 为空 → 用默认路由；仍为空且仅一个 Provider → 返回该 Provider；
3. 0 个 → 未找到；多个 → 歧义。

## 5. 插件内：调用其他 Capability

在 `initialize` / `start` 或 Provider 方法中：

```java
@Override
public void initialize(PluginContext context) {
    MessageSender sender = context.capabilities()
            .find(MESSAGE_SENDER, Tags.of("channel", "wecom"))
            .orElseThrow();
}
```

只能看到已 `ACTIVE` 的 Provider（含依赖插件先启动完成）。

## 6. 声明依赖

```java
.require(MESSAGE_SENDER, true)                    // 必填
.require(MESSAGE_SENDER, false, Tags.of("x","y")) // 可选 + 标签
```

`DependencyResolver` 在 `ManagedPlugin.start()` 前检查；不满足则 `WAITING`，批量 `start()` 多轮推进。

## 7. 停止保护

`DefaultPluginManager.stop(pluginId)` 若会导致其他 **ACTIVE** 插件的必填 Capability 失去最后一个 Provider，则拒绝（`PLUGIN_IN_USE`）。

## 8. 示例：双 Provider 同类型

插件 A 提供 `wecom`，插件 B 提供 `smtp`，均为 `message.sender@1`：

```java
capabilities.require(MESSAGE_SENDER, Tags.of("channel", "wecom"));
capabilities.require(MESSAGE_SENDER, Tags.of("channel", "smtp"));
```

同一 Tags 命中多个 Provider 时抛 `CAPABILITY_AMBIGUOUS`——必须靠标签或默认路由消歧。
