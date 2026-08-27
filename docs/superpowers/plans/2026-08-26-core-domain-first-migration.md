# Core Domain-First Package Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize existing `innospots-nexus-core` types by domain and responsibility, moving concrete entities into their owning module subpackages while changing only the kernel references required for compilation.

**Architecture:** Keep shared persistence parents under `core.domain.entity`, move MyBatis support into `core.persistence`, and organize existing session, resource, server, quartz, watcher, and extension types without introducing an `api` layer or new abstractions. Preserve fields, tables, ID prefixes, event names, and behavior.

**Tech Stack:** Java 25, Maven, Jakarta Persistence, MyBatis-Plus, Quartz, Lombok, JUnit 5, AssertJ.

---

## Files and Package Map

Production files are moved and package declarations/imports are updated as follows:

| Current path/package | Target path/package |
|---|---|
| `core/domain/entity/BaseEntity.java` | unchanged: `core.domain.entity` |
| `core/entity/ProjectBaseEntity.java` | `core/domain/entity/ProjectBaseEntity.java` |
| `core/entity/AuditMetaObjectHandler.java` | `core/persistence/handler/AuditMetaObjectHandler.java` |
| `core/entity/DbPrimaryGenerator.java` | `core/persistence/id/DbPrimaryGenerator.java` |
| `core/entity/ConversationEntity.java` | `core/session/domain/entity/ConversationEntity.java` |
| `core/entity/SessionMessageEntity.java` | `core/session/domain/entity/SessionMessageEntity.java` |
| `core/entity/MetaResourceEntity.java` | `core/resource/domain/entity/MetaResourceEntity.java` |
| `core/entity/ServiceRegistryEntity.java` | `core/server/domain/entity/ServiceRegistryEntity.java` |
| `core/session/Conversation.java` | `core/session/domain/model/Conversation.java` |
| `core/session/SessionMessage.java` | `core/session/domain/model/SessionMessage.java` |
| `core/session/SessionMessageType.java` | `core/session/domain/enums/SessionMessageType.java` |
| `core/session/*CreatedEvent.java` | `core/session/domain/event/` |
| `core/session/*Repository.java` | `core/session/repository/` |
| `core/session/SessionService.java` | `core/session/service/SessionService.java` |
| `core/server/ServiceInfo.java` | `core/server/domain/model/ServiceInfo.java` |
| `core/server/ServiceLifecycle.java` | `core/server/domain/model/ServiceLifecycle.java` |
| `core/server/ServiceRole.java`, `ServiceStatus.java` | `core/server/domain/enums/` |
| `core/server/ServiceRegistry.java` | `core/server/registry/ServiceRegistry.java` |
| `core/server/ServiceNodeHolder.java` | `core/server/runtime/ServiceNodeHolder.java` |
| `core/quartz/CronConverter.java` | `core/quartz/converter/CronConverter.java` |
| `core/quartz/QuartzJobInfo.java`, `QuartzTriggerInfo.java` | `core/quartz/domain/model/` |
| `core/quartz/QuartzJobRequest.java` | `core/quartz/domain/request/QuartzJobRequest.java` |
| `core/quartz/ScheduleMode.java` | `core/quartz/domain/enums/ScheduleMode.java` |
| `core/quartz/QuartzScheduleManager.java` | `core/quartz/service/QuartzScheduleManager.java` |
| `core/watcher/IWatcher.java` | `core/watcher/contract/IWatcher.java` |
| `core/watcher/AbstractWatcher.java`, `WatcherSupervisor.java` | `core/watcher/runtime/` |

Existing `core.extension.contract` and `core.extension.declaration` packages
remain unchanged. No empty `model`, `entity`, `api`, or other placeholder
packages are created for modules with no existing types.

## Task 1: Move Shared Persistence Support

**Files:**

