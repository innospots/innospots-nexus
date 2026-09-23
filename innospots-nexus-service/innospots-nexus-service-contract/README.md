# innospots-nexus-service-contract

## 模块简介

服务框架的**契约层**：定义调用上下文、生命周期、策略注解与扩展 SPI。  
业务模块通常**只直接依赖本模块**；运行时编排、HTTP/WebSocket/流式传输由 `innospots-nexus-service-runtime` 及 Spring/Quarkus adapter 传递引入。

本模块 **middleware-free**：不得依赖 Spring、Quarkus、Servlet、Reactor 等运行时基础设施。

## 版本与阶段说明

| 项 | 说明 |
|----|------|
| **工程版本** | `1.0.0-SNAPSHOT`（根 POM `${revision}`，由 `innospots-nexus-bom` 对齐） |
| **本模块阶段** | **M1 契约层**：公共类型、注解与 SPI 已随服务框架 M1–M9 交付；**行为**由 `service-runtime` + adapter 实现，非本 JAR 自带 |
| **文档状态快照** | 2026-09-16；实现细节以仓库源码与 [service-future-work.md](../docs/service-future-work.md) 为准 |
| **适配器** | Spring / Quarkus adapter 已具备 HTTP 黑盒场景；**kernel 尚未提供**默认 `PermissionProvider` / `AuditStorage` 生产实现 |

### 实现状态图例（下文通用）

| 标记 | 含义 |
|------|------|
| ✅ | 已在对应层落地并有单测或 adapter 场景覆盖 |
| 🔶 | 部分落地：契约或 runtime 已有，默认装配 / 生产集成 / 注解解析仍缺一环 |
| 📋 | **仅契约**：类型与注解已定义，运行时或 adapter **尚未接线** |
| ❌ | 明确未做或不在当前框架范围 |

### 分层对照（读表时先看列名）

| 列 | 含义 |
|----|------|
| **契约** | `innospots-nexus-service-contract` 类型/注解是否齐全 |
| **Runtime** | `innospots-nexus-service-runtime`（及 governance/observability 库）是否执行 |
| **Spring 默认** | `innospots-nexus-spring-service` 的 `ServiceCoreConfiguration` 等是否**开箱注册** |
| **生产集成** | kernel/platform/运维侧是否已有默认可用实现（非 test fixture） |

### 能力总表（实现状态）

| 能力域 | 契约 | Runtime | Spring 默认 | 生产集成 |
|--------|------|---------|-------------|----------|
| 请求上下文 `context` | ✅ | ✅ | ✅ Filter 安装上下文 | 🔶 认证主体多靠宿主 `SecurityProvider` 填充 |
| 调用链 `invocation` | ✅ | ✅ | ✅ `InvocationEngine` | ✅ |
| 策略解析 `policy` | ✅ | 🔶 | ✅ `AnnotationPolicyResolver` | 🔶 见各注解子项 |
| `@ServiceOperation` | ✅ | 🔶 | ✅ 解析进 `OperationPolicy` | ✅ |
| `@RequiresPermission` / `@PublicAccess` | ✅ | ✅ | 🔶 须 `ServiceInvocationBridge` | ❌ kernel 未注册 `PermissionProvider` |
| `@RateLimited` | ✅ | ✅ | 🔶 须 Bridge + 默认仅本地令牌桶 | 🔶 单 JVM，无分布式限流 |
| `@TimeoutProtected` | ✅ | 📋 | 🔶 超时靠 `GovernanceConfig.timeouts` 的 **operationId 键**，**未**从注解解析 | 🔶 |
| `@BulkheadProtected` / `@CircuitProtected` | ✅ | ✅ 拦截器在 governance | ❌ Spring 默认**未**注册舱壁/熔断拦截器 | 🔶 需自行 `addInterceptor` |
| `@Traced` / `@Execution` | ✅ | 📋 | 📋 | 📋 |
| 审计 `@Audited` | ✅ | ✅ `AuditInterceptor` | 🔶 默认 `auditEnabled=false`，noop 存储 | ❌ 无持久化 `AuditStorage` |
| 审计快照 `AuditSnapshotProvider` | ✅ | 📋 | 📋 | ❌ |
| 事务审计 `TransactionalAuditStorage` | ✅ | 🔶 运行时语义 | ❌ | ❌ 业务 outbox 在 kernel/platform |
| 治理 SPI `governance` | ✅ | ✅ 本地 + 可选 Redis 模块 | 🔶 仅限流+超时 | 🔶 Redis 需 `store=redis`（Spring） |
| 指标 `observation` | ✅ | ✅ | 🔶 访问日志+MDC；指标 Bean 需另配 | 🔶 |
| 追踪 `trace` | ✅ | ✅ | 🔶 默认 `NoOpTraceProvider` | 🔶 OTel 实现已有，非默认 |
| 取消 `cancellation` | ✅ | ✅ | ✅ 请求结束取消 | ✅ |
| 截止时间 `time` | ✅ | ✅ | ✅ `DeadlineScheduler` | ✅ |
| 幂等（契约外，runtime） | — | 🔶 内存单 JVM | ❌ 默认未开 `idempotencyEnabled` | 🔶 |
| 错误目录 `error` / 状态码 `status` | ✅ | ✅ 映射与测试 | ✅ HTTP 错误映射 | ✅ |

