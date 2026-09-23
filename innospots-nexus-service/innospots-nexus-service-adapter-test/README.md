# innospots-nexus-service-adapter-test

## 模块简介

Spring 与 Quarkus adapter 共享的**黑盒回归场景库**（12 个场景）。  
使用 JDK `HttpClient` / `WebSocket`，不依赖 Spring Test 或 Quarkus Test 注解，保证两个 adapter 行为一致。

## 何时使用

| 场景 | 用法 |
|---|---|
| 新增 adapter 或改边界行为 | 跑 `AdapterScenarioRunner` |
| 新增跨框架契约场景 | 实现 `AdapterScenario` 并加入 runner |
| 生产业务代码 | **不要**依赖本模块 |

## 包结构

```text
com.innospots.nexus.service.adapter.test
├── scenario           # HttpContextScenario、WebSocketSessionScenario、HttpDownloadScenario …
├── runner             # AdapterScenarioRunner
├── client             # HttpTestClient
├── fixture            # AdapterSampleWebSocketHandler、AdapterDownloadFixtures …
└── support            # AdapterTestTarget（baseUrl + 默认头）
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-adapter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 快速接入

### 场景 1：Spring MVC 跑全量场景

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AdapterScenarioMvcTest {

    @LocalServerPort int port;

    @Test
    void runsSharedAdapterScenarios() {
        AdapterTestTarget target = new AdapterTestTarget(
                "http://localhost:" + port, Map.of());
        new AdapterScenarioRunner().runAll(target);
    }
}
```

WebFlux / Quarkus 同理，换测试 Application 与 `@Profile("webflux")` 即可。

### 场景 2：只跑单个场景

```java
new HttpContextScenario().run(target);
new WebSocketPermissionScenario().run(target);
```

### 场景 3：自定义请求头（如 requestId）

```java
AdapterTestTarget target = new AdapterTestTarget(
        baseUrl,
        Map.of(StandardHeaders.REQUEST_ID, "req-test-001"));
new HttpContextScenario().run(target);
```

## 默认场景列表

| 场景 | 验证点 |
|---|---|
| HttpUnaugmentedScenario | 普通 GET 200 |
| HttpContextScenario | requestId 透传 |
| HttpErrorScenario | 401/403/404/429/500 映射 |
| HttpSecureScenario | PermissionProvider |
| HttpSlowScenario | 取消/慢请求 |
| StreamSseScenario / StreamCancelScenario | SSE |
| HttpDownloadScenario | DownloadResource 写回 |
| WebSocketSessionScenario | onOpen ready + 关闭 |
| WebSocketPermissionScenario | 未知消息类型错误码 |
| WebSocketGovernanceRateLimitScenario | 入站消息限流第二条 error envelope |
| GovernanceRateLimitScenario / GovernanceTimeoutScenario | 治理 |
| ObservabilityTraceScenario | trace 头占位 |

路径常量见 `AdapterScenarioPaths`。

## 夹具约定

- `AdapterSampleWebSocketHandler`：标准 `WebSocketHandler` 示例。
- `AdapterDownloadFixtures.memoryDownload()`：内存下载字节 `download-ok`。
- 各 adapter 测试模块注册**薄端点**，委托 `*EndpointBridge`。

## Maven 验证

| 范围 | 命令 |
|------|------|
| 本模块契约（场景注册表，无网络） | `mvn test -pl innospots-nexus-service/innospots-nexus-service-adapter-test` |
| 全量黑盒（Spring MVC / WebFlux + Quarkus） | `mvn test -pl innospots-nexus-spring/innospots-nexus-spring-service,innospots-nexus-quarkus/innospots-nexus-quarkus-service -am` |

`mvn test -pl innospots-nexus-service` 会执行 adapter-test 的契约测试；宿主级场景在 Spring/Quarkus 模块中运行。

## 注意事项

- 本模块 **禁止**依赖 Spring/Quarkus。
- 需要随机端口 + 网络（本地 loopback）；CI 需 `required_permissions` 或等价网络权限。
- 新增场景应同时能在 MVC、WebFlux、Quarkus 三套夹具上绿。

## 相关文档

- [Adapter 设计](../../docs/service-adapter-design.md)
- [Spring adapter README](../../innospots-nexus-spring/innospots-nexus-spring-service/README.md)
