# 控制台鉴权、权限目录与资源组

本文是 Java 技能体系对 Nexus 管理控制台鉴权机制的跨技能执行参考。适用于：

- 新增或调整 console domain、module、页面和 endpoint。
- 新增权限、`viewGroups`、`operationGroups`、`@EndpointGrant`。
- 维护 `plugin-declaration.yaml`、`meta-sitemap.yaml` 和 YAML 页面访问配置。
- 排查菜单缺失、页面 403、按钮消失、越权请求和角色授权不生效。

完整现状与图例见
[`console-authorization-implementation.md`](../../../../docs/design/console-authorization-implementation.md)。
代码、运行配置和测试始终是最终事实源；本文不得被当作跨项目通用协议。

## 1. 事实源与职责

| 信息 | 唯一 owner |
|------|------------|
| domain、catalog module、入口页、浏览/操作资源组 | console@2 contribution，当前 platform 使用 `META-INF/innos-resources/plugin-declaration.yaml` |
| endpoint 的动作、稳定名称、授权组、登录/权限跳过、审计 | core `@EndpointModule` / `@EndpointGrant` |
| domain/module/group 的持久化权限目录 | console `nx_console_catalog_resource` |
| 角色/组织单元实际授权 | `nx_permission_grant` |
| 默认导航 | Spring 宿主 `src/main/resources/console/meta-sitemap.yaml` |
| 前端页面路径、布局和页面清单 | console UI `src/backend/pages/yaml/sitemap.yaml` |
| 页面访问模式及页面所属模块 | 页面 YAML 的 `page` |

不要把这些信息复制成第二套事实源：

- `plugin-declaration.yaml` 负责目录与权限组声明，不负责 endpoint 实现。
- `@EndpointGrant` 负责 endpoint 鉴权，不负责插件安装和页面路由。
- `meta-sitemap.yaml` 负责导航入口，不负责页面 DSL 和 API 安全。
- UI `sitemap.yaml` 不再维护 `menus`，只维护 `pages` 与 `layouts`。
- 菜单隐藏和前端组件过滤都不是安全边界；真实 endpoint 鉴权是最终边界。

## 2. 稳定标识与授权数据

### 2.1 稳定授权键

所有 catalog 授权键统一为：

```text
{domainKey}.{moduleKey}:{grantName}
```

| grantName 来源 | 使用场景 | 示例 |
|----------------|----------|------|
| `viewGroups[].group` | 页面访问和导航可见性 | `operation.role:view` |
| `operationGroups[].group` | 当前角色页的分组授权 | `operation.role:create` |
| `@EndpointGrant.name` | 兼容细粒度操作授权 | `operation.role:role.create` |

`domainKey`、`moduleKey`、group 和 endpoint name 都属于稳定标识。发布后不要只因
Java 类、菜单标题或页面标题变化而重命名它们。展示文案通过 `label`、`displayName`
和 i18n 管理，不能参与鉴权。

### 2.2 主体与默认放行规则

- 授权主体为 `ROLE` 和 `ORG_UNIT`，当前主体命中的两类授权取并集。
- 管理员由 `AuthorizationSubject.administrator()` 直接放行。
- 权限消费者兼容 `*`；普通角色授权 API 只接受当前 catalog 中存在的资源键，不能借
  `*` 绕过保存时校验。
- PLATFORM 会话使用平台 ownership；租户会话必须有 workspace，并按对应 ownership 查询。
- `ignoreAuth` 与 `ignorePermission` 是独立开关。`ignorePermission=true` 仍可能要求登录。

## 3. console@2 与权限组声明

`plugin-declaration.yaml` 的 contribution 把页面入口和权限资源声明到 catalog：

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.orders.console
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 订单管理
    en: Order management
  contributions:
    - type: console
      majorVersion: 2
      domainKey: operation
      displayName:
        zh-CN: 运营管理
        en: Operations
      modules:
        - moduleKey: order
          displayName:
            zh-CN: 订单管理
            en: Order management
          description:
            zh-CN: 订单查询与处理。
            en: Order lookup and processing.
          orderIndex: 20
          entryPageKey: platform-order-main
          icon: shopping-bag
          viewGroups:
            - group: view
              label:
                zh-CN: 浏览订单页面
                en: Browse order pages
          operationGroups:
            - group: operate
              label: { zh-CN: 操作, en: Operations }
            - group: create
              label: { zh-CN: 创建, en: Create }
            - group: update
              label: { zh-CN: 更新, en: Update }
            - group: status
              label: { zh-CN: 状态变更, en: Status change }
          children: []
