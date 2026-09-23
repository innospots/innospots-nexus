# 模块清单、测试与验收

本文供**核对实现完整性**与**回归测试**使用：Maven 结构、主要源文件、测试类与需求书验收项。架构语义见 [总览](service-framework-design.md)；**未完成能力**见 [service-future-work.md](service-future-work.md)。

---

## 1. 工程结构

### 1.1 中立库与适配器

| artifact | 说明 |
|----------|------|
| `innospots-nexus-service` | 聚合 POM（八个中立子模块） |
| `innospots-nexus-service-contract` … `governance` | 中立实现，继承 `innospots-nexus-parent` |
| `innospots-nexus-spring-service` | Spring MVC / WebFlux 适配（`@EnableNexusService` 或 bootstrap 引入） |
| `innospots-nexus-quarkus-service` + `…-deployment` | Quarkus runtime + 构建期扩展 |
| `innospots-nexus-service-adapter-test` | 共享黑盒场景（非生产 JAR） |

版本与坐标登记在 `innospots-nexus-bom`；第三方库版本只来自 BOM / Boot / Quarkus BOM。

### 1.2 依赖登记（治理相关）

| 坐标 | 消费方 |
|------|--------|
| `io.opentelemetry:opentelemetry-api` | observability |
| `io.opentelemetry:opentelemetry-sdk-testing` | adapter-test / 适配测试 |
| `io.github.resilience4j:resilience4j-circuitbreaker` | governance |

---

## 2. 源文件索引

包前缀：中立 `com.innospots.nexus.service.<segment>`；Spring `com.innospots.nexus.spring.service`；Quarkus `com.innospots.nexus.quarkus.service`（deployment 子包 `.deployment`）。每包含 `package-info.java`，目录内 Java 文件 ≤ 15。

类型清单与签名以 [契约设计](service-contract-design.md) 为准；下表为**模块落点**索引。

### 2.1 contract

| 包 | 主要类型 |
|----|----------|
| contract.context | ServiceContext, ServiceContextAccessor, RequestMetadata, ContextAttributes, AttributeKey |
| contract.security | ServicePrincipal, PrincipalType, ServiceScope, ResourceRef, AuthenticationResult, SecurityProvider, PermissionProvider, PermissionCheck, PermissionDecision |
| contract.security.annotation | RequiresPermission, PublicAccess |
| contract.cancellation | CancellationToken, CancellationRegistration, CancellationReason |
| contract.time | Deadline, Ticker |
| contract.trace | TraceSnapshot, TraceProvider, TraceHandle |
| contract.invocation | InvocationContext, InvocationOutcome, OutcomeType, ServiceInterceptor, InvocationLease, TransportCompletion, ExecutionMode, InterceptorOrders, InterceptorIds |
| contract.policy | OperationPolicy, PolicyCatalog, OperationDescriptor, ResponseProfile, AuditMode |
| contract.policy.annotation | ServiceOperation, RateLimited, BulkheadProtected, CircuitProtected, TimeoutProtected, Execution, Traced |
| contract.error | ServiceError, ServiceErrorCatalog |
| contract.status | ServiceStatusCode |
| contract.audit | AuditEvent, AuditStorage, TransactionalAuditStorage, AuditSnapshotProvider, AuditSnapshot, CommitObserver, CommitState |
| contract.audit.annotation | Audited |
| contract.governance | RateLimitProvider, RateLimitRequest, RateLimitDecision, CircuitBreakerProvider, CircuitPermit, BulkheadProvider, BulkheadPermit |
| contract.observation | MetricsProvider, InvocationObservation, ServiceMeters |

### 2.2 runtime

| 包 | 主要类型 |
|----|----------|
| runtime.context | ThreadBoundServiceContext, ContextSnapshot, ContextPropagation, ContextExecutor |
| runtime.invocation | InvocationEngine, InvocationCall, InvocationControl, TransportCompletionController, ControlInterceptor |
| runtime.cancellation | CancellationSource |
| runtime.time | DeadlineScheduler, DeadlineInterceptor |
| runtime.security | AuthenticationCoordinator, AuthorizationInterceptor |
| runtime.audit | AuditInterceptor, AuditDispatcher, AuditLifecycle |
| runtime.lifecycle | ServiceRuntime, RuntimeState |
| runtime.idempotency | IdempotencyCoordinator, IdempotencyEntry, IdempotencyKey |
| runtime.policy | AnnotationPolicyResolver, DefaultPolicyCatalog |

审计 / 幂等：`service.audit.enabled` / `service.idempotency.enabled` 为 false 时拦截器不注册或 no-op；REQUIRED 审计无 SPI 时启动失败。

### 2.3 http

