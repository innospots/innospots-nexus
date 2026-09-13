---
name: java:spring
display_name: Spring / Spring Boot 专项
description: |
  Spring 与 Spring Boot 专项能力。当用户处理本仓库 Spring 运行时组装、依赖版本、
  配置与迁移评估时使用。强调：Spring 版本跟随 innospots-nexus-bom，禁止模块内联
  版本；禁止 Spring Data 与 Spring Security（持久化用 MyBatis-Plus，鉴权用
  kernel/console）。端点仍用 Jakarta REST，不用 Spring MVC。
  触发词：Spring、Spring Boot、starter、自动配置、BOM、spring-boot 版本、
  依赖注入、@Configuration、迁移评估。
category: java
version: 1.1.0
---

# Spring / Spring Boot 专项能力

## 先读：本仓库的适用边界

`innospots-nexus` **不是** Spring Boot 工程。使用本技能时必须先区分两件事：

| 场景 | 约束 |
|------|------|
| 在本仓库写管理端 REST 端点 | **必须用 `jakarta.ws.rs` 注解**。禁止 `@RestController`、`@RequestMapping`、`@GetMapping`、`@PostMapping` 等 Spring MVC 注解 |
| 在本仓库声明事务 | **必须用 `jakarta.transaction.Transactional`**。禁止 `org.springframework.transaction.annotation.Transactional` |
| `innospots-nexus-core` | **不得**绑定 Spring Boot 自动配置（业务中立约束） |
| `innospots-nexus-base` | **不得**引入任何 Spring 或其他运行时框架依赖 |
| 其他 Spring 工程、Spring Boot 应用、迁移评估 | 本技能的全部内容适用 |

端点与事务规范通过 `java:reference` 消费（`quick-constraints.md`、`standards-index.md`），
**优先级高于** Spring 的惯用写法。

Spring 集成边界未定时（如 filter 与 `SessionContext` 绑定、Boot 自动配置落位），
在改代码前调用 `grill-me`（见 `java:reference` → `grill-me.md`）。

> 结论：在本仓库内，本技能主要用于「理解 Spring 语义、评估迁移、处理 Spring 相关
> 依赖或调试 Spring 集成」，而不是用来写端点。

---

## 依赖与版本（硬约束）

| 必须 | 禁止 |
|------|------|
| Spring / Boot 及 starter **版本只来自 `innospots-nexus-bom`** | 在 `innospots-nexus-spring` 子模块 POM 写 `<version>` |
| 升 Boot 只改 BOM 的 `spring-boot.version` + `dependency:tree` 验证 | 在 spring 聚合 POM 维护第二套 `spring-boot.version` |
| 持久化用 **MyBatis-Plus**（`mybatis-plus-spring-boot4-starter`） | **Spring Data**（`spring-boot-starter-data-*`、`spring-data-*`） |
| 认证/授权用 **kernel / console** 领域与 Jakarta 边界 | **Spring Security**（`spring-boot-starter-security`、`spring-security-*`） |
| 新增 starter 先 `java:design` + BOM 登记 | 绕过 BOM 直接引第三方坐标 |

细则与自检 → [spring-dependencies.md](references/spring-dependencies.md)、
[`dependency-conventions.md`](../java-project/references/dependency-conventions.md)。

---

## 版本线（截至 2026-09，以 spring.io 与 endoflife.date 为准）

本仓库**实际使用版本**以 `innospots-nexus-bom` 的 `spring-boot.version` 为准（当前与下表主线一致时仅为巧合），不得在各模块 POM 单独指定。

| Boot | Framework | Java 支持 | 状态 |
|------|-----------|----------|------|
| 4.1.x | 7.0.x | 17 – 26 | 当前主线（4.1.1，2026-08） |
| 4.0.x | 7.0 | 17 – 25 | OSS 支持至 2026-12 |
| 3.5.x | 6.2 | 17 – 25 | **OSS 支持已于 2026-06 结束** |
| 3.0 – 3.2 | 6.0 – 6.1 | 17 – 21 | 均已 EOL |
| 2.7.x | 5.3 | 8 – 21 | 商业支持延长至 2029-06 |

