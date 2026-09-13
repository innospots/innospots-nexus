# 代码风格

## 大括号

所有 `if`、`else`、`for`、`while` 块必须使用大括号 `{}`，即使是单语句体。条件后不得在同一行写裸语句。

```java
// Correct
if (condition) {
    doSomething();
}

// Incorrect
if (condition) doSomething();
if (condition) doSomething();
```

## 缩进

使用 4 空格缩进。不使用 Tab。

```java
public class Example {
    public void method() {
        if (condition) {
            for (int i = 0; i < 10; i++) {
                process(i);
            }
        }
    }
}
```

## 行宽

保持合理的行宽，优先最大 120 字符。

```java
// Lines exceeding 120 chars should be broken:
return NexusException.build("SOME_LONG_CODE",
        "A descriptive message that would otherwise exceed the line limit");
```

## 导入顺序

1. `java.*` / `javax.*` — 标准库（按字母排序）
2. 第三方导入（按字母排序）
3. `com.innospots.*` — 项目导入（按字母排序）

组之间用空行分隔。禁止通配符导入。

```java
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
```

## 源文件组织

按可预测顺序排列成员，使读者无需扫描整个文件即可找到契约和状态：

1. 静态常量；
2. 静态可变状态（仅当设计明确要求时）；
3. 实例字段；
4. 构造函数和静态工厂；
5. public 方法；
6. protected 方法；
7. private 方法；
8. 嵌套类型。

组内保持相关成员在一起，生命周期方法按执行顺序排列，如 `initialize`、`start`、`stop`、`destroy`。不要机械排序而将 public 操作与其小型 private 辅助方法分离。

- 每条语句声明一个字段或局部变量。
- 声明有多个注解时，每个注解单独一行。
- 声明紧跟其注解和 Javadoc 之后；不要在它们之间插入空行。
- 尽可能缩小变量作用域，在首次使用附近声明。
- 对构造后不再替换的依赖和状态，优先使用 `final` 字段。
- 避免可变静态状态。当需要进程级运行时状态时，将其生命周期和线程安全封装在具名类型中。

## 多行格式

- 方法声明或调用超出行宽限制时按参数换行。续行缩进 8 空格。
- 流式链的点在续行行首。当链表达多个过滤或映射阶段时，每行一个操作。
- 多行 record 头每行一个组件，闭合括号与声明对齐。
- 仅当表达式立即可读时保持短 lambda 内联。多语句或需要注释时使用块 lambda。
- 编译器可推断泛型参数时使用菱形运算符。
- 避免嵌套三元表达式。分支不明显时使用命名局部变量或普通条件语句。

```java
public record PluginRuntimeInfo(
        String pluginId,
        PluginState state,
        Instant startedAt
) {
}

return registrations.stream()
        .filter(CapabilityRegistration::active)
        .map(CapabilityRegistration::provider)
        .toList();
```

## Lombok 使用

- 使用构造函数注入的实现类应优先使用 `@RequiredArgsConstructor` 配合 `final` 依赖字段，而不是手写样板构造函数。
- operator 和 service 类必须使用 Lombok `@Slf4j` 记录日志。不要声明手写 logger 字段，不要使用错误的 `@Sl4j` 拼写。
- 可变持久化实体必须使用 Lombok `@Getter` 和 `@Setter`。可变配置文件绑定对象和声明式 UI/规格对象在绑定框架需要 JavaBean 访问器时也应使用相同注解。
- 内部领域模型不自动为可变。不可变或行为导向的模型应仅暴露其契约所需的访问器和状态变更；视情况使用不带 `@Setter` 的 `@Getter`、record 或显式行为方法。
- `domain.request` 和 `domain.vo` 下的类型必须是 Java record，因此不适用 Lombok `@Getter` 和 `@Setter`。
- Lombok 导入放在第三方导入组，位于 `com.innospots.*` 项目导入之前。
- Lombok 仅消除访问器样板。领域类仍应暴露校验、状态转换和业务行为的显式方法。
- 不要在领域或持久化类型上使用 `@Data`。它对实体和安全敏感对象生成的相等性、字符串、构造函数和变更行为过于宽泛。
- 不要对凭证、密钥、令牌、密码材料或其他敏感值使用 Lombok 生成的 `toString` 行为。

## 领域类型

- `domain.request` 和 `domain.vo` 类型必须声明为 record。
- 请求和 VO record 可使用紧凑构造函数、静态工厂和实例方法校验或派生值。
- 实体和 model 类可封装自身的不变式、校验、计算和状态转换。不要强迫领域类型成为无行为的数据持有者。