> 注解生效的**硬条件**（Spring）：`@RequiresPermission`、`@RateLimited`、`@Audited` 等写在**被 `ServiceInvocationBridge.invoke(...)` / `invokeStream(...)` 调用的类型与方法**上；仅在 Controller 方法上贴注解**不会**进入 `InvocationEngine`。见 [spring-service HTTP 接入](../../../innospots-nexus-spring/innospots-nexus-spring-service/docs/http-api-integration.md)。

## 何时使用

| 场景 | 是否依赖本模块 |
|------|----------------|
| 写 Controller/Service 并加 `@RequiresPermission`、`@RateLimited` 等 | 是 |
| 实现 `PermissionProvider`、`AuditStorage`、`RateLimitProvider` | 是 |
| 在应用服务中读取 `ServiceContext`、声明 `@Audited` | 是 |
| 直接操作 HTTP 响应、WebSocket 帧、SSE 写出 | 否（用 adapter + `service-http` / `service-stream` 等） |

## 能力一览

| 能力域 | 包路径 | 开发者常用入口 | 平台需实现的 SPI |
|--------|--------|----------------|------------------|
| 请求上下文 | `context` | `ServiceContextAccessor` | runtime 提供线程绑定实现 |
| 调用与拦截 | `invocation` | `ServiceInterceptor`（扩展） | runtime `InvocationEngine` |
| 操作策略 | `policy` / `policy.annotation` | `@ServiceOperation`、治理类注解 | `PolicyCatalog`、adapter 配置 |
| 认证授权 | `security` / `security.annotation` | `@RequiresPermission`、`@PublicAccess` | `SecurityProvider`、`PermissionProvider` |
| 技术审计 | `audit` / `audit.annotation` | `@Audited` | `AuditStorage`、`AuditSnapshotProvider` |
| 限流熔断舱壁 | `governance` | `@RateLimited` 等（策略键） | `RateLimitProvider`、`CircuitBreakerProvider`、`BulkheadProvider` |
| 可观测指标 | `observation` | （多为自动采集） | `MetricsProvider` |
| 分布式追踪 | `trace` | `@Traced` | `TraceProvider` |
| 取消与超时 | `cancellation`、`time` | `CancellationToken`、`Deadline` | runtime 与 transport 协作 |
| 服务错误目录 | `error` | `ServiceError`、`ServiceErrorCatalog` | 各模块注册错误条目 |
| 服务状态码 | `status` | `ServiceStatusCode` | 与 `NexusStatusCode` 对齐的 SRV 段 |

---

## 功能说明（按包）

### 1. `context` — 请求上下文

**作用**：在一次服务调用（通常对应一次 HTTP/WS 请求或受管后台任务）内，聚合请求标识、路由元数据、安全主体、租户作用域、追踪快照、取消令牌、截止时间以及类型化扩展属性。

| 类型 | 说明 |
|------|------|
| `ServiceContext` | 不可变 record；各组件非空（无 deadline 时为「无限」占位，非 null） |
| `RequestMetadata` | 方法、路径、路由模板、Header 快照、客户端地址等 |
| `ServiceContextAccessor` | `current()` / `requireCurrent()`；无上下文时 `requireCurrent` 抛服务状态码 |
| `ContextAttributes` / `AttributeKey` | 类型安全的扩展属性袋，禁止塞入可变实体或凭据 |
| `ServiceScope` | tenant / workspace / project 作用域（定义在 `security` 包，挂在上下文中） |

**实现状态**：契约 ✅ · Runtime ✅（`ThreadBoundServiceContext`）· Spring 默认 ✅（`ServiceServletFilter` / WebFlux 对等）· 生产集成 🔶（`ServicePrincipal` 内容取决于是否注册 `SecurityProvider`）。

**用法要点**：

- 通过构造注入 `ServiceContextAccessor`（adapter 将其实现为对 `ThreadBoundServiceContext` 的桥接），在 **Application Service** 内读取 `requestId`、租户作用域等。
- **不要**从 Controller 层层下传 `ServiceContext` 到 Repository；后台任务须走受管入口创建上下文，否则 `requireCurrent()` 抛 `ServiceStatusCode` 相关错误。
- `ContextAttributes` 只放不可变、非敏感扩展；**禁止**把原始请求、连接或 JWT 塞进属性袋。
- Domain 层不得依赖 `ServiceContext` 或本包类型。

---

### 2. `invocation` — 调用链与拦截器

**作用**：描述单次「服务操作」从进入到结束的控制面：执行模式、拦截器顺序、调用结果类型、租约与传输层完成信号。runtime 的 `InvocationEngine` 按此处契约驱动 Filter/Interceptor 链。

| 类型 | 说明 |
|------|------|
| `InvocationContext` | 当前调用的操作描述、策略、上下文引用 |
| `ServiceInterceptor` | 扩展点：在授权、审计、治理前后插入逻辑 |
| `InterceptorOrders` / `InterceptorIds` | 拦截器排序与稳定标识 |
| `ExecutionMode` | 同步/异步等执行语义 |
| `InvocationOutcome` / `OutcomeType` | 成功、业务失败、系统失败等分类 |
| `InvocationLease` | 调用租约（与取消、超时协作） |
| `TransportCompletion` | 传输层（HTTP 等）完成回调契约 |

