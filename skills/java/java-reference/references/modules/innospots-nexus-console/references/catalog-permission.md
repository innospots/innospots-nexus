# 目录索引与权限

## 目录（`catalog`）

| 类型 | 说明 |
|------|------|
| `ConsoleCatalogSyncService` | ACTIVE 贡献 + Page DSL → `nx_console_catalog_resource` |
| `ConsoleCatalogService` | 目录树读取（权限配置 UI） |
| `ConsoleCatalogSyncStartupTask` | 启动时同步钩子 |
| `ConsoleCatalogResourceEntity` | 持久化目录节点 |

同步时按模块贡献中的 **全部 `pageKey`** 加载 YAML，并根据 **`page.parentPageKey`** 写入 `parent_resource_id`。页面清单来源见 [entry.md](entry.md) 与 [console-entry-and-pages.md](../../../console-entry-and-pages.md)。

### 权限树 API 形状

`GET /api/d/nexus/catalog/tree` 返回 **MODULE → 一级 PAGE → 嵌套子 PAGE**；**不含** MENU / ACTION / DATASOURCE 节点（与 `console@1` 菜单树不同）。

## 权限（`permission`）

| 类型 | 说明 |
|------|------|
| `PermissionGrantService` | 角色/组织单元授权全量替换 |
| `PermissionVisibilityService` | 当前用户可见资源 |
| `ConsolePagePermissionAuthorizer` | PAGE + DATASOURCE 授权判定 |
| `AuthorizationSubjectResolver` | 解析当前授权主体 |
| `AuthorizationRequest` / `AuthorizationDecision` | 判定入参与结果 |

包级参考：`catalog-*.md`、`permission-*.md`。
