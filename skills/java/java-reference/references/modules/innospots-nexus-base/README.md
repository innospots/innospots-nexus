# innospots-nexus-base — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：1.2.0

## 模块概览

不依赖中间件的共享基础层，提供可序列化契约与轻量级工具。

**能力概览：**

| 能力 | 描述 |
|------------|-------------|
| **Status & Exceptions** | 双语状态码与 `NexusException` |
| **JSON** | `Jsons` 门面、脱敏、值转换 |
| **Session & Scope** | 租户/工作区/项目快照、`SessionContext`、`TLC` |
| **Domain Contracts** | 条件、数据交换、字段元数据、身份快照 |
| **Execution** | 带上下文与记录的执行器 SPI（预留） |
| **Infrastructure Utils** | HTTP 客户端、加密、ID（ULID）、日期、指标、线程 |
| **I18n & Resources** | 区域设置解析、文件存储 SPI |

## 类参考

### 包 `config`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `NexusConfig` | `class` | 包装扁平键值映射的不可变配置存储。 |

### 包 `domain.condition`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `DatabaseFactorStatement` | `class` | 将 `Factor` 渲染为 SQL 表达式字符串。 |
| `EmbedCondition` | `class` | 支持嵌套的子条件，可递归包含 `EmbedCondition` 子节点。 |
| `Factor` | `class` | 单个过滤条件：字段编码、运算符、值及可选值类型。 |
| `FactorStatementBuilder` | `class` | 按 `Mode` 选择 `IFactorStatement` 实现。 |
| `IFactorStatement` | `interface` | 将 `Factor` 渲染为 SQL、脚本或 Java 表达式。 |
| `Mode` | `enum` | 条件语句的目标输出模式。 |
| `Operator` | `enum` | 过滤条件使用的比较运算符。 |
| `Relation` | `enum` | 组合多个 `Factor` 的逻辑连接符（`AND` / `OR`）。 |
| `ScriptFactorStatement` | `class` | 将 `Factor` 渲染为脚本/表达式形式。 |
| `SimpleCondition` | `class` | 由单一 `Relation` 连接的 `Factor` 平面列表。 |

### 包 `domain.data`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `DataBody` | `class` | 自带计时的数据载荷。 |
| `DataOperation` | `enum` | 目标数据源上的数据操作类型。 |
| `DataPage` | `record` | 不可变的分页数据容器。 |
| `DataRequest` | `class` | 对命名数据源执行 `DataOperation` 的请求。 |
| `DataResponse` | `class` | 数据操作的通用响应封装。 |
| `DataSchema` | `class` | 载荷结构：`DomainField` 列表及配置项。 |

### 包 `domain.dictionary`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `DictionaryItem` | `record` | 字典类型内的单个键值条目。 |
| `DictionaryType` | `record` | 字典类型元数据及条目。 |

### 包 `domain.enums`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `BasicStatus` | `enum` | 通用的启用/禁用状态。 |

### 包 `domain.field`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `DomainField` | `class` | 领域模式或数据结构中的字段。 |
| `FieldScope` | `enum` | 字段在模式中的角色或边界。 |
| `FieldValueType` | `enum` | 领域字段支持的值类型。 |
| `ParamField` | `class` | 参数字段，含值类型、必填标志及默认值。 |
| `SelectOption` | `record` | 可选项：存储值与显示标签。 |

### 包 `domain.identity`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `RoleSnapshot` | `record` | 角色标识、名称及程序编码。 |
| `UserGroupSnapshot` | `record` | 用户组，含层级、负责人及协助人。 |
| `UserSnapshot` | `class` | 会话/传输层用户快照（`Long userId`）。 |

### 包 `domain.organization`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `OrganizationSnapshot` | `record` | 租户业务档案（区域设置、货币、品牌）。非内核组织单元。 |

### 包 `domain.project`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ProjectSnapshot` | `record` | 工作区内项目的会话快照。 |

### 包 `domain.tenant`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `TenantSnapshot` | `record` | 平台租户的会话快照。 |

### 包 `domain.workspace`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `WorkspaceSnapshot` | `record` | 租户工作区的会话快照。 |

### 包 `domain.request`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `Pagination` | `class` | 共享的分页默认值与规范化。 |
| `SimpleQueryRequest` | `record` | 带关键词过滤的分页查询。 |

### 包 `domain.response`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `PageResult` | `record` | 分页 API 响应封装。 |
| `R` | `record` | 通用 API 响应，含编码、消息、数据及 i18n 显示。 |

### 包 `events`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `DomainEvent` | `interface` | 领域事件基接口。 |
| `EventBus` | `class` | 内存事件总线：订阅、发布、同步发布。 |
| `EventHandler` | `interface` | 特定事件类型的处理器。 |

### 包 `exception`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `NexusException` | `class` | 绑定 `StatusCode` 的平台运行时异常。 |

### 包 `execution`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ExecutionContext` | `class` | 单次执行的执行上下文。 |
| `ExecutionRecord` | `class` | 已完成执行的不可变记录。 |
| `ExecutionStatus` | `enum` | 从创建到完成的生命周期状态。 |
| `Executor` | `interface` | 核心执行单元接口（预留 SPI）。 |

### 包 `http`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `HttpClientBuilder` | `class` | 带默认配置的流式 `CloseableHttpClient` 构建器。 |
| `HttpResult` | `record` | 不可变的 HTTP 响应。 |
| `HttpUtils` | `class` | 便捷的 GET 与 JSON POST 辅助方法。 |

