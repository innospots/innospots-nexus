# Exception and Status-Code Conventions

This document is the authoritative standard for application exceptions,
business status codes, and status-code extensions in Nexus. It describes the
current `NexusException`, `StatusCode`, `StatusCategory`,
`StatusCodeRules`, `NexusStatusCode`, `PluginStatusCode`, and `R<T>` contracts.
API-specific method and boundary rules remain in
[`api-design.md`](api-design.md); this document defines the shared error
semantics those APIs must use.

## 1. Principles

- A failure has one owning boundary, one stable status code, and one useful
  cause chain.
- Status codes describe a stable application meaning. HTTP status codes
  describe only the transport result.
- Expected application failures are represented by `NexusException`; lower
  layers may use native exceptions for programmer misuse when that failure is
  never exposed as an application result.
- Prefer an existing status code with the same meaning. Add a new code only
  when reusing one would make the contract ambiguous.
- Messages and advice are part of a compatibility surface. Keep them stable,
  actionable, bilingual, and free of secrets or volatile runtime data.

## 2. Exception taxonomy

Choose the exception form from the meaning of the failure, not from the layer
where it happened.

| Failure kind | Required representation | Examples and rules |
| --- | --- | --- |
| Expected application or domain failure | `NexusException` with a typed `StatusCode` | Invalid request, missing record, duplicate business key, forbidden operation, invalid lifecycle transition. |
| Infrastructure failure translated at an application boundary | `NexusException` with the most accurate status and the original cause | Database timeout, unavailable external channel, malformed configuration, plugin loading failure. Do not expose driver or provider details to callers. |
| Pure utility or programmer misuse | JDK/framework exception may be retained below the application boundary | `StatusCodeRules` rejecting a malformed code, a paging utility rejecting an impossible page size, or a null passed to a private invariant. The exception must not be presented as a user-facing business result. |
| Interruption or cancellation | Preserve interruption/cancellation semantics | Restore the interrupt flag when catching `InterruptedException`, then rethrow or translate only when the owning contract explicitly defines cancellation. Do not turn cancellation into a generic business error. |
| Fatal JVM or process failure | Do not catch or map routinely | `Error` subclasses, linkage failures, and other fatal conditions must propagate. Cleanup handlers may observe them but must not hide them. |

The application boundary normally includes an endpoint, service, operator, job
handler, listener, or public extension callback. A pure utility may reject a
programmer contract with `IllegalArgumentException`, `IllegalStateException`,
or `NullPointerException`, but callers must translate it before it becomes an
application response if the boundary is responsible for user or business
input.

## 3. Constructing `NexusException`

### 3.1 Typed status codes are the default

Use the typed overloads and let the status define the stable code, category,
message, advice, and HTTP mapping used by the owning boundary:

```java
throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND, cause);
throw NexusException.build(statusCode, displayOverride, cause);
```

`NexusException.build(StatusCode, ...)` is preferred because it preserves the
status-code contract and prevents spelling or ownership drift. Do not create a
new exception subclass for each business error; differentiate errors with
`StatusCode` implementations.

The raw-code overload (`build(String, String, ...)`) is reserved for an
interop boundary such as a plugin, remote provider, persisted historical
payload, or compatibility adapter. `StatusCodeRules` validates the separate
module/category/local components used by typed statuses; it is not, by itself,
a parser or allowlist for arbitrary full-code strings. Before accepting a raw
code:

1. validate the complete `MODULE + CATEGORY + LOCAL` shape with a dedicated
   full-code parser or equivalent adapter check (and use `StatusCodeRules` for
   the individual components);
2. check it against an explicit allowlist or the owning registry;
3. preserve the source system in structured logs or metadata rather than
   embedding untrusted text in the public message; and
4. translate it to a typed local status when the boundary can do so.

Ordinary in-repository application calls must not pass
`status.fullCode()` or a copied string to the raw-code overload when the typed
status is available. Existing opaque legacy strings that do not have the
canonical nine-character shape are compatibility debt: accept them only in a
designated adapter, never allocate new ones, and map them to a canonical typed
status before a current transport contract when possible.

### 3.2 Messages, display overrides, and causes

- `code()` and `fullCode()` identify the stable nine-character status code.
- `display()` is an optional localized or presentation-specific override; it
  does not change the status identity or HTTP mapping.
