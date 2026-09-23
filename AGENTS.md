# Agent 操作指南

本仓库为 greenfield 重建工程。旧 Innospots 项目仅作参考，不是源码模板。

## 核心约束

- 不得将 legacy 源码复制到本仓库。
- 不得将 legacy 文件移动到本仓库。
- 不得机械复刻 legacy POM 或包结构。
- 创建新行为前，应询问或推断当前开发者意图。
- 保持 foundation 轻量、依赖最小。
- **代码生成完成后必须编译验证** — 每次修改完 Java 源文件后，立即运行 `mvn clean compile` 确保没有不可编译的代码。禁止生成不能编译的通代码。
- 普通代码开发过程中，不得创建、更新或同步
  `skills/java/java-reference/references/modules/` 下的模块 API 参考文档。
- 模块 API 参考文档仅在开发者明确要求模块或项目目录扫描时生成或刷新。
  该操作必须整体更新所选文档集，而不是随单个代码变更增量更新。

## Agent 工作流

### grill-me（方案压力测试，优先）

在编写代码、修改 POM、定架构或注册新模块**之前**，若出现以下任一情况，**必须先**
使用 **`grill-me`** 做方案压力测试（完整说明见
[`skills/java/java-reference/references/grill-me.md`](skills/java/java-reference/references/grill-me.md)）：

| 情形 | 说明 |
|------|------|
| **通用、宽泛、不具体** | 需求边界、归属模块、术语、兼容面尚未清晰；存在多种理解 |
| **新需求** | 首次提出的业务能力、产品行为或平台能力 |
| **新设计** | 新业务域、跨模块协作、重大契约或存储变更 |
| **新建工程 / Maven 模块** | 调整 reactor 拓扑、改变依赖方向、拆分/合并模块 |

未安装 `grill-me` 时**不得**代行上述决策，须先执行：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

按生成技能的完整说明执行；会话结束且开发者确认一致后，方可进入对应的 `java:*` 技能。

**可跳过 grill-me 的典型场景**（须规范与契约已清晰）：单字段增补、已确认契约的局部修补、
纯 `${revision}` 对齐、单模块插件微调。详见 grill-me.md「何时不调用」。

### Java 技能（按需优先选用）

涉及本仓库 Java 工作时，**不要凭印象即兴发挥**。先判断场景，**优先读取并遵循**
`.cursor/skills/` 下对应的 **`java:*` 技能**（读其 `SKILL.md` 全文后再动手）：

| 场景 | 技能 | 何时用 |
|------|------|--------|
| 查规范、问约定、不确定走哪条路 | `java:reference` | 只读、不产出代码；规范路由中枢 |
| 新建/调整 Maven 模块、POM、构建 | `java:project` | 动**工程骨架**；须对照下文模块职责与依赖规则 |
| 架构/模块/接口/契约/技术方案设计 | `java:design` | 动**设计决策**；须对照下文模块边界 |
| 写功能、改功能、修 Bug、重构、单测 | `java:develop` | 动**实现与配套测试** |
| 编译、跑测试、规范/质量/依赖/安全检查 | `java:check` | **验证**动作 |
| Spring Boot 组装、配置与迁移 | `java:spring` | 涉及 **Spring 生态** |
| 工程 `${revision}` 升版、发版 | `java:project-upgrade` | 改变**自身产物版本号** |
| JDK、第三方依赖、框架升级 | `java:dependency-upgrade` | 改变**外部技术栈** |

**典型链路**（按需省略中间环节）：

```text
grill-me  →  java:project（需新建/改模块时）  →  java:design  →  java:develop  →  java:check
```

- 新建 Maven 模块、新设计：**grill-me 为必经**（见上表）。
- 规范条文以 `skills/java/java-reference/standards/` 为准；各技能不得重复复述规范。
- 与本文件冲突时，以**本文件为上位约束**（裁决顺序见 `java:reference`）。
- 新建工程/模块或修订 Agent 指南时，按
  [`skills/java/java-reference/references/agents-template.md`](skills/java/java-reference/references/agents-template.md)
  生成或增补；合入前由 `java:check` 对照本文件做 AGENTS 合规检查。

## 模块职责

### `innospots-nexus-base`

- 共享依赖、契约、原语与 utility 包的纯 Java foundation。
- 提供可复用能力：异常、状态码、响应包装、领域事件契约、MapStruct 支持、
  JSON 工具、ID 生成、密码学、HTTP 工具、condition DSL、execution SPI、
  进程内事件及其他轻依赖工具。
- 拥有 **transport/session snapshots**（`UserSnapshot`、`TenantSnapshot`、
  `OrganizationSnapshot`、`WorkspaceSnapshot`、`ProjectSnapshot` 等）
  作为共享可序列化形状；kernel 拥有业务实体与工作流。
- 作用域层级：**Tenant → Workspace（共享资源）→ Project（业务隔离）**。
  `OrganizationSnapshot` 是租户业务 profile，不是 kernel 的 `OrganizationUnit`。
