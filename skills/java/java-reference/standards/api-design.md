# API 设计原则

## 方法签名

- 优先使用静态工厂方法而非 public 构造函数：`of()`、`create()`、`from()`、`named()`。
- 返回不可变集合：`List.copyOf()`、`Map.copyOf()`。切勿暴露内部可变引用。
- 对应用层面向的单值结果，当其缺失属于预期结果时，使用 `Optional<T>`。面向框架的 DAO 方法在 mapper 契约允许时可返回可空实体；service/operator 边界在向外暴露前必须规范化或拒绝该缺失。
- 切勿将 `Optional` 用于参数、字段、record 组件、集合元素或集合返回值。
- 对简单的数据载体使用 `record`，自带 equals/hashCode/toString。

```java
// Good
public static <T> DataPage<T> of(List<T> records, long pageNo, long pageSize, long total) {
    return new DataPage<>(records, pageNo, pageSize, total, calculatePages(total, pageSize));
}

// Acceptable for more complex construction
public static DataRequest<T> create(String target, DataOperation operation) {
    return new DataRequest<>(target, operation);
}
```

## 契约与实现边界

当至少满足以下条件之一时，应引入接口：

- 它是公共模块、插件、适配器或 SPI 边界；
- 需要或有意支持多种实现；
- 调用方必须与运行时特定实现隔离；
- 契约拥有实现方必须遵守的生命周期或资源边界。

不要仅为便于 mock 而为每个具体类创建接口。对于单一稳定的内部实现，在出现真正的替换边界之前，应依赖具体类型。

- 契约以能力命名，例如 `ResourceStore`、`PasswordDecryptor` 或 `PluginManager`。
- 仅当契约承认存在其他有效实现时，标准实现才命名为 `DefaultXxx`。当限定词能提供信息时，使用策略特定限定词，例如 `ClasspathPluginDiscovery` 或 `RsaPasswordDecryptor`。
- 传输接口放在**领域内**的 `endpoint`（`role.endpoint`，禁止 `endpoint.role`）；可复用的非 HTTP 模块契约放在领域内 `api`；仅当该分离代表真实边界时，扩展契约才放在专门的 `contract` 包中。包树规则见 [`package-structure.md`](../references/package-structure.md)。
- 通过构造函数向具体实现注入依赖，并将必需依赖字段声明为 `final`。不要暴露依赖 setter。
- 公共契约不得返回其实现的可变状态、框架会话、DAO、实体管理器或其他基础设施内部对象。

命名细节见 [`naming.md`](naming.md)；源码构造与 Lombok 规则见 [`code-style.md`](code-style.md)。

## 不可变性

- 字段在可能的情况下应为 `final`。
- 访问器返回的集合必须是不可变副本。
- 传入构造函数的参数若为可变类型，应进行防御性复制。

```java
public final class ExecutionRecord {
    private final Map<String, Object> context;

    public ExecutionRecord(Map<String, Object> context) {
        this.context = context == null ? Map.of() : Map.copyOf(context);
    }

    public Map<String, Object> context() {
        return context;  // already immutable from constructor
    }
}
```

## 空值处理

- 对必需的 null 或无效值，使用 `NexusException` 及对应的 `StatusCode` 拒绝。
- 不要对调用方/业务校验或应用可见逻辑使用 `Objects.requireNonNull()`、`IllegalArgumentException`、`NullPointerException`、`IllegalStateException`、`RuntimeException`、`Exception` 或 `UnsupportedOperationException`。一律使用 `NexusException.build(StatusCode, …)` 或 `com.innospots.nexus.base.util.Checks`（其内部抛出 `NexusException`）。受检或框架异常仅在归属边界捕获并包装为 `NexusException`，保留 cause。
- 对可选参数优雅接受 null：
  - setter 中的 `null -> default` 模式
  - 集合构建器中的 `null -> skip` 模式
- 集合结果优先使用空不可变集合而非 `null`。
- 公共的集合返回方法不得返回 `null`。
- 不要嵌套缺失表示，例如 `Optional<List<T>>`；无值时返回空列表。
- 当泛型 mapper 签名无法表达可空性时，在 DAO 方法 Javadoc 中说明可空的框架结果。