| 包 | 主要类型 |
|----|----------|
| http.error | HttpErrorMapper, ErrorResponseProfile, ProblemDetailVo |
| http.header | HeaderPolicy, RequestIdPolicy, StandardHeaders |

### 2.4 stream

| 包 | 主要类型 |
|----|----------|
| stream.session | StreamSink, StreamSession, DefaultStreamSession, StreamManager, DefaultStreamManager, StreamState, StreamSnapshot |
| stream.channel | StreamChannel, StreamChannelFactory, BoundedStreamChannel, EmitResult, OverflowPolicy |
| stream.event | StreamEvent, StreamEventType |
| stream.config | StreamConfig |

### 2.5 websocket

| 包 | 主要类型 |
|----|----------|
| websocket.session | WebSocketSession, WebSocketContext, WebSocketClose, ConnectionState |
| websocket.message | WebSocketMessage, WebSocketCodec, JsonWebSocketCodec, MessageDescriptor, FrameType |
| websocket.handler | WebSocketHandler, ReactiveWebSocketHandler, AbstractWebSocketHandler |
| websocket.registry | WebSocketService, WebSocketRegistry, LocalWebSocketRegistry, BroadcastResult, ConnectionSnapshot |
| websocket.config | WebSocketRuntimeConfig |

### 2.6 governance / observability

| 模块 | 包 | 主要类型 |
|------|-----|----------|
| governance | governance.ratelimit / bulkhead / circuit / timeout / config | LocalTokenBucketProvider, RateLimitInterceptor, SemaphoreBulkheadProvider, BulkheadInterceptor, Resilience4jCircuitBreakerProvider, CircuitBreakerInterceptor, TimeoutInterceptor, GovernanceConfig |
| observability | observability.trace / metric / logging / config | OpenTelemetryTraceProvider, MicrometerMetricsProvider, AccessLogWriter, SensitiveValueMasker, MdcContextBridge, ObservabilityConfig |

无 OTel / MeterRegistry 时须有 no-op 回退，不造假 traceId。

### 2.7 transfer

| 包 | 主要类型 |
|----|----------|
| transfer.content | BinarySource, PublisherBinarySource, ResourceContentReader, ContentMetadata, ByteRange |
| transfer.upload | UploadResource, UploadPolicy, UploadValidator, MalwareScanner, ScanResult |
| transfer.download | DownloadResource, DownloadRequest, DownloadPlanner, DownloadPlan |
| transfer.config | TransferConfig |

`service.file.enabled=false` 为默认；误开且无 `ResourceContentReader` 时启动失败 `SRV150017`。上传适配器缺口见 [待完善](service-future-work.md)。

### 2.8 Spring / Quarkus 适配

类表见 [适配设计 §3–§4](service-adapter-design.md)。测试夹具：`MvcTestApplication`, `WebFluxTestApplication`, `QuarkusTestApplication` 及 sample 资源/端点。

---

## 3. 测试体系

### 3.1 契约测试（形状锁定）

| 类名 | 模块 | 要点 |
|------|------|------|
| ServiceStatusCodeContractsTest | contract | SRV 九字符、local 唯一、双语、HTTP 映射 |
| ServiceErrorCatalogContractsTest | contract | 注册目录、未知码 → SYSTEM_ERROR |
| ServiceContextContractsTest | contract | 非空组件、Header 拷贝、Principal 不可变 |
| OperationPolicyContractsTest | contract | 注解成员、PublicAccess 冲突 |
| StreamSessionContractsTest | stream | 状态、单订阅、OverflowPolicy |
| WebSocketMessageContractsTest | websocket | envelope、connectionId≠sessionId |
| UploadDownloadContractsTest | transfer | record 形状与下载规划边界 |

### 3.2 行为单测（中立库）

| 类名 | 要点 |
|------|------|
| InvocationEngineTest | sync/async/stream、拦截器顺序、lease 逆序 |
| CancellationSourceTest / MonotonicDeadlineTest / ContextPropagationTest | 取消、deadline、上下文嵌套 |
| BoundedStreamChannelTest / DefaultStreamManagerTest / StreamSinkTest | 背压、鉴权 emit、Sink API |
| DeveloperExperienceContractsTest | 无 Enable*；StreamSink 为业务入口 |
| LocalWebSocketRegistryTest / JsonWebSocketCodecTest | 租户隔离、未知 type |
| LocalTokenBucketProviderTest / SemaphoreBulkheadProviderTest / Resilience4jCircuitBreakerProviderTest | 治理拒绝语义 |
| HttpErrorMapperTest / SensitiveValueMaskerTest / AuditDispatcherTest | 错误形态、掩码、审计模式 |

### 3.3 适配器黑盒（adapter-test）

