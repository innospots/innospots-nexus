# WebSocket 接入手册（Spring）

说明在 Spring Boot 中通过 **标准 `WebSocketHandler`** 与 **Spring 桥接器** 接入 Nexus WebSocket 运行时（注册表、JSON codec、连接生命周期、消息级权限）。

---

## 1. 架构关系

```text
浏览器 WebSocket
    → Spring WebSocketHandler（Bridge）
    → SpringWebSocketSessionAdapter / SpringReactiveWebSocketSessionAdapter
    → WebSocketHandler（业务，中立接口）
    → LocalWebSocketRegistry / WebSocketService
```

业务 **只实现** `com.innospots.nexus.service.websocket.handler.WebSocketHandler`（或继承 `AbstractWebSocketHandler`），**不要**在业务模块依赖 `org.springframework.web.socket.*`。

---

## 2. 接入步骤

### 步骤 1：Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-spring-service</artifactId>
</dependency>
<!-- MVC WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

WebFlux 使用 `spring-boot-starter-webflux`（含 reactive WebSocket）。

### 步骤 2：确认运行时 Bean（自动装配）

`ServiceWebSocketModuleConfiguration` 在应用标注 `@EnableNexusServiceWebSocket` 时注册（通常与 `@EnableNexusServiceHttp` 同用）：

| Bean | 说明 |
|------|------|
| `WebSocketRuntimeConfig` | 连接数、缓冲、ping、idle 等 |
| `LocalWebSocketRegistry` | 连接注册与 `WebSocketService` |
| `WebSocketService` | 按 `connectionId` / `sessionId` 推送 |
| `WebSocketInboundMessageHandler` | 入站治理派发（限流/舱壁/熔断/超时/权限） |

可通过 `@Bean` **覆盖**（带 `@ConditionalOnMissingBean`）：

```java
@Bean
WebSocketRuntimeConfig serviceWebSocketRuntimeConfig() {
    WebSocketRuntimeConfig defaults = WebSocketRuntimeConfig.defaults();
    return new WebSocketRuntimeConfig(
            5_000,
            defaults.maxConnectionsPerUser(),
            defaults.maxMessageBytes(),
            defaults.inboundBufferSize(),
            defaults.outboundBufferSize(),
            defaults.inboundBufferBytes(),
            defaults.outboundBufferBytes(),
            defaults.globalInboundBufferBytes(),
            defaults.globalOutboundBufferBytes(),
            defaults.overflow(),
            defaults.pingInterval(),
            defaults.pongTimeout(),
            defaults.idleTimeout(),
            defaults.messageTimeout(),
            defaults.inboundGovernanceEnabled());
}
```

未自定义时使用 `WebSocketRuntimeConfig.defaults()`。`inboundGovernanceEnabled` 为 `false` 时关闭限流/舱壁/熔断/超时，**仍**对声明了 `permissionKeys` 的消息走 `InvocationEngine` 授权。

### 入站服务治理

在 `MessageDescriptor` 上声明治理键（与 `GovernanceConfig` 表对应），Spring 桥接器经 `WebSocketInboundMessageHandler` 自动 `invokeAsync` 派发。详见 [websocket-governance.md](../../../innospots-nexus-service/innospots-nexus-service-governance/docs/websocket-governance.md)。

### 步骤 3：实现业务 Handler

```java
public final class ChatWebSocketHandler extends AbstractWebSocketHandler<ChatInbound, ChatOutbound> {

    @Override
    public CompletionStage<Void> onOpen(WebSocketSession<ChatInbound, ChatOutbound> session) {
        return session.send(WebSocketMessage.of("chat.ready", new ChatOutbound("ok")));
    }

    @Override
    public CompletionStage<Void> onMessage(
            WebSocketSession<ChatInbound, ChatOutbound> session,
            WebSocketMessage<ChatInbound> message) {
        // 处理 chat.send 等 type
        return CompletableFuture.completedFuture(null);
    }
}
```

### 步骤 4：注册 JSON Codec

为每种 `type` 注册 `MessageDescriptor`（含可选 permission）：

```java
@Bean
JsonWebSocketCodec chatWebSocketCodec() {
    return JsonWebSocketCodec.builder()
            .maxMessageBytes(64 * 1024)
            .descriptors(Map.of(
                    "chat.send",
                    new MessageDescriptor(
                            "chat.send",
                            ChatInbound.class,
                            ChatOutbound.class,
                            Set.of("chat.send"),
                            null,
                            ExecutionMode.BLOCKING,
                            FrameType.TEXT,
                            "chat-send-rate",
                            null,
                            null,
                            null)))
            .build();
}
```

测试夹具可参考 `AdapterWebSocketFixtures.codec()`。

### 步骤 5：注册 Spring Bridge Bean

**Servlet（MVC）** — 参考 `AdapterWebSocketTestBeans`：

