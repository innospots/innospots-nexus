# innospots-nexus-sample：工程结构、DDD 与契约边界

> **权威范围：** 本仓库内 `innospots-nexus-sample` 聚合器及其子模块（可运行示例 + 无框架扩展库）。
> **上位约束：** 平台库分层仍以根 [`AGENTS.md`](../../../../AGENTS.md) 与
> [module-layout.md](../../java-project/references/module-layout.md) 为准；本文描述 **在平台之上的示例/扩展层**。
>
> **关联技能：** `java:project`（reactor/POM）· `java:design`（交付面与路径契约）·
> `java:develop`（包树实现与测试）· `java:reference`（本文件为专题索引入口）。

---

## 1. 定位

`innospots-nexus-sample` 是 **不参与制品发布**（`maven.deploy.skip=true`）的示例工程，用于：

| 目的 | 说明 |
|------|------|
| 演示可运行组装 | Spring / Quarkus 下 portal、platform、app 的启动与打包 |
| 演示平台扩展 | 在 `innospots-nexus-platform` 之上增加业务，且 **扩展 JAR 与 Spring/Quarkus 解耦** |
| 契约与测试样板 | JAX-RS 契约测试、OpenAPI、控制台 Web 集成测试等 |

**不是**外部产品工程模板；仓库外产品见 [external-project-layout.md](../../java-project/references/external-project-layout.md)。

### 1.1 全量示例 vs 按需创建（必读）

| | `innospots-nexus-sample`（本仓库） | 新建工程 / 新扩展 / 外部产品 |
|---|-----------------------------------|------------------------------|
| **目的** | **陈列** Nexus 支持的各类可运行形态与扩展模式，供对照与回归 | **只实现当前产品需要的模块** |
| **模块数量** | reactor 内 **同时存在** app、portal、platform 的 Spring **与** Quarkus 等子模块 | **禁止**默认复制 sample 的全套 `<modules>` |
| **运行框架** | Spring 与 Quarkus **各有一套** 示例，便于两边维护契约 | **二选一**（须先问清）；未要求则只建一套 |
| **扩展库** | 存在 `innospots-nexus-sample-platform` 演示无框架扩展 | 仅当需要在 platform 之上加 **自有业务** 且无框架 JAR 时才建同类库；否则直接在 platform/产品模块加领域包 |
| **交付面** | `core` / `console` / `inbound` 均有示例领域 | 按产品形态 **裁剪**：仅管理台则不必建 `inbound`；仅对外 API 则不必建 `console` 包（仍须有 `core`） |

**原则：** 把 sample 当作 **模块类型与包结构的参考书**，不是 greenfield 的复制清单。
先定 **产品形态**（仅 portal / 仅 platform / 仅 app / 组合）、**运行框架**、**是否要无框架扩展库**，再对照下文 §2.2 选型表 **只创建对应 Maven 子模块**。

外部产品 greenfield 流程仍以 [project-deliverables.md](../../java-project/references/project-deliverables.md) § 外部产品工程为准；
sample 子模块命名仅作 **本仓库内** 对照，不要求产品工程出现 `innospots-nexus-sample-*` 前缀。

---

## 2. Maven 模块树（reactor）

以下为 **当前 sample 聚合器内的全量 reactor**（便于 CI 与文档对照），**不是**每个新工程必须注册的列表。

```text
innospots-nexus-sample/                    packaging=pom，parent=innospots-nexus-parent
├── innospots-nexus-sample-platform        无框架扩展库（依赖 innospots-nexus-platform）
├── innospots-nexus-sample-spring-app      可运行：spring-app + H2
├── innospots-nexus-sample-spring-portal   可运行：spring-portal + 组装/发行包
├── innospots-nexus-sample-spring-platform 可运行：spring-platform + 组装/发行包
├── innospots-nexus-sample-quarkus-app
├── innospots-nexus-sample-quarkus-portal
├── innospots-nexus-sample-quarkus-platform
├── assets/                                发行脚本、assembly 描述符
└── dist/                                  组装输出（gitignore）
```

### 2.1 模块类型与依赖（硬性）

