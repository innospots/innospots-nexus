# 包 `domain.project`

## ProjectSnapshot

**Type:** record

工作区内项目的会话/传输快照。项目在共享工作区下提供可选的
业务隔离。

| 组件 | 类型 | 说明 |
|-----------|------|-------------|
| `tenantId` | `String` | 所属租户 |
| `workspaceId` | `String` | 所属工作区 |
| `projectId` | `String` | 项目标识 |
| `projectCode` | `String` | 稳定的业务编码 |
| `projectName` | `String` | 显示名称 |
| `description` | `String` | 可选描述 |
| `status` | `BasicStatus` | 项目可用状态 |

非内核 `ProjectEntity`；内核拥有持久化的项目记录。
