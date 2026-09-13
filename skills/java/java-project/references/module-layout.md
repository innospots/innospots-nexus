# 模块结构与职责边界

> 本文档说明**模块职责与包结构约定**，供新建/调整工程时选型。
> Maven 依赖应引哪个 artifact、如何最小声明，见
> [dependency-conventions.md](dependency-conventions.md)。

## 分层全景

```text
innospots-nexus (root, packaging=pom)
├── innospots-nexus-bom              依赖版本清单（无代码，所有 JAR 版本唯一来源）
├── innospots-nexus-parent           构建 parent（无代码，默认继承目标）
├── innospots-nexus-base             纯 Java 基础
├── innospots-nexus-core             业务中立的平台基础设施
├── innospots-nexus-plugin           插件运行时与 Page DSL
├── innospots-nexus-console          管理台契约与 catalog 索引（引用此模块 = 控制台地基）
├── innospots-nexus-kernel           租户侧管理业务（业务类管理端）
├── innospots-nexus-platform         运营域平台（系统运营类）
├── innospots-nexus-spring           Spring Boot 运行时聚合
│   ├── innospots-nexus-spring-app
│   └── innospots-nexus-spring-console
└── innospots-nexus-quarkus          Quarkus 运行时聚合
    ├── innospots-nexus-quarkus-app
    └── innospots-nexus-quarkus-console
```

库模块依赖方向严格单向：

```text
base  →  core  →  plugin  →  console  →  kernel
                                    ↘ platform
```

`kernel` 与 `platform` 是 `console` 下的两个平行业务模块，**互不依赖**。
`spring` / `quarkus` 为**运行时组装层**，依赖上层的库模块，不在中立库中反向被依赖。
任何反向依赖或跨平级依赖都必须在设计阶段消解：

| 需求 | 正确做法 | 错误做法 |
|------|---------|---------|
| kernel 需要 platform 的租户信息 | 把业务中立契约下沉到 `console` 或 `core` | 让 kernel 依赖 platform |
| 两个模块都要消费同一事件 | 事件契约放可共同依赖的低层，或由可同时依赖两者的 application/adapter 模块协调 | 把具体业务事件塞进 core 只为绕过依赖规则 |
| platform 需要用户能力 | 抽象成 console 契约或独立 application 模块编排 | 让 platform 依赖 kernel |

---

## 各模块详解

### innospots-nexus-base

**定位**：纯 Java 基础，为其他模块提供共享依赖、契约、原语与工具包。

**已有的典型能力**（以实际源码为准，非穷举）：

| 能力域 | 包 | 说明 |
|-------|-----|------|
| 异常与状态码 | `exception`、`status` | `NexusException`、`StatusCode`、`StatusCategory`、`StatusCodeRules`、`NexusStatusCode` |
| 响应包装 | `domain.response` | `R<T>`、`PageResult<T>` |
| 领域事件 | `events` | `EventBus`、`DomainEvent`、`EventHandler`、`Subscription` |
| MapStruct 支持 | `mapstruct` | `BaseMapperConfig`、`BaseBeanConverter` |
| JSON | `json` | `Jsons`、`@MaskValue`、`ValueConverter` |
| ID 生成 | `util` | `IdGenerator`（`ulid(prefix)`） |
| 加解密 | `util` | `CryptoUtils` |
| HTTP | `http` | `HttpUtils` |
| 线程 | `thread` | `ThreadPoolBuilder`、`TLC` |
| 国际化 | `i18n` | 注解与转换器 |
| 配置 | `config` | `NexusConfig` |
| 资源 SPI | `resources` | `ResourceStore`、`MetaResource`、`FileResource` |
| 领域原语 | `domain.{identity,organization,project,field,condition,dictionary,request,data}` | 共享业务中立的数据契约 |

**硬约束**：

- 不含业务领域逻辑
- 不含数据库、消息、调度、Servlet、Spring、Quarkus 等运行时基础设施
- 保持轻量、依赖最少

### innospots-nexus-core

**定位**：在 base 之上扩展业务中立的中间件、数据库与平台基础设施支持。

**可包含**：共享持久化基类、审计填充、Quartz 调度、服务节点注册、watcher、启动 SPI、
文件元数据（`nx_meta_resource`）与存储注册表。

**硬约束**：

- 必须保持业务中立：用户、角色、权限、菜单、catalog 索引等不属于此模块
- 可以依赖所需的中间件 API 与实现，但**不得绑定 Spring Boot 自动配置**
- 不含插件运行时与 Page DSL（见 `innospots-nexus-plugin`）

### innospots-nexus-plugin

**定位**：classpath 插件运行时、贡献解码与 Page DSL。

**可包含**：插件发现/安装/生命周期、`console@1` 贡献契约、Pactor Page DSL 1.0。

**硬约束**：

- 不持久化 console catalog 索引表（`nx_console_catalog_resource` 属于 console）
- 不绑定 Spring / Quarkus 自动配置

### innospots-nexus-console

**定位**：**管理控制台地基** — 管理平台 API 契约与扩展支撑（域无关）。

**Maven 引用场景**：需要管理台 REST 契约、catalog、权限运行时等，但**不含**具体业务实现时引此模块。

**可包含**：Jakarta REST 端点契约、console catalog 索引持久化与同步、VO/转换器。

**硬约束**：

- 不拥有插件规范与 contribution 约束定义（属于 `innospots-nexus-plugin`）
- 不实现具体管理业务功能；用户、角色、权限、注册等属于 `kernel` 之类的业务模块

