# API Design Principles

## Method Signatures

- Prefer static factory methods over public constructors: `of()`, `create()`,
  `from()`, `named()`.
- Return immutable collections: `List.copyOf()`, `Map.copyOf()`. Never
  expose internal mutable references.
- Use `Optional<T>` for application-facing single-value results whose absence
  is an expected outcome. Framework-facing DAO methods may return nullable
  entities when that is the mapper contract; service/operator boundaries must
  normalize or reject that absence before exposing it further.
- Never use `Optional` for parameters, fields, record components, collection
  elements, or collection return values.
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

## Contract and Implementation Boundaries

Introduce an interface when at least one of these conditions is true:

- it is a public module, plugin, adapter, or SPI boundary;
- multiple implementations are required or intentionally supported;
- callers must be isolated from a runtime-specific implementation;
- the contract owns a lifecycle or resource boundary that implementations must
  honor.

Do not create an interface for every concrete class merely to enable mocking.
For one stable internal implementation, depend on the concrete type until a
real substitution boundary appears.

- Name a contract after its capability, such as `ResourceStore`,
  `PasswordDecryptor`, or `PluginManager`.
- Name the standard implementation `DefaultXxx` only when the contract admits
  other valid implementations. Use a strategy-specific qualifier when it adds
  information, such as `ClasspathPluginDiscovery` or
  `RsaPasswordDecryptor`.
- Keep transport interfaces in `endpoint`, reusable non-HTTP module contracts
  in `api`, and extension contracts in a focused `contract` package only when
  that separation represents a real boundary.
- Constructor-inject dependencies into concrete implementations and keep
  required dependency fields `final`. Do not expose dependency setters.
- A public contract must not return its implementation's mutable state,
  framework session, DAO, entity manager, or other infrastructure internals.

Naming details are defined in [`naming.md`](naming.md); source construction and
Lombok rules are defined in [`code-style.md`](code-style.md).

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
  `NullPointerException`, or `IllegalStateException` for caller/business
  validation or application-visible logic. A pure lower-level utility may use
  a JDK/framework precondition exception for programmer misuse when it never
  represents user input; translate it at the application boundary if it can
  escape into an application result.
- Accept null gracefully for optional parameters:
  - `null -> default` pattern in setters
  - `null -> skip` pattern in collection builders
- Prefer empty immutable collections to `null` for collection results.
- Do not return `null` from a collection-returning public method.
- Do not nest absence representations such as `Optional<List<T>>`; return an
  empty list when no values exist.
- State nullable framework results in the DAO method Javadoc when the generic
  mapper signature cannot express nullability.

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

## Validation and Normalization

Place each rule at the narrowest boundary that owns it:

- A record compact constructor or domain type enforces invariants required for
  every valid instance and performs defensive collection copies.
- A request's `validate()` method checks request-local field combinations that
  do not require persistence or another domain.
- An operator validates direct data-operation preconditions and translates
  mapper absence into the appropriate status code.
- A service validates workflow, authorization, cross-record, and cross-domain
  rules.
- An endpoint validates only transport concerns that cannot be expressed by
  Jakarta REST binding, then delegates application behavior.

Normalization must be deterministic and documented. Normalize transport
defaults, casing, whitespace, pagination, and collection emptiness before a
value becomes a stable business key or persisted state. Do not silently repair
an invalid value when the caller needs to know that the contract was violated.

Validation methods that reject input throw `NexusException` with a suitable
`StatusCode`. Boolean probes use names such as `isValid` and must not mutate
state. Never rely only on database constraint failures for validation that the
application can express clearly, but retain database constraints for integrity
under concurrency.

## Exception Handling

- All expected business, application, and translated infrastructure failures
  must use `com.innospots.nexus.base.exception.NexusException`.
- Prefer `NexusException.build(StatusCode, ...)` with an existing typed status;
  use `NexusStatusCode` for reusable platform failures and a domain or
  technical `XxxStatusCode` for narrower meanings.
- Domain-specific status code enums implement
  `com.innospots.nexus.base.status.StatusCode`, live under the owning domain's
  `domain.enums` package, and follow the ownership and nine-character format
  in [`exception-status-code.md`](exception-status-code.md).
- The raw string-code overload is only for validated, allowlisted interop or
  compatibility boundaries. Ordinary in-repository calls must not pass copied
  literals or `status.fullCode()` when the typed overload is available.
