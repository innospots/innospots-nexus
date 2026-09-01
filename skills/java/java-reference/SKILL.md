---
name: java:reference
display_name: Java 规范总索引
description: |
  Java 通用规范与标准总入口。当用户询问 Java 编码规范、命名规则、注释要求、
  API 设计原则、异常与状态码约定、代码风格，或不确定某个 Java 任务应该走哪个
  技能时使用。提供 standards/ 下 7 份权威规范的索引、硬性红线速查、规范冲突
  裁决顺序，以及 9 个 java:* 技能的路由表。
  触发词：Java 规范、编码规范、命名规范、代码风格、注释规范、API 设计、
  异常规范、状态码、java 标准、standards。
category: java
version: 1.0.0
---

# Java 通用规范与标准

## 定位

本技能是 `java:*` 技能体系的**规范总索引与路由中枢**，不直接产出代码。

- 需要查规范原文 → 本技能给出权威文件与章节位置
- 需要动手做事 → 按下方路由表转交对应技能
- 规范之间打架 → 按「冲突裁决顺序」判定

## 规范源文件

所有规范的唯一权威来源是仓库 `standards/` 目录。任何时候都**以源文件为准**，
本技能只做索引与速查。

| 规范文件 | 管辖范围 | 主要使用者 |
|---------|---------|-----------|
| [`code-style.md`](../../../standards/code-style.md) | 花括号、缩进、行宽、导入顺序、成员顺序、Lombok、REST 端点、MapStruct、MyBatis-Plus DAO、依赖注入、集合、日志、字面量 | `java:develop`、`java:check` |
| [`naming.md`](../../../standards/naming.md) | 命名决策顺序、词汇表、类型后缀、方法动词、字段/参数/局部名、缩写、包命名、持久化命名、测试命名 | `java:design`、`java:develop`、`java:test` |
| [`api-design.md`](../../../standards/api-design.md) | 方法签名、契约与实现边界、不可变性、空值处理、校验、异常、持久化实体、领域模型、REST 契约、DAO 契约、Service/Operator 边界、事务、事件、兼容性 | `java:design`、`java:develop` |
| [`code-comments.md`](../../../standards/code-comments.md) | 包文档、类型注释、方法注释、行内注释、TODO 规则、禁用注释 | `java:develop`、`java:check` |
| [`exception-status-code.md`](../../../standards/exception-status-code.md) | 异常分类、`NexusException` 构造、抛出/捕获/翻译、状态码九字符格式、类别语义、HTTP 映射、扩展流程、契约测试 | `java:design`、`java:develop`、`java:test` |
| [`domain-module-initialization.md`](../../../standards/domain-module-initialization.md) | 新业务领域的六阶段初始化工作流、测试先行顺序、编译门禁、完整验证门禁 | `java:develop`、`java:test` |
| [`module-skills.md`](../../../standards/module-skills.md) | 模块技能文档的生成策略与 `SKILL.md` / `references/` 格式 | 仅开发者显式请求扫描时使用 |

仓库另有 `AGENTS.md` 定义模块职责与依赖方向，是工程结构的权威来源。

## 技能路由表

| 场景 | 使用技能 | 一句话判据 |
|------|---------|-----------|
| 查规范、问约定、不确定走哪个技能 | `java:reference` | 只读、不产出代码 |
| 建工程、改模块结构、配 Maven/POM、调构建 | `java:project` | 动的是**工程骨架** |
| 做架构/模块/接口/类/技术方案设计 | `java:design` | 动的是**设计决策** |
| 写功能、改功能、修 Bug、重构 | `java:develop` | 动的是**实现代码** |
| 写单测、集成测试、回归测试 | `java:test` | 动的是**测试代码** |
| 编译、跑测试、规范/质量/依赖/安全/性能检查 | `java:check` | 动的是**验证动作** |
| Spring / Spring Boot 专项能力 | `java:spring` | 涉及 **Spring 生态** |
| JDK / Spring 大版本 / Jakarta / 构建体系升级 | `java:project-upgrade` | 改变**运行或构建基线** |
| 基础组件、公共依赖、内部 SDK 升级 | `java:tool-upgrade` | 改变**使用的基础组件** |