```java
public record RoleCreateRequest(String roleName, String roleCode) {

    /**
     * Validates required role attributes.
     */
    public void validate() {
        if (roleName == null || roleName.isBlank()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
    }
}
```

## REST 端点

- REST API 边界属于 `endpoint` 包，必须使用 `*Endpoint` 后缀。
- 新领域初始化默认使用具体 endpoint 类。仅当开发者明确要求独立传输契约时才声明 endpoint 接口。
- 使用 `jakarta.ws.rs` 注解声明 REST。不要在 endpoint 契约中使用 Spring MVC 注解，如 `@RestController`、`@RequestMapping`、`@GetMapping` 或 `@PostMapping`。
- 将资源级 `@Path` 和共享 `@Produces`/`@Consumes` 放在 endpoint 类型上。将 HTTP 方法注解和方法特定路径放在对应方法上。
- 优先使用显式参数注解，如 `@PathParam`、`@QueryParam`、`@HeaderParam` 和 `@BeanParam`。不要依赖运行时特定的隐式参数绑定。
- endpoint 类型仅定义传输边界。业务工作流和持久化操作属于 service、operator 和 DAO 类型。
- 当行为有意延后时，具体 endpoint 方法必须使用聚焦的 `TODO` 并抛出 `NexusException.build(合适的 StatusCode)`。
- 每个 endpoint 方法必须返回 `com.innospots.nexus.base.domain.response.R<T>`。
- 普通响应使用 `R<XxxVo>`，分页响应使用 `R<PageResult<XxxVo>>`，无需响应载荷时使用 `R<Void>`。

```java
@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleEndpoint {

    /**
     * Returns one role.
     *
     * @param roleId role identifier
     * @return role details
     */
    @GET
    @Path("/{roleId}")
    public R<RoleVo> getRole(@PathParam("roleId") String roleId) {
        // TODO Implement role lookup through the role service.
        throw NexusException.build(NexusStatusCode.SYSTEM_ERROR, "Role lookup is not implemented");
    }
}
```

## MapStruct 转换器

- **注意：** MapStruct 的 `@Mapper(config = BaseMapperConfig.class)` 仅用于**对象映射**，
  与 MyBatis 的 Mapper 接口 / `mapper.xml` **无关**；持久化访问只用 `*Dao extends BaseMapper<Entity>`。
- Domain POJO（含 request、VO、model 和 entity 类型）之间的非平凡结构转换必须使用 MapStruct。
- 将业务 converter 放在业务领域的 `converter` 包，命名为 `*Converter`。
- 每个 converter 必须使用 `@Mapper(config = BaseMapperConfig.class)`。
- model 到 entity 的 converter 应继承 `BaseBeanConverter<ModelType, EntityType>`，并可添加 request、VO 或其他领域转换方法。
- 使用 MapStruct 生成的集合转换或 `BaseBeanConverter` 继承的列表转换方法。
- endpoint、service 和 operator 类不得包含大块逐字段复制或重复的 Domain POJO 转换逻辑。
- 对一两个标量值的本地、明显且不重复的映射，直接构造可接受。小规模标量转换和领域对象内在行为不需要 converter。

```java
@Mapper(config = BaseMapperConfig.class)
public interface RoleConverter extends BaseBeanConverter<Role, RoleEntity> {

    RoleVo modelToVo(Role model);

    Role requestToModel(RoleCreateRequest request);
}
```

## MyBatis-Plus DAO

- DAO 类型属于 `dao` 包，使用 `*Dao` 后缀，并继承 `BaseMapper<EntityType>`。
- 对直接操作优先使用继承的 `BaseMapper` CRUD 方法。
- 将可复用自定义操作定义为 Java `default` 方法。用 `Wrappers.lambdaQuery()` / `LambdaQueryWrapper` 构建动态谓词，用 `Wrappers.lambdaUpdate()` / `LambdaUpdateWrapper` 构建更新。
- 构建 wrapper 时优先使用 lambda 方法引用，如 `RoleEntity::getRoleCode`，而非字符串列名。
- 每个 DAO 方法必须只访问一张表。禁止 SQL join，包括注解 SQL 和 mapper XML。
- 仅当单表查询作为显式 SQL 更清晰或无法用 lambda wrapper 清晰表达时，才使用 MyBatis 注解 SQL，尤其是 `@Select`。
- 禁止 mapper XML 文件和基于 XML 的语句。
- 将事务编排和多 DAO 业务工作流保持在 DAO 接口之外。
- 在 operator 或 service 中通过独立批量查询组装跨表结果。禁止 N+1 查询。

