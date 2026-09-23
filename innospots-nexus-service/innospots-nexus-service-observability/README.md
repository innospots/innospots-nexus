# innospots-nexus-service-observability

## 模块简介

可观测性**中立实现库**：访问日志、MDC 桥接、Trace/Metrics SPI 的 NoOp 与 OTel/Micrometer 适配。  
依赖 **contract** 中的 `TraceProvider` / `MetricsProvider` / `ServiceMeters`，**不**依赖 runtime、Spring 或 Quarkus。

---

## 框架无关 vs 框架相关：先看这张表

| 层级 | 是否框架无关 | 内容 | 业务怎么用 |
|---|---|---|---|
| **契约** | 是 | `TraceProvider`、`MetricsProvider`、`ServiceMeters`、`@Traced` | 业务只依赖 `service-contract` |
| **本模块** | 是 | `AccessLogWriter`、`MdcContextBridge`、`OpenTelemetryTraceProvider`、`MicrometerMetricsProvider` | 由 adapter 注册为 Bean，业务一般不直接 new |
| **运行时** | 是 | `InvocationEngine` 在调用链上挂观测（平台指标） | 经 `ServiceInvocationBridge` 进入 |
| **Adapter 装配** | **否** | Filter 写 access log、安装 MDC、覆盖 SPI Bean | Spring `@Bean` / Quarkus `@Produces` |
| **业务代码** | 是（写法相同） | SLF4J 日志、`ServiceMeters.increment(...)` | Spring 与 Quarkus **完全相同** |

结论：

- **观测语义与 SPI 是框架无关的** — 同一套 contract API、同一套 observability 实现类。
- **自动生效的 HTTP 观测**（requestId、MDC、access log）由各自 adapter 的 **Filter** 触发，但行为一致。
- **替换 OTel/Micrometer** 只在 adapter 层换 Bean，业务代码不变。

```mermaid
flowchart TB
    subgraph contract [框架无关 · contract]
        SM[ServiceMeters]
        TP[TraceProvider]
        MP[MetricsProvider]
        TR[@Traced]
    end
    subgraph obs [框架无关 · observability]
        AL[AccessLogWriter]
        MDC[MdcContextBridge]
        OTel[OpenTelemetryTraceProvider]
        MM[MicrometerMetricsProvider]
    end
    subgraph adapter [框架相关 · adapter]
        SF[Servlet / WebFilter / JAX-RS Filter]
        BR[ServiceInvocationBridge]
    end
    SF --> MDC
    SF --> AL
    BR --> IE[InvocationEngine]
    IE --> MP
    IE --> TP
    TR --> TP
    SM --> MM
    business[业务 SLF4J / ServiceMeters] --> SM
```

---

## 能力分层：什么自动、什么要显式

### Level 0 — 默认自动（无需注解、无需 Bridge）

引入 `innospots-nexus-spring-service` 或 `innospots-nexus-quarkus-service` 后，**每个 HTTP 请求**自动具备：

| 能力 | 行为 |
|---|---|
| `X-Request-Id` | Filter 生成或透传，写入响应头 |
| `ServiceContext` | 线程绑定（Servlet/WebFilter/JAX-RS 过滤器内） |
| MDC | `requestId`、`traceId`、`spanId`、`principalId`、`operation` 自动填入 SLF4J MDC |
| Access Log | 请求结束时写 `service.access` 日志（可关） |

**普通 GET 即使没有任何框架注解**，也会有 requestId 与 access log（见 adapter-test `HttpUnaugmentedScenario`）。

业务写法：

```java
@Slf4j
@RestController
public class OrderResource {
    @GetMapping("/orders/{id}")
    public OrderVo get(@PathVariable String id) {
        log.info("fetch order, orderId={}", id);  // 自动带 MDC 字段
        return service.find(id);
    }
}
```

**禁止**：业务里 `MDC.put/remove/clear`；禁止 `@Tracing` / `@Logging` 开关类注解。

### Level 1 — 平台 SPI（adapter 装配，业务不直接碰 SDK）

| SPI | 默认 | 替换方式 |
|---|---|---|
| `TraceProvider` | `NoOpTraceProvider` | 注册 `OpenTelemetryTraceProvider` |
| `MetricsProvider` | `NoOpMetricsProvider` | 注册 `MicrometerMetricsProvider` |

平台会为每次经 `InvocationEngine` 的调用记录 HTTP 指标（设计：`service.http.requests` 等），label 受白名单约束。

### Level 2 — 业务可选

