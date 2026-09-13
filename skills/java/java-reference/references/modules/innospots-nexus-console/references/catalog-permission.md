# 目录索引与权限运行时

## 目录 vs 插件边界

| 层 | 模块 | 职责 |
|-------|--------|----------------|
| 贡献 + Page DSL 事实来源 | **plugin** | `ConsoleContributionCatalog`、`PageDslLoader`、解码/验证 |
| 持久化目录索引 | **console** | `nx_console_catalog_resource`、同步 + 读取 API |
| 授权授予 | **console** | `nx_permission_grant`、替换 + 可见性 |

同步规则：**ACTIVE 插件贡献**是事实来源；数据库索引是用于权限 UI 和 `RequestAuthorizer` 的
**物化视图**。同步**不会**自动授权。

## 目录资源类型

`CatalogResourceType`：

- `MODULE` — 控制台模块根
- `MENU` — 来自贡献的导航入口
- `PAGE` — UiSpec / Page DSL 页面
- `ACTION` — 按钮/操作权限
- `DATASOURCE` — HTTP 或服务数据源端点
- `CAPABILITY` — 不透明能力标志

`ConsoleCatalogResourceEntity` 存储树形父链接、请求匹配元数据
（方法、路径模式）、`security_realm` 和源插件 ID。

## 同步管道

```text
Plugin enable / POST /console/catalog/sync / ConsoleCatalogSyncStartupTask
    → ConsoleCatalogSyncService.sync()
    → read ConsoleContributionCatalog + PageDslLoader
    → upsert/disable rows in nx_console_catalog_resource
    → CatalogSyncResult(created, updated, disabled)
```

注入 `ConsoleCatalogSyncService` 时，`PluginManagementEndpoint` 可在启用/禁用后可选调用同步。

## 权限授权

`PermissionGrantEntity` 关联：

- **主体：** `PermissionSubjectType` + 主体 ID（角色或组织单元）
- **资源：** 目录资源 ID + 可选数据源查询条件

`PermissionGrantReplaceRequest` 是 `GrantManagementEndpoint` 上 PUT 替换端点的**完整快照**。

`PermissionGrantService` 执行替换语义；部分补丁不是契约。

## 请求授权（框架中立）

`RequestAuthorizer` API：

```text
Input:  AuthorizationRequest (page key, HTTP method, path, workspace context)
Output: AuthorizationDecision (allowed / denied)
```

固定评估顺序：

1. **PAGE** — 若页面未授权则提前拒绝
2. **DATASOURCE** — 将方法 + URL 匹配到目录数据源条目

不读取原始 `HttpServletRequest`；适配器提取 `AuthorizationRequest`。

支持类型：

| 类型 | 用途 |
|------|---------|
| `AuthorizationSubject` | 用于授权查找的用户 ID + 角色/组织绑定 |
| `AuthorizationSubjectResolver` | 端口：从运行时获取当前主体 |
| `AuthorizationContext` | 用于检查的工作空间 + 主体快照 |
| `AuthorizationScope` | 用于嵌套检查的线程本地作用域 |

## 可见性与导航

| 组件 | 输出 |
|-----------|--------|
| `PermissionVisibilityService` | 当前用户的 `PermissionResourceVo` 列表 |
| `NavigationMenuAssembler` | 按授权过滤的 `NavigationMenuVo` 树 |
| `CurrentAuthorizationEndpoint` | 可见资源的 REST 暴露 |
| `NavigationMenuEndpoint` | 侧边栏菜单的 REST 暴露 |

两者都需要工作空间上下文（导航使用 `SessionContext.requireWorkspaceId()`）。

## 管理 UI 流程

```text
GET  /console/catalog/tree        → configure grants (full catalog)
GET  /console/roles/{id}/permissions → load grant editor state
PUT  /console/roles/{id}/permissions → save full grant set
GET  /console/me/permissions        → runtime menu/page/action/datasource visibility
GET  /console/navigation/menus      → rendered sidebar
```

组织单元授权在 `/organization-units/{unitId}/permissions` 下镜像角色授权。
