# 包 `domain.workspace`

## WorkspaceSnapshot

**类型：** record

租户工作区（{@code nx_workspace}）的会话/传输快照。 工作区是租户下的资源共享边界。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | 租户 ID |
| `workspaceId` | `String` | 工作区 ID |
| `workspaceCode` | `String` | 工作区编码 |
| `workspaceName` | `String` | 工作区名称 |
| `status` | `BasicStatus` | 状态 |