- The status `message()` states what happened. `advice()` states what the
  caller can do next. Keep both English and Chinese values meaningful even
  when a client currently displays only one locale. A successful status may
  intentionally have blank advice; a new non-success status must provide both
  locales. Technical catalogs that currently duplicate English text for both
  locales are legacy behavior and should be localized when changed without
  silently changing compatibility keys.
- Pass the original `Throwable` as the cause when translating a lower-level
  failure. Never replace a useful cause with a newly constructed exception
  that loses diagnostics.
- Never put passwords, tokens, credentials, encryption keys, authorization
  headers, full SQL with secrets, or stack traces in `message()`, `advice()`,
  `display()`, or endpoint response data.
- Stable status text must not contain request IDs, record IDs, file paths,
  provider responses, user input, or other volatile values. Put such values in
  structured, access-controlled logs or tracing context.
- A runtime message passed to `NexusException.build(StatusCode, String)` is
  also a response surface. Apply the same restrictions as `message()`,
  `advice()`, and `display()`: do not include secrets, credentials, IDs, user
  input, provider text, SQL, paths, or stack traces. Prefer the status summary
  and put safe diagnostics in structured context.

## 4. Throwing, catching, and translating

### 4.1 Throw at the owning boundary

Throw a typed `NexusException` where the code can select the correct business
meaning:

- request validation selects an input-validation status;
- an operator translates mapper absence into a not-found or data status;
- a service selects workflow, authorization, conflict, or cross-record
  statuses;
- an infrastructure adapter selects an external, middleware, configuration,
  or internal-error status appropriate to its contract.

Do not throw a generic status in a low-level helper merely because it is
convenient. The helper should either return a typed result or propagate its
native failure for the owning boundary to translate.

### 4.2 Catch narrowly and preserve the chain

- Rethrow an existing `NexusException` unchanged unless the current boundary
  can add a more specific, semantically correct status. If translating, keep
  the original exception as the cause.
- Catch the narrowest checked or provider exception that the boundary can
  understand. Wrap it with `NexusException.build(status, cause)`.
- Do not catch `Exception` merely to return a fabricated success, empty result,
  or generic message. Never swallow a failure.
- Do not log the same exception at every layer. The translation layer may add
  structured context; the outer request/job boundary owns the final error log
  and response mapping.
- Preserve `InterruptedException` and cancellation. Restore the interrupt flag
  (`Thread.currentThread().interrupt()`) before returning or rethrowing when a
  method catches interruption only to add context.
- Do not catch `Throwable` in ordinary application code. If a top-level runner
  has a fatal-error hook, it must rethrow after cleanup and must not convert a
  fatal `Error` into a normal business response.

### 4.3 Response mapping

Endpoint infrastructure catches `NexusException` centrally and maps it to
`R.fail(...)` (or `R.from(exception)`). The HTTP transport adapter resolves the
stable code through the owning status catalog/registry and applies its
`httpStatusCode()` separately; `R<T>` itself carries code, message, display,
and data, not an HTTP-status field. Services, operators, and domain models must
not construct `R` responses or leak stack traces. A public response contains
the stable code, localized message/advice selected by the transport, and safe
details only; it does not contain the cause chain or internal stack trace.

Unknown failures reaching the outer boundary must be logged with correlation
context and mapped to `NexusStatusCode.SYSTEM_ERROR` (or an explicitly
documented generic internal status owned by another transport boundary). The
mapping must not expose implementation class names, SQL, filesystem paths, or
provider diagnostics.

## 5. Status-code structure

### 5.1 Canonical format

Every full code is exactly nine characters:

```text
MODULE (3 uppercase letters) + CATEGORY (2 digits) + LOCAL (4 digits)
```

For example, `NEX080002` means module `NEX`, category `08`, local code
`0002`. `StatusCodeRules` is the source of truth for shape validation. A
status enum must return the same value from `bisCode()` and `fullCode()`.

| Segment | Rule | Meaning |
| --- | --- | --- |
| Module | Exactly three uppercase ASCII letters, such as `NEX` or `PLG` | Owning product/module or technical boundary. |
| Category | Exactly two decimal digits backed by `StatusCategory` | Failure semantics, not an HTTP status. |
| Local | Exactly four decimal digits, normally zero-padded | Stable allocation within the module/category namespace. |

