# 目录索引与权限

## 目录（`catalog`）

| 类型 | 说明 |
|------|------|
| `ConsoleCatalogSyncService` | 将活动 `console@2` MODULE/MENU 同步到 `nx_console_catalog_resource` |
| `ConsoleCatalogService` | 组装权限设置 MODULE → MENU / 嵌套 MODULE 树 |
| `CatalogPermissionCatalogAssembler` | 将叶子 MODULE/MENU 与 endpoint grant 操作组合并 |
| `ConsoleCatalogSyncStartupTask` | 宿主启动同步钩子 |
| `ConsoleCatalogResourceEntity` | 持久化目录节点 |

同步规则：

- `ConsoleContributeNode` 的 MODULE、MENU 稳定资源键分别写入 catalog。
- 叶子 MODULE 保存 `viewGroups` / `operationGroups`；MENU 继承所属 MODULE 的分组。
- `GET /api/d/nexus/catalog/tree` 返回 MODULE / MENU 树，不再生成 PAGE、ACTION 或
  DATASOURCE 节点。
- 页面访问项 `grantName` 使用 MENU 的 `entryPageKey`；叶子 MODULE 使用自身
  `entryPageKey`。
- 操作项来自 `EndpointGrantManager.moduleCatalog(moduleKey)`；`READ` / `EXPORT`
  进入只读项，其余动作进入 `operable`。

## 权限（`permission`）

| 类型 | 说明 |
|------|------|
| `PermissionGrantService` | 角色/组织单元授权全量替换 |
| `PermissionVisibilityService` | 当前用户可见资源 |
| `ConsolePagePermissionAuthorizer` | 页面/资源授权判定 |
| `AuthorizationSubjectResolver` | 解析当前授权主体 |
| `AuthorizationRequest` / `AuthorizationDecision` | 判定入参与结果 |

包级参考：`catalog-*.md`、`permission-*.md`。
