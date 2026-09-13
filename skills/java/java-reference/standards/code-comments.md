# 代码注释

注释用于记录名称和类型单独无法表达的契约、约束和设计意图。不得叙述显而易见的语法，也不得保存属于版本控制的变更历史。

## 包文档

当包暴露公共契约、定义架构边界或存在非显而易见的使用约束时，添加 `package-info.java`。
其 Javadoc 应说明：

- 包的单一职责；
- 什么属于、什么不属于该包；
- 相关时的依赖方向或扩展边界；
- 包内共享的生命周期、线程安全、持久化或传输假设。

建议包含 `@author`、`@date`，以及指向包内关键类型或模块文档的 `@see`（见下方「类型注释」）。
不要创建仅展开包名的空或样板包文档。当包职责发生重大变化时，更新 `package-info.java`
正文；**不要**因后续改动而更新 `@date`。

## 类型注释

每个**公共**类型（`public` class、interface、enum、record、annotation）必须在声明前立即有
Javadoc 块注释。注释描述类型自身抽象层级的契约；接口说明实现方承诺什么；实现说明其
策略、生命周期或区分行为，而不复制接口文本。

### 必填内容

正文第一段用完整句子说明：

- 类型是什么及其主要职责；
- 重要的使用约束、归属范围或线程安全保证（若有）。

标签区**必须**包含：

| 标签 | 要求 |
|------|------|
| `@author` | 作者登记名（创建或首次引入该类型的开发者标识，与 Git 提交者一致） |
| `@date` | 类型**首次引入**日期，格式 `yyyy/MM/dd`（如 `2026/09/13`） |
| `@param <T>` | 泛型类型参数（有泛型时必填） |
| `@see` | 存在相关类型、契约、端点、应用模块或上下游集成点时**必填**（可多条） |

`@see` 至少覆盖以下情形（存在则不得省略）：

- 对称或镜像类型（如 `RoleEntity` ↔ `RoleSnapshot`、console 实体 ↔ base 快照）；
- 实现的接口、继承的基类、主要协作类型；
- 对外暴露的 REST 端点、SPI、插件入口或启动注册任务；
- 跨 Maven 模块的消费方/提供方（可写全限定类名或模块文档路径）。

可选：`@version` 仅在类型对外发布多版本兼容面时使用；**不要**用 `@version` 记录每次修改。

### 标签顺序

类型 Javadoc 标签顺序：

```text
正文描述
@author
@date
@param <T> …（若有）
@see …
@since …（仅当 @date 不足以表达对外 API 引入版本时）
@deprecated …（若有）
```

### 示例

```java
/**
 * Console-scoped role persistence entity. Ownership is PLATFORM, TENANT, or WORKSPACE.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.domain.identity.RoleSnapshot
 * @see com.innospots.nexus.console.role.endpoint.RoleEndpoint
 */
public class RoleEntity extends WorkspaceBaseEntity { ... }
```

```java
/**
 * An immutable paginated data container. Validates page bounds at construction
 * and provides convenience methods for pagination navigation.
 *
 * @author Smars
 * @date 2026/09/13
 * @param <T> the record type contained in this page
 * @see PageResult
 */
public record DataPage<T>(...) { ... }
```

### 其他类型

record 文档化其整体不变式，对含义、单位、可空性、安全角色或归属不显而易见的组件使用
`@param`。枚举文档化封闭概念。当业务含义不能从常量名清楚看出时，为单个枚举常量添加
Javadoc（枚举常量可省略 `@author`/`@date`，由枚举类型承担）。

注解必须文档化其预期使用位置及触发的运行时效果。异常必须文档化其代表的失败类别，而不是
列出每个调用点。

**包级** `package-info.java`：正文说明职责与边界；建议同样包含 `@author`、`@date`，以及
指向关键公共类型或模块文档的 `@see`。

## 方法注释

每个声明行为或可复用契约的 public 和 protected 方法必须有 Javadoc 块，描述：

- **做什么**（不是怎么做——代码说明怎么做）
- 每个参数的 `@param`，描述参数角色
- 返回值的 `@return`
- 任何受检或重要未受检异常的 `@throws`

当属于方法契约的一部分时，还要文档化重要的可空性、归属、变更、幂等性、线程安全、安全和生命周期要求。

