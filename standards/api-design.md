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