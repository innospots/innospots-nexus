# Naming Conventions

## Java

| Element | Convention | Example |
|---------|-----------|---------|
| Class | UpperCamelCase | `NexusConfig`, `DataPage`, `HttpUtils` |
| Interface | UpperCamelCase | `IFactorStatement`, `ResourceStore`, `Executor` |
| REST endpoint | UpperCamelCase with `Endpoint` suffix | `RoleEndpoint`, `ConsoleEndpoint` |
| MyBatis-Plus DAO | UpperCamelCase with `Dao` suffix | `RoleDao`, `UserRoleDao` |
| Persistence entity | UpperCamelCase with `Entity` suffix | `RoleEntity`, `UserRoleEntity` |
| Request record | UpperCamelCase with `Request` suffix | `RoleCreateRequest`, `UserPageRequest` |
| View record | UpperCamelCase with `Vo` suffix | `RoleInfoVo`, `UserProfileVo` |
| Configuration object | UpperCamelCase with `Config` suffix | `SecurityConfig`, `StorageConfig` |
| Bean converter | UpperCamelCase with `Converter` suffix | `RoleConverter`, `UserConverter` |
| Business status code | UpperCamelCase with `StatusCode` suffix | `RoleStatusCode`, `UserStatusCode` |
| Domain event | UpperCamelCase with `Event` suffix | `RoleCreatedEvent`, `UserDisabledEvent` |
| Event handler | UpperCamelCase with `EventHandler` suffix | `RoleCreatedEventHandler` |
| Enum | UpperCamelCase (singular) | `Mode`, `Operator`, `BasicStatus` |
| Enum constants | UPPER_SNAKE_CASE | `ENABLED`, `IS_NULL`, `GREATER_EQUAL` |
| Record | UpperCamelCase | `DataPage`, `HttpResult`, `PageResult` |
| Annotation | UpperCamelCase with `@` prefix | `@MaskValue`, `@ValueConverter`, `@I18n` |
| Method | lowerCamelCase | `getInputString()`, `normalizeValue()`, `nextId()` |
| getter/setter | `getXxx()` / `isXxx()` / `setXxx()` | `getInput()`, `isSuccessful()`, `hasNext()` |
| Static factory | `of()`, `create()`, `from()`, `named()` | `DataPage.of()`, `DataRequest.create()`, `IdGenerator.from()` |
| Field/param | lowerCamelCase | `pageNo`, `pageSize`, `executionId` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_QUEUE_CAPACITY`, `PLACEHOLDER_PREFIX_1` |
| Package | lowercase, dot-separated, singular | `domain.condition`, `domain.field`, `util` |

## Package Naming

- Non-business modules and packages, such as `base`, `script`, utilities, and
  reusable technical capabilities, are organized by function.
- Business code is organized by business domain first. Use domain roots such
  as `user`, `role`, `menu`, and `resource` before dividing code by technical
  responsibility.
- Inside each business domain, use responsibility packages such as
  `endpoint`, `dao`, `domain`, `converter`, `operator`, `service`,
  `interceptor`, `handler`, and `listener` as needed. Create only packages
  that have a concrete responsibility; do not scaffold empty layers.
- Use singular nouns: `domain.condition` not `domain.conditions`. The
  `domain.enums` package is an explicit project convention and exception.
- No "impl", "util" sub-packages for implementation details.
- Infrastructure belongs in adapter/plugin modules, not in domain packages.
- `domain` contains business data types and business-facing object models. Use
  these sub-packages consistently:
  - `domain.entity` for database persistence entities. Types must use the
    `Entity` suffix.
  - `domain.request` for endpoint request records. Types must use the
    `Request` suffix.
  - `domain.vo` for endpoint response records. Types must use the `Vo` suffix;
    do not use `VO` or `Response` as the suffix for types in this package.
  - `domain.model` for internal business models. Name each type after its
    actual business concept without a mandatory technical suffix.
  - `domain.enums` for business enumeration types. Name each enum after the
    singular business concept it represents. Domain-specific status code enums
    also belong here and use the `StatusCode` suffix.
  - `domain.event` for domain event contracts published by this business
    domain. Event types must use the `Event` suffix.
- System configuration is not part of the business domain. Configuration
  classes and configuration-file binding objects belong in a module-level
  `config` package and use the `Config` suffix.
- Module packages with application contracts should use these responsibility
  names consistently:
  - `endpoint` exclusively for HTTP API boundary types declared with Jakarta
    REST annotations. Endpoint types may be concrete classes or explicitly
    requested contract interfaces. Every public type in this package must use
    the `Endpoint` suffix, such as `RoleEndpoint`.
  - `api` for non-HTTP application-facing contracts exposed to other modules
    or upper layers. Jakarta REST endpoint types must not be placed in `api`.
  - `operator` for data-operation ports that usually coordinate DAO access.
    Operators implement straightforward data-oriented operations, normally
    against one DAO but may coordinate multiple DAOs when no complex business
    workflow is involved.
  - `service` for business services that contain non-trivial workflow,
    orchestration, validation, cross-domain logic, or coordination across
    multiple operators.
  - `dao` for MyBatis-Plus persistence mapper interfaces and direct database
    access contracts. DAO interfaces must use the `Dao` suffix and extend
    `BaseMapper<EntityType>`.
  - `converter` for MapStruct bean converters. Converter interfaces must use
    the `Converter` suffix.
  - `interceptor` for request, invocation, or execution interception.
  - `handler` for focused event, command, protocol, or strategy handling.
    Domain event consumers should use the `EventHandler` suffix.
  - `listener` for event listeners. Use the correct `listener` spelling.

```text
com.innospots.nexus.kernel
  ├── user
  │   ├── endpoint
  │   ├── dao
  │   ├── domain
  │   │   ├── entity
  │   │   ├── request
  │   │   ├── vo
  │   │   ├── model
  │   │   ├── enums
  │   │   └── event
  │   ├── converter
  │   ├── operator
  │   ├── service
  │   └── handler
  └── role
      ├── endpoint
      ├── dao
      ├── domain
      ├── converter
      ├── operator
      └── service
```

```java
com.innospots.nexus.base
  ├── config           // configuration store
  ├── domain           // domain primitives
  │   ├── condition    // filter/condition DSL
  │   ├── data         // data operation contracts
  │   ├── field        // field metadata
  │   └── ...
  ├── events           // event bus
  ├── exception        // base exception
  ├── execution        // executor SPI
  ├── http             // HTTP client
  ├── i18n             // internationalization
  ├── json             // JSON utilities
  ├── mapstruct        // mapping support
  ├── resources        // resource store SPI
  ├── status           // status code system
  ├── thread           // concurrency utilities
  ├── ui               // UI descriptors
  └── util             // general utilities
```

## File Naming

- One top-level public class per file.
- File name matches class name (case-sensitive).
- Test files: `{ClassName}Test.java` placed under `src/test/java/` in the
  same package structure.