```java
public void validateRoleCode(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
    }
}

public SimpleCondition factor(Factor factor) {
    if (factor != null) {
        factors.add(factor);
    }
    return this;
}
```

## 校验与规范化

将每条规则放在拥有该规则的最窄边界：

- record 紧凑构造函数或领域类型强制执行每个有效实例所需的不变式，并进行防御性集合复制。
- 请求的 `validate()` 方法检查不依赖持久化或其他领域的请求局部字段组合。
- operator 校验直接数据操作前置条件，并将 mapper 缺失翻译为相应状态码。
- service 校验工作流、授权、跨记录和跨领域规则。
- endpoint 仅校验无法用 Jakarta REST 绑定表达的传输关注点，然后委托应用行为。

规范化必须确定且已文档化。在值成为稳定业务键或持久化状态之前，对传输默认值、大小写、空白、分页和集合空态进行规范化。当调用方需要知道契约被违反时，不要静默修复无效值。

拒绝输入的校验方法应抛出带合适 `StatusCode` 的 `NexusException`。布尔探测使用 `isValid` 等名称，且不得修改状态。不要仅依赖数据库约束失败来表达应用可清晰表达的校验，但应保留数据库约束以保障并发下的完整性。

## 异常处理

- 所有预期的业务、应用及已翻译的基础设施失败，必须使用 `com.innospots.nexus.base.exception.NexusException`。
- 优先使用 `NexusException.build(StatusCode, ...)` 配合已有类型化状态；可复用平台失败用 `NexusStatusCode`，更窄语义用领域或技术 `XxxStatusCode`。
- 领域特定状态码枚举实现 `com.innospots.nexus.base.status.StatusCode`，位于所属领域的 `domain.enums` 包，并遵循 [`exception-status-code.md`](exception-status-code.md) 中的归属与九字符格式。
- 原始字符串码重载仅用于经校验的、白名单内的互操作或兼容边界。仓库内普通调用在类型化重载可用时不得传递复制的字面量或 `status.fullCode()`。现有原始重载调用点为迁移债务；不要新增，并在行为可保持不变时改为类型化构造。
- 不要为每个业务错误创建单独的异常子类。通过 `StatusCode` 实现区分业务失败。
- 所有调用方/业务校验、状态检查和应用前置条件必须抛出带对应状态码的 `NexusException`。业务模块与新增 `base` 公共 API **禁止**以 JDK 异常作为失败契约。
- 在拥有翻译权的边界包装受检或提供方异常，保留原始 cause。除非有更具体状态码的充分理由，否则应原样重抛已有 `NexusException`。
- 切勿静默捕获 `Exception`、返回伪造成功，或在每一层都记录并重抛同一失败。保留中断与取消语义，不要例行捕获 `Throwable`。
- endpoint 基础设施将 `NexusException` 集中映射为 `R.fail(...)` 或 `R.from(...)`；service 和 operator 方法返回领域值而非传输包装或堆栈跟踪。传输适配器单独解析状态的 HTTP 映射，因为 `R<T>` 不携带 HTTP 状态字段。

完整分类、捕获/翻译规则、状态结构、归属、扩展流程及契约测试要求见 [`exception-status-code.md`](exception-status-code.md)。

```java
if (role == null) {
    throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND);
}
```

## 持久化实体

