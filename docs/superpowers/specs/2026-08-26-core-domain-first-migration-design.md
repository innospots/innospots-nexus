# Core Domain-First Package Migration Design

**Date:** 2026-08-26

**Scope:** `innospots-nexus-core`, plus import/package-reference updates in
`innospots-nexus-kernel` that are required by the core package migration.

## Goal

Restructure the core module around domain boundaries so persistence entities
live under the subpackage that owns their domain, shared persistence support is
separated from concrete entities, and flat packages no longer mix models,
events, and services. This is a package-organization refactor: keep existing
interfaces and behavior, do not introduce a standalone `api` layer, and do not
add complex logic or new abstractions.

## Constraints

- Only core production and core test structure is redesigned.
- Kernel changes are limited to imports, package declarations, and test
  references required to compile against the new core packages.
- No kernel business behavior, persistence shape, or domain package is
  redesigned.
- No compatibility classes are retained under the old core packages.
- Existing user changes in the working tree remain untouched unless they are
  direct references to a moved core type.
- No core Maven module, dependency direction, table name, or event type is
  added or removed.
- Module skill documentation is not updated as part of this code refactor.

## Target Package Structure

```text
com.innospots.nexus.core
├── domain
│   └── entity
│       ├── BaseEntity
│       └── ProjectBaseEntity
├── persistence
│   ├── handler
│   │   └── AuditMetaObjectHandler
│   └── id
│       └── DbPrimaryGenerator
├── extension
│   ├── contract
│   └── declaration
├── quartz
│   ├── converter
│   │   └── CronConverter
│   ├── domain
│   │   ├── enums
│   │   │   └── ScheduleMode
│   │   ├── model
│   │   │   ├── QuartzJobInfo
│   │   │   └── QuartzTriggerInfo
│   │   └── request
│   │       └── QuartzJobRequest
│   └── service
│       └── QuartzScheduleManager
├── resource
│   └── domain
│       └── entity
│           └── MetaResourceEntity
├── server
│   ├── registry
│   │   └── ServiceRegistry
│   ├── domain
│   │   ├── entity
│   │   │   └── ServiceRegistryEntity
│   │   ├── enums
│   │   │   ├── ServiceRole
│   │   │   └── ServiceStatus
│   │   └── model
│   │       ├── ServiceInfo
│   │       └── ServiceLifecycle
│   └── runtime
│       └── ServiceNodeHolder
├── session
│   ├── repository
│   │   ├── ConversationRepository
│   │   └── SessionMessageRepository
│   ├── domain
│   │   ├── entity
│   │   │   ├── ConversationEntity
│   │   │   └── SessionMessageEntity
│   │   ├── enums
│   │   │   └── SessionMessageType
│   │   ├── event
│   │   │   ├── ConversationCreatedEvent
│   │   │   └── SessionMessageCreatedEvent
│   │   └── model
│   │       ├── Conversation
│   │       └── SessionMessage
│   └── service
│       └── SessionService
└── watcher
    ├── contract
    │   └── IWatcher
    └── runtime
        ├── AbstractWatcher
        └── WatcherSupervisor
```

The existing extension contract/declaration split remains because it already
separates the SPI boundary from extension metadata. The existing watcher
interface stays a contract type and its executor/lifecycle implementation stays
in `runtime`. Quartz records are separated from conversion and scheduler
orchestration without introducing an adapter or API module.

## Responsibility and Dependency Rules

### Shared persistence foundation

`domain.entity.BaseEntity` and `ProjectBaseEntity` are the only shared entity
parents. They contain audit/project persistence fields and no business-domain
state. `persistence.handler.AuditMetaObjectHandler` owns MyBatis-Plus fill
behavior, while `persistence.id.DbPrimaryGenerator` owns identifier
generation. This removes technical infrastructure from the generic `entity`
namespace without adding a new abstraction.

### Session domain

The session domain owns conversation/message persistence entities, in-memory
domain models, message classification, creation events, existing repository
interfaces, and the write service. Repository interfaces remain unchanged in
shape and the service publishes the existing event types only after a
successful repository save.

### Resource domain

The resource domain owns `MetaResourceEntity` because it represents project
resource metadata rather than session or platform registry state. Its table
name, primary-key field, and ID prefix remain unchanged.

### Server domain

The server domain owns service registry persistence, service metadata models,
status/role enums, the existing registry interface, and local-node runtime
coordination.
`ServiceNodeHolder` is runtime coordination rather than a domain entity, so it
does not share the `domain.entity` package with `ServiceRegistryEntity`.

