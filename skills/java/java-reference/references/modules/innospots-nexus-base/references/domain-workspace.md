# 包 `domain.workspace`

## WorkspaceSnapshot

**Type:** record

租户工作区（`nx_workspace`）的会话/传输快照。工作区是租户下的
资源共享边界。

| 组件 | 类型 | 说明 |
|-----------|------|-------------|
| `tenantId` | `String` | 所属租户 |
| `workspaceId` | `String` | 工作区标识 |
| `workspaceCode` | `String` | 稳定的业务编码 |
| `workspaceName` | `String` | 显示名称 |
| `status` | `BasicStatus` | 工作区可用状态 |

非内核领域实体。
