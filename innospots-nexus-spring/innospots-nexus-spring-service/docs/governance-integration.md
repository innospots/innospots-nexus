# 熔断与限流治理接入手册（Spring）

说明在 Spring 应用中配置 **限流、超时、舱壁、熔断** 等治理能力：契约注解、`GovernanceConfig` 策略表、`ServiceInvocationBridge` 执行路径，以及扩展拦截器链。

---

## 1. 核心结论（必读）

| 事实 | 说明 |
|------|------|
| 注解位置 | `@RateLimited`、`@BulkheadProtected`、`@CircuitProtected`、`@TimeoutProtected` 定义在 `service-contract` |
| 执行入口 | **必须** `ServiceInvocationBridge.invoke(...)` 进入 `InvocationEngine` |
| 仅贴注解在 Controller | **不会** 触发限流/熔断 |
| 默认已注册拦截器 | `RateLimitInterceptor`、`TimeoutInterceptor`（见 `ServiceCoreConfiguration`） |
| 默认未注册 | `BulkheadInterceptor`、`CircuitBreakerInterceptor` — 须自行 `addInterceptor` |

---

## 2. 接入步骤（限流 + 超时）

### 步骤 1：定义策略键（注解）

```java
@GetMapping("/promo/claim")
@RateLimited("promo.claim")
public Map<String, String> claim() throws NoSuchMethodException {
    Method method = PromoResource.class.getMethod("claim");
    return bridge.invoke(this, method, "promo.claim", () -> Map.of("status", "ok"));
}
```

注解 `value()` 是 **配置 Map 的键名**，不是 `"10 per minute"` 字符串。

### 步骤 2：配置 `GovernanceConfig`

默认 Bean 在 `ServiceCoreConfiguration.serviceGovernanceConfig()`（含测试用 `adapter-rate-limit`、`adapter.governance.timeout`）。

生产环境自定义示例：

```java
@Configuration
public class GovernanceConfiguration {

    @Bean
    GovernanceConfig serviceGovernanceConfig() {
        return new GovernanceConfig(
                Map.of("promo.claim", new RateLimitPolicy(10, 1.0D)),
                Map.of("report.export", new BulkheadPolicy(4)),
                Map.of("payment.client", CircuitBreakerPolicy.defaults()),
                Map.of(
                        "payment.client", Duration.ofSeconds(5),
                        "adapter.governance.timeout", Duration.ofMillis(500)),
                100_000,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
    }
}
```

`GovernanceConfig` 字段说明：

| 字段 | 说明 |
|------|------|
| `rateLimits` | 键 → `RateLimitPolicy(burst, refillPerSecond)` |
| `bulkheads` | 键 → `BulkheadPolicy(maxConcurrent)` |
| `circuits` | 键 → `CircuitBreakerPolicy` |
| `timeouts` | 键 → `Duration`；查找键常用 **bridge 的 operationId** |
| `defaultHttpTimeout` | 默认 HTTP 超时 |
| `defaultStreamTimeout` | 流式默认超时 |
| `defaultWebSocketTimeout` | WebSocket 消息默认超时 |

### 步骤 3：限流存储（本地 / Redis）

默认 `service.governance.rate-limit.store=local`（`LocalTokenBucketProvider`，单 JVM）。

跨服务 / 多副本按客户共享配额：

```yaml
service:
  governance:
    rate-limit:
      store: redis
      redis:
        uri: redis://127.0.0.1:6379/0
        key-prefix: "nexus:ratelimit:"
```

认证层写入 `ServicePrincipal.attributes["customerId"]`；策略使用 `new RateLimitPolicy(burst, refill, RateLimitDimensionMode.CUSTOMER)` 即可按客户限流。详见 governance [rate-limit.md](../../../innospots-nexus-service/innospots-nexus-service-governance/docs/rate-limit.md)。

### 步骤 4：超时与取消协作

`bridge.invoke(..., "adapter.governance.timeout", () -> { ... })` 时，`timeouts` 表中同键的 `Duration` 生效。

业务长循环应检查：

```java
if (contextAccessor.requireCurrent().cancellation().isCancelled()) {
    throw NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED);
}
```

拒绝时 HTTP **504** / `DEADLINE_EXCEEDED`；限流 **429** / `LIMIT_EXCEEDED`（带 `Retry-After`）。

### 步骤 5：与权限组合

```java
@RequiresPermission("order.export")
@BulkheadProtected("report.export")
public void export() throws NoSuchMethodException {
    Method m = ExportResource.class.getMethod("export");
    bridge.invoke(this, m, "report.export", () -> { doExport(); return null; });
}
```

须注册 `PermissionProvider` Bean。

---

## 3. 舱壁与熔断（扩展默认链）

实现类已存在于 `innospots-nexus-service-governance`：

- `SemaphoreBulkheadProvider` + `BulkheadInterceptor`
- `Resilience4jCircuitBreakerProvider` + `CircuitBreakerInterceptor`

### 步骤 1：注册 Provider Bean

