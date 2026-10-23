# Plugin Extensions v2 与 Catalog 权限资源模型设计

## 1. 文档定位

- **状态**：已接受；**部分已落地**（见 [README.md](README.md) 实现状态表）
- **日期**：2026-10-20（持续修订）
- **归属模块**：`innospots-nexus-plugin`、`innospots-nexus-console`；适配层 `innospots-nexus-platform`、`innospots-nexus-portal`（entry 声明）；装配 `innospots-nexus-spring-console`
- **关联文档**：
  - [README.md](README.md) — 设计目录索引
  - [plugin-runtime-design.md](plugin-runtime-design.md) — 运行时（无 Capability）
  - [plugin-dsl-spec.md](plugin-dsl-spec.md) — YAML 当前形状与目标 `apiVersion: 2`
  - [plugin-installation-design.md](plugin-installation-design.md) — 安装与对账
  - [2026-09-04-console-catalog-and-navigation-design.md](../../../../docs/superpowers/specs/2026-09-04-console-catalog-and-navigation-design.md)（Catalog/Navigation 投影原则仍有效，**资源类型与 sync 输入**按本文修订）
- **规范层级**：与 [plugin-runtime-design.md](plugin-runtime-design.md)、[plugin-dsl-spec.md](plugin-dsl-spec.md) 冲突时，**字段与 catalog/权限**以本文为准；**发现与生命周期**以 runtime 文档为准。

---

## 2. 背景与目标

### 2.1 问题

1. **Plugin 运行时过度分层**：`capabilities` 与 `contributions` 命名与职责重叠；`CapabilityType`、路由（`Tags`/`CapabilityRouter`）、依赖解析在业务侧几乎无消费者，却占用认知与代码量。
2. **跨模块协作**：期望以 **`functionName` + 统一上下文** 同步调用，而非 Java 接口 + 多 Provider 路由；结果用 **`CallResult`**；可选事件通知。
3. **Console 声明过重**：旧模型并列维护 `pages` / `menuTree`；v2 统一为 `modules[]` +
  递归 `children[]`，插件只声明目录与稳定入口键。
4. **权限 Catalog 展示**：当前 `MODULE → PAGE` 树与菜单/页面展示名不一致；权限应基于 **模块 + 访问分组 + 操作分组/操作项**，操作来自 **`@EndpointModule` / `@EndpointGrant`**，分组元数据由 **plugin contribute** 提供。

### 2.2 成功标准

| 标准 | 说明 |
|------|------|
| 心智模型单一 | 插件作者只记 `extensions[]` 三种 `kind` |
| Plugin 只声明目录入口 | contribute **`modules` + 递归 `children`**；catalog sync 不读取任何页面运行时资源 |
| 权限树清晰 | 模块下：**viewItems 可勾选**；**operationGroups（`CatalogPermissionGroup`）+ 组内项可勾选**；无 PAGE 子树 |
| 可彻底删 v1 能力线 | 移除 Capability 路由/依赖及相关 manifest 字段（**breaking**，`apiVersion: 2`） |

---

## 3. 不在本方案中的能力

| 排除项 | 理由 |
|--------|------|
| `provide` / `subscribe` 进入角色权限 UI | 跨模块函数与 HTTP 授权命名空间分离 |
| 恢复 `nx_menu` 业务表或第三套菜单存储 | 仍用 catalog 索引 + navigation 投影 |
| Portal 租户 IAM 域模型变更 | 仅 grant 目标 `resourceKey` 与 catalog 资源类型变化 |
| 动态插件 ClassLoader 热加载 | 仍 classpath / 既有安装模型 |
| 长期双轨 v1 Capability API | 无调用方则 **不**保留兼容层；一次性迁移 |

---

## 4. 归属与词汇（四步法 ①②）

| 概念 | 英文名 | 归属模块/包 | 稳定键 | 备注 |
|------|--------|-------------|--------|------|
| 插件扩展条目 | `PluginExtension` | `core.plugin.declaration` | — | `kind` 判别 |
| 提供函数 | `provide` / `functionName` | `core.plugin.function` | **全局唯一** `functionName` | 与 HTTP grant `name` 无关 |
| 订阅 | `subscribe` | `core.plugin.event` | `on` 模式串 | 匹配 function 或 `event:` |
| 调用上下文 | `PluginCallContext` | `core.plugin.function` | — | 不可变 record |
| 调用结果 | `PluginCallResult` | `core.plugin.function` | — | 含可选 `event` |
| Console 贡献节点 | `ConsoleContributeNode` | `core.plugin.extension.console` | 见 §6.1.2 | MODULE/MENU 递归 `children`；扩展根用 **`modules[]`** |
| 页面访问分组 | `grantGroup` | `console.permission.pageaccess` | `viewGroups[].group` | 请求缺省 `default`；与 catalog 访问分组对齐 |
| 节点类型 | `MODULE` / `MENU` | contribute / catalog | — | **由 `entryPageKey` 与 `children` 推导**，无显式 type 字段 |
| 操作分组 | `operationGroup` | **MODULE 节点** `operationGroups[]` | `group` key | label 为 I18n；须含 `default` |
| 访问分组元数据 | `viewGroups` | **MODULE 节点** | `group` key | 访问分组标题（I18n）；须含 `default`；**MENU 不声明** |
| 目录资源类型 | `CatalogResourceType` | `console.catalog` | — | **仅** `MODULE`、`MENU`（§6.6） |
| 授权项 | `CatalogViewItemVo` | `console.catalog` | `grantName` | 访问与操作共用；见 §6.5 |
| 授权稳定键 | — | `console.permission` | `{grantName}:{domainKey}.{moduleKey}` | 角色 grant 与 `CatalogViewItemVo.resourceKey` 一致 |
| 权限目录树节点 | `CatalogNodeVo` | `console.catalog` | `resourceKey` | `children` 仅 **MODULE / MENU** |
| 模块授权面 | `CatalogModuleGrantVo` | `console.catalog` | — | `viewItems` + `operationGroups`（`CatalogPermissionGroup`） |
| 权限分组 | `CatalogPermissionGroup` | `console.catalog` | `group` | 操作侧分组；**访问侧**分组仅 contribute 元数据，响应不套一层 |
| 展示名 | `I18nObject` | `base.i18n` | — | **所有** VO 的 `displayName` 均为 `I18nObject`，API 不预解析 locale |
| HTTP 模块归属 | `@EndpointModule` | `console.endpoint.grant` | `moduleKey` | 已有 |
| HTTP 操作 | `@EndpointGrant` | `console.endpoint.grant` | `name` | 仅新增可选 **`group`**（语义由 `action` 判别，见 §6.3） |

