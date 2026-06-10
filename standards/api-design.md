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

- Reject null where it would cause ambiguity or NPE:
  - `Objects.requireNonNull()` for critical parameters
  - `IllegalArgumentException` for invalid values
- Accept null gracefully for optional parameters:
  - `null -> default` pattern in setters
  - `null -> skip` pattern in collection builders

```java
public NexusException(String code, String message) {
    super(message);
    this.code = Objects.requireNonNull(code, "code must not be null");
}

public SimpleCondition factor(Factor factor) {
    if (factor != null) {
        factors.add(factor);
    }
    return this;
}
```

## Exception Handling

- Use `NexusException` (unchecked) for all platform exceptions.
- Wrap checked exceptions into `NexusException` at boundary points.
- Include both a machine-readable error code and a human-readable message.
- Never catch `Exception` silently — log or rethrow.

```java
try {
    return mapper.writeValueAsString(value);
} catch (JsonProcessingException e) {
    throw new NexusException("JSON_WRITE_FAILED", "Failed to write JSON", e);
}
```

## Persistence Entities

- Every JPA/MyBatis-Plus persistence entity must inherit either
  `com.innospots.nexus.core.entity.BaseEntity` or
  `com.innospots.nexus.core.entity.ProjectBaseEntity`.
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
  with `@TableId(type = IdType.INPUT)`, `@Id`, and `@Column(length = 32,
  nullable = false)`.
- Because entity primary keys use `IdType.INPUT`, data operators must assign
  IDs before insertion. Prefer `IdGenerator.ulid(prefix)` or
  `IdGenerator.monotonicUlid(prefix)` with a short domain prefix.
- Concrete persistence entities should declare necessary JPA `@Table(indexes =
  ...)` annotations for unique lookup fields, foreign-key lookup fields, and
  page-query filter fields. Keep index names explicit and table-prefixed.

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
- Module-specific page query requests should extend
  `com.innospots.nexus.base.domain.request.BasePageRequest` when they need the
  common `input`, `pageNo`, and `pageSize` fields.
- Page query methods should accept a request object instead of separate page
  number, page size, and filter parameters.

## Transaction Boundaries

- Methods that perform multiple DAO writes or coordinate writes across multiple
  tables must declare `jakarta.transaction.Transactional`.
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
