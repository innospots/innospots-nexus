# innospots-nexus-spring-service

## 模块简介

Nexus 服务框架的 **Spring Boot adapter**：自动装配 runtime、HTTP 错误映射、治理/观测、WebSocket 桥接、流式与下载写回。  
业务应用引入本 artifact + `spring-boot-starter-web`（或 webflux）即可。

## 何时使用

| 应用类型 | 依赖 |
|---|---|
| Spring MVC REST | 本模块 + `spring-boot-starter-web` |
| Spring WebFlux | 本模块 + `spring-boot-starter-webflux` |
| WebSocket | 本模块 + `spring-boot-starter-websocket`（MVC）或 WebFlux WS |
| 纯库/Domain | **仅** `service-contract`，不要依赖本模块 |

## 包结构

```text
com.innospots.nexus.spring.service
├── bootstrap          # @EnableNexusService*（应用显式引入）
├── core               # ServiceCoreConfiguration、ServiceProperties
├── http
│   ├── config         # Servlet / WebFlux 装配入口
│   ├── mvc            # Filter、异常 advice、请求生命周期
│   ├── webflux        # WebFilter、异常 handler
│   ├── invocation     # ServiceInvocationBridge、GovernedInvocationExecutor
│   └── governance     # GovernedInvocationAspect、DeadlineOperationTimeoutArmer
├── stream
│   ├── config         # StreamManager、模块装配
│   ├── mvc / webflux  # StreamSession 写回
│   └── codec          # SSE 帧编码
├── transfer
│   ├── config
│   ├── mvc / webflux  # DownloadResource 写回
├── websocket
│   ├── config         # Registry Bean
│   └── bridge         # SpringWebSocketEndpointBridge 等
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-spring-service</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

版本由 BOM 管理。WebFlux 项目改 webflux starter。

## 按需启用（`@EnableNexusService*`）

本模块**无** Boot 自动配置清单；须在应用主类或 `@Configuration` 上**显式**标注。注解位于 `com.innospots.nexus.spring.service.bootstrap`。

| 注解 | 装配内容 | 典型 Maven starter |
|---|---|---|
| `@EnableNexusServiceHttp` | 运行时、观测/治理、Servlet Filter 或 WebFlux Filter、错误映射 | `spring-boot-starter-web` 或 `webflux` |
| `@EnableNexusServiceStream` | `StreamManager`、`StreamSession` SSE 写回 | 同上（须先启用 Http） |
| `@EnableNexusServiceTransfer` | `DownloadResource` 写回 | 同上（须先启用 Http） |
| `@EnableNexusServiceWebSocket` | `LocalWebSocketRegistry` 等 | + `spring-boot-starter-websocket`（MVC） |
| `@EnableNexusService` | 以上四项组合 | 全量 adapter-test 夹具 |

```java
@SpringBootApplication
@EnableNexusServiceHttp
@EnableNexusServiceStream
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

仅 REST、不需要流/下载/WS：

```java
@SpringBootApplication
@EnableNexusServiceHttp
public class ApiApplication { ... }
```

全能力（与 adapter 测试一致）：

```java
@SpringBootApplication
@EnableNexusService
public class FullApplication { ... }
```

`service.enabled: false` 时上述装配均不生效。

## 快速接入

### 场景 1：最小 Spring Boot 应用

```yaml
# application.yml
service:
  enabled: true
  name: my-app
  response-profile: LEGACY
```

```java
@SpringBootApplication
@EnableNexusServiceHttp
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 场景 2：带权限 / 治理的 REST（推荐注解 AOP）

默认开启 **`GovernedInvocationAspect`**：在 `@RestController` 方法上声明契约注解即可进入 `InvocationEngine`，**无需** `ServiceInvocationBridge`。

完整说明见 [docs/governance-annotation-advice.md](docs/governance-annotation-advice.md)。

```java
@RestController
public class OrderResource {

    @GetMapping("/orders/{id}")
    @ServiceOperation("order.read")
    @RequiresPermission("order.read")
    public OrderVo get(@PathVariable String id) {
        return service.find(id);
    }

