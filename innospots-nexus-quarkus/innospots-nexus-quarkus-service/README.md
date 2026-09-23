# innospots-nexus-quarkus-service

## 模块简介

Nexus 服务框架的 **Quarkus adapter**：CDI 装配 runtime、JAX-RS 过滤器、异常映射、WebSockets Next 桥接与下载写回。  
业务应用引入本 artifact + `quarkus-rest` +（可选）`quarkus-websockets-next`。

## 何时使用

| 应用类型 | 依赖 |
|---|---|
| Quarkus REST API | 本模块 + `quarkus-rest` |
| WebSocket | + `quarkus-websockets-next` |
| 业务 Domain 模块 | **仅** `service-contract` |

## 包结构

```text
com.innospots.nexus.quarkus.service
├── config             # ServiceRuntimeHolder、ServiceRuntimeBeans、QuarkusWebSocketBeans
├── rest               # ServiceRequestFilter、ServiceExceptionMapper、QuarkusDownloadBodyWriter
├── invocation         # ServiceInvocationBridge
├── websocket          # QuarkusWebSocketEndpointBridge、QuarkusWebSocketSessionAdapter
└── governance         # DeadlineOperationTimeoutArmer
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-quarkus-service</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest</artifactId>
</dependency>
```

WebSocket 再加 `quarkus-websockets-next`。版本由 Quarkus BOM 管理。

## 快速接入

### 场景 1：最小 Quarkus 应用

```properties
# application.properties
quarkus.http.port=8080
```

```java
@ApplicationScoped
@Path("/hello")
public class HelloResource {
    @GET
    public String hello() {
        return "ok";
    }
}
```

引入本模块后，`ServiceRequestFilter` 自动建立 `ServiceContext`、写 access log、回写 requestId。

### 场景 2：JAX-RS + 权限（须经 InvocationBridge）

```java
@Path("/orders")
public class OrderResource {
    @Inject ServiceInvocationBridge bridge;

    @GET
    @Path("/{id}")
    @RequiresPermission("order.read")
    public OrderVo get(@PathParam("id") String id) throws NoSuchMethodException {
        Method method = OrderResource.class.getMethod("get", String.class);
        return bridge.invoke(this, method, "order.read", () -> service.find(id));
    }
}
```

```java
@ApplicationScoped
public class MyPermissionProvider implements PermissionProvider {
    // authorize(...)
}
```

CDI 会自动注入到 `ServiceRuntimeHolder`。治理注解同样须 `bridge.invoke(...)`。

### 场景 3：InvocationBridge（与 Spring 相同模式）

```java
@GET
@Path("/secure")
@RequiresPermission("demo.secure")
public Map<String, String> secure() throws NoSuchMethodException {
    Method m = getClass().getMethod("secure");
    return bridge.invoke(this, m, "demo.secure", () -> Map.of("status", "ok"));
}
```

## WebSocket

完整说明见 [`service-websocket` README](../../innospots-nexus-service/innospots-nexus-service-websocket/README.md)。

### 接入步骤

1. 实现 `WebSocketHandler` + 注册 `JsonWebSocketCodec`（CDI `@Produces`）。
2. 薄端点委托 bridge：

```java
@WebSocket(path = "/ws/chat")
public class ChatEndpoint {

    @Inject QuarkusWebSocketEndpointBridge bridge;

    @OnOpen
    void onOpen(WebSocketConnection connection) {
        bridge.onOpen(connection);
    }

    @OnTextMessage
    void onMessage(String payload, WebSocketConnection connection) {
        bridge.onTextMessage(payload, connection);
    }

    @OnClose
    void onClose(WebSocketConnection connection) {
        bridge.onClose(connection);
    }
}
```

参考：`src/test/.../fixture/SampleWebSocketEndpoint.java`。

### 刷新 / 重连

- 页面刷新 → 旧连接关闭，须重新握手；**不会**在原 `connectionId` 上继续收消息。
- 业务在 `onOpen` 自行恢复状态；多实例部署需 sticky 或应用层广播。

---

## 流式响应

完整说明见 [`service-stream` README](../../innospots-nexus-service/innospots-nexus-service-stream/README.md)。

Quarkus REST 返回 `StreamSession<T>` 或 `Multi`/SSE 类型时，由 adapter 写回（与 Spring 相同的中立 `StreamManager` 语义）。

### 刷新 / 断连

- SSE：刷新 = 新 HTTP 请求，旧流取消（`CLIENT_DISCONNECTED`），**不**自动续流。
- 断连探测依赖下一次写或 heartbeat；业务循环须检查 `session.isCancelled()`。

---

## 文件下载

```java
@GET
@Path("/files/{id}")
public DownloadResource download(@PathParam("id") String id) {
    return buildMemoryDownload(id);
}
```

`QuarkusDownloadBodyWriter`（JAX-RS `MessageBodyWriter`）自动编码响应。

## 观测

完整说明见 [`service-observability` README](../../innospots-nexus-service/innospots-nexus-service-observability/README.md)。

| 能力 | 框架无关？ | Quarkus 侧 |
|---|---|---|
| Access log / MDC / requestId | 语义相同 | `ServiceRequestFilter` |
| SPI 替换 | 相同 | `@Produces @DefaultBean` |
| 业务 API | 相同 | 与 Spring 无差异 |

默认：`NoOpTraceProvider`；OTel 用 `@Produces TraceProvider`。Micrometer 须自行装配。

## 治理

完整说明见 [`service-governance` README](../../innospots-nexus-service/innospots-nexus-service-governance/README.md)。

| 能力 | 框架无关？ | Quarkus 侧 |
|---|---|---|
| 注解 + invoke 模式 | 相同 | `ServiceInvocationBridge` |
| 拦截器 | 相同类 | `ServiceRuntimeHolder.@PostConstruct` |
| 默认 GovernanceConfig | 相同语义 | 内联在 Holder 常量（生产应外置） |

当前默认：限流 + 超时。舱壁/熔断须手动 `addInterceptor`。

## SPI Bean

| Bean | 默认 |
|---|---|
| `ServiceRuntimeHolder` | 启动 `ServiceRuntime` + 治理拦截器 |
| `LocalWebSocketRegistry` | 进程内 WS 注册表 |
| `TraceProvider` | `NoOpTraceProvider` |
| `PermissionProvider` | 可选，无则匿名 |

覆盖示例：

```java
@Produces
@Singleton
@DefaultBean
TraceProvider traceProvider(OpenTelemetry otel) {
    return new OpenTelemetryTraceProvider(otel);
}
```

## 测试

```bash
mvn -pl innospots-nexus-quarkus/innospots-nexus-quarkus-service -am test \
  -Dtest=AdapterScenarioQuarkusTest
```

## 注意事项

- 业务模块禁止 import `io.quarkus.*` / `jakarta.ws.rs.*`（除 Web Entry）。
- 完整构建期注解拦截（deployment processor）为后续增强；当前以 Filter + CDI 为主。
- `QuarkusWebSocketEndpointBridge` 注入 `LocalWebSocketRegistry`；勿重复 `@Produces` 第二个 `WebSocketRegistry` bean。

## 相关文档

- [Adapter 设计](../../innospots-nexus-service/docs/service-adapter-design.md)
- [WebSocket 中立库 README](../../innospots-nexus-service/innospots-nexus-service-websocket/README.md)
- [Spring adapter 对照](../innospots-nexus-spring-service/README.md)
