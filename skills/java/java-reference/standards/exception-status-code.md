# 异常与状态码约定

本文档是 Nexus 中应用异常、业务状态码和状态码扩展的权威标准。描述当前的 `NexusException`、`StatusCode`、`StatusCategory`、`StatusCodeRules`、`NexusStatusCode`、`PluginStatusCode` 和 `R<T>` 契约。API 特定的方法与边界规则仍在 [`api-design.md`](api-design.md) 中；本文档定义这些 API 必须使用的共享错误语义。

## 1. 原则

- 一个失败有一个归属边界、一个稳定状态码和一条有用的 cause 链。
- 状态码描述稳定的应用含义。HTTP 状态码仅描述传输结果。
- **所有**应用可见失败必须由 `NexusException`（或 `base.exception` 下统一继承 `NexusException` 的批准子类型）表示；**禁止**向业务调用方抛出 `RuntimeException`、`Exception`、`IllegalArgumentException`、`IllegalStateException`、`NullPointerException`、`UnsupportedOperationException` 等 JDK 通用异常作为失败契约。
- 优先使用含义相同的已有状态码。仅当复用会使契约模糊时才添加新码。
- 消息和建议是兼容性表面的一部分。保持稳定、可操作、双语，且不包含密钥或易变运行时数据。

## 2. 异常分类

根据失败含义选择异常形式，而非根据发生层。

| 失败类型 | 必需表示 | 示例与规则 |
| --- | --- | --- |
| 预期的应用或领域失败 | 带类型化 `StatusCode` 的 `NexusException` | 无效请求、记录缺失、业务键重复、禁止操作、无效生命周期转换。 |
| 在应用边界翻译的基础设施失败 | 带最准确状态码和原始 cause 的 `NexusException` | 数据库超时、外部通道不可用、配置格式错误、插件加载失败。不要向调用方暴露驱动或提供方细节。 |
| 纯工具内部前置条件（**存量**） | 逐步迁移为 `NexusException` 或 `Checks`；**新增**公共 API 不得再抛 JDK 异常 | `base` 中部分 record/工具仍抛 `IllegalArgumentException` 为遗留债务；新代码与业务模块路径一律 `NexusException`。 |
| 中断或取消 | 保留中断/取消语义 | 捕获 `InterruptedException` 时恢复中断标志，然后仅在所属契约明确定义取消时才重抛或翻译。不要将取消转为通用业务错误。 |
| 致命 JVM 或进程失败 | 不要例行捕获或映射 | `Error` 子类、链接失败及其他致命条件必须传播。清理 handler 可观察它们，但不得隐藏。 |

应用边界通常包括 endpoint、service、operator、作业 handler、监听器或公共扩展回调。这些边界**只**向外抛出 `NexusException`。捕获 JDK/框架/受检异常时，必须在边界内翻译为 `NexusException.build(StatusCode, cause)`，不得改抛其他运行时异常族。

## 3. 构造 `NexusException`

### 3.1 类型化状态码是默认方式

使用类型化重载，让状态定义稳定码、类别、消息、建议和所属边界使用的 HTTP 映射：

```java
throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND, cause);
throw NexusException.build(statusCode, displayOverride, cause);
```

优先使用 `NexusException.build(StatusCode, ...)`，因为它保留状态码契约并防止拼写或归属漂移。不要为每个业务错误创建新的异常子类；通过 `StatusCode` 实现区分错误。

原始码重载（`build(String, String, ...)`）保留给互操作边界，如插件、远程提供方、持久化历史载荷或兼容适配器。`StatusCodeRules` 校验类型化状态使用的独立 module/category/local 组件；它本身不是任意完整码字符串的解析器或白名单。在接受原始码之前：