- 每个 JPA/MyBatis-Plus 持久化实体必须继承 `com.innospots.nexus.core.persistence.entity.BaseEntity`、`TenantBaseEntity`、`WorkspaceBaseEntity`，或——仅当设计评审明确批准项目级隔离时——`ProjectBaseEntity`。
- **新业务实体的默认选择是 `WorkspaceBaseEntity`。**
- 对工作空间范围（tenant + workspace）的记录使用 `WorkspaceBaseEntity`。对租户范围但非工作空间范围的记录使用 `TenantBaseEntity`。仅当需求明确标识数据为平台级或 realm 全局时，才使用 `BaseEntity`（用户、凭证、服务注册等）。
- 仅当领域需要在工作空间内进行项目级数据隔离且设计评审记录了该决策时，才使用 `ProjectBaseEntity`。不要向未继承 `ProjectBaseEntity` 的实体添加裸 `projectId` 列。项目定义记录（例如 `ProjectEntity`）通常仍使用 `WorkspaceBaseEntity`。
- 不要在具体实体中重复审计字段（`createdAt`、`updatedAt`、`createdBy`、`updatedBy`）。它们继承自 `BaseEntity`。
- 不要在具体作用域实体中重复 `tenantId` 或 `workspaceId`。
- 具体持久化实体主键必须是带 `@TableId(type = IdType.ASSIGN_UUID)`、`@Id` 和 `@Column(length = 32, nullable = false)` 的 `String` 字段。
- 每个持久化实体必须使用 Jakarta Persistence `@Entity`，并用 `@Table` 声明表，包括唯一查找、外键查找和常见分页查询过滤所需的显式索引。
- 每个持久化字段必须使用适用的 Jakarta Persistence 映射注解，包括主键的 `@Id` 和普通列的 `@Column`。
- 字符串列长度必须是 2 的幂，例如 `16`、`32`、`64`、`128`、`256`、`512` 或 `1024`。有意无界的文本使用 `@Lob`，不要选择任意大长度。
- 每个持久化实体必须声明 `public static final String TABLE_NAME` 常量，并在 Jakarta Persistence `@Table(name = EntityType.TABLE_NAME)` 和 MyBatis-Plus `@TableName(EntityType.TABLE_NAME)` 中使用该常量。
- 持久化实体类是非 record 领域类，因此必须使用 Lombok `@Getter` 和 `@Setter`。
- 实体主键使用 `IdType.ASSIGN_UUID`。每个实体必须重写 `BaseEntity.idPrefix()`，使用简短稳定的领域前缀。共享的 `DbPrimaryGenerator` 通过 `IdGenerator.ulid(prefix)` 生成 ID；operator 不得手动分配生成的主键。
- 具体持久化实体应为唯一查找字段、外键查找字段和分页查询过滤字段声明必要的 JPA `@Table(indexes = ...)` 注解。索引名称应显式且带表前缀。

```java
@Getter
@Setter
@Entity
@Table(name = RoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_code", columnList = "role_code", unique = true)
})
@TableName(RoleEntity.TABLE_NAME)
public class RoleEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_role";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String roleId;

    @Column(length = 64, nullable = false)
    private String roleCode;
}
```

## 领域模型

- 业务数据类型属于 `domain`，按职责分为 `entity`、`request`、`vo`、`model` 和 `enums`。
- 请求和 VO 类型必须是 record。请求名以 `Request` 结尾，响应视图名以 `Vo` 结尾。
- 实体名以 `Entity` 结尾。内部模型名表达实际业务概念，无需技术后缀。
- 领域类型可拥有校验、计算、不变式和状态转换。当行为属于对象时，优先使用行为丰富的模型；不要仅为保持领域对象为纯数据结构而将所有业务逻辑移到 service。
- 配置文件绑定对象及其他系统配置类型属于模块级 `config` 包，不属于 `domain`。

## REST Endpoint 契约

- HTTP API 边界必须使用 `*Endpoint` 后缀，并放在 `endpoint` 包中。
- 新领域初始化默认使用具体 endpoint 类。仅当开发者明确要求独立传输契约时才声明接口。
- **`innospots-nexus-console` 例外：** 管理台 REST **传输契约**可声明为 `interface *Endpoint`，由 `kernel` 或 `platform` 提供实现类。这不属于「仅为 mock 建接口」；须满足：契约与 VO 留在 console、实现不含 console 业务工作流、路径与 `R<T>` 形状在 console 锁定。详见 [`api-contract.md`](../references/api-contract.md)「Console 传输契约」。
- 使用 Jakarta REST（`jakarta.ws.rs`）注解声明资源路径、HTTP 方法、媒体类型和请求参数。
- 保持 endpoint 签名面向传输。将校验、编排、事务处理和持久化委托给 service 或 operator 边界。
- 有意延后的具体方法必须包含聚焦的 `TODO`，并抛出 `NexusException.build(合适的 StatusCode)`（如暂无专用码可用 `NexusStatusCode.SYSTEM_ERROR`），而非 `UnsupportedOperationException` 或返回伪造数据。
- 保留 `api` 包用于非 HTTP 公共模块契约；不要将 Jakarta REST endpoint 放在那里。
- 每个 endpoint 返回类型必须是 `com.innospots.nexus.base.domain.response.R<T>`。
- 普通载荷返回 `R<XxxVo>`，分页载荷返回 `R<PageResult<XxxVo>>`，无响应数据的操作返回 `R<Void>`。
- `R` 是传输边界包装。service 和 operator 方法必须直接返回领域值或 `PageResult<T>`，不要用 `R` 包装结果。
- 通过 endpoint 运行时的集中异常处理将 `NexusException` 转换为 `R.fail(...)`。不要在每个 endpoint 方法中重复异常到响应的映射。