| 能力 | API | 说明 |
|---|---|---|
| 子 Span | `@Traced("validation")` | 仅复杂链路需要；默认请求追踪**不需要**此注解 |
| 业务计数/计时 | `ServiceMeters.increment("order.created")` | 名称须预先注册；tag 须在白名单内 |
| 审计 | `@Audited` + `AuditStorage` SPI | 属 runtime 审计链，见 [runtime README](../innospots-nexus-service-runtime/README.md) |

---

## 如何使用

### 1. Access Log（零代码）

Filter 在请求完成时调用 `AccessLogWriter.write(...)`：

```
requestId=... method=GET route=/api/orders status=200 result=success durationMs=12 principalId=... traceId=...
```

关闭：

```java
// Spring
@Bean
ObservabilityConfig serviceObservabilityConfig() {
    return new ObservabilityConfig(false, Set.of(), Map.of(), Set.of(...));
}

// Quarkus
@Produces @DefaultBean
ObservabilityConfig observabilityConfig() {
    return new ObservabilityConfig(false, ...);
}
```

设计契约 YAML：`service.observability.access-log-enabled: false`（adapter 绑定后等效）。

### 2. MDC 与业务日志

Filter 内：

```java
MdcSnapshot snapshot = mdcContextBridge.install(context);
try {
    chain.doFilter(...);
} finally {
    snapshot.restore();
}
```

业务只用 SLF4J；日志自动关联 requestId/traceId。**不要在异步线程裸跑业务** — 须经 `ContextExecutor` / 受管入口传播上下文。

### 3. 接入 OpenTelemetry Trace

**Spring**（覆盖默认 Bean）：

```java
@Bean
TraceProvider serviceTraceProvider(OpenTelemetry openTelemetry) {
    return new OpenTelemetryTraceProvider(openTelemetry);
}
```

**Quarkus**：

```java
@Produces @Singleton @DefaultBean
TraceProvider traceProvider(OpenTelemetry openTelemetry) {
    return new OpenTelemetryTraceProvider(openTelemetry);
}
```

未安装 OTel SDK 时保持 `NoOpTraceProvider` — **不会**伪造全零 traceId（见 `ObservabilityTraceScenario`）。

可选子 Span（须走 `ServiceInvocationBridge` 或未来 adapter 自动代理）：

```java
@Traced("payment.validate")
public PaymentResult validate(PaymentRequest req) { ... }
```

### 4. 业务指标 ServiceMeters

```java
@Service
public class OrderService {
    private final ServiceMeters meters;

    public OrderService(ServiceMeters meters) {
        this.meters = meters;
    }

    public void publish(Order order) {
        // 名称须已在 ObservabilityConfig.businessMeterNames 注册
        meters.increment("order.publish.success");
        meters.record("order.publish.duration", Duration.ofMillis(elapsed));
    }
}
```

**禁止**业务直接使用 `MeterRegistry`、`Counter.builder()` 或 OTel SDK。

注册指标名（Spring 示例）：

```java
@Bean
ObservabilityConfig serviceObservabilityConfig() {
    return new ObservabilityConfig(
            true,
            Set.of("order.publish.success", "order.publish.duration"),
            Map.of("order.publish.success", Set.of("region")),  // 允许的标签
            ObservabilityConfig.defaults().metricLabelWhitelist());
}
```

当前 adapter **默认未注册** `MicrometerMetricsProvider` / `ServiceMeters` Bean — 需自行 `@Bean` / `@Produces` 注入 `MicrometerMetricsProvider` 并暴露为 `ServiceMeters`。

### 5. 敏感字段脱敏

```java
@Bean
SensitiveValueMasker serviceSensitiveValueMasker() {
    return new SensitiveValueMasker();
}
```

`AccessLogWriter` 对 `principalId`、`traceId` 等字段走 masker。

---

## 配置项（`ObservabilityConfig`）

| 字段 | 默认值 | 说明 |
|---|---|---|
| `accessLogEnabled` | `true` | 是否写 `service.access` |
| `businessMeterNames` | `[]` | 允许的业务指标名；未注册名调用失败 |
| `businessMeterTagWhitelist` | `{}` | 每个指标名允许的标签键 |
| `metricLabelWhitelist` | `method,route,status,...` | 平台指标 label 白名单 |

