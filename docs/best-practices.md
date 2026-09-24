# 最佳开发实践

本文说明如何在 `innospots-nexus` 框架上进行二次开发，以及如何通过仓库内置的 **Java 技能体系**（`skills/java/`）与 AI 辅助工具协作完成设计、实现与验证。

> 模块职责与依赖红线以根目录 [AGENTS.md](../AGENTS.md) 为准；编码规范以 `skills/java/java-reference/standards/` 为准。

---

## 1. 读者与目标

| 读者 | 目标 |
|------|------|
| 业务功能开发者 | 在 portal / platform 中扩展管理能力与租户侧业务 |
| 插件开发者 | 通过 Capability 与 `console@1` 贡献扩展控制台 |
| 集成与宿主开发者 | 在 Spring / Quarkus 进程中装配框架与插件 |
| 使用 Cursor / AI Agent 的开发者 | 按技能路由完成规范一致的设计与交付 |

**成功标准：** 代码落在正确模块与包结构内，依赖方向不被破坏，测试与 `mvn` 验证通过，设计结论可追溯。

---

## 2. 框架概览

### 2.1 工程定位

本仓库是 **greenfield 重建工程**。旧 Innospots 项目仅作行为参考，**不得**复制遗留源码、POM 或机械复刻包结构。

### 2.2 模块分层与依赖方向

```text
innospots-nexus-base          共享契约、工具、Snapshot（middleware-free）
        ↓
innospots-nexus-core          持久化基类、Quartz、资源元数据、平台基础设施
        ↓
innospots-nexus-plugin        插件运行时、Contribution、Page DSL
        ↓
innospots-nexus-console       管理台 REST 契约、VO、Catalog 索引
        ↓
innospots-nexus-portal        租户侧管理业务（用户、权限、菜单…）
innospots-nexus-platform      平台运维域（租户生命周期、企业主体…）
        ↓（并行，互不依赖）
innospots-nexus-spring / innospots-nexus-quarkus   可运行宿主与适配层
```

**硬性规则：**

- `portal` 与 `platform` **不得相互依赖**。
- `base` 不得引入数据库、Spring、Servlet、消息中间件等运行时基础设施。
- 业务 REST 端点使用 `jakarta.ws.rs`，返回 `R<T>`；事务使用 `jakarta.transaction.Transactional`。

### 2.3 作用域层级

```text
Tenant → Workspace（共享资源）→ Project（业务隔离）
```

持久化实体按作用域继承 `BaseEntity` → `TenantBaseEntity` → `WorkspaceBaseEntity` → `ProjectBaseEntity`。业务代码须明确数据落在哪一层作用域。

### 2.4 包结构：领域优先

业务代码 **先按领域分包，再按职责分包**：

```text
com.innospots.nexus.portal.<domain>/
  ├── endpoint / dao / domain / converter / operator / service（按需）
  └── <subcapability>/   # 单包超过约 15 类时再分子能力包
```

禁止 `endpoint/role`、`dao/role` 这类技术层优先结构；禁止在模块根用单个 `service` 包承载全部业务。

详见 [package-structure.md](../skills/java/java-reference/references/package-structure.md)。

---

## 3. 二次开发路径选择

动手前先判断「改什么、放哪」：

| 你想做的事 | 推荐落点 | 是否新建 Maven 模块 |
|-----------|---------|-------------------|
| 用户、角色、权限、菜单、字典等租户管理功能 | `innospots-nexus-portal` 新领域包 | 通常 **否** |
| 租户开通、企业主体、平台审计 | `innospots-nexus-platform` | 通常 **否** |
| 管理台 REST 路径与 VO 契约 | `innospots-nexus-console`（interface + VO） | 否 |
| 管理台业务实现与工作流 | `portal` / `platform` | 否 |
| 插件能力、控制台页面贡献 | 独立插件 JAR + `innospots-nexus-plugin` 运行时 | 常为 **是**（插件 artifact） |
| 客户专属集成、外部系统对接 | 新建 `adapter` 模块 | **是** |
| 同进程组装 portal + platform 的可运行服务 | `innospots-nexus-spring-*` / Quarkus 对应模块 | 按需 |
| 统一 HTTP/流/WS 治理（权限、审计、追踪） | 业务模块依赖 `innospots-nexus-service` 中立库 | 否 |

归属判定表见 [module-ownership.md](../skills/java/java-reference/references/module-ownership.md)。

**默认原则：** 新业务域多数只需在 `portal` 或 `platform` 内新增领域包，**不要**为每个功能新建 Maven 模块。

---

## 4. 标准开发流程

### 4.1 功能交付主链路