1. 用专用完整码解析器或等效适配器检查验证完整的 `MODULE + CATEGORY + LOCAL` 形态（并对各组件使用 `StatusCodeRules`）；
2. 对照显式白名单或所属注册表检查；
3. 在结构化日志或元数据中保留源系统，而非将不可信文本嵌入公共消息；以及
4. 当边界能够做到时，翻译为本地类型化状态。

仓库内普通应用调用在类型化状态可用时不得向原始码重载传递 `status.fullCode()` 或复制的字符串。现有不具有规范九字符形态的不透明遗留字符串是兼容债务：仅在指定适配器中接受，永不分配新码，并在可能时映射为规范类型化状态，再用于当前传输契约。

### 3.2 消息、展示覆盖与 cause

- `code()` 和 `fullCode()` 标识稳定的九字符状态码。
- `display()` 是可选的本地化或展示特定覆盖；不改变状态身份或 HTTP 映射。
- 状态的 `message()` 说明发生了什么。`advice()` 说明调用方可采取的下一步。即使客户端当前只显示一种语言，也要保持英文和中文值有意义。成功状态可有意留空 advice；新的非成功状态必须提供两种语言。当前对两种语言重复英文文本的技术目录是遗留行为，变更时应本地化，且不得静默更改兼容性键。
- 翻译较低层失败时，将原始 `Throwable` 作为 cause 传递。不要用丢失诊断信息的新构造异常替换有用的 cause。
- 切勿在 `message()`、`advice()`、`display()` 或 endpoint 响应数据中放入密码、令牌、凭证、加密密钥、授权头、含密钥的完整 SQL 或堆栈跟踪。
- 稳定状态文本不得包含请求 ID、记录 ID、文件路径、提供方响应、用户输入或其他易变值。将这些值放入结构化、访问控制的日志或追踪上下文。
- 传给 `NexusException.build(StatusCode, String)` 的运行时消息也是响应表面。对 `message()`、`advice()` 和 `display()` 应用相同限制：不要包含密钥、凭证、ID、用户输入、提供方文本、SQL、路径或堆栈跟踪。优先使用状态摘要，将安全诊断放入结构化上下文。

### 3.3 `Checks` 与 `NexusException` 的分工

`com.innospots.nexus.base.util.Checks` 用于**编程式前置条件**与**不变量守卫**。
所有 `Checks` 方法在失败时抛出 `NexusException`（通常映射为 `NexusStatusCode.INVALID_PARAMETER`
或等价平台校验码），**不是** JDK 的 `IllegalArgumentException`。

| 场景 | 使用 | 禁止 |
|------|------|------|
| 参数/引用非空、非空白、正数、非空集合 | `Checks.notNull` / `notBlank` / `positive` / `notEmpty` | `Objects.requireNonNull`、`IllegalArgumentException` |
| record 紧凑构造器中的形状守卫（无领域语义） | `Checks.*` 或紧凑构造器内显式 `NexusException` | 裸 `null` 传播 |
| 业务拒绝：记录不存在、重复键、禁止操作、授权失败 | `NexusException.build(领域 StatusCode)` | 用 `Checks` 表达业务失败 |
| 工作流/跨记录/授权规则 | service 或 operator 边界选**领域**状态码 | 在深层 helper 随意 `Checks` 代替归属翻译 |
| 互操作边界解析外部码 | 白名单 + 类型化 `StatusCode` | 复制字面量 `fullCode()` |

**规则：** `Checks` 回答「这个调用在语法上是否合法」；`StatusCode` 回答「在这个业务上下文中为何失败」。
若失败需要领域 module 前缀的状态码或调用方需区分 not-found / conflict / forbidden，**不得**仅用 `Checks`。

```java
// 前置条件 — OK
Checks.notBlank(roleCode, "roleCode");

// 业务拒绝 — 必须用领域码
if (role == null) {
    throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND);
}
```

## 4. 抛出、捕获与翻译

### 4.1 在归属边界抛出

在能选择正确业务含义的代码处抛出类型化 `NexusException`：

