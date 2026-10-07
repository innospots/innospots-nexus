# 包 `role.domain.vo`

## RoleBindingVo

**类型：** record

分配管理中展示的角色绑定。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `bindingId` | `String` | binding 标识符 |
| `roleId` | `String` | bound 角色标识符 |
| `subjectType` | `RoleBindingSubjectType` | USER 或 ORG_UNIT |
| `subjectId` | `String` | subject 标识符 |
| `createdAt` | `LocalDateTime` | 分配时间 |


## RoleOptionVo

**类型：** record

用于选择器与分配表单的角色精简选项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `roleId` | `String` | 角色标识符 |
| `roleName` | `String` | 显示名称 |
| `roleCode` | `String` | 稳定编码 |
| `administrator` | `Boolean` | 是否为管理员角色 |


## RoleVo

**类型：** record

管理控制台角色视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `roleId` | `String` | 角色标识符 |
| `roleName` | `String` | 显示名称 |
| `roleCode` | `String` | 归属范围内唯一的稳定编码 |
| `ownerType` | `RoleOwnerType` | 归属层级 |
| `ownerId` | `String` | owner 标识符; empty for PLATFORM |
| `securityRealm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `description` | `String` | 可选 描述 |
| `status` | `BasicStatus` | 生命周期状态 |
| `sortOrder` | `Integer` | 显示顺序 |
| `builtIn` | `Boolean` | 角色是否由系统管理 |
| `administrator` | `Boolean` | 角色是否绕过普通资源检查 |
| `memberCount` | `long` | 已分配用户数量 |
| `createdAt` | `LocalDateTime` | 创建时间 |
| `updatedAt` | `LocalDateTime` | 最后更新时间 |