**实现状态**：契约 ✅ · Runtime ✅（`InvocationEngine`、内置拦截器链）· Spring 默认 ✅ · 生产集成 ✅。

**用法要点**：

- 业务代码一般**不**直接实现 `ServiceInterceptor`，除非做平台级扩展；通过 `ServiceRuntime.Builder#addInterceptor` 注册（Spring 可对 `ServiceRuntime` Bean 定制）。
- 进入引擎的每次调用须构造 `InvocationContext`（adapter 的 `ServiceInvocationBridge` 已封装：解析注解 + 绑定当前 `ServiceContext`）。
- `InterceptorOrders` 约定治理、安全、审计相对顺序；自定义拦截器应使用文档化 order，避免覆盖内置语义。
- `TransportCompletion` 由 HTTP/流式 adapter 在响应写完或断连时触发，业务通常不直接实现。

---

### 3. `policy` — 操作策略与描述符

**作用**：把「这个方法是什么操作、用什么响应形态、审计是否强制」等从注解与配置汇总为不可变策略对象，供 runtime 与治理模块消费。

| 类型 | 说明 |
|------|------|
| `OperationPolicy` | 聚合后的操作策略（限流键、超时键、审计模式等） |
| `OperationDescriptor` | 稳定操作 ID、显示名、资源类型等元数据 |
| `PolicyCatalog` | 按策略键查找具体参数（QPS、超时毫秒等） |
| `ResponseProfile` | 错误响应外形（如 LEGACY / PROBLEM_DETAIL） |
| `AuditMode` | 审计必填、可选、关闭等 |

**注解包 `policy.annotation`**（标在 **方法** 或 **类** 上，具体见各注解 `@Target`）：

| 注解 | 说明 |
|------|------|
| `@ServiceOperation("order.create")` | 声明稳定操作 ID；无注解时由路由+方法名派生 |
| `@RateLimited("order.create")` | 引用 `PolicyCatalog` 中的限流策略键 |
| `@TimeoutProtected("downstream.payment")` | 引用超时策略键 |
| `@BulkheadProtected("...")` | 引用舱壁并发策略键 |
| `@CircuitProtected("...")` | 引用断路器策略键 |
| `@Execution` | 执行语义补充（与 `ExecutionMode` 对齐） |
| `@Traced` | 声明需要追踪跨度（与 `trace` 包协作） |

**实现状态（注解解析）**：

| 注解 | 契约 | `AnnotationPolicyResolver` | 拦截器/生效 |
|------|------|------------------------------|-------------|
| `@ServiceOperation` | ✅ | 🔶（操作 ID 多由 Bridge 的 `operationId` 参数传入） | ✅ |
| `@RequiresPermission` / `@PublicAccess` | ✅ | ✅ | ✅ 须注册 `PermissionProvider` + Bridge |
| `@RateLimited` | ✅ | ✅ | ✅ 须 Bridge + `RateLimitInterceptor` |
| `@BulkheadProtected` / `@CircuitProtected` | ✅ | ✅ 写入 `OperationPolicy` 键 | 🔶 须自行注册 governance 拦截器 |
| `@TimeoutProtected` | ✅ | 📋 **未**解析进 `OperationPolicy` | 🔶 用 `GovernanceConfig.timeouts` 的 operationId |
| `@Audited` | ✅ | ✅ | 🔶 见 audit 节 |
| `@Traced` / `@Execution` | ✅ | 📋 | 📋 |

**用法要点**：

- 注解上的字符串是**策略键名**（或权限键），不是 `"100/min"` 字面量；QPS、超时毫秒、并发度在 `GovernanceConfig`（Java Bean 或后续配置绑定）中维护。
- 契约层**没有** `application.yml` 开关；Spring 侧见 `innospots-nexus-spring-service` 与 [治理接入](../../../innospots-nexus-spring/innospots-nexus-spring-service/docs/governance-integration.md)。
- `OperationPolicy` 中某键为 **null** 表示未启用该能力（例如未配限流键则不限流）。
- `ResponseProfile` 影响 HTTP 错误体外形，在 adapter 的 `HttpErrorMapper` 与 `service.response-profile` 配置中生效。

---

### 4. `security` — 认证与授权

**作用**：定义调用方身份、权限判定输入输出，以及认证/授权 SPI。adapter 在请求入口填充 `ServicePrincipal`，在拦截器阶段调用 `PermissionProvider`。

| 类型 | 说明 |
|------|------|
| `ServicePrincipal` | 主体 ID、类型、realm、角色、权限快照、扩展属性 |
| `PrincipalType` | USER / APPLICATION / SERVICE / API_KEY / ANONYMOUS |
| `AuthenticationResult` | 认证阶段结果 |
| `SecurityProvider` | 认证 SPI |
| `PermissionProvider` | 授权 SPI：根据 `PermissionCheck` 返回 `PermissionDecision` |
| `PermissionCheck` | 权限键、资源引用、当前上下文 |
| `ResourceRef` | 被访问资源的类型、ID、作用域 |