- 请求校验选择输入校验状态；
- operator 将 mapper 缺失翻译为 not-found 或数据状态；
- service 选择工作流、授权、冲突或跨记录状态；
- 基础设施适配器根据其契约选择外部、中间件、配置或内部错误状态。

不要仅为方便在低层 helper 中抛出通用状态。helper 应返回类型化结果，或传播其原生失败供归属边界翻译。

### 4.2 窄捕获并保留链

- 除非当前边界能添加更具体、语义正确的状态，否则原样重抛已有 `NexusException`。若翻译，保留原始异常作为 cause。
- 捕获边界能理解的最窄受检或提供方异常。用 `NexusException.build(status, cause)` 包装。
- 不要仅为返回伪造成功、空结果或通用消息而捕获 `Exception`。切勿吞掉失败。
- 不要在每一层都记录同一异常。翻译层可添加结构化上下文；外层请求/作业边界拥有最终错误日志和响应映射。
- 保留 `InterruptedException` 和取消。当方法仅为添加上下文而捕获中断时，在返回或重抛前恢复中断标志（`Thread.currentThread().interrupt()`）。
- 不要在普通应用代码中捕获 `Throwable`。若顶层 runner 有致命错误 hook，清理后必须重抛，不得将致命 `Error` 转为正常业务响应。

### 4.3 响应映射

endpoint 基础设施集中捕获 `NexusException` 并映射为 `R.fail(...)`（或 `R.from(exception)`）。HTTP 传输适配器通过所属状态目录/注册表解析稳定码，并单独应用其 `httpStatusCode()`；`R<T>` 本身携带 code、message、display 和 data，不携带 HTTP 状态字段。service、operator 和领域模型不得构造 `R` 响应或泄漏堆栈跟踪。公共响应包含稳定码、传输选择的本地化 message/advice 和安全细节；不包含 cause 链或内部堆栈跟踪。

到达外层边界的未知失败必须用关联上下文记录，并映射为 `NexusStatusCode.SYSTEM_ERROR`（或另一传输边界明确文档化的通用内部状态）。映射不得暴露实现类名、SQL、文件系统路径或提供方诊断。

## 5. 状态码结构

### 5.1 规范格式

每个完整码恰好九字符：

```text
MODULE (3 uppercase letters) + CATEGORY (2 digits) + LOCAL (4 digits)
```

例如，`NEX080002` 表示 module `NEX`、category `08`、local code `0002`。`StatusCodeRules` 是形态校验的权威来源。状态枚举必须从 `bisCode()` 和 `fullCode()` 返回相同值。

| 段 | 规则 | 含义 |
| --- | --- | --- |
| Module | 恰好三个大写 ASCII 字母，如 `NEX` 或 `PLG` | 所属产品/模块或技术边界。 |
| Category | 恰好两位十进制数字，由 `StatusCategory` 支撑 | 失败语义，不是 HTTP 状态。 |
| Local | 恰好四位十进制数字，通常零填充 | module/category 命名空间内的稳定分配。 |

不要缩短码、插入分隔符、使用小写，或在 local 段编码 HTTP 状态。完整码是外部可见标识符；更改它是兼容性破坏。

### 5.2 类别语义

选择解释操作为何失败的类别。当前 `StatusCategory` 值分组如下。每个类别还暴露稳定的人类可读 `label()` 和操作 `priority()` 提示（`L`、`M`、`H`、`B` 或 `C`）；priority 不是 HTTP 状态，本身不决定重试行为。

