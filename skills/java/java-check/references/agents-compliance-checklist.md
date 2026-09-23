# AGENTS.md 合规检查清单

`java:check` 在 L5 静态巡检中使用的 **AGENTS 规范符合性** 检查项。
对照对象为仓库根 [`AGENTS.md`](../../../../AGENTS.md)；若存在模块级
`<artifact-id>/AGENTS.md`，须同时对照且**不得与根文件冲突**。

设计侧 AGENTS 对齐节见 [`agents-template.md`](../../java-reference/references/agents-template.md)。

---

## 何时执行

| 触发 | 检查范围 |
|------|---------|
| 新建/修订 `AGENTS.md`（根或模块补充） | 被改动的 AGENTS 文件 + 相关 `pom.xml` |
| 新建 Maven 模块（`java:project` 交付后） | 根 `AGENTS.md` 是否已增补模块职责与依赖规则 |
| L2 设计含「AGENTS 对齐」节 | 设计文档对齐表 vs 根 AGENTS + 实际代码归属 |
| 提交前全量评审（可选） | 变更模块是否仍符合根 AGENTS 模块边界 |

**不在此检查：** 编码风格、命名、DAO 细则——见 [review-checklist.md](review-checklist.md)。

---

## A. 文档结构（必需章节）

根 `AGENTS.md` **必须**包含以下章节（标题可略有变化，语义须完整）：

| # | 章节 | 不合格信号 |
|---|------|-----------|
| A1 | **核心约束** | 缺少 greenfield/legacy/编译验证/模块 API 索引策略任一条 |
| A2 | **Agent 工作流** | 缺少 grill-me 说明或 Java 技能路由表 |
| A3 | **grill-me 触发条件** | 未覆盖「通用宽泛」「新需求」「新设计」「新建工程/模块」 |
| A4 | **模块职责** | 缺少本变更涉及的 `innospots-nexus-*` 模块说明 |
| A5 | **依赖规则** | 缺少 BOM/parent、单向依赖链、kernel/platform 隔离 |
| A6 | **DDD 规则** | 完全缺失 |
| A7 | **编码规范** | 未指向 `skills/java/java-reference/standards/` |
| A8 | **验证** | 缺少 `mvn validate` / `mvn test` / `help:effective-pom` |

模块补充 `AGENTS.md` ** additionally**：

| # | 检查项 | 不合格信号 |
|---|--------|-----------|
| A9 | 上位声明 | 未链接根 `AGENTS.md` 或未声明冲突以根为准 |
| A10 | 对齐声明表 | 缺少与根 AGENTS 各章节的对齐说明 |

---

## B. Agent 工作流合规

| 检查项 | 不合格信号 |
|--------|-----------|
| grill-me 安装命令 | 新设计/新模块路径未给出 `npx skills use ... grill-me` |
| Java 技能表完整 | 缺少 `reference/project/design/develop/check` 任一行 |
| 典型链路 | 未说明 grill-me 在新模块/新设计前的位置 |
| 上位约束声明 | 未说明 AGENTS 与 `standards/` 的裁决关系 |
| 可跳过 grill-me 场景 | 完全未提及（易导致过度或不足使用） |

---

## C. 模块职责 vs 实际工程

对照 `pom.xml`、`dependency:tree` 与源码包结构：

| 检查项 | 不合格信号 |
|--------|-----------|
| 每个 reactor 模块在 AGENTS 有职责节 | 新模块已注册但 AGENTS 无 `###` 条目 |
| 职责与代码一致 | AGENTS 写「不得拥有 REST 端点」但模块内有 `*Endpoint` |
| base middleware-free | AGENTS 与 POM 均允许 base 引数据库/Spring |
| core 不绑 Spring Boot auto-config | AGENTS 或代码出现 `@AutoConfiguration` 在 core |
| kernel ↔ platform | AGENTS 或 POM 允许互依；或设计对齐表声称无互依但 tree 有 |
| plugin / console 边界 | catalog 索引归 console；Page DSL 归 plugin — 写反或代码反 |
| service 模块中立 | service 子模块依赖 kernel/platform/console |
| 禁止清单 | AGENTS「不得拥有」项出现在该模块 diff 中 |