选版本时先查官方支持状态，**不要凭记忆选版本**。

### 三代之间的关键断点

| 迁移 | 关键变化 |
|------|---------|
| 2.x → 3.x | Java 17 基线；`javax.*` → `jakarta.*` 全量迁移（Servlet、Persistence、Validation、Annotation）；Spring Framework 6；Hibernate 6；移除部分废弃 API；`spring.factories` 自动配置注册逐步迁向 `AutoConfiguration.imports` |
| 3.x → 4.x | Spring Framework 7；**代码库模块化**（自动配置类的 public 成员被移除，自定义 starter 需改用新模块结构与文档化扩展点）；JSpecify 全组合空安全；Java 25 一等支持（基线仍为 17）；Jackson 3（Jackson 2 以废弃形式提供）；Hibernate 7；Tomcat 11；若干配置属性重命名；**必须先升到 3.5 再升 4.0** |

Spring 大版本升级属于 `java:dependency-upgrade` 的主流程，本技能只提供版本与差异知识。

---

## 核心能力速查

### 依赖注入与 Bean

| 关注点 | 要点 |
|--------|------|
| 注入方式 | 构造器注入优先（与本项目规范一致）；字段注入难测试、隐藏依赖 |
| Bean 作用域 | 默认 singleton；`prototype`、`request`、`session` 需显式声明并理解其生命周期风险 |
| 生命周期 | `@PostConstruct` / `@PreDestroy`；或 `InitializingBean` / `DisposableBean`；注意与本项目「创建者负责清理」的规则对齐 |
| 条件装配 | `@ConditionalOnProperty` / `@ConditionalOnMissingBean` / `@ConditionalOnClass` |
| 循环依赖 | 构造器注入下无法被容器自动解决，需重构依赖方向 |
| 可选依赖 | `ObjectProvider<T>` 优于把必需依赖设为可空 |

### 配置

| 能力 | 说明 |
|------|------|
| `@ConfigurationProperties` | 类型安全绑定，配合 `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan` |
| `@Value` | 适合单点取值；大量取值应用 `@ConfigurationProperties` |
| Profile | `@Profile` 与 `spring.profiles.active`；避免用 profile 表达业务分支 |
| 配置优先级 | 命令行 > 环境变量 > application-{profile}.yml > application.yml > 默认值 |
| 宽松绑定 | `myProp` / `my-prop` / `MY_PROP` 等价 |
| 敏感配置 | 走外部密钥管理或环境变量，**不得**入库或进日志 |

### 事务

| 关注点 | 要点 |
|--------|------|
| 注解 | Spring 工程用 `@Transactional`；**本仓库用 `jakarta.transaction.Transactional`** |
| 自调用失效 | 同类内部方法调用不经过代理，事务不生效；需拆分或走 AOP 代理 |
| 传播行为 | 默认 `REQUIRED`；`REQUIRES_NEW` 会挂起当前事务，注意连接消耗 |
| 回滚规则 | 默认只对 `RuntimeException` / `Error` 回滚；受检异常需显式 `rollbackFor` |
| 只读 | 查询方法用 `readOnly = true`，但注意它只影响部分 ORM 优化行为 |
| 事务范围 | 事务内避免远程调用、文件 I/O、大批量循环 |
| 类级注解 | 易把只读方法也卷入事务，优先方法级 |

### AOP

- 仅对 Spring 容器管理的 Bean 生效；同类自调用不触发
- 优先使用 Spring 内置能力（事务、缓存、重试、校验）而非自定义切面
- 自定义切面要明确切点表达式、顺序（`@Order`）与异常处理策略
- 不得在切面中吞掉异常或改变业务语义

### Spring MVC / WebFlux

