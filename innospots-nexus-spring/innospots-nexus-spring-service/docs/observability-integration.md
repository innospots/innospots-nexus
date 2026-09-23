# 观测与监控采集接入手册（Spring）

说明在 Spring 应用中启用 **访问日志、MDC、分布式追踪、业务指标**：默认自动能力、Bean 覆盖方式，以及与 `ServiceInvocationBridge` 的关系。

---

## 1. 能力分层

| 层级 | 是否需要 Bridge | 内容 |
|------|-----------------|------|
| **Level 0 — 自动** | 否 | `X-Request-Id`、`ServiceContext`、MDC、Access Log |
| **Level 1 — SPI** | 部分 | `TraceProvider`、`MetricsProvider` / `ServiceMeters` |
| **Level 2 — 业务** | 可选 | `@Traced`、`ServiceMeters.increment`、审计 `@Audited` |

引入 `innospots-nexus-spring-service` 后，**任意普通 REST 请求** 即具备 Level 0（见 `SampleHttpResource.unaugmented()`）。

---

## 2. Level 0：访问日志与 MDC（零代码）

### 步骤 1：确认 Filter 生效

| 栈 | 类 |
|----|-----|
| MVC | `ServiceServletFilter`（`ServiceMvcConfiguration` 注册，`HIGHEST_PRECEDENCE + 20`） |
| WebFlux | `ServiceWebFilter` |

### 步骤 2：业务打日志

```java
@Slf4j
@RestController
public class OrderResource {
    @GetMapping("/orders/{id}")
    public OrderVo get(@PathVariable String id) {
        log.info("fetch order, orderId={}", id);
        return service.find(id);
    }
}
```

MDC 自动包含 `requestId`、`traceId`、`spanId`、`principalId`、`operation` 等（由 `MdcContextBridge` 安装）。

**禁止**：业务代码 `MDC.put` / `clear`；禁止自建 `@Tracing` 开关注解。

### 步骤 3：Access Log 格式

Logger 名称：`service.access`。示例字段：

```text
requestId=... method=GET route=/orders status=200 result=success durationMs=12 principalId=... traceId=...
```

### 步骤 4：关闭 Access Log

```java
@Bean
ObservabilityConfig serviceObservabilityConfig() {
    return new ObservabilityConfig(
            false,
            Set.of(),
            Map.of(),
            ObservabilityConfig.defaults().metricLabelWhitelist());
}
```

设计 YAML：`service.observability.access-log-enabled: false`。

---

## 3. Level 1：Trace（OpenTelemetry）

### 默认

`ServiceCoreConfiguration` 注册 `NoOpTraceProvider` — 不伪造全零 traceId。

### 接入 OTel

```java
@Bean
TraceProvider serviceTraceProvider(OpenTelemetry openTelemetry) {
    return new OpenTelemetryTraceProvider(openTelemetry);
}
```

应用须自行引入 OpenTelemetry SDK / Spring Boot OTel starter，并与 Nexus Bean 共存。

### `@Traced` 子 Span

契约注解 `@Traced` 依赖 `TraceProvider`；完整织入须经 `InvocationEngine` 或未来 adapter 扫描。**当前**以显式 Trace API 或 OTel 自动 instrumentation 为主。

---

## 4. Level 1：指标（Micrometer）

### 现状

`MicrometerMetricsProvider`、`ServiceMeters` 类已实现；**adapter 默认未注册** `MetricsProvider` / `ServiceMeters` Bean。

### 接入步骤

```java
@Bean
MetricsProvider metricsProvider(MeterRegistry meterRegistry) {
    return new MicrometerMetricsProvider(meterRegistry);
}

@Bean
ServiceMeters serviceMeters(MetricsProvider metricsProvider) {
    return metricsProvider.meters();
}
```

平台 HTTP 指标（如 `service.http.requests`）在 `MetricsProvider` 装配后由 `InvocationEngine` 侧记录；label 受白名单约束。

### 业务指标

```java
@Service
public class OrderService {
    private final ServiceMeters meters;

    public void publish(Order order) {
        meters.increment("order.publish.success");
        meters.record("order.publish.duration", Duration.ofMillis(elapsed));
    }
}
```

指标名须在 `ObservabilityConfig.businessMeterNames` 中注册：

