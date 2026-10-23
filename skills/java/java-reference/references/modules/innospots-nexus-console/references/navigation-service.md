# 包 `navigation.service`

## MetaSitemapMenuFilter

**类型：** class

按 meta-sitemap 节点的 `hidden` 与 `roles` 裁剪默认菜单；不执行 catalog 授权判定。

### 方法

#### `filterVisibleMenus(List<SitemapMenuItem> menus, AuthorizationSubject subject) → List<SitemapMenuItem>`

返回过滤后的菜单子树；空 `group` 在子节点全部被裁剪时移除。

## MetaSitemapNavigationConverter

**类型：** class

将 meta-sitemap 菜单节点转换为 `NavigationMenuVo`。

### 方法

#### `toNavigationMenus(List<SitemapMenuItem> menus) → List<NavigationMenuVo>`

仅转换 `page` 与有可见子节点的 `group`。

## NavigationMenuAssembler

**类型：** class

从 catalog 可见资源组装当前用户可导航的 MODULE/MENU 树。

### 构造方法

#### `NavigationMenuAssembler(PermissionVisibilityService visibilityService)`

可见性服务必填。

### 方法

#### `navigationMenus(String workspaceId, AuthorizationSubject subject) → List<NavigationMenuVo>`

以当前 `TLC.securityRealm()`（缺省 `TENANT`）构造工作区归属。

#### `navigationMenus(ConsoleOwnership ownership, AuthorizationSubject subject) → List<NavigationMenuVo>`

按显式归属组装导航树，适用于 PLATFORM 等非工作区会话。

## NavigationMenuComposer

**类型：** class

合并 catalog 导航与经角色裁剪的 meta-sitemap 默认菜单，catalog 项排在前面。

### 构造方法

#### `NavigationMenuComposer(NavigationMenuAssembler catalogAssembler, SitemapService sitemapService, MetaSitemapMenuFilter metaSitemapMenuFilter, MetaSitemapNavigationConverter metaSitemapNavigationConverter)`

四个依赖均必填。

### 方法

#### `compose(ConsoleOwnership ownership, AuthorizationSubject subject) → List<NavigationMenuVo>`

任一侧为空时返回另一侧；两侧均有内容时按 catalog → meta-sitemap 顺序合并。
