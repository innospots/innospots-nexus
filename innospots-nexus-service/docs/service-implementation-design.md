# 服务框架实施与验收设计

状态：可实施规格 v1.2，2026-09-13。本文件是 `java:project` / `java:develop` 的施工图。签名与状态机以[主方案](service-framework-design.md)、[开发体验](service-developer-experience-design.md)、[契约](service-contract-design.md)、[运行时](service-runtime-design.md)、[适配](service-adapter-design.md)为准；本文件给出模块增量、源文件、测试类、里程碑和需求追踪。

实施约束：

- 不复制遗留 Innospots 源码或 POM。
- 每批 Java 源文件改动后立即 `mvn clean compile`；本地 JDK 低于 25 时报告环境不匹配，不降低基线。
- 不更新 `skills/java/java-reference/references/modules/`。
- 推迟能力用 `TODO` + `NexusException.build(StatusCode)`，禁止 `UnsupportedOperationException`。
- 无业务 REST 端点；不新建持久化表或 DAO。

---

## 1. 工程增量（先 `java:project`）

已存在、本规格不重建：

| artifact | 现状 |
|---|---|
| `innospots-nexus-service` 及八个子模块 | POM + 空包根 |
| `innospots-nexus-spring-service` | 已组合六个能力库 |
| `innospots-nexus-quarkus-service` | 已组合六个能力库 |
| 根 BOM 中八个 service 坐标 | 已登记 |

M0 必须新增（无这些模块不得宣称 Quarkus 零侵入或双适配器契约一致）：

| artifact | 类型 | parent | 直接依赖 |
|---|---|---|---|
| `innospots-nexus-quarkus-service-deployment` | Quarkus deployment | `innospots-nexus-quarkus` | `quarkus-service` runtime + Quarkus deployment API（BOM 管理） |
| `innospots-nexus-service-adapter-test` | 测试夹具 JAR | `innospots-nexus-parent` | contract、http、stream、websocket；test 范围 JUnit/AssertJ。禁止 Spring/Quarkus |

BOM 在 M0 新登记（版本以 effective POM 锁定，本稿不编造补丁号）：

| 坐标 | 消费方 | 备注 |
|---|---|---|
| `io.opentelemetry:opentelemetry-api` | observability | 与宿主 OTel 对齐 |
| `io.opentelemetry:opentelemetry-sdk-testing` | adapter-test / 适配 test | test scope |
| `io.github.resilience4j:resilience4j-circuitbreaker` | governance | 不引 Spring/Quarkus starter |

`micrometer-core` 已在 BOM。Spring MVC/WebFlux/WebSocket、Quarkus REST/WebSockets Next 以 optional / runtime 依赖进入适配模块，版本只来自 Boot/Quarkus BOM。

---

## 2. 源文件清单

包前缀：中立库 `com.innospots.nexus.service.<segment>`；Spring `com.innospots.nexus.spring.service`；Quarkus runtime `com.innospots.nexus.quarkus.service`；Quarkus deployment `com.innospots.nexus.quarkus.service.deployment`。每个目录含 `package-info.java`，计入 ≤15 文件上限。下列为主生产类型；测试类见 §4。

### 2.1 contract（M1）

| 包 | 文件 |
|---|---|
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

`contract.invocation` 含 InterceptorOrders/Ids 后为 9 个类型 + package-info = 10，未超限。

### 2.2 runtime（M2）

| 包 | 文件 |
|---|---|
| runtime.context | ThreadBoundServiceContext, ContextSnapshot, ContextPropagation, ContextExecutor |
| runtime.invocation | InvocationEngine, InvocationCall, InvocationControl, TransportCompletionController, ControlInterceptor |
| runtime.cancellation | CancellationSource |
| runtime.time | MonotonicDeadline, DeadlineScheduler, DeadlineInterceptor |
| runtime.security | AuthenticationCoordinator, AuthorizationInterceptor |
| runtime.audit | AuditInterceptor, AuditDispatcher, AuditLifecycle |
| runtime.lifecycle | ServiceRuntime, RuntimeState |
| runtime.idempotency | IdempotencyCoordinator, IdempotencyEntry, IdempotencyKey |
| runtime.policy | AnnotationPolicyResolver, DefaultPolicyCatalog |

