# 设计四步法（分步门禁）

`java:design` 核心流程。每步**出口门禁全部勾选**后才能进入下一步；
四步完成后再过 SKILL「设计评审门禁」，再交 `java:develop`。

产出落盘见 [design-deliverables.md](design-deliverables.md)。
场景选型见 [design-scenarios.md](design-scenarios.md)。

---

## 总览

```text
grill-me（新设计必经，琐碎 L0 可跳过）
    ↓
① 定归属  →  module-ownership.md
    ↓
② 建词汇  →  naming.md（经 standards-index.md）
    ↓
③ 划边界  →  package-structure.md、domain-modeling.md、scope-hierarchy.md
    ↓
④ 定契约  →  api-contract、exception-contract、persistence-contract、test-scope
    ↓
设计评审门禁（SKILL.md）→ java:develop
```

若 ① 判定需**新建 Maven 模块** → 在 ④ 完成后先 `java:project`，再 `java:develop`（见 design-deliverables「交接 project」）。

---

## ① 定归属

**读：** [module-ownership.md](../../java-reference/references/module-ownership.md)、
[module-layout.md](../../java-project/references/module-layout.md)（Maven 模块类型）。

### 出口门禁

- [ ] 能力归属的 **Maven 模块**已确定（base / core / plugin / console / kernel / platform / adapter / application）
- [ ] 与 plugin、console catalog、kernel 业务边界无混同
- [ ] `kernel` ↔ `platform` **无** Maven 互依方案
- [ ] 已判定：**仅新领域包** vs **需新建 Maven 模块**（后者须 `java:project`）
- [ ] 相邻域交互方式已列出（直接调用 / 事件 / 禁止依赖）
- [ ] 有意延后的能力已记录

**禁止进入 ② 若：** 说不清归属模块，或计划用新 Maven 模块「装多个无关领域」。

---

## ② 建词汇

**读：** [standards-index.md](../../java-reference/references/standards-index.md) →
[naming.md](../../java-reference/standards/naming.md)。

### 出口门禁

- [ ] 主概念中英文与包段一致（端点 / 实体 / 表 / 测试用同一词）
- [ ] 技术 ID（UUID 等）与**稳定业务键**已区分；稳定键不可变规则已写
- [ ] `state` / `status` / `mode` / `type` 语义无歧义
- [ ] 状态码 **module** 三字前缀与归属域一致（先搜现有目录）
- [ ] 失败语义初分：平台 / 领域 / 技术（供 ④ exception-contract）
- [ ] 未引入相邻域同义词

---

## ③ 划边界

**读：** [package-structure.md](../../java-reference/references/package-structure.md)、
[domain-modeling.md](../../java-reference/references/domain-modeling.md)、
[scope-hierarchy.md](../../java-reference/references/scope-hierarchy.md)。

### 出口门禁

- [ ] 包路径 **领域优先**（`role/endpoint`，非 `endpoint/role`）
- [ ] 大领域已规划**功能子模块**（grant / authorization …），非扁平 service 桶
- [ ] 单包预计 **≤15** 类；过大已规划拆分
- [ ] Session 作用域（Snapshot）与 Entity 基类（Tenant/Workspace/Project）已分别选定
- [ ] 分层：`endpoint → service → operator → dao`；无 endpoint→dao、operator→service
- [ ] 未规划空 `service` / `event` / `model` 包
- [ ] 跨表读：分批单表 + 内存组装（无 join 设计）

---

## ④ 定契约

**读：**

- [api-contract.md](../../java-reference/references/api-contract.md)
- [exception-contract.md](exception-contract.md)
- [persistence-contract.md](persistence-contract.md)
- [event-contract.md](event-contract.md)（按需）
- [test-scope.md](test-scope.md)
- [quick-constraints.md](../../java-reference/references/quick-constraints.md)

### 出口门禁

- [ ] 端点表：方法、路径、`R<T>`、委托层；端点 >7 个已规划复审
- [ ] request / vo 为 **record** 骨架；校验归属明确
- [ ] 每应用可见失败有 **StatusCode** 行 + 抛出边界（仅 `NexusException`）
- [ ] 持久化：一表一 Dao、无 join/XML/properties；Dao 自定义方法清单（见 persistence-contract）
- [ ] 配置：yaml 键路径 + Java `@ConfigurationProperties` 归属（若有）
- [ ] 事务 / 幂等 / 并发策略已写
- [ ] 领域事件（若有）：见 event-contract 清单
- [ ] 测试范围表：契约类名 + 行为单测边界 + 不测范围
- [ ] 兼容面 / 迁移（若有）已写
- [ ] 已选定 L0 / L1 / L2 / L3 产出形态与文档路径

**禁止进入 develop 若：** 契约骨架不足以让 develop 测试先行（无测试类清单、无状态码表）。

---

## 完成后

1. 过 SKILL.md「设计评审门禁」全文。
2. （可选）交 develop 前再开一轮 `grill-me`。
3. 需新建 Maven 模块 → `java:project` → `java:develop` → `java:check`。
4. 否则 → `java:develop` → `java:check`。