Do not shorten a code, insert separators, use lowercase, or encode an HTTP
status in the local segment. Full codes are externally visible identifiers;
changing one is a compatibility break.

### 5.2 Category semantics

Select the category that explains why the operation failed. The current
`StatusCategory` values are grouped as follows. Each category also exposes a
stable human-readable `label()` and an operational `priority()` hint
(`L`, `M`, `H`, `B`, or `C`); priority is not an HTTP status and does not by
itself determine retry behavior.

| Category family | Current categories | Use for |
| --- | --- | --- |
| General and input | `GENERAL`, `INPUT_VALIDATION` | Unclassified shared outcomes and malformed caller input. |
| Business and authorization | `BUSINESS_RULE`, `PERMISSION_SECURITY`, `COMPLIANCE` | Domain invariants, forbidden actions, policy or regulatory constraints. |
| Conflict and limits | `TRANSACTION_CONFLICT`, `RESOURCE_LIMIT`, `BATCH_JOB` | Concurrency/idempotency conflicts, quotas, and batch/job lifecycle failures. |
| Resource and data | `RESOURCE_DATA`, `DATA_OPERATION`, `DATA_CONSISTENCY`, `DATA_SCHEMA` | Missing or invalid data, persistence operations, integrity, and schema mismatch. |
| External and middleware | `EXTERNAL_FAILURE`, `CHANNEL_INTERACTION`, `MIDDLEWARE`, `DATA_CONNECTION` | Provider/channel failures, middleware lifecycle, and connectivity. |
| Configuration and execution | `CONFIGURATION`, `INTERNAL_ERROR`, `SCRIPT`, `SQL_EXECUTION` | Invalid setup, unexpected application faults, scripts, or SQL execution. |
| File and cryptography | `FILE_OPERATION`, `CRYPTO` | File/object-storage operations and cryptographic processing. |

Do not choose a category solely because it has a convenient HTTP mapping. For
example, a duplicate business key is a business or transaction conflict, not
an arbitrary `INTERNAL_ERROR`, even if both eventually produce a server-side
response in one transport.

### 5.3 HTTP mapping

`httpStatusCode()` is a transport default and may be adapted by an endpoint
adapter only when the external protocol requires it. Common mappings are:

| HTTP code | Typical meaning |
| --- | --- |
| `400` | Invalid input or request shape. |
| `401` | Missing or invalid authentication. |
| `403` | Authenticated but not permitted. |
| `404` | Requested resource is absent and the contract treats absence as an error. |
| `409` | Duplicate, stale-version, idempotency, or other transaction conflict. |
| `429` | Quota or rate/resource limit. |
| `500` / `502` / `503` | Internal, external, middleware, or unavailable service failure. |

The same HTTP code may serve several statuses; clients must branch on the full
status code, not only on HTTP. Conversely, a status must not be duplicated just
to obtain a different HTTP code without a distinct application meaning.

## 6. Status-code naming and placement

### 6.1 Enum and member names

- Name status enum types `XxxStatusCode` and implement
  `com.innospots.nexus.base.status.StatusCode`.
- Use `UPPER_SNAKE_CASE` enum constants with a stable business meaning, such
  as `ROLE_NOT_FOUND` or `INVALID_PARAMETER`.
- Keep the local code, category, message, advice, and HTTP mapping adjacent in
  the enum declaration or an equally discoverable immutable definition.
- Do not name a status after a temporary implementation (`MYBATIS_ERROR`) when
  the contract is actually a stable business or data failure.
- Do not reuse an enum constant name or full code for a new meaning. If the
  meaning changes, create a new status and deprecate the old one through the
  compatibility process.

### 6.2 Ownership and package location

Place the status at the narrowest module boundary that owns its meaning:

- Platform-wide, reusable failures belong in base's
  `NexusStatusCode`.
- A domain-specific business failure belongs in the owning domain's
  `<domain>.domain.enums` package. The owning domain is the only source of
  that code's business semantics.
- A technical status belongs beside the technical boundary that emits it,
  such as `core.plugin.status.PluginStatusCode` for plugin infrastructure.
- `console` may define business-neutral console-contract statuses only when
  the console boundary owns the meaning; concrete user, role, permission,
  menu, or tenant statuses belong to their owning business module.
- Sibling `kernel` and `platform` modules must not import one another's
  status enums, events, or business packages. If a workflow spans both, use a
  neutral console/core contract or an application adapter that translates
  each side's status.