设计文档 YAML 前缀：`service.observability.*`（见 [adapter 配置契约](../../docs/service-adapter-design.md#7-配置契约)）。

---

## Spring vs Quarkus：差异只在装配

| 项目 | Spring | Quarkus |
|---|---|---|
| 自动 Filter | `ServiceServletFilter` / `ServiceWebFilter` | JAX-RS 请求过滤器 |
| 默认 Bean 定义 | `ServiceCoreConfiguration` | `ServiceRuntimeBeans` |
| 覆盖 TraceProvider | `@Bean` 同名替换 | `@Produces @DefaultBean` |
| 业务 API | `ServiceMeters` / SLF4J / `@Traced` | **相同** |
| Access log 格式 | **相同** | **相同** |

**用法相同示例** — 治理/权限/审计也须 `ServiceInvocationBridge`（两框架 API 一致）：

```java
// Spring
return invocationBridge.invoke(this, method, "order.create", () -> orderService.create(cmd));

// Quarkus — 同一签名，注入 quarkus 包下的 ServiceInvocationBridge
return invocationBridge.invoke(this, method, "order.create", () -> orderService.create(cmd));
```

---

## 当前实现状态（阅读代码时请以此为准）

| 能力 | 状态 |
|---|---|
| Access log + MDC + requestId | ✅ Spring MVC/WebFlux、Quarkus 已接 Filter |
| `NoOpTraceProvider` 默认 | ✅ |
| `OpenTelemetryTraceProvider` | ✅ 类已实现；须应用侧注册 Bean |
| `MicrometerMetricsProvider` / `ServiceMeters` | ✅ 类已实现；**adapter 默认未装配** |
| `@Traced` 自动织入 | 🔜 设计有；须经 InvocationEngine / 未来 adapter 扫描 |
| 平台 HTTP 指标自动上报 | 🔜 设计完整；依赖 MetricsProvider Bean 装配 |
| Stream/WS 平台指标与连接级 access | 🔜 设计完整；见 [专题文档](docs/stream-and-websocket-observability.md) §5、§3.1 |
| 业务 audit 存储 | 归 `AuditStorage` SPI（runtime），非本模块 |

---

## 包结构

```text
com.innospots.nexus.service.observability
├── logging            # AccessLogWriter、MdcContextBridge、SensitiveValueMasker
├── trace              # OpenTelemetryTraceProvider、NoOpTraceProvider
├── metric             # MicrometerMetricsProvider、NoOpMetricsProvider
└── config             # ObservabilityConfig
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-observability</artifactId>
</dependency>
```

业务模块通常**不直接依赖**本 artifact；由 adapter 传递引入。仅当编写自定义 adapter 或测试时才显式依赖。

---

## 注意事项

- 观测与**业务 audit 查询**分离：本模块负责技术 access log / trace / metrics；`@Audited` 持久化走 `AuditStorage`（kernel/platform）。
- 高基数 label（principalId、requestId、sessionId、path）**禁止**作为指标 tag。
- WS 连接生命周期也会记 access/指标；不是「只有 REST 才有观测」。

---

## 流式 Stream 与 WebSocket 观测（专题）

长连接与长响应的日志、追踪、指标与短 HTTP **不是同一套时机**：以 **transportCompletion**（传输结束）写 access 终态，以 **协议事件**（`StreamEvent`、WS envelope）面向客户端，以 **MDC + ContextPropagation** 面向服务端诊断。

| 主题 | 文档 |
|------|------|
| 设计思路、三种终态、Access/MDC/Trace/Metrics、adapter 实践与实现差距 | [docs/stream-and-websocket-observability.md](docs/stream-and-websocket-observability.md) |
| 规范指标名与 label 白名单 | [Runtime §9](../../docs/service-runtime-design.md#9-日志tracing-与-metrics) |

**速览**：

- **Access Log**：`AccessLogWriter` 写 `service.access` 终态；Stream/WS 设计为 **整段传输或整连接一条**，不是每个 chunk/帧一条（Spring MVC 异步 SSE 的 access 时机仍在演进，见专题文档 §3.1）。
- **日志**：HTTP Filter 用 `MdcContextBridge`；Stream 生产线程须 `ContextPropagation`；WS handler 依赖 adapter 安装 `ServiceContext`（MDC 与连接级 access 见专题 §3.2）。
- **追踪**：默认 NoOp；OTel 用 `OpenTelemetryTraceProvider`。Stream 不按 token 建 Span；WS 握手/消息短 Span + `correlationId`（专题 §4）。
- **指标**：`service.stream.*`、`service.websocket.*` 已在 runtime 设计锁定；`MicrometerMetricsProvider` 已实现，stream/ws 自动上报与 adapter 装配按版本推进（专题 §5）。

---

## 相关文档

- [Stream / WebSocket 观测专题](docs/stream-and-websocket-observability.md)
- [Runtime · 日志/Tracing/Metrics](../../docs/service-runtime-design.md#9-日志tracing-与-metrics)
- [Contract · 观测 SPI](../../docs/service-contract-design.md#9-审计与观测-spi)
- [开发者体验 · 观测使用面](../../docs/service-developer-experience-design.md#8-观测与治理使用面)
- [Spring adapter · 观测](../../innospots-nexus-spring/innospots-nexus-spring-service/README.md#观测)
- [Governance README](../innospots-nexus-service-governance/README.md)
