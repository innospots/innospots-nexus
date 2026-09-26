# 包 `persistence.scope`

## OwnerType

**类型：** enum

持久化行归属层级（与控制台 `RoleOwnerType` 名称一致）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PLATFORM` | 平台级资源，无 `ownerId` |
| `TENANT` | 租户级资源，`ownerId` 为租户 ID |
| `WORKSPACE` | 工作区级资源，`ownerId` 为工作区 ID |

## OwnershipScope

**类型：** class

基于 `OwnershipEntity` 的查询隔离与归属写入。

### 方法

#### `captureFromSession() → PersistenceOwnership`

- **说明：** 从当前会话解析资源默认归属（工作区优先，其次租户，否则 PLATFORM）。

#### `stamp(OwnershipEntity entity, PersistenceOwnership ownership) → void`

- **说明：** 将归属三元组写入实体列（`ownerType` / `ownerId` / `securityRealm`）。

#### `apply(LambdaQueryWrapper<T> query, PersistenceOwnership ownership) → LambdaQueryWrapper<T>`

- **说明：** 在 MyBatis-Plus 查询上追加归属过滤条件。

#### `assertOwnership(OwnershipEntity entity, PersistenceOwnership ownership) → void`

- **说明：** 校验实体与期望归属一致；否则抛出 `NexusException`（`NO_PERMISSION`）。

## PersistenceOwnership

**类型：** record

`OwnershipEntity` 归属三元组。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `ownerType` | `OwnerType` | 归属层级 |
| `ownerId` | `String` | 与层级匹配的 ID；`PLATFORM` 时为 null |
| `securityRealm` | `String` | 安全域（`PLATFORM` 或 `TENANT`） |

### 方法

#### `ownerTypeName() → String`

- **说明：** 返回 `ownerType.name()`，用于持久化列 `ownerType`。
