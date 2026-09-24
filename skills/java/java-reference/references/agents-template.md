# AGENTS.md 生成模板

为 **新建工程**、**新建 Maven 模块**、**L2/L3 设计方案** 提供可复制的 `AGENTS.md` 规范骨架。
Agent 须按本模板产出或增补文档，并**继承**仓库根 [`AGENTS.md`](../../../../AGENTS.md) 的上位约束。

**不是**设计产出物本身——设计文档仍按 `java:design` → `design-deliverables.md`；
本模板用于需要**新增或修订 Agent 操作指南**时。

---

## 何时生成 / 修订 AGENTS.md

| 场景 | 动作 | 产出位置 |
|------|------|---------|
| **新建独立仓库**（greenfield，沿用 Nexus 分层） | 从下方「仓库根模板」生成完整 `AGENTS.md` | 新仓库根目录 |
| **本仓库新建 Maven 模块** | 在根 `AGENTS.md` **追加**模块职责与依赖条目；**不**默认创建模块级 `AGENTS.md` | 根 `AGENTS.md` |
| **新建 adapter / application 等边界模块** | 同上 + 在依赖规则中登记新 artifact | 根 `AGENTS.md` |
| **L2 跨模块方案** | 设计文档中增加「AGENTS 对齐」节（见 design-deliverables）；若持久改变模块边界则**同步修订**根 `AGENTS.md` | 设计文档 + 根 `AGENTS.md` |
| **模块内特殊 Agent 约束**（少见） | 使用「模块补充模板」，须显式引用根 `AGENTS.md` | `<artifact-id>/AGENTS.md` |

**禁止：** 未经开发者确认，将单次设计结论写入根 `AGENTS.md`；模块 API 索引仍归
`references/modules/`，不得混入 AGENTS 模板。

**流程衔接：**

```text
grill-me  →  核对根 AGENTS.md  →  java:project（动 POM）  →  java:design
    →  按本模板生成/修订 AGENTS 条目  →  java:develop  →  java:check（含 AGENTS 合规）
```

---

## 生成前必读（上位约束）

从根 [`AGENTS.md`](../../../../AGENTS.md) **不得省略或弱化**的章节：

1. **核心约束** — greenfield、不复制 legacy、编译验证、模块 API 索引策略
2. **Agent 工作流** — grill-me 优先触发条件、Java 技能路由表、典型链路
3. **依赖规则** — 单向链、BOM/parent、portal/platform 隔离
4. **DDD 规则**
5. **编码规范** — 指向 `skills/java/java-reference/standards/`
6. **验证** — `mvn validate` / `mvn test` / `help:effective-pom`

新建模块时：**只增补**「模块职责」与「依赖规则」中与本模块相关的条目；
其余章节保持与根文件一致，用链接引用而非复制粘贴全文（避免分叉）。

---

## 仓库根模板（新建独立仓库时使用）

将 `{{...}}` 替换为实际值。若仓库是 **innospots-nexus 子集或 fork**，优先**修订现有根
`AGENTS.md` 而非另起文件。

