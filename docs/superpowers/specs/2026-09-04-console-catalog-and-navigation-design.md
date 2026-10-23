# Console Catalog 与 Navigation 设计

## 1. 文档定位

本文定义 `innospots-nexus-console` 当前的控制台目录与导航投影，以及它们与
`console@2` 插件贡献、`EndpointGrant` 和权限授权之间的关系。

事实源固定为：

```text
ACTIVE 插件的 console@2 Contribution
  -> ConsoleContributionCatalog（内存活动快照）
  -> ConsoleCatalogSyncService（全量对账）
  -> nx_console_catalog_resource（宿主级持久化目录）
  -> permission-catalog / navigation / page-access 三个读侧投影
```

`nx_menu` 不是插件目录事实源，`entryPageKey` 也不指向任何宿主页面运行时文件。

## 2. 当前模型

### 2.1 Catalog 资源

目录只持久化两类资源：

| 类型 | resourceKey | 说明 |
|------|-------------|------|
| `MODULE` | `module:{moduleKey}` | 顶层或嵌套管理模块；叶子 MODULE 可携带 `entryPageKey` |
| `MENU` | `menu:{moduleKey}.{entryPageKey}` | 叶子 MODULE 下的菜单入口 |

插件贡献根节点不落 catalog。MODULE 必须有 `moduleKey`，并满足以下二选一：

- 有 `entryPageKey` 且 `children` 为空；
- 无 `entryPageKey` 且 `children` 非空。

MENU 没有 `moduleKey`，必须有 `entryPageKey`，且 `children` 为空。

`entryPageKey` 是稳定、不透明的小写 kebab-case 身份，推荐格式为
`{domainKey}-{moduleKey}-{pageSuffix}`。运行时不得通过拆分字符串推导领域或模块。

### 2.2 权限目录

`GET /api/d/nexus/catalog/permission-catalog` 从持久化 catalog 组装权限配置树：

```text
MODULE
  ├─ viewItems[]        ← 叶子 MODULE / MENU 的 entryPageKey
  ├─ operationGroups[]  ← EndpointGrantManager.moduleCatalog(moduleKey)
  └─ children[]         ← 嵌套 MODULE 或 MENU
```

授权键统一为：

```text
grantName:domainKey.moduleKey
```

- 页面访问项的 `grantName` 是叶子 MODULE/MENU 的 `entryPageKey`；
- 操作项的 `grantName` 是 `@EndpointGrant.name`；
- 当前 `@EndpointGrant.group` 尚未参与目录归组，操作项进入 `default` 分组；
- 当前页面访问校验只实现 `grantGroup=default`，其他分组失败关闭。

`GET /api/d/nexus/catalog/tree` 已废弃，仅作为 `permission-catalog` 的兼容别名。

### 2.3 Navigation

`GET /api/d/nexus/navigation/menus` 从启用 catalog 中读取 MENU 与叶子 MODULE，
按当前主体的页面授权裁剪后返回侧栏导航。导航与权限目录是同一索引的两个投影，
不维护第三套菜单表。

## 3. 同步

`ConsoleCatalogSyncService.sync()` 只读取 `ConsoleContributionCatalog` 中已提交且
ACTIVE 的 `console@2` 贡献，并按宿主目录做全量对账：

```text
discover active contributions
  -> upsert MODULE / MENU
  -> mark missing resources DISABLED
  -> preserve nx_permission_grant history
```

触发点：

| 时机 | 调用方 |
|------|--------|
| 服务启动 | `ConsoleCatalogSyncStartupTask`（order=200） |
| 插件安装、启用或停用成功后 | `PluginManagementEndpoint` |
| 管理员显式刷新 | `POST /api/d/nexus/catalog/sync` |

停用插件不会删除历史授权记录；对应 catalog 资源变为 DISABLED，重新启用后可继续使用
相同稳定键。

## 4. EndpointGrant

`@EndpointModule` 声明 REST 契约归属的 `moduleKey`，`@EndpointGrant` 声明每个 HTTP
方法的授权、审计与跳过策略。console 契约接口是注解单一事实源；portal/platform
实现类不得重复声明。

最小示例：

```java
@EndpointModule("dictionary")
@Path("/api/d/nexus/dictionary-types")
public interface DictionaryTypeEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "dictionary.type.page")
    R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(
            @BeanParam DictionaryTypePageRequest request);
}
```

`EndpointGrantManager` 在启动时登记资源类，拼接类级与方法级 `@Path`，形成模块级
只读/可操作目录。解析优先级和完整字段语义见
`skills/java/java-develop/references/endpoint-grants.md`。

## 5. 页面访问校验

`POST /api/public/page-access` 接收：

```json
{
  "domainKey": "platform",
  "moduleKey": "user",
  "pageKey": "platform-user-main",
  "grantGroup": "default"
}
```

`GrantGroupPageAccessVerifier` 先按 `entryPageKey` 查找唯一启用的叶子 MODULE/MENU，
再以 `entryPageKey:domainKey.moduleKey` 校验角色或组织单元授权。管理员直接通过；
未找到资源、未授权或非 `default` 分组均拒绝。

## 6. 模块职责

| 模块 | 职责 |
|------|------|
| `innospots-nexus-plugin` | `console@2` 声明、解码、校验、Contribution Handler 与活动快照 |
| `innospots-nexus-console` | catalog 持久化索引、权限目录、页面访问校验、`EndpointGrant` 运行时 |
| `innospots-nexus-platform` | 平台侧导航 REST 实现；不维护第二份 catalog |
| `innospots-nexus-portal` | 租户业务域；不维护插件目录同步 |

Spring/Quarkus 适配层只负责注册 DAO、Service、Handler 和 Endpoint Bean，不定义目录语义。

## 7. 已移除

- console 页面运行时包、页面解析/渲染端点和测试页面资源；
- 旧页面规格、页面 URL 权限模型和 PAGE/ACTION/DATASOURCE 资源类型；
- Capability 路由、Provider 注册表和跨插件 Capability 依赖；
- `console@1` 页面树与旧 Extension 运行时。

## 8. 验收

1. ACTIVE 插件贡献可同步为 MODULE/MENU，停用后标记 DISABLED。
2. `permission-catalog` 同时包含页面访问项和 `EndpointGrant` 操作项。
3. 页面访问授权键与 `entryPageKey` 稳定对应。
4. 非 `default` 页面授权分组明确拒绝。
5. console、plugin、platform、portal 均不依赖已删除的页面运行时。
