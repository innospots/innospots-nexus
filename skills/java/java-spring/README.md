# java:spring — Spring / Spring Boot 专项

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:spring` 提供 **Spring 生态在本仓库中的边界、依赖约束、版本知识与迁移要点**。

`innospots-nexus` **不是** Spring Boot 业务工程：管理端 REST 用 Jakarta REST，事务用 `jakarta.transaction.Transactional`；Spring 主要用于 `innospots-nexus-spring` 运行时组装与集成调试。

## 怎么用

1. **动手前** — 读 [spring-boundary.md](references/spring-boundary.md) 确认本仓库允许什么。
2. **加/改 Spring 依赖** — 遵循 [spring-dependencies.md](references/spring-dependencies.md)：版本只来自 BOM，禁止 Spring Data / Spring Security。
3. **评估迁移** — [spring-migration-notes.md](references/spring-migration-notes.md)；大版本升级主流程在 `java:dependency-upgrade`。
4. **集成边界不清** — 先 `grill-me`，再改 filter、自动配置等。

向代理说明：是 spring 子模块 POM、配置类，还是外部 Spring 项目对照本仓库规范。

## 输入

| 类型 | 示例 |
|------|------|
| Spring 任务 | 新增 starter、调自动配置、Session 与 Filter 绑定 |
| 迁移背景 | 遗留 Spring Boot 2/3 工程对照、javax→jakarta |
| 依赖疑问 | 「能不能加 spring-boot-starter-data-jpa」 |
| 版本目标 | 计划升到 Boot 4.x（须走 dependency-upgrade） |

## 输出

| 类型 | 内容 |
|------|------|
| 边界结论 | 允许/禁止的注解、模块、依赖 |
| POM 建议 | 符合 BOM 的 starter 声明（无内联 version） |
| 迁移对照 | Repository→Dao、Security→kernel/console 等 |
| 自检命令 | grep / dependency:tree 验证项 |
| 语义解释 | DI、AOP、配置、测试切片（外部项目或集成层） |

**不产出**：用 `@RestController` 写管理端端点；在 `base`/`core` 绑 Boot 自动配置。

## 适用场景

- `innospots-nexus-spring` 模块依赖与配置
- Spring Boot 版本评估与断点查阅
- 从 Spring Data / Security 迁移到本仓库栈
- 调试 Spring 容器中的 kernel/console 组装
- 与 `java:dependency-upgrade` 配合的 Spring 大版本升级

## 不适用 / 边界

| 禁止 / 不做 | 说明 |
|------------|------|
| Spring MVC 写端点 | 用 `jakarta.ws.rs` |
| `org.springframework.transaction.annotation.Transactional` | 用 Jakarta 事务注解 |
| Spring Data（`starter-data-*`、`spring-data-*`） | 持久化用 MyBatis-Plus |
| Spring Security | 鉴权用 kernel / console |
| 模块 POM 内联 Spring `<version>` | 只改 BOM `spring-boot.version` |
| 在 `innospots-nexus-base` 引 Spring | 模块职责禁止 |

允许的 starter 白名单见 [spring-dependencies.md](references/spring-dependencies.md)（以 `spring-app` 为基准）。

## 与上下游技能

```text
java:project（spring 子模块骨架）
    ↓
java:design（新增 starter / 集成方案）
    ↓
java:spring（边界与依赖）
    ↓
java:develop（配置与集成代码）
    ↓
java:dependency-upgrade（Boot 大版本）→ java:check
```

## 详细参考

- [spring-boundary.md](references/spring-boundary.md) — 本仓库边界
- [spring-dependencies.md](references/spring-dependencies.md) — BOM、禁止项、白名单
- [spring-migration-notes.md](references/spring-migration-notes.md) — 版本线与迁移
- [dependency-conventions.md](../java-project/references/dependency-conventions.md) — 工程级依赖约定
