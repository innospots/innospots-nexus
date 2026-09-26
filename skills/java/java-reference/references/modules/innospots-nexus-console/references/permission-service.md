# 包 `permission.service`

## GrantSubjectAccess

**类型：** class

校验授权主体属于当前控制台归属上下文。


## PermissionGrantService

**类型：** class

管理角色和组织单元授权，并以全量替换方式保存授权结果。

### 方法

#### `replace(PermissionSubjectType subjectType,
            String subjectId,
            PermissionGrantReplaceRequest request) → void`
- **说明：** 创建授权服务。 / public PermissionGrantService( PermissionGrantDao grantDao, ConsoleCatalogResourceDao resourceDao, GrantSubjectAccess grantSubjectAccess ) { this.grantDao = Checks.notNull(grantDao, "grantDao"); this.resourceDao = Checks.notNull(resourceDao, "resourceDao"); this.grantSubjectAccess = Checks.notNull(grantSubjectAccess, "grantSubjectAccess"); } /** 在一个事务中全量替换指定角色或组织单元的授权。 方法先完成主体、资源、父子关系和 datasource 条件校验，再删除旧授权并写入新授权， 从而避免部分校验失败时留下不完整的授权集合。
- **参数：**
  - `subjectType` — 授权主体类型
  - `subjectId` — 角色或组织单元 ID
  - `request` — 主体最终应拥有的完整授权集合

#### `list(PermissionSubjectType subjectType,
            String subjectId) → PermissionGrantReplaceRequest`
- **说明：** 查询指定角色或组织单元当前的完整授权集合。
- **参数：**
  - `subjectType` — 授权主体类型
  - `subjectId` — 角色或组织单元 ID
- **返回：** 当前授权及 datasource 附加查询条件


## PermissionVisibilityService

**类型：** class

按当前主体构建菜单、页面、action 和 datasource 的可见资源视图。

### 方法

#### `visible(String workspaceId,
            AuthorizationSubject subject) → List<ConsoleCatalogResourceEntity>`
- **说明：** 创建权限资源可见性服务。 / public PermissionVisibilityService( ConsoleCatalogResourceDao resourceDao, PermissionGrantDao grantDao ) { this.resourceDao = resourceDao; this.grantDao = grantDao; } /** 返回主体可见的启用资源。 角色和组织单元授权按集合并集处理；子资源必须依赖可见的页面或菜单父节点，MODULE 仅作为 分组节点，不要求单独授权。
- **参数：**
  - `workspaceId` — 当前 Workspace ID
  - `subject` — 当前授权主体
- **返回：** 按资源目录顺序返回的可见资源；参数无效时返回空集合
