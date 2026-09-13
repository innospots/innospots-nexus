# 包 `thread`

## SessionContext

**Type:** class

`TLC` 的类型化门面，用于用户与作用域快照。

绑定顺序：`bindUser` → `bindTenant` → `bindWorkspace` → `bindProject`（可选）。

| 方法 | 说明 |
|--------|-------------|
| `bindUser` / `clearUser` | 用户快照及 TLC 身份键 |
| `bindTenant` | `TenantSnapshot` + `OrganizationSnapshot`（`tenantId` 必须一致） |
| `bindWorkspace` | `WorkspaceSnapshot`；同步 `tenantId` / `workspaceId` 到 TLC |
| `bindProject` | `ProjectSnapshot` 或清除；同步租户/工作区/项目到 TLC |
| `requireUser` / `user()` | 当前用户快照 |
| `tenant()` / `organization()` / `workspace()` / `project()` | 可选的作用域快照 |
| `tenantId()` / `workspaceId()` / `projectId()` | TLC 作用域键 |
| `requireWorkspaceId()` | 工作区缺失时失败 |

## TLC

**Type:** class

线程本地上下文映射，用于追踪 ID、租户/工作区/项目 ID、用户身份、
会话/对话 ID、安全域及平台/租户成员 ID。

| 键常量 | 说明 |
|--------------|-------------|
| `TRACE_ID` | 分布式追踪 |
| `TENANT_ID` / `WORKSPACE_ID` / `PROJECT_ID` | 作用域隔离键 |
| `USER_ID` / `USER_NAME` | 已认证用户（`userId` 为 `Long`） |
| `SESSION_ID` / `CONVERSATION_ID` | 会话追踪 |
| `SECURITY_REALM` | 认证域 |
| `TENANT_MEMBER_ID` / `PLATFORM_USER_ID` | 成员标识 |

使用 `TLC.scope(Map)` 配合 try-with-resources 进行作用域注入。

## AsyncExecutors

**Type:** class

由单例 `NexusThreadPoolExecutor` 支撑的全局异步执行器门面。

## NexusThreadFactory

**Type:** class

命名 `ThreadFactory`（默认前缀 `nexus-worker`），带序号。

## NexusThreadPoolExecutor

**Type:** class

将 `TLC` 从提交线程传播到工作线程的 `ThreadPoolExecutor`。

## ThreadPoolBuilder

**Type:** class

`NexusThreadPoolExecutor` 实例的流式构建器。
