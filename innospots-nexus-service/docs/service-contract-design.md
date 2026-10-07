# 服务框架契约与类型设计

**契约参考**：公共类型、SPI 与状态码。边界与决策见 [总览](service-framework-design.md)；业务用法见 [开发体验](service-developer-experience-design.md)。下文代码块为签名/数据形状（省略 import 与校验）；每 public 类型单独源文件。模块落点见 [模块清单](service-implementation-design.md)。

## 1. 包与类型归属

前缀 `com.innospots.nexus.service`。config record 在各模块 `config` 包。每个包含 `package-info` 后仍 ≤ 15 个 `.java` 文件。

| 模块 | 包后缀 | 主要类型 |
|---|---|---|
| contract | contract.context | ServiceContext、ServiceContextAccessor、RequestMetadata、ContextAttributes、AttributeKey |
| contract | contract.security | ServicePrincipal、PrincipalType、ServiceScope、ResourceRef、AuthenticationResult、SecurityProvider、PermissionProvider、PermissionCheck、PermissionDecision |
| contract | contract.security.annotation | RequiresPermission、PublicAccess |
| contract | contract.cancellation | CancellationToken、CancellationRegistration、CancellationReason |
| contract | contract.time | Deadline、Ticker |
| contract | contract.trace | TraceSnapshot、TraceProvider、TraceHandle |
| contract | contract.invocation | InvocationContext、InvocationOutcome、OutcomeType、ServiceInterceptor、InvocationLease、TransportCompletion、ExecutionMode、InterceptorOrders、InterceptorIds |
| contract | contract.policy | OperationPolicy、PolicyCatalog、OperationDescriptor、ResponseProfile、AuditMode |
| contract | contract.policy.annotation | ServiceOperation、RateLimited、BulkheadProtected、CircuitProtected、TimeoutProtected、Execution、Traced |
| contract | contract.error | ServiceError、ServiceErrorCatalog |
| contract | contract.status | ServiceStatusCode（技术目录，对齐 `PluginStatusCode` 包位，不放 `domain.enums`） |
| contract | contract.audit | AuditEvent、AuditStorage、TransactionalAuditStorage、AuditSnapshotProvider、AuditSnapshot、CommitObserver、CommitState |
| contract | contract.audit.annotation | Audited |
| contract | contract.governance | RateLimitProvider、RateLimitRequest、RateLimitDecision、CircuitBreakerProvider、CircuitPermit、BulkheadProvider、BulkheadPermit |
| contract | contract.observation | MetricsProvider、InvocationObservation、ServiceMeters |
| runtime | runtime.context | ThreadBoundServiceContext、ContextSnapshot、ContextPropagation、ContextExecutor |
| runtime | runtime.invocation | InvocationEngine、InvocationCall、InvocationControl、TransportCompletionController |
| runtime | runtime.cancellation | CancellationSource |
| runtime | runtime.time | MonotonicDeadline、DeadlineScheduler |
| runtime | runtime.security | AuthenticationCoordinator、AuthorizationInterceptor |
| runtime | runtime.audit | AuditInterceptor、AuditDispatcher、AuditLifecycle |
| runtime | runtime.lifecycle | ServiceRuntime、RuntimeState |
| runtime | runtime.idempotency | IdempotencyCoordinator、IdempotencyEntry、IdempotencyKey |
| runtime | runtime.policy | AnnotationPolicyResolver、DefaultPolicyCatalog |
| http | http.error | HttpErrorMapper、ErrorResponseProfile、ProblemDetailVo |
| http | http.header | HeaderPolicy、RequestIdPolicy、StandardHeaders |
| stream | stream.session | StreamSink、StreamSession、StreamManager、StreamState、StreamSnapshot |
| stream | stream.channel | StreamChannel、StreamChannelFactory、BoundedStreamChannel、EmitResult、OverflowPolicy |
| stream | stream.event | StreamEvent、StreamEventType |
| stream | stream.config | StreamConfig |
| websocket | websocket.session | WebSocketSession、WebSocketContext、WebSocketClose、ConnectionState |
| websocket | websocket.message | WebSocketMessage、WebSocketCodec、MessageDescriptor、FrameType |
| websocket | websocket.handler | WebSocketHandler、ReactiveWebSocketHandler、AbstractWebSocketHandler |
| websocket | websocket.registry | WebSocketService、WebSocketRegistry、BroadcastResult、ConnectionSnapshot |
| websocket | websocket.config | WebSocketRuntimeConfig |
| transfer | transfer.content | BinarySource、ResourceContentReader、ContentMetadata、ByteRange |
| transfer | transfer.upload | UploadResource、UploadPolicy、UploadValidator、MalwareScanner、ScanResult |
| transfer | transfer.download | DownloadResource、DownloadRequest、DownloadPlanner、DownloadPlan |
| observability | observability.trace | OpenTelemetryTraceProvider |
| observability | observability.metric | MicrometerMetricsProvider |
| observability | observability.logging | AccessLogWriter、SensitiveValueMasker、MdcContextBridge |
| governance | governance.ratelimit | LocalTokenBucketProvider、RateLimitInterceptor |
| governance | governance.bulkhead | SemaphoreBulkheadProvider、BulkheadInterceptor |
| governance | governance.circuit | Resilience4jCircuitBreakerProvider、CircuitBreakerInterceptor |
| governance | governance.timeout | TimeoutInterceptor |