**注解包 `security.annotation`**：

| 注解 | 说明 |
|------|------|
| `@RequiresPermission("order.read")` | 类型级与方法级可叠加（AND）；可指定 `resource` 解析器键 |
| `@PublicAccess` | 显式跳过权限检查（仍可有认证） |

**实现状态**：契约 ✅ · Runtime ✅（`AuthenticationCoordinator`、`AuthorizationInterceptor`）· Spring 默认 🔶（`ObjectProvider` 可选注册 Provider）· 生产集成 ❌（**无** kernel 默认 Bean；仅 adapter-test 的 `HostSecurityProvider` / `HostPermissionProvider`）。

**用法要点**：

- 权限注解应标在 **被 Bridge 调用的 Service / 应用服务方法**（与 Controller 解耦）；Controller 只负责 `bridge.invoke(service, method, operationId, () -> ...)`。
- 类型级与方法级 `@RequiresPermission` **AND** 组合；`@PublicAccess` 与 `@RequiresPermission` 不可共存（解析期 `CONFIG_ERROR`）。
- `SecurityProvider` 负责把凭据变为 `ServicePrincipal`；`PermissionProvider` 只做授权判定，**不**替代 kernel 的权限模型实现。
- `resource` 解析器键预留扩展；默认使用操作级 `ResourceRef`（Bridge 当前使用 adapter 占位资源）。
- Domain / Repository **禁止**依赖本包注解或类型。

---

### 5. `audit` — 技术审计

**作用**：记录「谁在何时对什么资源做了什么」的技术审计事件契约。**不**承担业务审计查询与报表（归属 kernel/platform）。

| 类型 | 说明 |
|------|------|
| `AuditEvent` | 审计事件载荷 |
| `AuditStorage` | 持久化 SPI |
| `TransactionalAuditStorage` | 与业务事务协作的存储扩展 |
| `AuditSnapshot` / `AuditSnapshotProvider` | 操作前后状态快照 |
| `CommitObserver` / `CommitState` | 与事务提交阶段对齐的审计提交语义 |

**注解包 `audit.annotation`**：

| 注解 | 说明 |
|------|------|
| `@Audited(action = "order.create", mode = AuditMode.REQUIRED)` | 标在 **Application Service** 方法/类；可配 `resourceType`、`snapshot` 键 |

**实现状态**：契约 ✅ · Runtime ✅（`AuditInterceptor`、`AuditDispatcher`、队列）· Spring 默认 🔶（`ServiceRuntime` 默认 `auditEnabled=false` + noop 存储）· 生产集成 ❌（持久化 `AuditStorage`、kernel 审计表未接）。

**用法要点**：

- `@Audited` 标在 **Application Service**（类或方法）；Domain 实体上不标。
- `AuditMode.REQUIRED`：无可用 `AuditStorage` 或分发失败时**拒绝**操作（runtime 单测已覆盖）；`BEST_EFFORT` 尽力写入。
- 开启审计须在构建 `ServiceRuntime` 时 `auditEnabled(true)` 并注入真实 `AuditStorage`（Spring 应用当前需扩展 `ServiceRuntime` Bean，见 runtime README）。
- `auditSnapshot` / `AuditSnapshotProvider`：契约已有，**运行时未**按快照键自动调用 Provider（📋）。
- `TransactionalAuditStorage` + `CommitObserver`：用于与 DB 事务对齐；**业务 outbox 与落库**归属 kernel/platform（见 [service-future-work.md](../docs/service-future-work.md)）。

---

### 6. `governance` — 限流、舱壁、断路器

**作用**：治理能力的**端口**定义。具体算法（令牌桶、Semaphore、Resilience4j 等）在 `innospots-nexus-service-governance` 实现；契约层只描述请求/许可/决策形状。

| 类型 | 说明 |
|------|------|
| `RateLimitProvider` / `RateLimitRequest` / `RateLimitDecision` | 限流判定 |
| `BulkheadProvider` / `BulkheadPermit` | 并发隔离 |
| `CircuitBreakerProvider` / `CircuitPermit` | 断路器许可 |

**实现状态**：契约 ✅ · Runtime/governance ✅（`LocalTokenBucketProvider`、`SemaphoreBulkheadProvider`、`Resilience4jCircuitBreakerProvider` 及对应拦截器）· 可选 `innospots-nexus-service-governance-redis`（`RedisTokenBucketRateLimitProvider`）· Spring 默认 🔶（**仅**注册限流+超时；`store=redis` 切换集群桶）· 生产集成 🔶（默认单 JVM；Redis 为可选装配）。

**用法要点**：

- 限流：注解键必须在 `GovernanceConfig.rateLimits()` 中有条目；Spring 样例仅预置 `adapter-rate-limit`。
- 舱壁/熔断：实现已在 `service-governance`，但 Spring **默认未** `addInterceptor`；按 [治理接入文档](../../../innospots-nexus-spring/innospots-nexus-spring-service/docs/governance-integration.md) 手动注册。
- 所有治理拦截器仅在 **`ServiceInvocationBridge` 路径**上执行；裸 Controller 不经过引擎则不受限。
- 集群限流：Spring 配置 `service.governance.rate-limit.store=redis`，或实现/替换 `RateLimitProvider` Bean；勿在业务代码写死 QPS。

