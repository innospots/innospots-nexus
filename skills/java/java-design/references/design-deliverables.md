# 设计产出物规范

`java:design` 的产出是**设计决策与契约**，供 `java:develop` 按图施工。可以是
**仓库内的 Markdown 文档**，也可以是 **PR / Issue / 评审纪要** 中的结构化结论——
按变更规模选用，但结构必须完整、可核对。

**不是**设计产出物：可编译 Java 源码、测试类、POM 改动、模块 API 索引（`skills/.../modules/`）、
`standards/` 规范条文（除非开发者显式要求改规范）。

---

## 按规模选产出形态

| 级别 | 何时使用 | 产出形态 | 是否必须落盘 |
|------|---------|---------|-------------|
| **L0 轻量** | 单端点字段增补、单方法行为澄清、无新模块/无新状态码/无兼容面影响 | PR 描述或 Issue 评论中的「设计结论」块（用下方 L0 模板） | 否 |
| **L1 域内** | 在已有 Maven 模块内新增或扩展一个业务域；边界清晰、不跨模块 | 模块内设计文档 **或** 既有设计文档的新章节 | 是（推荐） |
| **L2 完整方案** | 触发 `architecture-decision.md` 中「必须先出方案」任一条；跨模块；新中间件；存储大改 | 独立设计文档 +（可选）测试范围附录 | 是 |
| **L3 单项 ADR** | 一个独立架构/技术选型决策（可与 L2 并存） | ADR 文件或父文档内的「决策记录」节 | 是 |

未达 L1 门槛仍须走完四步法 mentally，但可用 L0 模板浓缩记录。
**不得**因「改动小」跳过归属、词汇、失败语义与测试范围思考。

---

## 文件格式与命名

| 项 | 约定 |
|----|------|
| 格式 | **Markdown**（`.md`），UTF-8，中文叙述为主 |
| 扩展名 | 设计文档：`*-design.md`；ADR：`NNNN-<kebab-slug>.md`（四位序号，从 `0001` 起） |
| Front matter | **不需要** YAML；元数据写在正文「文档定位」节 |
| 图表 | Mermaid 或 ASCII；与正文同文件，不单独拆图库 |
| 代码块 | 只写**契约骨架**（record 字段、端点签名、伪 SQL 表意），不写可编译实现 |

### 文件命名示例

```text
permission-design.md
2026-09-04-console-catalog-and-navigation-design.md   # 带日期的规格名
0003-use-event-bus-for-in-process-events.md             # ADR
```

---

## 存放目录（按归属选一处）

| 设计范围 | 目录 | 示例 |
|---------|------|------|
| **平台级 / 跨多模块** | `docs/design/` | `docs/design/multi-tenant-governance-design.md` |
| **带日期的规格迭代**（superpowers 流程） | `docs/superpowers/specs/` | `docs/superpowers/specs/2026-09-04-console-catalog-and-navigation-design.md` |
| **单项架构决策（ADR）** | `docs/design/adr/` | `docs/design/adr/0002-kernel-platform-isolation.md` |
| **单 Maven 模块、单域或子系统** | `<artifact-id>/docs/` | `innospots-nexus-kernel/docs/permission-design.md` |
| **模块内子系统（较深）** | `<artifact-id>/docs/<area>/design/` | `innospots-nexus-plugin/docs/plugin/design/plugin-runtime-design.md` |

**选择规则：**

1. 只影响一个 `innospots-nexus-*` 模块 → 放该模块 `docs/`。
2. 影响 kernel **与** platform/console 等多模块协作 → 放 `docs/design/`。
3. 已存在同域设计文档 → **追加章节或修订同一文件**，不要平行写第二份冲突文档。
4. ADR 记录「一件事一个决策」；完整域方案用 L1/L2 设计文档，ADR 可链接过去。

**禁止：**

- 把设计文档放进 `src/main/resources` 或 `skills/java/java-reference/references/modules/`（后者是 API 索引，不是设计库）。
- 未经开发者要求把设计结论写进 `standards/` 或 `AGENTS.md`（影响全局规范须单独评审）。

---

## L2 标准文档结构（完整方案）

新建或大幅修订的设计文档**按顺序**包含以下章节（无内容则写「无」并简述原因）：

```markdown
# <标题>

## 1. 文档定位

- 状态：草案 / 评审中 / 已接受 / 已废弃
- 日期：YYYY-MM-DD
- 归属模块：<artifact-id> 列表
- 关联文档：<链接>

## 2. 背景与目标

触发原因、要解决的问题、成功标准。

## 3. 不在本方案中的能力

明确排除项，防止范围蔓延。

## 4. 归属与词汇（四步法 ①②）

| 概念 | 英文名 | 归属模块/包 | 技术 ID | 稳定业务键 | 备注 |
|------|--------|------------|---------|-----------|------|

## 5. 边界与包结构（四步法 ③）

- 领域包路径（领域优先，单包 ≤15 类）
- 与相邻域交互方式（调用 / 事件 / 禁止直接依赖）
- Session 作用域 vs Entity 基类选择

## 6. API 与分层契约（四步法 ④）

### 6.1 端点

| 方法 | 路径 | 请求 | 响应 | 委托 |
|------|------|------|------|------|

### 6.2 分层调用

`endpoint → service → operator → dao` 职责说明；禁止项。

### 6.3 Request / VO（record 骨架）

```java
// 仅字段与校验意图，非最终实现
public record XxxCreateRequest(...) {}
```

### 6.4 持久化意图

表名、主键、索引、单表约束（无 join）；每表 `*Dao`；自定义访问清单（default 方法级）。
**禁止** mapper.xml / beans.xml；配置只用 yaml + Java config（见 [persistence-contract.md](persistence-contract.md)）。

## 7. 失败与状态码

| 场景 | StatusCode（module+category+local） | 抛出边界 | HTTP 映射意图 |
|------|-------------------------------------|---------|--------------|

推迟实现的端点须标注未来状态码策略（见 exception-contract.md）。

## 8. 事务、幂等与并发

写操作事务边界；重复调用语义；锁或乐观策略。

## 9. 兼容与迁移

公共兼容面变化、数据迁移、双写/灰度（若有）。

## 10. 测试范围

引用或内嵌 test-scope 清单（契约测试 + 行为单测 + 不测范围）。

## 11. 架构约束自检

对照 quick-constraints 与 AGENTS.md 模块职责的勾选清单。

## 12. 开放问题

未决项与决策截止；已闭合项移到「已确认决策」。
```

