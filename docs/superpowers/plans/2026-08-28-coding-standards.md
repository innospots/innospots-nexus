# 编码规范改进实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 将既有 Nexus 开发规范整理为连贯、基于代码的 naming、Java 源码构造、文档、API
design 与 domain initialization 指南。

**架构：** 每个 concern 保留在既有权威 standards
file，用交叉引用替代重复规则，并保留 `AGENTS.md` 的全部
module boundary。从当前源码 name 推导示例，
不重命名源码，也不将历史 inconsistency 当作
requirement。

**技术栈：** Markdown、Java 25 项目约定、Jakarta REST/JPA、
MyBatis-Plus、MapStruct、Lombok、Maven verification 策略。

---

### Task 1：扩展 naming 决策体系

**文件：**
- Modify: `standards/naming.md`

- [x] **Step 1：保留既有 project-specific suffix 与 package 规则**

Review 当前 type table、domain-first package layout 与 file naming
规则。保留 `Endpoint`、`Dao`、`Entity`、`Request`、`Vo`、`Converter`、
`StatusCode`、`Event`、`EventHandler` 约定。

- [x] **Step 2：添加 naming 推导规则**

添加 business vocabulary、qualifier ordering、technical responsibility
suffix、request/view name、method verb、field 与 parameter、boolean 与
collection name、time/unit name、acronym casing、persistence name、test
name。使用 `PlatformUserPasswordEntity`、
`RoleStatusUpdateRequest`、`CapabilityRegistry`、`PluginManager`、
`ClasspathPluginDiscovery`、`hasAvailableThread` 等当前示例。

- [x] **Step 3：添加反例与 naming checklist**

显式 discourage 模糊名（`CommonUtils`、`DataManager`、`process`、`flag`）与冗余
名（`RoleBusinessService`、`RoleDataDao`）。以 short decision checklist 结尾。

- [x] **Step 4：验证 naming 文档**

运行：

```bash
rg -n '^## ' standards/naming.md
git diff --check -- standards/naming.md
```

预期：required naming 章节齐全，diff check 无错误。

### Task 2：澄清 Java 源码与 Lombok 风格

**文件：**
- Modify: `standards/code-style.md`

- [x] **Step 1：添加源码布局与 multiline 格式化规则**

文档化 member order、one declaration per line、annotation placement、method
chain wrapping、record formatting、lambda clarity、local-variable scope。

- [x] **Step 2：修正并澄清 Lombok 规则**

mutable persistence entity 与 configuration
binder 要求 `@Getter`/`@Setter`，immutable 或 behavior-oriented internal model 仅暴露所需
accessor，并保留 `@Slf4j` 与 constructor injection
指导。

- [x] **Step 3：添加实现卫生规则**

覆盖 dependency field、logging、immutable collection exposure、magic value、
避免 `System.out` 或 hidden global mutable state。architectural boundary 交叉引用
API design。

- [x] **Step 4：验证 style 文档**

运行：

```bash
rg -n '^## ' standards/code-style.md
git diff --check -- standards/code-style.md
```

预期：新 source layout 与 implementation 章节存在，无 whitespace
错误。

### Task 3：扩展 comment 与 Javadoc 约定

**文件：**
- Modify: `standards/code-comments.md`

- [x] **Step 1：定义文档 ownership**

添加 package-level documentation、contract/implementation documentation、record
component 与 enum documentation、override-method 规则、public API
expectation。

- [x] **Step 2：定义有用的 inline 与 TODO comment**

comment 需说明 rationale、lifecycle、concurrency、security、compatibility、
non-obvious side effect。TODO 需聚焦 missing
behavior 或 boundary；禁止 commented-out code 与 change-history comment。

- [x] **Step 3：验证 comment 文档**

运行：

```bash
rg -n '^## ' standards/code-comments.md
git diff --check -- standards/code-comments.md
```

预期：package、override、record/enum、TODO 指导可发现，diff check 干净。

### Task 4：完善 API design 指导

**文件：**
- Modify: `standards/api-design.md`

- [x] **Step 1：澄清 contract 与 dependency direction**

文档化 interface 何时合理、implementation naming、constructor
injection、dependency-field ownership、public-contract stability。

- [x] **Step 2：澄清 nullability、validation 与 collection contract**

application-facing absence 保留 `Optional`，framework DAO result 在需要处显式允许 nullable，
禁止 `Optional` parameter 与 field，
定义 immutable empty collection，并将 validation/normalization 分配到正确
boundary。

- [x] **Step 3：添加 behavior 与 lifecycle contract**

定义 query/command 语义、idempotency、lifecycle state、resource cleanup、
thread-safety 文档、event/callback ownership，使用当前 plugin、
event、session、resource 模式。

- [x] **Step 4：验证 API 文档**

运行：

```bash
rg -n '^## ' standards/api-design.md
git diff --check -- standards/api-design.md
```

预期：contract、nullability、validation、lifecycle、compatibility
章节存在且无 whitespace 错误。

### Task 5：对齐 domain initialization 工作流

**文件：**
- Modify: `standards/domain-module-initialization.md`

- [x] **Step 1：在第一个 gate 添加 terminology 与 naming 决策**

创建源码 type 前要求 domain vocabulary、ownership qualifier、stable-key terminology 与
planned type-role suffix。

- [x] **Step 2：通过交叉引用对齐后续 stage gate**

检查 entity、request、view、converter、endpoint、operator、service、
test 时引用权威 naming、style、comment、API 规则。保留既有 compile 与 full-verification 命令。

- [x] **Step 3：验证 workflow 文档**

运行：

```bash
rg -n 'naming.md|code-style.md|code-comments.md|api-design.md' standards/domain-module-initialization.md
git diff --check -- standards/domain-module-initialization.md
```

预期：四个权威 standard 均被引用，无 whitespace
错误。

### Task 6：跨文件一致性与范围验证

**文件：**
- Verify: `standards/naming.md`
- Verify: `standards/code-style.md`
- Verify: `standards/code-comments.md`
- Verify: `standards/api-design.md`
- Verify: `standards/domain-module-initialization.md`
- Verify unchanged: `standards/module-skills.md`

- [x] **Step 1：检查禁止 placeholder 与 stale 矛盾**

运行 focused search：placeholder 语言、冲突 Lombok requirement、
ambiguous `Optional` requirement、不一致 `VO`/`Vo` 拼写。

- [x] **Step 2：检查范围与格式**

运行：

```bash
git diff --check
git status --short
git diff --stat
git diff -- standards
```

预期：本 task 仅涉及五个 approved standards file 加 design/plan tracking；
无关 Java 变更保持不变。

- [x] **Step 3：记录验证结果**

确认无 Java 源码变更，故不触发仓库 mandatory
`mvn clean compile` gate。若本 task diff 意外含 Java 源码，停止并在完成前分离。
