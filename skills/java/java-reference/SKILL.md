---
name: java:reference
display_name: Java 规范总索引
description: |
  Java 通用规范与标准总入口。当用户询问 Java 编码规范、命名规则、注释要求、
  API 设计原则、异常与状态码约定、代码风格，或不确定某个 Java 任务应该走哪个
  技能时使用。提供 skills/java/java-reference/standards/ 下权威规范的索引、专题参考、硬性红线速查、规范
  冲突裁决顺序，以及 8 个 java:* 技能的路由表；并索引跨技能的 `grill-me` 调用时机。
  其他 java:* 技能通过本技能复用规范，不得在各自正文中重复规范条文。
  触发词：Java 规范、编码规范、命名规范、代码风格、注释规范、API 设计、
  异常规范、状态码、java 标准、standards。
category: java
version: 1.6.0
---

# Java 通用规范与标准

## 定位

本技能是 `java:*` 技能体系的**规范总索引、专题参考库与路由中枢**，不直接产出代码。

- 需要查规范原文 → 本技能给出权威文件与章节位置
- 需要跨技能复用的专题规则 → 本技能 `references/` 下的专题文件
- 需要动手做事 → 按下方路由表转交对应技能
- 规范之间打架 → 按「冲突裁决顺序」判定

**其他 `java:*` 技能不得在 SKILL 正文复述规范条文**，应链接到本技能下的
`references/` 或 `standards/` 原文。

## 规范源文件（权威原文）

`skills/java/java-reference/standards/` 目录是规范条文的**唯一权威来源**（随 Java 技能包安装）。
本技能提供索引与专题摘要，疑义一律回源原文。

| 规范文件 | 管辖范围 | 主要使用者 |
|---------|---------|-----------|
| [`code-style.md`](standards/code-style.md) | 格式、Lombok、REST 端点、MapStruct、DAO、日志 | `java:develop`、`java:check` |
| [`naming.md`](standards/naming.md) | 命名决策、词汇、类型后缀、包与持久化命名 | `java:design`、`java:develop` |
| [`api-design.md`](standards/api-design.md) | 签名、契约边界、实体、REST/DAO、事务、事件、兼容性 | `java:design`、`java:develop` |
| [`code-comments.md`](standards/code-comments.md) | 包/类型/方法/行内注释与 TODO | `java:develop`、`java:check` |
| [`exception-status-code.md`](standards/exception-status-code.md) | 异常、`NexusException`、状态码九字符格式与扩展 | `java:design`、`java:develop` |
| [`domain-module-initialization.md`](standards/domain-module-initialization.md) | 六阶段领域初始化权威流程 | `java:develop` |
| [`module-skills.md`](standards/module-skills.md) | 模块 API 参考生成策略（`README.md` 索引，非技能） | 仅开发者显式请求扫描时 |

仓库另有 [`AGENTS.md`](../../../AGENTS.md) 定义模块职责与依赖方向。

## 专题参考（跨技能复用）

按主题组织的执行参考，供 `java:design`、`java:develop`、`java:check` 链接复用：

| 专题 | 文件 | 用途 |
|------|------|------|
| 硬性红线速查 | [quick-constraints.md](references/quick-constraints.md) | 按场景的最常违反约束 |
| 规范章节地图 | [standards-index.md](references/standards-index.md) | 定位规则在哪份 standards 文件的哪一节 |
| 模块归属 | [module-ownership.md](references/module-ownership.md) | Maven 模块与业务域判定 |
| 包结构（领域优先） | [package-structure.md](references/package-structure.md) | 领域 → 功能子模块 → 职责；禁止 service 堆积；单包 ≤15 类 |
| 作用域层级 | [scope-hierarchy.md](references/scope-hierarchy.md) | Session/Snapshot 与 Entity 基类 |
| 领域建模 | [domain-modeling.md](references/domain-modeling.md) | 实体/请求/VO/事件建模决策 |
| API 契约 | [api-contract.md](references/api-contract.md) | 签名、分层、事务、兼容性；含 Console interface 例外 |
| 持久化与配置 | [persistence-config.md](references/persistence-config.md) | yaml、禁 XML/properties、Dao 组织细则 |
| 六阶段 checklist | [domain-initialization-checklist.md](references/domain-initialization-checklist.md) | develop 执行清单 |
| 测试规范路由 | [testing-index.md](references/testing-index.md) | 设计/实现/检查测试文档索引 |
| 单元测试规约 | `java:develop` → [test-conventions.md](../java-develop/references/test-conventions.md) | 命名、断言、Mock、独立性 |
| 契约测试写法 | `java:develop` → [contract-tests.md](../java-develop/references/contract-tests.md) | 实体/DAO/端点/状态码契约测试 |
| 测试范围（设计） | `java:design` → [test-scope.md](../java-design/references/test-scope.md) | 设计阶段测什么/不测什么 |
| 方案压力测试 | [grill-me.md](references/grill-me.md) | 重大决策前的 `/grilling` 会话 |