## DAO 契约

- MyBatis-Plus mapper 接口必须放在 `dao` 下，使用 `*Dao` 后缀，并继承 `BaseMapper<EntityType>`。
- 在引入自定义方法之前，优先使用继承的 `BaseMapper` 方法。
- 自定义动态查询和更新应实现为 `default` 方法，使用 `LambdaQueryWrapper` 或 `LambdaUpdateWrapper`，优先通过 `Wrappers.lambdaQuery()` 和 `Wrappers.lambdaUpdate()` 创建。
- 每个 DAO 方法必须只访问一张表。
- 禁止在 DAO 方法、注解 SQL、mapper XML 及其他持久化语句中使用 SQL join。
- 仅当显式单表查询比 wrapper 更清晰时，才使用 `@Select` 等注解 SQL。
- 不要创建 MyBatis mapper XML 文件或 XML 语句定义。
- 业务与应用配置使用 `resources/**/*.yaml` + Java `@Configuration` / 配置绑定类；**禁止**新增业务级 `*.properties`、`beans.xml` 及 XML 装配（见 [`code-style.md`](code-style.md)「配置与资源文件」）。
- 每个持久化业务表对应一个 `*Dao`；Dao 过大时拆表/拆 Dao 或上提 Operator，勿用 XML 堆砌（细则见 [`persistence-config.md`](../references/persistence-config.md)）。
- DAO 方法应仅表达直接数据库操作。跨 DAO 协调和业务决策属于 operator 或 service 类型。
- 跨表读取必须使用独立批量查询，并在 operator 或 service 中组装结果。先收集标识符或稳定键，再批量查询各表，最后在内存中映射。
- 禁止 N+1 查询模式。不要为每条记录发起一次关联表查询。
- 跨表写入、关系完整性、级联和稳定键传播属于事务性 service。

## Service 与 Operator 边界

- operator 实现直接面向数据的操作。operator 通常只使用一个 DAO，但当操作仍简单且内聚时，可使用多个 DAO。
- service 实现复杂工作流、跨领域协调、校验以及跨多个 operator 或 DAO 的编排。
- service 可依赖 operator 和 DAO。
- operator 不得依赖 service 或其他 operator。其业务数据依赖限于 DAO；也可使用无状态 converter 和共享底层技术工具。
- 当逻辑需要协调多个 operator 时，应在 service 中定义，而不是允许 operator 之间的依赖。
- 分页的 service 和 operator 方法必须返回 `com.innospots.nexus.base.domain.response.PageResult<T>`。
- service 和 operator 方法不得返回 endpoint 包装 `R<T>`。

## 查询与命令语义

方法名和返回类型必须使操作形态可预测。权威动词词汇见 [`naming.md`](naming.md)。

- 查询不修改业务状态。`find` 返回可选单值结果，`list` 返回有限集合，`page` 返回 `PageResult<T>`，`count` 返回数字。
- 命令使用精确的业务动词，并返回调用方继续所需值。不要仅因持久化框架产出了实体就返回实体。
- `create` 操作在稳定键重复时必须失败，除非其契约明确为幂等。不要静默将 create 重新解释为 update。
- `update` 仅修改已文档化的可变属性。不可变的稳定键不得出现在 update 请求中。
- `replace` 将提供的值或关联集视为完整集合。必须定义省略是否移除现有成员。
- `delete` 必须定义缺失目标是成功还是 not-found 失败。在同一公共资源边界内一致应用该选择。
- `register`、`subscribe`、`start`、`stop`、`close` 及类似生命周期操作必须文档化重复调用行为。
- 方法不得用类似属性的名称隐藏昂贵的 I/O、阻塞、发布或持久化。

