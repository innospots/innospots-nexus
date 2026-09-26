# 目录索引与权限

## 目录（`catalog`）

| 类型 | 说明 |
|------|------|
| `ConsoleCatalogSyncService` | ACTIVE 贡献 → `nx_console_catalog_resource` |
| `ConsoleCatalogService` | 目录树读取 |
| `ConsoleCatalogSyncStartupTask` | 启动时同步钩子 |
| `ConsoleCatalogResourceEntity` | 持久化目录节点 |

## 权限（`permission`）

| 类型 | 说明 |
|------|------|
| `PermissionGrantService` | 角色/组织单元授权全量替换 |
| `PermissionVisibilityService` | 当前用户可见资源 |
| `ConsolePagePermissionAuthorizer` | PAGE + DATASOURCE 授权判定 |
| `AuthorizationSubjectResolver` | 解析当前授权主体 |
| `AuthorizationRequest` / `AuthorizationDecision` | 判定入参与结果 |

包级参考：`catalog-*.md`、`permission-*.md`。