- 不得包含业务域逻辑或持久化绑定。
- 必须保持 middleware-free，不得依赖 database、messaging、
  scheduling、Servlet、Spring、Quarkus 或其他 runtime 基础设施。
- 模块 API 参考位于
  `skills/java/java-reference/references/modules/<artifact-id>/README.md`
  （仅索引 — 不是 `SKILL.md`；不在 `src/main/resources/skills/` 下）。
  当前索引：`innospots-nexus-base`、`innospots-nexus-core`。
- **保留但尚无消费者：** `domain.condition`（filter DSL）、
  `execution`（executor SPI）。未经明确边界决策不得删除；
  扩展前需接入真实消费者或标记为 experimental。
- **base 中的新 public API** 至少需要两个上层模块消费者，
  除非明确标记为 experimental 或 reserved。
- **禁止出现在 base**（归属 core、adapter 或业务模块）：
  - Cache（本地或分布式）
  - Retry / circuit breaker
  - Jakarta Bean Validation
  - Scheduling runtime（Quartz/cron 逻辑；scheduling enum 可放在 core）
  - Messaging middleware（Kafka 等）
  - ORM / JDBC / connection pools
  - Spring / Servlet bindings
  - 业务域实体与服务工作流

### `innospots-nexus-service`

- 聚合八个 framework-neutral 库：contract、runtime、http、
  websocket、stream、transfer、observability、governance。
- contract 依赖 base；runtime 依赖 contract；protocol/transfer
  模块依赖 runtime；observability 与 governance 依赖 contract。
- runtime 消费扩展契约，但不依赖 observability 或
  governance 实现。protocol 模块之间不得相互依赖。
- 不得依赖 Spring、Quarkus、Servlet、Reactor、Mutiny、persistence、
  或 console/kernel/platform 业务模块。
- 复用 base snapshots、status/exception 契约与 ResourceStore。
  core 文件元数据集成属于 assembly 边界。
- 拥有技术 audit events/output 集成，而非业务 audit 存储
  或查询（仍归 kernel/platform）。
- Spring 与 Quarkus bindings 归属各自 framework aggregator 下的
  innospots-nexus-spring-service、innospots-nexus-spring-core 与 innospots-nexus-quarkus-service。
- 中立库直接继承 innospots-nexus-parent；service POM
  是 aggregator，不是它们的 build parent。

### `innospots-nexus-spring-core`

- Spring Boot 侧 core 基础设施组装（当前含 i18n 桥接）。
- 通过 Spring `MessageSource` / ResourceBundle 实现 base 的 `I18nMessageResolver`，
  并在启动时注册到 `I18nConverter`；由 `@EnableNexusI18n` 或 bootstrap 注解显式 `@Import` 引入。
- 依赖 `innospots-nexus-base`；可装配 Web locale 同步 Filter。
- 不得引入 kernel/console/platform 业务模块。

### `innospots-nexus-core`

- 在 `innospots-nexus-base` 之上扩展业务中立中间件、database
  与平台基础设施支持。
- 拥有共享持久化基类 `BaseEntity`、audit fill、ID 生成、
  Quartz scheduling、service-node registry、watcher runtime、startup SPI、
  以及按 {@code OwnershipEntity} 隔离的文件元数据（`nx_meta_resource` + `MetaResourceService`）。
- 必须保持业务域中立。User、role、permission、menu、catalog
  index 等 management-console 关注点不属于本模块。
- 可依赖可复用平台支持所需的 middleware API 与实现，
  但不得绑定 Spring Boot auto-configuration。
- **禁止出现在 core**（归属 plugin、console、kernel、platform 或
  adapter）：
  - Classpath plugin runtime、contribution decoders、Page DSL、plugin
    installation tables
  - Jakarta REST endpoints 与 console VO
  - User/role/permission/menu/dictionary 业务实体与工作流
  - Auth/session conversation 或 chat 产品域
  - Spring / Quarkus / Servlet bindings
- **core 中的新 public API** 至少需要两个上层模块消费者，
  除非明确标记为 experimental。
- 二进制存储 SPI 位于 `base.resources.ResourceStore`；core 通过
  `core.resource.storage.ResourceStorageRegistry` 将 store 绑定到持久化元数据。

### `innospots-nexus-plugin`

- 在 `innospots-nexus-core` 之上扩展 classpath plugin runtime 与 contribution
  处理。
- 拥有 plugin discovery、declaration、lifecycle、installation、capability
  routing、contribution decode/validate/snapshot，以及
  `core.plugin.contribution.console.ui.spec` 下的 **Pactor Page DSL 1.0**。
- 拥有 `console@1` contribution 契约与 runtime handler；**不**拥有
  持久化 console catalog 索引（`nx_console_catalog_resource` — 归属
  `innospots-nexus-console`）。
- 必须保持业务域中立且 middleware-binding-free（无 Spring
  Boot auto-configuration）。
- 包名仍为 `com.innospots.nexus.core.plugin`（兼容既有 import）；
  Maven artifact 为 `innospots-nexus-plugin`。

### `innospots-nexus-console`

