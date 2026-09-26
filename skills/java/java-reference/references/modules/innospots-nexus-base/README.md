# innospots-nexus-base — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：0.1.0-SNAPSHOT

## 模块概览

不依赖中间件的共享基础层，提供可序列化契约与轻量级工具。

**能力一览：**

| 能力 | 说明 |
|------|------|
| **Status & Exceptions** | 双语状态码与 `NexusException` |
| **JSON** | `Jsons` 门面、脱敏、值转换、I18n 序列化 |
| **Session & Scope** | 租户/工作区/项目快照、`TenantScope`、`SessionContext`、`TLC` |
| **Domain Contracts** | 条件、数据交换、字段元数据、身份快照 |
| **Execution** | 带上下文与记录的执行器 SPI（预留） |
| **Infrastructure Utils** | HTTP 客户端、加密、ID（ULID）、日期、指标、线程 |
| **I18n & Resources** | 区域设置解析、文件存储 SPI |

## 类参考

### 包 `config`

| 类 | 类型 | 说明 |
|------|------|------|
| `NexusConfig` | `class` | 包装扁平键值映射的不可变配置存储 |

### 包 `domain.condition`

| 类 | 类型 | 说明 |
|------|------|------|
| `DatabaseFactorStatement` | `class` | 将 Factor 渲染为 SQL 表达式字符串 |
| `EmbedCondition` | `class` | 支持嵌套的条件——每个 EmbedCondition 可递归包含子条件 |
| `Factor` | `class` | 由字段编码、运算符、值及可选值类型组成的单个过滤条件 |
| `FactorStatementBuilder` | `class` | 根据目标 Mode 选择合适 IFactorStatement 实现的工厂 |
| `IFactorStatement` | `interface` | 将 Factor 渲染为模式特定表达式字符串（SQL、脚本或 Java）的策略接口 |
| `Mode` | `enum` | 条件语句的目标输出模式 |
| `Operator` | `enum` | 过滤条件中使用的比较运算符 |
| `Relation` | `enum` | 用于连接多个 Factor 条件的逻辑组合符 |
| `ScriptFactorStatement` | `class` | 将 Factor 渲染为脚本/表达式语言（如 MVEL、SpEL）字符串 |
| `SimpleCondition` | `class` | 由单一 Relation 连接的 Factor 平面列表 |

### 包 `domain.data`

| 类 | 类型 | 说明 |
|------|------|------|
| `DataBody` | `class` | 自带计时的数据载荷 |
| `DataOperation` | `enum` | 对目标数据源执行的数据操作类型 |
| `DataPage` | `record` | 不可变的分页数据容器 |
| `DataRequest` | `class` | 对命名目标数据源执行 DataOperation 的请求 |
| `DataResponse` | `class` | 数据操作的通用响应信封 |
| `DataSchema` | `class` | 描述数据载荷结构：com.innospots.nexus.base.domain.field.DomainField 列表加自由格式配置项 |

### 包 `domain.dictionary`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItem` | `record` | 字典类型内的单个键值条目 |
| `DictionaryType` | `record` | 字典类型（如 "gender"、"country"），用于分组相关的 DictionaryItem 条目 |

### 包 `domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `BasicStatus` | `enum` | 跨领域实体通用的启用/禁用状态 |

### 包 `domain.field`

| 类 | 类型 | 说明 |
|------|------|------|
| `DomainField` | `class` | 描述领域模式或数据结构中的字段 |
| `FieldScope` | `enum` | 字段在领域模式中的角色或归属边界 |
| `FieldValueType` | `enum` | 领域字段支持的值类型 |
| `ParamField` | `class` | 具有特定 FieldValueType、必填标志与可选默认值的参数字段 |
| `SelectOption` | `record` | 具有存储值与显示标签的可选项 |

### 包 `domain.identity`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleSnapshot` | `record` | 角色定义，包含唯一标识符、显示名称与程序化编码 |
| `UserGroupSnapshot` | `record` | 具有层级结构（父组）、负责人与协助人的用户组/团队 |
| `UserSnapshot` | `class` | 用户的会话/传输快照 |

### 包 `domain.organization`

| 类 | 类型 | 说明 |
|------|------|------|
| `OrganizationSnapshot` | `record` | 租户面向业务的档案（语言环境、货币、品牌标识） |

### 包 `domain.project`

| 类 | 类型 | 说明 |
|------|------|------|
| `ProjectSnapshot` | `record` | 工作区内项目的会话/传输快照 |

### 包 `domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `Pagination` | `class` | 查询请求共享的分页默认值与规范化逻辑 |
| `SimpleQueryRequest` | `record` | 带关键词过滤的分页查询请求 |