不引入 service-security/service-audit Maven 模块。运行时审计队列属于 runtime.audit，日志实现不能持有审计存储事务。

## 2. 基础类型与上下文

```java
public record ServiceContext(
        String requestId,
        RequestMetadata request,
        ServicePrincipal security,
        ServiceScope scope,
        TraceSnapshot trace,
        CancellationToken cancellation,
        Deadline deadline,
        ContextAttributes attributes
) {}

public record RequestMetadata(
        String method,
        String path,
        String routeTemplate,
        Map<String, List<String>> headers,
        String remoteAddress,
        String clientId
) {}

public interface ServiceContextAccessor {
    Optional<ServiceContext> current();
    ServiceContext requireCurrent();
}

public record ServicePrincipal(
        String id,
        PrincipalType type,
        String realm,
        Set<String> roles,
        Set<String> permissions,
        Map<String, String> attributes
) {}

public enum PrincipalType { USER, APPLICATION, SERVICE, API_KEY, ANONYMOUS }

public record ServiceScope(String tenantId, String workspaceId, String projectId) {}
public record ResourceRef(String type, String id, ServiceScope scope) {}
public record TraceSnapshot(String traceId, String spanId, boolean sampled) {}
```

- ServiceContext 所有组件非空；未指定 deadline 使用无限 Deadline，未采样 trace 使用合法空快照，不使用 null 对象。
- Principal.id 除 ANONYMOUS 外必填；ANONYMOUS 的 id 固定 `anonymous`，realm 必填。id 不做 Long 转换，不允许凭客户端 Header 构造已认证主体。
- ServiceScope 各 ID 可空；workspace 非空必须有 tenant，project 非空必须有 workspace；PLATFORM realm 的无租户请求可全空。认证源与资源解析器必须验证所属关系，不只是形状校验。
- RequestMetadata.method/path/remoteAddress 非空；routeTemplate 在路由尚未匹配时为空，匹配后创建新上下文；clientId 可空；Header 名统一小写且值列表深度复制，只保留允许传递的头。凭据只在认证阶段短暂存在，不进入这个 Map。
- TraceSnapshot 未启用/无 SDK 时 traceId/spanId 使用空字符串；Request ID 仍必有，不能造假的全零 OTel traceId。
- roles/permissions/attributes 返回不可变副本，角色仅为外部授权输入，不能把声明的权限集合视为跳过资源范围检查的许可。
- ContextAttributes 是不可变类型化属性袋：`<T> Optional<T> find(AttributeKey<T> key)` 与 `<T> ContextAttributes with(AttributeKey<T> key,T value)`；AttributeKey 字段为 `String name, Class<T> type`。允许值仅不可变标量/record/不可变集合，不承载原始请求、连接、凭据或 mutable entity。
- Snapshot 类型可由 scope 解析器消费 base 已有形状，但不能直接跨线程共享可变 UserSnapshot。向旧 SessionContext 桥接时新建副本并校验 ID 兼容性。

ServiceContextAccessor 由宿主注入。业务不必在每个方法增加 context 参数；低层显式端口可以接受上下文以暴露边界。无活动上下文的 `requireCurrent()` 抛 SRV080001；后台任务用受管任务入口创建服务主体上下文。

record 额外便利方法（实现为默认方法或同文件工厂，不新增 SecurityView 类型）：

```java
ServicePrincipal principal();          // 返回 security 组件
Optional<String> clientId();           // request.clientId()
Optional<String> tenantId();           // scope.tenantId()
Optional<String> workspaceId();
Optional<String> projectId();
```

`requestId()`、`deadline()`、`cancellation()` 使用 record 组件访问器。实践文 `security().principal()` 即 `principal()`。

## 3. Cancellation 与 Deadline

```java
public interface CancellationToken {
    boolean isCancelled();
    Optional<CancellationReason> reason();
    CancellationRegistration onCancel(Consumer<CancellationReason> listener);
}
public interface CancellationRegistration extends AutoCloseable {
    void close();
}
public enum CancellationReason {
    CLIENT_DISCONNECTED, DEADLINE_EXCEEDED, APPLICATION_CANCELLED, SERVER_SHUTDOWN
}
public interface Deadline {
    Duration remaining();
    boolean isExpired();
    boolean isUnlimited();
    Deadline shorten(Duration timeout);
}
public interface Ticker { long readNanos(); }
```

CancellationSource 是 runtime 内部写端，暴露 `boolean cancel(CancellationReason)` 和 `CancellationToken token()`。首次取消返回 true，后续 false；取消监听注册后若已取消，立即通知一次；关闭 registration 幂等，取消与注册竞态保证至多一次回调。回调在调用方线程执行且不得阻塞；一次回调异常记录后不妨碍其他回调，致命 Error 不吞掉。

Deadline 使用单调 Ticker；remaining 最小为零，shorten 不得延长当前截止时间，timeout 必须正数。跨服务传的是剩余 Duration 或约定的绝对时间，接收端扣除可信时间差并取本地上限，不传 `System.nanoTime()` 数值。默认不信任客户端自报的延长期限。

