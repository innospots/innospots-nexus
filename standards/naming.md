# Naming Conventions

Names must communicate the business concept, scope, responsibility, and
behavior of the code. Correct casing is necessary but not sufficient: a name
that is syntactically valid but semantically vague is not acceptable.

## Naming Decision Order

Choose a name in this order:

1. **Business concept** — use the term from the current domain vocabulary,
   such as `Role`, `Tenant`, `PermissionResource`, or `Plugin`.
2. **Scope or variant** — add a qualifier only when it distinguishes a real
   ownership or runtime boundary, such as `PlatformUser`, `TenantMember`,
   `ClasspathPluginDiscovery`, or `CurrentAuthorizationEndpoint`.
3. **Operation or view purpose** — describe the specific use case when the
   type is not the complete concept, such as `RoleStatusUpdateRequest` or
   `DictionaryTypeOptionVo`.
4. **Technical responsibility** — end with the project suffix that tells the
   reader what the type does, such as `Entity`, `Dao`, `Endpoint`, `Service`,
   `Registry`, or `Resolver`.

The usual construction is:

```text
[scope or variant] + business concept + [operation or view purpose] + responsibility
```

Use the shortest name that remains unambiguous in its package. Do not repeat
information already expressed by the package or suffix. For example,
`RoleService` is sufficient inside the role domain; `RoleBusinessService` is
redundant. `RoleDao` already means data access; do not name it `RoleDataDao`.

## Vocabulary Rules

- Use one stable English term for one concept across packages, types, fields,
  methods, endpoints, database objects, and tests.
- Prefer domain nouns over technical placeholders. Use `role`, `tenant`,
  `credential`, or `capability`, not `data`, `info`, `item`, or `object`, unless
  the broader word is the actual modeled concept.
- Use precise qualifiers that identify a real difference in ownership,
  transport, source, lifecycle, or representation. `PlatformUser` and
  `TenantUser` are meaningful; `NewUser` and `CommonUser` usually are not.
- Do not use `Common`, `General`, `Base`, `Default`, `Simple`, or `Generic` as a
  substitute for defining responsibility. These words are allowed only when
  the distinction is concrete, such as `BaseEntity`, `DefaultPluginManager`,
  or `SimpleQueryRequest`.
- Do not encode implementation details that callers do not need. Prefer
  `PasswordDecryptor` over a concrete algorithm name in the contract; use
  `RsaPasswordDecryptor` for the algorithm-specific implementation.
- Avoid overloaded terms. Within one bounded context, `code`, `key`, `id`,
  `name`, `status`, and `type` must each have a documented and consistent
  meaning.
- Never preserve a poor historical name merely for visual consistency. Existing
  names are evidence of vocabulary, not automatic precedents.

## Java Naming

