# 包 `scope`

## ConsoleOwnership

**类型：** record

控制台行级归属三元组，由会话或业务规则解析。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `ownerType` | `RoleOwnerType` | PLATFORM、TENANT 或 WORKSPACE |
| `ownerId` | `String` | 归属 ID；PLATFORM 时为 null |
| `securityRealm` | `String` | PLATFORM 或 TENANT |


## ConsoleOwnershipGuard

**类型：** class

控制台 API 入口校验会话是否满足数据归属层级要求。


## ConsoleOwnershipLevel

**类型：** enum

控制台数据默认归属层级（业务可见性与会话要求）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `TENANT` | — |
| `WORKSPACE` | — |


## ConsoleOwnershipScope

**类型：** class

基于 ownerType / ownerId / securityRealm 的查询隔离。

### 方法

#### `requireOwnership(ConsoleOwnershipLevel level) → ConsoleOwnership`
- **说明：** 从当前会话解析指定层级的归属。
- **参数：**
  - `level` — 数据默认归属层级
- **返回：** 归属三元组

#### `captureForAudit() → ConsoleOwnership`
- **说明：** 为审计等场景捕获当前会话归属；无租户时回退为 PLATFORM。 / public static ConsoleOwnership captureForAudit()

#### `apply(LambdaQueryWrapper<T> query,
            ConsoleOwnership ownership) → LambdaQueryWrapper<T>`
- **说明：** 将归属条件追加到查询。 / public static LambdaQueryWrapper apply( LambdaQueryWrapper query, ConsoleOwnership ownership )

#### `applyRoleListVisibility(LambdaQueryWrapper<RoleEntity> query,
            String tenantId,
            String workspaceId,
            String securityRealm) → LambdaQueryWrapper<RoleEntity>`
- **说明：** 工作区会话下可见角色：同租户 TENANT 级 + 当前 WORKSPACE 级。 / public static LambdaQueryWrapper applyRoleListVisibility( LambdaQueryWrapper query, String tenantId, String workspaceId, String securityRealm )

#### `assertRoleReadable(RoleEntity entity,
            String tenantId,
            ConsoleOwnership workspaceOwnership) → void`
- **说明：** 校验角色在当前工作区会话下可读。 / public static void assertRoleReadable( RoleEntity entity, String tenantId, ConsoleOwnership workspaceOwnership )

#### `assertRoleWritable(RoleEntity entity,
            String tenantId,
            ConsoleOwnership workspaceOwnership) → void`
- **说明：** 校验角色在当前会话下可写（TENANT 或当前 WORKSPACE 归属）。 / public static void assertRoleWritable( RoleEntity entity, String tenantId, ConsoleOwnership workspaceOwnership )

#### `assertOwnership(OwnershipEntity entity, ConsoleOwnership ownership) → void`
- **说明：** 校验实体行与期望归属一致。 / public static void assertOwnership(OwnershipEntity entity, ConsoleOwnership ownership)

#### `workspaceOwnership(String workspaceId, String securityRealm) → ConsoleOwnership`
- **说明：** 由工作区 ID 构造 WORKSPACE 归属（导航、授权可见性等）。 / public static ConsoleOwnership workspaceOwnership(String workspaceId, String securityRealm)
