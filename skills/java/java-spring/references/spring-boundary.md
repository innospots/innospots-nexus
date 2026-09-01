# Spring 在本仓库的适用边界

---

## 硬边界

| 位置 | 约束 | 权威来源 |
|------|------|---------|
| 管理端 REST 端点 | 只用 `jakarta.ws.rs` 注解 | `standards/code-style.md` → REST Endpoints |
| 端点返回类型 | 必须 `R<T>` / `R<PageResult<T>>` / `R<Void>` | `standards/api-design.md` → REST Endpoint Contracts |
| 事务注解 | 只用 `jakarta.transaction.Transactional` | `standards/api-design.md` → Transaction Boundaries |
| `innospots-nexus-base` | 不得引入任何 Spring 或其他运行时框架 | `AGENTS.md` |
| `innospots-nexus-core` | 可依赖中间件 API，但**不得**绑定 Spring Boot 自动配置 | `AGENTS.md` |
| service / operator | **不得**返回 `R<T>`；异常不在下层转响应 | `standards/api-design.md` |

**这些规范优先于 Spring 的惯用写法。** 冲突时以 `standards/` 为准。

---

## 注解对照表

在 Spring 工程与本仓库之间做映射或迁移时使用。

| 能力 | Spring MVC | 本仓库（Jakarta REST） |
|------|-----------|----------------------|
| 资源类 | `@RestController` / `@Controller` | 类级 `@Path`，无 `@RestController` |
| 请求映射 | `@RequestMapping` / `@GetMapping` / `@PostMapping` | 类级 `@Path` + 方法级 `@GET` / `@POST` / `@PUT` / `@DELETE` |
| 媒体类型 | `produces` / `consumes` 属性 | `@Produces` / `@Consumes` |
| 路径参数 | `@PathVariable` | `@PathParam` |
| 查询参数 | `@RequestParam` | `@QueryParam` |
| 请求头 | `@RequestHeader` | `@HeaderParam` |
| 参数默认值 | `@RequestParam(defaultValue=...)` | `@DefaultValue` |
| 组合参数 | 无直接对应 | `@BeanParam` |
| 请求体 | `@RequestBody` | 直接收请求 record（无额外注解） |
| 校验 | `@Valid` / `@Validated` | 在 record `validate()` 或领域边界做 |
| 异常处理 | `@RestControllerAdvice` + `@ExceptionHandler` | 端点基础设施集中把 `NexusException` 映射为 `R.fail(...)` |
| 事务 | `@Transactional`（Spring） | `@Transactional`（`jakarta.transaction`） |

---

## 依赖注入：概念对照

本项目的构造器注入 + `final` 字段 + Lombok `@RequiredArgsConstructor` 与
Spring 推荐的构造器注入**完全一致**，无需适配。

| Spring 概念 | 本仓库对应 |
|------------|-----------|
| `@Component` / `@Service` / `@Repository` | 按职责落包：`service` / `operator` / `dao`；托注册入方式取决于运行时 |
| `@Autowired` 构造器注入 | `@RequiredArgsConstructor` + `final` 字段 |
| `@Value` / `@ConfigurationProperties` | 模块级 `config` 包 + `*Config` 类型 |
| `@Bean` 工厂方法 | 静态工厂 `of()` / `create()` / `from()` / `named()` |
| `@PostConstruct` / `@PreDestroy` | 明确的生命周期方法：`initialize` / `start` / `stop` / `destroy` / `close` |
| `@Profile` 条件装配 | 配置驱动；避免用 profile 表达业务分支 |
| `@ConditionalOnMissingBean` 扩展点 | `contract` / `declaration` / `discovery` 包构成的扩展边界 |

### 关键差异

Spring 严重依赖**代理**（事务、AOP、缓存、异步）。本仓库更强调**显式边界**：

- 事务在 service 方法上显式声明，而不是靠代理织入所有 public 方法
- 生命周期由类型自己暴露并文档化，而不是靠容器回调
- 跨模块解耦用领域事件（`EventBus`），而不是靠 Bean 注入跨模块服务

因此从 Spring 迁移到本仓库时，最容易出错的是**依赖代理的隐式行为**：
自调用失效、事务边界不清、跨模块直接注入服务。

---

## 从 Spring 工程迁移代码到本仓库的步骤

```text
1. 拆注解       去掉 Spring MVC / Spring 事务注解，换成 jakarta.* 对应物
2. 定归属       按 AGENTS.md 判断落哪个 Maven 模块
3. 拆分层       Controller → endpoint；Service → service/operator；Repository → dao
4. 换持久化     Spring Data Repository → MyBatis-Plus BaseMapper；注意单表约束与禁 join
5. 换异常       Spring 异常体系 → NexusException + StatusCode
6. 换响应       ResponseEntity → R<T> / R<PageResult<T>>；service 不得返回 R
7. 换校验       Bean Validation → record validate() + 领域不变量
8. 补契约测试   端点/实体/DAO/状态码契约测试
9. 过检查       java:check
```

### 迁移中的高频坑

| 坑 | 说明 |
|----|------|
| 把 `ResponseEntity` 语义带进 service | service/operator 只返回领域值或 `PageResult<T>` |
| Spring Data 的 join fetch 习惯 | 本仓库 DAO 禁止 join，改用分批查询 + 内存组装 |
| 依赖 ORM 的懒加载 | MyBatis-Plus 无懒加载语义，跨表数据必须显式分批查询 |
| `@Transactional` 自调用失效的经验不适用 | 但事务边界仍需显式声明在 service |
| 用 Spring `@Async` | 本仓库用显式的 `ThreadPoolBuilder` 与生命周期管理 |
| Bean Validation 散落各处 | 校验归属到 record 构造器 / `validate()` / operator / service |
| 异常处理器逐方法写 | 端点基础设施集中映射 |

---

## 何时可以引入 Spring 组件

在本仓库引入 Spring 相关依赖前，必须同时满足：

1. 不落在 `innospots-nexus-base`（该模块零中间件约束）
2. 不落在 `innospots-nexus-core` 的自动配置形态
3. 不进入 `domain` 包或端点契约
4. 已通过 `java:design` 的技术选型评估（必要性、传递依赖、许可证、维护活跃度、退出成本）
5. 版本已登记到 `innospots-nexus-bom`

若只是需要一个 Spring 生态的**库**（如某客户端、工具类）而非 Spring 容器本身，
评估是否能用更轻量的替代——本仓库的基调是「轻量、依赖最少」。
