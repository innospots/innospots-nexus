# innospots-nexus-core — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：0.1.0-SNAPSHOT

## 模块概览

基于 `innospots-nexus-base` 的业务中立平台基础设施：共享持久化基类、审计填充、归属列（`OwnershipEntity`）、Quartz 调度、服务节点注册、Watcher 运行时、启动 SPI、OpenAPI 安全注解，以及工作区级文件元数据。

**能力一览：**

| 能力 | 说明 |
|------|------|
| **持久化基类** | `BaseEntity` 审计字段；`OwnershipEntity` 归属列（console 资源默认） |
| **归属作用域** | `OwnershipScope`、`PersistenceOwnership`、`OwnerType` 查询隔离与写入 |
| **审计与 ID** | `AuditMetaObjectHandler`（TLC 驱动填充）、`DbPrimaryGenerator`（ULID） |
| **启动 SPI** | 通过 `NexusStartup` 编排有序的 `NexusStartupTask` |
| **Quartz** | 内存型 `QuartzScheduleManager`，支持 cron/单次/定时模式 |
| **服务节点** | `ServiceRegistry`、`ServiceNodeHolder`、`ServiceRegistryEntity` |
| **Watchers** | `IWatcher` / `AbstractWatcher` / `WatcherSupervisor` 后台循环 |
| **OpenAPI** | `@NexusAuthenticatedApi` Bearer 安全要求标记 |
| **文件元数据** | `nx_meta_resource`、`MetaResourceService`、`ResourceStorageRegistry` |

## 扩展指南

| 需求 | 使用 core 中的 | 不要放在 core 中 |
|------|---------------|-------------------|
| 新业务实体（portal 租户域） | portal 的 `TenantBaseEntity` 链（见 portal 模块） | 业务工作流、REST 端点 |
| Console 归属资源 | 继承 `OwnershipEntity` + `OwnershipScope` | 用户/角色业务规则 |
| 宿主启动后的钩子 | 实现 `NexusStartupTask`，在宿主 `NexusStartup.builder()` 中注册 | 插件/目录/会话逻辑 |
| 后台轮询/同步 | 继承 `AbstractWatcher`，在 `WatcherSupervisor` 上注册 | Watcher 中的领域特定业务规则 |
| 集群服务注册 | `ServiceRegistry` + `ServiceNodeHolder` | 租户/工作空间业务数据 |
| 定时任务 | `QuartzScheduleManager` + `QuartzJobRequest` | 任务业务逻辑（保留在上层模块的 Job 类中） |
| 二进制文件 + 元数据 | 在 `ResourceStorageRegistry` 上注册 `ResourceStore`；使用 `MetaResourceService` | 控制台/内核特定的文件策略 |

作用域快照和 `SessionContext` 位于 **base**（`java-reference` → `innospots-nexus-base`）。


## 类参考

### 包 `bootstrap`