```java
@Bean
BulkheadInterceptor bulkheadInterceptor(GovernanceConfig config) {
    return new BulkheadInterceptor(new SemaphoreBulkheadProvider(config));
}

@Bean
CircuitBreakerInterceptor circuitBreakerInterceptor(GovernanceConfig config) {
    return new CircuitBreakerInterceptor(new Resilience4jCircuitBreakerProvider(config));
}
```

### 步骤 2：加入 `ServiceRuntime`

默认 `serviceRuntime` Bean **只** 添加限流与超时。扩展方式：自定义 `ServiceRuntime` 装配（见下文「二次扩展」）。

熔断适用于 **下游 Client 调用** 的 operationId，不建议对所有入站 HTTP 无差别开启。

---

## 4. 拦截器链顺序（框架无关）

```text
Control → Deadline → Authentication → Authorization → Audit → Idempotency
  → RateLimit → Timeout → Bulkhead → Circuit → 业务
```

Spring adapter 当前追加：`RateLimitInterceptor`、`TimeoutInterceptor`。

---

## 5. 配置与 YAML

`ServiceProperties` **仅** 含 `enabled` / `name` / `response-profile`。治理数值 **全部** 在 `GovernanceConfig` Java Bean 中维护。

设计文档中的 `service.governance.*` YAML 绑定见 `service-adapter-design.md`；落地以项目版本为准。

---

## 6. 验证

参考 `SampleHttpResource.governanceRateLimit()`、`governanceTimeout()` 与 `AdapterScenarioMvcTest`。

- 连续请求触发 429
- 慢操作触发 504 / 取消

---

## 7. 二次扩展开发

### 7.1 替换 `GovernanceConfig`

`ServiceCoreConfiguration` 中 `serviceGovernanceConfig` **无** `@ConditionalOnMissingBean`。可选做法：

1. **`spring.main.allow-bean-definition-overriding=true`**，在应用中声明同名 `@Bean GovernanceConfig serviceGovernanceConfig()`（团队需规范覆盖范围）。
2. **自定义运行时配置类**：复制 `ServiceCoreConfiguration` 中 `serviceRuntime` / 拦截器相关 `@Bean`，按团队策略组装，并从 `@Import` 中排除默认 `ServiceCoreConfiguration`（仅保留 MVC/WebFlux 配置）。

### 7.2 扩展 `ServiceRuntime` 拦截器链

```java
@Bean
ServiceRuntime serviceRuntime(
        ThreadBoundServiceContext contexts,
        ObjectProvider<SecurityProvider> securityProvider,
        ObjectProvider<PermissionProvider> permissionProvider,
        RateLimitInterceptor rateLimitInterceptor,
        TimeoutInterceptor timeoutInterceptor,
        BulkheadInterceptor bulkheadInterceptor,
        CircuitBreakerInterceptor circuitBreakerInterceptor) {

    ServiceRuntime.Builder builder = ServiceRuntime.builder()
            .contexts(contexts)
            .addInterceptor(rateLimitInterceptor)
            .addInterceptor(timeoutInterceptor)
            .addInterceptor(bulkheadInterceptor)
            .addInterceptor(circuitBreakerInterceptor);

    securityProvider.ifAvailable(builder::securityProvider);
    permissionProvider.ifAvailable(builder::permissionProvider);

    ServiceRuntime runtime = builder.build();
    runtime.start();
    return runtime;
}
```

注意：若保留默认 `serviceRuntime` Bean，会产生重复定义；须用覆盖或排除策略 **只保留一个** `ServiceRuntime`。

### 7.3 自定义 `ServiceInterceptor`

实现 `com.innospots.nexus.service.contract.invocation.ServiceInterceptor`，通过 `ServiceRuntime.builder().addInterceptor(...)` 注册。适合团队专有配额、租户级限流等。

### 7.4 自定义 `DeadlineOperationTimeoutArmer`

Spring 已提供 `DeadlineOperationTimeoutArmer` 连接 `DeadlineScheduler` 与 `ServiceRequestLifecycleAccessor`。替换该 Bean 可改变超时与 Servlet 生命周期挂接方式。

### 7.5 分布式限流

默认 `store=local` 为单 JVM 令牌桶。多副本共享配额时设置 `service.governance.rate-limit.store=redis`，由模块
`innospots-nexus-service-governance-redis` 注册 `RedisTokenBucketRateLimitProvider`（Lettuce + Lua）。
拦截器不变；Redis 故障时 Provider **失败关闭**（`SYSTEM_ERROR`），不自动降级本地桶。
`GovernanceConfig.maxRateLimitKeys` 仅约束本地 Map，Redis 路径靠 `bucket-ttl-seconds` 过期键。
详见 [rate-limit.md §9](../../../innospots-nexus-service/innospots-nexus-service-governance/docs/rate-limit.md)。

仍需自定义配额算法时，可实现 `RateLimitProvider` 并替换 Spring Bean。

---

## 8. 相关文档

- [service-governance README](../../../innospots-nexus-service/innospots-nexus-service-governance/README.md)
- [HTTP API 接入](http-api-integration.md)（Bridge 基础用法）