`runtime.audit` 与 `runtime.idempotency` 一期编译空实现：`enter` 在未启用时直接返回 completed lease；启用但 SPI 缺失则启动失败（REQUIRED）或跳过（BEST_EFFORT 且 `service.audit.enabled=false`）。

### 2.3 http（M3）

| 包 | 文件 |
|---|---|
| http.error | HttpErrorMapper, ErrorResponseProfile, ProblemDetailVo |
| http.header | HeaderPolicy, RequestIdPolicy, StandardHeaders |

`StandardHeaders` 落在 `http.header`。契约附录若出现同名类，以本清单为准。

### 2.4 stream（M4）

| 包 | 文件 |
|---|---|
| stream.session | StreamSink, StreamSession, DefaultStreamSession, StreamManager, DefaultStreamManager, StreamState, StreamSnapshot |
| stream.channel | StreamChannel, StreamChannelFactory, BoundedStreamChannel, EmitResult, OverflowPolicy |
| stream.event | StreamEvent, StreamEventType |
| stream.config | StreamConfig |

`stream.session` 7 类型 + package-info = 8。`DefaultStreamSession` 同时实现 Session 与 Sink。

### 2.5 websocket（M5）

| 包 | 文件 |
|---|---|
| websocket.session | WebSocketSession, WebSocketContext, WebSocketClose, ConnectionState |
| websocket.message | WebSocketMessage, WebSocketCodec, JsonWebSocketCodec, MessageDescriptor, FrameType |
| websocket.handler | WebSocketHandler, ReactiveWebSocketHandler, AbstractWebSocketHandler |
| websocket.registry | WebSocketService, WebSocketRegistry, LocalWebSocketRegistry, BroadcastResult, ConnectionSnapshot |
| websocket.config | WebSocketRuntimeConfig |

`AbstractWebSocketHandler` 提供 onOpen/onClose/onError 的默认 completed stage，业务可不继承。`LocalWebSocketRegistry` 实现 `WebSocketService`。

### 2.6 governance（M6）

| 包 | 文件 |
|---|---|
| governance.ratelimit | LocalTokenBucketProvider, RateLimitInterceptor |
| governance.bulkhead | SemaphoreBulkheadProvider, BulkheadInterceptor |
| governance.circuit | Resilience4jCircuitBreakerProvider, CircuitBreakerInterceptor |
| governance.timeout | TimeoutInterceptor |
| governance.config | GovernanceConfig |

### 2.7 observability（M6）

| 包 | 文件 |
|---|---|
| observability.trace | OpenTelemetryTraceProvider |
| observability.metric | MicrometerMetricsProvider |
| observability.logging | AccessLogWriter, SensitiveValueMasker, MdcContextBridge |
| observability.config | ObservabilityConfig |

一期必须提供 no-op 安全回退：无 OTel SDK 时 `TraceProvider` 返回空快照且不造假全零 traceId；无 MeterRegistry 时指标变成空操作。回退类放 observability 模块，由适配器按 classpath 选择。

### 2.8 transfer（二期 M9）

| 包 | 文件 |
|---|---|
| transfer.content | BinarySource, PublisherBinarySource, ResourceContentReader, ContentMetadata, ByteRange |
| transfer.upload | UploadResource, UploadPolicy, UploadValidator, MalwareScanner, ScanResult |
| transfer.download | DownloadResource, DownloadRequest, DownloadPlanner, DownloadPlan |
| transfer.config | TransferConfig |

一期 `service.file.enabled=false`。若误开且无 Reader，启动失败 `SRV150017`。

### 2.9 Spring 适配（M7）

见[适配文档 §3](service-adapter-design.md) 表。测试夹具另加：

| 包 | 文件 |
|---|---|
| test 夹具（src/test） | MvcTestApplication, WebFluxTestApplication, HostSecurityProvider, HostPermissionProvider, SampleHttpResource, SampleStreamResource, SampleWebSocketEndpoint |

### 2.10 Quarkus 适配（M8）

见[适配文档 §4](service-adapter-design.md) 表。deployment 至少：`ServiceProcessor`, `ServiceAnnotationTransformer`, `ServiceMetadataBuildItem`。测试夹具：`QuarkusTestApplication` 等价资源。

---

## 3. 里程碑

顺序强制：后一里程碑依赖前一里程碑的契约测试绿灯。一期 = M0–M8；二期 = M9。

