# 服务框架开发实践与开发者体验设计

状态：可实施规格 v1.2，2026-09-13。本文件把《统一服务框架开发实践与开发者体验规范》落到本仓库真实类型与模块名。实现与测试以本文件 + [契约](service-contract-design.md) 为准；实践文中的示例名不自动覆盖仓库坐标。

## 1. 文档定位

- 读者：业务开发人员、适配层、基础设施层。
- 成功标准：实践规范 §49 的 15 条验收，全部能在本规格中找到对应类型、禁止项和测试。
- 非目标：不在中立库演示 Model/Chat 业务实体；不新增 Spring MVC 业务端点规范。

## 2. 四级能力与模块依赖

| 级别 | 谁用 | 依赖 | 典型入口 |
|---|---|---|---|
| 0 无感 | 全体业务 | 应用引入适配器即可；业务模块只依赖 `service-contract` | 原生 Controller/Resource，无框架注解 |
| 1 声明 | 业务意图 | `service-contract` 注解 | `@RequiresPermission`、`@Audited`、治理注解 |
| 2 运行时 | 流/WS/文件/按需读上下文 | `service-stream` / `service-websocket` / `service-transfer` | `StreamSink`、`WebSocketHandler`、`UploadResource` |
| 3 SPI | 平台/基础设施 | 同上 + 实现 SPI | `PermissionProvider`、`AuditStorage`、治理 Provider |

业务 **Application / Domain** 模块：

```text
业务模块 → service-contract
         → service-stream | service-websocket | service-transfer（按需）
```

禁止业务核心依赖 `innospots-nexus-spring-service`、`innospots-nexus-quarkus-service` 或 `org.springframework.web.*` / `reactor.core.*` / `io.smallrye.mutiny.*` / `io.quarkus.*`。这些只出现在 Web Entry / Adapter。实践文中的 `service-core` = `service-contract`（+ 运行时由适配器传递引入，业务不要直接依赖 `service-runtime`，除非显式调用 `InvocationEngine`）。

应用启动坐标（不是新 Maven 模块）：

| 实践文名称 | 仓库 artifact |
|---|---|
| innospots-service-spring-boot-starter | `innospots-nexus-spring-service`（AutoConfiguration） |
| innospots-service-quarkus | `innospots-nexus-quarkus-service` + deployment（D7） |

## 3. 默认自动接入（禁止逐方法开启）

下列能力由 Filter / Interceptor / ResultHandler 默认完成，**禁止**提供或要求 `@Tracing`、`@Logging`、`@Metrics`、`@RequestContextEnabled`、`@EnableTracing` 等：

```text
Request ID、ServiceContext、Authentication Context、Trace、Access Log、
基础 Metrics、统一异常映射、生命周期、Context 清理
```

可选声明（Level 1，有业务差异才写）：

| 实践名 | 本规格注解 | 推荐放置 |
|---|---|---|
| 权限 | `@RequiresPermission` | Controller / Resource |
| 审计 | `@Audited` | Application Service |
| 限流 | `@RateLimited` | Resource 或高风险 Service |
| 舱壁 | `@BulkheadProtected` | Application Service |
| 熔断 | `@CircuitProtected` | Client / Provider |
| 超时 | `@TimeoutProtected` | Client / Provider |
| 执行模式 | `@Execution` | 与返回类型/宿主 `@Blocking` 一致时 |
| 可选业务 Span | `@Traced` | 确需子 Span 的应用方法；默认不要 |

`@TimeoutProtected` / `@RateLimited` / `@BulkheadProtected` / `@CircuitProtected` 的 `value` 都是**配置策略键**，不是 Duration 字面量。时长在 `service.policies.<key>`。

Domain 层禁止上述基础设施注解，也禁止 `ServiceContext`、HTTP/SSE/WS 类型。

## 4. 普通 HTTP 与分层

业务继续用宿主原生路由。框架不引入 Router、不要求 BaseController / BaseService。

推荐注解落点：

```text
Controller/Resource  → Permission（必要时 RateLimited）
Application Service  → Audited、BulkheadProtected
Client/Provider      → CircuitProtected、TimeoutProtected
Domain               → 无框架注解
```

同一执行边界（同一 operationId）上重复拦截必须去重，见运行时设计。下游 Client 使用独立 operationId，以便熔断只包下游。

## 5. ServiceContext 使用

禁止把 `ServiceContext` 从 Controller 一路传到 Repository。运行时自动传播。

按需读取（注入 `ServiceContextAccessor` 或当前上下文方法）：

```java
ServiceContext ctx = accessor.requireCurrent();
ServicePrincipal principal = ctx.principal();     // 等于 ctx.security()
String requestId = ctx.requestId();
Optional<String> clientId = ctx.clientId();
Optional<String> tenantId = ctx.tenantId();
Deadline deadline = ctx.deadline();
CancellationToken cancellation = ctx.cancellation();
```

实践文中的 `serviceContext.security().principal()` 映射为 `principal()`；`security()` 组件本身就是 `ServicePrincipal`，不再包一层 SecurityView。

业务数据仍走方法参数。后台任务必须经 `ContextExecutor` / 受管入口，禁止业务 `MDC.put/remove/clear` 和裸 ThreadLocal。

## 6. 异步、流、WebSocket、文件

异步返回 `CompletionStage<T>` 或 `Flow.Publisher<T>`。业务不调用 `copyContext()` / `restoreMdc()` / 手工 Span。

### 6.1 StreamSink（Level 2 主入口）