    @GetMapping("/promo/claim")
    @ServiceOperation("promo.claim")
    @RateLimited("promo.claim")
    public Map<String, String> claim() {
        return Map.of("status", "ok");
    }
}
```

关闭注解 Advice：`service.governance.annotation-advice-enabled=false`，此时须回退为 `ServiceInvocationBridge.invoke(...)`。

注册 SPI：

```java
@Bean
PermissionProvider permissionProvider() {
    return new MyPermissionProvider();
}
```

治理注解默认由 AOP 织入；详见 [governance-annotation-advice.md](docs/governance-annotation-advice.md) 与 [governance README](../../innospots-nexus-service/innospots-nexus-service-governance/README.md)。

### 场景 3：经 InvocationEngine 执行（复杂策略）

```java
@RestController
public class SecureResource {
    private final ServiceInvocationBridge bridge;

    @GetMapping("/secure")
    @RequiresPermission("demo.secure")
    public Map<String, String> secure() throws NoSuchMethodException {
        Method m = getClass().getMethod("secure");
        return bridge.invoke(this, m, "demo.secure", () -> Map.of("status", "ok"));
    }
}
```

## WebSocket

完整说明见 [`service-websocket` README](../../innospots-nexus-service/innospots-nexus-service-websocket/README.md)（打开/关闭、刷新行为、配置表、客户端时序）。

### 接入步骤

1. 实现中立 Handler（见 `service-websocket` README）。
2. 注册 codec bean + bridge bean。
3. 配置 WS 路径：

```java
@Configuration
@EnableWebSocket
public class WsConfig implements WebSocketConfigurer {
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(springWebSocketEndpointBridge, "/ws/chat")
                .setAllowedOrigins("*");
    }
}
```

WebFlux 使用 `SimpleUrlHandlerMapping` 映射 `SpringReactiveWebSocketEndpointBridge`。  
完整示例见本模块 `src/test/.../websocket/` 与 `AdapterSampleWebSocketHandler`。

### 刷新 / 重连

- 浏览器**刷新或关闭 tab** → 旧 WebSocket **立即断开**，`onClose` 触发，registry 移除 `connectionId`。
- 新页面须 **重新握手**；**不会**在旧连接上继续推送。
- 若需「断线恢复」，业务在 `onOpen` 重发 snapshot 或让客户端带恢复 token；框架不自动重放未 ack 消息。

### 关闭方式

| 方式 | 效果 |
|---|---|
| 客户端 `ws.close()` | 正常关闭，服务端 `onClose` |
| 服务端 `session.close(...)` | 按关闭码断开（1000/1008/1011 等） |
| `service.enabled: false` / 进程 shutdown | 批量关闭，码 `1001` |

---

## 流式响应

完整说明见 [`service-stream` README](../../innospots-nexus-service/innospots-nexus-service-stream/README.md)（SSE 时序、断连 FAQ、配置表）。

### 推荐写法

返回 `StreamSession<T>`；adapter 的 return handler 优先于 JSON 序列化：

```java
@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public StreamSession<Event> events() {
    StreamSession<Event> session = streamManager.open(Event.class);
    runAsyncProducer(session);
    return session;
}
```

### 取消协作（WebFlux 夹具）

```java
CancellationToken cancellation = contextAccessor.requireCurrent().cancellation();
return Flux.interval(Duration.ofMillis(200))
        .takeWhile(i -> !cancellation.isCancelled())
        .map(i -> ServerSentEvent.builder("chunk-" + i).build())
        .doOnCancel(() -> ServiceWebFilter.cancelIfClientDisconnected(exchange));