| 模块 artifact | 类型 | 直接依赖（典型） | 禁止 |
|---------------|------|------------------|------|
| `innospots-nexus-sample-platform` | 扩展库 | `innospots-nexus-platform` | Spring、Quarkus、Servlet；`innospots-nexus-portal` |
| `innospots-nexus-sample-spring-platform` | 可运行 | `innospots-nexus-spring-platform`；可选 `sample-platform` | 在 POM 中绕过 BOM 写 version |
| `innospots-nexus-sample-spring-portal` | 可运行 | `innospots-nexus-spring-portal` | 依赖 `platform` 库模块（portal/platform 不互依） |
| `innospots-nexus-sample-spring-app` | 可运行 | `innospots-nexus-spring-app` | 同上 |
| `*-quarkus-*` | 可运行 | 对称的 `innospots-nexus-quarkus-*` | 与 spring 模块混在一个 JAR |

**扩展库注册顺序（仅当 reactor 内同时存在扩展库与可运行模块时）：** 将
`innospots-nexus-sample-platform` 置于各 `*-spring-*` / `*-quarkus-*` 之前，便于 `-am` 先编译扩展再装配。

### 2.2 按需选型：需要哪些 Maven 子模块

在 **本仓库** 增删 sample 子模块、或 **参考 sample 新建** 外部/内部工程时，按行勾选，**未命中行则不建该模块**。

| 你需要 | 创建（Spring 示例名） | 创建（Quarkus 示例名） | 不必创建 |
|--------|----------------------|------------------------|----------|
| 可运行 **插件宿主 / service 框架**（`spring-app`） | `sample-spring-app` | `sample-quarkus-app` | portal、platform、sample-platform |
| 可运行 **租户管理端**（portal） | `sample-spring-portal` | `sample-quarkus-portal` | platform、app（除非产品同时要） |
| 可运行 **运营平台**（platform） | `sample-spring-platform` | `sample-quarkus-platform` | portal、app（除非产品同时要） |
| 在 platform 上增加 **无框架业务 JAR** | `sample-platform` + **一个** 与之匹配的运行模块（通常 spring/quarkus-platform） | 同上 | 未使用的另一套框架下的对称模块 |
| 仅改平台库、不跑进程 | 不建 sample 可运行模块 | — | 全部 `sample-*-app/portal/platform` |
| 同时要 portal 与 platform 进程 | 两个可运行模块 + **application 组装**（产品侧常见一个 app 模块依赖两者；sample 为演示拆成两个进程） | 对称 Quarkus | 不要让 portal 与 platform **库** Maven 互依 |

**框架：** 用户/产品已选定 Spring **或** Quarkus 时，**只维护所选栈**下的 `sample-*` 子模块；不要为了对齐 sample 仓库而去建另一套。

**在已有 reactor 中增模块：** 只向 `<modules>` **追加当前任务需要的一项**，不要一次性补全与 sample 主分支相同的六元组。

### 2.3 包根命名

| 模块 | Java 包根 |
|------|-----------|
| `innospots-nexus-sample-platform` | `com.innospots.nexus.sample.platform` |
| `innospots-nexus-sample-spring-platform` | `com.innospots.nexus.sample.spring.platform` |
| `innospots-nexus-sample-spring-portal` | `com.innospots.nexus.sample.spring.portal` |
| `innospots-nexus-sample-quarkus-platform` | `com.innospots.nexus.sample.quarkus.platform` |
| … | `com.innospots.nexus.sample.quarkus.*` |

运行模块 **只放** 主类、框架配置、测试支撑；**业务领域** 放在对应的 **无框架扩展库**（如 `sample-platform`）。

### 2.4 发行与资源

- 可运行子模块使用父 POM `pluginManagement` 中的 **maven-assembly-plugin**，输出到 `innospots-nexus-sample/dist/`。
- 父 POM **resources 过滤**：`*.yaml` / `*.properties` / `*.xml` 等打进发行包 `config/`，thin jar 保留 schema 等 classpath 资源（见 `innospots-nexus-sample/pom.xml` 注释）。

---

## 3. 扩展库：交付面 × 领域（DDD 边界）

`innospots-nexus-sample-platform` 在 **单个 Maven 模块** 内用 **第一级交付面、第二级领域、第三级职责** 组织代码。
这与平台库 `com.innospots.nexus.platform.<domain>.<responsibility>`（领域在第一级）**并存**：
扩展库多一刀交付面，避免与平台内置领域包名冲突，且区分管理台与对外 API。

### 3.1 三层结构

```text
交付面（console | core | inbound）   ← 按产品裁剪：不是每个领域都需要三个面
  └── 业务领域（announcement | support | …）
        └── 功能子模块（assignment | publish | …，按需）
              └── 职责包（endpoint | service | dao | domain/… | loader）
```

