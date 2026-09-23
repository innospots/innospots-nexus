# innospots-nexus-service-runtime

## 模块简介

服务框架的**运行时引擎**：组装拦截器链、执行 `InvocationEngine`、传播 `ServiceContext`、协调审计/幂等/鉴权。  
**业务模块一般不要直接依赖**；由 Spring/Quarkus adapter 启动并注入。平台扩展或显式调用执行引擎时除外。

## 何时使用

| 场景 | 是否直接依赖 |
|---|---|
| 普通业务 CRUD | 否（经 adapter 透明获得） |
| 自定义 `ServiceInterceptor` | 是 |
| 手动调用 `InvocationEngine` | 是 |
| 单元测试拦截器链 | 是（test scope） |

## 包结构

```text
com.innospots.nexus.service.runtime
├── lifecycle        # ServiceRuntime 启动/关闭
├── invocation       # InvocationEngine、InvocationControl
├── context          # ThreadBoundServiceContext、ContextSnapshot、ContextPropagation
├── policy           # AnnotationPolicyResolver（注解 → OperationPolicy）
├── security         # AuthenticationInterceptor、AuthorizationInterceptor
├── audit            # AuditInterceptor、AuditDispatcher
├── idempotency      # IdempotencyCoordinator、InMemoryIdempotencyStore
├── cancellation     # CancellationSource
└── time             # DeadlineScheduler、DeadlineInterceptor
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-runtime</artifactId>
</dependency>
```

传递依赖 `innospots-nexus-service-contract`。

## 快速接入

### 场景 1：在 adapter 中启动运行时（Spring 已内置）

Spring 应用引入 `innospots-nexus-spring-service` 后，`ServiceCoreConfiguration` 会自动：

```text
ServiceRuntime.builder()
    .contexts(threadBoundServiceContext)
    .addInterceptor(rateLimitInterceptor)
    .addInterceptor(timeoutInterceptor)
    .securityProvider(optional)
    .permissionProvider(optional)
    .build()
    .start();
```

业务无需手写上述代码。

### 场景 2：注册自定义拦截器

```java
@Bean
ServiceRuntime serviceRuntime(
        ThreadBoundServiceContext contexts,
        MyBusinessInterceptor myInterceptor) {
    ServiceRuntime runtime = ServiceRuntime.builder()
            .contexts(contexts)
            .addInterceptor(myInterceptor)
            .build();
    runtime.start();
    return runtime;
}
```

`MyBusinessInterceptor` 实现 `ServiceInterceptor`，在 `before`/`after` 中读写 `InvocationContext`。

### 场景 3：注解策略解析（测试或工具）

```java
AnnotationPolicyResolver resolver = new AnnotationPolicyResolver();
OperationPolicy policy = resolver.resolve(method);
```

## 配置说明

运行时本身无独立 YAML。治理策略通过 `GovernanceConfig`（Java 或 adapter 装配）注入；审计/幂等行为由 `AuditInterceptor`、`IdempotencyCoordinator` 与 SPI 决定。

## 幂等与审计（当前实现）

| 能力 | 说明 |
|---|---|
| 审计 | `AuditInterceptor`：REQUIRED 无存储则拒绝；BEST_EFFORT 尽力 dispatch |
| 幂等 | `InMemoryIdempotencyStore` + 请求头 `idempotency-key`（单 JVM） |

生产持久化需上层实现 `AuditStorage` 并替换默认 dispatch 路径（adapter 装配）。

## 注意事项

- 不得依赖 Spring/Quarkus/Servlet。
- 新 public API 需至少两个上层消费者，或标记 experimental。
- Domain 业务代码禁止 import `com.innospots.nexus.service.runtime.*`（除平台扩展）。

## 相关文档

- [运行时设计](../../docs/service-runtime-design.md)
- [Spring adapter README](../../innospots-nexus-spring/innospots-nexus-spring-service/README.md)