```java
@Bean
SpringWebSocketEndpointBridge chatWebSocketEndpointBridge(
        ChatWebSocketHandler chatWebSocketHandler,
        JsonWebSocketCodec chatWebSocketCodec,
        LocalWebSocketRegistry serviceWebSocketRegistry,
        ServiceTransportSupport serviceTransportSupport,
        ThreadBoundServiceContext serviceThreadBoundServiceContext,
        WebSocketInboundMessageHandler serviceWebSocketInboundMessageHandler) {
    return new SpringWebSocketEndpointBridge(
            chatWebSocketHandler,
            chatWebSocketCodec,
            serviceWebSocketRegistry,
            serviceTransportSupport,
            serviceThreadBoundServiceContext,
            serviceWebSocketInboundMessageHandler);
}
```

**WebFlux** — 参考 `ReactiveWebSocketConfiguration`，使用 `SpringReactiveWebSocketEndpointBridge` + `SimpleUrlHandlerMapping` + `WebSocketHandlerAdapter`。

### 步骤 6：映射 URL

**MVC**：

```java
@Configuration
@EnableWebSocket
public class WebSocketEndpointConfiguration implements WebSocketConfigurer {

    private final SpringWebSocketEndpointBridge bridge;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(bridge, "/ws/chat")
                .setAllowedOrigins("*");   // 生产环境请收紧
    }
}
```

**WebFlux**：

```java
@Bean
HandlerMapping chatWebSocketHandlerMapping(SpringReactiveWebSocketEndpointBridge bridge) {
    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(Map.of("/ws/chat", bridge));
    mapping.setOrder(-1);
    return mapping;
}

@Bean
WebSocketHandlerAdapter webSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
}
```

### 步骤 7：服务端主动推送

注入 `WebSocketService`：

```java
webSocketService.send(connectionId, message);
webSocketService.sendToSession(sessionId, message);
```

`sessionId` 由应用的 `SessionResolver` 解析；**不可**信任客户端随意声明的 session 归属。

---

## 3. 配置项（`WebSocketRuntimeConfig`）

| 字段 | 默认 | 说明 |
|------|------|------|
| `maxConnections` | `10000` | 进程最大物理连接 |
| `maxConnectionsPerUser` | `64` | 每 principal 连接上限 |
| `maxMessageBytes` | `1 MiB` | 单消息上限 |
| `pingInterval` | `30s` | 协议 ping |
| `idleTimeout` | `30m` | 无业务消息 idle 关闭 |
| `messageTimeout` | `30s` | 单条 `onMessage` 处理超时 |

YAML 设计前缀：`service.websocket.*`。

全局关闭：`service.enabled: false` 时 adapter 与 WS 上下文一并不可用。

---

## 4. 客户端与刷新行为

| 行为 | 说明 |
|------|------|
| 刷新页面 | 旧连接关闭，`onClose` 触发；须重新握手 |
| `connectionId` | 每次新连接变化 |
| 断线续传 | 框架不自动重放；业务用 snapshot / correlationId |

---

## 5. 验证

- 对照 `SpringWebSocketConfiguration`、`ReactiveWebSocketConfiguration` 与 `adapter-test` WebSocket 场景路径。
- 验证未知 `type` 返回 error envelope（默认不断开）。
- 验证超 `maxMessageBytes` 时关闭码 `1009`。

---

## 6. 二次扩展开发

### 6.1 继承 `AbstractWebSocketHandler`

推荐入口：实现 `onOpen` / `onMessage` / `onClose`，共用异常与日志模板。泛型 `I`/`O` 与 codec 注册类型一致。

### 6.2 覆盖 `WebSocketRuntimeConfig` / Registry

`ServiceWebSocketConfiguration` 中相关 Bean 带 `@ConditionalOnMissingBean`，应用内声明同名类型 Bean 即可替换。

### 6.3 自定义 `SessionResolver` / 消息鉴权

在 `MessageDescriptor` 上声明 `permissionKeys`，并注册 `PermissionProvider`；入站由 `WebSocketInboundMessageHandler` 经 `InvocationEngine` 调用 `AuthorizationInterceptor`。握手后 `ServiceContext` 由 Bridge 内 `ServiceTransportSupport` 建立。

### 6.4 扩展 Bridge（少见）

`SpringWebSocketEndpointBridge` 为 `public` 类，一般 **组合** 而非继承。若需额外握手头解析，可包装 `TextWebSocketHandler` 或在 `afterConnectionEstablished` 前后增加 Filter 层鉴权。

### 6.5 Reactive 与 Servlet 双栈

同一应用通常只选一种 WS 栈；勿在同一端口混用未协调的 Handler 映射。

---

## 7. 相关文档

- [service-websocket README](../../../innospots-nexus-service/innospots-nexus-service-websocket/README.md)
- [治理接入](governance-integration.md)（`defaultWebSocketTimeout`）
- [观测接入](observability-integration.md)（连接级 access / 指标）