| Element | Convention | Example |
|---------|-----------|---------|
| Class | UpperCamelCase noun or noun phrase | `NexusConfig`, `ThreadPoolBuilder` |
| Interface | UpperCamelCase responsibility; no mandatory `I` prefix | `ResourceStore`, `Executor` |
| REST endpoint | UpperCamelCase with `Endpoint` suffix | `RoleEndpoint`, `NavigationMenuEndpoint` |
| MyBatis-Plus DAO | UpperCamelCase with `Dao` suffix | `RoleDao`, `UserOauthIdentityDao` |
| Persistence entity | UpperCamelCase with `Entity` suffix | `RoleEntity`, `UserRoleEntity` |
| Request record | Resource + operation/purpose + `Request` | `RoleCreateRequest`, `UserPageRequest` |
| View record | Resource + view purpose + `Vo` | `RoleOptionVo`, `UserProfileVo` |
| Internal domain model | Business concept without a mandatory suffix | `Conversation`, `KernelUser` |
| Configuration object | UpperCamelCase with `Config` suffix | `SecurityConfig`, `PluginRuntimeConfig` |
| Bean converter | UpperCamelCase with `Converter` suffix | `RoleConverter`, `CronConverter` |
| Business status code | UpperCamelCase with `StatusCode` suffix | `PluginStatusCode`, `UserStatusCode` |
| Domain event | Past-tense fact with `Event` suffix | `RoleCreatedEvent`, `PluginStoppedEvent` |
| Event handler | Handled event + `EventHandler` | `RoleCreatedEventHandler` |
| Enum | UpperCamelCase singular business concept | `Mode`, `PluginState`, `BasicStatus` |
| Enum constant | UPPER_SNAKE_CASE | `ENABLED`, `IS_NULL`, `GREATER_EQUAL` |
| Record | UpperCamelCase noun or noun phrase | `DataPage`, `HttpResult`, `PageResult` |
| Annotation | UpperCamelCase adjective or noun, used with `@` | `@MaskValue`, `@ValueConverter` |
| Method | lowerCamelCase verb or predicate phrase | `normalizeValue()`, `hasAvailableThread()` |
| JavaBean accessor | `getXxx()` / `isXxx()` / `setXxx()` | `getInput()`, `isSuccessful()` |
| Static factory | `of()`, `from()`, `create()`, `named()`, or a precise variant | `DataPage.of()`, `IdGenerator.from()` |
| Field/parameter/local | lowerCamelCase noun or predicate | `pageNo`, `executionId`, `required` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_QUEUE_CAPACITY`, `TABLE_NAME` |
| Package | lowercase, dot-separated, normally singular | `domain.condition`, `plugin.runtime` |
| Type parameter | One capital letter, or short role name when needed | `T`, `K`, `V`, `Model`, `Entity` |

Do not add an `I` prefix to new interfaces. Historical types such as
`IFactorStatement` and `IWatcher` may remain until a separately approved
compatibility refactor; they are not templates for new contracts.

## Type Responsibility Names

Choose a responsibility suffix only when the type actually owns that
responsibility.

| Suffix | Use when the type | Current-style example |
|--------|-------------------|-----------------------|
| `Service` | orchestrates a cohesive business workflow or cross-domain behavior | `PermissionGrantService` |
| `Operator` | performs straightforward data-oriented business operations over DAOs | `PlatformUserOperator` |
| `Manager` | owns the lifecycle and coordinated state of a runtime facility | `PluginManager`, `QuartzScheduleManager` |
| `Registry` | stores and looks up registrations by stable identity | `CapabilityRegistry`, `ExtensionRegistry` |
| `Catalog` | exposes an indexed or discovered set of definitions for inspection | `PluginCatalog` |
| `Router` | selects one destination or provider from available registrations | `CapabilityRouter` |
| `Provider` | supplies a capability through an extension or SPI boundary | `CapabilityProvider` |
| `Factory` | creates instances whose construction is variable or encapsulated | `CapabilityProviderFactory` |
| `Resolver` | turns an input or requirement into a resolved result | `DependencyResolver`, `I18nMessageResolver` |
| `Discovery` | discovers implementations or declarations from an external source | `ClasspathPluginDiscovery` |
| `Loader` | loads a known resource from a location | `UiSpecLoader` |
| `Parser` | converts serialized syntax into a structured representation | `UiSpecParser` |
| `Validator` | checks a reusable rule set without owning a workflow | `PasswordValidator`, `UiSpecValidator` |
| `Builder` | incrementally assembles a value with a final build step | `ThreadPoolBuilder` |
| `Repository` | abstracts storage when it is a domain or infrastructure port, not a MyBatis mapper | `ConversationRepository` |
| `Store` | provides key/resource-oriented persistence as a reusable port | `CredentialStore`, `ResourceStore` |
| `Facade` | presents one application-facing entry point over several internal collaborators | `AuthFacade` |
| `Holder` | owns the current local runtime value and its controlled replacement | `ServiceNodeHolder` |
| `Handler` | handles one focused event, protocol, logging, or extension callback | `InvocationLogHandler` |
| `Interceptor` | surrounds an invocation or request before/after execution | `XxxInterceptor` |
| `Listener` | observes lifecycle or external notifications without selecting a strategy | `XxxListener` |

Do not use `Manager`, `Helper`, `Processor`, `Handler`, or `Utils` as a generic
fallback. If none of the responsibilities above fits, name the type after the
specific capability it provides. Utility classes are permitted only for
cohesive, stateless operations and use a plural or established utility name,
such as `Checks`, `Jsons`, `StringUtils`, or `DateTimeUtils`; never create
`CommonUtils` or `BaseHelper`.

`Default` identifies the standard implementation of a contract when multiple
implementations are valid, such as `DefaultPluginManager`. `Abstract`
identifies an incomplete base class designed for inheritance, such as
`AbstractWatcher`. Neither word may hide an unclear responsibility.

## Domain Type Names

### Entities and models

- Name an entity after the persisted business record and add `Entity`:
  `RoleEntity`, `PermissionGrantEntity`, `PlatformUserPasswordEntity`.
- Name association entities from both sides or from the association concept:
  `RoleBindingEntity`, `OrganizationMemberEntity`.
- Name internal models after business concepts without `Dto`, `Pojo`, `Bean`,
  `Data`, or a mandatory `Model` suffix: `Conversation`, `SessionMessage`,
  `KernelUser`.
- Use `Base` only for an intentional inheritance abstraction with a defined
  scope, such as `BaseEntity`, `TenantBaseEntity`, and `WorkspaceBaseEntity`.

### Requests

Request names express the resource first and the allowed operation second:

| Purpose | Pattern | Example |
|---------|---------|---------|
| Create | `XxxCreateRequest` | `RoleCreateRequest` |
| Update mutable attributes | `XxxUpdateRequest` | `DictionaryItemUpdateRequest` |
| Update one lifecycle attribute | `XxxStatusUpdateRequest` | `MenuStatusUpdateRequest` |
| Paginated query | `XxxPageRequest` | `UserPageRequest` |
| Tree query | `XxxTreeRequest` | `MenuTreeRequest` |
| Reorder | `XxxOrderRequest` | `MenuOrderRequest` |
| Add association | `XxxAddRequest` | `RoleBindingAddRequest` |
| Replace a complete association set | `XxxReplaceRequest` | `PermissionGrantReplaceRequest` |
| Register through a variant | `XxxPasswordRegisterRequest` | `UserPasswordRegisterRequest` |

Do not use one vague `XxxRequest` for operations with different mutation
rights. Do not add `Dto`, `Command`, or `Payload` when `Request` already
describes the transport role.

### Views

- Use plain `XxxVo` for the primary management/detail representation.
- Add a use-case qualifier before `Vo` for a deliberately smaller or different
  projection: `RoleOptionVo`, `UserProfileVo`, `NavigationMenuVo`.
- Name collection components in the singular because the type represents one
  element, even when returned as a list.
- Do not use uppercase `VO`, `Response`, `Result`, or `Dto` for types in
  `domain.vo`. `R<T>` and `PageResult<T>` already express transport/result
  wrapping.

### Events and lifecycle types

- Name events as facts that already occurred: `TenantCreatedEvent`,
  `PluginFailedEvent`, `SessionMessageCreatedEvent`.
- Use a present-tense command name only for an actual command contract; do not
  disguise commands as events.
- Use `State` for a runtime state machine, `Status` for business availability
  or persisted lifecycle status, `Mode` for a selected operating mode, and
  `Type` for a closed classification.

## Method Naming

Method names describe observable behavior, not implementation steps.

### Query verbs

| Prefix | Meaning | Example |
|--------|---------|---------|
| `get` | returns a directly addressed or required value; absence follows the declared contract | `getRole(roleId)` |
| `find` | searches for one value that may be absent | `findByUserId(userId)` |
| `list` | returns a finite collection, optionally filtered | `listActiveTenantIds(userId)` |
| `page` | returns a paginated result | `pageUsers(request)` |
| `count` | returns a numeric count | `countActivePlugins()` |
| `load` | reads a known resource from storage or a location | `loadPage(path)` |
| `discover` | scans a source for implementations or declarations | `discoverPlugins()` |
| `resolve` | derives one effective result from inputs, defaults, or dependencies | `resolveDependencies()` |
| `snapshot` | returns a point-in-time immutable copy | `snapshot()` |

Do not use `getAll`; use `list`. Do not use `queryXxx` when `find`, `list`,
`page`, or `count` states the result shape more precisely.

### Command and lifecycle verbs

- Use `create` for a business creation operation and `of`/`from`/`named` for
  value factories. A builder terminates with `build`.
- Use `add`/`remove` for membership in an existing collection,
  `register`/`unregister` for a keyed runtime registration, and
  `subscribe`/`unsubscribe` for event subscriptions.
- Use `update` for partial mutable state, `replace` for a complete set or
  value, and `save` only when the same operation intentionally covers insert
  and update semantics.
- Use explicit business verbs when they are clearer than CRUD: `grant`,
  `revoke`, `enable`, `disable`, `freeze`, `unfreeze`, `publish`, `route`.
- Use symmetrical lifecycle pairs: `initialize`/`destroy`, `start`/`stop`,
  `open`/`close`, `register`/`unregister`.
- Use `validate` when invalid input causes an exception; use `isValid` when the
  result is a boolean. Use `normalize` when the method returns or applies a
  canonical representation.
- Use `toXxx` for conversion to a target representation, `from` for a static
  factory from a source value, and `xxxToYyy` when a converter exposes several
  explicit mappings, such as `modelToEntity`.
- Reserve `handle` for a type whose responsibility is actually a handler.
  Avoid vague verbs such as `process`, `execute`, `doWork`, and `operate` unless
  the abstraction itself defines that established operation.

### Boolean predicates

- Boolean methods start with `is`, `has`, `can`, `supports`, `contains`,
  `matches`, or another predicate verb: `isBlank`, `hasAvailableThread`,
  `canStart`, `supportsType`.
- Boolean fields and record components use an adjective or past participle
  without an `is` prefix: `enabled`, `required`, `administrator`, `closed`.
  JavaBean getters may expose them as `isEnabled()`.
- Prefer positive names. Use `enabled` rather than `notDisabled` and
  `hasChildren` rather than `childrenNotEmpty`.
- A predicate name must not hide mutation or I/O.

## Field, Parameter, and Local Names

- Use the qualified concept identifier, such as `roleId`, `tenantId`,
  `pluginId`, or `bindingId`. Use bare `id` only when the enclosing scope makes
  the concept unmistakable and no second identifier is present.
- Stable business identifiers use their business term, such as `roleCode`,
  `menuKey`, or `extensionKey`; do not call them generic IDs.
- Collections and arrays use plural nouns: `roles`, `pluginIds`,
  `registrations`. Maps describe both value purpose and, when needed, their
  key: `registrationsByKey`, `providersByPluginId`.
- Singular variables represent one value. Do not name a collection `roleList`
  or `userCollection` when `roles` or `users` is sufficient.
- Counts end in `Count`; zero-based locations use `index`; business ordering
  uses `order` or `sortOrder`; capacities use `Capacity`.
- A point in time ends in `At`, such as `createdAt`, `expiresAt`, or
  `discoveredAt`. A calendar value may use `Date`, and an elapsed amount uses a
  duration noun.
- Numeric durations include the unit unless the type makes it explicit:
  `timeoutMillis`, `keepAliveSeconds`, `retryDelayMillis`. Avoid bare
  `timeout`, `delay`, or `interval` when the unit is ambiguous.
- Paired bounds use symmetrical names: `startTime`/`endTime`,
  `minLength`/`maxLength`, `source`/`target`, `previous`/`current`.
- Lambdas may use short names only when their meaning is obvious within a few
  lines. Prefer `registration` to `r` in nested or business-significant logic.
- Do not use `temp`, `tmp`, `foo`, `bar`, `obj`, `data`, `result`, `value`, or
  `flag` when a more specific name is available. Narrow local scopes may use
  `result` or `value` only when the method and type make the meaning explicit.
- Parameters express the caller-visible concept, not the called framework's
  internal terminology.

## Abbreviations and Acronyms

- In UpperCamelCase and lowerCamelCase names, treat an acronym as a word:
  `HttpClientBuilder`, `Jsons`, `RsaPasswordDecryptor`, `OauthIdentity`,
  `apiVersion`, `pluginId`.
- Use the established project forms `Id`, `Url`, `Uri`, `Http`, `Json`, `Rsa`,
  `Oauth`, `Dao`, `Vo`, `Api`, `Ui`, `Db`, and `Io`.
- Use all capitals only for constants and enum constants. Do not create new
  all-capital type names. Existing names such as `TLC` are historical
  exceptions, not a convention.
- Do not alternate spellings such as `ID`/`Id`, `URL`/`Url`, `DAO`/`Dao`, or
  `VO`/`Vo` in Java identifiers.
- Avoid new abbreviations unless they are standard in the domain and remove
  substantial noise. Prefer `configuration` to `cfg`, `request` to `req`, and
  `context` to `ctx` in public contracts.

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
- Do not create `impl`, `common`, `misc`, or `util` subpackages to hide unclear
  ownership. Implementation classes stay with their contract or in a precise
  responsibility package such as `runtime`, `adapter`, or `persistence`.
- Infrastructure belongs in adapter/plugin modules, not in domain packages.
- Package segments must express boundaries, not individual class names.
- `domain` contains business data types and business-facing object models. Use
  these subpackages consistently:
  - `domain.entity` for database persistence entities. Types use `Entity`.
  - `domain.request` for endpoint request records. Types use `Request`.
  - `domain.vo` for endpoint response records. Types use `Vo`.
  - `domain.model` for internal business models without a mandatory suffix.
  - `domain.enums` for business enumeration types and domain status codes.
  - `domain.event` for events published by the business domain.
- System configuration belongs in a module-level `config` package, not under
  `domain`, and configuration types use `Config`.
- Module packages with application contracts use these names consistently:
  - `endpoint` exclusively for Jakarta REST HTTP boundaries.
  - `api` for non-HTTP contracts exposed to other modules or upper layers.
  - `operator` for straightforward data-operation boundaries over DAOs.
  - `service` for workflows, orchestration, validation, or cross-domain logic.
  - `dao` for MyBatis-Plus persistence mappers.
  - `converter` for MapStruct and focused representation converters.
  - `interceptor`, `handler`, and `listener` for their exact runtime roles.
  - `runtime` for concrete lifecycle coordination and runtime state.
  - `contract` for SPI contracts when separating them materially improves the
    extension boundary; do not add it mechanically to every domain.
  - `declaration` for immutable metadata declarations consumed by a runtime.
  - `discovery` for implementation/declaration discovery from external
    locations.

```text
com.innospots.nexus.console
  └── role
      ├── endpoint
      ├── dao
      ├── domain
      │   ├── entity
      │   ├── request
      │   ├── vo
      │   ├── model
      │   ├── enums
      │   └── event
      ├── converter
      ├── operator
      ├── service
      └── handler
