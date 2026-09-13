# innospots-nexus-core — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：1.0.0

## 模块概览

基于 `innospots-nexus-base` 的业务中立平台基础设施：共享持久化基类、审计填充、Quartz 调度、
服务节点注册、Watcher 运行时、启动 SPI，以及工作空间级文件元数据。

**不包含：** 插件运行时、Page DSL、控制台目录、用户/角色/权限业务，或 Spring Boot 自动配置。

**能力一览：**

| 能力 | 说明 |
|------------|-------------|
| **持久化基类** | `BaseEntity` → `Tenant` → `Workspace` → `Project` 继承链 |
| **审计与 ID** | `AuditMetaObjectHandler`（TLC 驱动填充）、`DbPrimaryGenerator`（ULID） |
| **启动 SPI** | 通过 `NexusStartup` 编排有序的 `NexusStartupTask` |
| **Quartz** | 内存型 `QuartzScheduleManager`，支持 cron/单次/定时模式 |
| **服务节点** | `ServiceRegistry`、`ServiceNodeHolder`、`ServiceRegistryEntity` |
| **Watchers** | `IWatcher` / `AbstractWatcher` / `WatcherSupervisor` 后台循环 |
| **文件元数据** | `nx_meta_resource`、`MetaResourceService`、`ResourceStorageRegistry` |

## 扩展指南

| 需求 | 使用 core 中的 | 不要放在 core 中 |
|------|---------------|-------------------|
| 新业务实体 | 继承 `WorkspaceBaseEntity`（默认）或经批准的 `ProjectBaseEntity` | 业务工作流、REST 端点 |
| 宿主启动后的钩子 | 实现 `NexusStartupTask`，在宿主 `NexusStartup.builder()` 中注册 | 插件/目录/会话逻辑 |
| 后台轮询/同步 | 继承 `AbstractWatcher`，在 `WatcherSupervisor` 上注册 | Watcher 中的领域特定业务规则 |
| 集群服务注册 | `ServiceRegistry` + `ServiceNodeHolder` | 租户/工作空间业务数据 |
| 定时任务 | `QuartzScheduleManager` + `QuartzJobRequest` | 任务业务逻辑（保留在上层模块的 Job 类中） |
| 二进制文件 + 元数据 | 在 `ResourceStorageRegistry` 上注册 `ResourceStore`；使用 `MetaResourceService` | 控制台/内核特定的文件策略 |

作用域快照和 `SessionContext` 位于 **base**（`java-reference` → `innospots-nexus-base`）。

## 类参考

### 包 `bootstrap`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `NexusStartup` | `class` | 有序启动任务执行器（`builder()` → `run()`）。 |
| `NexusStartupContext` | `class` | 启动任务跨步骤属性映射。 |
| `NexusStartupTask` | `interface` | 框架中立的启动后步骤（`name`、`order`、`run`）。 |

### 包 `persistence.entity`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `BaseEntity` | `class` | 审计字段 + `idPrefix()`；平台/领域全局作用域。 |
| `TenantBaseEntity` | `class` | + `tenantId`（TLC 填充）。 |
| `WorkspaceBaseEntity` | `class` | + `workspaceId`（TLC 填充）；业务实体的**默认**基类。 |
| `ProjectBaseEntity` | `class` | + `projectId`（TLC 填充）；仅在设计批准项目隔离时使用。 |

### 包 `persistence.handler`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `AuditMetaObjectHandler` | `class` | 从 `TLC` + 时间戳进行 MyBatis-Plus 插入/更新填充。 |

### 包 `persistence.id`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `DbPrimaryGenerator` | `class` | MyBatis-Plus `IdentifierGenerator`；通过 `entity.idPrefix()` 生成 ULID。 |

### 包 `quartz.converter`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `CronConverter` | `class` | cron 表达式与 `TimePeriod` 辅助类型之间的转换。 |

### 包 `quartz.enums`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `TimePeriod` | `enum` | 用于构建 cron 的命名调度周期预设。 |

### 包 `quartz.domain.enums`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ScheduleMode` | `enum` | `ONCE`、`SCHEDULED`、`CRON`、`MANUAL`。 |

### 包 `quartz.domain.model`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `QuartzJobInfo` | `class` | 用于列表/管理视图的作业摘要。 |
| `QuartzTriggerInfo` | `class` | 触发器摘要（cron、下次触发时间、状态）。 |

### 包 `quartz.domain.request`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `QuartzJobRequest` | `record` | 调度请求，工厂方法：`cron` / `once` / `scheduled`。 |

### 包 `quartz.service`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `QuartzScheduleManager` | `class` | 作业组的 RAM Quartz 调度器包装器。 |

### 包 `resource.dao`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `MetaResourceDao` | `interface` | `nx_meta_resource` 的 `BaseMapper<MetaResourceEntity>`。 |

### 包 `resource.domain.entity`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `MetaResourceEntity` | `class` | 工作空间级文件元数据（`nx_meta_resource`）。 |

### 包 `resource.service`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `MetaResourceService` | `class` | 协调 `ResourceStore` 与持久化元数据。 |

### 包 `resource.storage`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ResourceStorageRegistry` | `class` | 按 `storeMode()` 注册 `ResourceStore`。 |

### 包 `server.domain.entity`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ServiceRegistryEntity` | `class` | 平台级服务实例行（`nx_service_registry`）。 |

### 包 `server.domain.enums`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ServiceRole` | `enum` | 集群角色（`LEADER`、`FOLLOWER` 等）。 |
| `ServiceStatus` | `enum` | 节点可用性（`ONLINE`、`OFFLINE` 等）。 |

### 包 `server.domain.model`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ServiceInfo` | `class` | 流式运行时模型；`serverKey()` = `host:port`。 |
| `ServiceLifecycle` | `class` | 服务节点的本地生命周期标志。 |

### 包 `server.registry`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ServiceRegistry` | `interface` | 注册/注销/列出服务节点。 |

### 包 `server.runtime`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ServiceNodeHolder` | `class` | 本地节点状态、Leader 检测、心跳有效性。 |

### 包 `watcher.contract`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `IWatcher` | `interface` | 后台循环：`check()` → `execute()` → 间隔。 |

### 包 `watcher.runtime`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `AbstractWatcher` | `class` | 带睡眠循环和停止条件的 `IWatcher` 模板实现。 |
| `WatcherSupervisor` | `class` | 多 Watcher 的线程池监管器（`AutoCloseable`）。 |

## 包参考

| 领域 | 参考 |
|------|-----------|
| 启动 SPI | [`references/bootstrap.md`](references/bootstrap.md) |
| 持久化 | [`references/persistence.md`](references/persistence.md) |
| Quartz | [`references/quartz.md`](references/quartz.md) |
| 资源存储 | [`references/resource.md`](references/resource.md) |
| 服务注册 | [`references/server.md`](references/server.md) |
| Watcher 运行时 | [`references/watcher.md`](references/watcher.md) |

## 相关模块

| 主题 | 模块 / 文档 |
|-------|----------------|
| 快照、`SessionContext`、`ResourceStore` SPI | `innospots-nexus-base` |
| 插件运行时、Page DSL | `innospots-nexus-plugin` |
| 控制台目录索引 | `innospots-nexus-console` |
| 模块布局规则 | `java:project` → `module-layout.md` |