- Move: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/entity/ProjectBaseEntity.java` to `innospots-nexus-core/src/main/java/com/innospots/nexus/core/domain/entity/ProjectBaseEntity.java`
- Move: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/entity/AuditMetaObjectHandler.java` to `innospots-nexus-core/src/main/java/com/innospots/nexus/core/persistence/handler/AuditMetaObjectHandler.java`
- Move: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/entity/DbPrimaryGenerator.java` to `innospots-nexus-core/src/main/java/com/innospots/nexus/core/persistence/id/DbPrimaryGenerator.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/domain/entity/BaseEntity.java`
- Modify: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/entity/CoreEntityContractsTest.java`
- Modify: kernel production and test files importing `ProjectBaseEntity` or `DbPrimaryGenerator`

- [x] **Step 1: Move files and update package declarations/imports.**

  `ProjectBaseEntity` becomes `com.innospots.nexus.core.domain.entity` and
  imports `BaseEntity` from its sibling package. `AuditMetaObjectHandler`
  imports both shared entity parents explicitly from `core.domain.entity`.
  `DbPrimaryGenerator` imports `BaseEntity` from `core.domain.entity`.
  `BaseEntity` points its Javadoc links to the sibling `ProjectBaseEntity` and
  the new `core.persistence.handler.AuditMetaObjectHandler` package.

- [x] **Step 2: Update all kernel references without changing kernel logic.**

  Replace only these import targets:

  ```text
  com.innospots.nexus.core.entity.ProjectBaseEntity
      -> com.innospots.nexus.core.domain.entity.ProjectBaseEntity
  com.innospots.nexus.core.entity.DbPrimaryGenerator
      -> com.innospots.nexus.core.persistence.id.DbPrimaryGenerator
  ```

- [x] **Step 3: Update the core entity contract test package/imports.**

  Keep the assertions and test behavior unchanged; point imports to the new
  persistence support package and keep the test in the shared entity contract
  area only if its package-local references require it.

- [x] **Step 4: Compile immediately after the Java changes.**

  Run:

  ```bash
  mvn clean compile
  ```

  Expected: `BUILD SUCCESS`; shared persistence support and kernel references
  compile from their new packages. Concrete core entities remain to be moved
  in Tasks 2 and 3.

## Task 2: Move Session and Resource Types

**Files:**

- Move the four session persistence/domain contract groups listed in the map
- Move `MetaResourceEntity.java` to `core/resource/domain/entity`
- Modify: `CoreEntityContractsTest.java`, `SessionContractsTest.java`, and any Javadoc/imports that refer to moved session/resource types

- [x] **Step 1: Move session entities, models, enum, events, repositories, and service.**

  Use these exact package declarations:

  ```text
  com.innospots.nexus.core.session.domain.entity
  com.innospots.nexus.core.session.domain.model
  com.innospots.nexus.core.session.domain.enums
  com.innospots.nexus.core.session.domain.event
  com.innospots.nexus.core.session.repository
  com.innospots.nexus.core.session.service
  ```

  Add imports between those packages explicitly. The repository interfaces
  remain the same interfaces and method signatures. `SessionService` keeps its
  existing constructor, save operations, and event type strings.

- [x] **Step 2: Move the resource entity.**

  Set its package to `com.innospots.nexus.core.resource.domain.entity` and
  import `ProjectBaseEntity` from `core.domain.entity`. Preserve
  `nexus_meta_resource`, `resourceId`, and the `res` ID prefix.

- [x] **Step 3: Update session/resource tests and core entity references.**

  Update imports and package declarations only. Keep the existing assertions for
  immutable collections, event publication, message classification, table
  names, and ID generation.

- [x] **Step 4: Compile immediately after the Java changes.**

  Run:

  ```bash
  mvn clean compile
  ```

  Expected: `BUILD SUCCESS`; the old flat `core.session` package is absent and
  all moved types are resolved from their new packages.

## Task 3: Move Server Types

**Files:**

- Move: `ServiceRegistryEntity.java` to `core/server/domain/entity`
- Move: `ServiceInfo.java`, `ServiceLifecycle.java` to `core/server/domain/model`
- Move: `ServiceRole.java`, `ServiceStatus.java` to `core/server/domain/enums`
- Move: `ServiceRegistry.java` to `core/server/registry`
- Move: `ServiceNodeHolder.java` to `core/server/runtime`
- Modify: `ServerContractsTest.java`, `ServiceNodeHolderTest.java`, and `CoreEntityContractsTest.java`

