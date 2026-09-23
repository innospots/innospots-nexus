# 包 `role.domain.request`

## RoleBindingAddRequest

**类型：** record

向角色添加 USER 或 ORG_UNIT 主体的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `subjectType` | `RoleBindingSubjectType` | 主体类型 |
| `subjectIds` | `List<String>` | subject 标识符s to bind |

### 构造方法

#### `RoleBindingAddRequest()`

## RoleBindingPageRequest

**类型：** record

绑定到角色的主体的分页查询。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | fuzzy subject 标识符 |
| `subjectType` | `RoleBindingSubjectType` | 可选 USER 或 ORG_UNIT filter |
| `pageNo` | `long` | 从 1 开始的页码 |
| `pageSize` | `long` | 分页大小 |

### 构造方法

#### `RoleBindingPageRequest()`

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

**类型：** record

由管理控制台查询参数绑定的分页角色查询。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 角色名称或编码的模糊匹配 |
| `status` | `BasicStatus` | 可选 生命周期状态 |
| `builtIn` | `Boolean` | 可选 built-in role filter |
| `pageNo` | `long` | 从 1 开始的页码 |
| `pageSize` | `long` | 分页大小 |

### 构造方法

#### `RolePageRequest()`

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
