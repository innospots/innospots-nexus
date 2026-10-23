# 包 `role.endpoint`

## RoleBindingEndpoint

**类型：** interface

角色绑定 JAX-RS 契约。接口声明 `@EndpointModule("role")` 与 `@EndpointGrant`，
类级路径由 portal/platform 的业务实现绑定。

### 方法

- `pageRoleBindings(roleId, request)`：分页
- `addRoleBindings(roleId, request)`：新增绑定
- `removeRoleBinding(roleId, bindingId)`：移除绑定


## RoleEndpoint

**类型：** interface

角色生命周期与查询 JAX-RS 契约。创建请求不携带 `ownerType` / `ownerId`，
由实现侧按业务域固化归属；类级路径由实现绑定。

### 方法

- `pageRoles(request)` / `getRole(roleId)`
- `createRole(request)` / `updateRole(roleId, request)`
- `updateRoleStatus(roleId, request)` / `deleteRole(roleId)`
- `listRoleOptions(status)`