---

### 7. `observation` — 指标

**作用**：单次调用的观测快照与指标 SPI，与 Micrometer 等实现解耦。

| 类型 | 说明 |
|------|------|
| `InvocationObservation` | 一次调用的耗时、结果、操作 ID 等观测字段 |
| `ServiceMeters` | 预定义指标名/维度约定 |
| `MetricsProvider` | 指标注册与上报 SPI |

**实现状态**：契约 ✅ · observability ✅（`MicrometerMetricsProvider`、`NoOpMetricsProvider`）· Spring 默认 🔶（`AccessLogWriter`、`MdcContextBridge` 已装配；**默认未**暴露 Micrometer Bean）· 生产集成 🔶。

**用法要点**：

- HTTP 入口自动写访问日志（含 requestId、耗时、状态）；敏感字段经 `SensitiveValueMasker`。
- 业务指标通过实现 `MetricsProvider` 或使用 `ServiceMeters` 约定名注册；与 `InvocationEngine` 集成为可选增强。
- `InvocationObservation` 供拦截器/adapter 上报单次调用维度，业务 rarely 直接构造。

---

### 8. `trace` — 追踪

**作用**：与 OpenTelemetry 等对齐的轻量追踪快照与作用域句柄。

| 类型 | 说明 |
|------|------|
| `TraceSnapshot` | traceId、spanId、是否采样 |
| `TraceProvider` | 创建/恢复追踪上下文 SPI |
| `TraceHandle` | 活跃跨度句柄，用于 try-with-resources 式关闭 |

**实现状态**：契约 ✅ · observability ✅（`OpenTelemetryTraceProvider`、`NoOpTraceProvider`）· Spring 默认 🔶（**NoOp**）· 生产集成 🔶（需自行 `@Bean TraceProvider` 替换）。

**用法要点**：

- `ServiceContext.trace()` 始终非 null；未接入 OTel 时 traceId/spanId 为空字符串，**不**伪造全零 trace。
- `@Traced` 目前 **未**参与 `AnnotationPolicyResolver`（📋）；追踪靠 adapter 在 Filter 层安装 `TraceProvider`。
- 业务代码不要解析 traceId 做安全决策；仅用于日志与链路关联。

---

### 9. `cancellation` — 取消协作

**作用**：在长时间操作或流式会话中，统一客户端断开、超时、主动取消等信号。

| 类型 | 说明 |
|------|------|
| `CancellationToken` | 查询是否已取消、注册回调 |
| `CancellationRegistration` | 注册句柄 |
| `CancellationReason` | 取消原因枚举 |

**实现状态**：契约 ✅ · Runtime ✅（`CancellationSource`）· Spring 默认 ✅（请求结束/客户端断开触发）· 生产集成 ✅。

**用法要点**：

- 每个 HTTP 请求在 Filter 中绑定 `CancellationToken`；流式/SSE 须在写出循环中检查取消。
- 注册 `CancellationRegistration` 须在 finally 中关闭，避免泄漏回调。
- 取消原因区分客户端断开、超时、主动取消，便于日志与指标。

---

### 10. `time` — 时间与截止

**作用**：可测试的单调时钟与调用截止时间，避免业务直接依赖 `System.currentTimeMillis()`。

| 类型 | 说明 |
|------|------|
| `Deadline` | 剩余时间、是否已过期 |
| `Ticker` | 单调时钟 SPI（runtime 提供系统实现） |

**实现状态**：契约 ✅ · Runtime ✅（`DeadlineInterceptor`、`DeadlineScheduler`）· Spring 默认 ✅ · 生产集成 ✅。

**用法要点**：

- 全局请求 deadline 由 transport 建立；操作级超时由 `TimeoutInterceptor` 与 `GovernanceConfig.timeouts` 叠加（取较小剩余时间）。
- `@TimeoutProtected` 注解暂 **未**写入 `OperationPolicy.timeout`（📋）；请用 **operationId** 作为 `GovernanceConfig` 超时 map 的键，或与 Bridge 传入的 `operationId` 一致。
- 单测可注入自定义 `Ticker`；生产使用 `Ticker.system()`。

---

### 11. `error` — 服务错误目录

**作用**：服务框架域内的稳定错误码与结构化错误描述，供 HTTP 错误映射与日志关联。

| 类型 | 说明 |
|------|------|
| `ServiceError` | 单条错误：码、消息键、HTTP 暗示等 |
| `ServiceErrorCatalog` | 注册与查找 |

**实现状态**：契约 ✅ · Runtime/HTTP ✅（`HttpErrorMapper` 消费目录）· Spring 默认 ✅ · 生产集成 ✅。

**用法要点**：

- 框架拦截器抛 `NexusException.build(ServiceStatusCode.*)`；业务模块使用各自 `*StatusCode`，不要重复定义 SRV 段。
- `ServiceErrorCatalog` 注册条目供映射与文档化；新增 SRV 码须补契约测试（见 `ServiceErrorCatalogContractsTest`）。
- **不存在** `ServiceException` 类型。

---

### 12. `status` — 服务技术状态码

