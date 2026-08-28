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

## Source File Organization

Keep members in a predictable order so a reader can find contracts and state
without scanning the entire file:

1. static constants;
2. static mutable state, only when the design explicitly requires it;
3. instance fields;
4. constructors and static factories;
5. public methods;
6. protected methods;
7. private methods;
8. nested types.

Within a group, keep related members together and order lifecycle methods in
execution order, such as `initialize`, `start`, `stop`, `destroy`. Do not sort
methods mechanically when doing so separates a public operation from its small
private helpers.

- Declare one field or local variable per statement.
- Put one annotation per line when a declaration has multiple annotations.
- Keep the declaration immediately after its annotations and Javadoc; do not
  insert blank lines between them.
- Keep variable scope as narrow as practical and declare a value near its first
  use.
- Prefer `final` fields for dependencies and state that is not replaced after
  construction.
- Avoid mutable static state. When process-wide runtime state is necessary,
  encapsulate its lifecycle and thread-safety in a specifically named type.

## Multiline Formatting

- Break a method declaration or invocation by parameter when it exceeds the
  line-width limit. Indent continuation lines by 8 spaces.
- Put a fluent chain's dots at the beginning of continuation lines. Keep one
  operation per line when the chain expresses multiple filtering or mapping
  stages.
- For a multiline record header, put one component per line and align the
  closing parenthesis with the declaration.
- Keep short lambdas inline only when the expression is immediately readable.
  Use a block lambda for multiple statements or when a comment is required.
- Use the diamond operator when the compiler can infer generic arguments.
- Avoid nested ternary expressions. Use a named local variable or ordinary
  conditional statements when branching is not obvious.

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

## Lombok

- Implementation classes that use constructor injection should prefer
  `@RequiredArgsConstructor` with `final` dependency fields instead of manually
  writing boilerplate constructors.
- Operator and service classes must use Lombok `@Slf4j` for logging. Do not
  declare hand-written logger fields, and do not use the invalid `@Sl4j`
  spelling.
- Mutable persistence entities must use Lombok `@Getter` and `@Setter`. Mutable
  configuration-file binding objects and declarative UI/specification objects
  should use the same annotations when a binding framework requires JavaBean
  accessors.
- Internal domain models are not automatically mutable. Immutable or
  behavior-oriented models should expose only the accessors and state changes
  their contract requires; use `@Getter` without `@Setter`, records, or
  explicit behavior methods as appropriate.
- Types under `domain.request` and `domain.vo` must be Java records, so Lombok
  `@Getter` and `@Setter` do not apply to them.
- Keep Lombok imports in the third-party import group, before
  `com.innospots.*` project imports.
- Lombok only removes accessor boilerplate. Domain classes should still expose
  explicit methods for validation, state transitions, and business behavior.
- Do not use `@Data` on domain or persistence types. It generates equality,
  string, constructor, and mutation behavior too broadly for entities and
  security-sensitive objects.
- Do not use Lombok-generated `toString` behavior for credentials, secrets,
  tokens, password material, or other sensitive values.

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

## Dependency Fields and Construction

- Prefer constructor injection with `final` fields. A dependency must be
  visible in the constructor contract and must not be replaced after creation.
- Use `@RequiredArgsConstructor` for ordinary service, operator, handler, and
  runtime implementation classes when it produces the intended constructor.
- Use an explicit constructor when parameters require validation, defensive
  copying, normalization, or explanation that Lombok cannot express clearly.
- Do not use field injection or mutable public dependency fields.
- Keep optional collaborators explicit. Do not represent a required dependency
  with a nullable field merely to simplify tests.

```java
@Slf4j
@RequiredArgsConstructor
public final class PermissionGrantService {

    private final PermissionGrantDao permissionGrantDao;
    private final EventBus eventBus;
}
```

## Collections and State

- Prefer immutable empty collections (`List.of()`, `Set.of()`, `Map.of()`) over
  `null` when absence and emptiness have the same meaning.
- Defensively copy caller-owned collections at construction or boundary entry
  with `List.copyOf`, `Set.copyOf`, or `Map.copyOf`.
- Do not expose an internal mutable collection from an accessor. Return an
  immutable view or snapshot according to the API contract.
- Use a collection type that communicates semantics: `Set` for uniqueness,
  `List` for stable order and duplicates, and `Map` for keyed lookup.
- Preserve deterministic iteration order when output, dependency resolution,
  routing priority, or tests depend on it.
- Keep mutation local to the type that owns the state. Do not pass a mutable
  collection between layers and rely on undocumented shared mutation.

## Logging and Diagnostics

- Use Lombok `@Slf4j` in concrete services, operators, handlers, managers, and
  other runtime classes that log. Do not declare handwritten logger fields.
- Do not use `System.out`, `System.err`, or `Throwable#printStackTrace` in
  production code.
- Log stable identifiers and lifecycle transitions rather than complete domain
  objects. Never log passwords, secrets, tokens, decrypted payloads, or
  sensitive configuration values.
- Use parameterized logging (`log.info("Started plugin {}", pluginId)`) instead
  of string concatenation.
- Do not both log and rethrow the same failure at every layer. Log where useful
  context is added or where the failure is finally handled.

## Literals and Expressions

- Give repeated or domain-significant literals a named constant. A literal
  that is obvious and local, such as zero in an emptiness check, does not need
  a constant.
- Use enum constants for closed domain states and types instead of comparing
  unexplained strings throughout business code.
- Keep expressions readable. Extract a named predicate or local variable when
  a condition mixes multiple business rules.
- Use `equals` from a known non-null value for nullable enum/string comparison,
  such as `PluginState.ACTIVE.equals(state)`.
- Do not use comments to compensate for unclear expressions; name the concept
  and then comment only the non-obvious reason.

Architectural boundaries, null handling, immutability contracts, and exception
behavior are defined in [`api-design.md`](api-design.md). Identifier and member
names are defined in [`naming.md`](naming.md).
