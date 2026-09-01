# Spring 版本线与迁移要点

版本状态随时变化，**动手前先查**：

- <https://spring.io/projects/spring-boot#support>
- <https://endoflife.date/spring-boot>
- <https://github.com/spring-projects/spring-boot/wiki> 的迁移指南

---

## 版本快照（2026-09 编写）

| Boot | Framework | Java | 状态 |
|------|-----------|------|------|
| 4.1.x | 7.0.x | 17 – 26 | 主线，4.1.1（2026-08-20） |
| 4.0.x | 7.0 | 17 – 25 | OSS 支持至 2026-12（4.0.8，2026-08-20） |
| 3.5.x | 6.2 | 17 – 25 | OSS 支持已于 2026-06-30 结束；商业支持至 2032-06 |
| 3.4.x | 6.2 | 17 – 24 | OSS 支持已结束 |
| 3.0 – 3.2 | 6.0 – 6.1 | 17 – 21 | 已 EOL |
| 2.7.x | 5.3 | 8 – 21 | 商业支持延至 2029-06 |

---

## 2.x → 3.x 关键断点

大版本工程升级，主流程归 `java:project-upgrade`。

| 断点 | 影响 |
|------|------|
| **Java 17 基线** | 2.x 支持 Java 8，3.x 起要求 17。需先升 JDK |
| **`javax.*` → `jakarta.*`** | Servlet、Persistence、Validation、Annotation、Transaction 全量换包。影响所有 import、注解、第三方库 |
| Spring Framework 6 | 部分废弃 API 移除 |
| Hibernate 6 | 查询语法与方言变化；`Criteria` API 变化 |
| 自动配置注册 | `spring.factories` 中的 `EnableAutoConfiguration` 逐步迁向 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| 第三方生态 | 需要各库提供 Jakarta 兼容版本，不兼容的需替换或升级 |
| 配置属性 | 部分属性重命名或移除，需查 `spring-boot-properties-migrator` |

### 迁移顺序建议

```text
1. 升到 2.7 最新补丁版（2.7 是 2.x 的最后一个功能线）
2. 升 JDK 到 17 并解决编译问题
3. 用 spring-boot-properties-migrator 找出需改的配置属性
4. 升到 Spring Boot 3.0，处理 javax → jakarta 全量替换
5. 逐个替换不兼容的第三方库
6. 升到 3.5 最新补丁版
7. 全量测试 + 回归
```

---

## 3.x → 4.x 关键断点

| 断点 | 影响 |
|------|------|
| **必须先到 3.5** | 不允许跨过 3.5 直接升 4.0 |
| Spring Framework 7 | 底层 API 变化 |
| **代码库模块化** | 自动配置类的 public 成员被移除（改为包级私有）。自定义 starter 与内部自动配置若依赖了这些成员会编译失败，需改用文档化的扩展点与新模块结构。例：MongoDB 健康指示从 `spring-boot-data-mongodb` 移到 `spring-boot-mongodb` |
| **JSpecify 空安全** | 全组合引入空安全注解，严格的空值检查可能暴露大量告警 |
| Java 25 一等支持 | 基线仍为 Java 17 |
| **Jackson 3** | Jackson 2 以废弃形式提供。序列化行为与包名有变化 |
| Hibernate 7 | ORM 行为变化 |
| Tomcat 11 | Servlet 容器升级 |
| 属性重命名 | 如 `spring.dao.exceptiontranslation.enabled`、`management.tracing.enabled` 等 |
| SSL `WILL_EXPIRE_SOON` 状态移除 | 依赖该状态的监控需调整 |
| `EnvironmentPostProcessor` 包变更 | `org.springframework.boot.env.EnvironmentPostProcessor` 被新的 `org.springframework.boot.EnvironmentPostProcessor` 取代，旧包废弃 |

### 4.x 新增能力

| 能力 | 说明 |
|------|------|
| HTTP Service Client 自动配置 | 用 `@HttpExchange` 声明接口即可生成实现 |
| API 版本控制 | MVC 与 WebFlux 内置 API Versioning |
| OpenTelemetry starter | `spring-boot-starter-opentelemetry` |
| 模块化 jar | 依赖树更精简、启动更快、Native Image 可达性分析面更小 |
| JMS Client 自动配置 | `JmsClient` |
| Kotlin Serialization | 模块与 starter |
| `RestTestClient` | 测试支持 |
| Gradle 9 支持 | |

---

## 升级节奏建议

| 当前版本 | 建议路径 |
|---------|---------|
| 2.7.x | 2.7 最新补丁 → JDK 17 → 3.0 → 3.5 最新补丁 |
| 3.0 – 3.2 | 先到 3.5 最新补丁（这些版本已 EOL，安全风险优先） |
| 3.4.x / 3.5.x | 3.5 最新补丁 → 4.0 或 4.1 |
| 4.0.x | 评估升 4.1（4.0 OSS 支持 2026-12 结束） |

任何大版本升级都需要：

1. 先跑 `java:check` 建立**升级前基线**（编译 + 测试结果要留存）
2. 按 `java:project-upgrade` 的流程做兼容性分析与分阶段改造
3. 具体组件（Jackson、Hibernate、Jakarta 各模块）交给 `java:tool-upgrade`
4. 升级后再跑 `java:check` 与升级前基线对比

---

## 版本选择原则

| 原则 | 说明 |
|------|------|
| 只用官方仍提供 OSS 支持的版本 | 已 EOL 的版本不再收安全补丁 |
| 不追最新版本，追支持周期 | 新版本发布后观察一个补丁周期 |
| 大版本升级排在计划里，不做临时动作 | 大版本升级必然有 breaking changes |
| 全组合版本一致 | 用 Spring Boot 的 BOM 统一管 Spring 全家桶版本，不要手工拼版本 |
| 记录当前版本与升级窗口 | 便于下次评估 |

---

## Spring Boot 特有检查项

在 Spring 工程中 `java:check` 需要额外关注：

| 检查项 | 说明 |
|--------|------|
| 依赖版本是否来自 Spring Boot BOM | 手工拼版本易出现组合不兼容 |
| 自动配置是否被意外启用 | 用 `--debug` 或 `ConditionEvaluationReport` 检查 |
| 是否有自定义 starter 依赖了内部 API | 4.x 模块化后尤其容易失效 |
| 配置属性是否有已重命名/废弃项 | 用 `spring-boot-properties-migrator` 扫描 |
| `javax.*` 残留 | 3.x 起必须全量 `jakarta.*` |
| `@Transactional` 自调用失效 | 同类内部调用不经过代理 |
| 事务回滚规则 | 受检异常默认不回滚 |
| Actuator 端点暴露范围 | 生产环境不得全量暴露 |
| 观测数据是否含敏感信息 | 凭据、令牌、用户隐私 |
| 测试上下文是否被过度重建 | `@DirtiesContext` 滥用拖慢测试 |
