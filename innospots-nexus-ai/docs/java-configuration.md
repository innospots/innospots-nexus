# Java 配置与装配设计

## 1. 文档定位

- 状态：草案
- 范围：`AiRuntimeConfig`、`AiProxyConfig`、环境变量、校验、Spring/Quarkus 装配
- 关联：[java-module-design.md](java-module-design.md)、[java-runtime-supervisor.md](java-runtime-supervisor.md)

Java 配置只描述 **宿主与代理**；模型、AgentProfile、工具、MCP、FileRoot 在 Node 侧配置。

## 2. 配置分层

```text
宿主配置（Spring/Quarkus/env）
  → AiRuntimeConfig / AiProxyConfig
      → Java supervisor / 代理
Node 配置（ConfigSource / YAML）
  → Runtime / Provider / Model / AgentProfile / Tool / McpServer / FileRoot
```

两者不互相覆盖：Java 不解析 Node YAML；Node 不读取 Java 配置。

## 3. `AiRuntimeConfig`

```java
public record AiRuntimeConfig(
        Path dataDir,
        Path configDir,
        Path runtimeDir,
        String nodeExecutable,
        String bindHost,
        int port,
        Duration startupTimeout,
        Duration shutdownTimeout,
        int maxRestarts,
        StartupPolicy startupPolicy,
        Duration healthCheckInterval,
        Duration healthTimeout,
        Duration healthCacheTtl,
        int unhealthyThreshold,
        boolean restartOnUnhealthy,
        String configUrl,
        Map<String, String> environment) {
}

public enum StartupPolicy {
    EAGER, LAZY
}
```

### 3.1 字段

| 字段 | 默认 | 校验 |
|------|------|------|
| `dataDir` | 必填 | 可创建、可写；用于 sessions/logs/secrets/runtime.json |
| `configDir` | `<dataDir>/config` | 可为空；不存在时 Node 以空配置启动 |
| `runtimeDir` | 空 | 空时按定位顺序查找；必须含 package.json + dist/server.mjs |
| `nodeExecutable` | `node` | 可执行；版本 ≥ 22.19 |
| `bindHost` | `127.0.0.1` | 只允许 `127.0.0.1` / `::1` |
| `port` | `0` | 0 或 1–65535；Java 默认随机 |
| `startupTimeout` | `30s` | > 0 |
| `shutdownTimeout` | `10s` | > 0 |
| `maxRestarts` | `3` | ≥ 0 |
| `startupPolicy` | `EAGER` | EAGER/LAZY |
| `healthCheckInterval` | `5s` | ≥ 1s |
| `healthTimeout` | `1s` | < interval |
| `healthCacheTtl` | `2s` | ≥ 0 |
| `unhealthyThreshold` | `3` | ≥ 1 |
| `restartOnUnhealthy` | `false` | - |
| `configUrl` | 空 | http/https；可选 |
| `environment` | 空 | 仅白名单键 |

### 3.2 启动策略

| 值 | 行为 |
|----|------|
| `EAGER` | 宿主启动时 `AiRuntime.start()`；服务部署推荐 |
| `LAZY` | 首次业务请求触发；嵌入式/工具推荐 |

readiness 不隐式启动；`EAGER` 未启动成功时 ready=503。

## 4. `AiProxyConfig`

```java
public record AiProxyConfig(
        String pathPrefix,
        int maxRequestBodyBytes,
        Duration connectTimeout,
        Duration requestTimeout,
        Duration sseIdleTimeout,
        List<ProxyRoute> allowedRoutes,
        List<ProxyRoute> anonymousRoutes,
        boolean failFastOnUnavailable,
        Duration retryAfter) {
}

public record ProxyRoute(String method, String path) {
}
```

| 字段 | 默认 | 说明 |
|------|------|------|
| `pathPrefix` | `/ai` | 对面前缀 |
| `maxRequestBodyBytes` | `8 MiB` | 超限 413 |
| `connectTimeout` | `5s` | 连接 Node |
| `requestTimeout` | `60s` | 普通请求 |
| `sseIdleTimeout` | `120s` | SSE 空闲 |
| `allowedRoutes` | 10 条 | 精确 method+path |
| `anonymousRoutes` | `/v1/openapi.yaml` | health 由本地端点处理 |
| `failFastOnUnavailable` | `true` | 不转发未就绪请求 |
| `retryAfter` | `5s` | 503 Retry-After 默认 |