- Do not create one exception subclass per business error. Differentiate
  business failures through `StatusCode` implementations.
- All caller/business validation, state checks, and application preconditions
  must throw `NexusException` with the corresponding status code. A pure
  utility may retain a JDK/framework precondition exception for programmer
  misuse below that boundary; it must not become the public error contract.
- Wrap checked or provider exceptions at the boundary that owns the
  translation, preserving the original cause. Rethrow an existing
  `NexusException` unchanged unless a more specific status is justified.
- Never catch `Exception` silently, return fabricated success, or log and
  rethrow the same failure at every layer. Preserve interruption and
  cancellation, and do not routinely catch `Throwable`.
- Endpoint infrastructure maps `NexusException` centrally to `R.fail(...)` or
  `R.from(...)`; service and operator methods return domain values rather than
  transport wrappers or stack traces.

The complete taxonomy, catch/translate rules, status structure, ownership,
extension procedure, and contract-test requirements are defined in
[`exception-status-code.md`](exception-status-code.md).

```java
if (role == null) {
    throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND);
}
```

## Persistence Entities

- Every JPA/MyBatis-Plus persistence entity must inherit
  `com.innospots.nexus.core.persistence.entity.BaseEntity`,
  `TenantBaseEntity`, or `WorkspaceBaseEntity`.
- Use `WorkspaceBaseEntity` for records scoped to a workspace (tenant + workspace).
  Use `TenantBaseEntity` for records scoped to a tenant but not a workspace.
  Use `BaseEntity` only when the requirement identifies the data as platform-wide
  or realm-global (users, credentials, service registry).
- Do not introduce `ProjectBaseEntity` or a `projectId` isolation column.
- Do not duplicate audit fields (`createdAt`, `updatedAt`, `createdBy`,
  `updatedBy`) in concrete entities. They are inherited from `BaseEntity`.
- Do not duplicate `tenantId` or `workspaceId` in concrete scoped entities.
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

## Query and Command Semantics

Method names and return types must make the operation shape predictable. The
authoritative verb vocabulary is defined in [`naming.md`](naming.md).

- Queries do not mutate business state. `find` returns an optional single
  result, `list` returns a finite collection, `page` returns `PageResult<T>`,
  and `count` returns a number.
- Commands use a precise business verb and return the value callers need to
  continue. Do not return an entity merely because the persistence framework
  produced one.
- A `create` operation fails on a duplicate stable key unless its contract is
  explicitly idempotent. Do not silently reinterpret create as update.
- `update` changes only documented mutable attributes. Immutable stable keys
  are not accepted in update requests.
- `replace` treats the supplied value or association set as complete. It must
  define whether omission removes existing members.
- `delete` defines whether a missing target is success or a not-found failure.
  Apply the choice consistently within the same public resource boundary.
- `register`, `subscribe`, `start`, `stop`, `close`, and similar lifecycle
  operations must document repeated-call behavior.
- A method must not hide expensive I/O, blocking, publication, or persistence
  behind a property-like name.

Use idempotency where retries are a normal boundary behavior, including
declarative synchronization and registration. An idempotent operation produces
the same externally visible state when repeated with the same effective input;
it does not have to return the same object instance. Protect idempotency with
stable keys and database/runtime uniqueness, not only a read-then-write check.

## Domain Events and EventBus

- `com.innospots.nexus.base.events.EventBus` is the in-process event bus for
  domain event publication and subscription.
- Use domain events to decouple collaborators without changing the permitted
  module dependency direction. Event publication is not a reason for one
  sibling business module to depend on another.
- The publishing business domain owns the event contract when consumers may
  legally depend on that domain. Place it under the publisher's `domain.event`
  package, name it `XxxEvent`, and implement
  `com.innospots.nexus.base.events.DomainEvent`.
- Parallel business modules such as kernel and platform must not import each
  other's event types. Coordinate them through an application/adapter module
  that may depend on both, or through a deliberately shared lower-layer
  contract only when the contract is genuinely business-neutral. Do not move a
  concrete business event into core or console merely to bypass dependency
  rules.
- Domain events should be immutable records containing only the data consumers
  need. Do not expose DAO, service, mutable entity, or infrastructure objects
  through an event.
