# Core Domain-First 包迁移设计

**日期：** 2026-08-26

**范围：** `innospots-nexus-core`，以及 core 包迁移所需的
`innospots-nexus-portal` import/package-reference 更新。

## 目标

按 domain boundary 重构 core module，使 persistence entity 位于
拥有该 domain 的子包，shared persistence support 与 concrete entity 分离，
flat package 不再混放 model、event 与 service。这是 package-organization refactor：
保留既有 interface 与 behavior，不引入 standalone `api` 层，
不添加复杂逻辑或新 abstraction。

## 约束

- 仅 redesign core production 与 core test 结构。
- Portal 变更限于 import、package declaration 与
  编译新 core package 所需的 test reference。
- 不 redesign portal business behavior、persistence shape 或 domain package。
- 不在旧 core package 下保留 compatibility class。
- working tree 中既有 user 变更保持不变，除非直接引用 moved core type。
- 不新增或删除 core Maven module、dependency direction、table name 或 event type。
- 本代码 refactor 不更新 module skill 文档。

## 目标包结构

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

既有 extension contract/declaration 拆分保留，因其已分离 SPI boundary 与 extension metadata。
既有 watcher interface 仍为 contract type，其 executor/lifecycle implementation 仍在
`runtime`。Quartz record 与 conversion、scheduler orchestration 分离，
不引入 adapter 或 API module。

## 职责与依赖规则

### 共享 persistence foundation

`domain.entity.BaseEntity` 与 `ProjectBaseEntity` 是唯一 shared entity
parent。它们包含 audit/project persistence field，无 business-domain
state。`persistence.handler.AuditMetaObjectHandler` 拥有 MyBatis-Plus fill
behavior，`persistence.id.DbPrimaryGenerator` 拥有 identifier
generation。这从 generic `entity`
namespace 移出 technical infrastructure，而不新增 abstraction。

### Session domain

Session domain 拥有 conversation/message persistence entity、in-memory
domain model、message classification、creation event、既有 repository
interface 与 write service。Repository interface shape 不变，
service 仅在 repository save 成功后发布既有 event type。

### Resource domain

Resource domain 拥有 `MetaResourceEntity`，因其表示 project
resource metadata 而非 session 或 platform registry state。table
name、primary-key field、ID prefix 不变。

### Server domain

Server domain 拥有 service registry persistence、service metadata model、
status/role enum、既有 registry interface 与 local-node runtime
coordination。
`ServiceNodeHolder` 是 runtime coordination 而非 domain entity，故不与
`ServiceRegistryEntity` 共享 `domain.entity` package。

### Quartz 与 watcher infrastructure

Quartz request/model/enumeration type 构成 scheduler domain surface；
`CronConverter` 是 converter，`QuartzScheduleManager` 是 scheduler
service。既有 watcher interface 仍在 contract package，
supervisor/abstract watcher implementation 仍为 runtime type。不引入 Spring 或
application auto-configuration。

## 逻辑与兼容性规则

迁移不添加 business logic、interface abstraction、新
entity、field、index、table name 或 event type。既有 logic 原样迁移，
仅 import/package/Javadoc 更新及编译所需的 small mechanical cleanup。
既有 defensive copy、identifier generation、audit filling、scheduler lifecycle、watcher lifecycle、
repository behavior、event publication 不变。

## 迁移映射

| 当前类型 | 目标包 | 原因 |
|---|---|---|
| `core.domain.entity.BaseEntity` | 不变 | shared persistence parent |
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
| `core.session.*CreatedEvent` | `core.session.domain.event` | session event contract |
| `core.session.*Repository` | `core.session.repository` | 既有 repository interface |
| `core.session.SessionService` | `core.session.service` | session workflow |
| `core.server.ServiceInfo` | `core.server.domain.model` | server metadata model |
| `core.server.ServiceLifecycle` | `core.server.domain.model` | server state model |
| `core.server.ServiceRole/Status` | `core.server.domain.enums` | server enum |
| `core.server.ServiceRegistry` | `core.server.registry` | 既有 registry interface |
| `core.server.ServiceNodeHolder` | `core.server.runtime` | local runtime coordination |
| `core.quartz.CronConverter` | `core.quartz.converter` | cron conversion |
| `core.quartz.QuartzJobRequest` | `core.quartz.domain.request` | scheduler request |
| `core.quartz.QuartzJobInfo/TriggerInfo` | `core.quartz.domain.model` | scheduler output model |
| `core.quartz.ScheduleMode` | `core.quartz.domain.enums` | scheduler enum |
| `core.quartz.QuartzScheduleManager` | `core.quartz.service` | scheduler orchestration |
| `core.watcher.IWatcher` | `core.watcher.contract` | 既有 watcher contract |
| `core.watcher.AbstractWatcher/Supervisor` | `core.watcher.runtime` | watcher runtime |

## 测试策略

1. core test 随 production package boundary 迁移并更新 import。
2. 保留 entity contract test：inheritance、JPA/MyBatis annotation、table
   name、ID length/type/prefix、audit fill behavior。
3. 保留 session、server、quartz、watcher、extension contract test
   的行为导向；仅更新 package/import reference。
4. 仅更新 portal import 与 test reference（`ProjectBaseEntity`、
   `DbPrimaryGenerator`）。确认无旧 core package reference。
5. 每组 Java 源码编辑后立即 `mvn clean compile`，
   package migration 全部完成后运行 `mvn validate`、`mvn test`、`mvn -q help:effective-pom`。

## 完成标准

- 无 concrete persistence entity 留在 `com.innospots.nexus.core.entity`。
- 迁移后无 `core.entity` package。
- migrated module 不引入 standalone `api` package。
- 不引入新 interface 或复杂 business logic。
- 每个 moved type 的 package declaration、import、Javadoc link、test 一致。
- Portal 使用新 core package name 编译，无无关 behavior
  变更。
- 既有 table、ID prefix、event type string、public contract 保持稳定。
- 在 configured JDK 上 core 与全仓库 verification command 通过；
  若环境低于 Java 25，报告 mismatch 而非降低项目基线。