在重试属于正常边界行为时使用幂等性，包括声明式同步和注册。幂等操作在相同有效输入重复时产生相同的外部可见状态；不必返回同一对象实例。用稳定键和数据库/运行时唯一性保护幂等性，不要仅依赖读-写检查。

## 领域事件与 EventBus

- `com.innospots.nexus.base.events.EventBus` 是用于领域事件发布与订阅的进程内事件总线。
- 使用领域事件解耦协作方，而不改变允许的模块依赖方向。事件发布不是同级业务模块相互依赖的理由。
- 当消费者可合法依赖该领域时，发布业务领域拥有事件契约。放在发布方的 `domain.event` 包下，命名为 `XxxEvent`，并实现 `com.innospots.nexus.base.events.DomainEvent`。
- kernel 和 platform 等并行业务模块不得相互导入对方的事件类型。通过可同时依赖两者的 application/adapter 模块协调，或仅在契约真正业务中立时通过刻意共享的较低层契约协调。不要仅为绕过依赖规则而将具体业务事件移到 core 或 console。
- 领域事件应为不可变 record，仅包含消费者所需数据。不要通过事件暴露 DAO、service、可变实体或基础设施对象。
- 允许引用事件契约的消费者在其自身领域的 `handler` 包中定义 `XxxEventHandler` 类型，并实现 `EventHandler<XxxEvent>`。
- 发布方仅依赖事件契约和 `EventBus`；不得依赖消费者 handler 或消费者实现类型。
- 仅在原始状态变更成功完成后发布事件。不要在写入或事务仍可能失败之前发布成功事件。
- 对不需要返回值的跨模块通知式操作使用 `EventBus.publish(event)`。
- 仅当调用方真正需要即时 handler 结果时才使用 `EventBus.publishSync(event)`。不要用同步事件在模块间重建直接 service 调用。
- 事件 handler 可委托给其自身模块的 service 或 operator。handler 失败必须抛出带对应 `StatusCode` 的 `NexusException`。
- 事件 handler 必须通过所属模块生命周期边界上的 `EventBus.subscribe(...)` 和 `EventBus.unsubscribe(...)` 注册和注销。
- 注册拥有方负责清理。返回的 `Subscription` 或等效句柄在所属组件停止时必须关闭/取消订阅。
- 事件载荷和事件类型字符串是兼容性契约。兼容地添加数据，不要作为内部重构的一部分重命名已发布的事件类型。

```java
public record RoleCreatedEvent(String roleId, String roleCode)
        implements DomainEvent {

    @Override
    public String eventType() {
        return "role.created";
    }
}

public final class RoleCreatedEventHandler
        implements EventHandler<RoleCreatedEvent> {

    @Override
    public Object handle(RoleCreatedEvent event) {
        // Delegate to this consumer module's service or operator.
        return null;
    }
}
```

## 领域转换

- 请求、VO、model 和 entity 类型之间的非平凡或重复结构转换必须使用 MapStruct。
- 业务领域 converter 接口属于 `converter`，使用 `*Converter` 后缀，并声明 `@Mapper(config = BaseMapperConfig.class)`。
- model 与 entity 之间的 converter 应继承 `BaseBeanConverter<ModelType, EntityType>`，并按需添加其他转换方法。
- 将批量字段复制和重复的 Domain POJO 转换逻辑移出 endpoint、service 和 operator 类。
- 对一两个标量值的本地、明显且不重复的映射，直接构造可接受。不要仅为隐藏该规模的构造函数调用而创建 converter。
- 领域拥有的行为、校验、计算和标量格式化不是 bean 转换，可保留在领域类型上。

## 密码注册请求

- 密码注册请求对象必须携带前端加密后的密码载荷，不得包含密码哈希、盐、算法或密码策略版本字段。
- 通过公共模块接口解密前端密码载荷，以便在不更改请求对象的情况下替换或扩展传输加密方式。
- 仅在解密前端载荷并使用 `innospots-nexus-base` 加密工具哈希后，才存储服务端密码哈希。
- 密码哈希工具必须支持外部提供的盐值，注册持久化应存储用于生成哈希的盐。

## 分页请求