- [x] **Step 1: Move server types and update package declarations.**

  Add only the imports needed for the new subpackages. Keep `ServiceInfo`
  fluent setters, immutable map accessors, server-key calculation, and elapsed
  heartbeat behavior unchanged. Keep `ServiceNodeHolder` lifecycle and shard
  calculations unchanged.

- [x] **Step 2: Update server tests and entity contract references.**

  Update package declarations/imports while preserving existing behavior
  assertions and the service registry table/ID contract.

- [x] **Step 3: Compile immediately after the Java changes.**

  Run:

  ```bash
  mvn clean compile
  ```

  Expected: `BUILD SUCCESS` with server models, enums, entity, registry, and
  runtime coordination resolved from their target packages.

## Task 4: Move Quartz and Watcher Types

**Files:**

- Move `CronConverter.java` to `core/quartz/converter`
- Move `QuartzJobInfo.java`, `QuartzTriggerInfo.java` to `core/quartz/domain/model`
- Move `QuartzJobRequest.java` to `core/quartz/domain/request`
- Move `ScheduleMode.java` to `core/quartz/domain/enums`
- Move `QuartzScheduleManager.java` to `core/quartz/service`
- Move `IWatcher.java` to `core/watcher/contract`
- Move `AbstractWatcher.java`, `WatcherSupervisor.java` to `core/watcher/runtime`
- Modify corresponding Quartz and watcher tests

- [x] **Step 1: Move Quartz types and update imports.**

  Preserve the request factories, schedule enum values, cron conversion
  behavior, scheduler group constants, and manager lifecycle. Update imports
  for `ScheduleMode`, `QuartzJobRequest`, `QuartzJobInfo`, and
  `QuartzTriggerInfo` rather than creating forwarding types.

- [x] **Step 2: Move watcher types and update imports.**

  Keep `IWatcher` as the existing interface. Update `AbstractWatcher` and
  `WatcherSupervisor` imports to the new contract/runtime packages without
  changing watcher callbacks, scheduling, or shutdown behavior.

- [x] **Step 3: Compile immediately after the Java changes.**

  Run:

  ```bash
  mvn clean compile
  ```

  Expected: `BUILD SUCCESS` with no old flat Quartz/watcher type references.

## Task 5: Remove Stale References and Verify Structure

**Files:**

- Modify only remaining core/kernel Java references discovered by search
- Do not modify unrelated base, console, or kernel business implementations

- [x] **Step 1: Search for old package references.**

  Run:

  ```bash
  rg -n "com\\.innospots\\.nexus\\.core\\.(entity|session|server|quartz|watcher)" --glob '*.java' --glob '*.md' .
  ```

  Expected: no old Java package imports or declarations; documentation may be
  updated only when it names a moved core class and is part of the requested
  reference synchronization.

- [x] **Step 2: Search for misplaced entity classes.**

  Run:

  ```bash
  find innospots-nexus-core/src/main/java/com/innospots/nexus/core -type f -name '*Entity.java' | sort
  ```

  Expected: only `core.domain.entity` shared parents plus entities under
  `session/domain/entity`, `resource/domain/entity`, and `server/domain/entity`.

- [x] **Step 3: Run full verification.**

  Run:

  ```bash
  mvn validate
  mvn test
  mvn -q help:effective-pom
  ```

  Expected: all commands succeed on the configured Java 25 environment.

- [x] **Step 4: Inspect the final diff.**

  Run:

  ```bash
  git diff --check
  git status --short
  ```

  Confirm the diff contains only the planned core moves, necessary kernel
  reference changes, tests, and the design/plan documents; preserve all
  unrelated pre-existing working-tree changes.

## Verification Record

- `mvn clean compile`: passed after each Java migration batch; the first batch
  required three explicit `ProjectBaseEntity` imports after the shared parent
  moved, then passed.
- `mvn test`: passed — base 175, core 40, console 1, kernel 74 tests.
- `mvn validate`: passed.
- `mvn -q help:effective-pom`: passed.
- Old Java package reference search: no matches.
