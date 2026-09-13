# java:design — Java 架构与设计

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:design` 负责**设计决策**：能力归属、词汇、包边界、API 契约、异常与状态码、持久化、事件、测试范围。

产出是**设计文档或 PR 内结构化结论**（非可运行代码）。格式与目录见
[design-deliverables.md](references/design-deliverables.md)。

## 怎么用

1. **`grill-me`（新设计开始前必经）** — 未安装则
   `npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"`。
2. **按四步法** — 每步过 [design-four-steps.md](references/design-four-steps.md) 出口门禁。
3. **选场景与级别** — [design-scenarios.md](references/design-scenarios.md)（L0–L3）。
4. **重大变更** — [architecture-decision.md](references/architecture-decision.md)。
5. **交下游前** — SKILL「设计评审门禁」；需新 Maven 模块 → `java:project` → `java:develop`。

## 输入

| 类型 | 示例 |
|------|------|
| 需求描述 | 新领域「租户工作区」、端点草案 |
| grill-me 结论 | 归属假设、备选方案 |
| 约束 | 多租户、平台域、插件贡献、兼容面 |
| 现有模型 | 相邻域实体、状态码 module 前缀 |

## 输出

见 [design-deliverables.md](references/design-deliverables.md)。

| 级别 | 形态 |
|------|------|
| L0 | PR「设计结论」块 |
| L1 | `<module>/docs/*-design.md` |
| L2 | `docs/design/` 或 `docs/superpowers/specs/` |
| L3 | `docs/design/adr/NNNN-*.md` |

**不产出**：`.java`、测试、POM（POM 属 `java:project`）、模块 API 索引。

## 适用场景

- 新业务领域或跨模块立项
- REST / 事件 / 状态机 / 生命周期设计
- 技术选型与 ADR
- 定义测试范围

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 写实现代码 | `java:develop` |
| 新建 Maven 模块（执行） | `java:project` |
| 查规范原文 | `java:reference` |
| 全量检查 | `java:check` |

## 与上下游技能

```text
grill-me
    ↓
java:design（四步法 + 设计文档）
    ↓
java:project（仅当需新建 Maven 模块）
    ↓
java:develop → java:check
```

## 详细参考

- [design-deliverables.md](references/design-deliverables.md) — 产出物、模板、交接
- [design-four-steps.md](references/design-four-steps.md) — **分步门禁**
- [design-scenarios.md](references/design-scenarios.md) — 场景与级别
- [persistence-contract.md](references/persistence-contract.md) — 持久化 / yaml
- [event-contract.md](references/event-contract.md) — 领域事件
- [exception-contract.md](references/exception-contract.md) — 失败与状态码
- [test-scope.md](references/test-scope.md) — 测试范围
- [architecture-decision.md](references/architecture-decision.md) — ADR

规范原文：`java-reference/references/`、`standards/`（**勿停在本目录 api-contract 存根**）。