```

MVC 夹具在 `SseEmitter` 超时/完成回调中调用 `ServiceServletFilter.cancelIfClientDisconnected(request)`。

### 刷新 / 断连

| 行为 | 说明 |
|---|---|
| 刷新页面 | 旧 SSE 连接关闭 → `CLIENT_DISCONNECTED` → 业务应 `isCancelled()` 停止 |
| 新页面 | **新 GET**，须重新 `open` 会话；**不**自动续接旧 sessionId 的流 |
| 断连探测 | Servlet 常延迟到**下一次写或 heartbeat**；WebFlux 在 subscription cancel 时较快 |
| 离线客户端 | 服务端**不保证**送达 `stream.cancelled` |

### 启用 / 关闭

- `service.enabled: false` 关闭整个 adapter（含流上下文）。
- 设计契约：`service.stream.enabled: false`。
- 自定义 `StreamConfig` / `DefaultStreamManager` bean 覆盖默认容量与超时。

## 文件下载

```java
@GetMapping("/files/{id}")
public DownloadResource download(@PathVariable String id) {
    return buildDownloadResource(id);
}
```

`ServiceDownloadReturnValueHandler`（MVC）或 WebFlux `HandlerResultHandler` 写回二进制。

## 配置项

| 属性 | 默认 | 说明 |
|---|---|---|
| `service.enabled` | `true` | 关闭整个 adapter |
| `service.name` | `nexus-service` | 服务名（日志/指标标签） |
| `service.response-profile` | `LEGACY` | HTTP 错误 JSON 形态 |

## 观测

完整说明见 [`service-observability` README](../../innospots-nexus-service/innospots-nexus-service-observability/README.md)。

| 能力 | 框架无关？ | Spring 侧 |
|---|---|---|
| Access log / MDC / requestId | 语义相同 | `ServiceServletFilter` / `ServiceWebFilter` |
| `TraceProvider` / `ServiceMeters` | SPI 相同 | `@Bean` 覆盖 `ServiceCoreConfiguration` |
| 业务 SLF4J / `@Traced` | 写法相同 | 与 Quarkus 无差异 |

默认已有：响应头 `X-Request-Id`、Logger `service.access`、SLF4J MDC 字段。  
替换 OTel：`@Bean TraceProvider` → `OpenTelemetryTraceProvider`。  
`MicrometerMetricsProvider` / `ServiceMeters` 须自行注册 Bean（adapter 默认 NoOp）。

## 治理

完整说明见 [`service-governance` README](../../innospots-nexus-service/innospots-nexus-service-governance/README.md)。

| 能力 | 框架无关？ | Spring 侧 |
|---|---|---|
| `@RateLimited` 等注解 | 相同 | **须** `ServiceInvocationBridge.invoke` |
| 拦截器实现 | 相同类 | `ServiceCoreConfiguration` 注册 |
| 429 / 504 | 相同 `HttpErrorMapper` | 自动 |

当前默认：`RateLimitInterceptor` + `TimeoutInterceptor`。舱壁/熔断类已实现但未默认加入链。  
覆盖策略：替换 `serviceGovernanceConfig` Bean。

## 测试

```bash
mvn -pl innospots-nexus-spring/innospots-nexus-spring-service -am test \
  -Dtest=AdapterScenarioMvcTest,AdapterScenarioWebFluxTest
```

依赖 `innospots-nexus-service-adapter-test`（test scope）。

## 注意事项

- 业务模块禁止依赖 `org.springframework.web.*`；仅 Web Entry 模块依赖本 artifact。
- `@RestController` 返回 `DownloadResource` 时，download handler 必须排在 JSON handler **之前**（本模块已 `addFirst`）。
- 可选依赖：web / webflux / websocket 为 optional，按应用选型引入 starter。

## 开发接入手册（分主题）

逐步说明 Spring 下标准接口的配置与接入，每主题独立成文：

- [手册索引](docs/README.md)
- [HTTP API](docs/http-api-integration.md)
- [流式 Stream](docs/stream-integration.md)
- [WebSocket](docs/websocket-integration.md)
- [文件上传下载](docs/file-transfer-integration.md)
- [熔断与限流治理](docs/governance-integration.md)
- [观测与监控采集](docs/observability-integration.md)

## 相关文档

- [Adapter 设计](../../innospots-nexus-service/docs/service-adapter-design.md)
- [开发者体验](../../innospots-nexus-service/docs/service-developer-experience-design.md)
- [中立模块索引](../../innospots-nexus-service/README.md)
