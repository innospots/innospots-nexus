# 包 `permission.service`

## `GrantSubjectAccess`

**类型：** class

校验 ROLE / ORG_UNIT 授权主体属于当前 `ConsoleOwnership`。

### 构造方法

#### `GrantSubjectAccess(RoleDao roleDao, OrganizationUnitDirectory organizationUnitDirectory)`

`roleDao` 必填；`organizationUnitDirectory` 可为 `null`，为空时只做基本归属校验。

### 方法

#### `ensureSubjectInScope(PermissionSubjectType subjectType, String subjectId, ConsoleOwnership ownership) → void`

从归属推导租户/工作区范围。PLATFORM 归属不依赖会话快照。

#### `ensureSubjectInScope(PermissionSubjectType subjectType, String subjectId, ConsoleOwnership ownership, String tenantId, String workspaceId) → void`

显式传入租户/工作区范围的完整形式。

## `PermissionGrantService`

**类型：** class

以全量替换方式管理角色与组织单元的 catalog 授权。

### 构造方法

#### `PermissionGrantService(PermissionGrantDao grantDao, ConsoleCatalogService catalogService, GrantSubjectAccess grantSubjectAccess)`

三个依赖均必填。

### 方法

#### `replace(PermissionSubjectType subjectType, String subjectId, PermissionGrantReplaceRequest request, ConsoleOwnership ownership) → void`

`@Transactional`。校验主体归属、grant key 唯一性与 catalog 可授权资源后，删除旧记录并写入完整集合。

#### `replace(PermissionSubjectType subjectType, String subjectId, PermissionGrantReplaceRequest request, ConsoleOwnership ownership, String tenantId, String workspaceId) → void`

显式租户/工作区版本，事务与校验语义相同。

#### `list(PermissionSubjectType subjectType, String subjectId, ConsoleOwnership ownership) → PermissionGrantReplaceRequest`

按归属读取完整授权集合；读取不检查主体当前是否仍可见。

#### `list(PermissionSubjectType subjectType, String subjectId, ConsoleOwnership ownership, String tenantId, String workspaceId) → PermissionGrantReplaceRequest`

兼容显式作用域参数的读取形式。

当前 catalog v2 拒绝非空 `constraintDefinition`，写入值统一归一为 `null`。

## `PermissionVisibilityService`

**类型：** class

按主体过滤启用的 catalog 资源。当前目录只包含 MODULE / MENU。

### 构造方法

#### `PermissionVisibilityService(ConsoleCatalogResourceDao resourceDao, PermissionGrantDao grantDao)`

### 方法

#### `visible(String workspaceId, AuthorizationSubject subject) → List<ConsoleCatalogResourceEntity>`

以当前 `TLC.securityRealm()`（缺省 `TENANT`）构造工作区归属后查询。

#### `visible(ConsoleOwnership ownership, AuthorizationSubject subject) → List<ConsoleCatalogResourceEntity>`

管理员返回全部启用资源；普通主体要求 MENU 或带 `entryPageKey` 的叶子 MODULE
拥有对应 view grant。父节点必须可见，分组 MODULE 在至少一个子节点可见时保留。