```java
@Bean
ObservabilityConfig serviceObservabilityConfig() {
    return new ObservabilityConfig(
            true,
            Set.of("order.publish.success", "order.publish.duration"),
            Map.of("order.publish.success", Set.of("region")),
            ObservabilityConfig.defaults().metricLabelWhitelist());
}
```

未注册名称调用将失败（防止指标爆炸）。

---

## 5. 敏感字段脱敏

```java
@Bean
SensitiveValueMasker serviceSensitiveValueMasker() {
    return new SensitiveValueMasker();
}
```

`AccessLogWriter` 对 principal、trace 等字段走 masker。

---

## 6. `ObservabilityConfig` 字段

| 字段 | 默认 | 说明 |
|------|------|------|
| `accessLogEnabled` | `true` | Access Log 开关 |
| `businessMeterNames` | `[]` | 允许的业务指标名 |
| `businessMeterTagWhitelist` | `{}` | 每指标允许的标签键 |
| `metricLabelWhitelist` | 平台预设 | HTTP 等指标 label 白名单 |

---

## 7. Stream / WebSocket 观测

长连接与长响应的 access 终态、MDC 传播、Trace 粒度、平台指标名与当前 adapter 差距，见中立库专题文档（设计与实践机制）：

- [stream-and-websocket-observability.md](../../../innospots-nexus-service/innospots-nexus-service-observability/docs/stream-and-websocket-observability.md)

本节仅保留 Spring 侧要点：开流 GET 仍经 `ServiceServletFilter` / `ServiceWebFilter` 获得 `requestId` 与 `CancellationToken`；SSE 生产须 `ContextPropagation` 与 `cancelIfClientDisconnected`；WebSocket 经 `SpringWebSocketEndpointBridge` 安装 `ServiceContext`。

---

## 8. MVC 与 WebFlux

| 能力 | MVC | WebFlux |
|------|-----|---------|
| Access Log / MDC | `ServiceServletFilter` | `ServiceWebFilter` |
| Bean 覆盖 | 同名 `@Bean` | 相同 |
| 业务 `ServiceMeters` / SLF4J | **相同** | **相同** |

---

## 9. 验证

- 请求任意 API，检查响应头 `X-Request-Id` 与 `service.access` 日志行。
- `AdapterScenarioMvcTest` 中 `OBSERVABILITY_TRACE` 路径。
- 注册 OTel 后检查 traceId 进入 MDC。

---

## 10. 二次扩展开发

### 10.1 自定义 `AccessLogWriter`

一般不继承；可包装默认 writer 或替换 Bean：

```java
@Bean
AccessLogWriter serviceAccessLogWriter(ObservabilityConfig config, SensitiveValueMasker masker) {
    return new AccessLogWriter(config, masker);
}
```

与 `GovernanceConfig` 类似，需注意与 `ServiceCoreConfiguration` 的 Bean 定义冲突，使用 `ConditionalOnMissingBean` 改进前依赖覆盖策略。

### 10.2 扩展 `MdcContextBridge`

实现额外 MDC 键时，优先在 Filter 层扩展 `ServiceTransportSupport` 建立的 `ServiceContext`，避免业务线程散落逻辑。

### 10.3 自定义 `TraceProvider`

实现 contract `TraceProvider`，供 `@Traced` 与 runtime 使用。`OpenTelemetryTraceProvider` 为参考实现。

### 10.4 自定义 `MetricsProvider`

实现 contract `MetricsProvider`；`MicrometerMetricsProvider` 对接 `MeterRegistry`。**禁止**业务直接使用 `Counter.builder()`。

### 10.5 高基数标签红线

禁止将 `requestId`、完整 path、未归一化 `principalId` 作为指标 tag。使用白名单内 label（`method`、`route`、`status` 等）。

### 10.6 异步与 MDC

异步任务须通过 `ContextPropagation` / 受管 executor 传播 `ServiceContext`；裸 `CompletableFuture.runAsync` 会丢失 MDC。

---

## 11. 相关文档

- [service-observability README](../../../innospots-nexus-service/innospots-nexus-service-observability/README.md)
- [HTTP API 接入](http-api-integration.md)
- [治理接入](governance-integration.md)