## 4. 执行、策略与拦截器

```java
public record InvocationContext(
        String invocationId,
        String operationId,
        ServiceContext service,
        OperationPolicy policy,
        ResourceRef resource
) {}
public enum ExecutionMode { NON_BLOCKING, BLOCKING, CPU_INTENSIVE }
public enum OutcomeType { SUCCEEDED, FAILED, CANCELLED, TIMED_OUT, REJECTED }
public record InvocationOutcome(
        OutcomeType type,
        String code,
        Instant finishedAt,
        Duration duration,
        long outputCount,
        long outputBytes
) {}
public interface ServiceInterceptor {
    String id();
    int order();
    CompletionStage<InvocationLease> enter(InvocationContext invocation);
}
public interface InvocationLease {
    CompletionStage<Void> finish(InvocationOutcome outcome);
}
public interface TransportCompletion {
    CompletionStage<InvocationOutcome> completion();
}
```

`enter` 成功后返回 lease；失败则不进入后续拦截器或业务。每个已取得 lease 都必须 finish，逆序执行且至多一次。lease 可以无状态，不为它强制创建实现继承树。终态通知失败不改写已完成业务结果，需被 runtime 记录/隔离；审计 REQUIRED 的前置保证不能通过终态回调补做。

OperationDescriptor 字段：`operationId, routeTemplate, transport, declaredResultType(java.lang.reflect.Type), executionMode`；运行时只存静态安全元数据。OperationPolicy 字段：`permissionKeys(Set), resourceResolverKey, rateLimitKey, bulkheadKey, circuitKey, timeout(Duration), audited(boolean), auditAction, auditResourceType, auditSnapshotKey, auditMode, responseProfile`。可选策略键为空表示未配置；注解引用不存在的键在启动时失败，不能退回关闭。

策略优先级：方法注解 > 类注解 > operationId 精确配置 > transport 默认。类和方法权限合并为 AND；`PublicAccess` 与非空权限同方法冲突时启动失败，不通过优先级取消权限。配置不得把已声明权限变成匿名。

注解为 RUNTIME + TYPE/METHOD（ServiceOperation 只 METHOD），不包含 Spring/CDI 注解：

| 注解 | 成员及默认 |
|---|---|
| ServiceOperation | value：稳定 operationId，必填；无注解时由路由模板+HTTP method派生 |
| RequiresPermission | value：String[] 必填；resource：解析器键，默认空 |
| PublicAccess | 无成员；只允许无鉴权的明确入口 |
| RateLimited / BulkheadProtected / CircuitProtected / TimeoutProtected | value：配置策略键，必填 |
| Execution | value：ExecutionMode，必填 |
| Traced | value：子 Span 名，必填；仅可选业务 Span，默认路径不要使用 |
| Audited | action 必填；resourceType 默认空（从资源解析器补全，否则启动失败）；snapshot 默认空；mode 默认 BEST_EFFORT |

框架 API 不要求业务自己调用 enter/finish。runtime 的 InvocationEngine 用如下三入口保持不同结果形态，实例是 framework adapter 的协作类型：

```java
// 三方法均自行创建 InvocationControl；不同完成点见运行时附录。
public <T> T invokeSync(InvocationContext context, Supplier<T> business);
public <T> CompletionStage<T> invokeAsync(
        InvocationContext context, Supplier<CompletionStage<T>> business);
public <T> Flow.Publisher<T> invokeStream(
        InvocationContext context, Supplier<Flow.Publisher<T>> business);
```

invokeSync 仅允许 worker/虚拟线程，异步前置检查以剩余 deadline 有界等待；NON_BLOCKING 不允许调用此入口。invokeAsync 在调度策略执行 supplier，不能在构造 stage 前隐式执行阻塞业务。invokeStream 懒执行，订阅时才进入策略和业务；订阅一次，多订阅拒绝。泛型、资源关闭与失败处理不能靠 `Object` 强转掩盖。

## 5. 安全 SPI

```java
public record AuthenticationResult(
        ServicePrincipal principal, ServiceScope scope, Instant expiresAt
) {}
public interface SecurityProvider {
    String id();
    CompletionStage<AuthenticationResult> authenticate(ServiceContext context);
}
public record PermissionCheck(
        String operationId, Set<String> permissions, ResourceRef resource
) {}
public record PermissionDecision(boolean allowed, String reasonCode) {}
public interface PermissionProvider {
    CompletionStage<PermissionDecision> authorize(
            ServicePrincipal principal, ServiceScope scope, PermissionCheck check);
}
```

SecurityProvider 接口接收的是预认证 context（ANONYMOUS），凭据由 provider 的宿主桥接在原生请求作用域安全提取；通用 runtime 不传明文凭据。该方法必须在原生请求仍有效时开始；需要异步验证时 provider 自行复制最小凭据并在完成后释放引用，严禁在 scope 消失后再读 ServletRequest。