- 共享分页查询请求对象属于 `domain.request`。
- 模块特定分页查询请求在需要公共 `input`、`pageNo` 和 `pageSize` 字段时，应内联或组合 `com.innospots.nexus.base.domain.request.SimpleQueryRequest`。
- 分页查询方法应接受请求对象，而不是分开的页码、页大小和过滤参数。

## 事务边界

- 执行多个 DAO 写入或协调多表写入的方法必须声明 `jakarta.transaction.Transactional`。
- 仅使用 `jakarta.transaction.Transactional`；不要使用 Spring 的 `org.springframework.transaction.annotation.Transactional`。
- 简单单表读取保持在事务外，除非有具体一致性要求需要事务读。
- 优先在最小写入操作上使用方法级 `@Transactional`，而不是默认注解整个类。
- 事务 API 版本属于 `innospots-nexus-bom`；模块必须依赖 `jakarta.transaction-api`，不得内联版本。

## 流式 API

- 可变对象上类似 setter 的方法应返回 `this` 以支持链式调用。
- 仅在可证明转换安全时使用 `@SuppressWarnings("unchecked")`，并说明原因。

```java
public UiDatasource param(String key, Object value) {
    if (key != null) {
        params.put(key, value);
    }
    return this;
}
```

## 生命周期与资源归属

拥有线程、执行器、订阅、类加载器、调度器、网络客户端或其他可关闭资源的类型，必须暴露并文档化清晰的生命周期。

- 构造建立有效的本地状态，但不得静默启动长期后台工作，除非类型的工厂契约如此规定。
- `initialize` 准备依赖和注册；`start` 开始活跃工作；`stop` 停止工作并在支持时保留可重启状态；`destroy` 或 `close` 永久释放资源。
- 生命周期操作定义允许的状态和重复调用行为。优先安全的幂等清理。
- 创建或注册资源的类型拥有其清理责任，除非 API 明确转移归属。
- 存在依赖时，按获取的逆序释放资源。
- 不要吞掉清理失败。保留主要失败，并对次要清理失败附加或记录足够诊断上下文。
- 不要在对应状态转换成功之前发布 started/stopped 成功事件。

当词法或显式清理能提高正确性时，使用 `AutoCloseable` 或聚焦句柄如 `Subscription`。避免 finalizer，不要依赖垃圾回收释放外部资源。

## 线程安全与并发

- 线程边界优先使用不可变 record 和快照。
- 可变公共类型必须说明其是否线程安全、限于单线程，或在可能并发使用时需要外部同步。
- 用一个清晰的同步策略保护一个不变式。不要在不解释各机制保护哪些状态的情况下混用 synchronized 块、原子类和并发集合。
- 不要在持有内部锁时调用未知的插件、事件 handler 或回调代码。先复制所需注册，再调用回调。
- 协调多个字段的状态转换，从调用方视角必须是原子的。
- 返回不可变快照，而非注册表、路由表、配置或指标的活可变视图。
- 必须传播或刻意恢复取消和中断；不要静默消费 `InterruptedException`。

## 公共契约兼容性

当以下表面跨越模块、插件、持久化或传输边界时，将其视为公共兼容性表面：

- 公共类型名、包、方法签名、泛型边界和声明语义；
- REST 路径、参数名、请求/响应字段、枚举值和状态码；
- 表/列名、稳定业务键、实体 ID 前缀和索引支撑的唯一性假设；
- 事件类型字符串、配置键、插件 ID、能力键、标签名和序列化字段名。

不要将这些表面之一作为机械重命名或内部重构来更改。先文档化迁移、兼容适配器、数据迁移或版本边界。增量变更仍必须为旧调用方和持久化数据定义默认值。

弃用同时使用 `@Deprecated` 和 Javadoc `@deprecated`，标明替代项，并在明确约定的兼容期内保留。不要在没有移除决策的情况下无限期保留过时别名。

## API 评审清单

- 抽象是否有真实的契约边界，还是机械地添加了接口？
- null、缺失、空集合、归属和变更是否明确？
- 校验是否放在拥有该规则的边界？
- 查询和命令名称是否与其结果和副作用匹配？
- 事务、幂等性、生命周期、清理和重复调用是否已定义？
- 并发访问是否安全或受到明确约束？
- 领域事件是否仅在状态变更成功后发布，并由注册拥有方清理？
- 是否已检查每个受影响的公共标识符的兼容性影响？
