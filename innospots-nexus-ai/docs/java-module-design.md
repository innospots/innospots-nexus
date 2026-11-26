# Java 模块开发设计

## 1. 文档定位

- 状态：草案
- 范围：`com.innospots:innospots-nexus-ai` 的包结构、核心类、调用链与装配
- 关联：[java-api-design.md](java-api-design.md)、[java-runtime-supervisor.md](java-runtime-supervisor.md)、[java-configuration.md](java-configuration.md)

Java 模块是 **thin jar**：supervisor + `/ai/**` 反向代理 + 健康检查。Node runtime 由 npm 包提供。

## 2. 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-console</artifactId>
</dependency>
```

- 复用 console 的 JAX-RS、Bearer 鉴权、`@EndpointGrant`、异常映射、审计。
- 不依赖 portal/platform。
- 不绑定 Spring/Quarkus；由引用方装配。
- 不引入 JDBC/ORM 使用；AI 数据在 Node 侧。

## 3. 包结构

```text
com.innospots.nexus.ai
├── config/
│   ├── AiRuntimeConfig
│   ├── AiProxyConfig
│   ├── AiPaths
│   └── EnvironmentSanitizer
├── runtime/
│   ├── AiRuntime
│   ├── DefaultAiRuntime
│   ├── AiRuntimeStatus
│   ├── AiRuntimeHealth
│   ├── AiEndpoint
│   ├── AiProcessSupervisor
│   ├── AiProcessLauncher
│   ├── RuntimeLocator
│   ├── RuntimePackageValidator
│   ├── NodeVersionChecker
│   └── AiRuntimeHealthMonitor
├── health/
│   ├── AiHealthEndpoint
│   ├── AiHealthService
│   ├── AiHealthView
│   └── AiAvailabilityGuard
├── proxy/
│   ├── AiProxyService
│   ├── AiProxyRequest
│   ├── AiProxyResponse
│   ├── AiProxyRoutes
│   ├── ProxyHeaderPolicy
│   ├── ProxyBodyLimiter
│   ├── SseRelay
│   └── ProxyErrorWriter
├── endpoint/
│   ├── AiRuntimeProxyEndpoint
│   └── AiSessionProxyEndpoint
└── status/
    └── AiStatusCode
```

技术模块按功能分包，不套用业务域 `domain/{entity,vo,model,request}`；健康接口使用少量
只读 record（`AiHealthView` 等）。

## 4. 核心类

| 类 | 职责 |
|----|------|
| `AiRuntime` | 公共生命周期接口：start/stop/status/health/endpoint |
| `DefaultAiRuntime` | 组合 supervisor、健康监控、可用性门禁 |
| `AiProcessSupervisor` | 进程启动、退出、重启、文件锁、日志捕获 |
| `AiProcessLauncher` | 构造 `spawn(node, args, env)`，不经过 shell |
| `RuntimeLocator` | 解析 runtimeDir（显式/env/node_modules/ai/runtime） |
| `RuntimePackageValidator` | 校验 `package.json` name/version、`dist/server.mjs` |
| `NodeVersionChecker` | `node --version` ≥ 22.19 |
| `AiRuntimeHealthMonitor` | 周期检查 Node `/v1/health`，维护快照 |
| `AiAvailabilityGuard` | 业务请求前判定，返回 503/Retry-After |
| `AiProxyService` | 转发 method/path/query/body/headers，SSE 透传 |
| `AiProxyRoutes` | 精确 method+path 白名单 |
| `ProxyHeaderPolicy` | 请求/响应头过滤，注入 `X-Nexus-User` |
| `ProxyBodyLimiter` | 流式计数，超限 413 |
| `SseRelay` | 读取上游流并 flush 到客户端 |
| `ProxyErrorWriter` | Node 风格错误体 + requestId |
| `AiHealthEndpoint` | `/ai/v1/health`、`/health/live`、`/health/ready` |
| `AiRuntimeProxyEndpoint` | `/ai/v1/openapi.yaml`、`/ai/v1/agents` |
| `AiSessionProxyEndpoint` | `/ai/v1/sessions*`、`/ai/v1/runs*` |
| `AiStatusCode` | `NAI` 前缀状态码 |

## 5. 公共契约骨架

```java
public interface AiRuntime extends AutoCloseable {

    void start();

    void stop();

    AiRuntimeStatus status();

    AiRuntimeHealth health();

    Optional<AiEndpoint> endpoint();
}

public record AiEndpoint(URI baseUri) {
}

public enum AiRuntimeStatus {
    STOPPED, STARTING, RUNNING, DEGRADED, STOPPING, FAILED
}