expiresAt 可空仅代表非过期型认证机制（例如经宿主认证的 mTLS）；JWT/API Key 的有效期和吊销政策由 provider 给出。所有受保护操作必须有 provider；没有 provider 且存在受保护端点则启动失败。PermissionDecision 拒绝不带敏感业务信息，外层映射 NO_PERMISSION。未知 scope/resource 不得退化成全局权限。

## 6. Stream API

```java
public record StreamEvent<T>(
        String id, long sequence, String type, Instant timestamp, T data
) {}
public enum StreamState { CREATED, OPEN, COMPLETED, FAILED, CANCELLED }
public enum OverflowPolicy { REJECT, DROP_LATEST, DROP_OLDEST, CLOSE }
public enum EmitResult { ACCEPTED, DROPPED_LATEST, DROPPED_OLDEST }
public interface StreamChannel<T> {
    CompletionStage<EmitResult> emit(T value);
    Flow.Publisher<T> publisher();
    CompletionStage<Void> complete();
    CompletionStage<Void> fail(NexusException failure);
    CompletionStage<Void> cancel(CancellationReason reason);
}
public interface StreamSink<T> {
    String sessionId();
    StreamState state();
    boolean isCancelled();
    CompletionStage<EmitResult> emit(T data);
    CompletionStage<EmitResult> emit(String type, T data);
    CompletionStage<Void> complete();
    CompletionStage<Void> fail(NexusException failure);
    CompletionStage<Void> cancel(CancellationReason reason);
}
public interface StreamSession<T> extends StreamSink<T> {
    Instant createdAt();
    Instant lastActivityAt();
    StreamChannel<StreamEvent<T>> channel();
    Flow.Publisher<StreamEvent<T>> publisher();
}
```

StreamManager API：

```java
<T> StreamSink<T> open(Class<T> eventType);
<T> StreamSink<T> open(Type eventType);
Optional<StreamSnapshot> find(String sessionId);
<T> CompletionStage<EmitResult> emit(String sessionId, String type, T data);
CompletionStage<Void> fail(String sessionId, NexusException failure);
CompletionStage<Void> cancel(String sessionId, CancellationReason reason);
CompletionStage<Void> close();
```

`open` 自动捕获当前已认证 context 与事件 `Type`。返回值类型是 `StreamSink`；需要 publisher 时再转为 `StreamSession`（同一实例）。`emit/fail(sessionId, …)` 校验：当前 principal+scope 与创建时 owner/scope 一致，且 `data` 可赋给捕获 Type，否则 `AIO040006` / `AIO010001`。管理查询只返回不可变快照。无当前上下文时按 id 写入失败 `SRV080001`。

`emit(T data)` 默认 type 为 `message`。`isCancelled()` 反映绑定的 `CancellationToken`，业务无需轮询，但长循环可检查。

StreamSnapshot 字段为 `sessionId, ownerId, scope, state, createdAt, lastActivityAt, bufferedItems, bufferedBytes`；仅平台内部使用，无默认 REST 管理接口。

StreamChannelFactory 签名：`<T> StreamChannel<T> create(StreamConfig config, CancellationToken token, ToLongFunction<T> sizeEstimator)`；工厂不负责查身份，实例必须按运行时附录实现单订阅与有界背压。

`emit` 成功表示接受到有界队列，不表示对端收到；REJECT 以 SRV100003 失败，CLOSE 先终止通道再以同码失败；DROP_* 必须显式配置且返回丢弃结果。新 value 为 null → AIO010001。`complete/fail/cancel` 完成 stage 表示本地 drain/取消和资源回收完成，不保证远端应用消费。

Level 2 业务只使用 `StreamSink` 与 `StreamManager`。`StreamChannel` / `StreamChannelFactory` 属 SPI。业务可忽略 emit 返回值，但丢弃/拒绝始终有指标，关键事件生产者应处理异步结果。

## 7. WebSocket API

```java
public record WebSocketMessage<T>(
        String id, String type, String correlationId, long sequence,
        Instant timestamp, T data, Map<String, String> metadata
) {}
public record WebSocketContext(
        String connectionId, String sessionId, ServiceContext service
) {}
public record WebSocketClose(int code, String reason) {}
public interface WebSocketSession<I, O> {
    String connectionId();
    String sessionId();
    WebSocketContext context();
    Flow.Publisher<WebSocketMessage<I>> inbound();
    CompletionStage<Void> send(WebSocketMessage<O> message);
    CompletionStage<Void> close(WebSocketClose close);
    boolean isOpen();
}
public interface WebSocketHandler<I, O> {
    CompletionStage<Void> onOpen(WebSocketSession<I, O> session);
    CompletionStage<Void> onMessage(WebSocketSession<I, O> session, WebSocketMessage<I> message);
    CompletionStage<Void> onClose(WebSocketContext context, WebSocketClose close);
    CompletionStage<Void> onError(WebSocketContext context, NexusException failure);
}
public interface ReactiveWebSocketHandler<I, O> {
    Flow.Publisher<WebSocketMessage<O>> handle(
            WebSocketContext context, Flow.Publisher<WebSocketMessage<I>> inbound);
}
```