### 包 `domain.response`

| 类 | 类型 | 说明 |
|------|------|------|
| `PageResult` | `record` | 分页 API 响应包装器 |
| `R` | `record` | 通用 API 响应包装器，包含成功/失败状态、结果码、消息、可选数据载荷， 以及失败时供前端渲染的国际化展示消息 |

### 包 `domain.scope`

| 类 | 类型 | 说明 |
|------|------|------|
| `TenantScope` | `record` | 租户身份及其业务组织档案（console 作用域端口与 portal 实现共享） |

### 包 `domain.tenant`

| 类 | 类型 | 说明 |
|------|------|------|
| `TenantSnapshot` | `record` | 平台租户（nx_tenant）的会话/传输快照 |

### 包 `domain.workspace`

| 类 | 类型 | 说明 |
|------|------|------|
| `WorkspaceSnapshot` | `record` | 租户工作区（nx_workspace）的会话/传输快照 |

### 包 `events`

| 类 | 类型 | 说明 |
|------|------|------|
| `DomainEvent` | `interface` | 所有领域事件的基础接口 |
| `EventBus` | `class` | 简单的内存事件总线 |
| `EventHandler` | `interface` | 领域事件处理器的函数式接口 |

### 包 `exception`

| 类 | 类型 | 说明 |
|------|------|------|
| `NexusException` | `class` | 平台基础运行时异常 |

### 包 `execution`

| 类 | 类型 | 说明 |
|------|------|------|
| `ExecutionContext` | `class` | 单次运行的执行上下文 |
| `ExecutionRecord` | `class` | 已完成执行的不变记录 |
| `ExecutionStatus` | `enum` | 执行从创建到完成的生命周期状态 |
| `Executor` | `interface` | 核心执行单元接口 |

### 包 `http`

| 类 | 类型 | 说明 |
|------|------|------|
| `HttpClientBuilder` | `class` | CloseableHttpClient 的流式构建器，提供合理默认值 （连接超时 10s、响应超时 30s、启用重定向） |
| `HttpResult` | `record` | HTTP 请求结果的不可变记录 |
| `HttpUtils` | `class` | HTTP GET 与 POST（JSON）请求的便捷方法 |

### 包 `i18n`

| 类 | 类型 | 说明 |
|------|------|------|
| `I18n` | `@interface` | Jackson 注解，标记字段在序列化时自动进行 i18n 翻译 |
| `I18nConverter` | `class` | 核心 i18n 翻译引擎 |
| `I18nMessageResolver` | `interface` | 将 i18n 键解析为本地化消息字符串的策略接口 |
| `I18nObject` | `class` | 表示国际化字符串的语言环境到值的映射 |

### 包 `json`

| 类 | 类型 | 说明 |
|------|------|------|
| `I18nModule` | `class` | 激活 I18nObject 序列化/反序列化及 @I18n 字段契约的 Jackson Module |
| `I18nObjectDeserializer` | `class` | I18nObject 的 Jackson 反序列化器 |
| `I18nObjectSerializer` | `class` | I18nObject 的 Jackson 序列化器 |
| `Jsons` | `class` | 基于 Jackson 的中央 JSON 工具门面 |
| `MaskStrategy` | `enum` | JSON 序列化时敏感数据的预定义脱敏策略 |
| `MaskValue` | `@interface` | 标记字段或访问器在 JSON 序列化时进行脱敏 |
| `MaskedSerializer` | `class` | 应用字段级脱敏的 Jackson 序列化器 |
| `MaskingModule` | `class` | 激活字段级值转换与脱敏的 Jackson Module |
| `ValueConverter` | `@interface` | 标记字段或访问器在 JSON 序列化时进行值转换 |
| `ValueConvertingSerializer` | `class` | 应用字段级值转换的 Jackson 序列化器 |

### 包 `mapstruct`

| 类 | 类型 | 说明 |
|------|------|------|
| `BaseBeanConverter` | `interface` | 领域模型与持久化实体之间的 MapStruct 风格转换器基接口 |
| `BaseMapperConfig` | `interface` | 所有领域 Mapper 共享的 MapStruct 配置 |
| `BaseMapperSupport` | `class` | 通过映射函数转换集合的工具类 |

### 包 `resources`