| 类 | 类型 | 说明 |
|------|------|------|
| `NexusStartup` | `class` | 启动后初始化编排入口；宿主在配置期组装任务，运行期仅调用 {@link #run()} |
| `NexusStartupContext` | `class` | 启动任务执行过程中跨步骤传递的轻量上下文 |
| `NexusStartupTask` | `interface` | 框架无关的启动后初始化步骤 |

### 包 `openapi`

| 类 | 类型 | 说明 |
|------|------|------|
| `NexusAuthenticatedApi` | `@interface` | 标记需要 Bearer 认证的控制台 API 资源 |
| `NexusOpenApiSecurityNames` | `class` | MicroProfile OpenAPI 安全方案名称常量 |

### 包 `persistence.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `BaseEntity` | `class` | 带自动审计字段的 JPA/MyBatis-Plus 基类实体 |
| `OwnershipEntity` | `class` | 控制台业务归属列：通过 ownerType / ownerId / securityRealm 表达可见性与存储分区；查询与写入见 com.innospots.nexus.core.persistence.scope.OwnershipScope |

### 包 `persistence.handler`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuditMetaObjectHandler` | `class` | MyBatis-Plus 元对象处理器，为继承 BaseEntity 或 portal 租户域基类的实体自动填充审计字段 |

### 包 `persistence.id`

| 类 | 类型 | 说明 |
|------|------|------|
| `DbPrimaryGenerator` | `class` | Nexus 持久化实体的 MyBatis-Plus 主键生成器 |

### 包 `persistence.scope`

| 类 | 类型 | 说明 |
|------|------|------|
| `OwnerType` | `enum` | 持久化行归属层级（与控制台 RoleOwnerType 名称一致） |
| `OwnershipScope` | `class` | 基于 OwnershipEntity 的查询隔离与归属写入 |
| `PersistenceOwnership` | `record` | com.innospots.nexus.core.persistence.entity.OwnershipEntity 归属三元组 |

### 包 `quartz.converter`

| 类 | 类型 | 说明 |
|------|------|------|
| `CronConverter` | `class` | 将常见调度时间参数转换为 Quartz Cron 表达式 |

### 包 `quartz.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `ScheduleMode` | `enum` | Quartz 作业的调度模式 |

### 包 `quartz.domain.model`

| 类 | 类型 | 说明 |
|------|------|------|
| `QuartzJobInfo` | `record` | 已调度 Quartz 作业及其触发器的不可变快照 |
| `QuartzTriggerInfo` | `record` | Quartz 触发器当前状态的不可变快照 |

### 包 `quartz.domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `QuartzJobRequest` | `record` | 调度 Quartz 作业的请求；构造时校验必填字段 |

### 包 `quartz.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `TimePeriod` | `enum` | Quartz Cron 转换使用的常见调度周期单位 |

### 包 `quartz.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `QuartzScheduleManager` | `class` | 围绕 Quartz Scheduler 的封装，在单一分组内管理作业与触发器，使用内存（RAM）作业存储 |

### 包 `resource.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `MetaResourceDao` | `interface` | 已存储文件元数据记录的 MyBatis-Plus Mapper |

### 包 `resource.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `MetaResourceEntity` | `class` | 已存储文件元数据的持久化实体（按 OwnershipEntity 归属列隔离） |

### 包 `resource.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `MetaResourceService` | `class` | 协调二进制存储后端与持久化文件元数据 |

### 包 `resource.storage`

| 类 | 类型 | 说明 |
|------|------|------|
| `ResourceStorageRegistry` | `class` | 按 ResourceStore 索引的 ResourceStore 实现注册表 |

### 包 `server.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `ServiceRegistryEntity` | `class` | 已注册服务实例的持久化实体；平台级基础设施数据，不按租户隔离 |

### 包 `server.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `ServiceRole` | `enum` | 服务节点在集群中的角色 |
| `ServiceStatus` | `enum` | 服务节点的可用性状态 |

### 包 `server.domain.model`

| 类 | 类型 | 说明 |
|------|------|------|
| `ServiceInfo` | `class` | 已注册服务节点的流式领域模型 |
| `ServiceLifecycle` | `class` | 本地服务节点的可变生命周期状态 |

### 包 `server.registry`

| 类 | 类型 | 说明 |
|------|------|------|
| `ServiceRegistry` | `interface` | 服务节点注册表契约，负责注册、注销与查询集群内服务实例 |

### 包 `server.runtime`

| 类 | 类型 | 说明 |
|------|------|------|
| `ServiceNodeHolder` | `class` | 持有本地节点的服务注册状态，并提供 Leader 判定、心跳失效检测与分片计算 |

### 包 `watcher.contract`

| 类 | 类型 | 说明 |
|------|------|------|
| `IWatcher` | `interface` | 后台 Watcher 接口，继承 Runnable 以便在线程池中执行 |

### 包 `watcher.runtime`

| 类 | 类型 | 说明 |
|------|------|------|
| `AbstractWatcher` | `class` | 后台 Watcher 的模板方法基类 |
| `WatcherSupervisor` | `class` | 管理后台 IWatcher 线程池的监督器 |


## 包参考

| 领域 | 参考 |
|------|-----------|
| 启动 SPI | [`references/bootstrap.md`](references/bootstrap.md) |
| 持久化 | [`references/persistence.md`](references/persistence.md) |
| OpenAPI | [`references/openapi.md`](references/openapi.md) |
| Quartz | [`references/quartz.md`](references/quartz.md) |
| 资源存储 | [`references/resource.md`](references/resource.md) |
| 服务注册 | [`references/server.md`](references/server.md) |
| Watcher 运行时 | [`references/watcher.md`](references/watcher.md) |

## 相关模块

| 主题 | 模块 / 文档 |
|-------|----------------|
| 快照、`SessionContext`、`ResourceStore` SPI | `innospots-nexus-base` |
| 插件运行时、Page DSL | `innospots-nexus-plugin` |
| 控制台 catalog 索引 | `innospots-nexus-console` |
| 模块布局规则 | `java:project` → `module-layout.md` |