```java
public interface RoleDao extends BaseMapper<RoleEntity> {

    /**
     * Finds a role by its stable code.
     *
     * @param roleCode stable role code
     * @return matching role or {@code null}
     */
    default RoleEntity selectByRoleCode(String roleCode) {
        return selectOne(Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getRoleCode, roleCode));
    }

    // Relationship identifiers and role rows are queried separately in batches.
}
```

## 配置与资源文件

- 业务与应用配置放在 `src/main/resources` 下的 **`*.yaml` / `*.yml`**（如 `application.yaml`、`config/<domain>.yaml`）。
- **禁止**新增业务/应用级 `*.properties` 配置文件。测试专用 `src/test/resources` 可沿用既有 properties，但新测试配置优先 yaml。
- **禁止** Spring `beans.xml`、`mybatis-config.xml` 及任何用 XML 装配 Bean 或定义 SQL 的方式。
- Bean 与中间件绑定使用 Java `@Configuration` / `@Bean`；MyBatis-Plus 使用 Java 配置类，不用 XML mapper。
- 配置绑定类型放在模块级 `config` 包（`com.innospots.nexus.<module>.config`），不放在 `domain` 下。
- 新增配置键属于公共兼容面；废弃须 `@Deprecated` + 文档化迁移。

操作细则见 [`references/persistence-config.md`](../references/persistence-config.md)。

## 依赖字段与构造

- 优先使用带 `final` 字段的构造函数注入。依赖必须在构造函数契约中可见，且创建后不得替换。
- 对产生预期构造函数的普通 service、operator、handler 和运行时实现类使用 `@RequiredArgsConstructor`。
- 当参数需要校验、防御性复制、规范化或 Lombok 无法清晰表达的说明时，使用显式构造函数。
- 不要使用字段注入或可变的 public 依赖字段。
- 保持可选协作方明确。不要仅为简化测试而用可空字段表示必需依赖。

```java
@Slf4j
@RequiredArgsConstructor
public final class PermissionGrantService {

    private final PermissionGrantDao permissionGrantDao;
    private final PermissionResourceDao permissionResourceDao;
}
```

## 集合与状态

- 当缺失与空态含义相同时，优先使用不可变空集合（`List.of()`、`Set.of()`、`Map.of()`）而非 `null`。
- 在构造或边界入口用 `List.copyOf`、`Set.copyOf` 或 `Map.copyOf` 防御性复制调用方拥有的集合。
- 不要从访问器暴露内部可变集合。根据 API 契约返回不可变视图或快照。
- 使用传达语义的集合类型：`Set` 表示唯一性，`List` 表示稳定顺序和重复，`Map` 表示键查找。
- 当输出、依赖解析、路由优先级或测试依赖时，保持确定性迭代顺序。
- 将变更限制在拥有状态的类型内。不要在层之间传递可变集合并依赖未文档化的共享变更。

## 日志与诊断

- 在记录日志的具体 service、operator、handler、manager 及其他运行时类中使用 Lombok `@Slf4j`。不要声明手写 logger 字段。
- 不要在生产代码中使用 `System.out`、`System.err` 或 `Throwable#printStackTrace`。
- 记录稳定标识符和生命周期转换，而非完整领域对象。切勿记录密码、密钥、令牌、解密载荷或敏感配置值。
- 使用参数化日志（`log.info("Started plugin {}", pluginId)`）而非字符串拼接。
- 不要在每一层都既记录又重抛同一失败。在添加上下文有用或最终处理失败处记录。

## 字面量与表达式

- 为重复或领域重要的字面量赋予命名常量。显而易见且局部的字面量（如空态检查中的零）不需要常量。
- 对封闭领域状态和类型使用枚举常量，不要在业务代码中比较未解释的字符串。
- 保持表达式可读。当条件混合多个业务规则时，提取命名谓词或局部变量。
- 按身份比较枚举（对 null 安全）：`state == PluginState.ACTIVE`。对可空 String，从已知非空字面量调用 `equals`，如 `"ACTIVE".equals(status)`。
- 不要用注释弥补不清晰的表达式；先命名概念，再仅注释非显而易见的原因。

架构边界、空值处理、不可变性契约和异常行为见 [`api-design.md`](api-design.md)。标识符和成员命名见 [`naming.md`](naming.md)。