两个升级技能的唯一区分原则：

```text
改变项目运行/构建基线  → java:project-upgrade
改变项目使用的基础组件 → java:tool-upgrade
```

| 升级示例 | 归属 |
|---------|------|
| JDK 17 → 25 | `java:project-upgrade` |
| Spring Boot 2 → 3、Java EE → Jakarta EE | `java:project-upgrade` |
| Maven 3 → 4、Gradle 大版本 | `java:project-upgrade` |
| JUnit 4 → JUnit 5、模块结构调整、语言特性迁移 | `java:project-upgrade` |
| Jackson 2 → 3、MapStruct、Lombok、SLF4J/Logback | `java:tool-upgrade` |
| commons-lang3 / commons-io / Guava / HTTP Client | `java:tool-upgrade` |
| 内部 common SDK、内部 starter、内部基础框架 | `java:tool-upgrade` |

若一次升级同时涉及两者（如 Spring Boot 2→3 连带 Jackson/Hibernate/Jakarta），
主流程由 `java:project-upgrade` 负责，过程中调用 `java:tool-upgrade` 处理具体
基础组件迁移。

## 硬性红线

以下规则无例外，违反即视为不合格。完整清单见
[quick-constraints.md](references/quick-constraints.md)。

| # | 红线 |
|---|------|
| 1 | 每次修改 Java 源文件后立即运行 `mvn clean compile`，不得延后、不得降低 `maven.compiler.release` 迁就旧 JDK |
| 2 | 业务异常一律 `NexusException` + 类型化 `StatusCode`，不得为每个错误建异常子类 |
| 3 | 状态码必须是 `MODULE(3) + CATEGORY(2) + LOCAL(4)` 共九字符，`bisCode()` 与 `fullCode()` 一致 |
| 4 | DAO 方法只能访问单表，禁止 SQL join、禁止 Mapper XML |
| 5 | 端点只用 `jakarta.ws.rs` 注解，禁止 Spring MVC 注解，必须返回 `R<T>` |
| 6 | `domain.request` / `domain.vo` 类型必须是 record |
| 7 | 依赖方向 `endpoint → service → operator → dao`，operator 不得依赖 service 或另一个 operator |
| 8 | 事务只用 `jakarta.transaction.Transactional` |
| 9 | 不得复制遗留工程源码、遗留 POM 结构或机械复刻包名 |
| 10 | 未获开发者显式请求时，不得创建/更新/同步模块 `SKILL.md` 与 `references/` 文档 |

## 冲突裁决顺序

当两份规范或规范与既有代码冲突时，按以下顺序裁决，**先命中者优先**：

1. **开发者当前明确意图** — 显式指令覆盖一切默认约定
2. **`AGENTS.md`** — 仓库最高层约束（模块职责、依赖方向、遗留代码禁令）
3. **`exception-status-code.md`** — 异常与状态码语义的专属权威
4. **`api-design.md`** — 契约、边界、不可变性、事务、事件、兼容性
5. **`naming.md`** / **`code-style.md`** / **`code-comments.md`** — 命名、格式、注释
6. **`domain-module-initialization.md`** — 领域初始化流程与门禁
7. **现有代码的既有命名** — 只是词汇证据，**不是**可沿用的先例

裁决后必须在回复中说明命中了哪一条。若冲突无法自行消解，停下来问开发者。

## 使用方式

1. 判断任务类型，查路由表
2. 若为规范查询，直接打开 `standards/` 对应文件确认原文，不要凭记忆回答
3. 若为实施任务，转交对应技能，并把本技能的红线清单作为前置约束带过去
4. 所有产出最终都要过 `java:check`

## 详细参考

- [quick-constraints.md](references/quick-constraints.md) — 按场景组织的硬性约束速查表
- [standards-index.md](references/standards-index.md) — 7 份规范的章节级索引
