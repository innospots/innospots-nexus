---
name: java:design
display_name: Java 架构与设计
description: |
  Java 架构、模块、接口、类与技术方案设计。当用户要做领域建模、划分模块与包
  结构、设计接口/类/方法签名、定义 REST 契约、设计领域事件、规划状态机与生命
  周期、做技术选型，或需要产出架构/技术方案评审意见时使用。产出是设计决策与
  契约，不是实现代码。
  触发词：架构设计、方案设计、领域建模、模块划分、接口设计、类设计、API 设计、
  技术选型、事件设计、状态机、契约设计、设计评审。
category: java
version: 1.3.0
---

# 架构、模块、接口与类设计

## 定位

负责**设计决策**：这块能力归谁、边界在哪、契约长什么样、状态怎么流转。
产出是设计结论与契约骨架，**不是**可运行实现。实现交给 `java:develop`。

规范与契约条文**不在本技能正文复述**，统一通过 `java:reference` 引用。

## grill-me（设计前/设计后）

非平凡设计（新域、跨模块、公共兼容面、架构评审触发项，见
`architecture-decision.md`）建议在以下节点调用 **`grill-me`**：

1. **设计四步法开始前** — 压力测试范围、归属假设与备选方案
2. **四步法完成、交 `java:develop` 前** — 评审设计树是否无遗漏假设

琐碎的局部契约（单端点字段增补等）可跳过。用法见
`java:reference` → [grill-me.md](../java-reference/references/grill-me.md)。

## 设计四步法

```text
1. 定归属  →  java:reference → module-ownership.md
2. 建词汇  →  java:reference → standards-index.md（naming.md）
3. 划边界  →  java:reference → package-structure.md、domain-modeling.md、scope-hierarchy.md
4. 定契约  →  java:reference → api-contract.md、exception-contract.md、test-scope.md、quick-constraints.md
```

每一步都有出口门禁，未通过不得进入下一步，也不得进入 `java:develop`。

## 何时需要完整设计评审

新建模块、改变依赖方向、引入中间件/重大依赖、改动公共兼容面、新增横切机制、
存储大改等情形必须先出方案。见 [architecture-decision.md](references/architecture-decision.md)。

## 设计评审门禁

- [ ] 归属模块与业务域正确（含 plugin / console 边界），未与相邻域混同
- [ ] 包结构领域优先（`role/endpoint`，禁止 `endpoint/role`）；大领域已划功能子模块；单包 ≤15 类（见 package-structure.md）
- [ ] 术语在端点/实体/DAO/数据库/测试之间一致
- [ ] 运行时作用域（Session）与持久化范围（Entity 基类）已分别判定
- [ ] 技术主键与稳定业务键已区分，稳定键的不可变性已定义
- [ ] 分层依赖方向正确，无 operator→service、无 endpoint→dao
- [ ] request/vo 是 record，集合已防御拷贝
- [ ] 接口引入有真实边界理由，非为 mock
- [ ] 端点边界内聚，约 7 个方法已触发复审
- [ ] 每个应用可见失败都有归属明确的状态码，且契约仅使用 `NexusException`（禁止 JDK/`RuntimeException` 等作为业务失败）
- [ ] 事务边界、幂等、重复调用、生命周期已定义
- [ ] 并发访问策略已声明
- [ ] 兼容面影响已识别并给出迁移或兼容方案
- [ ] 未创建空分层、占位类型、投机性 model/event 包
- [ ] 测试范围已定义（契约测试 + 行为单测；见 test-scope.md），实现由 `java:develop` 测试先行交付

## 详细参考

### 本技能（设计专属）

- [architecture-decision.md](references/architecture-decision.md) — 架构与技术选型、ADR 模板
- [exception-contract.md](references/exception-contract.md) — 失败归属、`StatusCode` 与 `NexusException` 契约
- [test-scope.md](references/test-scope.md) — 测试范围与契约测试清单（设计侧）

### 规范复用（`java:reference`，不在此重复）

- [module-ownership.md](../java-reference/references/module-ownership.md) — 模块归属决策
- [package-structure.md](../java-reference/references/package-structure.md) — 领域优先包结构
- [scope-hierarchy.md](../java-reference/references/scope-hierarchy.md) — Session 与持久化作用域
- [domain-modeling.md](../java-reference/references/domain-modeling.md) — 实体/请求/VO/事件建模
- [api-contract.md](../java-reference/references/api-contract.md) — 方法签名、分层、事务、兼容性
- [exception-status-code.md](../java-reference/standards/exception-status-code.md) — 异常与状态码权威条文
- [quick-constraints.md](../java-reference/references/quick-constraints.md) — 硬性红线速查
- [standards-index.md](../java-reference/references/standards-index.md) — 规范章节地图