`allowedRoutes` 必须与 `java-api-design.md` 的路由表一致，不允许 `/**` 通配。

## 5. 环境变量

| 变量 | 对应 |
|------|------|
| `NEXUS_AI_DATA_DIR` | `dataDir` |
| `NEXUS_AI_CONFIG_DIR` | `configDir` |
| `NEXUS_AI_RUNTIME_DIR` | `runtimeDir` |
| `NEXUS_AI_NODE` | `nodeExecutable` |
| `NEXUS_AI_HOST` | `bindHost`（仅 loopback） |
| `NEXUS_AI_PORT` | `port`（Java 默认 0） |
| `NEXUS_AI_CONFIG_URL` | `configUrl` |
| `NEXUS_AI_LOG_LEVEL` | 传给 Node |

优先级：宿主配置 > 环境变量 > 默认值。

## 6. 校验

```text
dataDir 可创建/可写
configDir 可读或不存在
runtimeDir 存在且含 package.json/dist/server.mjs
node 可执行且版本 ≥ 22.19
bindHost 是回环
port 范围
timeouts > 0
healthTimeout < healthCheckInterval
allowedRoutes 非空且无通配
consolePathPatterns 包含 /ai/**
```

失败：`INVALID_RUNTIME_CONFIG`（400/500，取决于启动路径），不启动 Node。

## 7. 装配示例

### 7.1 Spring Boot

```java
@Configuration
public class AiConfiguration {

    @Bean
    public AiRuntimeConfig aiRuntimeConfig(AiProperties props) {
        return new AiRuntimeConfig(
                props.dataDir(),
                props.configDir(),
                props.runtimeDir(),
                props.nodeExecutable(),
                "127.0.0.1",
                0,
                Duration.ofSeconds(30),
                Duration.ofSeconds(10),
                3,
                StartupPolicy.EAGER,
                Duration.ofSeconds(5),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2),
                3,
                false,
                props.configUrl(),
                Map.of());
    }

    @Bean
    public AiRuntime aiRuntime(AiRuntimeConfig config) {
        return new DefaultAiRuntime(config);
    }

    @Bean
    public AiProxyConfig aiProxyConfig() {
        return AiProxyConfig.defaults();
    }
}
```

还需：

- 注册 3 个端点类。
- 注册 console filters/exception mappers。
- `consolePathPatterns` 增加 `/ai/**`。
- 启动时调用 `aiRuntime.start()` 或依赖 EAGER 生命周期。
- 关闭时调用 `aiRuntime.close()`。

### 7.2 Quarkus

```java
@ApplicationScoped
public class AiProducer {
    @Produces
    @Singleton
    AiRuntime aiRuntime(AiRuntimeConfig config) {
        return new DefaultAiRuntime(config);
    }
}
```

- 用 `@Startup`/`@Observes StartupEvent` 调 `start()`。
- 用 `@PreDestroy` 调 `close()`。
- 注册 JAX-RS 资源与 console Providers。

## 8. 运行时目录

```text
<app.dir>/node_modules/@innospots/nexus-ai-runtime/
├── package.json
├── dist/server.mjs
├── bin/nexus-ai-runtime.mjs
└── config/
```

`runtimeDir` 指向包根；supervisor 使用 `bin/nexus-ai-runtime.mjs` 启动。

## 9. 配置示例

```yaml
nexus:
  ai:
    data-dir: /srv/nexus-ai
    config-dir: /etc/nexus-ai
    runtime-dir: /app/node_modules/@innospots/nexus-ai-runtime
    node-executable: node
    startup-policy: EAGER
    health-check-interval: 5s
    health-timeout: 1s
    unhealthy-threshold: 3
    proxy:
      max-request-body-bytes: 8388608
      connect-timeout: 5s
      request-timeout: 60s
      sse-idle-timeout: 120s
```

## 10. 测试

- 缺失/非法 dataDir。
- configDir 不存在时仍可启动。
- runtimeDir 缺失/版本不一致。
- Node 版本检查。
- EAGER/LAZY 行为。
- health timeouts 校验。
- allowedRoutes 与接口表一致。
- 环境变量优先级。