**表名**：沿用 `nx_console_catalog_resource`（实体 `ConsoleCatalogResourceEntity`）；列与 **console@2 `ConsoleContributeNode`** 对齐，见 **§6.6.1**；**不新建表**（v2 迁移为 **列增删**，非第二张 catalog 表）。

---

## 5. 边界与包结构（四步法 ③）

### 5.1 `innospots-nexus-plugin`

**当前代码（2026-10）**：

```text
com.innospots.nexus.core.plugin
├── declaration/          # PluginDefinition、Tags/ProviderRef、PluginManifest（v1 文档头）
├── discovery/            # SPI + plugin.yaml → PluginDefinition
├── contribution/         # 泛型 Decoder/Handler/Snapshotter；console@2 在 contribution.console
├── contribution/console/ # ConsoleContributeNode、Decoder、Handler、Catalog
├── lifecycle/            # ManagedPlugin：配置 + Contribution 事务 + Plugin 生命周期
├── runtime/              # PluginManager（无 Capability 查询）
├── event/                # PluginEventBus
├── config/               # ConfigurationManager、PluginConfig
└── installation/         # 安装表、快照（capabilities 摘要恒为空列表）
```

**已删除（不再文档化）**：`capability/`、`dependency/`、`CapabilityProvider*`、`CapabilityContribution`。

**目标包（未完全落地）**：

```text
├── function/             # FunctionRegistry、invoke、PluginCallContext/Result（规划）
├── extension/console/    # 可选：将 console 从 contribution 内聚为 extension facade
```

Manifest **`apiVersion: 2` + `extensions[]`** 见 §6.1；编译器仍接受 `nexus.plugin/v1` + `spec.contributions` 直至迁移完成（见 [plugin-dsl-spec.md](plugin-dsl-spec.md)）。

### 5.2 `innospots-nexus-console`

```text
console.catalog          # Sync：仅 MODULE、MENU 落库；grants 由组装器在读取时派生
console.navigation       # 仍读 MENU + visibility；路由用 entryPageKey
console.endpoint.grant   # @EndpointGrant(+group)；Manager 按 action 归类输出
console.permission       # grant 存取以 resourceKey 为对外稳定键
```

**禁止**：plugin 模块依赖 portal/platform；catalog sync 只消费 `console@2` 与
`EndpointGrantManager`，不读取页面运行时资源。

### 5.3 数据流（一层索引，两个投影）

```text
PluginDefinition.extensions (contribute.console@2)
        │
        ├──────────────────────────────────────┐
        ▼                                      ▼
ConsoleCatalogSyncService              NavigationMenuAssembler
  MODULE / MENU（DB）                      MENU 树（+ 权限裁剪）

CatalogPermissionCatalogAssembler ← DB (MENU entry_page_key) + EndpointGrantManager
        │
        ▼
nx_console_catalog_resource
        │
        ▼
GET /api/d/nexus/catalog/permission-catalog  → 权限设置 UI（模块 + 菜单结构 + 授权面）

PluginDefinition.extensions (provide/subscribe)
        │
        ▼
FunctionRegistry + PluginEventBus  （不进 permission-catalog）
```

---

## 6. API 与契约（四步法 ④）

### 6.1 Plugin 协议：`apiVersion: 2`

#### 6.1.1 顶层 manifest（YAML）

```yaml
apiVersion: 2
metadata:
  pluginId: com.innospots.nexus.platform.console
  version: "1.0.0"
  displayName: { "zh-CN": "平台控制台" }
spec:
  config: []   # 结构同 v1，可选
  extensions:
    - kind: provide
      functions:
        - name: platform.tenant.notify
          description: { "zh-CN": "租户变更通知" }
    - kind: subscribe
      on: platform.tenant.notify
    - kind: contribute
      type: console
      majorVersion: 2
      domainKey: nexus
      displayName: { "zh-CN": "平台管理" }
      description: { "zh-CN": "..." }
      orderIndex: 0
      entryPageKey: null
      modules: [ ... ]              # 顶层 MODULE 列表，元素见 §6.1.2（其下仍用 children）
```

Java `Plugin#definition()` 使用同名 `extensions` 列表；编译后与 YAML **同一 `PluginDefinition` 形状**。

#### 6.1.2 `kind: contribute` / `console@2`（递归 `ConsoleContributeNode`）

**扩展根**（`console@2` 容器，**不落 catalog 行**）与 **MODULE/MENU 节点**字段不同：

| 层级 | 子节点字段 | 说明 |
|------|------------|------|
| **扩展根** | **`modules[]`** | 仅顶层；元素为 **MODULE** 节点（见下表）；**无** `moduleKey` |
| **MODULE / MENU** | **`children[]`** | 递归；MENU 叶 `children` 必须为 `[]` |

**不再使用** console@1 的扁平 `modules[]`+`menus[]`、`pages[]`、`menuTree` 等并列数组；v2 的 **`modules` 仅指扩展根下 MODULE 列表**。

**扩展根字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `domainKey` | string | 整棵树的 Page 路由领域 |
| `displayName` / `description` | I18n | 可选展示 |
| `orderIndex` | int | 扩展间排序（若宿主展示多扩展） |
| `entryPageKey` | null | 根必须为「空入口 + 非空 `modules`」 |
| `modules` | `ConsoleContributeNode[]` | 顶层 MODULE，**至少一条** |