```markdown
# Agent 操作指南

{{ONE_LINE_PROJECT_DESCRIPTION — 例：本仓库为 XXX greenfield 工程。旧 YYY 项目仅作参考，不是源码模板。}}

## 核心约束

- 不得将 legacy 源码复制到本仓库。
- 不得将 legacy 文件移动到本仓库。
- 不得机械复刻 legacy POM 或包结构。
- 创建新行为前，应询问或推断当前开发者意图。
- 保持 foundation 轻量、依赖最小。
- **代码生成完成后必须编译验证** — 每次修改完 Java 源文件后，立即运行 `mvn clean compile`。
- 普通代码开发过程中，不得创建、更新或同步
  `skills/java/java-reference/references/modules/` 下的模块 API 参考文档。
- 模块 API 参考文档仅在开发者明确要求模块或项目目录扫描时生成或刷新。

## Agent 工作流

### grill-me（方案压力测试，优先）

在编写代码、修改 POM、定架构或注册新模块**之前**，若需求**通用宽泛不具体**、
**新需求**、**新设计**或**新建工程/模块**，**必须先**使用 `grill-me`。
完整说明见 [`skills/java/java-reference/references/grill-me.md`](skills/java/java-reference/references/grill-me.md)。

未安装时：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

### Java 技能（按需优先选用）

| 场景 | 技能 | 何时用 |
|------|------|--------|
| 查规范、不确定走哪条路 | `java:reference` | 只读、规范路由 |
| 新建/调整 Maven 模块、POM | `java:project` | 动工程骨架 |
| 架构/契约/技术方案设计 | `java:design` | 动设计决策 |
| 写功能、修 Bug、重构、单测 | `java:develop` | 动实现 |
| 编译、测试、质量/依赖/安全检查 | `java:check` | 验证出口 |
| Spring Boot 组装 | `java:spring` | Spring 生态 |
| 工程 revision 升版 | `java:project-upgrade` | 自身产物版本 |
| JDK/第三方升级 | `java:dependency-upgrade` | 外部技术栈 |

典型链路：`grill-me → java:project → java:design → java:develop → java:check`。

## 模块职责

### `{{ARTIFACT_ID_1}}`

- **职责：** {{WHAT_IT_OWNS}}
- **拥有：** {{KEY_PACKAGES_OR_CAPABILITIES}}
- **不得拥有：** {{EXPLICIT_EXCLUSIONS — 引用相邻模块}}
- **middleware / 运行时：** {{middleware-free | 允许的中间件 API | 禁止 Spring Boot auto-config}}

### `{{ARTIFACT_ID_2}}`

- （同上结构，每个 Maven 模块一节）

## 依赖规则

- `{{FOUNDATION_MODULE}}` 必须保持 middleware-free。
- 内部 Java 模块应继承 `{{PARENT_ARTIFACT}}`。
- 依赖版本归属 `{{BOM_ARTIFACT}}`。
- 主依赖方向为
  `{{DEPENDENCY_CHAIN — 例：base → core → console → portal ↘ platform}}`。
- {{PORTAL_PLATFORM_RULE — 例：Portal 与 platform 不得相互依赖。}}
- 业务特定基础设施归属其业务模块或 adapter/application 模块。

## DDD 规则

- 按职责与边界命名包与模块。
- 保持领域概念独立于基础设施实现。
- 中间件集成优先采用 ports and adapters。
- 仅在边界足够清晰、可独立测试时再新增模块。

## 编码规范

生成代码或文档前，AI agent 必须加载
`skills/java/java-reference/standards/` 下的完整规则。

| 文件 | 内容 |
|------|------|
| [`code-style.md`](skills/java/java-reference/standards/code-style.md) | 花括号、缩进、行宽、import 顺序 |
| [`code-comments.md`](skills/java/java-reference/standards/code-comments.md) | Javadoc 层级 |
| [`naming.md`](skills/java/java-reference/standards/naming.md) | 命名约定 |
| [`api-design.md`](skills/java/java-reference/standards/api-design.md) | 方法签名、异常、事务 |
| [`domain-module-initialization.md`](skills/java/java-reference/standards/domain-module-initialization.md) | 领域初始化 |
| [`module-skills.md`](skills/java/java-reference/standards/module-skills.md) | 模块 API 索引策略 |

## 验证

当本地 JDK 支持配置的 release 时，结构变更后运行：

```bash
mvn validate
mvn test
mvn -q help:effective-pom
```

若本地 JDK 低于 {{JAVA_RELEASE}}，应报告环境不匹配，而不是降低项目基线。
```

---

## 本仓库新建模块 — 根 AGENTS.md 增补片段

在 [`AGENTS.md`](../../../../AGENTS.md) 的「模块职责」与「依赖规则」中**追加**（不替换既有条目）：