`StreamSession<T>` **extends** `StreamSink<T>`。业务默认只使用 Sink：

```java
StreamSink<AnalysisEvent> stream = streamManager.open(AnalysisEvent.class);
stream.emit("progress", event);
stream.complete();
if (stream.isCancelled()) {
    return;
}
```

`StreamChannel` / `StreamChannelFactory` 属 Level 3，普通业务禁止直接使用。

跨组件推流（实践 §9.2）允许按 id 发送，但必须鉴权 + 类型校验：

```java
streamManager.emit(sessionId, "progress", event);
streamManager.fail(sessionId, NexusException.build(status));
```

当前 `ServiceContext` 的 principal/scope 必须与会话创建时的 owner/scope 一致，事件类型必须可赋给 open 时捕获的 `Type`。否则 `NEX040006` 或 `NEX010001`。禁止未鉴权的 `Object` emit。

`fail` 只接受 `NexusException`。业务领域失败先在归属边界翻译。

### 6.2 WebSocket

业务实现 `WebSocketHandler<I,O>`，可选继承 `AbstractWebSocketHandler`（默认 onOpen/onClose/onError）。不强制基类。

当前连接：`session.send(message)`。跨组件：注入 `WebSocketService`（对 `WebSocketRegistry` 的业务门面）：

```java
webSocketService.send(connectionId, message);
webSocketService.sendToSession(sessionId, message);
```

消息类型权限由 MessageDescriptor 声明，Handler 内禁止手写 `if (!user.hasPermission)`。

### 6.3 文件

业务方法参数/返回使用 `UploadResource` / `DownloadResource`。禁止直接操作 `HttpServletResponse`、`OutputStream`、Vert.x Response。

## 7. 异常

实践文中的 `ServiceException` **不存在**（D4）。业务：

```java
throw NexusException.build(ModelStatusCode.MODEL_NOT_FOUND);
```

领域自定义异常若使用，必须是 `base.exception` 下批准的 `NexusException` 子类型。到达适配器的其它 JDK/框架异常映射 `NEX030017`，不把 `exception.getMessage()` 或路径放入响应。禁止在 Controller 里手写 `ResponseEntity.status(...)` 处理业务失败。

Jakarta `@Valid` 失败由宿主校验转为 `NEX010001`。

## 8. 观测与治理使用面

| 允许 | 禁止 |
|---|---|
| SLF4J `log.info("model published, modelId={}", id)` | `MDC.put/remove/clear` |
| 可选 `@Traced("model.validation")` | 普通方法里操作 Tracer/Span |
| `ServiceMeters.increment("model.publish.success")`（须先注册名称，禁止高基数 tag） | 业务直接 `counter.increment()` / Micrometer / OTel SDK |
| `@RateLimited("model-invoke")` 等策略键 | `rateLimiter.acquire()`、直接操作 CircuitBreaker/Semaphore |
| 配置全局默认策略 | 对非幂等写默认 Retry |

Retry **不在本框架一期实现**（D15）。若后续引入，只允许幂等下游 Client，禁止对全部业务方法默认重试。

## 9. 生命周期

启动：引入适配 artifact 后自动注册 Filter、拦截器、错误映射、观测、治理、Stream/WS runtime。业务不写 Bootstrap。

关闭：由 `ServiceRuntime` 停止准入、取消流、关闭 WS、flush、释放自建执行器。业务只关自己的资源。

## 10. 测试体验

| 类型 | 要求 |
|---|---|
| 业务单元测试 | Service 可纯 Java 构造；不强制启动 Web 容器；无活动上下文时不要调用 `requireCurrent()` |
| 框架能力 | 用适配夹具测权限/审计/限流/流/WS/追踪/上下文/文件/错误 |
| 双适配器 | `adapter-test` 同源场景，见实施附录 |

## 11. 禁止实践（实现与评审红线）

- 手工 Trace / MDC / 治理组件 / Connection Map / SseEmitter Map
- Core 暴露 `Flux`/`Multi`/`ServerWebExchange`/`SecurityIdentity`
- 无界 Queue / Stream Buffer / WS outbound
- 把 `ServiceContext` 作为每层方法参数
- Domain 依赖框架运行时类型
- 要求继承 BaseController/BaseService
- 为每个业务 Service 定义 `BusinessHandler` 之类空接口

## 12. 开发体验验收（对应实践 §49）

| # | 要求 | 规格落点 |
|---|---|---|
| 1 | 普通 HTTP 无框架专用代码可运行 | Level 0；适配默认拦截 |
| 2 | 自动日志/Trace/Metrics/Context | 默认能力列表；禁止 Enable* 注解 |
| 3–5 | 权限/审计/治理用注解 | §3 表 |
| 6 | Streaming 只用 StreamSink/Manager | §6.1；Channel 为 SPI |
| 7–9 | WS 只实现 Handler；Registry/生命周期框架维护 | §6.2 |
| 10 | 断开传播 Cancellation | 运行时设计 |
| 11 | 不操作 Trace/MDC | §8、§11 |
| 12 | 不直接操作限流/舱壁/熔断 | §8 |
| 13 | Spring/Quarkus 习惯不变 | 原生路由；D1 |
| 14 | App/Domain 不依赖框架运行时类型 | §2 |
| 15 | 启停无需业务管理框架资源 | §9 |

对应测试：`DeveloperExperienceContractsTest`（注解集合不含 Enable*；StreamSink 为公开类型；Channel 不在 contract 对外业务 Javadoc 推荐列表）、adapter-test 的无注解 HTTP 场景。
