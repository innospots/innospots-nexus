# API Design Principles

## Method Signatures

- Prefer static factory methods over public constructors: `of()`, `create()`,
  `from()`, `named()`.
- Return immutable collections: `List.copyOf()`, `Map.copyOf()`. Never
  expose internal mutable references.
- Use `Optional<T>` for return values that may be absent (not for parameters).
- Use `record` for simple data carriers with built-in equals/hashCode/toString.

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

## Immutability

- Fields should be `final` where possible.
- Collections returned from accessors must be unmodifiable copies.
- Parameters passed to constructors should be defensively copied if mutable.

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

## Null Handling

- Reject required null or invalid values with `NexusException` and the
  corresponding `StatusCode`.
- Do not use `Objects.requireNonNull()`, `IllegalArgumentException`,
  `NullPointerException`, or `IllegalStateException` for validation or
  application logic.
- Accept null gracefully for optional parameters:
  - `null -> default` pattern in setters
  - `null -> skip` pattern in collection builders

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

## Exception Handling

- All business exceptions must use
  `com.innospots.nexus.base.exception.NexusException`.
- Prefer `NexusException.build(NexusStatusCode.XXX)` with an existing
  platform-wide status code.
- Add a constant to `NexusStatusCode` when a missing status is broadly reusable
  across modules.
- Domain-specific business status codes must be enum types implementing
  `com.innospots.nexus.base.status.StatusCode`. Place them under the business
  domain's `domain.enums` package and name them `XxxStatusCode`.
- Do not create one exception subclass per business error. Differentiate
  business failures through `StatusCode` implementations.
- All validation, state checks, business rules, and internal API precondition
  failures must throw `NexusException` with the corresponding status code.
- Project application logic must not explicitly throw
  `IllegalArgumentException`, `NullPointerException`, or
  `IllegalStateException`.
- Wrap checked exceptions into `NexusException` at boundary points, preserving
  the original cause.
- Never catch `Exception` silently — log or rethrow.

```java
if (role == null) {
    throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND);
}
```

## Persistence Entities

- Every JPA/MyBatis-Plus persistence entity must inherit either
  `com.innospots.nexus.core.entity.BaseEntity` or
  `com.innospots.nexus.core.entity.ProjectBaseEntity`.
- Use `ProjectBaseEntity` by default. An entity may inherit `BaseEntity` only
  when the requirement explicitly identifies the data as platform-wide and
  not project-scoped.
- Use `BaseEntity` for platform-wide records that are not scoped to one
  project, such as users, credentials, OAuth identities, service registry
  records, and global dictionaries.
- Use `ProjectBaseEntity` for records whose lifecycle and queries are scoped
  to a project.
- Do not duplicate audit fields (`createdTime`, `updatedTime`, `createdBy`,
  `updatedBy`) in concrete entities. They are inherited from `BaseEntity`.
- Do not duplicate `projectId` in concrete project-scoped entities. It is
  inherited from `ProjectBaseEntity`.
- Concrete persistence entity primary keys must be `String` fields annotated
  with `@TableId(type = IdType.ASSIGN_UUID)`, `@Id`, and `@Column(length = 32,
  nullable = false)`.
- Every persistence entity must use Jakarta Persistence `@Entity` and declare
  its table with `@Table`, including explicit indexes for unique lookups,
  foreign-key lookups, and common page-query filters.
- Every persistence field must use Jakarta Persistence mapping annotations as
  applicable, including `@Id` for primary keys and `@Column` for ordinary
  columns.
- String column lengths must be powers of two, such as `16`, `32`, `64`,
  `128`, `256`, `512`, or `1024`. Use `@Lob` for text that is intentionally
  unbounded instead of choosing an arbitrary large length.
- Every persistence entity must declare a `public static final String
  TABLE_NAME` constant and use that constant in both Jakarta Persistence
  `@Table(name = EntityType.TABLE_NAME)` and MyBatis-Plus
  `@TableName(EntityType.TABLE_NAME)`.
- Persistence entity classes are non-record domain classes and therefore must
  use Lombok `@Getter` and `@Setter`.
- Entity primary keys use `IdType.ASSIGN_UUID`. Each entity must override
  `BaseEntity.idPrefix()` with a short, stable domain prefix. The shared
  `DbPrimaryGenerator` generates IDs through `IdGenerator.ulid(prefix)`;
  operators must not assign generated primary keys manually.