### Quartz and watcher infrastructure

Quartz request/model/enumeration types form the scheduler domain surface;
`CronConverter` is a converter and `QuartzScheduleManager` is the scheduler
service. The existing watcher interface remains under a contract package while
supervisor/abstract watcher implementations remain runtime types. No Spring or
application auto-configuration is introduced.

## Logic and Compatibility Rules

The migration does not add business logic, interface abstractions, new
entities, fields, indexes, table names, or event types. Existing logic is moved
as-is, with only import/package/Javadoc updates and small mechanical cleanups
that are necessary to compile after the move. Existing defensive copies,
identifier generation, audit filling, scheduler lifecycle, watcher lifecycle,
repository behavior, and event publication remain unchanged.

## Migration Map

| Current type | Target package | Reason |
|---|---|---|
| `core.domain.entity.BaseEntity` | unchanged | shared persistence parent |
| `core.entity.ProjectBaseEntity` | `core.domain.entity` | shared persistence parent |
| `core.entity.AuditMetaObjectHandler` | `core.persistence.handler` | MyBatis infrastructure |
| `core.entity.DbPrimaryGenerator` | `core.persistence.id` | ID infrastructure |
| `core.entity.ConversationEntity` | `core.session.domain.entity` | session-owned table |
| `core.entity.SessionMessageEntity` | `core.session.domain.entity` | session-owned table |
| `core.entity.MetaResourceEntity` | `core.resource.domain.entity` | resource-owned table |
| `core.entity.ServiceRegistryEntity` | `core.server.domain.entity` | server-owned table |
| `core.session.Conversation` | `core.session.domain.model` | session model |
| `core.session.SessionMessage` | `core.session.domain.model` | session model |
| `core.session.SessionMessageType` | `core.session.domain.enums` | session enum |
| `core.session.*CreatedEvent` | `core.session.domain.event` | session event contracts |
| `core.session.*Repository` | `core.session.repository` | existing repository interfaces |
| `core.session.SessionService` | `core.session.service` | session workflow |
| `core.server.ServiceInfo` | `core.server.domain.model` | server metadata model |
| `core.server.ServiceLifecycle` | `core.server.domain.model` | server state model |
| `core.server.ServiceRole/Status` | `core.server.domain.enums` | server enums |
| `core.server.ServiceRegistry` | `core.server.registry` | existing registry interface |
| `core.server.ServiceNodeHolder` | `core.server.runtime` | local runtime coordination |
| `core.quartz.CronConverter` | `core.quartz.converter` | cron conversion |
| `core.quartz.QuartzJobRequest` | `core.quartz.domain.request` | scheduler request |
| `core.quartz.QuartzJobInfo/TriggerInfo` | `core.quartz.domain.model` | scheduler output models |
| `core.quartz.ScheduleMode` | `core.quartz.domain.enums` | scheduler enum |
| `core.quartz.QuartzScheduleManager` | `core.quartz.service` | scheduler orchestration |
| `core.watcher.IWatcher` | `core.watcher.contract` | existing watcher contract |
| `core.watcher.AbstractWatcher/Supervisor` | `core.watcher.runtime` | watcher runtime |

## Test Strategy

1. Move core tests with their production package boundaries and update imports.
2. Keep entity contract tests for inheritance, JPA/MyBatis annotations, table
   names, ID lengths/types/prefixes, and audit fill behavior.
3. Keep session, server, quartz, watcher, and extension contract tests
   behavior-oriented; update package/import references only.
4. Update kernel imports and test references for `ProjectBaseEntity` and
   `DbPrimaryGenerator` only. Verify no old core package references remain.
5. Run `mvn clean compile` immediately after each Java source edit group, then
   run `mvn validate`, `mvn test`, and `mvn -q help:effective-pom` when all
   package migration changes are complete.

## Completion Criteria

- No concrete persistence entity remains under `com.innospots.nexus.core.entity`.
- No `core.entity` package remains after the migration.
- No standalone `api` package is introduced for the migrated modules.
- No new interface or complex business logic is introduced.
- Every moved type's package declaration, imports, Javadoc links, and tests are
  consistent.
- Kernel compiles using the new core package names without unrelated behavior
  changes.
- Existing tables, ID prefixes, event type strings, and public contracts remain
  stable.
- Core and full-repository verification commands pass on the configured JDK;
  if the environment is older than Java 25, report that mismatch rather than
  lowering the project baseline.
