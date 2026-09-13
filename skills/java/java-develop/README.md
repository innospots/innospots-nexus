# java:develop — Java 功能开发

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:develop` 负责**实现与配套测试**：`src/main/java` 与 `src/test/java` 下的生产代码与测试类。

测试与实现是**同一交付单元**（无独立 `java:test` 技能）。设计来自 `java:design`，规范来自 `java:reference`，全量验证交给 `java:check`。

实现交付物格式、文件树、**模块/包拆分建议**与清单见 [develop-deliverables.md](references/develop-deliverables.md)。

## 怎么用

1. **确认阶段零** — `java:design` 完成且设计产物可定位（见 develop-deliverables）；新模块须先 `java:project`（上游 `grill-me` 已确认）。
2. **按任务选流程**：
   - 新领域 → [domain-initialization.md](references/domain-initialization.md) + checklist
   - 加功能 / 修 Bug / 重构 → [change-workflow.md](references/change-workflow.md)
3. **测试先行** — 先写契约或复现测试（红灯）→ 实现 → `mvn test` 转绿。
4. **每批 Java 改动后** — `mvn -pl <module> -am clean compile`。
5. **交付前** — 过 [develop-deliverables.md](references/develop-deliverables.md) 交付清单 → `java:check`。

向代理说明：设计文档路径或 PR 设计结论、模块名、包路径、任务类型。

## 输入

| 类型 | 来源 / 示例 |
|------|------------|
| 设计产物（必经） | L0：PR「设计结论」块；L1+：`<module>/docs/*-design.md`、`docs/design/`（见 [design-deliverables.md](../java-design/references/design-deliverables.md)） |
| 测试范围 | `java:design` → [test-scope.md](../java-design/references/test-scope.md) |
| 任务类型 | 新领域 / 加功能 / 修 Bug / 重构 |
| 模块与包 | `innospots-nexus-kernel`、`com.innospots.nexus.kernel.role` |
| Bug 复现 | 步骤、期望/实际、失败语义归属 |

**无设计产物** → 不回退到口头约定，先 `java:design`。

## 输出

实现产出是**源码与测试**，不是设计文档。完整规范见 [develop-deliverables.md](references/develop-deliverables.md)。

### 产出位置

```text
innospots-nexus-<module>/src/main/java/.../<domain>/{endpoint,service,operator,dao,converter,domain/...}
innospots-nexus-<module>/src/test/java/...   # 镜像包路径
```

### 按任务的最小交付集

| 任务 | 产出 |
|------|------|
| 新领域 | 域包树 + 契约测试 + 行为单测 |
| 加功能 | 受影响分层增量 + 聚焦单测 |
| 修 Bug | 复现测试 + 最小修复 |
| 重构 | 结构变更 + 既有测试全绿 |

### 验证命令

```bash
mvn -pl <module> -am clean compile    # 每批改动后
mvn -pl <module> -am test             # 交付前（有 IT 用 verify）
```

**不产出**：设计文档（除非任务含文档同步）、POM 新模块、API 索引、削弱后的测试。

## 适用场景

- 新业务领域六阶段初始化
- 在已有域增加端点、Service 方法、持久化
- 缺陷修复（先复现测试）
- 行为不变的重构（测试锁定）
- 状态码、枚举、record、MapStruct 转换器实现

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 无设计产物就写源文件 | `java:design` |
| 新建 Maven 模块 | `java:project` |
| 方案压力测试 | `grill-me`（在 design/project 前） |
| 只问规范 | `java:reference` |
| 全量评审报告 | `java:check` |
| 升 BOM / revision | 对应升级技能 |

**硬约束摘要**：`mvn clean compile`；`NexusException` + `StatusCode`；`jakarta.ws.rs` + `R<T>`；
禁止 Spring MVC/Data/Security；禁止 mapper.xml / beans.xml / 新建 `*.properties`；
Dao 用 LambdaWrapper + `default`（见 persistence-mybatis.md）。

## 与上下游技能

```text
grill-me（新域/新模块设计前，见 java-reference）
    ↓
java:project（新建 Maven 模块时）
    ↓
java:design（设计文档 / test-scope）
    ↓
java:develop（本技能）
    ↓
mvn test / verify
    ↓
java:check
```

## 详细参考

- [develop-deliverables.md](references/develop-deliverables.md) — **交付物、顺序表、清单、命令**
- [domain-initialization.md](references/domain-initialization.md) — 新领域逐步实施
- [code-templates.md](references/code-templates.md) — 代码骨架
- [test-conventions.md](references/test-conventions.md) — 测试命名与结构
- [contract-tests.md](references/contract-tests.md) — 契约与转换器测试
- [domain-events.md](references/domain-events.md) — 领域事件
- [integration-tests.md](references/integration-tests.md) — `*IT`
- [persistence-mybatis.md](references/persistence-mybatis.md) — MyBatis-Plus、yaml 配置、禁 XML/properties
- [exception-handling.md](references/exception-handling.md) — 异常实现
- [change-workflow.md](references/change-workflow.md) — 加功能 / Bug / 重构