```

字段规则：

- `domainKey` 归组领域；`moduleKey` 是 catalog 模块；两者由小写 kebab-case 组成。
- `entryPageKey` 是最长 128 字符的小写 kebab-case 稳定入口键。UI 页面的
  `page.pageKey` 必须与它一致。
- MODULE 要么有 `entryPageKey` 且无 children，要么没有入口页但有非空 children；
  不能同时声明入口页和子节点。
- 多页面 MODULE 使用 `children` 中的 MENU；每个 MENU 必须有稳定的
  `entryPageKey`，没有 `moduleKey`。
- 省略 `viewGroups`/`operationGroups` 时分别默认为 `view`/`operate`。
- 一旦显式覆盖，`viewGroups` 必须包含 `view`，`operationGroups` 必须包含
  `operate`；分组键同样使用小写 kebab-case。
- `viewGroups` 生成页面访问/导航组键；`operationGroups` 生成角色页可选择的操作组键。
- READ/EXPORT 的自定义 group 应与同一 MODULE 的 `viewGroups` 对齐；其他动作的自定义
  group 应与 `operationGroups` 对齐，否则目录缺项或运行时不匹配。

当前 plugin manifest 是严格单文档 YAML：拒绝未知字段、YAML anchor/alias、自定义
tag、多文档和超过 1 MiB 的输入。`spec.capabilities` 与 `spec.requirements` 已移除；
至少声明一个 `spec.contributions[]`。

plugin 的 `spec.config` 与 console permission 无关。插件配置 schema、来源优先级和
`SecretValue` 见
[`plugin-entry-and-configuration.md`](../../java-develop/references/plugin-entry-and-configuration.md)。

## 4. Endpoint 授权声明

### 4.1 推荐声明位置

| 资源形态 | `@EndpointModule` | `@EndpointGrant` |
|----------|-------------------|------------------|
| console 契约 interface | 当前标准不在 interface 上声明 | 每个 HTTP 方法声明 |
| platform/portal 契约实现类 | 实现类声明一次 | 不重复 interface 方法注解 |
| platform/portal 自有 endpoint | 实现类声明一次 | 每个 HTTP 方法声明 |
| 公共 endpoint | 实现类声明，按需模块级 `ignoreAuth` | 按需方法级 `ignorePermission` |

console 契约实现类声明类型级 `@Path`/`@Produces`/`@Consumes`、
`@NexusAuthenticatedApi` 与 `@EndpointModule`；契约接口保留方法级
JAX-RS/OpenAPI 和 `@EndpointGrant`，实现方法直接覆盖，不重复授权元数据。

`@EndpointModule.domain` 应与 contribution 的 `domainKey` 一致。建议总是显式填写：
省略时宿主只能在 enabled MODULE 中按 `moduleKey` 反查且要求唯一，重复 module key
会令授权失败。

### 4.2 `@EndpointGrant` 字段

| 字段 | 默认值 | 要求 |
|------|--------|------|
| `action` | 必填 | 使用最具体动作，不用一律 `WRITE` |
| `name` | 必填 | 稳定操作名，建议 `resource.operation`；发布后不可漂移 |
| `group` | 空 | READ/EXPORT 解析为 `view`，其余动作解析为 `operate` |
| `label` | 空 | 审计和目录展示使用；审计动作应提供 |
| `recordArgs` | `true` | 密码、令牌、OTP、邀请链接和敏感批量载荷必须设 `false` |
| `auditLog` | `false` | 只对需要审计的管理动作开启 |
| `ignorePermission` | `false` | 与模块级值做 OR；只跳过资源权限 |
| `ignoreAuth` | `false` | 与模块级值做 OR；只跳过登录，不自动跳过权限 |

### 4.3 授权组必须显式对齐

请求侧同时生成“操作项键”和“分组键”，命中任一即可：

```text
{domainKey}.{moduleKey}:{grant.name()}
{domainKey}.{moduleKey}:{grant.group()}
```

当前角色授权面板保存的是分组键，不保存组内每个操作项。因此：

| Action | 未填 `group` | 自定义 `group` 的要求 |
|--------|---------------|-----------------------|
| READ / EXPORT | `view`，由 MODULE `viewGroups` 的页面浏览授权命中 | 与 MODULE `viewGroups` 中声明的键对齐 |
| 其他动作 | `operate` | 与 MODULE `operationGroups` 中声明的键对齐；使用 `create`、`update` 等细分 UI 组时必须显式设置同名 `group` |

目录组装还能按 action 把未显式分组的可变更操作项展示到
`create`/`update`/`delete`/`status` 等已声明组中，但运行时 grant group 仍是
`operate`。只增加 YAML group 而不设置 `@EndpointGrant.group`，会让“目录显示组”和
“请求实际校验组”不一致，角色页勾选细分组后仍可能返回 403。

其他要求：

- 使用 `CatalogOperationGroups.*` 常量，不要散落字符串字面量。
- 新目录测试同时断言 YAML 分组、endpoint group 和最终资源键一致。

### 4.4 跳过策略与公共 API

- `/api/public/**` 与 `/api/d/*/public/**` 是公共路径。带无效 Bearer 时仍失败，不会
  静默降级为匿名。
- 公共且无需权限的 endpoint 才可同时使用 `ignoreAuth=true` 与
  `ignorePermission=true`。
- 需要登录但无需特定资源授权的 endpoint 只设 `ignorePermission=true`。
- 当前权限 Filter 在解析不到 `@EndpointGrant` 时不会自动拒绝。因此新增
  `/api/d/**` endpoint 若遗漏授权元数据，通常仍是“登录后可访问”，不是“默认拒绝”。
  契约测试必须覆盖每个 HTTP 方法，不能把 `@NexusAuthenticatedApi` 当作运行时鉴权。

## 5. Catalog 同步与角色授权保存

### 5.1 目录同步

`ConsoleCatalogSyncService` 从已激活的 console@2 contribution 同步 MODULE/MENU 到
`nx_console_catalog_resource`。同步结果：

- 新资源新增。
- 内容变化则更新。
- 本次已激活 contribution 中不存在的旧资源标记为 `DISABLED`。
- 启动任务和显式同步接口都走同一服务。

权限目录接口为：

```text
GET /api/d/nexus/catalog/permission-catalog
```

该接口要求登录并设置 `ignorePermission=true`，因为角色设置页需要读取资源目录，但
普通已登录用户也必须能安全取得目录模型。旧的 `/tree` 是已废弃兼容入口。

### 5.2 角色授权写入

角色授权采用全量替换：

```text
GET /api/d/platform/roles/{roleId}/permissions
PUT /api/d/platform/roles/{roleId}/permissions
```

请求：

```json
{
  "grants": [
    { "resourceKey": "operation.role:view" },
    { "resourceKey": "operation.role:create" }
  ]
}
```

`constraintDefinition` 是历史 wire 字段；catalog v2 只接受空值，非空值以
`INVALID_PARAMETER` 拒绝。当前 UI 可以省略该字段。

`PermissionGrantService.replace` 在一个事务中：

1. 校验 subject type/ID 与当前 ownership。
2. 校验 resourceKey 非空、不重复、长度不超过 256 且存在于当前 catalog。
3. 删除该主体的旧授权。
4. 按请求写入完整新授权。
5. 任一失败整体回滚。

保存结果不是增量命令，调用方必须发送最终完整集合。空集合表示清空授权。

## 6. 请求侧过滤器顺序

| 顺序 | Filter | 作用 |
|------|--------|------|
| 1 | `ConsoleCorsFilter` | CORS |
| 2 | `ConsoleRequestContextFilter` | 请求开始清理线程身份、requestId；响应后清理 |
| 3 | `ConsoleResolvedEndpointGrantFilter` | 从资源类/契约方法解析 grant |
| 4 | `ConsoleAuthenticationFilter` | 公共路径、`ignoreAuth`、Bearer 登录 |
| 5 | `ConsoleDevSessionFilter` | 仅安全关闭时注入 dev session |
| 6 | `WorkspaceScopeSessionFilter` | 绑定租户/工作区/项目作用域 |
| 7 | `ConsoleResourcePermissionFilter` | 按操作项键或分组键鉴权 |
| 8 | `ConsoleAuditLogFilter` | 按 grant 记录审计 |

`ConsoleRequestContextFilter` 必须在鉴权过滤器之前执行并在请求开始时清理会话：异常路径只执行
「已运行过」过滤器的响应阶段，否则 401/403 会在线程池遗留上一个请求的身份。

新增接口时依次检查：路径是否被 `consolePathPatterns` 覆盖；grant 是否成功解析；
自助或公开路径是否应命中 `ignoreAuth`；是否只应跳过权限；最终 grant key 是否和
角色授权一致。

## 7. meta-sitemap 与导航可见性

Spring 宿主导航来自两处并追加合并：

1. `nx_menu` 等持久化菜单，按 ownership 和主体权限过滤。
2. classpath `console/meta-sitemap.yaml` 默认菜单，按 catalog 浏览组、`roles` 和
   `hidden` 过滤。

导航接口要求登录但忽略资源权限：

```text
GET /api/d/platform/navigation/menus
```

菜单项关键字段：

```yaml
id: nav-order
type: page
pageId: platform-order-list
title: 订单管理
icon: shopping-bag
layout: admin
domainKey: operation
moduleKey: order
viewGroup: view
roles: []
hidden: false
```

- `pageId` 必须对应 UI `sitemap.yaml` 中的页面 ID。
- `domainKey/moduleKey` 必须对应 catalog MODULE；`viewGroup` 缺省为 `view`。
- `roles` 使用 roleId，不是 role code。
- 管理员和 `*` 可绕过浏览组过滤；普通用户必须持有对应浏览组键。
- group 子节点全部不可见时，整个 group 移除。
- 当前加载逻辑在找不到已声明的 viewGrantKey 时会清空菜单上的 catalog 绑定；无绑定
  节点再由可见性过滤器处理时只受 `roles`/`hidden` 约束。因此保护型菜单必须配置完整
  binding，并用测试确认 binding 存在于 catalog。页面访问检查和 API 鉴权仍提供后层
  保护，但这会导致错误入口可见。

Next.js sitemap 流程：

1. 已登录时调用后端 navigation endpoint 获取当前用户菜单。
2. 读取本地 UI `sitemap.yaml` 的 `pages` 和 `layouts`。
3. 将后端菜单、本地页面和隐藏路由拼成 Pactor sitemap。
4. 菜单标题依次取 `common.menu.{menuId}.title`、page 标题，最后回退 YAML `title`。

不要在 UI `sitemap.yaml` 再维护 `menus`。菜单权限也不会替代页面和 API 鉴权。

## 8. YAML 页面访问与 DSL 操作过滤

页面声明示例：

```yaml
page:
  id: platform-order-list
  access-mode: grant
  domain: operation
  module: order
  pageKey: platform-order-main
  grantGroup: view
```

要求：

- `access-mode` 必填，只能是 `public`、`authenticated`、`grant`。
- UI sitemap 中对应页面必须 `lazy: true`。
- `grant` 必须有 `domain`、`module`、`pageKey`；`pageKey` 匹配 catalog
  `entryPageKey`，`grantGroup` 默认 `view`，并与 `viewGroups` 对齐。
- `group` 仅是 `grantGroup` 的兼容别名；两者同时存在时当前实现优先 `group`。

浏览器只调用 Next.js：

```text
GET /api/public/pages/{id}
```

Next.js 服务端读取页面 DSL、递归收集请求。`public` 页直接加载并跳过 page-access；
`authenticated` 和 `grant` 页调用一次：

```text
POST /api/public/page-access/verify
```

`authenticated` 只校验会话有效性，不要求 `domain`/`module`/`pageKey`，也不做页面与
操作 grant 判定（返回 `ignoreGrantGroup=true`，服务端仅移除 `access` 声明）。
`grant` 才执行页面授权组校验与逐请求的 grant 过滤。

返回 `allowed/reason/requestPermissions/grantGroups`，不返回或授予业务 API 权限。
浏览器不要再直接调用 page-access。

操作过滤覆盖：

- 任一组件/事件中的 `action: request`。
- `action: call` 引用的命名 action 内部请求。
- overlay/modal 中的嵌套请求。
- 自定义组件/action 显式声明的 `access.requests[]`。

上述过滤只对 `grant` 页生效；`authenticated` 页返回 `ignoreGrantGroup=true`，
服务端剥离 `access` 声明后直接下发，不做逐请求判定。

无权限时的默认表现：

- Button 移除。
- Form 禁用字段和提交按钮。
- 普通组件移除；只有 `access.mode: disable` 的自定义组件保留为禁用。
- `when` 继续使用注入状态：
  `state.grantGroupList`、`state.grantGroups`、`state.grantGroupsAll`。
- `access` 声明在返回浏览器前移除。

动态 URL 只允许完整路径段表达式，如 `${row.id}`、`${params.id}`。后端按注册的
JAX-RS 路径模板匹配；未知、歧义、非法 URL 或超过 256 个请求均失败关闭。

## 9. 新增模块、权限或资源组

### 9.1 新增 Maven 模块

先按 [`java-project`](../../java-project/SKILL.md) 与根 `AGENTS.md` 判定是否真的需要
新 Maven 模块。新建 Java 包或新业务域默认仍放现有 portal/platform 模块。

若新 Maven 模块暴露管理台能力，还必须完成：

- console@2 contribution；
- endpoint 的 module/domain/grant；
- page access 和导航绑定；
- catalog、grant、navigation 与 page 集成测试。

### 9.2 新增 catalog MODULE

1. 在 console@2 contribution 增加唯一 `moduleKey` 和稳定 `entryPageKey`。
2. 声明 `viewGroups`，至少含 `view`；声明 `operationGroups`，至少含 `operate`。
3. endpoint 使用相同 `domain`/`moduleKey`，每个 HTTP 方法声明 `@EndpointGrant`。
4. 对子页面增加 MENU 或复用 MODULE 入口页；保证每个 UI `pageKey` 可定位。
5. 页面 YAML 设置 `access-mode`、domain、module、pageKey、grantGroup。
6. 在 `meta-sitemap.yaml` 为侧栏入口绑定同一 catalog module 和 viewGroup。
7. 启动或调用 catalog sync，确认目录未重复、未禁用。
8. 用角色页选择分组并验证普通用户、管理员和直接 API 调用。

### 9.3 新增或调整权限组

- 新增 `viewGroups` 时同步页面 access mode、meta-sitemap 和导航测试。
- 新增 `operationGroups` 时同步 endpoint `group`、角色授权测试和 action-access 测试。
- 不要删除或重命名已持久化的 group、endpoint name。需要调整时先新增新键，授权和
  调用方迁移后再移除旧键。
- 删除 catalog 资源会让同步任务禁用旧索引，但历史授权行不应被当成新资源的自动迁移。

## 10. 排查顺序

| 现象 | 首要检查 |
|------|----------|
| 菜单不显示 | 插件是否激活；catalog 是否同步；`hidden`、roleId、view group 是否匹配；后端 navigation 是否 502/401 |
| 菜单对所有登录用户都显示 | 菜单是否缺少或写错 `domainKey/moduleKey/viewGroup`；catalog binding 是否被加载器清空 |
| 页面返回 403 | 页面 `access-mode` 是否合法；domain/module/pageKey 是否匹配；grant group 是否存在并已授权；服务是否可用 |
| 角色页资源目录为空 | `/api/d/nexus/catalog/permission-catalog` 是否 200；插件是否激活；catalog 是否同步 |
| 勾选分组后请求仍 403 | endpoint `@EndpointGrant.group` 是否与 YAML operation group 相同；ownership/workspace 是否正确 |
| 按钮或表单操作消失 | YAML 请求是否可被 `action-access.ts` 收集；method/path 是否唯一命中；动态路径是否合法；角色是否持有 group |
| 未授权用户仍能直接调用接口 | endpoint 是否遗漏 `@EndpointModule`/`@EndpointGrant`；路径是否被安全配置绕过；ignore 标志是否过宽 |
| UI 提示成功但数据未保存 | 不要把提示单独当成功后端证据；检查实际 HTTP 状态、响应信封和事务结果。当前通用 action 链不因前序 action 返回失败而自动短路 |

认证与授权要分开判断：

- 401：没有有效会话、Bearer 无效、公共路径携带了无效 token。
- 403：会话有效，但页面 grant、资源权限或 ownership/workspace 判定失败。

## 11. 验证矩阵

| 层 | 至少验证 |
|----|----------|
| plugin manifest | strict YAML、必填字段、唯一 key、默认组、自定义组包含 `view`/`operate` |
| catalog | MODULE/MENU 同步、缺失资源禁用、权限目录资源键稳定 |
| endpoint | 每个 HTTP 方法有 grant；module/domain/action/name/group 正确；实现类不重复契约 grant |
| 角色授权 | 未知/重复 key 拒绝、ownership 隔离、全量替换事务回滚、空集合清空 |
| 请求鉴权 | READ/写入/公共/仅登录/未知/动态路径；401 与 403；直接 API 调用 |
| 导航 | 持久化菜单与 meta-sitemap 合并、catalog view group、roleId、hidden、空 group、标题本地化 |
| 页面 | public 不调用 page-access；authenticated/grant 调用一次；无效页面配置拒绝 |
| DSL 操作 | action request/call、嵌套 overlay、Form/Button、多请求、`access.mode`、`when`、服务失败关闭 |
| 端到端 | 管理员与至少一个普通角色分别验证导航、页面、按钮/表单和直接 API |

常用测试入口：

- `EndpointGrantManagerTest`
- `ConsoleCatalogServiceTest`、`ConsoleCatalogSyncServiceTest`
- `PermissionGrantServiceTest`
- `ConsoleResourcePermissionFilterTest`、`ConsoleResourcePermissionAuthorizerTest`
- `PlatformPageAccessServiceTest`、`PlatformActionAccessServiceTest`
- `NavigationMenuComposerTest`、`MetaSitemapMenuFilterTest`
- UI `action-access.test.ts`、`page-access.test.ts`

## 12. 当前代码锚点

排查时先定位 owner，再从 owner 向数据与调用方追踪：

| 关注点 | 当前实现 |
|--------|----------|
| console@2 分组默认值与校验 | `innospots-nexus-plugin/.../ConsoleContributeNode.java`、`ConsolePermissionDefaults.java` |
| grant 注解解析与路径模板 | `innospots-nexus-core/.../EndpointGrantAnnotations.java`、`EndpointGrantHttpIntrospection.java` |
| grant 默认 runtime group | `innospots-nexus-core/.../ResolvedEndpointGrant.java` |
| 操作键与分组键判定 | `innospots-nexus-console/.../ConsoleResourcePermissionFilter.java`、`ConsoleResourcePermissionAuthorizer.java` |
| catalog 组装与同步 | `CatalogPermissionCatalogAssembler.java`、`ConsoleCatalogSyncService.java` |
| 角色授权全量替换 | `PermissionGrantService.java` |
| meta-sitemap 绑定与裁剪 | `SitemapMenuGrantBinding.java`、`MetaSitemapMenuFilter.java`、`NavigationMenuComposer.java` |
| Next.js 页面/操作检查 | `src/backend/pages/page-access.ts`、`sitemap.ts`、`action-access.ts` |
| 当前平台声明示例 | `innospots-nexus-platform/src/main/resources/META-INF/innos-resources/plugin-declaration.yaml` |
| 当前导航示例 | `innospots-nexus-sample/.../src/main/resources/console/meta-sitemap.yaml` |