**MODULE / MENU 节点公共字段**（`modules[]` 元素及其 `children` 递归同构）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `displayName` | I18n map | 必填 |
| `description` | I18n map | 可选 |
| `orderIndex` | int | 同级排序 |
| `entryPageKey` | string \| null | 稳定入口键；**空**表示非菜单叶 |
| `children` | `ConsoleContributeNode[]` | 子节点；**MENU 必须为 `[]`** |

**仅 `CatalogResourceType.MODULE` 节点额外字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `moduleKey` | string | 必填；与 `@EndpointModule` 一致；插件内唯一 |
| `viewGroups` | array | **访问分组**元数据：`group` + `label`（I18n）；须含 **`default`** |
| `operationGroups` | array | **操作分组**元数据：`group` + `label`（I18n）；须含 **`default`** |

**MENU 叶可选**：`icon`（string）。访问授权名 **`grantName`** 在 catalog 组装时
**恒等于 `entryPageKey`**（见 §6.6.2）；页面访问分组由请求中的 `grantGroup` 对齐
MODULE **`viewGroups[].group`**，**不在 MENU contribute 重复声明**。

**类型判定**（无 YAML `type` 字段；编译/sync 时推导，并写入 catalog / `CatalogNodeVo.resourceType`）：

| 条件 | 推导类型 |
|------|----------|
| `entryPageKey` **为空**（null / 缺省 / 空白）且 `children` **非空** | **`MODULE`** |
| `entryPageKey` **非空**且 `children` **为空** | **`MENU`** |
| 其余组合 | **非法**，编译或校验失败 |

**扩展根节点**：**不写入** catalog；须满足 **`entryPageKey` 为空**且 **`modules` 非空**；**禁止**根上出现 `moduleKey` 或 `children`（子树一律从 `modules[]` 起）。

```yaml
- kind: contribute
  type: console
  majorVersion: 2
  domainKey: nexus
  displayName: { "zh-CN": "平台管理" }
  description: { "zh-CN": "运营管理平台" }
  orderIndex: 0
  entryPageKey: null
  modules:
    - moduleKey: organization
      displayName: { "zh-CN": "组织" }
      description: { "zh-CN": "租户与组织档案" }
      orderIndex: 10
      entryPageKey: null
      viewGroups:
        - group: default
          label: { "zh-CN": "页面访问" }
      operationGroups:
        - group: default
          label: { "zh-CN": "操作" }
        - group: tenant-lifecycle
          label: { "zh-CN": "租户生命周期" }
      children:
        - displayName: { "zh-CN": "组织管理" }
          description: { "zh-CN": "组织入口" }
          orderIndex: 0
          entryPageKey: platform-organization-main
          icon: apartment
          children: []
    - moduleKey: user
      displayName: { "zh-CN": "平台用户" }
      description: { "zh-CN": "用户与邀请" }
      orderIndex: 20
      entryPageKey: null
      viewGroups:
        - group: default
          label: { "zh-CN": "页面访问" }
      operationGroups:
        - group: default
          label: { "zh-CN": "操作" }
      children:
        - displayName: { "zh-CN": "用户列表" }
          orderIndex: 0
          entryPageKey: platform-user-main
          children: []
        - displayName: { "zh-CN": "邀请" }
          orderIndex: 1
          entryPageKey: platform-user-invite
          children: []
```

**`resourceKey` / 路由（`domainKey` 来自扩展根）**：

| 推导类型 | catalog `resourceKey` | 页面路由（不落库） |
|----------|----------------------|-------------------|
| MODULE | `module:{moduleKey}` | — |
| MENU | `menu:{moduleKey}.{entryPageKey}` | 读取时派生 `/page/{domainKey}/{moduleKey}/{entryPageKey}`（navigation 等） |

**permission-catalog `grants`（Q2）**：**每个 MODULE 节点**各自一份 `CatalogModuleGrantVo`，**仅聚合以该 MODULE 为直接父级的 MENU 叶**（`parent_resource_id` → 该 MODULE 行；**不**含嵌套子 MODULE 下的 MENU）。父 MODULE 若只有子 MODULE、无直接 MENU，则 `grants.viewItems` 为空、`operationGroups` 仅含该 `moduleKey` 上注册的 Endpoint 操作项（若有）。

**`grants.viewItems`**：由上述 MENU 组装为 **`CatalogViewItemVo` 扁平列表**（访问侧不按 `viewGroups` 再套 VO）。`grantName` = MENU **`entryPageKey`**（§6.6.2）；`resourceKey` = `{grantName}:{domainKey}.{moduleKey}`。

**权限 UI（Q1）**：`permission-catalog` **展示** MODULE → MENU **`children` 结构**（与 navigation 对齐）；**可勾选**项**仅**出现在 MODULE 的 **`grants.viewItems` / `grants.operationGroups[].items`**，MENU 行本身**不**作为 grant 叶子（无独立 `menu:*` 授权键）。

**约束**：

- MODULE 节点**禁止**设置非空 `entryPageKey`；MENU 节点**禁止**非空 `children`。
- 同一 **直接父 MODULE** 下，各 MENU 叶的 **`entryPageKey` 唯一**。
- 允许多级 **MODULE**（`entryPageKey` 为空且 `children` 非空）；每个 MODULE 须有 **`moduleKey`**（插件内全局唯一）；**`@EndpointModule` 值 = 该 Endpoint 所属 MODULE 节点的 `moduleKey`**（与 Q2 一致）。
- 同一 **`domainKey` + `moduleKey`** 下，所有授权项（view + operation）的 **`grantName` 全局唯一**（因 `resourceKey` 不含 `entryPageKey`）。
- `@EndpointGrant`：`READ`/`EXPORT` 时 `group` = 目标 **`CatalogViewItemVo.grantName`**（与 MENU 组装规则一致，见 §6.3）。

