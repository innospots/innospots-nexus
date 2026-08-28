# Code Comments

Comments document contracts, constraints, and design intent that names and
types cannot express alone. They must not narrate obvious syntax or preserve a
change history that belongs in version control.

## Package Documentation

Add `package-info.java` when a package exposes a public contract, defines an
architectural boundary, or has non-obvious usage constraints. Its Javadoc
should explain:

- the package's single responsibility;
- what belongs and does not belong in the package;
- the direction of dependencies or extension boundary when relevant;
- lifecycle, thread-safety, persistence, or transport assumptions shared by
  the package.

Do not create empty or boilerplate package documentation that merely expands
the package name. Update `package-info.java` when the package responsibility
changes materially.

## Type Comments

Every public type must have a Javadoc block comment immediately before its
declaration that explains:

- What the type is and its primary responsibility
- Any important usage constraints or thread-safety guarantees
- `@see` references to related types where applicable
- `@param <T>` for generic type parameters

The comment describes the contract at the type's own abstraction level. An
interface explains what implementations promise; an implementation explains
its strategy, lifecycle, or distinguishing behavior without copying the
interface text.

Records document their overall invariant and use `@param` for components whose
meaning, unit, nullability, security role, or ownership is not self-evident.
Enums document the closed concept. Add Javadoc to individual enum constants
when their business meaning is not clear from the constant name.

Annotations must document where they are intended to be used and the runtime
effect they trigger. Exceptions must document the failure category they
represent rather than list every call site.

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

## Method Comments

Every public and protected method that declares behavior or a reusable contract
must have a Javadoc block that describes:

- **What** the method does (not how — the code explains how)
- `@param` for each parameter with a description of the parameter's role
- `@return` for the return value
- `@throws` for any checked or important unchecked exceptions

Also document important nullability, ownership, mutation, idempotency,
thread-safety, security, and lifecycle requirements when they are part of the
method contract.

The following do not require duplicated Javadoc when their contract is already
clear:

- record accessors;
- trivial JavaBean getters and setters whose field is already documented;
- enum `values()` and `valueOf()` methods;
- an override that preserves the complete parent contract without adding
  constraints, side effects, or failure behavior.

Use `{@inheritDoc}` only when it helps a generated documentation set. Do not add
an empty comment or copy parent Javadoc merely to satisfy a comment count. If an
override changes performance, blocking behavior, resource ownership, thread
safety, or permitted inputs, document the difference explicitly.

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

Javadoc tags follow the prose in this order: `@param` (including type
parameters), `@return`, `@throws`, `@see`, `@since`, `@deprecated`. Keep tag
descriptions as noun phrases unless a full sentence adds necessary detail.

Use `{@code ...}` for identifiers, literals, and short expressions. Use
`{@link Type}` or `{@link Type#method(...)}` when the relationship helps a
reader navigate the contract. Do not use raw HTML for formatting that standard
Javadoc tags can express.

## Inline Comments for Complex Logic

Complex or non-obvious code paths must be annotated with line comments that
explain the *why*, not the *what*. Apply these patterns:

- **Algorithm rationale**: Why a specific approach was chosen
- **Edge cases**: Why a null check, fallback, or special handling exists
- **Non-obvious side effects**: When a method has effects beyond its signature
- **Multi-step flows**: Brief markers for each stage of a pipeline
- **Lifecycle and cleanup**: Why order, rollback, or resource release matters
- **Concurrency**: Which invariant is protected and why the synchronization or
  atomic operation is required
- **Security**: Why data is masked, copied, rejected, or kept out of logs
- **Compatibility**: Why a stable identifier, fallback, or legacy behavior
  cannot be changed yet

```java
// BigDecimal MVEL literal (e.g. 10.5M) — pass through without quoting
// Range operator values are left unquoted for arithmetic comparison
```

Note: over-commenting obvious code is discouraged. Trust the method and
variable names to convey intent where possible.

## TODO and Deferred Behavior

A TODO is allowed only for intentionally deferred behavior with a clear future
boundary. State what is missing and, when useful, the component that should own
it.

```java
// TODO Delegate role lookup to the role service once the service boundary exists.
throw new UnsupportedOperationException("Role lookup is not implemented");
```

- Do not write bare `TODO`, `FIXME`, `later`, or `temporary` comments.
- Do not use a TODO to excuse fabricated success data, swallowed exceptions, or
  an incomplete security check.
- Deferred concrete endpoint methods must pair the focused TODO with a specific
  `UnsupportedOperationException`, as required by the endpoint standards.
- Remove the TODO in the same change that implements the behavior.

## Prohibited Comments

- Do not keep commented-out code. Version control preserves removed code.
- Do not add author names, modification dates, ticket histories, or change logs
  to source comments.
- Do not restate the type, method, or line of code in different words.
- Do not promise behavior that is not enforced by the implementation or tests.
- Do not expose secrets, production values, personal data, or internal attack
  details in examples or comments.
- Do not use comments to redefine a misleading name; rename the identifier
  according to [`naming.md`](naming.md).

## Comment Review Checklist

- Does each public contract explain responsibility and important constraints?
- Are record components, enum constants, and annotations documented where
  their meaning is not obvious?
- Do method comments describe observable behavior rather than implementation?
- Are non-obvious lifecycle, concurrency, security, and compatibility choices
  explained close to the relevant code?
- Are all TODOs focused, actionable, and paired with explicit deferred
  behavior?
- Can any comment be removed by improving the name or simplifying the code?
