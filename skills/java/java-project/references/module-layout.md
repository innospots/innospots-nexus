# 模块结构与职责边界

## 分层全景

```text
innospots-nexus (root, packaging=pom)
├── innospots-nexus-bom        依赖版本清单（无代码）
├── innospots-nexus-parent     构建 parent（无代码）
├── innospots-nexus-base       纯 Java 基础
├── innospots-nexus-core       业务中立的基础设施
├── innospots-nexus-console    管理台契约与扩展
├── innospots-nexus-kernel     核心管理业务能力
└── innospots-nexus-platform   运营域平台能力
```

依赖方向严格单向：

```text
base  →  core  →  console  →  kernel
                          ↘ platform
```

`kernel` 与 `platform` 是 `console` 下的两个平行业务模块，**互不依赖**。
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
| UI 规格 | `ui.spec` | `UiSpec` 及其解析 |
| 领域原语 | `domain.{identity,organization,project,field,condition,dictionary,request,data}` | 共享业务中立的数据契约 |

**硬约束**：

- 不含业务领域逻辑
- 不含数据库、消息、调度、Servlet、Spring、Quarkus 等运行时基础设施
- 保持轻量、依赖最少

### innospots-nexus-core

**定位**：在 base 之上扩展业务中立的中间件、数据库与平台基础设施支持。

**可包含**：共享持久化实体与公共表、数据库支持、调度、服务生命周期、会话基础设施、
watcher、扩展边界，以及其他非管理类的通用能力。

**硬约束**：

- 必须保持业务中立：用户、角色、权限、菜单等具体业务域不属于此模块
- 可以依赖所需的中间件 API 与实现，但**不得绑定 Spring Boot 自动配置**
- 业务专属基础设施应放在其业务模块或独立 adapter / plugin / extension / application 模块

### innospots-nexus-console

**定位**：管理平台的地基与扩展契约模块，为管理台特性模块提供业务中立的支撑。

**可包含**：Jakarta REST 端点契约、扩展声明、菜单/路由贡献模型、共享管理台抽象。

**硬约束**：

- 不实现具体管理业务功能；用户、角色、权限、注册等属于 `kernel` 之类的业务模块
- 管理台特性模块暴露管理能力时依赖此模块

### innospots-nexus-kernel

**定位**：建设在 console 与 core 之上的 Nexus 核心业务功能模块。

**负责**：认证、注册、用户、角色、权限、菜单、字典、审计等基础平台管理能力。

**代码组织**：先按业务域划分，再按职责分包
`endpoint`、`dao`、`domain`、`converter`、`operator`、`service`、`handler`、
`interceptor`、`listener`。

**硬约束**：使用 base / core / console 的共享基础设施与契约，不得自行重造。

### innospots-nexus-platform

**定位**：运营域平台，与 kernel 平行，建设在 console 地基之上。

**负责**：租户生命周期（`nx_tenant`）、企业主体（`nx_enterprise`），后续扩展平台用户、
支持访问、平台审计。

**硬约束**：

- 暴露 `/platform/**` 契约
- 不提供对外自助注册
- 依赖 `console` 及传递的 `core` / `base`；**不得依赖 `innospots-nexus-kernel`**

---

## 业务域内部包结构

业务代码**先按业务域、再按职责**组织：

```text
com.innospots.nexus.kernel
  └── role
      ├── endpoint        仅 Jakarta REST HTTP 边界
      ├── dao             MyBatis-Plus 持久化映射
      ├── domain
      │   ├── entity      数据库持久化实体（*Entity）
      │   ├── request     端点请求 record（*Request）
      │   ├── vo          端点响应 record（*Vo）
      │   ├── model       内部业务模型（无强制后缀）
      │   ├── enums       业务枚举与领域状态码
      │   └── event       领域事件
      ├── converter       MapStruct 与定向转换器
      ├── operator        基于 DAO 的直接数据操作
      ├── service         工作流、编排、校验、跨域逻辑
      ├── handler         事件处理器
      ├── interceptor     环绕调用
      └── listener        生命周期/外部通知监听
```

技术/非业务模块（base、script、工具、可复用技术能力）按**功能**组织，不按业务域。

### 包命名约束

| 必须 | 禁止 |
|------|------|
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
- [ ] `AGENTS.md` 中的模块职责是否需要同步更新？
