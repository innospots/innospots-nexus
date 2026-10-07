# 框架适配、装配与配置设计

说明 Spring / Quarkus **如何把中立运行时接到宿主 Web 栈**。决策 D1–D15 见 [总览 §9](service-framework-design.md)；业务用法见 [开发体验](service-developer-experience-design.md)。缺口（上传绑定、Quarkus 完整 build-time 处理等）见 [待完善](service-future-work.md)。

## 1. 装配边界

两个 adapter 组合 contract/runtime 与 http、stream、websocket、transfer、observability、governance，**不**依赖 console/portal/platform。宿主保留 Web starter、JSON、认证；adapter 不得隐式引入 JDBC 或全套 IAM。

| 适配器 | 引入方式 |
|--------|----------|
| Spring | `innospots-nexus-spring-service`；`@EnableNexusService` 或应用 bootstrap `@Import`；MVC / WebFlux 条件配置分离 |
| Quarkus | `innospots-nexus-quarkus-service` 扩展 + `innospots-nexus-quarkus-service-deployment` |

Spring：Web 栈依赖 optional，应用只引入实际使用的一种。`spring-app` 是完整宿主，不是 adapter 父 POM。

Quarkus：runtime 与 deployment 分离；**runtime POM 不依赖 deployment JAR**。当前 deployment 已注册扩展名；完整 Jandex 扫描与注解 binding 见 [待完善](service-future-work.md)。

共享黑盒：`innospots-nexus-service-adapter-test`（仅 JDK 客户端 + 中立契约）；Spring / Quarkus 测试依赖它，各自提供最小宿主应用。

## 2. 适配能力矩阵

| 能力 | Spring MVC | Spring WebFlux | Quarkus |
|---|---|---|---|
| 路由 | 原生 Controller，仅新适配范围 | 原生 Controller/Router | Jakarta REST Resource |
| 网络前置 | OncePerRequestFilter + 容器 limits | WebFilter + server limits | Vert.x/REST filters + server limits |
| 静态方法信息 | HandlerMethod + 目标方法 | HandlerMethod + Reactor boundary | 构建期索引 + ResourceInfo |
| 普通同步服务 | worker + InvocationEngine.invokeSync | worker卸载，不能在event loop join | @Blocking/worker + invokeSync |
| CompletionStage | DeferredResult/原生异步返回桥接 | Mono 适配 | Uni 适配 |
| Flow.Publisher | 单订阅→有界 emitter worker | Flow→Reactive Streams→Flux | Flow→Multi |
| SSE | SseEmitter，仅adapter可见 | 原生 SSE writer | REST SSE writer |
| WS | Spring WebSocketHandler/session | Reactive WebSocketHandler | WebSockets Next callbacks |
| 上传下载 | Multipart / streaming response | DataBuffer按需转换 | REST multipart / Vert.x有界流 |
| 权限拦截 | Spring AOP + transport metadata | 延迟订阅时 AOP/策略 | build-time binding + CDI拦截 |
| Context | TLC/MDC + managed executor | Reactor Context + signal wrapper | SmallRye context provider + signal wrapper |
| 启停 | SmartLifecycle/销毁钩子 | 同左 | StartupEvent/ShutdownEvent + recorder |

Spring MVC 对 reactive 返回可适配流，但响应写入仍可能阻塞，必须使用有界 worker；不能声称它等同非阻塞 I/O。[Spring 异步请求文档](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)是该区别及异步回调挂接点依据。