#### 6.1.3 `kind: provide`

- YAML 列出 `functions[].name`（全局唯一，点分命名约定）。
- Java 实现：`@PluginFunction("platform.tenant.notify")` 标注在宿主扫描的 bean 方法上；启动时与 YAML **合并注册**，冲突 **启动失败**。

#### 6.1.4 `kind: subscribe`

```yaml
- kind: subscribe
  on: platform.tenant.notify
# 或
- kind: subscribe
  on: event:tenant.created
```

运行时：`PluginEventBus` 投递；handler 由插件在 Java 侧注册（与 v1 类似，但绑定 `on` 声明校验）。

### 6.2 函数调用契约

```java
// 契约骨架，非最终实现
public record PluginCallContext(
        String callerPluginId,
        String callerModuleKey,
        ConsoleOwnership ownership,   // 来自 console 作用域端口，plugin 仅透传
        AuthorizationSubject subject, // 可空；内部调用时由桥接填充
        Map<String, Object> attributes,
        Map<String, Object> payload
) {}

public record PluginCallResult(
        boolean success,
        String code,
        String message,
        Map<String, Object> data,
        Optional<PluginCallEvent> event   // 非空则发布 EventBus
) {}

public record PluginCallEvent(String name, Map<String, Object> payload) {}

public interface PluginFunctionInvoker {
    PluginCallResult invoke(String functionName, PluginCallContext context);
}
```

规则：

- **同步**：`invoke` 阻塞返回 `PluginCallResult`；被调方内部可用线程池实现异步，但对调用方仍一次 `CallResult`。
- **调用方**：仅 **provide 实现内部** + **宿主桥接**（如 console service）；
  页面运行时不直接 invoke。
- **权限**：function **不**出现在 `permission-catalog` 与角色 grant UI。

### 6.3 REST 注解契约

`@EndpointGrant` **只增加一个可选字段 `group`**。不再使用 `viewGroupId`；**是否归入访问分组、是否归入操作分组，完全由 `action` 决定**。

```java
@EndpointModule("organization")
public class PlatformOrganizationEndpoint {

    // READ / EXPORT：group = 目标 CatalogViewItemVo.grantName
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.tenant.list",
            group = "view.platform-organization-main"
    )
    @GET
    ...

    // 可变更类 action：group = contribute operationGroups[].group；省略则为 default
    @EndpointGrant(
            action = EndpointGrantAction.CREATE,
            name = "platform.tenant.create",
            group = "tenant-lifecycle"
    )
    @POST
    ...
}
```

| `action` | `group` 语义 | 缺省 `group` |
|----------|--------------|--------------|
| `READ`、`EXPORT` | 目标访问项的 **`grantName`**（与对应 `CatalogViewItemVo.grantName` 一致） | **不允许缺省** |
| 其余（`CREATE`、`UPDATE`、`DELETE`、`EXECUTE`、`STATUS` 等） | **操作分组 key**，对应 contribute `operationGroups[].group` | **`default`** |

校验（sync 或启动期）：

- `action` 为 `READ` 或 `EXPORT`：`group` 必须是该 MODULE 下某一 **`viewItems[].grantName`**（与 MENU 叶组装规则一致）。
- 否则：`group` 必须在 `operationGroups` 中声明（**`default` 始终合法**）。

**说明**：contribute 为同构 **`children` 树**；**`viewGroups` / `operationGroups` 仅挂在 MODULE 节点**（元数据）。注解层共用 `group`：READ/EXPORT → **`grantName`**；可变更类 → **`operationGroups.group`**（操作项本身用 `EndpointGrant.name` 作为 `grantName`）。

### 6.4 Catalog HTTP

| 方法 | 路径 | 变更要点 |
|------|------|----------|
| GET | `/api/d/nexus/catalog/permission-catalog` | **新增**（v2 权限配置专用）；响应见 §6.5 |
| GET | `/api/d/nexus/catalog/tree` | **废弃**（v2 移除或 410）；不再返回 v1 多态权限树（OPERATION 等混入 `children`） |
| POST | `/api/d/nexus/catalog/sync` | 输入：contribute.console@2 + EndpointGrantManager |

Navigation：`GET /api/d/nexus/navigation/menus` 仍为运行时侧栏（`NavigationMenuVo`，权限裁剪）；与权限配置 API **分离**。

**设计原则**：

- **`CatalogNodeVo`** 与 contribute **同构字段**：`displayName`、`description`（I18n）、`orderIndex`、`entryPageKey`、`children`；类型 **MODULE / MENU** 与 §6.1.2 相同规则推导。
- **`children`** 仅含 **MODULE / MENU** 节点，**不含** OPERATION；可授权项仅在 MODULE 的 **`grants`**。
- MENU 在 catalog 索引：`menu:{moduleKey}.{entryPageKey}`；**授权**与 MENU 解耦，统一为 **`CatalogViewItemVo`**，`resourceKey` = `{grantName}:{domainKey}.{moduleKey}`。
- 所有面向 UI 的 **`displayName` 均为 `I18nObject`**（与 contribute、`@EndpointGrant.label` 同源）；由前端或网关按 locale 解析。

### 6.5 权限目录响应：`CatalogNodeVo`

`GET /catalog/permission-catalog` 返回 **`CatalogNodeVo`** 聚合根（**通常** `resourceType` 不落库；**`children`** = 各插件 contribute 根下 **`modules[]`** 内容，字段名与 YAML 根层不同见 §6.8）。