### innospots-nexus-kernel

**定位**：**业务类管理端** — 建设在 console 之上的租户侧管理业务实现。

**Maven 引用场景**：租户侧认证、用户、角色、权限、菜单、字典、审计等管理功能实现。

**负责**：认证、注册、用户、角色、权限、菜单、字典、审计等基础平台管理能力。

**代码组织**：先按业务域划分，再按职责分包
`endpoint`、`dao`、`domain`、`converter`、`operator`、`service`、`handler`、
`interceptor`、`listener`。

**硬约束**：使用 base / core / console 的共享基础设施与契约，不得自行重造。

### innospots-nexus-platform

**定位**：**系统运营类平台**，与 kernel 平行，建设在 console 地基之上。

**Maven 引用场景**：租户生命周期、企业主体、平台 IAM、`/platform/**` 等运营侧能力。

**负责**：租户生命周期（`nx_tenant`）、企业主体（`nx_enterprise`），后续扩展平台用户、
支持访问、平台审计。

**硬约束**：

- 暴露 `/platform/**` 契约
- 不提供对外自助注册
- 依赖 `console` 及传递的 `core` / `base`；**不得依赖 `innospots-nexus-kernel`**

### innospots-nexus-spring

**定位**：基于 **Spring Boot** 的运行时组装聚合（`packaging=pom`）。

**子模块**：

| 子模块 | 用途 |
|--------|------|
| `innospots-nexus-spring-app` | Spring 基础设施（Web、JDBC、MyBatis-Plus 等） |
| `innospots-nexus-spring-console` | 可运行的管理端 Spring Boot 应用 |

新建 Spring 服务时引用此聚合下的子模块，不要在中立库模块中引入 `spring-boot-starter`。

### innospots-nexus-quarkus

**定位**：基于 **Quarkus** 的运行时组装聚合（`packaging=pom`）。

**子模块**：

| 子模块 | 用途 |
|--------|------|
| `innospots-nexus-quarkus-app` | Quarkus 基础设施组装 |
| `innospots-nexus-quarkus-console` | 可运行的管理端 Quarkus 应用 |

新建 Quarkus 服务时引用此聚合下的子模块，不要在中立库模块中绑定 Quarkus 扩展。

---

## 业务域内部包结构

业务代码**先按业务域、再按职责**组织（`role/endpoint`、`role/dao`，**禁止**
`endpoint/role`、`dao/menu`）。完整规则与反例见
[java-reference → package-structure.md](../../java-reference/references/package-structure.md)。

```text
com.innospots.nexus.kernel
  ├── permission                    # 较大领域：先划功能子模块
  │   ├── authorization             # 请求鉴权
  │   ├── grant                     # 授权授予（可含 service/operator/domain）
  │   ├── entry                     # 控制台插件入口
  │   ├── endpoint / dao / domain / service（单包 ≤15 类）
  └── role                          # 较小领域：领域根下直接挂职责包
      ├── endpoint                  # Jakarta REST HTTP 边界
      ├── dao                       # MyBatis-Plus 持久化映射
      ├── domain
      │   ├── entity / request / vo / model / enums / event
      ├── converter                 # MapStruct（按需）
      ├── operator                  # 直接数据操作（按需）
      ├── service                   # 工作流编排（按需；勿堆满）
      └── handler / interceptor / listener（按需）
```

技术/非业务模块（base、script、工具、可复用技术能力）按**功能**组织，不按业务域。

### 包命名约束

| 必须 | 禁止 |
|------|------|
| 领域优先：`kernel.role.endpoint` | 技术层优先：`kernel.endpoint.role` |
| 大领域按功能子模块：`permission.authorization`、`grant.service` | 模块根 `service` 或单包堆满 `*Service` |
| 单包 ≤15 个 `.java` | 单包 16+ 类不分子包 |
| 只用清单内的职责包名 | 自造层级 |
| 只为有实际职责的包建目录 | 脚手架式空分层、占位类型 |
| 单数名词（`domain.condition`） | 复数（`domain.conditions`） |
| 实现类与其契约同包，或放精确的职责包（`runtime`/`adapter`/`persistence`） | `impl`/`common`/`misc`/`util` 子包 |
| 基础设施放 adapter/plugin 模块 | 基础设施塞进领域包 |

`domain.enums` 是项目显式约定的复数例外。

### 初始面最小化

新领域初始只包含：

```text
<domain>
  ├── dao
  ├── domain
  │   ├── entity
  │   ├── enums
  │   ├── request
  │   └── vo
  └── endpoint
```

`model`、`converter`、`operator`、`service`、`event`、`handler` 等**仅在当前任务确实
需要时**才加。

---

## 新增模块决策清单

在动手建新 Maven 模块前逐条确认：

- [ ] 边界是否清晰到可以独立测试？
- [ ] 依赖方向是否单向、无环？
- [ ] 是否确实无法并入现有模块？
- [ ] 若属业务能力，是否应放在 `kernel` / `platform` 的某个域下而非新模块？
- [ ] 若为基础设施，是业务中立（→ `core`）还是业务专属（→ 业务模块或 adapter）？
- [ ] 是否需要被其他模块依赖？（是 → 需在 BOM 登记）
- [ ] POM 是否只声明了最小直接依赖？（见 dependency-conventions.md）
- [ ] `AGENTS.md` 中的模块职责是否需要同步更新？