- Concrete persistence entities should declare necessary JPA `@Table(indexes =
  ...)` annotations for unique lookup fields, foreign-key lookup fields, and
  page-query filter fields. Keep index names explicit and table-prefixed.

```java
@Getter
@Setter
@Entity
@Table(name = RoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_code", columnList = "role_code", unique = true)
})
@TableName(RoleEntity.TABLE_NAME)
public class RoleEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_role";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String roleId;

    @Column(length = 64, nullable = false)
    private String roleCode;
}
```

## Domain Models

- Business data types belong under `domain`, separated into `entity`,
  `request`, `vo`, `model`, and `enums` according to their responsibility.
- Request and VO types must be records. Request names end with `Request`, and
  response view names end with `Vo`.
- Entity names end with `Entity`. Internal model names express the actual
  business concept and do not require a technical suffix.
- Domain types may own validation, calculations, invariants, and state
  transitions. Prefer behavior-rich models when the behavior belongs to the
  object; do not move all business logic into services merely to keep domain
  objects as data-only structures.
- Configuration-file binding objects and other system configuration types
  belong in a module-level `config` package, not under `domain`.

## REST Endpoint Contracts

- HTTP API boundaries must use the `*Endpoint` suffix and be placed under an
  `endpoint` package.
- New domain initialization uses concrete endpoint classes by default.
  Declare an interface only when a developer explicitly requires a separate
  transport contract.
- Use Jakarta REST (`jakarta.ws.rs`) annotations for resource paths, HTTP
  methods, media types, and request parameters.
- Keep endpoint signatures transport-oriented. Delegate validation,
  orchestration, transaction handling, and persistence to service or operator
  boundaries.
- Intentionally deferred concrete methods must contain a focused `TODO` and
  throw `UnsupportedOperationException` instead of returning fabricated data.
- Keep the `api` package available for non-HTTP public module contracts; do not
  place Jakarta REST endpoints there.
- Every endpoint return type must be
  `com.innospots.nexus.base.domain.response.R<T>`.
- Return `R<XxxVo>` for ordinary payloads, `R<PageResult<XxxVo>>` for paginated
  payloads, and `R<Void>` for operations without response data.
- `R` is a transport-boundary wrapper. Service and operator methods must return
  domain values or `PageResult<T>` directly rather than wrapping results in
  `R`.
- Convert a `NexusException` to `R.fail(...)` through the endpoint runtime's
  centralized exception handling. Do not repeat exception-to-response mapping
  in every endpoint method.

## DAO Contracts

- MyBatis-Plus mapper interfaces must be placed under `dao`, use the `*Dao`
  suffix, and extend `BaseMapper<EntityType>`.
- Use inherited `BaseMapper` methods before introducing custom methods.
- Custom dynamic queries and updates should be implemented as `default`
  methods with `LambdaQueryWrapper` or `LambdaUpdateWrapper`, preferably
  created through `Wrappers.lambdaQuery()` and `Wrappers.lambdaUpdate()`.
- Every DAO method must access exactly one table.
- SQL joins are prohibited in DAO methods, annotation SQL, mapper XML, and
  other persistence statements.
- Use annotation-based SQL such as `@Select` only for explicit single-table
  queries that are clearer than wrappers.
- Do not create MyBatis mapper XML files or XML statement definitions.
- DAO methods should express direct database operations only. Cross-DAO
  coordination and business decisions belong in operator or service types.
- Cross-table reads must use separate batch queries and assemble results in an
  operator or service. Collect identifiers or stable keys first, query each
  table in batches, and map results in memory.
- N+1 query patterns are prohibited. Do not issue one related-table query per
  record.
- Cross-table writes, relationship integrity, cascades, and stable-key
  propagation belong in a transactional service.

## Service and Operator Boundaries

- Operators implement straightforward data-oriented operations. An operator
  usually works with one DAO, but may use multiple DAOs when the operation
  remains simple and cohesive.
- Services implement complex workflows, cross-domain coordination, validation,
  and orchestration across multiple operators or DAOs.
