# grill-me 最佳使用实践

本文说明在本仓库中如何**高效、可复用**地使用 `grill-me`（`/grilling` 会话）做方案压力测试。
调用时机与技能衔接见
[`skills/java/java-reference/references/grill-me.md`](../skills/java/java-reference/references/grill-me.md)；
本文侧重**怎么开好一场会话、怎么沉淀结论、怎么避免常见失误**。

---

## 1. 定位与产出

`grill-me` 是**方案与决策压力测试**，不是编码技能。

| 维度 | 说明 |
|------|------|
| 输入 | 待审查范围（模块路径、设计文档、升级方案、技能目录等） |
| 过程 | 多轮结构化问答，按**设计树前沿**逐轮消歧 |
| 产出 | 双方确认的**设计树**（关键决策表），不是 Java 代码 |
| 落盘 | PR 描述、`docs/design/`、ADR、`§设计树` 小节 |

**原则：** 事实由 Agent 查代码与文档；**决策**留给开发者。会话结束前不得开始实现。

---

## 2. 安装与触发

### 2.1 安装（未安装时）

`java:project` 与 `java:design` **开始前**必须先能调用 `grill-me`：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

安装后 Agent 须读取技能全文；若终端输出过长，先重定向到临时文件再读取。
技能若提供 **supporting-files** 目录，相对路径以该目录为根解析。

### 2.2 推荐触发方式

```text
/grill-me innospots-nexus-kernel/src/.../permission
/grill-me docs/design/multi-tenant-governance-design.md
/grill-me innospots-nexus-service 新建八个中立库的方案
/grill-me JDK 25 + Spring Boot 4 升级路径
```

在 Cursor 中也可在对话中附加 `grill-me` 技能，并写明审查范围。

**好的范围：** 模块路径、已有设计草稿、明确的变更类型（新建模块 / 跨模块能力 / 大版本升级）。

**差的范围：** 「帮我设计 XX」「把整个系统理一遍」——问题会发散，前沿轮次失控。

---

## 3. 设计树与会话协议

`grilling` 协议将待决事项映射为**设计树**：每个决策分叉出依赖它的子决策。

### 3.1 前沿（frontier）

**前沿** = 前置条件已 settled、**现在就能问**的所有问题。
每轮一次性问完当前前沿上的全部问题，编号并给出推荐答案；等开发者回答后再进入下一轮。

```text
❓ **Q1** - **模块归属**：permission 的 grant 逻辑应放在 kernel 还是 platform？

➡️ 推荐：kernel。grant 是租户侧 IAM，platform 只做租户生命周期。

---

❓ **Q2** - **持久化边界**：是否需要新表 nx_permission_grant？

➡️ 推荐：是，单表 + Dao，无 join。
```

### 3.2 轮次推进

- 开发者回答 → 已决节点推进前沿 → 解锁依赖它们的下一层问题
- 某题答案依赖本轮尚未回答的题 → 放到**下一轮**，不要混进当前轮
- Agent 需要查仓库事实时，自行派发探索；**不把能查的事问开发者**
- 探索进行中时，仅阻塞**依赖该事实**的下游问题；其余前沿问题照常提问

### 3.3 结束条件

前沿为空，且开发者确认「理解一致」→ 会话结束 → 可进入 `java:project` / `java:design` / 后续技能。

---

## 4. 开发者最佳实践

### 4.1 会前准备（5～15 分钟）

1. **写一句目标**：「我要决定 X 放在哪个模块 / 用什么契约 / 是否新建 Maven 模块」
2. **附上锚点**：相关 `AGENTS.md` 段落、已有设计草稿、目标包路径
3. **列出已知约束**：例如「不能动 base」「kernel 与 platform 不能互依」
4. **标出已决项**：避免重复讨论已经锁定的基线（JDK 25、Jakarta REST 等见 `architecture-decision.md`）

### 4.2 会中回答

| 做法 | 原因 |
|------|------|
| 对推荐答案直接说「采纳 / 否决 + 理由」 | 加快收敛；否决时一句理由即可 |
| 不确定时说「延后到 L2 §12」或「需要 spike」 | 避免假装已决 |
| 一次只改一个决策维度 | 便于前沿重算，减少回滚 |
| 发现范围膨胀时喊停，收窄审查对象 | 防止单会话变成全库架构评审 |

### 4.3 会后确认

用一句话复述：**「我们锁定了 A、B、C；下一步走 java:design L1」**。不要只说「可以了」而不点名决策项。

---

## 5. Agent 最佳实践

| 职责 | 做法 |
|------|------|
| 查事实 | 读 `AGENTS.md`、目标模块源码、`skills/java/`、已有 `docs/design/` |
| 不问废话 | 规范条文、包结构、现有类名 — Agent 自己查 |
| 每轮给推荐答案 | 基于仓库约束给出默认推荐，降低开发者认知负担 |
| 尊重门禁 | 未安装 `grill-me` 不得代行设计决策；未确认一致不得写实现代码 |
| 控制粒度 | L0 琐碎变更可建议跳过 grill；L2/L3 必须完整走树 |
| 并行探索 | 多模块归属问题时，可并行读代码，但**决策仍等开发者逐轮回答** |

---

## 6. 何时必做 / 可跳过

与
[`design-scenarios.md`](../skills/java/java-design/references/design-scenarios.md)
对齐：