普通 Handler 的 onOpen/onClose/onError 由可选 `AbstractWebSocketHandler` 提供默认 completed stage；也可直接实现接口，框架不强制继承。onMessage 必须实现。一次连接选择 callback 或 reactive 一种模式，不同时消费 inbound。Reactive Handler 也不能绕开逐消息授权/限流，它收到的是已校验输入流。业务 Handler 禁止手写权限 if。

WebSocketCodec 接口：`<T> WebSocketMessage<T> decode(ByteBuffer frame,Type payloadType)`、`ByteBuffer encode(WebSocketMessage<?> message)`。FrameType TEXT/BINARY 由 MessageDescriptor 固定；一条消息只能用一种 frame 编码，碎片组装和总字节上限由容器执行，再按完整消息上限复核。

MessageDescriptor 字段：`type, inputType, outputType, permissionKeys, resourceResolverKey, executionMode`；Type 来自原生端点静态声明或注册 metadata，不从不可信消息传入任意 class 名。Message type 未注册返回 SRV120009。

WebSocketRegistry：

- `CompletionStage<Void> send(String connectionId,WebSocketMessage<?> message)`：内部 Codec 验证该连接允许的输出类型；未找到 AIO130005；检查调用主体/资源范围。
- `CompletionStage<BroadcastResult> sendToSession(String sessionId,WebSocketMessage<?> message)`：按 scope+realm+sessionId 查快照，再逐连接检查和有界发送；不在注册表锁内 I/O。
- `CompletionStage<Void> close(String connectionId,WebSocketClose close)`：本地幂等；不存在返回完成，未授权仍失败。
- `List<ConnectionSnapshot> listByPrincipal(ServicePrincipal principal,ServiceScope scope)`：有数量限制，仅内部查询。

`WebSocketService` 是业务 Level 2 门面，方法与上表 send/sendToSession/close 相同；`WebSocketRegistry` 额外包含注册/注销与 `listByPrincipal`，仅 adapter/runtime 使用。业务注入 `WebSocketService`，不依赖 Spring/Quarkus Session 类型。

BroadcastResult 字段 `attemptedCount, sentCount, rejectedCount, unavailableCount`；部分失败不伪装全部成功，默认不返回其他主体的 connectionId。

## 8. Binary / File API

```java
public interface BinarySource extends AutoCloseable {
    Flow.Publisher<ByteBuffer> publisher();
    void close();
}
public record ByteRange(long startInclusive, long endInclusive) {}
public record ContentMetadata(
        String resourceId, long size, String contentType, String etag,
        Instant lastModified, Map<String, String> checksums, boolean rangeSupported
) {}
public interface ResourceContentReader {
    CompletionStage<ContentMetadata> metadata(String resourceId);
    CompletionStage<BinarySource> open(String resourceId, ByteRange range);
}
public record UploadResource(
        String fieldName, String filename, String contentType,
        long size, BinarySource content
) {}
public record DownloadResource(
        String resourceId, String filename, ContentMetadata metadata,
        Supplier<CompletionStage<BinarySource>> content
) {}
```

- UploadResource.size 为 -1 表示未知长度，其余非负；实时计数是强约束，不能只相信 Content-Length。
- `ResourceContentReader.open` 的 range 可空代表全量，非空要求 rangeSupported；调用前必须由业务确认 resourceId 归属，reader 不提供跨租户授权。
- DownloadResource 的 supplier 是延迟全量读取入口；Range 下载使用注入的 ResourceContentReader，不能调用全量 supplier 后跳过若干字节假装随机读取。
- BinarySource 单订阅。生产方交付只读 ByteBuffer，其字节在信号后仍不可被池复用破坏；适配器复制有界块或管理引用计数，但引用计数类型不暴露公共 API。
- 消费/取消后 adapter 调用 close，重复关闭幂等；关闭失败翻译为 NexusException 并保留原因，不覆盖更早的主要失败。
- UploadResource 只在调用与传输所有权覆盖的生命周期内有效，业务持久化完成前不可返还成功；不得跨请求缓存原始临时文件。
- DownloadRequest 字段：`method, ifMatch, ifNoneMatch, ifModifiedSince, ifUnmodifiedSince, ifRange, range`；均由 header adapter 校验后提供。
- DownloadPlan 字段：`httpStatus, contentLength, contentRange, headers, readRange, bodyRequired`，含 200/206/304/412/416 的固定处理，见运行时附录。
- MalwareScanner：`CompletionStage<ScanResult> scan(UploadResource)`；ScanResult 为 CLEAN/INFECTED/UNAVAILABLE，不把无法扫描当作安全。

## 9. 审计与观测 SPI

```java
public record AuditSnapshot(Map<String, Object> before, Map<String, Object> after) {}
public record AuditEvent(
        String eventId, Instant timestamp, ServicePrincipal principal,
        ServiceScope scope, String action, String resourceType, String resourceId,
        String requestId, String traceId, String result,
        Map<String, Object> before, Map<String, Object> after,
        Map<String, Object> details
) {}
public interface AuditStorage {
    CompletionStage<Void> append(AuditEvent event);
    CompletionStage<Void> flush(Duration timeout);
}
public interface TransactionalAuditStorage {
    void appendInCurrentTransaction(AuditEvent event);
}
public interface AuditSnapshotProvider {
    AuditSnapshot capture(InvocationContext context, Object input, Object result);
}
public interface CommitObserver {
    CommitState currentState();
    void afterCompletion(Consumer<CommitState> callback);
}
public enum CommitState { NO_TRANSACTION, ACTIVE, COMMITTED, ROLLED_BACK }
```