**作用**：`ServiceStatusCode` 枚举/目录，对齐 `NexusStatusCode` 中 SRV 段（如上下文缺失、序列化失败等框架级错误）。

**实现状态**：契约 ✅ · 各模块引用 ✅ · Spring HTTP 映射 ✅ · 生产集成 ✅。

**用法要点**：

- 与 kernel/console **业务**状态码分离；SRV 段表示框架、传输、治理类失败。
- adapter 将 `ServiceStatusCode` 映射为 HTTP 状态与 `R` / Problem Detail 外形（由 `ResponseProfile` 决定）。
- 契约测试锁定双语消息形状与模块前缀（`ServiceStatusCodeContractsTest`）。

---

## 包结构（目录树）

```text
com.innospots.nexus.service.contract
├── context          # ServiceContext、RequestMetadata、ServiceContextAccessor
├── invocation       # InvocationContext、ServiceInterceptor、ExecutionMode
├── policy           # OperationPolicy、OperationDescriptor、PolicyCatalog
│   └── annotation   # @ServiceOperation、@RateLimited、@TimeoutProtected …
├── security         # ServicePrincipal、PermissionProvider、PermissionCheck
│   └── annotation   # @RequiresPermission、@PublicAccess
├── audit            # AuditEvent、AuditStorage、AuditSnapshotProvider
│   └── annotation   # @Audited
├── governance       # RateLimitProvider、CircuitBreakerProvider、BulkheadProvider
├── observation      # MetricsProvider、InvocationObservation、ServiceMeters
├── trace            # TraceProvider、TraceSnapshot、TraceHandle
├── cancellation     # CancellationToken、CancellationReason
├── time             # Deadline、Ticker
├── error            # ServiceError、ServiceErrorCatalog
└── status           # ServiceStatusCode
```

---

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-contract</artifactId>
</dependency>
```

版本由 `innospots-nexus-bom` 统一管理，模块 POM 不写版本号。

---

## 快速接入

本节说明 **典型业务场景**：各自解决什么问题、需要哪些 Maven 模块、Spring 下如何配置。  
契约注解与 SPI 定义在本模块；**真正执行**依赖 `innospots-nexus-spring-service`（或 Quarkus adapter）+ `service-runtime`。

### 接入分层（先选对依赖）

| 层级 | Maven | 用途 |
|------|--------|------|
| 仅类型/注解 | `innospots-nexus-service-contract` | Domain 以外的 API 模块引用注解；**无**运行时行为 |
| 完整 HTTP 能力 | `innospots-nexus-spring-service` + `spring-boot-starter-web`（或 webflux） | Filter、`ServiceContext`、`InvocationEngine`、错误映射 |
| 平台身份与权限 | 上者 + 自研 `SecurityProvider` / `PermissionProvider` Bean | 授权拦截生效（kernel 默认实现尚未提供） |

Spring 应用须在启动类 **显式**启用（无 `spring.factories` 自动开关）：

```java
@SpringBootApplication
@EnableNexusServiceHttp   // 来自 com.innospots.nexus.spring.service.bootstrap
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

所有场景的 **公共配置**（`ServiceProperties`，前缀 `service`）：

```yaml
# application.yml
service:
  enabled: true                    # false 时整个 Spring adapter 不装配
  name: order-api                  # 访问日志 / 指标等服务名标签
  response-profile: LEGACY         # 错误 JSON：LEGACY | PROBLEM_DETAIL
```

契约模块本身 **没有** `application.yml`；治理策略键、审计开关等见各场景。

---

### 场景 1：HTTP 接口鉴权（`@RequiresPermission`）

**用途**：在**应用服务**入口统一做权限校验，避免每个 Controller 手写 `if (!canRead)`；与 `ServicePrincipal`、租户作用域一致，便于后续审计与治理共用同一 `operationId`。

**生效条件**（缺一不可）：

1. 已启用 `@EnableNexusServiceHttp` 且 `service.enabled=true`（Filter 会安装 `ServiceContext`）。
2. 注册了 `PermissionProvider` Bean；若需真实用户而非匿名，还须注册 `SecurityProvider`（在 `InvocationEngine` 进入时升级主体）。
3. 权限注解写在 **被调用的 Service 方法**上，Controller 通过 **`ServiceInvocationBridge.invoke`** 进入引擎（仅贴在 Controller 上**不会**鉴权）。

**Maven**（应用模块）：

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

**代码**：

```java
@RestController
public class OrderResource {

    private final OrderService orders;
    private final ServiceInvocationBridge bridge;

    public OrderResource(OrderService orders, ServiceInvocationBridge bridge) {
        this.orders = orders;
        this.bridge = bridge;
    }

    @GetMapping("/orders/{id}")
    public OrderVo get(@PathVariable String id) throws Exception {
        // operationId 建议与权限/治理键保持一致，便于超时与指标
        return bridge.invoke(
                orders,
                OrderService.class.getMethod("find", String.class),
                "order.read",
                () -> orders.find(id));
    }
}

@Service
public class OrderService {

    @RequiresPermission("order.read")
    public OrderVo find(String id) {
        return /* ... */;
    }
}
```

**配置与 SPI**（示例：本地开发用测试夹具同等逻辑，生产替换为 kernel 实现）：