### 包 `i18n`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `I18n` | `@interface` | 标记字段在序列化时进行 i18n 翻译。 |
| `I18nConverter` | `class` | 核心 i18n 翻译引擎。 |
| `I18nMessageResolver` | `interface` | 将 i18n 键解析为本地化消息。 |
| `I18nObject` | `class` | 区域设置到值的映射，表示国际化字符串。 |

### 包 `json`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `Jsons` | `class` | 基于 Jackson 的中央 JSON 工具门面。 |
| `MaskStrategy` | `enum` | 序列化时敏感数据的预定义脱敏策略。 |
| `MaskValue` | `@interface` | 标记字段在序列化时脱敏。 |
| `MaskingModule` | `class` | 用于值转换与脱敏的 Jackson 模块。 |
| `ValueConverter` | `@interface` | 标记字段在序列化时进行值转换。 |

### 包 `mapstruct`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `BaseBeanConverter` | `interface` | 领域模型与实体之间的基础转换器。 |
| `BaseMapperConfig` | `interface` | 共享的 MapStruct 映射器配置。 |
| `BaseMapperSupport` | `class` | 通过映射函数转换集合。 |

### 包 `resources`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `FileResource` | `record` | 文件内容流及元数据标志。 |
| `MetaResource` | `record` | 不可变的已存储资源元数据。 |
| `ResourceEvent` | `record` | 资源元数据持久化时的事件。 |
| `ResourcePatternResolver` | `class` | 解析类路径/资源位置模式。 |
| `ResourceStore` | `interface` | 二进制资源持久化 SPI。 |

### 包 `status`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `NexusStatusCode` | `enum` | 平台级双语状态码。 |
| `StatusCategory` | `enum` | 状态码关注领域分类。 |
| `StatusCode` | `interface` | 可组合的 9 位状态码契约。 |
| `StatusCodeRules` | `class` | 状态码格式校验。 |

### 包 `thread`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `AsyncExecutors` | `class` | 全局异步执行器门面。 |
| `NexusThreadFactory` | `class` | 命名线程工厂（默认 `nexus-worker`）。 |
| `NexusThreadPoolExecutor` | `class` | 向工作线程传播 `TLC` 的线程池。 |
| `SessionContext` | `class` | 用户与作用域快照绑定的类型化门面。 |
| `TLC` | `class` | 线程本地上下文（租户、工作区、项目、用户、追踪）。 |
| `ThreadPoolBuilder` | `class` | `NexusThreadPoolExecutor` 的流式构建器。 |

### 包 `util`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `BeanUtils` | `class` | Bean 拷贝/转换（Hutool `BeanUtil`）。 |
| `Checks` | `class` | 前置条件检查 → `NexusException` + `INVALID_PARAMETER`。 |
| `CryptoUtils` | `class` | BCrypt、AES-GCM、RSA/OAEP。 |
| `DateTimeUtils` | `class` | 日期/时间格式化与解析。 |
| `EnvUtils` | `class` | 支持覆盖的环境属性解析器。 |
| `IdGenerator` | `class` | Snowflake、**ULID**（`ulid`/`monotonicUlid`）、随机、时间戳 ID。 |
| `MetricsSnapshot` | `record` | 指标计数器/计时器的时点快照。 |
| `MetricsUtils` | `class` | Micrometer 指标门面。 |
| `StringUtils` | `class` | 空白检查、占位符、大小写转换。 |

## 包参考

| 包 | 参考 |
|---------|-----------|
| `config` | [`references/config.md`](references/config.md) |
| `domain.condition` | [`references/domain-condition.md`](references/domain-condition.md) |
| `domain.data` | [`references/domain-data.md`](references/domain-data.md) |
| `domain.dictionary` | [`references/domain-dictionary.md`](references/domain-dictionary.md) |
| `domain.enums` | [`references/domain-enums.md`](references/domain-enums.md) |
| `domain.field` | [`references/domain-field.md`](references/domain-field.md) |
| `domain.identity` | [`references/domain-identity.md`](references/domain-identity.md) |
| `domain.organization` | [`references/domain-organization.md`](references/domain-organization.md) |
| `domain.project` | [`references/domain-project.md`](references/domain-project.md) |
| `domain.tenant` | [`references/domain-tenant.md`](references/domain-tenant.md) |
| `domain.workspace` | [`references/domain-workspace.md`](references/domain-workspace.md) |
| `domain.request` | [`references/domain-request.md`](references/domain-request.md) |
| `domain.response` | [`references/domain-response.md`](references/domain-response.md) |
| `events` | [`references/events.md`](references/events.md) |
| `exception` | [`references/exception.md`](references/exception.md) |
| `execution` | [`references/execution.md`](references/execution.md) |
| `http` | [`references/http.md`](references/http.md) |
| `i18n` | [`references/i18n.md`](references/i18n.md) |
| `json` | [`references/json.md`](references/json.md) |
| `mapstruct` | [`references/mapstruct.md`](references/mapstruct.md) |
| `resources` | [`references/resources.md`](references/resources.md) |
| `status` | [`references/status.md`](references/status.md) |
| `thread` | [`references/thread.md`](references/thread.md) |
| `util` | [`references/util.md`](references/util.md) |
