# 异常与状态码规范实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 新增连贯、基于代码的 exception、platform 与
domain status code 及安全 status-code extension 规范。

**架构：** 创建单一权威 `exception-status-code.md` 文档，
然后 API 与 domain-initialization 文档通过链接保持简洁并
添加本地 gate。保留当前 module boundary 与既有 Java
behavior；本变更仅涉及文档。

**技术栈：** Markdown、Java 25 项目约定、`NexusException`、
`StatusCode`、`NexusStatusCode`、`PluginStatusCode`、`StatusCodeRules`、
`R<T>`。

**规格：** `docs/superpowers/specs/2026-08-28-exception-status-code-design.md`

## 全局约束

- 预期 application、domain 与 translated
  infrastructure failure 使用 `NexusException`。
- 优先 typed `StatusCode` overload；raw code string 为 interop boundary。
- 翻译 lower-level failure 时保留 original cause。
- full status code 使用 `MODULE(3) + CATEGORY(2) + LOCAL(4)`，共九
  字符，例如 `AIO080002`。
- 平台级 code 归属 `NexusStatusCode`。
- domain-specific code 归属 owning domain 的 `domain.enums` package；
  technical module code 留在 technical boundary。
- 不为每个 status code 创建 exception 子类。
- 不修改 Java 源码或 `standards/module-skills.md`。

---

### Task 1：编写权威 exception 与 status-code 规范

**文件：**
- Create: `standards/exception-status-code.md`

**接口：**
- 消费：`NexusException`、`StatusCode`、
  `StatusCategory`、`StatusCodeRules`、`NexusStatusCode`、`PluginStatusCode`、
  `R<T>` 的当前 behavior。
- 产出：API 与 domain initialization 文档链接的 canonical 规则。

- [x] **Step 1：添加 exception taxonomy 与 construction 规则**

文档化 expected business failure、translated infrastructure failure、pure
utility programmer misuse、interruption/cancellation 与 fatal error。覆盖
`NexusException.build(StatusCode, ...)`、typed 与 raw-code overload、
message/display 分离、cause preservation 与 sensitive-data 处理。

- [x] **Step 2：添加 catch、translation 与 response 规则**

定义何时 unchanged rethrow、何时 wrap 为更 specific status、如何
preserve cause、unknown failure 在何处变为 generic system status、
如何避免 duplicate logging、endpoint infrastructure 如何将
`NexusException` 映射为 `R.fail(...)` 而不泄漏 stack trace。

- [x] **Step 3：添加 status-code 结构与 semantic 规则**

文档化 module allocation、category selection、四位 local code、
英文/中文 message 与 advice、HTTP mapping、enum constant naming、
uniqueness 与 full code 兼容性。显式区分 business
status 与 transport HTTP status。

- [x] **Step 4：添加 extension 流程与测试**

描述 reuse search、ownership 决策、module/local allocation、metadata、
raw string 的 registration/allowlist concern，以及 format、uniqueness、message、category、HTTP status、behavior 的 required contract test。

- [x] **Step 5：添加 review checklist 并验证文档**

运行：

```bash
rg -n '^## ' standards/exception-status-code.md
git diff --check -- standards/exception-status-code.md
```

预期：exception、status、extension、compatibility、checklist
章节齐全且无 whitespace 错误。

### Task 2：使 API design 与权威 error 规范对齐

**文件：**
- Modify: `standards/api-design.md`

**接口：**
- 消费：`standards/exception-status-code.md`。
- 产出：简洁 API-specific 链接与 boundary 规则。

- [x] **Step 1：用交叉引用替换重复的 exception 指导**

保留 API 层 rule：business failure 使用 `NexusException`，然后
链接权威文档的 taxonomy、wrapping 与 response
mapping。

- [x] **Step 2：澄清 pure utility precondition 与 status selection**

说明 JDK/framework precondition exception 仅允许用于不表示 caller 或 business input 的 pure
programmer misuse。application-visible failure 需要 typed reusable status code，禁止 ordinary in-repo call 使用 raw
string code。

- [x] **Step 3：验证 API 文档**

运行：

```bash
rg -n 'exception-status-code.md|NexusException|StatusCode' standards/api-design.md
git diff --check -- standards/api-design.md
```

预期：API 文档指向权威规范且无矛盾 exception rule。

### Task 3：为 domain initialization 添加 status-code gate

**文件：**
- Modify: `standards/domain-module-initialization.md`

**接口：**
- 消费：`standards/exception-status-code.md` 与既有 stage gate。
- 产出：domain failure 的 pre-creation 与 verification 检查。

- [x] **Step 1：在 domain vocabulary gate 添加 status ownership**

添加 status enum 前决定 failure 是 platform-wide、domain-specific 还是
technical，并要求 name 遵循 domain
与 status-code 约定。

- [x] **Step 2：在 domain contract gate 添加 status-code 检查**

要求 reuse search、唯一 module/local allocation、category 与 HTTP mapping、
双语 message/advice、text 中无 runtime secret、无 per-error exception
subclass。

- [x] **Step 3：在 verification gate 添加 extension 测试**

要求 full-code shape、uniqueness、metadata、exception
translation behavior 的 contract test；保留既有 compile 与 full-test 命令。

- [x] **Step 4：验证 workflow 文档**

运行：

```bash
rg -n 'exception-status-code.md|status code|StatusCode|NexusException' standards/domain-module-initialization.md
git diff --check -- standards/domain-module-initialization.md
```

预期：相关 stage 可见 status ownership 与 extension 检查，diff check 干净。

### Task 4：跨文件 review 与验证

**文件：**
- Verify: `standards/exception-status-code.md`
- Verify: `standards/api-design.md`
- Verify: `standards/domain-module-initialization.md`
- Verify unchanged: `standards/module-skills.md`

- [x] **Step 1：检查一致性与实现对齐**

搜索 conflicting code format、`VO`/`Vo` 风格 status naming、raw-code
recommendation、uncaught-cause 措辞、违反 `AGENTS.md` 的 module placement。

- [x] **Step 2：运行仓库验证**

运行：

```bash
git diff --check
mvn validate
mvn test
mvn -q help:effective-pom
git status --short
```

预期：Java 25 上 Maven 成功、测试零失败、
commit 后 working tree 干净，且仅 intended 文档变更。

- [x] **Step 3：确认源码范围**

运行：

```bash
git diff --name-only HEAD~4..HEAD
```

预期：task 最终变更集中无 Java 源码与 `standards/module-skills.md`。