```java
@Configuration
public class SecurityBeans {

    @Bean
    SecurityProvider securityProvider() {
        // 从 Authorization 头解析用户并返回 AuthenticationResult
        return /* 实现 SecurityProvider：解析 Header/Token → ServicePrincipal */;
    }

    @Bean
    PermissionProvider permissionProvider() {
        // 根据 principal + PermissionCheck 返回 allow/deny
        return /* 实现 PermissionProvider */;
    }
}
```

**验证**：带合法凭据请求应 200；无权限应返回框架映射的 **403**（`ServiceStatusCode` / `NexusException`）。  
公开接口可在类或方法上使用 `@PublicAccess`（与 `@RequiresPermission` 互斥）。

---

### 场景 2：应用服务审计（`@Audited`）

**用途**：对关键业务操作（创建订单、改权限、删数据）产生**技术审计事件**，写入 `AuditStorage`，供合规与排障；与 kernel 侧「审计查询 UI」分离（查询不在 contract 层）。

**生效条件**：

1. 与场景 1 相同：须 **`ServiceInvocationBridge`** 调用带 `@Audited` 的方法。
2. `AnnotationPolicyResolver` 会把 `@Audited` 编入 `OperationPolicy`；`AuditInterceptor` 在引擎链中分发事件。
3. Spring 默认 `ServiceRuntime` **未**打开 `auditEnabled`，且使用 **noop** 存储 → 生产须 **自定义 `ServiceRuntime` Bean**（或扩展配置类）并注入真实 `AuditStorage`。

**代码**（Application Service）：

```java
@Service
public class OrderCommandService {

    @Audited(action = "order.create", mode = AuditMode.REQUIRED)
    public Order create(CreateOrderCommand cmd) {
        return /* ... */;
    }
}
```

Controller 仍通过 Bridge 调用上述方法；`action` 与 Bridge 的 `operationId` 可一致，便于日志关联。

**配置**（Spring — 启用审计运行时，示意）：

```java
@Configuration
public class AuditRuntimeConfiguration {

    @Bean
    ServiceRuntime serviceRuntime(
            ThreadBoundServiceContext contexts,
            ObjectProvider<SecurityProvider> securityProvider,
            ObjectProvider<PermissionProvider> permissionProvider,
            RateLimitInterceptor rateLimitInterceptor,
            TimeoutInterceptor timeoutInterceptor,
            AuditStorage auditStorage) {   // 自行实现并 @Bean
        ServiceRuntime.Builder builder = ServiceRuntime.builder()
                .contexts(contexts)
                .auditEnabled(true)
                .auditStorage(auditStorage)
                .addInterceptor(rateLimitInterceptor)
                .addInterceptor(timeoutInterceptor);
        securityProvider.ifAvailable(builder::securityProvider);
        permissionProvider.ifAvailable(builder::permissionProvider);
        ServiceRuntime runtime = builder.build();
        runtime.start();
        return runtime;
    }
}
```

`AuditMode.REQUIRED`：存储不可用或分发失败时**拒绝**操作；`BEST_EFFORT` 仅尽力写入。  
`auditSnapshot` / `AuditSnapshotProvider` 键目前 **未**自动接线（见上文实现状态）。

---

### 场景 3：限流与超时（`@RateLimited` 等）

**用途**：保护下游与自身：按操作维度限 QPS、限制单次调用最长耗时；键名与监控、熔断策略对齐。

**生效条件**：

1. **Bridge 路径** + 方法（或类）上的 `@RateLimited("键")`。
2. `GovernanceConfig` 的 `rateLimits` / `timeouts` Map 中存在**同名键**。
3. Spring 默认已注册 `RateLimitInterceptor`、`TimeoutInterceptor`；舱壁/熔断须额外注册拦截器（见 [治理接入手册](../../../innospots-nexus-spring/innospots-nexus-spring-service/docs/governance-integration.md)）。

**说明**：`@TimeoutProtected` 在契约中存在，但 **`AnnotationPolicyResolver` 尚未解析**；超时请用 Bridge 的 **`operationId`** 作为 `GovernanceConfig.timeouts` 的键（与限流键可相同或不同）。

**代码**：

```java
@Service
public class OrderCommandService {

    @RateLimited("order.create")
    public Order create(CreateOrderCommand cmd) {
        return /* ... */;
    }
}

// Controller
bridge.invoke(orders, OrderCommandService.class.getMethod("create", CreateOrderCommand.class),
        "order.create",   // 同时作为 timeouts Map 的查找键
        () -> orders.create(cmd));
```

**配置**（覆盖默认 `GovernanceConfig` Bean）：

```java
@Configuration
public class GovernanceConfiguration {

    @Bean
    GovernanceConfig serviceGovernanceConfig() {
        return new GovernanceConfig(
                Map.of("order.create", new RateLimitPolicy(20, 5.0D)),  // burst, 每秒补充
                Map.of(),           // bulkheads — 需自注册 BulkheadInterceptor 后才有意义
                Map.of(),           // circuits
                Map.of("order.create", Duration.ofSeconds(10)),        // 操作超时
                100_000,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
    }
}
```

限流默认为 **单 JVM 令牌桶**（`LocalTokenBucketProvider`）；可选 `store=redis` 使用集群桶。触发限流 → HTTP **429**；超时 → **504** / `DEADLINE_EXCEEDED`。

