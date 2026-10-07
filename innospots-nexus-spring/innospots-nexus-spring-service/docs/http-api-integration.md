# HTTP API 接入手册（Spring）

说明在 Spring Boot 应用中，如何通过 **innospots-nexus-spring-service** 使用 Nexus **HTTP 标准契约**：请求生命周期、`ServiceContext`、统一错误映射、以及经 `InvocationEngine` 的权限与治理。

---

## 1. 目标与能力边界

| 能力 | 由 adapter 提供 | 业务侧职责 |
|------|-----------------|------------|
| 每请求 `X-Request-Id`、Access Log、MDC | `ServiceServletFilter` / `ServiceWebFilter` | 使用 SLF4J 打日志，勿手动改 MDC |
| `ServiceContext` 线程绑定 | Filter + `ThreadBoundServiceContext` | 通过 `ServiceContextAccessor` 读取 |
| `NexusException` → HTTP JSON | `ServiceExceptionAdvice` / `ServiceWebExceptionHandler` + `HttpErrorMapper` | 抛出 `NexusException` |
| `@RequiresPermission` / `@RateLimited` 等 | **仅当**经 `ServiceInvocationBridge` 调用 | Controller 内显式 `bridge.invoke(...)` |

普通 `@GetMapping` **无需** Bridge 即可获得 requestId 与 access log；权限与治理注解 **不会** 因贴在 Controller 上而自动生效。

---

## 2. 接入步骤

### 步骤 1：添加依赖

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

WebFlux 项目将 `starter-web` 换为 `spring-boot-starter-webflux`。

### 步骤 2：显式启用 HTTP 装配

在 `@SpringBootApplication` 或配置类上标注 **`@EnableNexusServiceHttp`**（仅 REST 时不要用 `@EnableNexusService`，以免拉起 Stream/Transfer/WS）。

```java
@SpringBootApplication
@EnableNexusServiceHttp
public class OrderApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApiApplication.class, args);
    }
}
```

```yaml
service:
  enabled: true
  name: order-api
  response-profile: LEGACY
```

| 属性 | 默认 | 说明 |
|------|------|------|
| `service.enabled` | `true` | `false` 关闭整个 adapter |
| `service.name` | `nexus-service` | 日志/指标等服务名标签 |
| `service.response-profile` | `LEGACY` | 错误 JSON：`LEGACY` 或 `PROBLEM_DETAIL` |

绑定类：`ServiceProperties`（`service.*` 前缀）。

### 步骤 3：编写 REST 端点

**普通 JSON API**（自动观测，无治理）：

```java
@RestController
public class OrderResource {

    private final OrderQueryService orderQueryService;

    @GetMapping("/orders/{id}")
    public OrderVo get(@PathVariable String id) {
        return orderQueryService.find(id);
    }
}
```

**需要统一错误语义**：在 service 层或边界抛出平台异常：

```java
throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND);
```

adapter 将其映射为对应 HTTP 状态码与统一 JSON（由 `response-profile` 决定外形）。

### 步骤 4：需要权限或治理时 — 使用 Invocation Bridge

```java
@RestController
public class SecureResource {

    private final ServiceInvocationBridge bridge;

    public SecureResource(ServiceInvocationBridge bridge) {
        this.bridge = bridge;
    }

    @GetMapping("/secure")
    @RequiresPermission("order.read")
    public Map<String, String> secure() throws NoSuchMethodException {
        Method method = SecureResource.class.getMethod("secure");
        return bridge.invoke(this, method, "order.read", () -> Map.of("status", "ok"));
    }
}
```

要点：

- `operationId`（第三个字符串参数）用于日志、指标、**超时策略表键名**；应稳定且可检索。
- 注解上的 `value()` 是 **GovernanceConfig 中的策略键**，不是 `"10/min"` 字面量。

### 步骤 5：注册安全 SPI（可选）

```java
@Configuration
public class ServiceSecurityConfiguration {

    @Bean
    PermissionProvider permissionProvider() {
        return new MyPermissionProvider();
    }

    @Bean
    SecurityProvider securityProvider() {
        return new MySecurityProvider();
    }
}
```

`ServiceCoreConfiguration` 通过 `ObjectProvider` 注入；存在 Bean 时挂到 `ServiceRuntime`。

### 步骤 6：长耗时与客户端断开

