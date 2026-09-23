# 包 `scope.service`

## ScopeFacade

**类型：** class

为租户业务会话激活工作区与项目作用域。

### 方法

#### `selectWorkspace(String tenantUserId, SelectWorkspaceRequest request) → AuthTokenVo`

- **说明：** 将租户业务会话交换为工作区作用域令牌对。
- **参数：**
  - `tenantUserId` — tenant-realm user 标识符
  - `request` — 待激活的租户与工作区
- **返回：** workspace-scoped 业务令牌对

#### `selectProject(String tenantUserId, SelectProjectRequest request) → AuthTokenVo`

- **说明：** 将工作区作用域会话交换为项目作用域令牌对。
- **参数：**
  - `tenantUserId` — tenant-realm user 标识符
  - `request` — 待激活的租户、工作区与项目
- **返回：** project-scoped 业务令牌对

## SessionScopeBinder

**类型：** class

将认证身份与作用域快照绑定到 {@link SessionContext}。

### 方法

#### `bindAfterAuth(AuthUser user, AuthSessionScope scope) → void`

- **说明：** 认证流程成功后绑定身份与作用域快照。
- **参数：**
  - `user` — 已认证用户
  - `scope` — 签发的会话作用域

#### `bindScope(AuthSessionScope scope) → void`

- **说明：** 为给定会话作用域绑定租户、工作区与项目快照。
- **参数：**
  - `scope` — 签发的会话作用域

#### `bindFromClaims(TokenClaims claims) → void`

- **说明：** 从紧凑令牌声明重建会话快照。
- **参数：**
  - `claims` — 解析后的令牌声明

#### `clear() → void`

- **说明：** 清除所有绑定的会话快照与 TLC 作用域键。
