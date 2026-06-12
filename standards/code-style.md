# Code Style

## Braces

All `if`, `else`, `for`, `while` blocks must use braces `{}`, even for
single-statement bodies. No bare statements on the same line as the
condition.

```java
// Correct
if (condition) {
    doSomething();
}

// Incorrect
if (condition) doSomething();
if (condition) doSomething();
```

## Indentation

Use 4-space indentation. No tabs.

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

## Line Width

Keep line width reasonable, prefer 120 characters maximum.

```java
// Lines exceeding 120 chars should be broken:
return NexusException.build("SOME_LONG_CODE",
        "A descriptive message that would otherwise exceed the line limit");
```

## Import Order

1. `java.*` / `javax.*` — standard library (sorted alphabetically)
2. Third-party imports (sorted alphabetically)
3. `com.innospots.*` — project imports (sorted alphabetically)

Separate groups with a blank line. No wildcard imports.

```java
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
```

## Lombok

- Implementation classes that use constructor injection should prefer
  `@RequiredArgsConstructor` with `final` dependency fields instead of manually
  writing boilerplate constructors.
- Operator and service classes must use Lombok `@Slf4j` for logging. Do not
  declare hand-written logger fields, and do not use the invalid `@Sl4j`
  spelling.
- Every concrete non-`record` class under a `domain` package must use Lombok
  `@Getter` and `@Setter`. Do not write JavaBean getters and setters manually.
- Types under `domain.request` and `domain.vo` must be Java records, so Lombok
  `@Getter` and `@Setter` do not apply to them.
- Keep Lombok imports in the third-party import group, before
  `com.innospots.*` project imports.
- Lombok only removes accessor boilerplate. Domain classes should still expose
  explicit methods for validation, state transitions, and business behavior.

## Domain Types

- `domain.request` and `domain.vo` types must be declared as records.
- Request and VO records may use compact constructors, static factories, and
  instance methods to validate or derive values.
- Entity and model classes may encapsulate their own invariants, validation,
  calculations, and state transitions. Do not force domain types to be
  behavior-free data holders.
- Internal `domain.model` types should be named after business concepts rather
  than technical roles or transport concerns.

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

## REST Endpoints

- REST API boundaries belong in an `endpoint` package and must use the
  `*Endpoint` suffix.
- New domain initialization uses concrete endpoint classes by default.
  Declare an endpoint interface only when a developer explicitly requires a
  separate transport contract.
- Use `jakarta.ws.rs` annotations for REST declarations. Do not use Spring MVC
  annotations such as `@RestController`, `@RequestMapping`, `@GetMapping`, or
  `@PostMapping` in endpoint contracts.
- Put the resource-level `@Path` and shared `@Produces`/`@Consumes`
  declarations on the endpoint type. Put HTTP method annotations and
  method-specific paths on the corresponding methods.
- Prefer explicit parameter annotations such as `@PathParam`, `@QueryParam`,
  `@HeaderParam`, and `@BeanParam`. Do not rely on runtime-specific implicit
  parameter binding.
- Endpoint types define transport boundaries only. Business workflows and
  persistence operations belong in service, operator, and DAO types.
- When behavior is intentionally deferred, concrete endpoint methods must use
  a focused `TODO` and throw `UnsupportedOperationException`.
- Every endpoint method must return
  `com.innospots.nexus.base.domain.response.R<T>`.
- Use `R<XxxVo>` for ordinary responses, `R<PageResult<XxxVo>>` for paginated
  responses, and `R<Void>` when no response payload is required.

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
        throw new UnsupportedOperationException("Role lookup is not implemented");
    }
}
```

## MapStruct Converters

- All structural conversion between Domain POJOs, including request, VO,
  model, and entity types, must use MapStruct.
- Place business converters in the business domain's `converter` package and
  name them `*Converter`.
- Every converter must use `@Mapper(config = BaseMapperConfig.class)`.
- Model-to-entity converters should extend
  `BaseBeanConverter<ModelType, EntityType>` and may add request, VO, or other
  domain conversion methods.
- Use MapStruct-generated collection conversions or the list conversion
  methods inherited from `BaseBeanConverter`.
- Endpoint, service, and operator classes must not contain large blocks of
  field-by-field copying or repeated Domain POJO conversion logic.
- Small scalar transformations and behavior intrinsic to a domain object do
  not require a converter.

```java
@Mapper(config = BaseMapperConfig.class)
public interface RoleConverter extends BaseBeanConverter<Role, RoleEntity> {

    RoleVo modelToVo(Role model);

    Role requestToModel(RoleCreateRequest request);
}
```

## MyBatis-Plus DAOs

- DAO types belong in a `dao` package, use the `*Dao` suffix, and extend
  `BaseMapper<EntityType>`.
- Prefer inherited `BaseMapper` CRUD methods for straightforward operations.
- Define reusable custom operations as Java `default` methods. Build dynamic
  predicates with `Wrappers.lambdaQuery()` / `LambdaQueryWrapper` and updates
  with `Wrappers.lambdaUpdate()` / `LambdaUpdateWrapper`.
- Prefer lambda method references such as `RoleEntity::getRoleCode` over string
  column names when constructing wrappers.
- Every DAO method must access one table only. SQL joins are prohibited,
  including annotation SQL and mapper XML.
- Use MyBatis annotation SQL, especially `@Select`, only when a single-table
  query is clearer as explicit SQL or cannot be expressed cleanly with a
  lambda wrapper.
- Mapper XML files and XML-based statements are prohibited.
- Keep transaction orchestration and multi-DAO business workflows outside DAO
  interfaces.
- Assemble cross-table results from separate batch queries in an operator or
  service. N+1 queries are prohibited.

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