在 `bridge.invoke` 的 supplier 内轮询取消标记，并在 Servlet 场景通知 Filter：

```java
return bridge.invoke(this, method, "adapter.slow", () -> {
    while (remaining > 0) {
        if (contextAccessor.requireCurrent().cancellation().isCancelled()) {
            ServiceServletFilter.cancelIfClientDisconnected(request);
            break;
        }
        // ...
    }
    return Map.of("status", "done");
});
```

WebFlux 使用 `ServiceWebFilter.cancelIfClientDisconnected(exchange)`（见流式手册中的取消协作）。

---

## 3. MVC 与 WebFlux 对照

| 项目 | Spring MVC | Spring WebFlux |
|------|------------|----------------|
| 启用注解 | `@EnableNexusServiceHttp` | 同上 |
| 条件配置 | `http.config.ServiceServletConfiguration` | `http.config.ServiceReactiveConfiguration` |
| 请求 Filter | `ServiceServletFilter` | `ServiceWebFilter` |
| 异常处理 | `ServiceExceptionAdvice` | `ServiceWebExceptionHandler` |
| `ServiceContext` | 线程绑定 | Reactor Context + 信号恢复 |
| Bridge 用法 | **相同** | **相同** |

不要在 WebFlux 的 event loop 线程上阻塞 `bridge.invoke` 内的长时间 `Thread.sleep`；应 offload 到 bounded 线程池。

---

## 4. 验证

1. 启动应用，请求任意 GET，响应头应含 `X-Request-Id`。
2. 触发 `NexusException`，检查状态码与 JSON 与 `response-profile` 一致。
3. 对照模块测试：`SampleHttpResource`、`AdapterScenarioMvcTest` / `AdapterScenarioWebFluxTest`。

---

## 5. 二次扩展开发

### 5.1 切换错误响应外形

仅配置（无需改代码）：

```yaml
service:
  response-profile: PROBLEM_DETAIL
```

### 5.2 自定义 `HttpErrorMapper` 行为

当前 `ServiceCoreConfiguration` 注册默认 `HttpErrorMapper` Bean。若需扩展映射规则，在应用中：

1. 启用 `spring.main.allow-bean-definition-overriding=true`（仅当团队接受 Bean 覆盖），**或**
2. 新建配置类，**不** `@Import(ServiceCoreConfiguration)`，自行注册 `HttpErrorMapper` 与下游 Filter（适合深度定制）。

推荐优先使用 `NexusException` + 既有 `HttpErrorMapper`，避免在业务模块复制映射表。

### 5.3 扩展 `ServiceInvocationBridge` 调用模式

`ServiceInvocationBridge` 为 `final` 类，**不宜继承**。扩展方式：

- **包装**：注入 `InvocationEngine`，自行构造 `InvocationContext` 调用 `engine.invokeSync` / `invokeStream`（与 Bridge 相同，适合非 Controller 入口）。
- **流式下游**：对内 `Flow.Publisher` 可用 `ServiceInvocationBridge.invokeStream`；写到对外 `StreamSession` 可用 `StreamPublisherRelay`，见 [流式接入](stream-integration.md)。
- **策略解析**：实现或替换 `AnnotationPolicyResolver`（需自定义 `ServiceCoreConfiguration` 等价装配）。

### 5.4 自定义 Filter 顺序

`ServiceMvcConfiguration` 将 `ServiceServletFilter` 注册为 `Ordered.HIGHEST_PRECEDENCE + 20`。若需在其前后插入 Filter，使用 `FilterRegistrationBean` 显式 `setOrder`，避免重复建立 `ServiceContext`。

### 5.5 宿主 Principal 桥接（规划）

设计文档中的 `SpringNativePrincipalProvider` 用于把 Spring Security `Authentication` 映射为 `ServicePrincipal`；按 adapter 版本查阅是否已提供。未提供时实现 `SecurityProvider` 读取宿主上下文即可。

---

## 6. 相关类与文档

- `com.innospots.nexus.spring.service.bootstrap.EnableNexusServiceHttp`
- `com.innospots.nexus.spring.service.http.invocation.ServiceInvocationBridge`
- `com.innospots.nexus.spring.service.http.mvc.ServiceServletFilter`
- [service-http README](../../../innospots-nexus-service/innospots-nexus-service-http/README.md)
- [治理接入](governance-integration.md)（限流/超时与 Bridge 配合）