```java
// 契约骨架，非最终实现；displayName、description 为 I18nObject
public record CatalogNodeVo(
        String resourceId,
        String resourceKey,
        CatalogResourceType resourceType,   // MODULE | MENU；由 entryPageKey/children 推导，见 §6.1.2
        String ownerPluginId,
        String domainKey,
        String moduleKey,                 // MODULE 与 MENU 均非空（MENU = 所属 MODULE 的 key）
        I18nObject displayName,
        I18nObject description,
        Integer orderIndex,
        String entryPageKey,              // MENU 非空；MODULE 为空
        String icon,                      // MENU 可选
        List<CatalogNodeVo> children,
        CatalogModuleGrantVo grants       // 仅 MODULE 非 null
) {}

/** 仅 MODULE 节点携带；与 children（菜单树）并列，不混入 children */
public record CatalogModuleGrantVo(
        List<CatalogViewItemVo> viewItems,
        List<CatalogPermissionGroup> operationGroups
) {}

/**
 * 权限分组：操作侧分组容器（由原 CatalogOperationGroupVo / 访问侧分组标题语义合并）。
 * 访问侧 contribute {@code viewGroups} 仅作元数据；响应中访问项在 {@link #viewItems()} 扁平列出。
 */
public record CatalogPermissionGroup(
        String group,
        I18nObject displayName,
        Integer orderIndex,
        List<CatalogViewItemVo> items
) {}

/**
 * 访问项与操作项统一授权条目（可勾选）。
 * {@code resourceKey} = {@code grantName + ":" + domainKey + "." + moduleKey}（单 MODULE 上下文中 domain/module 固定）。
 */
public record CatalogViewItemVo(
        String resourceKey,
        String grantName,
        I18nObject displayName,
        I18nObject description,
        Integer orderIndex
) {}
```

`CatalogViewItemVo` **不含** `ownerPluginId`、`domainKey`、`moduleKey`（避免与父级 `CatalogNodeVo` 重复）。HTTP 元数据（`action`、`httpMethod`、`path`）仅保留在 **`@EndpointGrant` / `EndpointGrantManager`** 侧，**不**进入权限目录 VO。

**`resourceKey` 约定**：

| 用途 | 格式 | 示例 |
|------|------|------|
| catalog 模块 | `module:{moduleKey}` | `module:organization` |
| catalog 菜单（`CatalogNodeVo` MENU） | `menu:{moduleKey}.{entryPageKey}` | `menu:organization.platform-organization-main` |
| **授权项**（`viewItems` / `operationGroups[].items`） | **`{grantName}:{domainKey}.{moduleKey}`** | `view.platform-organization-main:platform.organization` |
| 操作项 `grantName` | = `@EndpointGrant.name` | `platform.tenant.create:platform.organization` |

**树形与授权（Q6 A）**：

```text
CatalogNodeVo (MODULE, grants 非空)
├── children[]  CatalogNodeVo (MENU, grants=null)
└── grants
    ├── viewItems[]            ← CatalogViewItemVo，可勾选
    └── operationGroups[]      ← CatalogPermissionGroup（标题不可勾选）
            └── items[]        ← CatalogViewItemVo，可勾选
```

- **v2 `CatalogNodeVo`**：permission-catalog 树节点（`children` 仅 MODULE/MENU，`grants` 承载可勾选项）；**删除** v1 **`CatalogNodeOperationVo`** 与 **`GET /catalog/tree`** 旧契约（与 v2 节点形态不兼容）。
- **`CatalogPermissionGroup`**：仅响应分组；**不落库**、**不是** `CatalogResourceType`。

### 6.6 `CatalogResourceType`（v2 替换 v1 枚举）

v1 全部旧值（`PAGE`、`ACTION`、`DATASOURCE`、`CAPABILITY` 等）**从枚举删除**；sync 对账清理历史行。v2 **只允许**下列两种，且 **只有这两种** 写入 `nx_console_catalog_resource`：

```java
public enum CatalogResourceType {
    /** resourceKey = module:{moduleKey} */
    MODULE,
    /** resourceKey = menu:{moduleKey}.{entryPageKey} */
    MENU
}
```

| 概念 | `CatalogResourceType` | catalog 表 | `CatalogNodeVo.children` | `CatalogModuleGrantVo` | 角色 grant `resourceKey` |
|------|----------------------|------------|--------------------------|------------------------|---------------------------|
| 模块 | `MODULE` | 是 | 根 | — | 通常不直接授权模块 |
| 菜单结构 | `MENU` | 是 | 子节点 | — | 否 |
| 访问项 | **无** | **否** | 否 | `viewItems` | `{grantName}:{domain}.{module}` |
| 操作项 | **无** | **否** | 否 | `operationGroups[].items` | `{grantName}:{domain}.{module}` |

**禁止**：在 `CatalogResourceType` 中增加 `VIEW_GROUP`、`OPERATION` 等授权专用类型或任何 v1 遗留类型。访问/操作授权由 **permission-catalog 组装器**在读取时派生，不增加 catalog 资源类型。

#### 6.6.1 `ConsoleCatalogResourceEntity`（与 contribute `console@2` 对齐）

**职责**：仅存 **MODULE / MENU** 结构索引（navigation、permission-catalog 树、`resourceId`↔`resourceKey` 解析）。**不**落库 PAGE / ACTION / DATASOURCE；**不**落库 `viewItems` / `operationGroups` 可授权行（运行时由 MENU + `EndpointGrantManager` 组装）。

**同步来源**：`PluginDefinition.extensions` 中 `kind: contribute` / `console@2`；**扩展根不写行**（`domainKey` + **`modules[]`**），从 **`modules[]` 各 MODULE** 起 DFS（沿 `children`）。`domainKey` 取自扩展根，**下沉到每一行**。

**v1 列删除**（sync 对账后无对应 `resource_type` 的行一并清理或禁用）：

| 删除列 | 原用途 |
|--------|--------|
| `page_key` | v1 PAGE 节点；v2 由 MENU 的 **`entry_page_key`** 表达入口页 |
| `datasource_key` | v1 DATASOURCE |
| `request_method` / `request_url` | v1 DATASOURCE / ACTION 匹配 |
| `route_path` | v1 曾落库；v2 **读取时派生**，不持久化 |
| 索引 `idx_nx_console_catalog_resource_request` | 随上列删除 |