| 场景 | grill-me | 说明 |
|------|----------|------|
| 新建 Maven 模块、改 reactor、改依赖方向 | **必经** | 在 `java:project` 之前 |
| 新业务域、新模块设计、重大契约变更 | **必经** | 在 `java:design` 之前 |
| 跨模块能力、kernel/platform 归属模糊 | **必经** | 在动手前 |
| JDK / Spring / 大依赖迁移方案 | **建议** | 路径不唯一时 |
| 四步法草稿完成、交 develop 前 | **可选** | 大方案复审 |
| 单端点字段增补、契约已清晰 | **可跳过** | 直接 `java:develop` + L0 |
| BOM patch、revision bump | **通常跳过** | 走 `java:dependency-upgrade` |
| 查编码规范、跑测试、修已定位 Bug | **不调用** | 走对应 `java:*` 技能 |

**不得在实现中途用 grill-me 代替设计。** 开发中发现归属错误 → 回到 `java:design`，必要时再开一轮。

---

## 7. 结论沉淀（必做）

grill-me 产出的是**双方确认的设计树**，不能只留在聊天记录。

### 7.1 推荐格式：设计树表

在设计文档开头或独立小节记录（示例见
[`innospots-nexus-service/docs/service-framework-design.md`](../innospots-nexus-service/docs/service-framework-design.md)
§1.1）：

```markdown
### 设计树（grill 结论）

| 节点 | 锁定结论 |
|------|----------|
| 模块拓扑 | 维持八个中立库 + 两个框架适配；不另建 security Maven 模块 |
| 异步模型 | 公共 API 仅 `T` / `CompletionStage<T>` / `Flow.Publisher<T>` |
| 异常 | 只使用 `NexusException` + 类型化 `StatusCode` |
```

### 7.2 落盘位置

| 变更规模 | 落盘位置 |
|----------|----------|
| 单项技术选型 | `docs/design/adr/NNNN-<slug>.md` |
| 完整域方案 L2+ | `docs/design/` 或 `<module>/docs/*-design.md` |
| 小范围 L0 | PR 描述中的「设计树」块 |
| 结构变更 | 同时更新 PR + 必要时 `module-layout` 相关说明 |

模板与 ADR 规范见
[`docs/design/adr/README.md`](design/adr/README.md)、
[`design-deliverables.md`](../skills/java/java-design/references/design-deliverables.md)。

### 7.3 与 java:design 四步法的关系

```text
grill-me（设计树：归属、拓扑、关键分叉）
    ↓
java:design ①定归属 ②建词汇 ③划边界 ④定契约
    ↓
（可选）grill-me 复审
    ↓
java:develop
```

grill-me 解决「往哪走、选哪条路」；四步法解决「怎么写进文档与契约」。
不要重复：已在设计树锁定的节点，四步法直接引用，不再开新分歧。

---

## 8. 典型工作流示例

### 8.1 新建 Maven 模块

```text
grill-me（模块类型、依赖方向、是否 application/adapter）
    → 记录设计树
    → java:project（POM / reactor）
    → java:design（L1/L2）
    → java:develop → java:check
```

### 8.2 新领域（无新模块）

```text
grill-me（kernel vs platform、包名、与相邻域边界）
    → java:design L1
    → java:develop → java:check
```

### 8.3 大版本升级

```text
grill-me（影响模块、回滚策略、是否分阶段）
    → ADR 或升级计划文档
    → java:dependency-upgrade / java:spring
    → java:check
```

---

## 9. 反模式

| 反模式 | 后果 | 正确做法 |
|--------|------|----------|
| 未 grill 直接 `java:project` 建模块 | 依赖方向错误、后期拆模块 | 先 grill，再 project |
| 会话结束不写设计树 | 后续 Agent/开发者重复争论 | 落盘到 PR / design / ADR |
| 用 grill 问「这个类怎么命名」 | 浪费轮次 | `java:reference` → naming |
| 实现中途临时 grill | 已写代码绑架决策 | 回 design，必要时重开 grill |
| 空泛范围「设计整个 service 层」 | 前沿爆炸、无法收敛 | 收窄到具体文档或模块 |
| Agent 不等回答就写代码 | 违反门禁 | 确认一致后再动手 |
| 把推荐答案当最终结论 | 开发者未真正决策 | 每轮等待明确采纳/否决 |
| 用 grill 代替完整 L2 设计文档 | 只有决策表、缺契约细节 | grill 后仍走 `java:design` 四步法 |

---

## 10. 出口检查清单

会话结束、准备进入下一技能前：

- [ ] 设计树表已写入 PR、`docs/design/` 或 ADR（非仅聊天记录）
- [ ] 模块归属、依赖方向、兼容面无未闭合假设
- [ ] 已明确下一步：`java:project` / `java:design` / `java:develop` / 升级类技能
- [ ] **确认前未开始写实现代码或改 POM**
- [ ] L2/L3 方案已标文档状态（提案 / 已接受），develop 依「已接受」执行

---

## 11. 相关文档

| 文档 | 内容 |
|------|------|
| [`skills/java/java-reference/references/grill-me.md`](../skills/java/java-reference/references/grill-me.md) | 调用时机、技能衔接、安装 |
| [`skills/java/java-design/references/design-four-steps.md`](../skills/java/java-design/references/design-four-steps.md) | 设计四步法 |
| [`skills/java/java-design/references/design-scenarios.md`](../skills/java/java-design/references/design-scenarios.md) | L0–L3 场景与 grill 是否必经 |
| [`skills/java/java-design/references/architecture-decision.md`](../skills/java/java-design/references/architecture-decision.md) | 必须先出方案的触发项 |
| [`docs/design/adr/README.md`](design/adr/README.md) | ADR 存放约定 |
| [`AGENTS.md`](../AGENTS.md) | 模块职责与依赖红线 |