**交付面按需：** `core` 为领域真源，**几乎总是需要**；`console` 仅在有管理台 HTTP 时建；
`inbound` 仅在有对外 API 时建。sample 源码里两个领域三面齐全，仅为 **演示完整模式**。

### 3.2 各交付面职责

| 交付面 | 包前缀 | 职责 | 典型类型 |
|--------|--------|------|----------|
| **core** | `…sample.platform.core.<domain>` | 领域真源：持久化、编排、启动加载 | `*Entity`、`*Dao`、`*Operator`、`*Service`、`loader/*Loader` |
| **console** | `…sample.platform.console.<domain>` | 管理台 HTTP 边界 + 管理台编排 | `Console*Endpoint`（interface）、`Console*Service` |
| **inbound** | `…sample.platform.inbound.<domain>` | 对外 API 边界 + 对外编排 | `Inbound*Endpoint`、`Inbound*Service`、按需 `inbound.<domain>.<function>` |

**调用方向（不可违反）：**

```text
console.<domain>.endpoint → console.<domain>.service → core.<domain>.service → core.<domain>.operator → core.<domain>.dao
inbound.<domain>.endpoint   → inbound.<domain>.service   → core.<domain>.service → …
core.<domain>.loader        → 仅调用 core 内 service/operator；由 NexusStartupTask 在运行模块触发
```

- `console` / `inbound` 的 `service` **必须薄**：鉴权语义、路径专属校验、组合调用；重复逻辑下沉 `core.*.service`。
- **禁止** `inbound` 直接依赖 `console` 包（共享只通过 `core`）。
- **禁止** 在 `core` 下放 Jakarta REST 端点（端点只属于 `console` / `inbound`）。

### 3.3 领域内职责（与平台一致）

在 `core.<domain>` 或各交付面的 `<domain>` 下，仍遵守
[package-structure.md](package-structure.md) 的职责包含义：`domain/entity|request|vo|enums`、
`dao`、`operator`、`service`；单包 ≤15 个 `.java`；大领域用功能子模块（示例：`core.support.assignment`）。

### 3.4 参考包树（两个领域）

**公告 `announcement`：**

```text
com.innospots.nexus.sample.platform
├── SamplePlatformModule.java
├── core.announcement.{dao,domain,operator,service,loader}
├── console.announcement.{endpoint,service}
└── inbound.announcement.{endpoint,service}
```

**支持 `support`（含功能子模块 `assignment`）：**

```text
core.support.{dao,domain,operator,service}
core.support.assignment.service
console.support.{endpoint,service}
inbound.support.{endpoint,service}
```

源码对照：`innospots-nexus-sample/innospots-nexus-sample-platform/src/main/java/`.

---

## 4. HTTP 与 API 契约

### 4.1 路径前缀（扩展专用）

与平台内置 `/platform/tenants` 等区分，sample 扩展统一加 **`/sample`** 段：

| 交付面 | `@Path` 前缀模式 | 示例 |
|--------|------------------|------|
| console | `/platform/console/sample/<resource>` | `/platform/console/sample/announcements` |
| inbound | `/platform/api/sample/<resource>` | `/platform/api/sample/announcements` |

新资源须在此命名空间下登记，**不得**占用未前缀的 `/platform/*` 路径以免与 `innospots-nexus-platform` 冲突。

### 4.2 端点形态

- 扩展库内：**Jakarta REST interface**（`*Endpoint`），返回 `R<T>`；方法契约测试见 `TenantEndpointContractsTest` 模式。
- **实现类**可放在扩展库（仍无 Spring）或由 `sample-spring-platform` / `quarkus-platform` 注册 Bean；**interface 与路径契约须留在无框架 JAR**。
- 其余 REST 规则见 [api-contract.md](api-contract.md) 与 [quick-constraints.md](quick-constraints.md)。

### 4.3 持久化

- 表名建议前缀 `nx_sample_`（与平台 `nx_tenant` 区分）。
- Schema 脚本放在 **可运行 sample 模块** 的 `src/main/resources/nexus/schema/`（或项目约定目录），**不由** `SamplePlatformModule` 静态建表。
- Dao 扫描包：`com.innospots.nexus.sample.platform.core.**.dao`（在 Spring/Quarkus 适配层配置）。

---

## 5. 模块标记与装配

| 类型 | 类 | 职责 |
|------|-----|------|
| 扩展库 marker | `SamplePlatformModule` | `final`、私有构造；Javadoc 说明依赖与交付面；**不**承担 DI/静态 init |
| 平台 marker（对照） | `PlatformModule` | 运维域库边界标记 |