```text
① grill-me          新建模块 / 重大设计前压力测试（按需安装）
        ↓
② java:project      需要新建 Maven 模块、调整 reactor 时
        ↓
③ java:design       新领域、契约变更、跨模块方案
        ↓
④ java:develop      测试先行 → 实现 → mvn clean compile → mvn test
        ↓
⑤ java:check        统一验证出口
```

### 4.2 何时可跳过步骤

| 场景 | 建议流程 |
|------|---------|
| **新建 Maven 模块** | `grill-me` → `java:project` → `java:design` → `java:develop` → `java:check` |
| **新业务领域（无新模块）** | `grill-me` → `java:design` → `java:develop` → `java:check` |
| **已有域小改动、契约清晰** | `java:develop` → `java:check`（契约不清时补 `java:design` L0） |
| **修 Bug** | 先写复现测试 → 修改 → `java:check` |
| **升工程版本 `${revision}`** | `java:project-upgrade` → `java:check` |
| **升 JDK / 第三方依赖** | `java:check`（基线）→ `java:dependency-upgrade` → `java:check` |

### 4.3 六阶段领域初始化（新领域）

权威流程见 [domain-module-initialization.md](../skills/java/java-reference/standards/domain-module-initialization.md)。摘要：

1. 归属与词汇（design）
2. 状态码与异常契约
3. 实体 / DAO / 端点骨架
4. 契约测试（红灯）
5. 实现与 Operator / Service
6. 单测转绿 + `java:check`

---

## 5. 典型二次开发场景

### 5.1 新增租户侧业务领域（portal）

**步骤：**

1. 用 [module-ownership.md](../skills/java/java-reference/references/module-ownership.md) 确认归属为 `portal`（非 platform）。
2. 走 `java:design` 四步法，产出设计文档或 PR「设计结论」块。
3. 在 `innospots-nexus-portal/src/main/java/com/innospots/nexus/portal/<domain>/` 下按领域优先建包。
4. 若暴露管理台 API：
   - 在 `innospots-nexus-console` 定义 REST interface 与 `domain.request` / `domain.vo`（record）。
   - 在 `portal` 实现 endpoint → service → operator → dao 调用链。
5. 在 `innospots-nexus-spring-console` 中注册 DAO / 端点装配（见既有 `*Configuration` 类模式）。
6. `java:develop` 完成实现与测试，`java:check` 全量验证。

**分层约束：**

```text
endpoint → service → operator → dao
```

Operator 不得依赖 service 或其他 operator。DAO 单表、无 join、无 Mapper XML。

### 5.2 新增平台运维能力（platform）

与 portal 类似，但代码落在 `innospots-nexus-platform`，暴露 `/platform/**` 契约。平台域不提供公开自助注册；不得依赖 portal。

### 5.3 插件二次开发

插件开发独立于 portal 业务包，遵循插件手册：

| 步骤 | 说明 | 文档 |
|------|------|------|
| 定义 Capability API 并实现 Provider | 共享契约 + 插件实现 | [02-development-workflow.md](../innospots-nexus-plugin/docs/plugin/manual/02-development-workflow.md) |
| 选择声明方式 | Java SPI 或 `META-INF/nexus/plugin.yaml` | [03-java-plugin.md](../innospots-nexus-plugin/docs/plugin/manual/03-java-plugin.md)、[04-yaml-plugin.md](../innospots-nexus-plugin/docs/plugin/manual/04-yaml-plugin.md) |
| 配置运行时参数 | schema + 宿主配置合并 | [08-configuration.md](../innospots-nexus-plugin/docs/plugin/manual/08-configuration.md) |
| 控制台贡献 | `console@1` 页面与菜单 | [07-exposure-and-contribution.md](../innospots-nexus-plugin/docs/plugin/manual/07-exposure-and-contribution.md) |
| 宿主装配 | Spring / Quarkus 注册 Decoder、Handler | [09-host-assembly.md](../innospots-nexus-plugin/docs/plugin/manual/09-host-assembly.md)、[10-host-extension-guide.md](../innospots-nexus-plugin/docs/plugin/manual/10-host-extension-guide.md) |

**V1 能力边界：** 支持 Java SPI、YAML 声明、`console@1`、Tags 路由；不支持远程 bind、动态 JAR 安装、独立 ClassLoader。

### 5.4 使用统一服务框架（service）

业务模块通过中立库接入 HTTP 治理，**不**直接依赖 Spring / Quarkus 适配器：

```text
业务模块 → innospots-nexus-service-contract（+ stream / websocket / transfer 按需）
```

- 普通 HTTP：继续用宿主原生路由；框架默认提供 Request ID、Trace、Access Log、异常映射。
- 按需声明：`@RequiresPermission`、`@Audited`、`@RateLimited` 等（见 [service-developer-experience-design.md](../innospots-nexus-service/docs/service-developer-experience-design.md)）。
- Domain 层禁止框架注解与 `ServiceContext` 透传。