| 类别族 | 当前类别 | 用于 |
| --- | --- | --- |
| 通用与输入 | `GENERAL`、`INPUT_VALIDATION` | 未分类的共享结果和格式错误的调用方输入。 |
| 业务与授权 | `BUSINESS_RULE`、`PERMISSION_SECURITY`、`COMPLIANCE` | 领域不变式、禁止操作、策略或监管约束。 |
| 冲突与限制 | `TRANSACTION_CONFLICT`、`RESOURCE_LIMIT`、`BATCH_JOB` | 并发/幂等冲突、配额和批处理/作业生命周期失败。 |
| 资源与数据 | `RESOURCE_DATA`、`DATA_OPERATION`、`DATA_CONSISTENCY`、`DATA_SCHEMA` | 缺失或无效数据、持久化操作、完整性和模式不匹配。 |
| 外部与中间件 | `EXTERNAL_FAILURE`、`CHANNEL_INTERACTION`、`MIDDLEWARE`、`DATA_CONNECTION` | 提供方/通道失败、中间件生命周期和连接性。 |
| 配置与执行 | `CONFIGURATION`、`INTERNAL_ERROR`、`SCRIPT`、`SQL_EXECUTION` | 无效设置、意外应用故障、脚本或 SQL 执行。 |
| 文件与加密 | `FILE_OPERATION`、`CRYPTO` | 文件/对象存储操作和加密处理。 |

不要仅因某类别有方便的 HTTP 映射而选择它。例如，重复的业务键是业务或事务冲突，不是任意的 `INTERNAL_ERROR`，即使两者在某种传输中最终都产生服务端响应。

### 5.3 HTTP 映射

`httpStatusCode()` 是传输默认值，仅当外部协议需要时 endpoint 适配器才可调整。常见映射：

| HTTP code | 典型含义 |
| --- | --- |
| `400` | 无效输入或请求形态。 |
| `401` | 缺失或无效认证。 |
| `403` | 已认证但无权限。 |
| `404` | 请求的资源缺失且契约将缺失视为错误。 |
| `409` | 重复、陈旧版本、幂等或其他事务冲突。 |
| `429` | 配额或速率/资源限制。 |
| `500` / `502` / `503` | 内部、外部、中间件或服务不可用失败。 |

同一 HTTP 码可服务多种状态；客户端必须按完整状态码分支，而非仅按 HTTP。反之，不要仅为获得不同 HTTP 码而复制状态，除非应用含义确实不同。

## 6. 状态码命名与放置

### 6.1 枚举与成员名

- 状态枚举类型命名为 `XxxStatusCode`，并实现 `com.innospots.nexus.base.status.StatusCode`。
- 枚举常量使用 `UPPER_SNAKE_CASE`，具有稳定业务含义，如 `ROLE_NOT_FOUND` 或 `INVALID_PARAMETER`。
- 将 local 码、category、message、advice 和 HTTP 映射相邻放在枚举声明中，或同等易发现的不可变定义中。
- 当契约实际是稳定的业务或数据失败时，不要以临时实现命名状态（`MYBATIS_ERROR`）。
- 不要为新的含义复用枚举常量名或完整码。若含义变更，创建新状态并通过兼容流程弃用旧状态。

### 6.2 归属与包位置

将状态放在拥有其含义的最窄模块边界：

- 平台级可复用失败属于 base 的 `NexusStatusCode`。
- 领域特定业务失败属于所属领域的 `<domain>.domain.enums` 包。所属领域是该码业务语义的唯一来源。
- 技术状态放在发出它的技术边界旁，如插件基础设施的 `core.plugin.status.PluginStatusCode`。
- `console` 仅当 console 边界拥有含义时，才可定义业务中立的 console 契约状态；具体的用户、角色、权限、菜单或租户状态属于其所属业务模块。
- 同级 `kernel` 和 `platform` 模块不得相互导入对方的状态枚举、事件或业务包。若工作流跨越两者，使用中立的 console/core 契约或可同时翻译双方状态的应用适配器。

## 7. 扩展状态码目录

对每个新增或变更的状态使用此流程。