**v2 列与 contribute 字段映射**：

| 列名 | 类型（建议） | MODULE | MENU | 对应 contribute |
|------|-------------|--------|------|-----------------|
| `resource_id` | PK | ✓ | ✓ | 生成 |
| `owner_plugin_id` | string | ✓ | ✓ | 插件 |
| `domain_key` | string | ✓ | ✓ | 扩展根 `domainKey` |
| `module_key` | string | ✓ | ✓ | MODULE 节点 `moduleKey`（MENU 行为**所属 MODULE** 的 key） |
| `resource_type` | enum 名 | `MODULE` | `MENU` | §6.1.2 推导 |
| `resource_key` | string | `module:{moduleKey}` | `menu:{moduleKey}.{entryPageKey}` | §6.1.2 |
| `parent_resource_id` | FK | 父 MODULE（或 null=插件下顶层 MODULE） | 父 MODULE | `children` 树 |
| `entry_page_key` | string | **NULL** | **非空** | MENU `entryPageKey` |
| `display_name` | JSON text | ✓ | ✓ | `displayName`（I18n map 原样序列化） |
| `description` | JSON text | ✓ | ✓ | `description`（可选） |
| `sort_order` | int | ✓ | ✓ | `orderIndex`（**列名保留**，语义 = orderIndex） |
| `icon` | string | NULL | 可选 | MENU `icon` |
| `view_groups` | JSON text | **NULL** | **非空** | 同步时自**所属 MODULE** contribute `viewGroups[]` **下沉复制**到每条 MENU 行 |
| `operation_groups` | JSON text | **NULL** | **非空** | 同步时自所属 MODULE `operationGroups[]` **下沉复制**到每条 MENU 行 |
| `status` / `security_realm` | string | ✓ | ✓ | 宿主策略（realm 不变） |
| BaseEntity 审计列 | — | ✓ | ✓ | 不变 |

**实体骨架（契约，非最终实现）**：

```java
@Entity
@Table(name = "nx_console_catalog_resource")
public class ConsoleCatalogResourceEntity extends BaseEntity {

    private String resourceId;
    private String ownerPluginId;
    private String domainKey;
    private String moduleKey;
    private String resourceType;      // CatalogResourceType.name()
    private String resourceKey;
    private String parentResourceId;

    private String entryPageKey;      // MENU only
    private String displayName;       // JSON: I18n map
    private String description;       // JSON: I18n map, nullable
    private Integer sortOrder;        // contribute orderIndex
    private String icon;              // MENU only

    private String viewGroups;        // MENU only, JSON; column view_groups
    private String operationGroups;   // MENU only, JSON; column operation_groups

    private String status;
    private String securityRealm;
}
```

**contribute → 表**：`viewGroups` / `operationGroups` 仍**只声明在 MODULE 节点**（§6.1.2）；**sync 写入 MENU 行**时把所属 MODULE 的两段 JSON **复制到该 MENU 的 `view_groups` / `operation_groups`**，MODULE 行两列保持 **NULL**（结构索引与分组元数据分离）。

**`ConsoleCatalogResourceEntity` → `CatalogNodeVo`**：读取时反序列化 `display_name` / `description` 为 `I18nObject`；`sortOrder` → `orderIndex`；MODULE 上 **`grants`** 由 **§6.6.2** `CatalogPermissionCatalogAssembler` 生成。

**约束（sync 校验）**：

- MODULE：`entry_page_key`、`view_groups`、`operation_groups` 均为 **NULL**。
- MENU：`entry_page_key` 非空；`view_groups` / `operation_groups` **非空**且 JSON 须含 **`default`** 分组（与源 MODULE contribute 一致）；`children` 不落库。
- 插件内 `resource_key` 全局唯一（沿用 UK）。
- 嵌套 MODULE：每个 MODULE 行自有 `module_key`；其下 MENU 的 `module_key` 与 **直接父 MODULE 行**一致（Q2）。

#### 6.6.2 `CatalogPermissionCatalogAssembler`（Q3）

组装 **`GET /permission-catalog`** 时输入：

| 输入 | 用途 |
|------|------|
| `nx_console_catalog_resource` | MODULE/MENU 树、`domain_key`、`module_key`、`entry_page_key`、MENU 行 `view_groups` / `operation_groups`、展示名 JSON |
| **`EndpointGrantManager`** | 按 `@EndpointModule(moduleKey)` 归入该 MODULE 的 operation 项 |

规则：

- **sync / DB 不存** `grant_name` 列；访问项 **`CatalogViewItemVo.grantName` = 对应 MENU 行的 `entry_page_key`**（与 contribute MENU 的 `entryPageKey` 一致）。
- **`viewItems`**：该 MODULE **直接子 MENU** 各生成一项；`resourceKey` = `{entryPageKey}:{domainKey}.{moduleKey}`。
- **`operationGroups`**：以 MENU 行复制的 `operation_groups` JSON 为分组标题；组内 items 来自 `EndpointGrantManager`（`group` = operation group key；item `grantName` = `@EndpointGrant.name`）。
- 组装 **不依赖** 运行时 `ConsoleContributionCatalog`（catalog 索引已含 MENU 展示字段与分组元数据）。

### 6.7 角色授权 API

对外 grant 条目以 **`resourceKey`** 为主（**Q5 B**）：

```java
public record PermissionGrantItemVo(
        String resourceKey,
        // resourceId 可选冗余，内部解析
) {}

public record PermissionGrantReplaceRequest(
        List<PermissionGrantItemVo> grants
) {}
```

- 授权统一：`{grantName}:{domainKey}.{moduleKey}`（见 `CatalogViewItemVo`）。

存储层可继续用 `resourceId` FK；API 与前端 Tree `key` 统一 `resourceKey`。

### 6.8 页面访问与 HTTP 鉴权