- 基于 Core 与 Plugin 构建的 management-console **API surface** 模块。
- 提供 Jakarta REST management endpoints、request/response VO、converter，
  以及持久化 **console catalog index**（`console.catalog.*`）。
- **不得**拥有 plugin specification 或 contribution constraint 定义；
  这些归属 `innospots-nexus-plugin`。
- 不得实现具体 management 业务功能。User、role、
  permission、registration 等 management 功能归属
  `innospots-nexus-kernel` 等业务模块。
- 模块 API 参考位于
  `skills/java/java-reference/references/modules/innospots-nexus-console/`
  （不在 `src/main/resources/skills/` 下）。

### `innospots-nexus-kernel`

- 拥有租户域持久化基类（`TenantBaseEntity` → `TenantWorkspaceBaseEntity` →
  `TenantProjectBaseEntity`）及 workspace/project 作用域端口与实现；platform 不包含 workspace/project 域。
- 基于 console 与 core foundation 构建的核心 Nexus 业务功能模块。
- 拥有 foundational management 能力：authentication、
  registration、users、roles、permissions、menus、dictionaries、audit support
  及其他 baseline 平台功能。
- 业务代码先按 domain 组织，再按职责分包，例如
  `endpoint`、`dao`、`domain`、`converter`、`operator`、`service`、
  `handler`、`interceptor`、`listener`（`kernel.role.endpoint`，而非
  `kernel.endpoint.role`）。大 domain 使用功能子包
  （`permission.authorization`、`grant.service`）；禁止 module 级 `service`
  堆放区；每个包目录最多 15 个 `.java` 文件。见
  `skills/java/java-reference/references/package-structure.md`。
- 必须使用 `base`、`core`、`console` 的共享基础设施与契约，
  而不是重新实现。

### `innospots-nexus-platform`

- 基于 console foundation 构建的 ops-domain 平台，与 kernel 并行。
- 拥有 tenant lifecycle（`nx_tenant`）、enterprise legal profile
  （`nx_enterprise`），以及后续的 platform users、support access、platform
  audit。
- 暴露 `/platform/**` 契约。不得提供公开 self-registration。
- 必须依赖 `console`（及传递的 `core` / `base`）。不得依赖
  `innospots-nexus-kernel`。

## 依赖规则

- `innospots-nexus-base` 必须保持 middleware-free。
- 内部 Java 模块应继承 `innospots-nexus-parent`。
- 依赖版本归属 `innospots-nexus-bom`。
- 共享 Java 模块依赖归属 `innospots-nexus-parent`，不在根 aggregator 或 BOM 中定义。
- `innospots-nexus-core` 可依赖 `innospots-nexus-base`。
- `innospots-nexus-plugin` 可依赖 `innospots-nexus-core` 及
  传递的 base foundation。
- `innospots-nexus-console` 可依赖 `innospots-nexus-core`、
  `innospots-nexus-plugin` 及传递的 base foundation。
- `innospots-nexus-kernel` 可依赖 `innospots-nexus-console`、
  `innospots-nexus-core` 及它们的传递 base foundation。
- `innospots-nexus-platform` 可依赖 `innospots-nexus-console`、
  `innospots-nexus-core` 及它们的传递 base foundation。
- 主依赖方向为
  `innospots-nexus-base -> innospots-nexus-core -> innospots-nexus-plugin ->
  innospots-nexus-console`，然后 `console -> innospots-nexus-kernel` 与
  `console -> innospots-nexus-platform` 并行。Kernel 与 platform 不得
  相互依赖。依赖不得指回更高层。
- `innospots-nexus-core` 可提供具体业务中立 middleware 与
  database 支持，但不得绑定 Spring Boot
  auto-configuration。
- 业务特定基础设施归属其所属业务模块，或
  专用 adapter、plugin、extension、application 模块。

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
| [`skills/java/java-reference/standards/code-style.md`](skills/java/java-reference/standards/code-style.md) | 花括号、缩进、行宽、import 顺序 |
| [`skills/java/java-reference/standards/code-comments.md`](skills/java/java-reference/standards/code-comments.md) | Javadoc 层级（class、method、inline） |
| [`skills/java/java-reference/standards/naming.md`](skills/java/java-reference/standards/naming.md) | Java、包、文件命名约定 |
| [`skills/java/java-reference/standards/api-design.md`](skills/java/java-reference/standards/api-design.md) | 方法签名、不可变性、null 处理、异常 |
| [`skills/java/java-reference/standards/domain-module-initialization.md`](skills/java/java-reference/standards/domain-module-initialization.md) | 业务域初始化的分阶段工作流 |
| [`skills/java/java-reference/standards/module-skills.md`](skills/java/java-reference/standards/module-skills.md) | 模块 API 索引（`README.md`）与 references/ 目录格式 |

## 验证

当本地 JDK 支持配置的 release 时，结构变更后运行：

```bash
mvn validate
mvn test
mvn -q help:effective-pom
```

若本地 JDK 低于 25，应报告环境不匹配，而不是降低项目基线。