Mutiny 2 基于 JDK Flow，Reactor 使用 Reactive Streams，桥接应按实际类型完成而不是对 Publisher 强转。[Mutiny 迁移说明](https://smallrye.io/smallrye-mutiny/latest/reference/migrating-to-mutiny-2/)、[转换指南](https://smallrye.io/smallrye-mutiny/latest/guides/converters/)。

## 3. Spring 适配实现文件

包前缀 `com.innospots.nexus.spring.service`；按包组织，以下每行不超过 15 个文件。

| 包 | 类/资源 | 责任 |
|---|---|---|
| config | EnableNexusService、ServiceProperties、ServiceCoreConfiguration、ServiceServletConfiguration、ServiceReactiveConfiguration 等 | 显式 Import 或 bootstrap；SPI 唯一性与默认注册 |
| mvc | ServiceServletFilter、ServiceMvcInterceptor、ServiceReturnValueHandler、ServiceExceptionAdvice、ServiceMvcConfiguration | 生命周期、metadata、结果形态与错误 |
| webflux | ServiceWebFilter、ServiceReactiveResultHandler、ServiceWebExceptionHandler、ServiceWebFluxConfiguration | Reactor上下文、流式结果和取消 |
| invocation | ServiceMethodAdvisor、ServiceAnnotationPolicyResolver | 从容器目标类/接口解析意图，不处理路由 |
| context | ServiceThreadLocalAccessor、ServiceTaskDecorator | context桥接、异步executor |
| websocket | SpringWebSocketSessionAdapter、SpringWebSocketEndpointBridge、SpringWebSocketConfiguration | native WS到标准接口 |
| security | SpringNativePrincipalProvider | 宿主已认证 Principal/context 到 ServicePrincipal |
| transfer | ServletUploadAdapter、ReactiveUploadAdapter、SpringDownloadWriter | Multipart与有界二进制转换 |
| lifecycle | SpringServiceLifecycle | 借用/自建资源区分与关闭 |
装配遵循 [Spring Boot 条件配置](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)：按 class/bean/property 条件加载；用户提供 SPI bean 则默认实现退让。不得把 optional 类型写进无条件类的字段/方法，避免只装 MVC 时 ClassNotFound。

### 3.1 MVC

Filter 在 REQUEST 建立唯一 request lifecycle，ASYNC/ERROR redispatch 查找同一 request attribute，不重复认证/扣限流/建 span；原线程 finally 只恢复线程视图，不能宣告网络响应结束。AsyncListener/DeferredResult completion 接入真实传输结束。

ServiceReturnValueHandler 仅支持 `StreamSession`、JDK Flow Publisher 和 DownloadResource 等新增形态，普通值继续原生 handler；注册顺序须在通用 body handler 前，否则对象会被错误 JSON 序列化。不得接管所有 Controller 返回类型。Spring AOP 包装异步返回，策略进入与输出包装保证只有一次 business 调用。

SSE 每次 native send 成功后才能 request 下一个批次。客户端消失通常要到下一次写/heartbeat 才能探测；承诺“探测后取消”，不能承诺网络静默时即时通知。[Spring 异步文档](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)说明 Servlet disconnect 检测限制。

### 3.2 WebFlux

ServiceWebFilter 使用 Reactor Context 保存 ServiceContext；不能在 filter 进入时设置 ThreadLocal 后跨整个异步链保留。通过每信号 scope 恢复支持业务 accessor。Flow 转换桥接的 request/cancel 双向传递，并保护终态只结束一次。

自定义 ResultHandler 处理标准 StreamSession/DownloadResource，其他交原生编解码。ByteBuffer 与 DataBuffer 转换须保证 release，在取消/解码失败/未消费路径也释放。安全 Provider 如果可能阻塞必须明确 offload，不能在 event loop join。

### 3.3 AOP 与零侵入边界

扫描有服务意图注解的 Spring 管理 bean 方法；未注解普通 HTTP 也从 transport 获得基础观测。同类自调用/private/final 或未托管 new 对象不会自动获得 Spring proxy 拦截；启动扫描对带注解但不可代理的方法报错，自调用文档要求拆为协作 bean 或显式注入 InvocationEngine 端口，不承诺字节码全局插桩。

端点和 service 同时注解时，以 invocationId+operationId+policyKey 识别同一执行边界的重复拦截；独立下游 operationId 创建子执行，不因已有请求级拦截就跳过下游熔断。运行时不得仅用布尔 ThreadLocal 表示“已拦截”。

认证 D2 未批准前使用宿主 Provider，不引 Spring Security。在批准的可选方案中，新增 `security.springsecurity` 独立条件配置，位于 Spring artifact，读 Authentication/ReactiveSecurityContextHolder；自身不创建第二条 SecurityFilterChain、不覆盖宿主 JWT 验证策略。

## 4. Quarkus 适配实现文件

Runtime 包前缀 `com.innospots.nexus.quarkus.service`；deployment 前缀增加 `.deployment`。

| 模块/包 | 类/资源 | 责任 |
|---|---|---|
| runtime.config | ServiceConfigMapping、QuarkusServiceProducer | Config→中立record、SPI装配 |
| runtime.rest | ServiceRequestFilter、ServiceResponseFilter、ServiceExceptionMapper、ServiceStreamingWriter | 原生请求、错误与输出桥接 |
| runtime.invocation | ServiceBoundary、ServiceCdiInterceptor | CDI专用binding，读取构建期生成metadata |
| runtime.context | ServiceThreadContextProvider | SmallRye注册TLC/MDC/service上下文快照 |
| runtime.websocket | QuarkusWebSocketSessionAdapter、QuarkusWebSocketEndpointBridge | WebSockets Next原生callback转发 |
| runtime.security | QuarkusSecurityPrincipalProvider | SecurityIdentity映射；使用已有认证机制 |
| runtime.transfer | QuarkusUploadAdapter、QuarkusDownloadWriter | 原生文件/Buffer转换与关闭 |
| runtime.lifecycle | QuarkusServiceLifecycle | startup/shutdown和共享资源所有权 |
| deployment | ServiceProcessor、ServiceAnnotationTransformer、ServiceMetadataBuildItem | index扫描、binding变换、bean保留、构建检查 |

表中的 `runtime.` 表示 runtime artifact 内能力包，不要求真实包再增加一层 runtime；实际为 `...quarkus.service.rest` 等。

### 4.1 构建期

ServiceProcessor 读取 Jandex，找到意图注解及其所在 bean/method，注入 adapter 专用 ServiceBoundary binding，生成 operation descriptor（资源解析器/泛型类型/策略键）。不修改中立注解本身、不把 Jakarta CDI API 下沉 contract。不可代理/重复 operationId/缺失 SPI/多 handler映射歧义在构建期失败。

无界运行时反射扫描与假定所有类都留在 native image 是禁止的。登记需要的 bean、codec type、构造器及配置；对动态插件载荷只支持显式类型注册，native 不支持任意类名解码。

Quarkus 扩展的 runtime/deployment 分离与构建期转换依据 [扩展开发指南](https://quarkus.io/guides/writing-extensions)及 [CDI integration](https://quarkus.io/guides/cdi-integration/)。这是完整注解支持需要 D7 的原因。

### 4.2 请求、异步与 WebSocket

原生路由不变。REST 前置 filter 处理 transport，ServiceCdiInterceptor 执行已解析策略；不要让 response filter 在 Publisher 返回但未结束时关闭 scope。CompletionStage 适配 Uni，并使用 `ThreadContext`/受管 executor 包装必要回调；Mutiny 信号也经过明确的 ContextPropagation。

WebSockets Next callback 的线程模式与返回类型/原生注解有关，adapter 不在收到 onMessage 后才发现要卸载的阻塞任务。用原生薄端点声明路径并委托 Bridge，输入/输出类型在构建期确认。Registry/业务 Session 独立于容器 Connection，但容器是网络连接事实源。[WebSockets Next reference](https://quarkus.io/guides/websockets-next-reference/)为 callback/生命周期适配依据。

不能在 application startup 后通过自建路由器动态注册任意 WS 路径。普通业务只编写标准 Handler，应用集成层提供一次原生端点薄壳；若需求进一步要求每新增 Handler 连薄壳都省略，则须由 deployment 生成端点，作为 D7 的后续增强，不混入运行时反射。

## 5. 现有 Nexus 宿主桥接

| 宿主包（计划） | 实现 | 依赖与限制 |
|---|---|---|
| spring-console.service.security / quarkus-console.service.security | ConsoleCompactTokenProvider | 调用 TokenIssuer.parse 后验证 access purpose/expiry/realm/scope，不叫 JWT Provider |
| 同上 | ConsoleCatalogPermissionProvider | 只处理原 page/datasource 授权，不假装支持任意 permission string |
| 宿主 service.security | ApplicationPermissionProvider | 通用 RBAC/resource权限由应用提供，纯 SPI，service库不存IAM表 |
| 宿主 service.resource | CoreResourceContentBridge | 组合 core registry 与真实流读 provider；缺少能力拒绝 |
| 宿主 service.audit | 审计存储/提交观察桥接 | 写入其拥有的业务表或outbox，通用库不依赖JDBC |

这些桥接是宿主应用配置，不产生 service→console 的反向依赖。portal/platform 各自提供业务能力，应用可以装配，但不互相 import。

既有 JavaScript/管理端客户端保持 R 和 compact token；新 JWT/OIDC 服务可并存但明确配置 realm/providerId 和受众，不用“尝试解密失败就换另一种认证”的模糊链来放行。

## 6. 依赖与构建规格

所有依赖版本来自 `innospots-nexus-bom`；下表为能力坐标归属，实际版本以 `mvn -q help:effective-pom` 为准。

| 能力/坐标 | 所属 | 管理策略 |
|---|---|---|
| SLF4J、JUnit、AssertJ、Mockito | parent 已提供 | 不重复声明 |
| `io.opentelemetry:opentelemetry-api` | observability | 根 BOM 显式管理与宿主 OTel 对齐 |
| `io.opentelemetry:opentelemetry-sdk-testing` | adapter test | test scope，和 OTel API 同一版本线 |
| `io.micrometer:micrometer-core` | observability | 已在 BOM 登记，以 effective POM 为准 |
| `io.github.resilience4j:resilience4j-circuitbreaker` | governance | 根 BOM 新登记，不引 Spring starter |
| Spring WebMVC/WebFlux/WebSocket、Reactor | spring-service optional | Boot BOM 管理，真实 artifact 坐标按锁定 Boot 版本校验 |
| `io.micrometer:context-propagation` | spring-service | BOM 管理，optional 依赖按实际装配需要 |
| `io.quarkus:quarkus-rest`、`quarkus-rest-jackson` | quarkus-service runtime | Quarkus BOM；Jackson 2/3 与宿主匹配 |
| `io.quarkus:quarkus-websockets-next`、`quarkus-security` | quarkus-service runtime | Quarkus BOM |
| `io.quarkus:quarkus-smallrye-context-propagation` | quarkus-service runtime | Quarkus BOM |
| `io.quarkus:quarkus-arc-deployment` 等真实用到的部署 API | 新deployment | Quarkus BOM，不流入runtime |
| JWT/OIDC、MIME探测/病毒扫描SDK | 对应宿主/provider | 环境集成前 BOM 登记与兼容检查 |

BOM import 的 properties 不自动成为消费 POM 的 properties。变更依赖后须检查依赖树、重复类与 enforcedPlatform。Resilience4j/OTel/Micrometer 为可替换中立库，安全评估在锁版时记录。

## 7. 配置契约

顶层统一使用需求书的 `service`，避免同时存在 nexus.service 和 service 两套命名。绑定后转换为不可变配置 record；SpringProperties/QuarkusConfigMapping 才含框架注解。以下为完整骨干配置示例，功能默认关闭/开启以注释为准。

```yaml
service:
  enabled: true
  name: nexus-api
  response-profile: legacy
  request:
    timeout: 30s
    max-header-bytes: 16KiB
    max-body-bytes: 1MiB
    trusted-proxies: []
  context:
    attributes-max-count: 32
    attributes-max-bytes: 16KiB
  execution:
    blocking-max-concurrency: 64
    blocking-queue-capacity: 256
    cpu-queue-capacity: 256
  security:
    provider: host
    public-operations: []
    cors:
      enabled: true
      allowed-origins: []
      allowed-methods: [GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD]
      allowed-headers: [Authorization, Content-Type, X-Request-Id, X-Client-Id, Idempotency-Key]
      exposed-headers: [X-Request-Id, Retry-After, ETag]
      credentials: false
    csrf-mode: auto
  stream:
    enabled: true
    max-sessions: 1000
    buffer-size: 256
    buffer-bytes: 1MiB
    global-buffer-bytes: 64MiB
    max-event-bytes: 64KiB
    overflow: REJECT
    subscribe-timeout: 30s
    ttl: 60m
    idle-timeout: 10m
    heartbeat: 15s
    terminal-write-timeout: 2s
  websocket:
    enabled: true
    max-connections: 10000
    max-connections-per-user: 5
    max-message-size: 1MiB
    inbound-buffer-size: 256
    outbound-buffer-size: 256
    inbound-buffer-bytes: 1MiB
    outbound-buffer-bytes: 1MiB
    global-inbound-buffer-bytes: 64MiB
    global-outbound-buffer-bytes: 64MiB
    overflow: REJECT
    ping-interval: 30s
    pong-timeout: 10s
    idle-timeout: 30m
    message-timeout: 30s
  file:
    enabled: false
    max-file-size: 100MiB
    max-file-count: 10
    max-total-size: 200MiB
    chunk-size: 64KiB
    temp-budget: 1GiB
    allowed-mime-types: []
    allowed-extensions: []
    checksum: SHA-256
    malware-scan-required: false
  governance:
    rate-limit:
      enabled: false
      max-keys: 100000
      idle-ttl: 15m
    bulkhead:
      enabled: false
    circuit-breaker:
      enabled: false
  observability:
    access-log: true
    metrics: true
    tracing: true
    instrumentation-owner: host
    payload-logging: false
    business-meters:
      model.publish.success: []
  audit:
    enabled: false
    mode: BEST_EFFORT
    queue-capacity: 4096
    queue-bytes: 16MiB
    max-event-bytes: 64KiB
    append-timeout: 3s
    max-attempts: 3
    delivery-timeout: 10s
  idempotency:
    enabled: false
    ttl: 10m
    max-entries: 10000
    max-response-bytes: 64KiB
    cache-bytes: 32MiB
  shutdown:
    grace-period: 30s
  policies:
    model-invoke:
      permission: [model:invoke]
      resource-resolver: model
      execution: BLOCKING
      timeout: 20s
      rate-limit:
        dimensions: [PRINCIPAL, RESOURCE]
        rate-per-second: 5
        burst: 10
      bulkhead:
        max-concurrent: 8
      circuit-breaker:
        enabled: false
    model-provider:
      circuit-breaker:
        enabled: true
        sliding-window-size: 100
        minimum-calls: 20
        failure-rate: 50
        slow-call-rate: 50
        slow-call-duration: 2s
        open-duration: 30s
        half-open-permits: 5
```

配置读取规则：

- service.enabled 缺省 true 适用于显式安装服务 adapter 的新应用；已有应用接入前明确设置 profile 与 provider。disable 后不注册任何新拦截器。
- stream/WS 没有任何对应端点时不启动 manager/timer；enabled 不等于自动新增端点。
- 未安装服务仍无效果；已安装且要求安全能力但 Provider 缺失时失败。显式关闭治理却有 `@RateLimited` / `@CircuitProtected` / `@BulkheadProtected` / `@TimeoutProtected` 的 operation，启动失败，不能静默忽略注解。
- Duration 支持 `ms/s/m/h` 和 ISO-8601，size 支持 KiB/MiB/GiB；示例 MB 按迁移工具转换，不把 MB 和 MiB 混为一谈。所有容量必须正数，不允许 -1 表示无限。
- method timeout 不得超过 request/stream 当前剩余预算；heartbeat/ping 间隔小于对应 idle；minCalls≤window；maxFile≤maxTotal≤tempBudget；百分比 0–100（failure threshold 大于0）。
- CPU线程数缺省 max(1,cores-1)；正式可用 `execution.cpu-threads` 覆盖且需正数。阻塞 worker为宿主提供时验证队列边界；不自动关闭宿主线程池。
- 配置修改初版只在重启时生效；不实现热更新。未来热更新必须给策略 immutable revision，不能改变在途许可所属配置。
- 日志/文件/认证 secret 从宿主外部配置注入，不在本 YAML 给值。

## 8. 开发者体验与示例边界

完整用法、分层落点和禁止项见[开发体验](service-developer-experience-design.md)。适配层必须保证：

- 无注解普通 HTTP 仍获得 Level 0 能力。
- 业务模块不需要依赖本适配 artifact。
- `innospots-nexus-spring-service` 即实践文中的 starter 坐标；不另建 starter 模块（D5）。由 `@EnableNexusService` 显式启用。
- Stream 返回 `StreamSink`/`StreamSession`/`Flow.Publisher` 时由 result handler 处理；后台生产必须走 `ContextExecutor`。
- WS 路由薄壳留在宿主；Handler 在中立业务模块；注入 `WebSocketService` 而非原生 Session。
- 业务抛出的非 `NexusException` 在边界映射 `SYSTEM_ERROR`，不把 JDK 消息写入响应体。

本框架不提供登录、权限授予、审计查询 REST API。相关 URL 仍由既有 console/portal/platform 所有；不得为了演示把模型、聊天、文件业务实体写入中立模块。