新建模块时对照 [`module-ownership.md`](../../java-reference/references/module-ownership.md)。

---

## D. 依赖规则 vs POM

| 检查项 | 不合格信号 |
|--------|-----------|
| 依赖链单向 | 低层模块依赖高层；或循环依赖 |
| BOM 唯一版本源 | AGENTS 要求 BOM 但新模块 POM 内联 `<version>` |
| parent 正确 | 库模块 parent 非 `innospots-nexus-parent`（spring/quarkus 子模块除外） |
| 最小依赖 | AGENTS 登记的直接依赖与 POM 重复声明可传递模块 |
| 新增模块依赖条目 | 根 AGENTS「依赖规则」未追加新 artifact 的上下游 |
| kernel/platform 隔离 | 任一方 POM 或 AGENTS 增补允许依赖对方 |

---

## E. 设计文档 AGENTS 对齐节（若有）

L2 设计文档中的「AGENTS 对齐」表：

| 检查项 | 不合格信号 |
|--------|-----------|
| 归属模块明确 | 空白或「待定」 |
| 与四步法归属一致 | 对齐表模块与 §4 归属表冲突 |
| 修订根 AGENTS 声明 | 持久改变边界却写「否」且未在 PR 中增补 AGENTS |
| grill-me 引用 | 新域/新模块设计无 grill-me 结论链接 |
| 禁止项可验证 | 对齐表声称符合但设计契约违反 AGENTS 禁止清单 |

---

## F. 与模板一致性

对照 [`agents-template.md`](../../java-reference/references/agents-template.md)：

| 检查项 | 不合格信号 |
|--------|-----------|
| 使用了正确模板变体 | 新独立仓库却只用增补片段；或反之 |
| 占位符已替换 | 残留 `{{MODULE_NAME}}`、`{{ARTIFACT_ID}}` |
| 未重复粘贴全文 | 模块补充文件复制了根 AGENTS 全文导致分叉 |
| 生成后自检 | PR 未提及 agents-compliance 或 check 未跑 AGENTS 项 |

---

## G. 判定级别

| 级别 | AGENTS 相关问题示例 |
|------|---------------------|
| **阻塞** | 新模块无 AGENTS 职责条目；kernel↔platform 互依；base 引中间件；模块 AGENTS 与根冲突；缺少 Agent 工作流/grill-me |
| **警告** | 依赖规则未登记新 artifact 但 POM 已合并；设计对齐节缺失 grill-me 引用；模块补充缺少对齐声明表 |
| **提示** | 章节标题措辞与模板略有不同但语义完整；可跳过 grill-me 场景未写但本次变更不适用 |

---

## 报告格式（AGENTS 专节）

在 `java:check` 输出报告中增加：

```markdown
## AGENTS 合规

| 项 | 结果 | 说明 |
|----|------|------|
| 文档结构 (A) | ✅ / ❌ | |
| Agent 工作流 (B) | ✅ / ❌ | |
| 模块职责 vs 工程 (C) | ✅ / ❌ | |
| 依赖规则 vs POM (D) | ✅ / ❌ | |
| 设计对齐节 (E) | ✅ / N/A | |
| 模板一致性 (F) | ✅ / ❌ | |

### AGENTS 问题

| 级别 | 位置 | 问题 | 建议 |
|------|------|------|------|
| 阻塞 | `AGENTS.md:§模块职责` | … | 按 agents-template 增补 |
```

---

## 相关文档

- 根规范：[`AGENTS.md`](../../../../AGENTS.md)
- 生成模板：[`agents-template.md`](../../java-reference/references/agents-template.md)
- 通用评审：[`review-checklist.md`](review-checklist.md)
- 模块归属：[`module-ownership.md`](../../java-reference/references/module-ownership.md)