阅读顺序见 [innospots-nexus-service/docs/README.md](../innospots-nexus-service/docs/README.md)。

### 5.5 可运行宿主与本地调试

| 运行时 | 模块 | 说明 |
|--------|------|------|
| Spring Boot | `innospots-nexus-spring-app`、`innospots-nexus-spring-console` | 库模块：`EnableNexusAppBootstrap` / `EnableNexusConsole`；可执行入口见 `innospots-nexus-sample` |
| Quarkus | `innospots-nexus-quarkus` | 见插件手册 Quarkus 宿主章节 |

本地环境：

- JDK **25**（与 `innospots-nexus-parent` 的 `maven.compiler.release` 一致）。
- IDE 配置见 [ide-setup.md](ide-setup.md)。
- 验证命令：

```bash
mvn validate
mvn clean compile
mvn test
```

若本地 JDK 低于 25，应升级环境，**不得**降低项目基线。

---

## 6. 使用技能体系进行二次开发

### 6.1 技能是什么

`skills/java/` 目录是一套 **分工化的开发规范与执行手册**，供开发者阅读，也供 Cursor / AI Agent 作为执行入口。

每个技能目录结构：

```text
java-<name>/
├── README.md      # 给人读：定位、输入输出、边界
├── SKILL.md       # AI 执行入口：流程、红线、路由
└── references/    # 详细参考手册
```

技能体系总览：[skills/java/README.md](../skills/java/README.md)。

### 6.2 八个 Java 技能

| 技能 | 用途 | 开发者手册 |
|------|------|-----------|
| `java:reference` | 查规范、红线、技能路由 | [java-reference/README.md](../skills/java/java-reference/README.md) |
| `java:project` | 新建模块、POM、reactor、构建 | [java-project/README.md](../skills/java/java-project/README.md) |
| `java:design` | 架构、契约、测试范围设计 | [java-design/README.md](../skills/java/java-design/README.md) |
| `java:develop` | 实现、修 Bug、重构、单元测试 | [java-develop/README.md](../skills/java/java-develop/README.md) |
| `java:check` | 编译、测试、规范与依赖检查 | [java-check/README.md](../skills/java/java-check/README.md) |
| `java:spring` | Spring Boot 集成边界与依赖 | [java-spring/README.md](../skills/java/java-spring/README.md) |
| `java:project-upgrade` | 工程 `${revision}` 升版 | [java-project-upgrade/README.md](../skills/java/java-project-upgrade/README.md) |
| `java:dependency-upgrade` | JDK、第三方依赖、框架升级 | [java-dependency-upgrade/README.md](../skills/java/java-dependency-upgrade/README.md) |

另有跨技能 **`grill-me`**：重大方案压力测试，不产出代码。见 [grill-me.md](../skills/java/java-reference/references/grill-me.md)。

### 6.3 在 Cursor 中如何使用技能

**方式一：自然语言触发**

在对话中描述任务，并附上触发词或技能名。例如：

- 「按 `java:design` 为通知中心做领域设计」
- 「用 `java:develop` 实现用户组 CRUD，先写契约测试」
- 「查一下 DAO 能不能 join」→ 路由到 `java:reference`

各技能 `SKILL.md` 的 `description` 字段列出了常见触发词。

**方式二：显式引用技能文件**

在 Agent 对话中 `@` 引用路径，例如：

- `@skills/java/java-develop/SKILL.md`
- `@skills/java/java-reference/standards/api-design.md`

**方式三：安装 grill-me（首次使用）**

新建模块或启动重大设计前，若环境未安装 `grill-me`：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

仓库亦内置副本：`.agents/skills/grill-me/`。安装后按技能全文完成 `/grilling` 会话，确认设计树后再进入 `java:project` 或 `java:design`。

**方式四：模块级领域技能（示例）**

部分业务模块在 `src/main/resources/skills/` 下附带领域契约索引，供 AI 理解模块能力边界，例如 `innospots-nexus-portal` 的 [SKILL.md](../innospots-nexus-portal/src/main/resources/skills/SKILL.md)。扩展 portal 时可 `@` 该文件，避免与相邻域混淆。

### 6.4 按任务选择技能

| 你想做的事 | 使用技能 |
|-----------|---------|
| 不确定规则、查命名/异常/分层约定 | `java:reference` |
| 新建 Maven 模块或 adapter | `grill-me` → `java:project` |
| 新领域、改 API 契约、写设计文档 | `grill-me` → `java:design` |
| 写 Entity、Endpoint、DAO、测试 | `java:develop` |
| 合入前全量验证 | `java:check` |
| 调 Spring 自动配置、spring 子模块 POM | `java:spring` |
| 发版改 `${revision}` | `java:project-upgrade` |
| 升 JDK 25、Spring Boot、BOM 依赖 | `java:dependency-upgrade` |

