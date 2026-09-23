# Plugin YAML Java Binding 文档实施计划

> 本计划已由拆分后的 `innospots-nexus-core/docs/plugin-dsl-spec.md` 和
> `plugin-v1-implementation-plan.md` 收敛，保留为历史执行记录，不再作为现行实施依据。

> **面向 agent 工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 修订统一 plugin 设计，使 YAML 通过 `bind.kind: java` 与显式 implementation class 声明 JVM 进程内实现，并为每种 bind kind 提供完整规则与示例。

**架构：** Java plugin 仍通过 `CapabilityProviderFactory` 绑定。在 catalog 发布前，将 YAML 的 `java`、`http`、`process`、`mcp`、`contract` binding 编译为同一 capability contribution 模型。用 `providerId` 标识多个 implementation，从 `type@majorVersion` 解析 Java API contract，并保留 tags 供 runtime routing 使用。

**技术栈：** Markdown 设计文档、YAML 示例、Java API 伪代码

---

### Task 1：规范化 binding 术语

**文件：**
- 修改：`innospots-nexus-core/docs/plugin-extension-design.md`

- [x] 将「YAML/JVM 禁止 class 名」的表述替换为 scoped 规则：仅 `bind.kind: java` 可接受 `class`。
- [x] 将 YAML bind kind 中的 `inprocess` 替换为 `java`；Java Factory 仍作为 Java 声明面。
- [x] 说明 YAML class 显式加载，不依赖 SPI、annotation scanning 或 package scanning。

### Task 2：文档化所有 bind kind

**文件：**
- 修改：`innospots-nexus-core/docs/plugin-extension-design.md`

- [x] 添加公共字段表，覆盖 `type`、`majorVersion`、`providerId`、`tags`、`bind`、`exposures`。
- [x] 为 `java`、`http`、`process`、`mcp`、`contract` 添加必填字段、执行、校验与示例章节。
- [x] 说明 `bind.kind` 选择 implementation adapter，而 `exposures[].kind` 发布外部入口。

### Task 3：对齐多 provider 身份与路由

**文件：**
- 修改：`innospots-nexus-core/docs/plugin-extension-design.md`

- [x] 将 provider 身份定义为 `(pluginId, CapabilityKey, providerId)`。
- [x] 允许同一 plugin 在 `providerId` 不同时为同一 `CapabilityKey` 声明多个 provider。
- [x] `providerId` 用于 identity/configuration/diagnostics，tags 用于 runtime selection。
- [x] 一致更新 configuration namespace、校验要求、迁移说明与验收标准。

### Task 4：验证文档一致性

**文件：**
- 验证：`innospots-nexus-core/docs/plugin-extension-design.md`

- [x] 搜索过时表述：YAML 永不含 class、`inprocess` 是 YAML kind、重复 `CapabilityKey` 永远禁止。
- [x] 运行 `git diff --check` 并检查最终 diff；未改 Java 源码，无需 Java 编译。