| 类 | 类型 | 说明 |
|------|------|------|
| `FileResource` | `record` | 带内容流与元数据标志的文件资源 |
| `MetaResource` | `record` | 已存储资源的不变元数据记录 |
| `ResourceEvent` | `record` | 资源元数据保存/持久化时发布的领域事件 |
| `ResourcePatternResolver` | `class` | 将资源位置模式（如 classpath*:mapper/**\/*.xml）解析为 Resource 列表 |
| `ResourceStore` | `interface` | 二进制资源持久化与读取的抽象接口 |

### 包 `status`

| 类 | 类型 | 说明 |
|------|------|------|
| `NexusStatusCode` | `enum` | 平台级状态码枚举，提供双语（EN/ZH）消息与建议，按 StatusCategory 分组 |
| `StatusCategory` | `enum` | 按关注领域对状态码进行分类 |
| `StatusCode` | `interface` | 可组合状态码接口，由 3 字母模块码、StatusCategory（2 位数字）和 4 位本地码组成 |
| `StatusCodeRules` | `class` | 状态码格式校验规则 |

### 包 `thread`

| 类 | 类型 | 说明 |
|------|------|------|
| `AsyncExecutors` | `class` | 全局异步执行器门面，由单例 NexusThreadPoolExecutor 支撑 |
| `ExecutorShutdown` | `class` | ExecutorService 优雅关闭工具 |
| `NexusThreadFactory` | `class` | 命名 ThreadFactory，按 ThreadExecutionRole 配置守护线程与名称前缀 |
| `NexusThreadPoolExecutor` | `class` | 自定义 ThreadPoolExecutor，在提交线程与工作线程之间捕获并传播 TLC 上下文 |
| `ScheduledThreadPoolBuilder` | `class` | 命名 ScheduledThreadPoolExecutor 的流式构建器；默认定时类后台线程（守护线程） |
| `SessionContext` | `class` | 基于 TLC 的用户与作用域快照类型化门面 |
| `TLC` | `class` | 线程本地上下文（Thread-Local Context）—— 类型化的 ThreadLocal 映射， 用于在异步边界间传播横切状态（追踪 ID、租户 ID、用户 ID、工作区 ID 等） |
| `ThreadExecutionRole` | `enum` | 线程执行角色：区分需随进程优雅收尾的业务工作线程与不阻塞 JVM 退出的后台线程 |
| `ThreadPoolBuilder` | `class` | NexusThreadPoolExecutor 的流式构建器 |

### 包 `util`

| 类 | 类型 | 说明 |
|------|------|------|
| `BeanUtils` | `class` | Bean 属性拷贝与转换工具，封装 Hutool BeanUtil |
| `Checks` | `class` | 前置条件校验工具，校验失败时抛出携带 NexusStatusCode 的 NexusException |
| `CryptoUtils` | `class` | 密码学工具类：密码哈希（BCrypt）、对称加密（AES-GCM）与不对称加密（RSA/OAEP） |
| `DateTimeUtils` | `class` | 日期时间格式化与解析工具类 |
| `EnvUtils` | `class` | 环境属性解析器，支持程序化覆盖 |
| `IdGenerator` | `class` | ID 生成工具，提供 Snowflake 分布式 ID、可配置字符集的随机 ID、 时间戳前缀 ID 以及批量生成能力 |
| `MetricsSnapshot` | `record` | 指标计数器/计时器的时点快照，记录指标名称、标签、总次数及累计耗时（纳秒） |
| `MetricsUtils` | `class` | 基于 Micrometer 的指标门面，提供计数器、计时器与仪表盘， 并自动规范化指标名称（小写、下划线分隔） |
| `StringUtils` | `class` | 字符串工具类，提供空白判断、占位符替换（${key} 与 {{key}}）、 驼峰/下划线命名转换以及随机键生成等能力 |


## 包参考

| 包 | 参考 |
|------|------|
| `config` | [`references/config.md`](references/config.md) |
| `domain.condition` | [`references/domain-condition.md`](references/domain-condition.md) |
| `domain.data` | [`references/domain-data.md`](references/domain-data.md) |
| `domain.dictionary` | [`references/domain-dictionary.md`](references/domain-dictionary.md) |
| `domain.enums` | [`references/domain-enums.md`](references/domain-enums.md) |
| `domain.field` | [`references/domain-field.md`](references/domain-field.md) |
| `domain.identity` | [`references/domain-identity.md`](references/domain-identity.md) |
| `domain.organization` | [`references/domain-organization.md`](references/domain-organization.md) |
| `domain.project` | [`references/domain-project.md`](references/domain-project.md) |
| `domain.request` | [`references/domain-request.md`](references/domain-request.md) |
| `domain.response` | [`references/domain-response.md`](references/domain-response.md) |
| `domain.scope` | [`references/domain-scope.md`](references/domain-scope.md) |
| `domain.tenant` | [`references/domain-tenant.md`](references/domain-tenant.md) |
| `domain.workspace` | [`references/domain-workspace.md`](references/domain-workspace.md) |
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