public record AiRuntimeHealth(
        AiRuntimeStatus state,
        Instant since,
        String runtimeVersion,
        String nodeVersion,
        long pid,
        Instant lastCheckAt,
        long latencyMs,
        String configVersion,
        String error) {
}
```

## 6. 调用链

### 6.1 应用启动

```text
宿主启动
  → new DefaultAiRuntime(config)
  → start()
      → RuntimeLocator.locate()
      → RuntimePackageValidator.validate()
      → NodeVersionChecker.check()
      → 获取 dataDir 文件锁
      → AiProcessLauncher.spawn()
      → 读取 readyFile / stdout readiness
      → 轮询 Node /v1/health
      → AiRuntimeHealthMonitor.start()
```

- `EAGER`：宿主启动时调用 `start()`。
- `LAZY`：首次业务请求由 `AiAvailabilityGuard` 触发。
- `start()` 幂等；并发调用只有一个执行启动。

### 6.2 业务请求

```text
ConsoleAuthenticationFilter
  → ConsoleResourcePermissionFilter（ai.read/ai.write）
  → AiSessionProxyEndpoint
      → AiAvailabilityGuard.ensure()
          → health snapshot
          → STOPPED/LAZY → start()
          → 非 RUNNING → 503 + Retry-After
      → AiProxyService.forward()
          → AiProxyRoutes.match()
          → ProxyHeaderPolicy.prepare()
          → ProxyBodyLimiter.wrap()
          → JDK HttpClient.send()
          → 普通响应 / SseRelay
```

- 不可用时不建立 SSE，不做上游调用。
- 可用时原样转发状态码/响应体。
- 代理层失败使用 Node 风格错误体。

### 6.3 健康检查

```text
GET /ai/v1/health
  → AiHealthEndpoint
  → AiRuntime.health()
  → AiHealthView
  → 200/503
```

- 不调用 Node；读健康监控快照。
- `live` 只反映 JVM。
- `ready` 判断 state、config、lastCheck。

### 6.4 关闭

```text
宿主关闭
  → AiRuntime.close()
      → AiRuntimeHealthMonitor.stop()
      → AiProcessSupervisor.stop()
          → SIGTERM
          → 等待 shutdownTimeout
          → SIGKILL
      → 释放文件锁
```

## 7. 线程与并发

| 场景 | 策略 |
|------|------|
| 启动 | 单锁 + 状态机，避免并发 spawn |
| 健康检查 | 单线程 ScheduledExecutorService |
| 健康快照 | volatile 不可变对象 |
| 业务请求 | JAX-RS 线程池；SSE 占用线程直到断连 |
| 停止 | 幂等；停止中拒绝新请求 |
| 配置 | Node 侧负责；Java 不解析 |

- SSE 使用阻塞 `StreamingOutput`，需容器线程池足够；后续可切换 `SseEventSink`。
- 健康检查不阻塞业务请求。

## 8. 异常与状态码

- 应用可见失败统一 `NexusException.build(AiStatusCode.*, cause)`。
- Node 透传的业务错误保持 Node 错误体，不二次包装。
- 代理层失败：Node 风格错误体 + `requestId` + `Retry-After`。
- console 认证/授权失败由 console 异常映射返回 `R`。

## 9. 装配要求

### 9.1 Spring

```text
@Bean AiRuntime aiRuntime(AiRuntimeConfig config)
@Bean AiProxyConfig aiProxyConfig()
注册 AiHealthEndpoint / AiRuntimeProxyEndpoint / AiSessionProxyEndpoint
注册 console filters / exception mappers
consolePathPatterns += /ai/**
```

### 9.2 Quarkus

```text
@Produces AiRuntime aiRuntime(...)
注册 JAX-RS resources、console filters、exception mappers
```

模块本身不提供自动配置，避免 framework binding。

## 10. 边界

| 禁止 | 原因 |
|------|------|
| 在 Java 里解析 YAML/AI 配置 | 配置归 Node |
| 在 Java 里存会话/消息 | SessionManager 是唯一真源 |
| 在 Java 里做模型调用 | pi-agent-core 负责 |
| 代理配置/诊断端点 | 最小对外面 |
| 依赖 portal/platform | 依赖规则 |
| 使用 JDBC/ORM | 无 Java 持久化 |

## 11. 测试

| 层 | 内容 |
|----|------|
| 单元 | RuntimeLocator、版本校验、HeaderPolicy、Routes、BodyLimiter、SseRelay |
| 状态机 | start/stop/restart、并发 start、启动超时、FAILED/DEGRADED |
| 代理 | 路径/方法白名单、头过滤、413、503、Retry-After、SSE |
| 健康 | live/ready/health 转换、缓存、阈值 |
| 集成 | 真实 npm runtime + 随机端口 + `/ai/**` |
| 版本 | jar manifest 与 npm package 不一致拒绝启动 |
