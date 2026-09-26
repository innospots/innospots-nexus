# 包 `navigation.service`

## NavigationMenuAssembler

**类型：** class

从持久化权限目录组装当前用户可见的导航菜单树。

### 方法

#### `navigationMenus(String workspaceId, AuthorizationSubject subject) → List<NavigationMenuVo>`
- **说明：** 创建导航菜单组装器。 / public NavigationMenuAssembler(PermissionVisibilityService visibilityService) { if (visibilityService == null) { throw new IllegalArgumentException("visibilityService is required"); } this.visibilityService = visibilityService; } /** 返回当前主体可见的 MENU 树。
- **参数：**
  - `workspaceId` — 当前 Workspace ID
  - `subject` — 当前鉴权主体
- **返回：** 导航树根节点
