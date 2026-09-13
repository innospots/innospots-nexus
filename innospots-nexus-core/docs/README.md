# innospots-nexus-core 文档

本目录描述 **`innospots-nexus-core`** 模块：业务中立的平台基础设施层。

## 模块定位

```text
innospots-nexus-base
        ↓
innospots-nexus-core          ← 本模块
        ↓
innospots-nexus-plugin
        ↓
innospots-nexus-console
        ↓
kernel / platform
```

Core **不再**包含插件运行时、Page DSL、console catalog 索引或会话/对话领域。详见根目录
[`AGENTS.md`](../../AGENTS.md)。

## 职责范围

| 包 | 职责 |
|----|------|
| `core.persistence` | `BaseEntity` 继承链、审计填充、`DbPrimaryGenerator` |
| `core.quartz` | Quartz 调度封装、`TimePeriod`、Cron 转换 |
| `core.server` | 服务节点注册与运行时持有 |
| `core.watcher` | 后台 watcher 契约与线程池运行时 |
| `core.bootstrap` | `NexusStartup` / `NexusStartupTask` 启动编排 SPI |
| `core.resource` | `nx_meta_resource` 元数据、`MetaResourceService`、`ResourceStorageRegistry` |

## 持久化基类

```text
BaseEntity
  └─ TenantBaseEntity        tenantId
       └─ WorkspaceBaseEntity  workspaceId
            └─ ProjectBaseEntity  projectId
```

上层模块（console、kernel、platform）的业务实体应继承对应层级，而不是在 core 中定义业务表。

## 资源存储

- 二进制 SPI：`base.resources.ResourceStore`
- 元数据持久化：`core.resource.dao.MetaResourceDao`
- 协调层：`core.resource.service.MetaResourceService`
- 后端注册：`core.resource.storage.ResourceStorageRegistry`

## 相关文档

| 主题 | 位置 |
|------|------|
| **模块 API 索引（开发引用）** | [`../../skills/java/java-reference/references/modules/innospots-nexus-core/README.md`](../../skills/java/java-reference/references/modules/innospots-nexus-core/README.md) |
| 插件运行时、安装、`console@1`、Page DSL | [`../../innospots-nexus-plugin/docs/plugin/manual/README.md`](../../innospots-nexus-plugin/docs/plugin/manual/README.md) |
| Console catalog 索引（`nx_console_catalog_resource`） | `innospots-nexus-console` 模块 `console.catalog.*` |
| 模块分层与依赖规则 | [`../../skills/java/java-project/references/module-layout.md`](../../skills/java/java-project/references/module-layout.md) |
| 历史扩展设计（归档，只读） | [`archive/README.md`](archive/README.md) |

## 归档说明

[`archive/`](archive/) 保存插件体系统一前的历史设计，**不得**作为新实现规范。现行插件文档已迁至
`innospots-nexus-plugin/docs/`。