| 里程碑 | 目标 | 完成定义 |
|---|---|---|
| M0 | project：deployment + adapter-test；BOM 登记 OTel API 与 Resilience4j；effective POM 锁定 | `mvn validate`；adapter-test 可编译空场景骨架 |
| M1 | contract 类型、ServiceStatusCode、ErrorCatalog | 状态码契约测试通过；无框架依赖 |
| M2 | InvocationEngine、上下文、取消、Deadline、ServiceRuntime | 同步/异步/流三入口单测；lease 逆序释放；取消至多一次 |
| M3 | HTTP 错误映射、Header/RequestId 策略 | legacy 与 problem 两套形状单测；未知码 → SYSTEM_ERROR |
| M4 | StreamSession/Channel/Manager、SSE 编码规则（中立） | 状态机、单订阅、背压、TTL/idle 单测 |
| M5 | WebSocket Session/Registry/Codec/Handler | 连接/消息权限、有界发送、关闭竞态单测 |
| M6 | 本地限流/bulkhead/熔断/超时 + 日志/指标/追踪 Provider | 治理拒绝路径；OTel no-op；指标 label 白名单 |
| M7 | Spring MVC + WebFlux 自动配置与夹具 | adapter-test 场景在 MVC、WebFlux 全绿 |
| M8 | Quarkus REST + WebSockets Next + deployment | 同一 adapter-test 场景在 Quarkus JVM 全绿 |
| M9 | 文件传输、审计 REQUIRED/BEST_EFFORT、NDJSON、幂等、细粒度 WS 权限 | 二期清单；一期接口保持兼容 |

一期不实现但必须保留编译入口：transfer/audit/idempotency 的 record 与注解在 M1 一并落地，未启用时不注册拦截器。这样二期不必改公共兼容面。禁用路径不得变成放行。

---

## 4. 测试范围

### 4.1 契约测试（形状锁定）

| 计划类名 | 模块 | 断言要点 |
|---|---|---|
| ServiceStatusCodeContractsTest | contract | 九字符、SRV module、local 全目录唯一、双语 message/advice、HTTP 映射、`NexusException.build` |
| ServiceErrorCatalogContractsTest | contract | 只接受注册目录；未知码 → SYSTEM_ERROR；禁止猜 HTTP |
| ServiceContextContractsTest | contract | 非空组件、Header 小写拷贝、Principal 不可变集合、ANONYMOUS id |
| OperationPolicyContractsTest | contract | 注解成员、PublicAccess 与权限冲突规则（解析器单测可放 runtime） |
| StreamSessionContractsTest | stream | 状态枚举、单订阅、OverflowPolicy 集合 |
| WebSocketMessageContractsTest | websocket | envelope 字段、connectionId≠sessionId |
| UploadDownloadContractsTest | transfer | record 字段；一期可先锁形状后跳过行为 |

### 4.2 行为单测（中立库）

| 计划类名 | 断言要点 |
|---|---|
| InvocationEngineTest | sync/async/stream 成功；拦截器失败不进业务；lease 逆序 finish；重复 finish 幂等 |
| CancellationSourceTest | 首次 true 其后 false；已取消后注册立即回调；回调异常隔离 |
| MonotonicDeadlineTest | shorten 不延长；remaining ≥ 0 |
| ContextPropagationTest | 嵌套恢复；跨线程 snapshot；不得 clear 外层 |
| BoundedStreamChannelTest | REJECT/DROP_*/CLOSE；n≤0 走 Flow 协议；单订阅第二次 LIFECYCLE_CLOSED |
| DefaultStreamManagerTest | 容量、subscribe-timeout、idle 不因 heartbeat 刷新、关闭排空；按 id emit 鉴权成功/越权 403；类型不匹配 400 |
| StreamSinkTest | open 返回 Sink；isCancelled；emit 默认 type=message |
| DeveloperExperienceContractsTest | 注解包不含 Enable*/Tracing/Logging/Metrics；StreamSink 公开；Channel 不出现在业务推荐入口测试白名单 |
| LocalWebSocketRegistryTest | 跨租户隔离；部分广播失败计数；未授权 close 失败 |
| JsonWebSocketCodecTest | 未知 type → SRV120009；超限 → SRV010006 |
| LocalTokenBucketProviderTest | 复合维度原子扣减；满 key 淘汰；retryAfter |
| SemaphoreBulkheadProviderTest | 无排队；close 幂等；异常路径释放 |
| Resilience4jCircuitBreakerProviderTest | 权限/429/取消不计失败；OPEN → SRV060005 |
| HttpErrorMapperTest | legacy R 与 problem 字段；已提交响应不改写 |
| SensitiveValueMaskerTest | Authorization/Cookie/JWT/API Key 默认掩码 |
| AuditDispatcherTest | BEST_EFFORT 丢弃计数；REQUIRED 无事务拒绝 |