before/after/details 必须深度转为安全 JSON 值树并设大小/深度上限，禁止保留实体引用、凭据或任意 Object。`Object` 仅允许在内部快照抽取边界使用，输出需白名单序列化。resourceId 可空表示创建前尚未产生；终态创建成功由 snapshot/resource resolver 提供真实 ID。无 snapshot Provider 时 before/after 为空，不能通过反射自动读取全部对象。

AuditStorage.append 完成意味着该实现声明的持久化确认，非仅入 JVM 队列；eventId 去重。TransactionalAuditStorage 不直接依赖前者，其契约是参与宿主当前事务；必须在业务提交前写入。普通 JVM 队列只能 BEST_EFFORT。审计 outcome 包括 SUCCEEDED/FAILED/REJECTED/CANCELLED/TIMED_OUT/UNKNOWN，不包含 token delta 或 heartbeat。

MetricsProvider：`InvocationObservation begin(InvocationContext)`；InvocationObservation 提供 `firstOutput(Duration)`、`output(long count,long bytes)`、`finish(InvocationOutcome)`。指标枚举与 label 白名单固定在实现配置，避免开放任意字符串的高基数计数器 API。

业务自定义指标走 `ServiceMeters`（contract.observation），不暴露 Micrometer：

```java
public interface ServiceMeters {
    void increment(String name);
    void increment(String name, Map<String, String> tags);
    void record(String name, Duration duration);
}
```

`name` 必须在 `service.observability.business-meters` 注册，否则启动失败或调用失败 `AIO080002`。tag 键必须属于该名称的白名单，禁止 principalId/resourceId/requestId/IP/path。未启用 metrics 时这些调用为空操作。

TraceProvider：`TraceHandle start(InvocationContext, TraceSnapshot parent)`；TraceHandle 提供 `TraceSnapshot snapshot()`、`finish(InvocationOutcome)`。提取和注入 W3C Context 在框架/OTel 边界完成，ServiceContext 不承载 OTel Scope 对象。可选 `@Traced` 创建子 Span，由适配器在拦截器中调用 TraceProvider，业务不持有 Tracer。

## 10. 治理 SPI

```java
public record RateLimitRequest(String policyKey, List<String> dimensions, int cost) {}
public record RateLimitDecision(boolean allowed, Duration retryAfter) {}
public interface RateLimitProvider {
    RateLimitDecision acquire(RateLimitRequest request);
}
public interface BulkheadProvider {
    Optional<BulkheadPermit> tryAcquire(String policyKey);
}
public interface BulkheadPermit extends AutoCloseable { void close(); }
public interface CircuitBreakerProvider {
    CircuitPermit acquire(String policyKey);
}
public interface CircuitPermit {
    void finish(InvocationOutcome outcome);
}
```

限流不等待，拒绝 retryAfter 非负；cost 正数。复合维度的原子扣减由 LocalTokenBucketProvider 统一实现，不能部分扣减后当作全部成功。Bulkhead.tryAcquire 无许可返回 empty；策略不存在抛配置错误。Circuit.acquire OPEN 拒绝 SRV060005；半开试探 permit 必须在真正执行结束后反馈，调用方取消不被计为成功。

## 11. 错误表示与状态码矩阵

```java
public record ServiceError(
        String code, int httpStatus, String message, boolean retryable,
        Map<String, Object> details
) {}
public record ProblemDetailVo(
        URI type, String title, int status, String detail, String instance,
        String code, String requestId, String traceId,
        boolean retryable, Map<String, Object> details
) {}
```

默认 type 使用稳定 `urn:innospots:problem:<code>`，instance 为 `urn:request:<requestId>`。title/detail 取安全双语状态文本，不直接输出 exception.getMessage()、底层路径或供应商原文。HTTP 状态位于响应行且与 body.status 一致；Request/Trace ID 是明确定义的关联字段，不混入稳定 message。

Problem profile 遵循 [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html) 的标准成员和扩展字段形式；legacy profile 继续 R 的字段，关联 ID 放响应头，错误附加数据仅在明确启用时放 R.data，保持旧接口兼容。

以下 SRV 为服务框架命名空间，由 `ServiceStatusCode` 与契约测试登记；local 在 SRV 内唯一，不得与 AIO/PLG 抢 module 段。