页面访问校验由 `console.permission.pageaccess` 承担，输入为 `domainKey`、`moduleKey`、
稳定 `pageKey` 与可选 `grantGroup`（缺省 `default`）。校验依据 catalog 中叶子
MODULE/MENU 的 `entryPageKey` 及对应 `CatalogViewItemVo.resourceKey`，不依赖页面运行时资源。

HTTP API 仍由 `@EndpointModule` / `@EndpointGrant` 元数据驱动：

- **变更类**：按 **method + path** 匹配 `EndpointGrant`，校验
  **`{name}:{domainKey}.{moduleKey}`** grant。
- **READ / EXPORT**：`group` = **`grantName`**，与 permission-catalog 中访问项一致。

**边界**：plugin 与 catalog sync 均不读取页面运行时资源。

**permission-catalog 聚合根**：HTTP 响应用 `CatalogNodeVo.children` 列出各 **MODULE**（内容与 contribute 根下 **`modules[]` 同序、同构**；仅根层 YAML 字段名为 `modules`，API 保持 `children` 以免双字段）。

---

## 7. 失败与状态码

| 场景 | 状态码归属 | 抛出边界 |
|------|------------|----------|
| `functionName` 重复注册 | `PluginStatusCode`（PLUGIN_CONFIG_INVALID 或新增 PLUGIN_FUNCTION_CONFLICT） | 启动 / register |
| `invoke` 无注册函数 | PLUGIN_FUNCTION_NOT_FOUND | `PluginFunctionInvoker` |
| contribute 缺 `default` operationGroup | RESOURCE_CONFLICT | decode / compile |
| `@EndpointGrant.group` 与 `action` 不匹配（如 READ 用了未知 menuKey，或 OPERABLE 用了未知 operation group） | ENDPOINT_GRANT_INVALID（或 catalog sync 失败） | 启动或 sync |
| READ/EXPORT 未声明 `group` | 同上 | 编译期或启动期 |
| sync 后 grant 指向已删除 resourceKey | PERMISSION_RESOURCE_UNKNOWN | grant replace（可选宽松忽略） |
| `PageAccessVerifyRequest.grantGroup` 不在 MODULE `viewGroups` 中 | PERMISSION_RESOURCE_UNKNOWN | 页面访问校验 |

具体 local code 在实现阶段对齐 `PluginStatusCode` / `NexusStatusCode` 目录，**不新增 JDK 业务异常**。

---

## 8. 事务、幂等与并发

| 项 | 策略 |
|----|------|
| catalog sync | 单事务全量对账（沿用现有 workspace 对账语义） |
| role permissions replace | 全量替换，幂等 |
| FunctionRegistry | 插件 STOP 时注销该 plugin 提供的 function；START 时注册 |
| invoke | 无跨插件分布式事务；CallResult 表达业务成败 |

---

## 9. 兼容与迁移

### 9.1 Breaking 面

| 变更 | 状态 |
|------|------|
| `PluginDefinition` 去掉 `capabilities` / `requirements`；YAML `spec.capabilities` / `requirements` 编译拒绝 | **已落地** |
| 删除 Capability / Dependency 运行时与 `PluginManager.capabilities()` | **已落地** |
| console@1 `pages` / `menuTree`；entry 使用 **console@2** | **已落地** |
| Catalog 仅 MODULE/MENU；`permission-catalog` 组装 | **已落地**（console） |
| Manifest **`apiVersion: 2` + `extensions[]`** 替代 v1 文档头 + `contributions` | **未落地** |
| `FunctionRegistry` / `provide` / `subscribe` | **未落地** |
| 页面访问校验基于 catalog `resourceKey`（§6.8） | **已落地** |
| DB 清理历史 PAGE/ACTION/DATASOURCE catalog 行 | **按环境迁移** |

### 9.2 迁移步骤（剩余）

1. **plugin**：Manifest `apiVersion: 2`、`extensions[]` 编译；`function/` 包与注册表。
2. **dsl**：发布 `plugin-dsl-v2.schema.json`；宿主能力档案更新为 `console@2` only。
3. **console**：`@EndpointGrant.group` 与 assembler 操作分组端到端；`resourceKey` grant 迁移。
4. **手册**：`docs/plugin/manual` 已去除 Capability / `console@1` 叙述，与当前代码同步。

### 9.3 双轨

**不**恢复 Capability 运行时。DSL 在 `extensions[]` 落地前，**仅**保留 `nexus.plugin/v1` 文档头 + `contributions`（console@2），见 [plugin-dsl-spec.md](plugin-dsl-spec.md)。

---

## 10. 测试范围

| 类型 | 计划类/场景 | 断言要点 |
|------|-------------|----------|
| Plugin 契约 | `PluginDefinitionTest`（v2） | extensions 校验、function 唯一 |
| Function | `PluginFunctionInvokerTest` | invoke 成功/失败、event 发布 |
| Subscribe | `PluginEventBusTest` | on 匹配 |
| Console decode | `ConsoleExtensionV2DecoderTest` | 根 `modules`、MODULE `children`、viewGroups、operationGroups |
| Endpoint | `EndpointGrantManagerRegistrationTest` | READ 用 **grantName**；operable 默认 default |
| 页面访问 | `GrantGroupPageAccessVerifierTest` | 请求 `grantGroup` 与 catalog 访问项一致；API 用 EndpointGrant |
| 鉴权 | `ConsolePagePermissionAuthorizerTest` | 叶子 MODULE/MENU `entryPageKey` + `resourceKey` |
| Catalog sync | `ConsoleCatalogSyncServiceTest` | 仅 MODULE/MENU 行；不读取页面运行时资源 |
| Permission catalog | `ConsoleCatalogPermissionCatalogTest` | Q1：MENU 在树、勾选在 grants；Q2 嵌套 MODULE；Q3 `grantName` = `entryPageKey` |
| 集成 | `ConsoleCatalogPermissionCatalogIntegrationTest` | HTTP permission-catalog 契约 |
| 权限 HTTP | `PlatformPermissionEndpointIntegrationTest` | grant 使用 resourceKey |