- A service may depend on operators and DAOs.
- An operator must not depend on a service or another operator. Its business
  data dependencies are limited to DAOs; it may also use stateless converters
  and shared lower-level technical utilities.
- When logic needs to coordinate multiple operators, define that logic in a
  service rather than allowing operator-to-operator dependencies.
- Paginated service and operator methods must return
  `com.innospots.nexus.base.domain.response.PageResult<T>`.
- Service and operator methods must not return endpoint wrapper `R<T>`.

## Domain Events and EventBus

- `com.innospots.nexus.base.events.EventBus` is the in-process event bus for
  domain event publication and subscription.
- Use domain events to decouple business modules. A module must not directly
  depend on another business module's service, operator, or DAO when the
  interaction can be expressed as a domain event.
- The publishing business domain owns the event contract. Place it under the
  publisher's `domain.event` package, name it `XxxEvent`, and implement
  `com.innospots.nexus.base.events.DomainEvent`.
- Domain events should be immutable records containing only the data consumers
  need. Do not expose DAO, service, mutable entity, or infrastructure objects
  through an event.
- Consumers define `XxxEventHandler` types in their own business domain's
  `handler` package and implement `EventHandler<XxxEvent>`.
- Publishers depend only on the event contract and `EventBus`; they must not
  depend on consumer handlers or consumer implementation types.
- Publish an event only after the originating state change completes
  successfully. Do not publish success events before a write or transaction
  can still fail.
- Use `EventBus.publish(event)` for notification-style cross-module actions
  where no return value is required.
- Use `EventBus.publishSync(event)` only when the caller truly requires an
  immediate handler result. Do not use synchronous events to recreate direct
  service calls between modules.
- Event handlers may delegate to their own module's service or operator.
  Handler failures must throw `NexusException` with the corresponding
  `StatusCode`.
- Event handlers must be registered and unregistered through
  `EventBus.subscribe(...)` and `EventBus.unsubscribe(...)` at the owning
  module's lifecycle boundary.

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

## Domain Conversion

- Structural conversion among request, VO, model, and entity types must use
  MapStruct.
- Business-domain converter interfaces belong under `converter`, use the
  `*Converter` suffix, and declare
  `@Mapper(config = BaseMapperConfig.class)`.
- Converters between models and entities should extend
  `BaseBeanConverter<ModelType, EntityType>` and add other conversion methods
  as required.
- Keep bulk field copying and repeated Domain POJO conversion out of endpoint,
  service, and operator classes.
- Domain-owned behavior, validation, calculations, and scalar formatting are
  not bean conversion and may remain on the domain type.

## Password Registration Requests

- Password registration request objects must carry the frontend encrypted
  password payload, not password hash, salt, algorithm, or password policy
  version fields.
- Decrypt frontend password payloads through a public module interface so the
  transport encryption method can be replaced or extended without changing
  request objects.
- Store server-side password hashes only after decrypting the frontend payload
  and hashing with `innospots-nexus-base` cryptographic utilities.
- Password hashing utilities must support externally supplied salt values, and
  registration persistence should store the salt used for the generated hash.

## Page Requests

- Shared paginated query request objects belong under `domain.request`.
- Module-specific page query requests should inline or compose
  `com.innospots.nexus.base.domain.request.SimpleQueryRequest` when they need
  the common `input`, `pageNo`, and `pageSize` fields.
- Page query methods should accept a request object instead of separate page
  number, page size, and filter parameters.

## Transaction Boundaries

- Methods that perform multiple DAO writes or coordinate writes across multiple
  tables must declare `jakarta.transaction.Transactional`.
- Use only `jakarta.transaction.Transactional`; do not use Spring's
  `org.springframework.transaction.annotation.Transactional`.
- Keep simple single-table reads outside transactions unless a concrete
  consistency requirement needs a transactional read.
- Prefer method-level `@Transactional` on the smallest write operation instead
  of annotating an entire class by default.
- Transaction API versions belong in `innospots-nexus-bom`; modules must depend
  on `jakarta.transaction-api` without inline versions.

## Fluent API

- Setter-like methods on mutable objects should return `this` for chaining.
- Use `@SuppressWarnings("unchecked")` only when the cast is provably safe,
  and document why.

```java
public UiComponent setting(String key, Object value) {
    if (key != null) {
        settings.put(key, value);
    }
    return this;
}
```