1. **添加前先搜索。** 搜索 `NexusStatusCode`、领域状态枚举、技术状态枚举和调用点，查找含义相同的已有码。比较 category、HTTP 映射、message、advice 和兼容性预期，而非仅枚举名。
2. **决定归属。** 将失败分类为平台级、领域特定或技术。在编写枚举前确认 Maven 模块和包。
3. **预留 module 段。** 使用已有批准的三字母 module 码（`NEX`、`PLG` 或其他注册码）。新 module 段需要注册表/白名单更新和评审；切勿发明与其他 module 冲突的码。
4. **选择 category。** 选择描述失败语义的 `StatusCategory`。若无当前类别适合，在分配 local 码之前单独提议 category 扩展。
5. **分配 local 码。** 在所属 module 命名空间中选择未使用的四位 local 值。检查该 module 中的所有状态，包括同级文件和生成/注册表定义。切勿重新编号已有码以使列表看起来连续。
6. **定义稳定元数据。** 添加 `UPPER_SNAKE_CASE` 常量、双语 message 和 advice（有意成功/无 advice 状态除外）、category、local 码和有意 HTTP 映射。文本必须解释稳定条件和安全下一步；不要放入运行时 ID、密钥或提供方文本。
7. **使用类型化构造。** 用新枚举常量抛出。仅当互操作边界真正需要时才添加原始码注册，并在同一变更中添加校验/白名单条目。
8. **集成前添加契约测试。** 验证形态、local 唯一性、元数据、category、HTTP 映射、类型化异常构造、cause 保留以及 endpoint/`R` 映射。对公共扩展，还要测试未知和不允许的原始码。
9. **评审兼容性。** 检查客户端、本地化资源、仪表板、告警规则、持久化错误载荷、事件消费者和配置键。将完整码、枚举名、HTTP 映射和消息键视为兼容性表面。文档化弃用和迁移行为。

## 8. 契约测试要求

每个状态目录或扩展应有聚焦测试，证明：

- module 是三个大写字母；
- category 存在且属于预期语义族；
- local 码是四位数字；
- 完整码是九字符且等于 `module + category + local`；
- local/完整码在所属 module 目录内唯一；
- 枚举常量使用 `UPPER_SNAKE_CASE`，类型以 `StatusCode` 结尾；
- 非成功状态的英文和中文 message/advice 非空；有意成功/无 advice 状态除外，遗留语言回退应记录；
- category 的 `label()` 和 `priority()` 有意且稳定；
- HTTP 映射有意且与边界契约一致；
- `NexusException.build(status)` 返回预期的 `code()` 并保留提供的 cause；
- 原始码互操作拒绝格式错误或不在白名单中的码；
- endpoint 基础设施将状态映射为 `R.fail(...)` 且不暴露 cause 或堆栈跟踪；
- 遵守 module/包归属和同级模块依赖规则。

## 9. 评审清单

### 异常清单

- [ ] 这是预期的应用失败、可翻译的基础设施失败、纯程序员误用、取消还是致命错误？
- [ ] 归属边界是否选择了类型化、可复用的状态码？
- [ ] 翻译时是否保留了原始 cause？
- [ ] 中断、取消和致命错误是否正确传播？
- [ ] 响应中是否没有重复日志、吞掉的异常、堆栈跟踪和敏感值？
- [ ] `R` 构造是否限于 endpoint/响应边界？

### 状态码清单

- [ ] 码是否恰好为 `MODULE(3) + CATEGORY(2) + LOCAL(4)`？
- [ ] module 段是否已注册且由正确边界拥有？
- [ ] category 是否语义化而非 HTTP 捷径？
- [ ] local 码是否未使用且稳定？
- [ ] 枚举、message、advice 和 HTTP 映射命名是否有意？
- [ ] 是否评审了双语文本和兼容性消费者？

### 扩展清单

- [ ] 添加码之前是否搜索了已有目录？
- [ ] 是否记录了平台/领域/技术归属？
- [ ] 是否仅在需要时更新了原始码白名单或注册表？
- [ ] 是否添加了契约和翻译测试？
- [ ] 是否检查了客户端、日志、仪表板、事件和配置消费者的兼容性？
