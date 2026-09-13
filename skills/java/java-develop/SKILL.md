---
name: java:develop
display_name: Java 功能开发
description: |
  Java 功能开发、功能修改、Bug 修复、重构与单元测试。当用户要写新的业务功能、
  初始化新的业务领域、修改现有实现、修复缺陷、做重构，或需要实体/DAO/端点/
  转换器/Operator/Service 的代码骨架与对应单元测试时使用。遵循测试先行与六阶段
  领域初始化；每批改动后 `mvn clean compile`，功能交付前必须有通过的单元测试。
  触发词：写功能、开发、实现、新增接口、改功能、修 Bug、缺陷修复、重构、
  领域初始化、建实体、建端点、建 DAO、代码模板、单元测试、契约测试、JUnit。
category: java
version: 1.3.0
---

# 功能开发、修改、Bug 修复、重构与单元测试

## 定位

负责**实现代码与配套单元测试**。设计结论来自 `java:design`，规范来自 `java:reference`，
全量验证交给 `java:check`。**测试不再是独立技能**——与实现同一交付单元完成。

本技能的硬约束：

> **每批 Java 源文件改动之后立即运行 `mvn clean compile`。**
> **功能实现完成后，必须有对应单元测试，并在交付 `java:check` 前 `mvn test` 通过。**
> 不得延后到任务末尾，不得在源码不可编译时继续添加功能，
> 不得下调 `maven.compiler.release` 以迁就本地旧 JDK（应报告环境不匹配）。

## 前置硬门禁（阶段零）

在写任何源文件之前，必须已完成 `java:design` 四步法（含测试范围：测什么、不测什么）。
若归属、词汇、边界、契约或测试范围未明确，**停下来走 `java:design`**。

## 阶段零引用

- 归属与模块边界 → [module-ownership.md](../java-reference/references/module-ownership.md)
- 包结构（领域优先） → [package-structure.md](../java-reference/references/package-structure.md)
- 作用域层级 → [scope-hierarchy.md](../java-reference/references/scope-hierarchy.md)
- 建模与契约 → [domain-modeling.md](../java-reference/references/domain-modeling.md)、[api-contract.md](../java-reference/references/api-contract.md)
- 异常实现 → [exception-handling.md](references/exception-handling.md)
- 测试范围（设计侧） → `java:design` → [test-scope.md](../java-design/references/test-scope.md)

## 任务类型与流程

| 任务 | 流程 | 出口 |
|------|------|------|
| 新业务领域初始化 | [domain-initialization-checklist.md](../java-reference/references/domain-initialization-checklist.md) + 测试先行 | `mvn test` + `java:check` |
| 在已有域加功能 | [change-workflow.md](references/change-workflow.md) | 聚焦单测 + `mvn clean compile` + `mvn test` |
| 修 Bug | change-workflow 缺陷分支 | **先写复现测试** → 修实现 → `mvn test` |
| 重构 | change-workflow 重构分支 | 行为锁定测试 + 全量 `mvn test` |

## 测试先行（强制）

新建领域或新增公共行为时，**先写测试、确认红灯，再写实现**：

```text
1.  写实体契约测试 → 红灯
2.  实现实体
3.  写 DAO 泛型绑定测试 → 红灯
4.  实现 DAO
5.  写 Request/VO/状态码/端点契约测试 + 行为单测 → 红灯
6.  实现领域 record、枚举、service/operator、端点
7.  重跑聚焦测试直到通过
8.  `mvn test`（模块或全量）→ `java:check`
```

权威流程见 [`domain-module-initialization.md`](../java-reference/standards/domain-module-initialization.md)。

### 何时必须有单元测试

| 变更类型 | 最低测试要求 |
|---------|-------------|
| 新业务领域 | 实体/DAO/端点契约测试 + 关键行为单测 |
| 新增 service/operator 方法 | 至少覆盖正常路径 + 主要拒绝路径 |
| 修 Bug | 复现测试（修复前红灯，修复后转绿） |
| 新增/变更状态码 | 状态码契约测试（形状、双语、HTTP 映射） |
| 重构 | 锁定外部行为的现有测试全部通过；缺则先补 |
| 纯文档/注释 | 可不新增测试 |

**不得**为让实现通过而削弱断言；**不得**无测试交付新功能。

## 测试技术栈

| 能力 | 库 |
|------|-----|
| 框架 | JUnit 5（parent 提供） |
| 断言 | AssertJ（唯一推荐） |
| Mock | Mockito（必要时） |
| 集成测试 | `*IT.java`（failsafe） |

## 详细参考

- [test-conventions.md](references/test-conventions.md) — 命名、结构、断言、Mock、独立性
- [contract-tests.md](references/contract-tests.md) — 实体/DAO/端点/状态码契约测试写法
- [code-templates.md](references/code-templates.md) — 生产代码骨架
- [exception-handling.md](references/exception-handling.md) — 异常实现
- [change-workflow.md](references/change-workflow.md) — 加功能/改功能/修 Bug/重构

## 六阶段领域初始化

权威定义：`skills/java/java-reference/standards/domain-module-initialization.md`。
执行清单：[domain-initialization-checklist.md](../java-reference/references/domain-initialization-checklist.md)。

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 未完成 design 就写源文件 | 阶段零是硬门禁 |
| 无对应单元测试交付新功能 | 契约测试 + 关键行为单测 |
| 生成不能编译的代码 | 每批改动后 `mvn clean compile` |
| 为让测试通过而削弱断言 | 回到 design 澄清契约 |
| 注释掉失败测试 | 等同于放弃验证 |
| 返回伪造成功数据 | 推迟实现用 `TODO` + `NexusException` |
| 抛出 JDK/通用异常 | 一律 `NexusException` + `StatusCode` |
| 复制遗留源码/POM | 遗留工程只作行为参考 |

规范红线 → [quick-constraints.md](../java-reference/references/quick-constraints.md)。

## 出口

`mvn test` 通过后交 `java:check` 执行全量验证（编译、规范、依赖、安全等）。