## 7. Extending the status-code catalog

Use this procedure for every new or changed status.

1. **Search before adding.** Search `NexusStatusCode`, domain status enums,
   technical status enums, and call sites for an existing code with the same
   meaning. Compare category, HTTP mapping, message, advice, and compatibility
   expectations, not only enum names.
2. **Decide ownership.** Classify the failure as platform-wide,
   domain-specific, or technical. Confirm the Maven module and package before
   writing the enum.
3. **Reserve the module segment.** Use an existing approved three-letter module
   code (`NEX`, `PLG`, or another registered code). A new module segment needs
   a registry/allowlist update and a review; never invent a code that collides
   with another module.
4. **Choose the category.** Select the `StatusCategory` that describes the
   failure semantics. If no current category fits, propose the category
   extension separately before allocating local codes.
5. **Allocate a local code.** Pick an unused four-digit local value in the
   owning module namespace. Check all statuses in that module, including
   sibling files and generated/registry definitions. Never renumber existing
   codes to make a list look contiguous.
6. **Define stable metadata.** Add an `UPPER_SNAKE_CASE` constant, bilingual
   message and advice (except an intentional success/no-advice status),
   category, local code, and intentional HTTP mapping. Text must explain the
   stable condition and a safe next action; do not put runtime IDs, secrets, or
   provider text in it.
7. **Use typed construction.** Throw with the new enum constant. Add raw-code
   registration only when an interop boundary genuinely requires it, and add
   the validation/allowlist entry in the same change.
8. **Add contract tests before integration.** Verify shape, local uniqueness,
   metadata, category, HTTP mapping, typed exception construction, cause
   preservation, and the endpoint/`R` mapping. For a public extension, test
   unknown and disallowed raw codes as well.
9. **Review compatibility.** Check clients, localized resources, dashboards,
   alert rules, persisted error payloads, event consumers, and configuration
   keys. Treat full codes, enum names, HTTP mappings, and message keys as
   compatibility surfaces. Document deprecations and migration behavior.

## 8. Contract-test requirements

Every status catalog or extension should have focused tests that prove:

- module is three uppercase letters;
- category is present and has the intended semantic family;
- local code is four digits;
- full code is nine characters and equals `module + category + local`;
- local/full codes are unique within the owning module catalog;
- enum constants use `UPPER_SNAKE_CASE` and the type ends in
  `StatusCode`;
- English and Chinese message/advice are nonblank for non-success statuses;
  intentional success/no-advice statuses are exempt, and legacy locale
  fallbacks are recorded;
- category `label()` and `priority()` are intentional and stable;
- HTTP mapping is intentional and consistent with the boundary contract;
- `NexusException.build(status)` returns the expected `code()` and preserves a
  supplied cause;
- raw-code interop rejects malformed or unallowlisted codes;
- endpoint infrastructure maps the status to `R.fail(...)` without exposing a
  cause or stack trace;
- module/package ownership and sibling-module dependency rules are respected.

## 9. Review checklists

### Exception checklist

- [ ] Is this an expected application failure, a translatable infrastructure
      failure, pure programmer misuse, cancellation, or a fatal error?
- [ ] Does the owning boundary choose a typed, reusable status code?
- [ ] Is the original cause preserved when translation occurs?
- [ ] Are interruption, cancellation, and fatal errors propagated correctly?
- [ ] Are duplicate logs, swallowed exceptions, stack traces, and sensitive
      values absent from the response?
- [ ] Is `R` construction limited to the endpoint/response boundary?

### Status-code checklist

- [ ] Is the code exactly `MODULE(3) + CATEGORY(2) + LOCAL(4)`?
- [ ] Is the module segment registered and owned by the correct boundary?
- [ ] Is the category semantic rather than an HTTP shortcut?
- [ ] Is the local code unused and stable?
- [ ] Are enum, message, advice, and HTTP mapping names intentional?
- [ ] Are bilingual text and compatibility consumers reviewed?

### Extension checklist

- [ ] Was the existing catalog searched before adding a code?
- [ ] Was platform/domain/technical ownership recorded?
- [ ] Were raw-code allowlists or registries updated only when required?
- [ ] Were contract and translation tests added?
- [ ] Were clients, logs, dashboards, events, and configuration consumers
      checked for compatibility?
