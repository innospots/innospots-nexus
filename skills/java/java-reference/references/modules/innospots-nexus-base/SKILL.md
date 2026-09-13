---
name: innospots-nexus-base
description: |
  Pure Java foundation: exceptions, status codes, JSON, domain contracts,
  condition DSL, execution SPI, events, HTTP utilities, and shared snapshots.
version: 1.1.0
---

# innospots-nexus-base

## Module Overview

Middleware-free shared foundation for serializable contracts and dependency-light utilities.

**What it can do:**

| Capability | Description |
|------------|-------------|
| **Status & Exceptions** | Bilingual status codes and `NexusException` |
| **JSON** | `Jsons` facade, masking, value conversion |
| **Domain Contracts** | Conditions, data exchange, field metadata, snapshots |
| **Execution** | Executor SPI with context and records |
| **Infrastructure Utils** | HTTP client, crypto, IDs, dates, metrics, threads |
| **I18n & Resources** | Locale resolution, file storage SPI |

## Class Reference

### Package `config`

| Class | Type | Description |
|-------|------|-------------|
| `NexusConfig` | `class` | Immutable configuration store that wraps a flat key-value map. |

### Package `domain.condition`

| Class | Type | Description |
|-------|------|-------------|
| `DatabaseFactorStatement` | `class` | Renders a `Factor` into a SQL expression string. |
| `EmbedCondition` | `class` | A condition that supports nesting — each `EmbedCondition` can contain child sub-conditions recursively. |
| `Factor` | `class` | A single filter criterion composed of a field code, an operator, a value, and an optional value type. |
| `FactorStatementBuilder` | `class` | Factory that selects the appropriate `IFactorStatement` implementation based on the target `Mode`. |
| `IFactorStatement` | `interface` | Strategy interface for rendering a `Factor` into a mode-specific expression string (SQL, script, or Java). |
| `Mode` | `enum` | The target output mode for condition statements. |
| `Operator` | `enum` | Comparison operators used in filter conditions. |
| `Relation` | `enum` | Logical combinators for joining multiple `Factor` conditions. |
| `ScriptFactorStatement` | `class` | Renders a `Factor` into a script/expression language (e. |
| `SimpleCondition` | `class` | A flat list of `Factor` conditions joined by a single `Relation`. |

### Package `domain.data`

| Class | Type | Description |
|-------|------|-------------|
| `DataBody` | `class` | A self-timing data payload. |
| `DataOperation` | `enum` | The type of data operation to perform on a target datasource. |
| `DataPage` | `record` | Immutable paginated data container. |
| `DataRequest` | `class` | A request to perform a `DataOperation` on a named target datasource. |
| `DataResponse` | `class` | A generic response envelope for data operations. |
| `DataSchema` | `class` | Describes the structure of a data payload: a list of `DomainField`s plus free-form configuration entries. |

### Package `domain.dictionary`

| Class | Type | Description |
|-------|------|-------------|
| `DictionaryItem` | `record` | A single key-value entry within a dictionary type. |
| `DictionaryType` | `record` | A dictionary type (e. |

### Package `domain.enums`

| Class | Type | Description |
|-------|------|-------------|
| `BasicStatus` | `enum` | Universal enable/disable status used across domain entities. |

### Package `domain.field`

| Class | Type | Description |
|-------|------|-------------|
| `DomainField` | `class` | Describes a field within a domain schema or data structure. |
| `FieldScope` | `enum` | The role or boundary a field belongs to within a domain schema. |
| `FieldValueType` | `enum` | Supported value types for domain fields. |
| `ParamField` | `class` | A parameter field with a specific `FieldValueType`, required flag, and optional default value. |
| `SelectOption` | `record` | A selectable option with a stored value and a display label. |

### Package `domain.identity`

| Class | Type | Description |
|-------|------|-------------|
| `RoleSnapshot` | `record` | A role definition with a unique identifier, display name, and programmatic code. |
| `UserGroupSnapshot` | `record` | A user group/team with a hierarchy (parent group), a head user, and assistant users. |
| `UserSnapshot` | `class` | Session/transport snapshot of a user. |

### Package `domain.organization`

| Class | Type | Description |
|-------|------|-------------|
| `OrganizationSnapshot` | `record` | An organization/tenant with locale, currency, and branding preferences. |

### Package `domain.project`

| Class | Type | Description |
|-------|------|-------------|
| `ProjectSnapshot` | `class` | A project within an organization. |

### Package `domain.request`

| Class | Type | Description |
|-------|------|-------------|
| `Pagination` | `class` | Shared pagination defaults and normalization for query requests. |
| `SimpleQueryRequest` | `record` | Paginated query request with a keyword filter. |

### Package `domain.response`

| Class | Type | Description |
|-------|------|-------------|
| `PageResult` | `record` | Paginated API response wrapper. |
| `R` | `record` | Generic API response wrapper with success/failure status, result code, message, optional data payload, and internationalized display message for frontend rendering on failures. |

### Package `events`

| Class | Type | Description |
|-------|------|-------------|
| `DomainEvent` | `interface` | Base interface for all domain events. |
| `EventBus` | `class` | Simple in-memory event bus. |
| `EventHandler` | `interface` | Functional interface for domain event handlers. |

### Package `exception`

| Class | Type | Description |
|-------|------|-------------|
| `NexusException` | `class` | Base runtime exception for the platform. |

### Package `execution`

| Class | Type | Description |
|-------|------|-------------|
| `ExecutionContext` | `class` | Execution context for a single run. |
| `ExecutionRecord` | `class` | Immutable record of a completed execution. |
| `ExecutionStatus` | `enum` | Lifecycle states for an execution from creation through to completion. |
| `Executor` | `interface` | Core execution unit interface. |

### Package `http`

| Class | Type | Description |
|-------|------|-------------|
| `HttpClientBuilder` | `class` | Fluent builder for `CloseableHttpClient` instances with sensible defaults (10s connect timeout, 30s response timeout, redirects enabled). |
| `HttpResult` | `record` | Immutable result of an HTTP request. |
| `HttpUtils` | `class` | Convenience methods for HTTP GET and POST (JSON) requests. |

### Package `i18n`

| Class | Type | Description |
|-------|------|-------------|
| `I18n` | `@interface` | Jackson annotation that marks a field for automatic i18n translation during serialization. |
| `I18nConverter` | `class` | Core i18n translation engine. |
| `I18nMessageResolver` | `interface` | Strategy for resolving an i18n key to a localized message string. |
| `I18nObject` | `class` | A locale-to-value map representing an internationalized string. |

### Package `json`

| Class | Type | Description |
|-------|------|-------------|
| `Jsons` | `class` | Central JSON utility facade built on Jackson. |
| `MaskStrategy` | `enum` | Predefined masking strategies for sensitive data during JSON serialization. |
| `MaskValue` | `@interface` | Marks a field or accessor for masking during JSON serialization. |
| `MaskingModule` | `class` | Jackson `Module` that activates field-level value conversion and masking. |
| `ValueConverter` | `@interface` | Marks a field or accessor for value conversion during JSON serialization. |

### Package `mapstruct`

| Class | Type | Description |
|-------|------|-------------|
| `BaseBeanConverter` | `interface` | Base MapStruct-like converter interface between domain models and persistence entities. |
| `BaseMapperConfig` | `interface` | Shared MapStruct mapper configuration used by all domain mappers. |
| `BaseMapperSupport` | `class` | Utility for mapping collections via a mapping function. |

### Package `resources`

| Class | Type | Description |
|-------|------|-------------|
| `FileResource` | `record` | A file resource with its content stream and metadata flags. |
| `MetaResource` | `record` | Immutable metadata record for a stored resource. |
| `ResourceEvent` | `record` | Domain event published when resource metadata is saved/persisted. |
| `ResourcePatternResolver` | `class` | Resolves resource location patterns (e. |
| `ResourceStore` | `interface` | Abstraction for persisting and retrieving binary resources. |

### Package `status`

| Class | Type | Description |
|-------|------|-------------|
| `NexusStatusCode` | `enum` | Platform-wide status codes with bilingual (EN/ZH) messages and advice, grouped by `StatusCategory`. |
| `StatusCategory` | `enum` | Categorises status codes by concern area. |
| `StatusCode` | `interface` | Interface for a composable status code consisting of a 3-letter module code, a `StatusCategory` (2 digits), and a 4-digit local code. |
| `StatusCodeRules` | `class` | Validation rules for status code formatting. |

### Package `thread`

| Class | Type | Description |
|-------|------|-------------|
| `AsyncExecutors` | `class` | Global async executor facade backed by a singleton `NexusThreadPoolExecutor`. |
| `NexusThreadFactory` | `class` | Named `ThreadFactory` that produces threads with a configurable prefix (default `nexus-worker`) and sequence number. |
| `NexusThreadPoolExecutor` | `class` | Custom `ThreadPoolExecutor` that captures and propagates `TLC` context from the submitting thread to the worker thread. |
| `TLC` | `class` | Thread-Local Context — a typed `ThreadLocal` map for propagating cross-cutting state (trace ID, tenant ID, user ID, workspace ID, etc. |
| `ThreadPoolBuilder` | `class` | Fluent builder for `NexusThreadPoolExecutor` instances. |

### Package `util`

| Class | Type | Description |
|-------|------|-------------|
| `BeanUtils` | `class` | Bean property copy and conversion utility wrapping Hutool's `BeanUtil`. |
| `Checks` | `class` | Precondition checks that fail with `NexusException` and `NexusStatusCode#INVALID_PARAMETER`. |
| `CryptoUtils` | `class` | Cryptographic utilities: password hashing (BCrypt), symmetric encryption (AES-GCM), and asymmetric encryption (RSA/OAEP). |
| `DateTimeUtils` | `class` | Date and time formatting and parsing utilities. |
| `EnvUtils` | `class` | Environment property resolver with override support. |
| `IdGenerator` | `class` | ID generation utilities: Snowflake-based distributed IDs, random IDs with configurable character sets, timestamp-prefixed IDs, and batch generation. |
| `MetricsSnapshot` | `record` | A point-in-time snapshot of a metrics counter/timer. |
| `MetricsUtils` | `class` | Micrometer-based metrics facade. |
| `StringUtils` | `class` | String utilities: blank checks, placeholder replacement (`${key`} and `{{key`}}), camelCase/underscore conversion, and random key generation. |

## References

| Package | Reference |
|---------|-----------|
| `config` | [`references/config.md`](references/config.md) |
| `domain.condition` | [`references/domain-condition.md`](references/domain-condition.md) |
| `domain.data` | [`references/domain-data.md`](references/domain-data.md) |
| `domain.dictionary` | [`references/domain-dictionary.md`](references/domain-dictionary.md) |
| `domain.enums` | [`references/domain-enums.md`](references/domain-enums.md) |
| `domain.field` | [`references/domain-field.md`](references/domain-field.md) |
| `domain.identity` | [`references/domain-identity.md`](references/domain-identity.md) |
| `domain.organization` | [`references/domain-organization.md`](references/domain-organization.md) |
| `domain.project` | [`references/domain-project.md`](references/domain-project.md) |
| `domain.request` | [`references/domain-request.md`](references/domain-request.md) |
| `domain.response` | [`references/domain-response.md`](references/domain-response.md) |
| `events` | [`references/events.md`](references/events.md) |
| `exception` | [`references/exception.md`](references/exception.md) |
| `execution` | [`references/execution.md`](references/execution.md) |
| `http` | [`references/http.md`](references/http.md) |
| `i18n` | [`references/i18n.md`](references/i18n.md) |
| `json` | [`references/json.md`](references/json.md) |
| `mapstruct` | [`references/mapstruct.md`](references/mapstruct.md) |
| `resources` | [`references/resources.md`](references/resources.md) |
| `status` | [`references/status.md`](references/status.md) |
| `thread` | [`references/thread.md`](references/thread.md) |
| `util` | [`references/util.md`](references/util.md) |