### 模块职责节（追加）

```markdown
### `innospots-nexus-{{MODULE_NAME}}`

- **职责：** {{ONE_SENTENCE — 例：外部 XXX 系统的 adapter，隔离第三方 SDK。}}
- **拥有：** {{PACKAGES_OR_TABLES_OR_ENDPOINT_PREFIX}}
- **不得拥有：** {{LIST — 与 module-ownership.md / 根 AGENTS 相邻模块对齐}}
- **依赖：** 直接依赖 `{{MINIMAL_DIRECT_DEPS}}`；禁止 {{FORBIDDEN_DEPS}}。
- **middleware / 运行时：** {{例：不得绑定 Spring Boot auto-configuration。}}
```

### 依赖规则节（追加）

```markdown
- `innospots-nexus-{{MODULE_NAME}}` 可依赖 `{{UPSTREAM_MODULES}}` 及传递的 foundation。
- `innospots-nexus-{{MODULE_NAME}}` **不得**依赖 `{{FORBIDDEN_MODULES}}`。
```

增补后须运行 `java:check` → [agents-compliance-checklist.md](../../java-check/references/agents-compliance-checklist.md)。

---

## 模块补充模板（`<artifact-id>/AGENTS.md`，可选）

仅当模块有**根 AGENTS 无法表达的局部约束**时使用。文件开头**必须**声明上位文件：

```markdown
# Agent 操作指南 — `innospots-nexus-{{MODULE_NAME}}`

> **上位约束：** 本文件补充仓库根 [`AGENTS.md`](../../AGENTS.md)。
> 冲突时以根文件为准。

## 本模块 Agent 约束

- **范围：** 仅适用于 `innospots-nexus-{{MODULE_NAME}}` 源码与 `pom.xml`。
- **额外禁止：** {{MODULE_SPECIFIC_FORBIDDEN — 例：不得引入 Reactor。}}
- **额外拥有：** {{MODULE_SPECIFIC_OWNED — 例：仅拥有 `com.innospots.nexus.adapter.xxx` 包。}}
- **设计文档：** {{DOCS_PATH — 例：`docs/adapter-design.md`}}

## 与根 AGENTS.md 的对齐声明

| 根 AGENTS 章节 | 本模块是否适用 | 说明 |
|---------------|---------------|------|
| 核心约束 | 是 | 完全继承 |
| Agent 工作流 | 是 | 完全继承 |
| 模块职责 | 部分 | 见上文「本模块 Agent 约束」 |
| 依赖规则 | 部分 | 见根文件 + 本文件禁止项 |
| DDD / 编码规范 / 验证 | 是 | 完全继承 |
```

---

## 设计文档中的 AGENTS 对齐节（L2 方案）

在 L2 设计文档中增加（不必单独落盘 AGENTS.md）：

```markdown
## N. AGENTS 对齐

| 检查项 | 结论 |
|--------|------|
| 归属模块 | `{{artifact-id}}` — 符合根 AGENTS.md §{{模块名}} |
| 依赖方向 | {{单向链说明}}；无 portal↔platform |
| 是否需修订根 AGENTS.md | 是 / 否 — {{若「是」，列出增补条目}} |
| grill-me 结论引用 | {{PR / ADR 链接}} |
```

---

## 生成后自检

Agent 完成 AGENTS 生成或增补后，**自行**对照：

- [agents-compliance-checklist.md](../../java-check/references/agents-compliance-checklist.md)

交付前由 `java:check` 正式核验。

---

## 相关文档

- 根规范：[`AGENTS.md`](../../../../AGENTS.md)
- 模块归属：[`module-ownership.md`](module-ownership.md)
- grill-me：[`grill-me.md`](grill-me.md)
- 设计产出：[`design-deliverables.md`](../../java-design/references/design-deliverables.md)
- 工程交付：[`project-deliverables.md`](../../java-project/references/project-deliverables.md)