- **本仓库端点不用 Spring MVC**，用 Jakarta REST
- MVC 适合阻塞式、Servlet 栈；WebFlux 适合非阻塞、响应式栈，但需全链路响应式才有效果
- 参数校验用 Jakarta Validation（`@Valid` / `@Validated`）
- 统一异常处理：`@RestControllerAdvice` + `@ExceptionHandler`（**映射到 `R` 的集中处理是本仓库端点基础设施的职责**）

### Spring Data / Spring Security（本仓库禁止）

- **不得**引入 `spring-boot-starter-data-*`、`spring-data-*`、`spring-boot-starter-security`、`spring-security-*`
- 从遗留 Spring 工程迁移时：Repository → `*Dao` + `BaseMapper`；Security 过滤器链 → console `permission` / `auth` 域
- 仅在外部非本仓库项目中讨论 Spring Data / Security 语义时，以下知识作对照：**Spring Data** 用 Repository 派生查询与 `@Query`；**Spring Security** 用 `SecurityFilterChain`——本仓库均不采用

### 测试

| 场景 | 方案 |
|------|------|
| 单元/切片测试 | `@JsonTest` 等；**不用** `@DataJpaTest`（禁止 Spring Data） |
| 完整上下文 | `@SpringBootTest`，配合 `@MockBean` / `@TestConfiguration` |
| HTTP 层 | `MockMvc`（MVC）、`WebTestClient`（WebFlux 与 MVC） |
| 真实依赖 | Testcontainers |
| 配置隔离 | `@TestPropertySource`、`@ActiveProfiles`、`@DynamicPropertySource` |
| 上下文复用 | 相同配置复用上下文；避免过度使用 `@DirtiesContext` |

> 本仓库的测试默认用 JUnit 5 + AssertJ + Mockito，见 `java:develop` → `test-conventions.md`。

### Actuator 与观测

- 健康检查、信息、指标端点需按暴露策略配置，生产环境只暴露必要端点
- 自定义 `HealthIndicator` 不应触发昂贵的下游调用或泄露内部细节
- 指标用 Micrometer；日志、指标、链路追踪一起考虑
- 观测数据不得包含凭据、令牌、用户隐私

---

## 本仓库内的 Spring 相关检查

| 检查 | 命令/方式 |
|------|----------|
| 端点是否混入 Spring MVC 注解 | `grep -rn "org.springframework.web.bind.annotation" --include=*.java */src/main/java` |
| 是否误用 Spring 事务注解 | `grep -rn "org.springframework.transaction.annotation" --include=*.java */src/main/java` |
| `base` 是否引入 Spring | 检查 `innospots-nexus-base/pom.xml` 依赖清单 |
| `core` 是否绑定自动配置 | 检查是否存在自动配置类或 `AutoConfiguration.imports` |
| Spring 相关依赖版本来源 | `mvn -q help:effective-pom`、`mvn dependency:tree`（应来自 BOM） |
| POM 内联 Spring `<version>` | `grep version pom.xml` in `innospots-nexus-spring/` |
| 禁止的 Spring Data / Security | `grep spring-boot-starter-data\|spring-security` in spring 模块 POM |

这些检查也包含在 `java:check` 的规范巡检中。

---

## 与升级技能的分工

| 工作 | 归属 |
|------|------|
| Spring Boot 2.x → 3.x、3.x → 4.x 及连带 Jakarta / Jackson / Hibernate | `java:dependency-upgrade` |
| 仅升 innospots-nexus 工程 `revision` 版本号 | `java:project-upgrade` |
| 版本差异知识与 Spring 语义解释 | 本技能 |

---

## 详细参考

- [spring-boundary.md](references/spring-boundary.md) — 本仓库适用边界与 Jakarta/Quarkus 对照
- [spring-dependencies.md](references/spring-dependencies.md) — BOM 版本、禁止 Spring Data/Security、允许 starter 白名单
- [spring-migration-notes.md](references/spring-migration-notes.md) — 版本线、断点与迁移要点