- Consumers that are allowed to reference the event contract define
  `XxxEventHandler` types in their own domain's `handler` package and implement
  `EventHandler<XxxEvent>`.
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
- The registration owner is responsible for cleanup. A returned `Subscription`
  or equivalent handle must be closed/unsubscribed when the owning component
  stops.
- Event payloads and event type strings are compatibility contracts. Add data
  compatibly and do not rename a published event type as part of an internal
  refactor.

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

- Non-trivial or repeated structural conversion among request, VO, model, and
  entity types must use MapStruct.
- Business-domain converter interfaces belong under `converter`, use the
  `*Converter` suffix, and declare
  `@Mapper(config = BaseMapperConfig.class)`.
- Converters between models and entities should extend
  `BaseBeanConverter<ModelType, EntityType>` and add other conversion methods
  as required.
- Keep bulk field copying and repeated Domain POJO conversion out of endpoint,
  service, and operator classes.
- Direct construction is acceptable for one or two scalar values when the
  mapping is local, obvious, and not repeated. Do not create a converter only
  to hide a constructor call of that size.
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
public UiDatasource param(String key, Object value) {
    if (key != null) {
        params.put(key, value);
    }
    return this;
}
```

## Lifecycle and Resource Ownership

Types that own threads, executors, subscriptions, class loaders, schedulers,
network clients, or other closeable resources must expose and document a clear
lifecycle.

- Construction establishes valid local state but must not silently start
  long-running background work unless the type's factory contract says so.
- `initialize` prepares dependencies and registrations; `start` begins active
  work; `stop` halts work while preserving restartable state when supported;
  `destroy` or `close` releases resources permanently.
- Lifecycle operations define their allowed states and repeated-call behavior.
  Prefer safe idempotent cleanup.
- The type that creates or registers a resource owns its cleanup unless the
  API explicitly transfers ownership.
- Release resources in reverse acquisition order when dependencies exist.
- Do not swallow cleanup failures. Preserve the primary failure and attach or
  log secondary cleanup failures with enough context to diagnose them.
- Do not publish a started/stopped success event until the corresponding state
  transition succeeds.

Use `AutoCloseable` or a focused handle such as `Subscription` when lexical or
explicit cleanup improves correctness. Avoid finalizers and do not rely on
garbage collection for external resource release.

## Thread Safety and Concurrency

- Immutable records and snapshots are preferred at thread boundaries.
- A mutable public type must state whether it is thread-safe, confined to one
  thread, or requires external synchronization when concurrent use is
  plausible.
- Protect one invariant with one clear synchronization strategy. Do not mix
  synchronized blocks, atomics, and concurrent collections without explaining
  which state each mechanism protects.
- Do not call unknown plugin, event-handler, or callback code while holding an
  internal lock. Copy the required registrations first, then invoke callbacks.
- State transitions that coordinate multiple fields must be atomic from the
  caller's perspective.
- Return immutable snapshots rather than live mutable views of registries,
  routing tables, configuration, or metrics.
- Cancellation and interruption must be propagated or deliberately restored;
  do not silently consume `InterruptedException`.

## Public Contract Compatibility

Treat the following as public compatibility surfaces when they cross a module,
plugin, persistence, or transport boundary:

- public type names, packages, method signatures, generic bounds, and declared
  semantics;
- REST paths, parameter names, request/response fields, enum values, and status
  codes;
- table/column names, stable business keys, entity ID prefixes, and index-backed
  uniqueness assumptions;
- event type strings, configuration keys, plugin IDs, capability keys, tag
  names, and serialized field names.

Do not change one of these surfaces as a mechanical rename or internal
refactor. Document the migration, compatibility adapter, data migration, or
version boundary first. Additive changes must still define defaults for older
callers and persisted data.

Deprecations use `@Deprecated` and Javadoc `@deprecated` together, identify the
replacement, and remain for an explicitly agreed compatibility period. Do not
keep an obsolete alias indefinitely without a removal decision.

## API Review Checklist

- Does the abstraction have a real contract boundary, or is an interface being
  added mechanically?
- Are null, absence, empty collections, ownership, and mutation explicit?
- Is validation placed at the boundary that owns the rule?
- Do query and command names match their result and side effects?
- Are transaction, idempotency, lifecycle, cleanup, and repeated calls defined?
- Is concurrent access safe or clearly constrained?
- Are domain events published only after successful state changes and cleaned
  up by their registration owner?
- Has every affected public identifier been checked for compatibility impact?