```

```text
com.innospots.nexus.core.plugin
  ├── contract
  ├── declaration
  ├── discovery
  ├── capability
  ├── lifecycle
  ├── event
  └── runtime
```

## Persistence Names

- Java persistence types and fields follow the Java rules above. Database
  tables and columns use lowercase snake_case.
- Nexus-owned tables use the `nx_` prefix and a singular business concept:
  `nx_role`, `nx_permission_grant`, `nx_platform_user_password`.
- Every entity declares `TABLE_NAME`; annotations reference the constant rather
  than repeating the literal.
- Column names mirror the Java business term: `roleId` maps to `role_id`,
  `createdAt` maps to `created_at`.
- Index names are explicit and table-scoped. Use `uk_` for unique indexes and
  `idx_` for non-unique indexes, followed by the table concept and indexed
  purpose, such as `uk_nx_role_owner_code` or `idx_nx_role_status`.
- Association-table names identify both sides or the association concept.
- Entity ID prefixes returned by `idPrefix()` are short, lowercase, stable,
  and unique enough to identify the record family. Do not derive them from a
  temporary module name or change them during ordinary refactoring.
- Event type strings and configuration keys use stable lowercase dotted names,
  such as `role.created`. Treat them as public identifiers, not display text.

## File and Test Naming

- Keep one top-level public type per Java file.
- The file name exactly matches the top-level public type, including case.
- `package-info.java` documents a package and is the only standard exception to
  the type/file-name rule.
- Test source files mirror the production package under `src/test/java`.
- A focused unit test uses `{TypeName}Test.java`, such as
  `PasswordValidatorTest.java`.
- A test that verifies a family of structural contracts may use
  `{Concept}ContractsTest.java`, such as `RoleEntityContractsTest.java`.
- Test methods use lowerCamelCase behavior phrases without a `test` prefix:
  `createRejectsMissingLegalName`, `roleEntitiesDeclareOwnerAwareIndexes`,
  `refreshIssuesNewPairFromRefreshToken`.
- Name the behavior and outcome; include the condition only when it matters.
  Avoid numbered tests and names such as `testCreate1` or `worksCorrectly`.

## Examples to Avoid

| Avoid | Prefer | Reason |
|-------|--------|--------|
| `CommonUtils` | a cohesive name such as `StringUtils` or `Checks` | `Common` has no boundary |
| `DataManager` | `CapabilityRegistry`, `SessionService`, or another exact role | neither the data nor responsibility is named |
| `RoleBusinessService` | `RoleService` | `Business` repeats the service role |
| `RoleDataDao` | `RoleDao` | `Data` repeats the DAO role |
| `UserDTO`, `UserVO` | `UserProfileVo` | project suffix and use case are explicit |
| `process(request)` | `registerUser(request)` or `resolveDependencies()` | observable behavior is named |
| `getAllUsers()` | `listUsers()` | collection shape uses the project query verb |
| `flag` | `enabled`, `required`, or another predicate | boolean meaning is explicit |
| `timeout` | `timeoutMillis` | numeric unit is explicit |
| `pluginID` | `pluginId` | acronym casing is consistent |
| `RoleImpl` | `DefaultRoleService` or a responsibility-specific name | implementation distinction is meaningful |

## Naming Checklist

Before introducing or renaming an identifier, verify:

- Does the name use the domain's established term for the concept?
- Is every qualifier necessary to distinguish scope, variant, operation, or
  representation?
- Does the suffix match the type's actual responsibility?
- Does a request or view name state its use case and mutation/read boundary?
- Does the method verb reveal result shape or observable behavior?
- Are boolean, collection, identifier, time, count, and unit names explicit?
- Are acronyms cased according to the project forms?
- Does the package identify the owning domain and responsibility without
  `impl`, `common`, or `misc`?
- Would a reader understand the name without opening the implementation?
- Is the name still accurate if the internal implementation changes?
