# 包 `role.domain.enums`

## RoleBindingSubjectType

**类型：** enum

可绑定到角色的主体。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `USER` | 平台用户或租户成员身份。 |
| `ORG_UNIT` | 租户组织单元。 |

## RoleOwnerType

**类型：** enum

拥有角色定义的层级。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PLATFORM` | 运维域平台角色。 |
| `TENANT` | 租户级 role。 |
| `WORKSPACE` | 工作区作用域内的 role。 |
