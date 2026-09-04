# Console Plugin Catalog and Navigation Design

## 1. 文档定位

`innospots-nexus-console` 的两类菜单 API：

1. **Catalog** — 权限设置页的插件功能树（菜单 + 页面操作）。
2. **Navigation** — 登录用户侧边栏 MENU 树。

共享持久化索引 `nx_permission_resource`；Catalog 与 Navigation 是不同投影。

## 2. 已确认决策

1. 事实源：运行时 `ConsoleContributionCatalog` + `UiSpecLoader`；**不是** `nx_menu`。
2. `PermissionResourceSyncService` 归属 **console**（从 kernel 迁入）。
3. sync 输入：**仅 ACTIVE 插件贡献**；workspace **全量对账**（非 per-plugin 增量）。
4. API：`GET /console/catalog/tree`、`POST /console/catalog/sync`、`GET /console/navigation/menus`。
5. 删除未实现的 `PermissionCatalogEndpoint`。
6. `nx_menu` / `MenuEndpoint` 本阶段不实现；`MenuEntryPlugin` 保留为插件入口页。
7. sync 触发：**启动 Task（order=200）** + **PluginManagementEndpoint 启停后** + 手动 POST；**不用** PluginEventBus 监听器（V1）。
8. 内置插件 ACTIVE：宿主配置 `requiredPluginIds`（唯一方式）。

## 3. 模块职责（单一职责）

| 包 | 职责 | 禁止 |
|----|------|------|
| `console.catalog` | sync、树组装、`ConsoleCatalogEndpoint` | JWT、grant 写入 |
| `console.navigation` | MENU 树组装、`NavigationMenuEndpoint` | 读内存 Catalog |
| `console.permission` | 实体/DAO、grant、visibility、`RequestAuthorizer` | HTTP 路由 |
| `console.plugin` | 安装启停 API；**启停成功后调 sync** | 定义插件运行时 |
| `console.entry` | 内置 entryPlugin 声明 | 业务 CRUD |
| **core** | `ConsoleContributionCatalog`、插件运行时 | sync、菜单 API |

kernel：**不**含 sync、菜单 API。

## 4. 架构（一层索引，两个投影）

```text
ACTIVE 插件 → ConsoleContributionCatalog（内存，鉴权校验用）
                    │
                    │  PermissionResourceSyncService.sync()  全量对账
                    ▼
              nx_permission_resource（DB，API 读这里）
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
  catalog/tree            navigation/menus
  （全集 ENABLED）         （MENU + 权限裁剪）
```

**不维护第三套菜单表。** `nx_menu` 废弃，不双轨。

## 5. API

| 方法 | 路径 | 读源 | 用途 |
|------|------|------|------|
| GET | `/console/catalog/tree` | DB | 权限设置勾选 |
| POST | `/console/catalog/sync` | 写 DB | 手动刷新 |
| GET | `/console/navigation/menus` | DB + visibility | 侧边栏 |

`GET /console/me/permissions` 保留为扁平可见资源（按钮/接口显隐），**不是**导航。

### 5.1 VO

**CatalogNodeVo** — 树节点（含 `children`）。由 `ConsoleCatalogService` 从 flat `PermissionResourceEntity` 组装。

**NavigationMenuVo** — 仅 MENU；扩展字段：`ownerPluginId`、`moduleKey`、`pageKey`、`resourceId`。

V1 **不做** `includeDisabled` 查询参数；DISABLED 资源不出现在 catalog tree（grant 历史仍在 DB）。

## 6. sync

### 6.1 算法（已有实现，保持不变）

```text
discover()       ← ConsoleContributionCatalog.activeContributions() 全集
loadExisting()   ← workspace 全部 nx_permission_resource
upsert           ← 新增/更新
disableMissing() ← 不在发现集合中的 ENABLED → DISABLED（grant 不删）
```

### 6.2 谁进入 discover

| 插件状态 | 进入 sync |
|----------|-----------|
| 扫描未安装 | 否 |
| 安装未启用 | 否 |
| ACTIVE | 是 |

### 6.3 触发（V1 仅三处，无事件监听器）

| 时机 | 调用方 |
|------|--------|
| 服务启动 | `ConsoleCatalogSyncStartupTask`（`NexusStartupTask` order=200） |
| `PluginManagementEndpoint` enable/disable 成功 | endpoint 内直接 `syncService.sync()` |
| 手动 | `POST /console/catalog/sync` |

**为何不用 PluginEventBus 监听器：** 启停入口已在 console `PluginManagementEndpoint`，直接调用最简单；
MISSING/重启由启动 Task 兜底。避免 core→console 事件订阅的额外装配。

### 6.4 两层状态

| 层 | 更新 | 谁读 |
|----|------|------|
| 内存 Catalog | 启停即时 | `RequestAuthorizer`（校验 plugin 仍 ACTIVE） |
| DB 索引 | sync 时 | catalog、navigation、grant |

disable 后必须 sync，否则 navigation 短暂过期；由 endpoint 内同步调用保证。

## 7. AuthorizationSubjectResolver

console 定义端口；Spring/Quarkus **应用层**实现（从 token 取 roleIds/orgUnitIds）。
navigation 与 `/me/permissions` 共用。console 不解析 HTTP。

## 8. 删除 / 不做（防过度设计）

| 项 | 决定 |
|----|------|
| `CatalogSyncOnPluginLifecycleListener` | **不做**（V1） |
| `disableByOwnerPluginId` 快速路径 | **不做**（V2 按需） |
| `includeDisabled` catalog 参数 | **不做**（V1） |
| `GET /console/pages/...` UiSpec HTTP | 后续 |
| `BuiltinConsolePluginInstallInitializer` | **删除**；用 `requiredPluginIds` |
| `NavigationMenuEndpoint` 读内存 Catalog | **禁止** |
| kernel `PermissionResourceSyncService` | **删除** |

## 9. 测试要点

1. sync 全量对账：ACTIVE upsert；停用后 disableMissing。
2. disable 后 navigation 不含该 MENU（endpoint 内 sync）。
3. catalog tree / navigation 契约与组装逻辑。
4. sync 不在 kernel。

## 10. 验收标准

1. 启动后 catalog tree 含 6 个内置模块（`requiredPluginIds` + 启动 sync）。
2. 插件 disable 后 navigation 立即更新（endpoint sync）。
3. 重启后 DB 与 ACTIVE 集合一致（启动 sync）。
4. core 无 console 业务；console 无 Spring 依赖。