| 场景/常量 | 完整码 | category / local | HTTP | retryable | 英文 / 中文稳定消息 | 拒绝或翻译边界 |
|---|---|---|---|---|---|---|
| INVALID_PARAMETER | AIO010001 | 01 / 0001 | 400 | false | Invalid parameter / 参数无效 | 形状校验 |
| RESOURCE_NOT_FOUND | AIO130005 | 13 / 0005 | 404 | false | Resource not found / 资源不存在 | 文件/连接查找 |
| NO_PERMISSION | AIO040006 | 04 / 0006 | 403 | false | No permission / 没有权限 | 授权器 |
| AUTHENTICATION_FAILED | AIO040007 | 04 / 0007 | 401 | false | Authentication failed / 认证失败 | 安全适配 |
| LIMIT_EXCEEDED | AIO100012 | 10 / 0012 | 429 | true | Limit exceeded / 超出限制 | 本地速率限制 |
| CONFIG_ERROR | AIO080002 | 08 / 0002 | 500 | false | Configuration error / 配置错误 | 启动/策略解析 |
| SERIALIZATION_FAILED | AIO030003 | 03 / 0003 | 500 | false | Serialization failed / 序列化失败 | 服务端输出编码 |
| SYSTEM_ERROR | AIO030017 | 03 / 0017 | 500 | false | System error / 系统错误 | 未知异常外边界 |
| CONTEXT_UNAVAILABLE | SRV080001 | 08 / 0001 | 500 | false | Service context unavailable / 服务上下文不可用 | context accessor |
| DEADLINE_EXCEEDED | SRV100002 | 10 / 0002 | 504 | false | Deadline exceeded / 执行已超时 | deadline 控制器 |
| BUFFER_OVERFLOW | SRV100003 | 10 / 0003 | 503 | false | Buffer capacity exceeded / 缓冲容量已满 | channel/发送队列 |
| CAPACITY_EXHAUSTED | SRV100004 | 10 / 0004 | 503 | true | Service capacity exhausted / 服务容量不足 | bulkhead/连接/session/executor |
| CIRCUIT_OPEN | SRV060005 | 06 / 0005 | 503 | true | Downstream temporarily unavailable / 下游暂不可用 | circuit permit |
| PAYLOAD_TOO_LARGE | SRV010006 | 01 / 0006 | 413 | false | Payload too large / 数据超过大小限制 | header/body/file/frame |
| MEDIA_TYPE_REJECTED | SRV010007 | 01 / 0007 | 415 | false | Media type not allowed / 媒体类型不允许 | 内容类型验证 |
| LIFECYCLE_CLOSED | SRV120008 | 12 / 0008 | 409 | false | Session is closed / 会话已关闭 | 终态 emit/send |
| MESSAGE_TYPE_UNKNOWN | SRV120009 | 12 / 0009 | 400 | false | Unknown message type / 未知消息类型 | WS decoder |
| RANGE_NOT_SATISFIABLE | SRV150010 | 15 / 0010 | 416 | false | Requested range unavailable / 请求范围不可用 | download planner |
| UPLOAD_UNSAFE | SRV040011 | 04 / 0011 | 422 | false | Upload rejected by security policy / 上传被安全策略拒绝 | 上传校验/病毒扫描 |
| AUDIT_UNAVAILABLE | SRV090012 | 09 / 0012 | 503 | false | Required audit unavailable / 必需审计不可用 | 强审计入口 |
| IDEMPOTENCY_CONFLICT | SRV050013 | 05 / 0013 | 409 | false | Idempotency key conflict / 幂等键冲突 | 幂等协调器 |
| OPERATION_CANCELLED | SRV120014 | 12 / 0014 | 无在线响应 | false | Operation cancelled / 操作已取消 | token/上游取消 |
| CONTENT_READ_FAILED | SRV150015 | 15 / 0015 | 502 | false | Resource read failed / 资源读取失败 | 存储流读取 |
| SCANNER_UNAVAILABLE | SRV060016 | 06 / 0016 | 503 | false | Upload scanner unavailable / 上传扫描不可用 | scanner 适配 |
| CONTENT_CAPABILITY_MISSING | SRV150017 | 15 / 0017 | 501 | false | Streaming storage capability unavailable / 存储流式能力不可用 | 旧 ResourceStore 桥接 |
| DOWNSTREAM_FAILED | SRV060018 | 06 / 0018 | 502 | false | Downstream call failed / 下游调用失败 | 下游提供方翻译 |
| PRECONDITION_FAILED | SRV050019 | 05 / 0019 | 412 | false | Resource precondition failed / 资源前置条件不满足 | If-Match 等 |

`ServiceStatusCode` 枚举必须按下行给出稳定 advice，不得动态拼接用户内容。取消码的 `httpStatusCode()` 可为 499 供内部归类，适配器不得向已断开客户端发送 499。

