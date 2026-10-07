# 包 `domain.project`

## ProjectSnapshot

**类型：** record

工作区内项目的会话/传输快照。 项目在共享工作区下提供业务隔离。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | 租户 ID |
| `workspaceId` | `String` | 工作区 ID |
| `projectId` | `String` | 项目 ID |
| `projectCode` | `String` | 项目编码 |
| `projectName` | `String` | 项目名称 |
| `description` | `String` | 项目描述 |
| `status` | `BasicStatus` | 状态 |