---

### 场景 4：读取请求上下文（`ServiceContextAccessor`）

**用途**：在 Service/组件内获取 **requestId**、当前用户、租户作用域、取消令牌，用于日志、业务校验与长任务协作；无需在方法签名上传递 `HttpServletRequest`。

**生效条件**：

1. 当前线程已由 `ServiceServletFilter` / `ServiceWebFilter` 安装上下文（即处于 HTTP 请求处理中，且 `service.enabled=true`）。
2. 若在 Bridge **之前**调用，主体可能仍为 Filter 阶段的匿名用户；经 Bridge 且配置了 `SecurityProvider` 后，拦截器链内会刷新为已认证主体。

**Maven**：场景 1 相同；业务模块若只读上下文、不经过 Bridge，可仅依赖 contract + 运行时传递的 accessor 接口（由 adapter 提供 Bean）。

**代码**：

```java
@Service
public class OrderQueryService {

    private final ServiceContextAccessor contexts;

    public OrderQueryService(ServiceContextAccessor contexts) {
        this.contexts = contexts;
    }

    public OrderDetail detail(String id) {
        ServiceContext ctx = contexts.requireCurrent();
        String requestId = ctx.requestId();           // 与响应头 x-request-id 一致
        String userId = ctx.security().id();
        if (ctx.cancellation().isCancelled()) {
            throw NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED);
        }
        return /* 使用 userId 做数据范围校验 */;
    }
}
```

**配置**：无额外 YAML；确保场景 1 的 `service.*` 与 `@EnableNexusServiceHttp` 已开启。  
**不要**把 `ServiceContext` 传到 Repository；Domain 层不得引用本类型。

---

### 场景 5：仅引用契约（库模块 / DTO）

**用途**：在 API 模块、插件声明、共享接口中引用契约注解或 SPI 类型（如 `PermissionProvider`），而 **不**拉起 HTTP 运行时。

**Maven**：

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-contract</artifactId>
</dependency>
```

**配置**：无。注意：此依赖 **不会** 注册 Filter、Bridge 或 `InvocationEngine`；行为仅在引入 adapter 的应用进程中生效。

---

## 配置说明（契约 vs Spring）

| 配置项 | 归属 | 说明 |
|--------|------|------|
| `service.enabled` / `name` / `response-profile` | Spring `ServiceProperties` | 开关 adapter、服务名、错误体外形 |
| `GovernanceConfig`（Java Bean） | Spring 默认在 `ServiceCoreConfiguration` | 限流/超时等**键 → 参数**；生产应 `@Bean` 覆盖 |
| `nexus.i18n.*` | `innospots-nexus-spring-core` | 与 JSON `${key}` 解析相关，非 service-contract |
| 注解中的 `"order.read"` | 契约 | 权限键；不是 YAML 路径 |
| `@RateLimited("order.create")` | 契约 | 策略键；对应 `GovernanceConfig.rateLimits` 的 Map 键 |

契约层**没有** `application.yml`。治理、审计、安全 SPI 均在 adapter 或应用 `@Configuration` 中装配。  
更完整的 HTTP/治理/观测步骤见 [spring-service 文档索引](../../../innospots-nexus-spring/innospots-nexus-spring-service/docs/README.md)。

---

## 继承与实现（SPI 汇总）

| 扩展点 | 接口 | 典型实现方 |
|--------|------|------------|
| 调用拦截 | `ServiceInterceptor` | 平台 / adapter 注册 |
| 认证 | `SecurityProvider` | kernel / gateway |
| 授权 | `PermissionProvider` | kernel / console |
| 审计存储 | `AuditStorage` | platform / adapter |
| 审计快照 | `AuditSnapshotProvider` | 业务模块按资源注册 |
| 限流 | `RateLimitProvider` | governance 模块 |
| 舱壁 | `BulkheadProvider` | governance 模块 |
| 断路器 | `CircuitBreakerProvider` | governance 模块 |
| 指标 | `MetricsProvider` | observability 模块 |
| 追踪 | `TraceProvider` | observability 模块 |
| 时钟 | `Ticker` | runtime 默认实现 |

---

## 注意事项

- Domain 层禁止 `@RequiresPermission`、`ServiceContext`、HTTP/WS 类型。
- 不存在 `ServiceException`；业务异常用 `NexusException.build(...)` 与各模块 `StatusCode`。
- 技术审计与业务审计查询分离；勿在 contract 中引入持久化 API。
- 本模块不得依赖 Spring Boot auto-configuration；启用能力在 `innospots-nexus-spring-service` 完成。

---

## 相关文档

- [服务框架 README](../README.md)（M1–M9 里程碑与模块索引）
- [待完善能力](../docs/service-future-work.md)（JWT 一等 Provider、分布式限流、审计落库等）
- [服务开发者体验](../docs/service-developer-experience-design.md)
- [契约设计](../docs/service-contract-design.md)
- [服务框架总览](../docs/service-framework-design.md)
- Spring 接入：[innospots-nexus-spring-service/docs/README.md](../../../innospots-nexus-spring/innospots-nexus-spring-service/docs/README.md)
