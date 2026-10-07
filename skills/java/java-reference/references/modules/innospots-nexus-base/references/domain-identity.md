# 包 `domain.identity`

## RoleSnapshot

**类型：** record

角色定义，包含唯一标识符、显示名称与程序化编码。 作为跨模块边界的领域值对象使用。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `roleId` | `String` | 角色 ID |
| `roleName` | `String` | 角色名称 |
| `roleCode` | `String` | 角色编码 |
| `status` | `BasicStatus` | 状态 |
| `userIds` | `List<String>` | 成员用户 ID 列表 |

### 方法

#### `of(String roleId, String roleName, String roleCode, BasicStatus status) → RoleSnapshot`
- **说明：** 创建不含成员用户 ID 的角色信息。
- **参数：**
  - `roleId` — 角色 ID
  - `roleName` — 角色名称
  - `roleCode` — 角色编码
  - `status` — 状态
- **返回：** 角色快照


## UserGroupSnapshot

**类型：** record

具有层级结构（父组）、负责人与协助人的用户组/团队。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `groupId` | `Long` | 用户组 ID |
| `groupName` | `String` | 用户组名称 |
| `groupCode` | `String` | 用户组编码 |
| `parentGroupId` | `Long` | 父组 ID |
| `headUserId` | `Long` | 负责人用户 ID |
| `assistantUserIds` | `List<Long>` | 协助人用户 ID 列表 |
| `status` | `BasicStatus` | 状态 |


## UserSnapshot

**类型：** class

用户的会话/传输快照。包含不可变身份字段（userId、userName、realName）及可变属性如邮箱、头像、组成员与分配角色。非 portal 领域实体。

### 方法

#### `fromContextOptional() → Optional<UserSnapshot>`
- **说明：** 当 TLC 中存在身份键时，从当前线程上下文重建快照。
- **返回：** 快照可选值，无用户 ID 时为空

#### `fromContext() → UserSnapshot`
- **说明：** 从当前 TLC 身份键重建快照。
- **返回：** 用户快照

#### `fromClaims(Map<String, ?> claims) → UserSnapshot`
- **说明：** 从令牌或会话声明构建快照。键与 TLC 常量 （TLC、TLC 等）对齐。 / public static UserSnapshot fromClaims(Map claims)
