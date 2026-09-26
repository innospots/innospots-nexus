# 包 `role.domain.request`

## RoleBindingAddRequest

**类型：** record

向角色添加 USER 或 ORG_UNIT 主体的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `subjectType` | `RoleBindingSubjectType` | 主体类型 |
| `subjectIds` | `List<String>` | subject 标识符s to bind |


## RoleBindingPageRequest

**类型：** class

绑定到角色的主体的分页查询。


## RoleCreateRequest

**类型：** record

创建由平台、租户或工作区节点拥有的角色的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `roleName` | `String` | 显示名称 |
| `roleCode` | `String` | 归属范围内唯一的稳定编码 |
| `ownerType` | `RoleOwnerType` | 归属层级 |
| `ownerId` | `String` | owner 标识符; empty for PLATFORM |
| `securityRealm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `description` | `String` | 可选 role 描述 |
| `sortOrder` | `Integer` | 显示顺序 |


## RolePageRequest

**类型：** class

由管理控制台查询参数绑定的分页角色查询。


## RoleStatusUpdateRequest

**类型：** record

启用或禁用角色的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `status` | `BasicStatus` | 目标角色状态 |


## RoleUpdateRequest

**类型：** record

更新可变角色档案字段的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `roleName` | `String` | 显示名称 |
| `description` | `String` | 可选 role 描述 |
| `sortOrder` | `Integer` | 显示顺序 |