已有文档（如 `permission-design.md`）可保留自有章节编号，但须**覆盖上表信息面**，
develop 不得因章节标题不同而遗漏契约。

---

## L0 轻量结论模板（PR / Issue / 评审纪要）

无需新建文件时，将下列块粘贴到 PR 描述或 Issue：

```markdown
## 设计结论（java:design L0）

- **归属**：`<module>` / `<domain>` / `<subpackage>`
- **Maven 模块**：无新建 / 需 `java:project`（说明 artifact 类型）
- **词汇**：<关键术语对齐说明，一行>
- **契约变更**：<端点/方法/record 字段 diff 摘要>
- **持久化**：无新表 / 新表+Dao 清单；禁止 join/XML/properties
- **配置**：无 / 新增 yaml 键路径 + 配置类归属
- **事件**：无 / <eventType + 发布域>
- **失败语义**：<新增/复用 StatusCode，或「无新增失败路径」>
- **测试范围**：<契约/单测点，或「沿用既有测试」>
- **兼容面**：无 / <说明>
- **develop 入口**：<建议先写的测试类或包路径>
```

---

## 附录模板（可合并进主文档或单独文件）

### A. API 契约附录

与主文档 §6 相同表格；端点超过 7 个时**必须**独立附录并做端点复审。

### B. 状态码矩阵

与主文档 §7 相同；新增码前搜索现有目录（见 exception-status-code.md）。

### C. 测试范围附录

| 类型 | 用例/类名（计划） | 断言要点 |
|------|------------------|---------|
| 实体契约 | `XxxEntityContractTest` | 表名、基类、索引 |
| DAO 契约 | `XxxDaoContractTest` | BaseMapper 绑定 |
| 端点契约 | `XxxEndpointContractTest` | 路径、R<T>、推迟行为 |
| 行为单测 | `XxxServiceTest` | 正常路径 + 各拒绝路径 |

完整期望见 [test-scope.md](test-scope.md)。

### D. ADR（单项决策）

使用 [architecture-decision.md](architecture-decision.md) 中的 ADR 模板；文件放
`docs/design/adr/NNNN-<slug>.md`，并在相关 L1/L2 文档中链接。

---

## 从设计到 project / develop 的交接

四步法分步门禁见 [design-four-steps.md](design-four-steps.md)。

### → java:project（按需）

当 ① 定归属判定需要**新建 Maven 模块**（adapter、application、可运行子模块等）时：

| 设计交付 | project 用法 |
|---------|-------------|
| 模块类型与 artifact 名 | 注册 `<modules>`、写 POM、BOM 登记 |
| 最小直接依赖 | dependency-conventions 选型 |
| grill-me / ADR 结论 | PR 或 `docs/design/adr/` |

**仅新领域 Java 包、无新 Maven 模块** → 跳过 project，直接 develop。

工程交付清单见 `java:project` →
[project-deliverables.md](../../java-project/references/project-deliverables.md)。

### → java:develop

实现侧完整规范见 `java:develop` →
[develop-deliverables.md](../../java-develop/references/develop-deliverables.md)。

设计阶段结束时，交付物须让 develop **无需猜测**：

| 交付项 | develop 如何使用 |
|--------|-----------------|
| 包路径与类名 | 直接作为 `src/main/java` 目标 |
| record / 端点签名骨架 | 复制为源文件起点（再按 code-templates 补全） |
| 状态码表 | 先写状态码契约测试，再实现枚举 |
| 测试范围表 | 测试先行的类清单与红灯顺序 |
| 明确「未实现」 | `TODO` + `NexusException`，禁止 `UnsupportedOperationException` |

设计文档**不要求**与代码逐步同步；但 **兼容面或契约变更** 时须在同一 PR 或紧随 PR 更新文档。

---

## 与 grill-me 的关系

- **`java:design` 开始前必经 `grill-me`**（未安装则
  `npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"`）。
- **L2 / L3** 或架构触发项：在文档草案阶段完成 grill-me，结论写入 §12 或 ADR。
- grill-me 产出的是**双方确认的设计树**，应沉淀到上述文档或 L0 块，而不是只留在聊天记录。

---

## 自检清单（设计完成）

- [ ] 已选定 L0 / L1 / L2 / L3 级别，且落盘位置符合上表
- [ ] 四步法信息面齐全（归属、词汇、边界、契约）
- [ ] 每个应用可见失败有 StatusCode 行
- [ ] 测试范围已列出，develop 可按表测试先行
- [ ] 未把实现代码、POM、模块 API 索引误当作设计产出
- [ ] 持久化无 join/XML/properties 规划；Dao 与 yaml 配置已写清（见 persistence-contract.md）
- [ ] 四步法分步门禁已勾选（见 design-four-steps.md）
- [ ] 需新 Maven 模块时已规划 `java:project`，而非在 develop 里偷建 POM
- [ ] 需要评审的变更已与开发者确认文档状态为「已接受」再交 develop