**不测**：前端页面渲染；**测** 目录、授权键与 HTTP grant 契约。

---

## 11. AGENTS 对齐

| AGENTS 约束 | 本方案 |
|-------------|--------|
| `innospots-nexus-plugin` 拥有 classpath plugin runtime、contribution 解码 | 保留；**收缩**为 `extensions` + console@2；**不**拥有 catalog 表 |
| `innospots-nexus-console` 拥有 catalog 索引、REST grant 注解 | sync/tree/grant 变更均在 console |
| plugin **middleware-free** 边界 | function invoker **不**绑 Spring；桥接在 spring-console |
| portal/platform 不互依 | 仅 entry 声明迁移 |
| base 不引入 Servlet/Spring | `PluginCallContext` 使用 console 端口类型时，plugin 依赖 console **禁止** → **上下文接口应定义在 base 或 plugin 内最小 DTO**，console 适配（实现时须复核依赖方向） |

**依赖复核（实现门禁）**：

- `PluginCallContext` 若引用 `ConsoleOwnership` / `AuthorizationSubject`，应置于 **console 契约包** 或 **base snapshot**，plugin 只依赖 base。
- 推荐：`PluginCallContext` 在 **plugin** 内用 `Map` + 可选 `base` transport snapshots；宿主桥接填充。

---

## 12. 不建什么（防过度设计）

| 不引入 | 理由 |
|--------|------|
| 新 Maven 模块 | 在 plugin/console 内重构即可 |
| CapabilityType / Tags / Router | 已决策删除 |
| 通用 ContributionDecoderRegistry 插件链 | console@2 + provide 内置解码 |
| 页面运行时资源进入 plugin 或 catalog sync | 明确排除 |
| 权限 UI 上的 function 勾选 | Q7 C |
| `operationId` / pluginId 前缀函数名 | 使用全局 `functionName` |
| 第三套菜单存储 | 沿用 catalog |
| 长期 v1/v2 双 manifest 解析 | 一次性切换 |

---

## 13. 架构约束自检

- [x] 能力归属 plugin vs console 清晰
- [x] 依赖方向：platform → console → core/plugin，无回环
- [x] 公共 breaking 已标识迁移步骤
- [x] 新抽象（FunctionRegistry）有 provide + subscribe 两个消费者场景
- [x] 单包类数：删除后 plugin 包内文件数应 **显著下降**
- [ ] 实现前复核 `PluginCallContext` 依赖不引入 plugin→console 编译依赖

---

## 14. 开放问题（实现前闭合）

| # | 问题 | 状态 |
|---|------|------|
| 1 | `OPERATION_GROUP` 是否落库 | **已闭合**：不落库，用 `CatalogPermissionGroup`（§6.5） |
| 2 | `MENU` 在权限树是否展示 | **已闭合（Q1）**：展示于 `children`；勾选仅在 `grants` |
| 3 | 嵌套 MODULE 的 `grants` 范围 | **已闭合（Q2）**：每 MODULE 仅直接子 MENU + 本 `moduleKey` 的 Endpoint |
| 4 | 访问项 `grantName` 数据来源 | **已闭合（Q3）**：= MENU `entry_page_key`（DB）；无 contribute 覆盖 |
| 5 | 历史 grant 存 resourceId 的迁移 | sync 后 resourceKey↔id 映射；PUT 只接受 resourceKey |
| 6 | `plugin-dsl-v2.schema.json` 位置 | 本目录 `innospots-nexus-plugin/docs/plugin/design/` |
| 7 | `@PluginFunction` 扫描范围 | **仅插件 JAR** |

---

## 15. 已确认决策（grill-me 摘要）

1. Plugin **双轨**（YAML + Java），统一 `extensions[]`。
2. Console：扩展根 **`modules[]`** + MODULE/MENU 递归 **`children`**；MODULE 上
   `viewGroups` / `operationGroups`；catalog sync 不读取页面运行时资源。
3. Catalog 表仅 **MODULE/MENU**（**§6.6.1** 列与 contribute 对齐）；**viewItems / operationGroups** 为读取时组装。
4. `@EndpointGrant` 的 **`group`**：READ/EXPORT = **`grantName`**；可变更类 = **operationGroups.group**（缺省 `default`）。
5. 授权键：**`{grantName}:{domainKey}.{moduleKey}`**；**`CatalogViewItemVo`** 统一访问项与操作项；权限树节点 **`CatalogNodeVo`**。
6. **invoke(functionName)** + **CallResult** + 可选 **event**；**subscribe** 用 `on:`。
7. **删除** capability 路由与依赖；**同 PR** 最小改 console（实现阶段）。
8. 页面访问校验只消费 `domainKey` / `moduleKey` / `pageKey` / `grantGroup`，
   不依赖页面运行时资源。
9. 扩展根 **无 `moduleKey`**；子树从 **`modules[]`** 起（Q5）。
10. **权限目录（Q1）**：树含 MENU；grant 勾选仅在 MODULE **`grants`**。
11. **嵌套 MODULE（Q2）**：每层 MODULE 独立 **`grants`**（直接子 MENU；`@EndpointModule` = 该层 `moduleKey`）。
12. **组装（Q3）**：`CatalogPermissionCatalogAssembler` = **catalog DB** + `EndpointGrantManager`（§6.6.2）；访问 `grantName` = `entryPageKey`。

---

## 16. 附录：已移除的 v1 设计材料

以下文件已从 `docs/plugin/design` **删除**，内容被本文与 [plugin-runtime-design.md](plugin-runtime-design.md)、[plugin-dsl-spec.md](plugin-dsl-spec.md) 取代：

- `plugin-extension-design.md`（Capability + 总览）
- `plugin-console-contribution-design.md`（console@1）
- `plugin-v1-implementation-plan.md`
- `plugin-dsl-v1.schema.json`

操作手册以 [`../manual/README.md`](../manual/README.md) 为准。