以下在契约已清楚时不需要重复 Javadoc：

- record 访问器；
- 字段已文档化的平凡 JavaBean getter 和 setter；
- 枚举的 `values()` 和 `valueOf()` 方法；
- 在不增加约束、副作用或失败行为的情况下完整保留父契约的 override。

仅在有助于生成文档集时使用 `{@inheritDoc}`。不要添加空注释或复制父 Javadoc 仅为满足注释数量。若 override 改变了性能、阻塞行为、资源归属、线程安全或允许的输入，应明确文档化差异。

短 getter/setter 和平凡单行可使用单行 Javadoc（`/** 简短描述。 */`）。例如：

```java
/**
 * Returns the value for the given key, falling back to a default if absent.
 *
 * @param key          config key
 * @param defaultValue fallback value when key is not found
 * @return the configured value or the default
 */
public String get(String key, String defaultValue) { ... }
```

方法 Javadoc 标签按此顺序跟在正文之后：`@param`（含类型参数）、`@return`、`@throws`、
`@see`、`@since`、`@deprecated`。方法级**不要求** `@author`/`@date`（由类型注释登记）。
标签描述使用名词短语，除非完整句子有必要细节。

对标识符、字面量和短表达式使用 `{@code ...}`。当关系有助于读者导航契约时，使用 `{@link Type}` 或 `{@link Type#method(...)}`。不要用原始 HTML 表达标准 Javadoc 标签可表达的格式。

## 复杂逻辑的行内注释

复杂或非显而易见的代码路径必须用行注释说明**原因**，而非**是什么**。应用以下模式：

- **算法理由**：为何选择特定方法
- **边界情况**：为何存在 null 检查、回退或特殊处理
- **非显而易见副作用**：当方法效果超出其签名时
- **多步流程**：流水线各阶段的简短标记
- **生命周期与清理**：为何顺序、回滚或资源释放重要
- **并发**：保护哪个不变式，以及为何需要该同步或原子操作
- **安全**：为何掩码、复制、拒绝数据或避免写入日志
- **兼容性**：为何稳定标识符、回退或遗留行为暂不能更改

```java
// BigDecimal MVEL literal (e.g. 10.5M) — pass through without quoting
// Range operator values are left unquoted for arithmetic comparison
```

注意：过度注释显而易见的代码不可取。在可能的情况下，信任方法和变量名传达意图。

## TODO 与延后行为

TODO 仅允许用于有意延后的行为，且有清晰的未来边界。说明缺失什么，并在有用时说明应由哪个组件负责。

```java
// TODO Delegate role lookup to the role service once the service boundary exists.
throw NexusException.build(NexusStatusCode.SYSTEM_ERROR, "Role lookup is not implemented");
```

- 不要写裸 `TODO`、`FIXME`、`later` 或 `temporary` 注释。
- 不要用 TODO 为伪造成功数据、吞掉异常或不完整的安全检查找借口。
- 延后的具体 endpoint 方法必须将聚焦的 TODO 与 `NexusException.build(StatusCode)` 配对，如 endpoint 标准所要求。
- 在实现行为的同一变更中移除 TODO。

## 禁止的注释

- 不要保留注释掉的代码。版本控制保存已删除的代码。
- 不要在**方法**或行内注释中堆砌作者名；类型级 `@author`/`@date` 仅登记**首次引入**，
  不得随每次修改更新，也不得写入工单历史或变更日志。
- 不要用不同措辞重述类型、方法或代码行。
- 不要承诺实现或测试未强制执行的行为。
- 不要在示例或注释中暴露密钥、生产值、个人数据或内部攻击细节。
- 不要用注释重新定义误导性名称；按 [`naming.md`](naming.md) 重命名标识符。

## 注释评审清单

- 每个公共类型是否包含职责描述、`@author`、`@date`？
- 存在关联契约、端点、快照、模块集成时是否已写 `@see`？
- 每个公共契约是否说明了职责和重要约束？
- record 组件、枚举常量和注解在含义不显而易见时是否已文档化？
- 方法注释是否描述可观察行为而非实现？
- 非显而易见的生命周期、并发、安全和兼容性选择是否在相关代码附近解释？
- 所有 TODO 是否聚焦、可执行，并与明确的延后行为配对？
- 是否可通过改进名称或简化代码来移除任何注释？
