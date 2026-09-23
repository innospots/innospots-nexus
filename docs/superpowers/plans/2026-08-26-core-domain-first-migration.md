# Core Domain-First 包迁移实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 按 domain 与 responsibility 重组既有 `innospots-nexus-core` 类型，将 concrete entity 移入 owning module 子包，仅变更编译所需的 kernel reference。

**架构：** shared persistence parent 保留在 `core.domain.entity`，MyBatis support 移入 `core.persistence`，组织既有 session、resource、server、quartz、watcher、extension 类型，不引入 `api` 层或新 abstraction。保留 field、table、ID prefix、event name 与 behavior。

**技术栈：** Java 25、Maven、Jakarta Persistence、MyBatis-Plus、Quartz、Lombok、JUnit 5、AssertJ。

---

## 文件与包映射

生产文件迁移并更新 package declaration/import 如下：

| 当前 path/package | 目标 path/package |
|---|---|
| `core/domain/entity/BaseEntity.java` | 不变：`core.domain.entity` |
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

既有 `core.extension.contract` 与 `core.extension.declaration` package
保持不变。不为尚无类型的 module 创建空 `model`、`entity`、`api` 或其他 placeholder
package。

## Task 1：迁移 Shared Persistence Support

**文件：**

- Move: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/entity/ProjectBaseEntity.java` to `innospots-nexus-core/src/main/java/com/innospots/nexus/core/domain/entity/ProjectBaseEntity.java`
- Move: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/entity/AuditMetaObjectHandler.java` to `innospots-nexus-core/src/main/java/com/innospots/nexus/core/persistence/handler/AuditMetaObjectHandler.java`
- Move: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/entity/DbPrimaryGenerator.java` to `innospots-nexus-core/src/main/java/com/innospots/nexus/core/persistence/id/DbPrimaryGenerator.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/domain/entity/BaseEntity.java`
- Modify: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/entity/CoreEntityContractsTest.java`
- Modify: kernel production and test files importing `ProjectBaseEntity` or `DbPrimaryGenerator`

- [x] **Step 1：迁移文件并更新 package declaration/import。**

  `ProjectBaseEntity` becomes `com.innospots.nexus.core.domain.entity` and
  imports `BaseEntity` from its sibling package. `AuditMetaObjectHandler`
  imports both shared entity parents explicitly from `core.domain.entity`.
  `DbPrimaryGenerator` imports `BaseEntity` from `core.domain.entity`.
  `BaseEntity` points its Javadoc links to the sibling `ProjectBaseEntity` and
  the new `core.persistence.handler.AuditMetaObjectHandler` package.

- [x] **Step 2：更新全部 kernel 引用，不改变 kernel 逻辑。**

  仅替换以下 import 目标：

  ```text
  com.innospots.nexus.core.entity.ProjectBaseEntity
      -> com.innospots.nexus.core.domain.entity.ProjectBaseEntity
  com.innospots.nexus.core.entity.DbPrimaryGenerator
      -> com.innospots.nexus.core.persistence.id.DbPrimaryGenerator
  ```

- [x] **Step 3：更新 core entity 契约测试的 package/import。**

  保持断言与测试行为不变；将 import 指向新
  persistence support 包，仅在 package-local reference 需要时保留测试于 shared entity 契约
  区域。

- [x] **Step 4：Java 变更后立即编译。**

  运行：

  ```bash
  mvn clean compile
  ```

  预期： `BUILD SUCCESS`; shared persistence support and kernel references
  从新 package 编译。concrete core entity 待在 Task 2、3 迁移
  。

## Task 2：Move Session and Resource Types

**文件：**

- 迁移 map 中列出的四组 session persistence/domain contract
- Move `MetaResourceEntity.java` to `core/resource/domain/entity`
- Modify: `CoreEntityContractsTest.java`, `SessionContractsTest.java`, and any Javadoc/imports that refer to moved session/resource types

- [x] **Step 1：迁移 session entity、model、enum、event、repository 与 service。**

  使用以下精确 package declaration：

  ```text
  com.innospots.nexus.core.session.domain.entity
  com.innospots.nexus.core.session.domain.model
  com.innospots.nexus.core.session.domain.enums
  com.innospots.nexus.core.session.domain.event
  com.innospots.nexus.core.session.repository
  com.innospots.nexus.core.session.service
  ```

  在 package 间显式添加 import。repository interface
  保持相同 interface 与方法签名。`SessionService` 保留
  既有 constructor、save 操作与 event type string。

- [x] **Step 2：迁移 resource entity。**

  设置 package 为 `com.innospots.nexus.core.resource.domain.entity` 并
  从 `core.domain.entity` import `ProjectBaseEntity`。保留
  `nexus_meta_resource`, `resourceId`, and the `res` ID prefix.

- [x] **Step 3：更新 session/resource 测试与 core entity 引用。**

  仅更新 import 与 package declaration。保留既有断言：
  immutable collection、event publication、message classification、table
  name 与 ID generation。

