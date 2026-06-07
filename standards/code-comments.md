# Code Comments

All source code must follow standard Java Javadoc conventions, covering three
comment tiers within every source file.

## 1. Class/Interface/Enum/Annotation Comments

Every public type must have a Javadoc block comment immediately before its
declaration that explains:

- What the type is and its primary responsibility
- Any important usage constraints or thread-safety guarantees
- `@see` references to related types where applicable
- `@param <T>` for generic type parameters

```java
/**
 * An immutable paginated data container. Validates page bounds at construction
 * and provides convenience methods for pagination navigation.
 *
 * @param <T> the record type contained in this page
 * @see PageResult
 */
public record DataPage<T>(...) { ... }
```

## 2. Method Comments

Every public and protected method must have a Javadoc block that describes:

- **What** the method does (not how — the code explains how)
- `@param` for each parameter with a description of the parameter's role
- `@return` for the return value
- `@throws` for any checked or important unchecked exceptions

Short getters/setters and trivial one-liners may use a single-line Javadoc
(`/** Short description. */`). For example:

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

## 3. Inline Comments for Complex Logic

Complex or non-obvious code paths must be annotated with line comments that
explain the *why*, not the *what*. Apply these patterns:

- **Algorithm rationale**: Why a specific approach was chosen
- **Edge cases**: Why a null check, fallback, or special handling exists
- **Non-obvious side effects**: When a method has effects beyond its signature
- **Multi-step flows**: Brief markers for each stage of a pipeline

```java
// BigDecimal MVEL literal (e.g. 10.5M) — pass through without quoting
// Range operator values are left unquoted for arithmetic comparison
```

Note: over-commenting obvious code is discouraged. Trust the method and
variable names to convey intent where possible.