### 4.3 适配器黑盒（adapter-test，M7/M8）

共享场景接口（类名锁定）：

| 场景类 | 覆盖需求 |
|---|---|
| HttpUnaugmentedScenario | 无框架注解的 GET 仍有 requestId、access log、错误映射 |
| HttpContextScenario | requestId 生成/透传、ServiceContext 可注入获取、MDC 字段（业务代码不调用 MDC） |
| HttpErrorScenario | 401/403/404/429/500 形状在 MVC/WebFlux/Quarkus 一致 |
| HttpSecurityScenario | 匿名拒绝；HostPermissionProvider 允许/拒绝 |
| HttpCancellationScenario | 客户端断开后 token 取消（MVC 允许写探测延迟） |
| StreamSseScenario | SSE Content-Type、event 类型、complete、buffer 拒绝 |
| StreamCancelScenario | 断开取消上游；不把取消计为 500 |
| WebSocketSessionScenario | onOpen/onMessage/onClose、registry send |
| WebSocketPermissionScenario | 握手通过后未知消息类型拒绝，连接可保持 |
| GovernanceRateLimitScenario | 429 + Retry-After |
| GovernanceTimeoutScenario | 方法超时 SRV100002 / 504 |
| ObservabilityTraceScenario | 响应或日志含合法 trace 关联；无 SDK 时不造假 |

每个适配模块提供 `AdapterScenarioRunner`：启动夹具、注入 URL、调用上述场景。禁止复制断言逻辑。

Quarkus native 不在一期必测；作为 D7 正式扩展的后续门禁。

### 4.4 不测范围

- 平凡 record getter。
- 宿主 Servlet/Vert.x 容器本身的正确性。
- 真实病毒扫描引擎、真实 JWT 签发服务器、真实 Redis 集群限流。
- 管理台 IAM 表、审计查询 API、console catalog。
- 性能绝对值（只测对照夹具可运行，数字写入测量记录而非代码断言）。
- 每 token 一个 Span 的负例以外的 OTel SDK 内部实现。

### 4.5 集成边界

一期 `*IT` 仅适配器夹具（嵌入式宿主）。不引入 Testcontainers / 真实 DB。二期审计 REQUIRED 若需事务，在宿主应用模块测，不在中立库测 JDBC。

---

## 5. 需求追踪

### 5.1 需求模块 → 仓库模块

| 需求书模块 | 仓库 artifact / 包 |
|---|---|
| service-core | contract + runtime |
| service-stream | service-stream |
| service-websocket | service-websocket |
| service-file | service-transfer（M9） |
| service-security | contract.security + runtime.security + 适配 security 包 |
| service-governance | service-governance |
| service-observability | service-observability |
| service-audit | contract.audit + runtime.audit（行为 M9） |
| service-spring | innospots-nexus-spring-service |
| service-quarkus | quarkus-service + deployment |
| service-bom | innospots-nexus-bom |

### 5.2 一期 / 二期

| 阶段 | 需求书 | 本规格 |
|---|---|---|
| 一期 | §45：core/stream/websocket/security/governance/observability/spring/quarkus | M0–M8 |
| 二期 | §46：file/audit/NDJSON/idempotency/advanced metrics/WS 细权限/hardening | M9 |

一期仍实施：WS 消息类型白名单 + 独立消息权限判定（运行时 §5.1），不把“握手成功=全部消息放行”留到二期。

### 5.3 验收项 §47

