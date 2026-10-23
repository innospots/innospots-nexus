# 包 `permission.endpoint`

全部类型均为 Jakarta REST **契约接口**。角色与组织单元路径由 portal/platform 的实现类绑定；
公共页面访问接口在契约自身固定路径。

## `CurrentAuthorizationEndpoint`

**类型：** interface

`@EndpointModule("permission")`、`@NexusAuthenticatedApi`。返回当前用户可用的授权资源。

### 方法

#### `listResources() → R<List<PermissionResourceVo>>`

`GET`；grant 为 `permission.visible.list`（`READ`），`ignorePermission = true`。

## `GrantManagementEndpoint`

**类型：** interface

角色权限全量替换契约。接口不声明类级 `@Path`，由实现类提供角色资源前缀。

### 方法

#### `getRolePermissions(String roleId) → R<PermissionGrantReplaceRequest>`

`GET`；grant 为 `permission.grant.role.get`（`READ`）。

#### `replaceRolePermissions(String roleId, PermissionGrantReplaceRequest request) → R<Void>`

`PUT`；grant 为 `permission.grant.role.replace`（`REPLACE`），
`recordArgs = false`、`auditLog = true`。

## `OrganizationUnitGrantManagementEndpoint`

**类型：** interface

组织单元权限全量替换契约。接口不声明类级 `@Path`，由实现类提供组织单元资源前缀。

### 方法

#### `getOrganizationUnitPermissions(String unitId) → R<PermissionGrantReplaceRequest>`

`GET`；grant 为 `permission.grant.org-unit.get`（`READ`）。

#### `replaceOrganizationUnitPermissions(String unitId, PermissionGrantReplaceRequest request) → R<Void>`

`PUT`；grant 为 `permission.grant.org-unit.replace`（`REPLACE`），
`recordArgs = false`、`auditLog = true`。

## `PageAccessEndpoint`

**类型：** interface

固定路径 `ConsoleConstant.PUBLIC_API_PREFIX + "/page-access"`，不声明
`@EndpointModule` / `@EndpointGrant`，属于公共 API。

### 方法

#### `verifyPageAccess(PageAccessVerifyRequest request) → R<PageAccessVerifyResult>`

`POST /verify`，校验 catalog 页面入口与权限分组对应的访问权限。