| 场景类 | 覆盖 |
|--------|------|
| HttpUnaugmentedScenario | 无注解仍有 requestId、access log、错误映射 |
| HttpContextScenario | context / MDC |
| HttpErrorScenario | 401/403/404/429/500 三宿主一致 |
| HttpSecurityScenario | 匿名拒绝、Provider 允许/拒绝 |
| HttpCancellationScenario | 断开 → cancellation |
| StreamSseScenario / StreamCancelScenario | SSE 与取消 |
| WebSocketSessionScenario / WebSocketPermissionScenario | 生命周期与消息权限 |
| GovernanceRateLimitScenario / GovernanceTimeoutScenario | 429、504 |
| ObservabilityTraceScenario | trace 关联；无 SDK 不造假 |

各适配模块提供 `AdapterScenarioRunner`，**禁止复制断言逻辑**。

### 3.4 不测范围

平凡 getter、容器自身正确性、真实病毒引擎/JWT 服务器/Redis 集群、console IAM 与审计查询 API、性能绝对值断言（仅对照测量记录）。

集成：适配夹具 `*IT`；REQUIRED 审计与真实 DB 在宿主应用模块验证，中立库不测 JDBC。

---

## 4. 需求映射与验收

### 4.1 需求书模块 → 仓库

| 需求书模块 | 仓库 |
|------------|------|
| service-core | contract + runtime |
| service-stream / websocket / file | service-stream / websocket / transfer |
| service-security | contract.security + runtime.security + 适配 security 包 |
| service-governance / observability / audit | governance / observability / contract.audit + runtime.audit |
| service-spring / quarkus | spring-service / quarkus-service + deployment |
| service-bom | innospots-nexus-bom |

### 4.2 验收项（§47 对照）

| # | 验收 | 证明 |
|---|------|------|
| 1 | 统一核心 Contract | adapter-test 同源场景 |
| 2 | 普通 HTTP 开发方式不变 | 夹具原生路由注解 |
| 3 | 无需显式日志/Trace/Metrics 注解 | HttpUnaugmentedScenario 等 |
| 4 | 权限 / Audit / Governance 注解 | 注解 + 治理场景；Quarkus 构建期增强见待完善 |
| 5 | SSE 统一 Stream API | StreamSseScenario；入口 StreamSink |
| 6 | Stream 生命周期 | DefaultStreamManagerTest |
| 7–9 | WS Session / 生命周期 / 主动推送 | WebSocket*Scenario |
| 10 | Security Context | HttpSecurityScenario |
| 11 | 断开 Cancellation | HttpCancellation / StreamCancel |
| 12 | 统一错误结构 | HttpErrorScenario |
| 13 | 本地限流 / bulkhead / 熔断 / 超时 | Governance*Scenario |
| 14–16 | 日志 / Tracing / Metrics | MDC、Observability、指标白名单单测 |
| 17 | 重要操作 Audit | AuditDispatcherTest + `@Audited` |
| 18 | 双适配器 Contract Test | adapter-test 全绿 |
| 19 | 业务核心不依赖框架类型 | contract/runtime 依赖树 |
| 20 | 启停无需业务管理框架资源 | ServiceRuntime 测试 |

开发体验 15 条见 [DX §12](service-developer-experience-design.md)。

### 4.3 默认应具备的行为

自动传播 ServiceContext、标准 Header、W3C trace、ExecutionMode、宿主 CORS、默认 requestId/context/trace/metrics/access log/error mapping/security adapter；audit / 治理按配置启用。

明确不在框架内：分布式会话、跨节点推送、SSE 重放、STOMP、断点续传、多段 Range、通用重试、默认 Spring Security、具体病毒引擎——见 [service-future-work.md](service-future-work.md)。可选 Redis 限流见 `innospots-nexus-service-governance-redis`（非默认装配）。

---

## 5. 性能与稳定性（测量目标）

非 SLA。在夹具上相对「未装服务拦截器」基线对照测量，结果写入 PR，**不写入测试断言**。

| 项 | 目标 |
|----|------|
| 无业务 GET 附加延迟 | p99 倾向 &lt; 2ms（同机预热后，作回归告警候选） |
| 队列 / 缓冲 | 全部有界；禁止 `-1` 表示无限 |
| 线程 | 每请求不新建线程；BLOCKING 走有界执行器 |
| 关闭 | `service.shutdown.grace-period` 默认 30s |

稳定性：清理只执行一次、孤儿任务 `service.work.orphaned`、不伪装已停止——见 [运行时 §1、§12](service-runtime-design.md)。

---

## 6. 变更自检

- 新 public 类型是否落在正确模块与包（≤15 文件）  
- 每个用户可见失败是否有 StatusCode 与测试行  
- 无业务 REST、无表 / DAO  
- 双适配器共用 adapter-test  
- 文档与 [待完善](service-future-work.md) 是否同步  