**原则：** `java:reference` 只读查规范；动手写代码走 `java:develop`；动设计走 `java:design`；不要在一个变更里混功能开发与依赖大版本升级。

### 6.5 与 AI Agent 协作的推荐话术

高效请求应包含：**目标、模块/领域、约束、期望技能**。

```text
# 示例 1：新领域
在 innospots-nexus-portal 新增「工作区邀请」领域。
请先走 java:design 四步法，确认归属与契约后再 java:develop。
设计文档放 innospots-nexus-portal/docs/workspace-invite-design.md（L1）。

# 示例 2：小改动
在 portal.user 增加按邮箱查询接口，契约已存在于 PR 描述。
直接 java:develop，先写端点契约测试。

# 示例 3：插件
实现一个 YAML 插件，提供 message.sender capability，
并按 console@1 贡献一个设置页。参考 plugin manual 02–07。

# 示例 4：规范咨询
DAO 层能否用 @Transactional？请只查 java:reference 并引用 standards 原文。
```

### 6.6 模块 API 索引（按需）

`skills/java/java-reference/references/modules/<artifact-id>/README.md` 收录 base、core、console 的公共 API 索引。

- **用途：** 查已有类型与包入口，避免重复造轮子。
- **限制：** 仅在开发者 **明确要求扫描** 时由 Agent 刷新；日常功能开发 **不要** 顺带更新这些索引。

---

## 7. 硬性约束速查

完整清单见 [quick-constraints.md](../skills/java/java-reference/references/quick-constraints.md)。

| # | 约束 |
|---|------|
| 1 | 每批 Java 改动后立即 `mvn clean compile` |
| 2 | 业务失败一律 `NexusException` + 类型化 `StatusCode` |
| 3 | 状态码九字符：`MODULE(3) + CATEGORY(2) + LOCAL(4)` |
| 4 | DAO 单表、无 join、无 Mapper XML；配置用 yaml |
| 5 | 端点 `jakarta.ws.rs`，返回 `R<T>` |
| 6 | `domain.request` / `domain.vo` 使用 record |
| 7 | `endpoint → service → operator → dao` |
| 8 | 事务 `jakarta.transaction.Transactional` |
| 9 | 不得复制遗留工程 |
| 10 | 新功能须有单元/契约测试，交付前 `mvn test` 通过 |

---

## 8. 相关文档索引

| 主题 | 路径 |
|------|------|
| Agent / 模块职责 | [AGENTS.md](../AGENTS.md) |
| IDE 与 JDK 25 | [ide-setup.md](ide-setup.md) |
| Java 技能体系 | [skills/java/README.md](../skills/java/README.md) |
| 模块归属 | [module-ownership.md](../skills/java/java-reference/references/module-ownership.md) |
| 包结构 | [package-structure.md](../skills/java/java-reference/references/package-structure.md) |
| 编码规范原文 | [skills/java/java-reference/standards/](../skills/java/java-reference/standards/) |
| 插件手册 | [innospots-nexus-plugin/docs/plugin/manual/](../innospots-nexus-plugin/docs/plugin/manual/) |
| 服务框架 | [innospots-nexus-service/docs/](../innospots-nexus-service/docs/) |
| 权限设计（portal） | [innospots-nexus-portal/docs/permission-design.md](../innospots-nexus-portal/docs/permission-design.md) |
| 架构决策（ADR） | [docs/design/adr/](design/adr/) |

---

## 9. 快速决策树

```text
开始二次开发
    │
    ├─ 只是查规范？ ──────────────────────────→ java:reference
    │
    ├─ 要新建 Maven 模块？ ──→ grill-me → java:project → java:design → …
    │
    ├─ 新业务能力落在哪？
    │       ├─ 租户管理（用户/权限/菜单） → portal 新领域包
    │       ├─ 平台运维（租户/企业）     → platform 新领域包
    │       ├─ 可插拔能力 + 控制台页     → plugin 手册流程
    │       └─ 外部系统 / 客户定制       → 新 adapter 模块
    │
    ├─ 需要 HTTP 治理注解？ ─────────────────→ service 文档 + java:design
    │
    ├─ 写代码 ──────────────────────────────→ java:develop（测试先行）
    │
    └─ 合入前 ──────────────────────────────→ java:check
```

按上述路径开发，可保持与框架分层、依赖方向和团队规范一致，并让 AI 辅助工具在正确的技能边界内协作。