| 常量 | adviceEn | adviceZh |
|---|---|---|
| CONTEXT_UNAVAILABLE | Contact the service operator | 请联系服务管理员 |
| DEADLINE_EXCEEDED | Retry after reducing work or increasing timeout | 请降低工作量或增大超时后重试 |
| BUFFER_OVERFLOW | Slow down producers or increase bounded buffer | 请降低生产速率或增大有界缓冲 |
| CAPACITY_EXHAUSTED | Retry after the advertised delay | 请按提示稍后重试 |
| CIRCUIT_OPEN | Retry after the advertised delay | 请按提示稍后重试 |
| PAYLOAD_TOO_LARGE | Check the request size limits | 请检查请求大小限制 |
| MEDIA_TYPE_REJECTED | Check the request | 请检查请求 |
| LIFECYCLE_CLOSED | Open a new session | 请新建会话 |
| MESSAGE_TYPE_UNKNOWN | Check the request | 请检查请求 |
| RANGE_NOT_SATISFIABLE | Check the requested range | 请检查请求范围 |
| UPLOAD_UNSAFE | Check access credentials and upload policy | 请检查访问凭据与上传策略 |
| AUDIT_UNAVAILABLE | Contact the service operator | 请联系服务管理员 |
| IDEMPOTENCY_CONFLICT | Check the idempotency key and retry policy | 请检查幂等键与重试策略 |
| OPERATION_CANCELLED | Repeat the operation if still required | 如仍需要请重新发起操作 |
| CONTENT_READ_FAILED | Contact the service operator | 请联系服务管理员 |
| SCANNER_UNAVAILABLE | Retry after the advertised delay | 请按提示稍后重试 |
| CONTENT_CAPABILITY_MISSING | Contact the service operator | 请联系服务管理员 |
| DOWNSTREAM_FAILED | Retry after the advertised delay | 请按提示稍后重试 |
| PRECONDITION_FAILED | Reload the resource and retry | 请重新加载资源后重试 |

协议库验证错误使用表中码；底层 Flow.request(n<=0) 必须依 JDK Flow 规范向 subscriber 发 IllegalArgumentException，这属于协议规则，不是业务失败契约，不能为了统一异常而破坏 Flow。业务异常进入流时仍为 NexusException。

ServiceErrorCatalog 只注册 NexusStatusCode、ServiceStatusCode 及宿主明确注册的目录；完整码重复或非法形状启动失败。未知原始码映射 SYSTEM_ERROR，不按字符串前缀猜 HTTP 状态。客户端取消、403、429、不执行的容量拒绝不计入下游失败率；retryable 只表示条件具备重试可能，不授权框架自动重试写操作。

## 12. 补全契约骨架

实现时按下列形状落文件，字段语义见上文各节。

```java
public enum ResponseProfile { LEGACY, PROBLEM }
public enum AuditMode { BEST_EFFORT, REQUIRED }

public record OperationPolicy(
        Set<String> permissionKeys,
        String resourceResolverKey,
        String rateLimitKey,
        String bulkheadKey,
        String circuitKey,
        Duration timeout,
        boolean audited,
        String auditAction,
        String auditResourceType,
        String auditSnapshotKey,
        AuditMode auditMode,
        ResponseProfile responseProfile
) {}

public record OperationDescriptor(
        String operationId,
        String routeTemplate,
        String transport,
        Type declaredResultType,
        ExecutionMode executionMode
) {}

public enum StreamEventType {
    STREAM_OPEN("stream.open"),
    MESSAGE("message"),
    MESSAGE_DELTA("message.delta"),
    PROGRESS("progress"),
    STATUS("status"),
    ERROR("error"),
    STREAM_COMPLETED("stream.completed"),
    STREAM_CANCELLED("stream.cancelled"),
    HEARTBEAT("heartbeat");
    private final String wireName;
}

public record StreamConfig(
        int maxSessions,
        int bufferSize,
        long bufferBytes,
        long globalBufferBytes,
        long maxEventBytes,
        OverflowPolicy overflow,
        Duration subscribeTimeout,
        Duration ttl,
        Duration idleTimeout,
        Duration heartbeat,
        Duration terminalWriteTimeout
) {}

public record WebSocketRuntimeConfig(
        int maxConnections,
        int maxConnectionsPerUser,
        long maxMessageBytes,
        int inboundBufferSize,
        int outboundBufferSize,
        long inboundBufferBytes,
        long outboundBufferBytes,
        long globalInboundBufferBytes,
        long globalOutboundBufferBytes,
        OverflowPolicy overflow,
        Duration pingInterval,
        Duration pongTimeout,
        Duration idleTimeout,
        Duration messageTimeout,
        boolean inboundGovernanceEnabled
) {}

// MessageDescriptor 另含可选治理键：rateLimitKey、bulkheadKey、circuitKey、timeoutPolicyKey

public final class InterceptorOrders {
    public static final int CONTROL = 0;
    public static final int DEADLINE = 10;
    public static final int AUTHN = 20;
    public static final int AUTHZ = 30;
    public static final int AUDIT = 40;
    public static final int IDEMPOTENCY = 50;
    public static final int RATE_LIMIT = 60;
    public static final int BULKHEAD = 70;
    public static final int CIRCUIT = 80;
}

public final class InterceptorIds {
    public static final String CONTROL = "runtime.control";
    public static final String DEADLINE = "runtime.deadline";
    public static final String AUTHN = "runtime.authn";
    public static final String AUTHZ = "runtime.authz";
    public static final String AUDIT = "runtime.audit";
    public static final String IDEMPOTENCY = "runtime.idempotency";
    public static final String RATE_LIMIT = "governance.ratelimit";
    public static final String BULKHEAD = "governance.bulkhead";
    public static final String CIRCUIT = "governance.circuit";
}
```

Header 常量 `StandardHeaders` 落在 `http.header`，不放 contract。`InterceptorOrders` / `InterceptorIds` 放 `contract.invocation`。治理拦截器实现类在 governance 模块，由适配器按 order 注入 `InvocationEngine`，runtime 不依赖 governance artifact。