## grill-me（方案压力测试）

规范条文用本技能；**方案、边界、归属有歧义或多种可行路径**时，在动手前先走
`grill-me`（见 [grill-me.md](references/grill-me.md)）。典型时机：新业务域设计前、
四步法结论交 develop 前、新建模块前、升级方案定稿前。

## 技能路由表

| 场景 | 使用技能 | 一句话判据 |
|------|---------|-----------|
| 查规范、问约定、不确定走哪个技能 | `java:reference` | 只读、不产出代码 |
| 建工程、改模块结构、配 Maven/POM、调构建 | `java:project` | 动的是**工程骨架** |
| 做架构/模块/接口/类/技术方案设计 | `java:design` | 动的是**设计决策** |
| 写功能、改功能、修 Bug、重构、单元/契约测试 | `java:develop` | 动的是**实现与配套测试代码** |
| 编译、跑测试、规范/质量/依赖/安全检查 | `java:check` | 动的是**验证动作** |
| Spring / Spring Boot 专项能力 | `java:spring` | 涉及 **Spring 生态** |
| 工程 `${revision}` 升版、发版、避免手改各模块 version | `java:project-upgrade` | 改变**自身产物版本号** |
| JDK、依赖、Spring/Jakarta、构建插件、第三方与内部 SDK 升级 | `java:dependency-upgrade` | 改变**外部技术栈** |

## 硬性红线

完整清单见 [quick-constraints.md](references/quick-constraints.md)。摘要：

| # | 红线 |
|---|------|
| 1 | 每批 Java 改动后立即 `mvn clean compile` |
| 2 | 业务异常一律 `NexusException` + 类型化 `StatusCode`；禁止 JDK/`RuntimeException`/`Exception` 作为失败契约 |
| 3 | 状态码九字符：`MODULE(3) + CATEGORY(2) + LOCAL(4)` |
| 4 | DAO 单表、无 join、无 Mapper XML；配置用 yaml，禁 properties/beans.xml |
| 5 | 端点只用 `jakarta.ws.rs`，返回 `R<T>` |
| 6 | `domain.request` / `domain.vo` 必须是 record |
| 7 | `endpoint → service → operator → dao`；operator 不得依赖 service 或其他 operator |
| 8 | 事务只用 `jakarta.transaction.Transactional` |
| 9 | 不得复制遗留工程源码或机械复刻包结构 |
| 10 | 未获显式请求时不得更新模块 API 索引（`README.md`）/ `references/` 文档 |
| 11 | 新功能必须有配套单元/契约测试；交付前 `mvn test` 通过（见 `java:develop`） |

## 冲突裁决顺序

1. 开发者当前明确意图
2. **`AGENTS.md`**
3. **`exception-status-code.md`**
4. **`api-design.md`**
5. **`naming.md`** / **`code-style.md`** / **`code-comments.md`**
6. **`domain-module-initialization.md`**
7. 现有代码的既有命名（仅作词汇证据，非先例）

## 模块 API 参考

位于 `references/modules/<artifact-id>/README.md`（**模块 API 索引，不是技能**）。
仅开发者显式请求扫描时生成（见 [`standards/module-skills.md`](standards/module-skills.md)）。

| 模块 | 入口 |
|------|------|
| `innospots-nexus-base` | [README.md](references/modules/innospots-nexus-base/README.md) |
| `innospots-nexus-core` | [README.md](references/modules/innospots-nexus-core/README.md) |
| `innospots-nexus-console` | [README.md](references/modules/innospots-nexus-console/README.md) |
| `innospots-nexus-plugin` | 暂无 API 索引；归属见 [module-ownership.md](references/module-ownership.md)；设计见 `innospots-nexus-plugin/docs/plugin/design/` |
| `innospots-nexus-kernel` | 暂无 API 索引；归属见 [module-ownership.md](references/module-ownership.md)；包结构见 [package-structure.md](references/package-structure.md) |
| `innospots-nexus-platform` | 暂无 API 索引；归属见 [module-ownership.md](references/module-ownership.md) |

显式扫描请求时可生成 plugin/kernel/platform 索引（见 [`standards/module-skills.md`](standards/module-skills.md)）。

## 详细参考

- [quick-constraints.md](references/quick-constraints.md)
- [standards-index.md](references/standards-index.md)
- [module-ownership.md](references/module-ownership.md)
- [package-structure.md](references/package-structure.md)
- [scope-hierarchy.md](references/scope-hierarchy.md)
- [domain-modeling.md](references/domain-modeling.md)
- [api-contract.md](references/api-contract.md)
- [persistence-config.md](references/persistence-config.md)
- [testing-index.md](references/testing-index.md)
- [domain-initialization-checklist.md](references/domain-initialization-checklist.md)
- [grill-me.md](references/grill-me.md)