| # | 验收 | 里程碑 | 证明 |
|---|---|---|---|
| 1 | 统一核心 Contract | M1+M7+M8 | adapter-test 同源场景 |
| 2 | 普通 HTTP 开发方式不变 | M7/M8 | 夹具使用原生路由注解 |
| 3 | 无需显式日志/Trace/Metrics 注解 | M6–M8 | 无注解 HTTP 仍有 access log/trace/metrics |
| 4 | 权限/Audit/Governance 可用注解 | M6–M8；Audit 行为 M9 | Quarkus 依赖 D7 |
| 5 | SSE 统一 Stream API 推送 | M4+M7/M8 | StreamSseScenario；业务入口为 StreamSink |
| 6 | Stream 生命周期自动管理 | M4 | DefaultStreamManagerTest |
| 7 | WS Session/Message/Handler | M5 | WebSocketSessionScenario |
| 8 | WS 生命周期自动管理 | M5+M7/M8 | 关闭竞态单测 |
| 9 | 主动向 Session 发消息 | M5 | WebSocketService.send / sendToSession |
| 10 | 统一 Security Context | M2+M7/M8 | HttpSecurityScenario |
| 11 | 断开 Cancellation | M2+M7/M8 | HttpCancellation / StreamCancel |
| 12 | 统一错误结构 | M3 | HttpErrorScenario |
| 13 | 本地限流/bulkhead/熔断/超时 | M6 | Governance*Scenario |
| 14 | 结构化日志 | M6 | MDC 字段单测 |
| 15 | OTel Tracing | M6 | ObservabilityTraceScenario |
| 16 | 统一 Metrics | M6 | 指标名白名单单测 |
| 17 | 重要操作 Audit | M9 | AuditDispatcherTest；一期注解可编译 |
| 18 | 双适配器 Contract Test | M7+M8 | adapter-test |
| 19 | 业务核心不依赖框架类型 | M1 | contract/runtime 依赖树检查 |
| 20 | 启停无需业务管理资源 | M2+M7/M8 | ServiceRuntime start/stop 测试 |

### 5.4 需求书能力对照（开发时不得漏项）

一期必须在代码中可见：ServiceContext 自动传播、Header 标准、W3C trace、ExecutionMode、CORS 复用宿主、默认启用 requestId/context/trace/metrics/access log/error mapping/security adapter、按需启用 audit/rate limit/circuit/bulkhead/timeout。

明确推迟：分布式 Session、跨节点推送、Redis 限流、SSE 重放、STOMP、断点续传、多段 Range、通用重试引擎、Spring Security 默认依赖、病毒扫描具体引擎。

开发体验验收（实践规范 §49）见 [DX 文档 §12](service-developer-experience-design.md)。额外证明：`HttpUnaugmentedScenario`、`DeveloperExperienceContractsTest`、`StreamSinkTest`。

---

## 6. 性能与稳定性预算（待测目标）

不是已达到的 SLA。M7 夹具可用后用同机对照测量，记录到 PR，不把数字写进断言。

| 项 | 目标 |
|---|---|
| 无业务的 GET 相对未装拦截器基线 | p99 附加延迟倾向 < 2ms（同机、预热后，仅作回归告警阈值候选） |
| 队列/缓冲 | 全部有界；禁止无配置 `-1` 表示无限 |
| 线程 | 不为每请求创建线程；BLOCKING 走有界执行器 |
| 关闭 | `service.shutdown.grace-period` 默认 30s 内停止准入并排空 |

稳定性门禁见运行时 §1 与 §12：清理只执行一次、孤儿任务计入 `service.work.orphaned`、不伪装已停止。

---

## 7. develop 入口

推荐红灯顺序（`java:develop` 测试先行）：

1. `ServiceStatusCodeContractsTest` → 实现 `ServiceStatusCode` / `ServiceErrorCatalog`
2. `ServiceContextContractsTest` → 实现 context records
3. `CancellationSourceTest` → `CancellationSource`
4. `InvocationEngineTest` → `InvocationEngine`（先假拦截器）
5. `BoundedStreamChannelTest` → channel
6. `HttpErrorMapperTest` → HTTP 映射
7. `LocalTokenBucketProviderTest` → 治理
8. adapter-test 场景 → Spring 再 Quarkus

`java:check` 在 M7/M8 后做依赖树、规范与安全扫描。

---

## 8. 架构约束自检（实施侧）

- [x] 源文件按包列出，develop 无需猜测类名
- [x] 每个应用可见失败有 StatusCode 与测试行
- [x] 无表 / 无 DAO / 无 mapper.xml
- [x] 需新 Maven 模块已标为 M0 `java:project`
- [x] 一期/二期与验收 20 条可追踪
- [x] 双适配器共用 adapter-test，不分叉断言