- [x] **Step 4：Java 变更后立即编译。**

  运行：

  ```bash
  mvn clean compile
  ```

  预期： `BUILD SUCCESS`; the old flat `core.session` package is absent and
  所有 moved type 从新 package 解析。

## Task 3：Move Server Types

**文件：**

- Move: `ServiceRegistryEntity.java` to `core/server/domain/entity`
- Move: `ServiceInfo.java`, `ServiceLifecycle.java` to `core/server/domain/model`
- Move: `ServiceRole.java`, `ServiceStatus.java` to `core/server/domain/enums`
- Move: `ServiceRegistry.java` to `core/server/registry`
- Move: `ServiceNodeHolder.java` to `core/server/runtime`
- Modify: `ServerContractsTest.java`, `ServiceNodeHolderTest.java`, and `CoreEntityContractsTest.java`

- [x] **Step 1：迁移 server 类型并更新 package declaration。**

  仅添加新 subpackage 所需 import。保留 `ServiceInfo`
  fluent setter、immutable map accessor、server-key 计算与 elapsed
  heartbeat 行为不变。保留 `ServiceNodeHolder` lifecycle 与 shard
  计算不变。

- [x] **Step 2：更新 server 测试与 entity 契约引用。**

  更新 package declaration/import，保留既有 behavior
  断言与 service registry table/ID 契约。

- [x] **Step 3：Java 变更后立即编译。**

  运行：

  ```bash
  mvn clean compile
  ```

  预期： `BUILD SUCCESS` with server models, enums, entity, registry, and
  runtime coordination 从目标 package 解析。

## Task 4：Move Quartz and Watcher Types

**文件：**

- Move `CronConverter.java` to `core/quartz/converter`
- Move `QuartzJobInfo.java`, `QuartzTriggerInfo.java` to `core/quartz/domain/model`
- Move `QuartzJobRequest.java` to `core/quartz/domain/request`
- Move `ScheduleMode.java` to `core/quartz/domain/enums`
- Move `QuartzScheduleManager.java` to `core/quartz/service`
- Move `IWatcher.java` to `core/watcher/contract`
- Move `AbstractWatcher.java`, `WatcherSupervisor.java` to `core/watcher/runtime`
- Modify corresponding Quartz and watcher tests

- [x] **Step 1：迁移 Quartz 类型并更新 import。**

  保留 request factory、schedule enum value、cron conversion
  behavior、scheduler group constant 与 manager lifecycle。更新 import
  ：`ScheduleMode`、`QuartzJobRequest`、`QuartzJobInfo`、
  `QuartzTriggerInfo`，不创建 forwarding type。

- [x] **Step 2：迁移 watcher 类型并更新 import。**

  保留 `IWatcher` 为既有 interface。更新 `AbstractWatcher` 与
  `WatcherSupervisor` import 到新 contract/runtime package，不
  改变 watcher callback、scheduling 或 shutdown behavior。

- [x] **Step 3：Java 变更后立即编译。**

  运行：

  ```bash
  mvn clean compile
  ```

  预期： `BUILD SUCCESS` with no old flat Quartz/watcher type references.

## Task 5：Remove Stale References and Verify Structure

**文件：**

- 仅修改 search 发现的剩余 core/kernel Java 引用
- 不修改无关 base、console 或 kernel 业务实现

- [x] **Step 1：搜索旧 package 引用。**

  运行：

  ```bash
  rg -n "com\\.innospots\\.nexus\\.core\\.(entity|session|server|quartz|watcher)" --glob '*.java' --glob '*.md' .
  ```

  预期： no old Java package imports or declarations; documentation may be
  命名 moved core class 时可更新，
  作为 reference synchronization 一部分。

- [x] **Step 2：搜索错位的 entity class。**

  运行：

  ```bash
  find innospots-nexus-core/src/main/java/com/innospots/nexus/core -type f -name '*Entity.java' | sort
  ```

  预期： only `core.domain.entity` shared parents plus entities under
  `session/domain/entity`、`resource/domain/entity`、`server/domain/entity` 下的 entity。

- [x] **Step 3：运行完整验证。**

  运行：

  ```bash
  mvn validate
  mvn test
  mvn -q help:effective-pom
  ```

  预期： all commands succeed on the configured Java 25 environment.

- [x] **Step 4：检查最终 diff。**

  运行：

  ```bash
  git diff --check
  git status --short
  ```

  确认 diff 仅含 planned core 迁移、必要 kernel
  reference 变更、测试与设计/计划文档；保留全部
  无关既有 working-tree 变更。

## 验证记录

- `mvn clean compile`: 每组 Java 迁移后通过; 第一批
  shared parent 迁移后需要三个显式 `ProjectBaseEntity` import，
  moved, then 通过。
- `mvn test`: 通过 — base 175, core 40, console 1, kernel 74 tests.
- `mvn validate`: 通过。
- `mvn -q help:effective-pom`: 通过。
- 旧 Java package 引用搜索： 无匹配。