**启动加载：** `core.*.loader` 或实现 `NexusStartupTask` 的类放在扩展库；**注册与执行**在
`innospots-nexus-sample-spring-platform` / `quarkus-platform`（或未来 `spring-sample-platform` 适配模块）。

**显式启用：** 运行模块应 **显式** 依赖 `innospots-nexus-sample-platform` 并装配端点/Dao/Startup；
不得假设扫描整个仓库。平台侧使用 `@EnableNexusPlatform`；sample 扩展宜单独 `@Import` / 等价开关（待产品命名）。

---

## 6. 与平台库 DDD 的关系（边界标准）

| 维度 | `innospots-nexus-platform` | `innospots-nexus-sample-platform` |
|------|------------------------------|-----------------------------------|
| 第一级包段 | 领域名 `tenant`、`user` | 交付面 `core` / `console` / `inbound` |
| 第二级包段 | 职责 `endpoint`、`dao` | 领域名 `announcement`、`support` |
| Maven 归属 | 平台运维域产品能力 | 示例/团队自定义扩展，**不**并入 platform JAR |
| 依赖 | `console` → 不得 `portal` | `platform` → 不得 `portal` |
| 何时升仓 | 多租户/平台 IAM 等 **产品级** 能力 | 仅示例或明确 experimental；成熟能力应回迁 `platform` 或外部产品模块 |

**判定口诀：** 能力若属于 AGENTS.md 中 **platform 模块职责**（租户生命周期、企业主体、平台用户等），
不得长期留在 `sample-platform`；`sample-platform` 用于 **演示扩展模式** 或 **尚未定型的运营域增量**。

---

## 7. 测试与边界守护

| 测试类型 | 位置 | 用途 |
|----------|------|------|
| `SamplePlatformModuleTest` | `sample-platform` | marker 可加载、包根正确 |
| `*EndpointContractsTest` | 扩展库 `src/test` | `@Path`、HTTP 方法、入参类型 |
| `SamplePlatformModuleBoundaryTest`（推荐） | 扩展库 | classpath 无 `portal`、无 Spring 配置类 |
| JAX-RS / OpenAPI 集成测试 | `sample-spring-platform` | 运行时过滤器、OpenAPI 聚合 |

---

## 8. 自检清单（design / develop / project 共用）

### 8.1 工程（`java:project`）

- [ ] 已用 §2.2 判定 **最小模块集**；**未**机械复制 sample 全量 reactor
- [ ] 运行框架为 Spring **或** Quarkus（未要求则未建两套）
- [ ] 新子模块 parent 为 `innospots-nexus-sample`（仅本仓库 sample 子模块），不写 JAR `<version>`
- [ ] 若存在扩展库：仅声明 `innospots-nexus-platform`（最小依赖）
- [ ] 若 reactor 内兼有扩展库与可运行模块：扩展库排在运行模块之前
- [ ] 未将 sample 模块加入 BOM 发布面（除非明确改为可发布产品）

### 8.2 设计（`java:design`）

- [ ] 已判定能力属于 platform 产品域还是 sample 扩展域（§6）
- [ ] 已选定交付面（console / core / inbound）与领域名
- [ ] HTTP 路径落在 `/platform/console/sample/**` 或 `/platform/api/sample/**`
- [ ] 分层调用符合 §3.2；无 inbound→console 依赖
- [ ] 与 `innospots-nexus-platform` 路径、表名无冲突

### 8.3 实现（`java:develop`）

- [ ] `core` 含 entity/dao/operator；端点仅在 console/inbound
- [ ] request/vo 为 record；失败为 `NexusException` + `NexusStatusCode`
- [ ] 每批改动后 `mvn -pl innospots-nexus-sample/innospots-nexus-sample-platform -am clean compile`
- [ ] 契约测试与边界测试已按 §7 补齐

---

## 9. 相关文档

- [package-structure.md](package-structure.md) — 领域内职责包（与平台 portal/platform 对齐）
- [module-ownership.md](module-ownership.md) — 平台库归属；§ sample 扩展归属
- [module-layout.md](../../java-project/references/module-layout.md) — 全仓库模块树（含 sample 摘要）
- [sample-extension-design.md](../../java-design/references/sample-extension-design.md) — 设计四步法补充
- [sample-extension-development.md](../../java-develop/references/sample-extension-development.md) — 实施步骤补充
