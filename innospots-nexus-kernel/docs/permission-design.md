# Kernel 菜单、页面与数据权限设计

## 1. 文档定位

本文定义 Nexus 管理平台的权限资源来源、角色与用户组授权模型、页面 UiSpec 权限裁剪，以及
基于请求 `URL`、HTTP `method`、页面 `pageKey` 和 datasource 管理端附加查询条件的数据权限方案。

本方案不再通过注解描述 Endpoint 所属页面，也不扫描 Endpoint 生成页面、按钮或 API 权限。
权限资源的事实源统一调整为：

```text
插件的 Console Contribution 声明
  -> 模块菜单树
  -> 页面声明
  -> 页面 UiSpec
  -> action / datasource
  -> URL 与请求参数定义
```

Endpoint 只负责提供标准 REST 能力。页面归属、按钮可见性、数据源 URL 和数据权限边界均由
插件 Contribution 及页面 UiSpec 描述，与 Java Endpoint 实现解耦。

## 2. 设计目标

1. 插件是管理平台功能的装配边界，每个插件通过 Console Contribution 贡献自己的菜单和页面。
2. 页面由 UiSpec 配置文件加载和渲染，不在 Endpoint 上重复声明页面信息。
3. 页面 UiSpec 中的 action 和 datasource 自动成为可发现、可授权的资源。
4. 管理平台按模块展示菜单、页面和按钮，支持给角色和用户组分配权限。
5. datasource 可以在角色或用户组权限设置页面中单独配置附加查询条件，并保存到该主体的授权数据中。
6. 权限设置不依赖 URL 或 Java 方法名作为稳定身份，接口路径变化不破坏既有授权。
7. 前端只负责展示权限结果和提交管理端配置；业务请求不携带或决定角色、用户组的数据约束，最终授权和
   数据条件由受信任的服务端处理。
8. 领域不变量仍由业务模块负责，不能通过权限配置绕过。

## 3. 不在本方案中的能力

- 不提供 Endpoint 权限注解和页面归属注解。
- 不进行全局 classpath Endpoint 扫描。
- 不把每个 REST Endpoint 自动登记为独立权限项。
- 不允许在权限配置中保存 SQL、SpEL、MVEL、Groovy 或其他可执行脚本。
- 不支持直接给单个用户授权，也不支持显式 DENY。
- 不要求业务 Service 编写 `hasPermission()` 或手工拼接数据权限条件。

未被页面 UiSpec 引用的后台 API、开放 API、回调接口和内部服务接口，不属于本页面权限模型，
应由对应应用边界使用认证、服务身份或独立 API 授权机制保护。

## 4. 模块职责

### 4.1 Core Plugin V1 契约

`innospots-nexus-core` 提供供外部工程实现和引用的稳定插件结构：

- `PluginDefinition`：插件身份、版本、能力和 Contribution 集合；
- `PluginManifest`：`plugin.yaml` 的严格解析结果；
- `PluginInstallationManager`：安装意图、缺失对账和运行时启停管理；
- `PluginRuntimeFactory`：按已安装插件装配隔离的运行时；
- `PluginContribution`：插件向宿主贡献管理平台资源的通用契约；

Core 负责插件发现、编译、安装状态持久化、运行时启停和通用 Contribution 生命周期，
不保存角色、用户组和权限分配。

### 4.2 Console Contribution

`innospots-nexus-console` 提供管理平台资源的 Contribution 契约和运行时处理器：

- `ConsolePluginContribution`：Contribution 类型 `console@1`；
- `ConsoleModuleDeclaration`：模块、菜单和页面的声明模型；
- `ConsolePluginContributionHandler`：校验、阶段提交、回滚和资源冲突；
- `ConsoleContributionCatalog`：只暴露已激活插件的 Console 资源。

Console 不负责插件安装记录，也不实现角色或用户组授权。

### 4.3 Base 页面 UiSpec 能力

`innospots-nexus-base` 的 `com.innospots.nexus.base.ui.spec` 提供页面规约基础：

- `UiSpec`：唯一页面规约根模型；
- `UiAction`：按钮和页面操作定义，通过 `datasourceKey` 引用数据源；
- `UiDatasource`：method、URL 和请求模板；
- `UiSpecConfig`：基础目录、文件后缀、格式和严格解析配置；
- `UiSpecParser` / `JacksonUiSpecParser`：YAML 解析和序列化；
- `UiSpecLoader` / `ClasspathUiSpecLoader`：按模块和页面加载规约；
- `UiSpecValidator`：页面、action、datasource 和组件交叉引用校验。

Base 不依赖 Console、Kernel、Servlet、Spring 或数据库，也不包含角色和用户组授权。

### 4.4 Kernel 权限业务

`innospots-nexus-kernel` 从 `ConsoleContributionCatalog` 读取已激活的管理平台资源，负责：

- 从已激活插件和已验证 UiSpec 构建权限资源目录；
- 角色和用户组的菜单、页面、action 授权；
- datasource 附加查询条件的管理端保存；
- 当前用户有效授权快照、UiSpec 裁剪和请求授权。

Kernel 不依赖插件实现类、插件 DAO 或插件安装表；插件运行时和安装记录由 Core 提供。

### 4.5 应用适配层

具体运行时负责一次性装配：

- 向 Kernel 发现器提供用于 SPI 加载的 ClassLoader；
- 根据 `moduleKey + pageKey` 定位并加载 UiSpec 文件；
- 注册权限资源同步服务和请求拦截适配器；
- 获取当前项目、用户、角色和用户组；
- 将请求拦截适配器接入实际请求链，并由适配器调用统一鉴权器；
- 保证受保护请求都经过对应适配器后才能继续执行业务接口。

Kernel 和 Console 不绑定 Spring Boot 自动配置。

## 5. 权限资源事实源

### 5.1 模块、菜单和页面

插件通过 `ConsolePluginContribution` 声明模块、菜单树和页面树：

```java
new ConsoleModuleDeclaration(
        "sales",
        displayName,
        description,
        List.of(new UiSpecPageDeclaration(
                "order-list",
                "/orders",
                List.of()
        )),
        List.of(MenuDeclaration.page(
                "order-management",
                menuTitle,
                "order",
                100,
                "order-list"
        ))
);
```

页面资源使用模块限定的稳定 key：

```text
page:sales.order-list
```

菜单资源使用：

```text
menu:sales.order-management
```

`pagePath` 只用于路由匹配，不作为权限主键。页面路径可以变化，`moduleKey`、`pageKey` 和
`menuKey` 发布后应保持稳定。

### 5.2 页面 UiSpec

Console Contribution 不保存 UiSpec 内容和物理路径。默认使用
`ui-spec/{moduleKey}/{pageKey}.yaml` 定位 classpath 规约；应用也可以通过替换 `UiSpecLoader`
从对象存储、数据库或独立页面服务加载。加载后校验 `UiSpec.pageInfo.pageId` 与插件 Contribution 的
`pageKey` 一致。

权限发现所需的最小 UiSpec 结构如下：

```yaml
pageInfo:
  pageId: order-list
  title:
    en: Order Management
pageType: table

datasources:
  order-query:
    method: GET
    url: /sales/orders
    params:
      groupIds: ${groupIds}
      ownerId: ${ownerId}

  order-export:
    method: POST
    url: /sales/orders/export
    body:
      filter:
        groupIds: ${groupIds}

  order-create:
    method: POST
    url: /sales/orders
actionDefinitions:
  create:
    actionId: create
    actionType: api
    label:
      en: Create Order
    datasourceKey: order-create

  export:
    actionId: export
    actionType: api
    label:
      en: Export
    datasourceKey: order-export
```

约束如下：

- action 和 datasource 的 `key` 在页面内唯一；
- action 必须引用一个 datasource，或者是纯前端操作；带有 inline request 的 action 不能进入权限目录；
- datasource 必须显式声明 HTTP method 和 URL；
- datasource 的 `params`、`headers` 和 `body` 只描述页面请求可以提交的参数；角色或用户组的附加查询条件
  不写入 UiSpec；
- `datasourceKey` 是页面内稳定的数据源身份；它不因 URL 变化而改变；
- datasource 是否存在附加查询条件由角色或用户组授权数据决定，不由 datasource 自动推导；
- URL 只是执行地址，权限授权始终指向稳定资源 key；
- UiSpec 不声明角色、用户组或具体授权结果。

### 5.3 资源类型

权限目录使用以下资源层次：

| 类型 | 稳定资源 key | 来源 | 是否直接授权 |
|------|--------------|------|--------------|
| MODULE | `module:sales` | 插件模块 | 否，作为管理树根节点 |
| MENU | `menu:sales.order-management` | 菜单声明 | 是 |
| PAGE | `page:sales.order-list` | 页面声明 | 是 |
| ACTION | `action:sales.order-list.export` | UiSpec action | 是 |
| DATASOURCE | `datasource:sales.order-list.order-query` | UiSpec datasource | 在数据权限区授权，可保存管理端附加查询条件 |

管理端的导航和按钮授权对象是 MENU、PAGE 和 ACTION。三者形成父子约束：从菜单进入页面时要求
菜单路径和 PAGE 均已授权，ACTION 生效要求所属 PAGE 已授权。DATASOURCE 在页面的数据权限区单独授权，
目录菜单仅在自身已授权且至少存在一个可见后代时展示，避免显示无法访问的空目录。

DATASOURCE 不作为普通菜单树节点，但它是实际接口调用必须校验的资源。一次 datasource 请求必须
同时满足：

1. 当前主体拥有该 `pageKey` 对应 PAGE 资源的权限；
2. 当前主体拥有由该页面 `URL + method` 定位到的 DATASOURCE 资源权限；
3. 如果管理员为该主体保存了附加查询条件，授权上下文可以携带这些条件的引用；本阶段不在权限
   鉴权器中执行具体查询拼接。

页面组件直接引用 datasource、或者 action 通过 `datasourceKey` 引用 datasource，都使用同一套
服务端判定。页面加载时可以根据 ACTION 权限裁剪按钮，但真正调用接口时仍必须重新执行 PAGE 和
DATASOURCE 权限判断。

这样管理端只展示用户能理解的菜单、页面和按钮，不把技术性的每个请求拆成大量权限项，
同时仍能对每个 datasource 做服务端授权和数据限制。

## 6. 资源发现与目录同步

### 6.1 发现流程

资源发现只读取已激活插件的 Console Contribution：

```text
ConsoleContributionCatalog.activeContributions()
  -> ConsoleModuleDeclaration
  -> MenuDeclaration / UiSpecPageDeclaration
  -> UiSpecLoader.load(moduleKey, pageKey)
  -> UiSpecParser / UiSpecValidator
  -> action / datasource manifest
  -> PermissionResourceCatalog
```

不读取 Endpoint 注解，不根据 Java 方法名或 URL 自动生成权限 key。

### 6.2 初始化校验

插件激活或资源同步前必须校验：

- plugin、module、menu、page key 的唯一性；
- 菜单引用的页面存在于同一模块；
- `moduleKey + pageKey` 能定位唯一 UiSpec；
- UiSpec 的 `pageInfo.pageId` 与页面声明一致；
- action key、datasource key 在页面内唯一；
- action 引用的 datasource 存在；
- datasource method、URL 和请求模板合法；
- 资源同步不处理角色或用户组的附加查询条件，相关配置在管理端保存时单独校验；
- 同一稳定资源 key 不存在冲突定义。

任何校验失败都阻止插件激活或本次同步，不产生半有效目录。

### 6.3 显式同步

资源目录只在管理操作显式触发时同步，不在模块加载时隐式写数据库。同步规则为：

1. 插件通过 Java SPI 或 `plugin.yaml` 被发现、编译并完成注册；
2. 插件的 Console Contribution 被激活，`UiSpecLoader` 成功加载对应页面 UiSpec；
3. `UiSpecValidator` 校验页面标识、action、datasource、method、URL 和请求模板；
4. 管理员显式调用权限目录同步接口；
5. 同步服务根据插件 Contribution 和 UiSpec 生成 MODULE、MENU、PAGE、ACTION、DATASOURCE 资源；
6. 新资源插入，元数据变化则更新；
7. 已从当前插件版本移除的资源标记为失效，不立即物理删除；
8. 自定义授权和历史审计记录保留；
9. 同一插件版本重复同步保持幂等。

`PermissionResourceEntity` 的创建时点是第 5 步生成资源定义并进入同步事务之后：同步服务按当前
项目上下文、资源类型和稳定 `resourceKey` 查找目录记录，记录不存在时创建实体并 `insert`，记录存在且
元数据发生变化时 `update`。插件发现、模块激活和 UiSpec 加载阶段只维护内存中的声明和 manifest，
不会直接创建 `PermissionResourceEntity`；用户访问页面、调用 datasource 或保存角色/用户组权限时
也不会临时创建资源目录记录。

资源目录和授权关系的生命周期必须分开：

```text
插件 Console Contribution + UiSpec
  -> 生成并同步 PermissionResourceEntity

管理端角色/用户组设置
  -> 针对已存在的 resourceId 创建或删除 PermissionGrantEntity
```

同步失败时整个目录同步事务回滚，不产生只有部分资源的目录。资源目录同步不删除授权历史，资源
失效后其历史授权仍可用于审计，但失效资源不进入有效授权快照。

#### 资源来源

页面权限模型中的资源可以全部由“插件 Console Contribution + UiSpec”生成，不需要扫描 Endpoint，也不需要在
Endpoint 上添加权限注解：

| 资源类型 | 来源 | 生成内容 |
|----------|------|----------|
| MODULE | `ConsoleModuleDeclaration` | 模块身份和模块根节点 |
| MENU | `MenuDeclaration` | 菜单树、标题、图标、顺序和页面引用 |
| PAGE | `UiSpecPageDeclaration` + `UiSpec.pageInfo` | 页面稳定 key、路由和页面元数据 |
| ACTION | `UiSpec.actionDefinitions` | 页面按钮和操作 key、显示信息及 datasource 引用 |
| DATASOURCE | `UiSpec.datasources` | datasource key、HTTP method、URL 和请求模板 |

Console Contribution 负责模块、菜单和页面的结构身份，UiSpec 负责页面内部的 action 和 datasource。UiSpec 的
`pageInfo.pageId` 必须与插件 Contribution 的 `pageKey` 一致，否则不能生成 PAGE、ACTION 或 DATASOURCE
资源。

角色、用户组、权限授权和 datasource 附加查询条件不属于插件或 UiSpec 的资源事实源，它们由管理端
保存到权限存储。未被插件或 UiSpec 声明的开放 API、回调接口和内部接口，也不自动进入本页面权限
目录，应由对应应用边界单独保护。

当前同步入口使用已激活的 `ConsoleContributionCatalog`、`UiSpecLoader` 和验证后的页面 manifest，不依赖
Endpoint 注解或方法扫描。

URL、标题和展示顺序属于可更新元数据。稳定 key 变化视为删除旧资源并创建新资源，不能自动迁移
授权；如确需迁移，应提供显式 key 迁移映射并记录审计。

## 7. 角色与用户组授权

### 7.1 授权来源

支持两种授权主体：

- 角色；
- 用户组。

当前用户有效功能权限为所有启用角色授权和所属启用用户组授权的并集：

```text
有效功能权限 = 角色授权集合 UNION 用户组授权集合
```

不支持直接用户授权和显式 DENY，避免复杂且难以解释的冲突优先级。

### 7.2 管理端权限树

管理端按以下结构加载资源：

```text
插件
  -> 模块
      -> 菜单目录
          -> 页面
              -> action 按钮
```

DATASOURCE 显示在页面或 action 的“数据权限”配置中，不作为普通功能树节点。管理端应支持：

- 按角色全量替换菜单、页面和 action 权限；
- 按用户组全量替换菜单、页面和 action 权限；
- 为 datasource 授予调用权限，并在权限页面单独配置附加查询条件；
- 查看授权来源、最终并集和拒绝原因；
- 预览某角色或用户组最终可见的页面 UiSpec。

选择 PAGE 时必须同时具备所属菜单路径权限，选择 ACTION 时必须同时具备所属 PAGE 权限。
管理端可以自动补选父节点，服务端保存时仍要校验，不能保存孤立页面或 action 授权。

### 7.3 页面加载裁剪

用户访问页面时，服务端按授权快照处理 UiSpec：

1. 校验 PAGE 权限；如果页面是从菜单进入的，再校验菜单路径权限；
2. 无 PAGE 权限时拒绝加载页面 UiSpec；
3. 删除未授权 action；
4. 删除仅被未授权 action 使用且不是页面共享的 datasource；
5. 保留页面初始化所需且已授权的 datasource；
6. 保留已授权 datasource 的 URL、method 和请求模板，前端按 UiSpec 发起实际请求；
7. 返回裁剪后的 UiSpec 和不可伪造的页面执行上下文标识，前端请求时通过 Header 传递
   `X-Nexus-Page-Key`。

前端隐藏按钮只是用户体验优化。即使调用者手工构造请求，服务端请求拦截适配器仍必须调用鉴权器重新校验 PAGE 和
DATASOURCE；已保存的附加查询条件由后续数据查询适配器使用，不能由请求方绕过。

### 7.4 请求鉴权和拦截适配

所有由页面 UiSpec 声明的 datasource 请求，在进入业务 Endpoint 前由应用侧请求拦截适配器调用
`RequestAuthorizer`。鉴权器只使用适配器传入的标准化请求信息，不依赖 Endpoint 注解，也不负责
代理、转发或调用业务接口：

```text
HTTP Request
  ├── method: POST
  ├── URL: /sales/orders/export
  └── X-Nexus-Page-Key: sales.order-list
```

拦截顺序固定如下：

1. 读取 HTTP method、请求路径和 `X-Nexus-Page-Key`；Header 的值使用完整页面标识
   `<moduleKey>.<pageKey>`，缺失或格式错误直接拒绝。
2. 规范化 method 和 URL。URL 使用路径模板匹配，例如实际路径
   `/sales/orders/ORD-1001` 匹配 `/sales/orders/{orderId}`；查询字符串不参与 datasource 身份。
3. 按 `pageKey` 查找已激活页面，确认页面属于当前项目和当前启用插件。
4. 从当前用户启用角色和所属用户组的授权集合中检查 PAGE 资源。没有 PAGE 权限立即拒绝，
   不继续匹配 datasource。
5. 在该页面已校验的 UiSpec manifest 中，用 `(method, URL)` 查找唯一 datasource。找不到或
   匹配到多个 datasource 都拒绝；客户端提交的 `datasourceKey` 只用于交叉校验，不能作为授权依据。
6. 使用解析出的 `datasourceKey` 查找 DATASOURCE 资源，并检查当前用户角色和用户组是否拥有
   该资源的权限。没有 DATASOURCE 权限拒绝。
7. 确认该主体是否存在管理端保存的附加查询条件。没有额外配置时，不自动推导用户、用户组、项目
   或其他数据范围；本期只支持保存和读取条件定义，不在鉴权器中执行具体查询拼接。
8. 生成授权上下文并写入请求上下文，把页面、datasource、主体和已保存条件的引用或摘要写入审计
   上下文。页面和 datasource 权限通过后继续执行请求链；权限不足时立即返回无权限响应，不进入
   Endpoint 或业务方法。

因此，权限判断不是“URL 直接对应一个权限”，而是：

```text
(pageKey -> PAGE 权限)
  AND
(pageKey + method + URL -> datasourceKey -> DATASOURCE 权限)
  AND
(该角色/用户组是否保存 datasource 附加查询条件；具体查询拼接机制后续实现)
```

当前请求只携带 `pageKey`，不能在服务端可靠区分同一 datasource 被两个具有不同权限的按钮调用。
需要独立控制按钮时，必须为按钮使用独立 `datasourceKey`；否则 ACTION 权限只负责页面裁剪，接口
最终以 DATASOURCE 权限为准。

## 8. Datasource 数据权限

### 8.1 页面权限和 datasource 权限

数据权限不直接绑定 URL，也不由系统预设 `SELF`、`GROUP` 或 `PROJECT` 范围。权限分为两层：

```text
PAGE grant
  subjectType: ROLE | GROUP
  subjectId
  resourceId: page resource ID

DATASOURCE grant
  subjectType: ROLE | GROUP
  subjectId
  resourceId: datasource resource ID
  constraintDefinition: 管理端页面保存的附加查询条件，可为空
```

PAGE grant 只表示可以访问页面，DATASOURCE grant 只表示可以调用该页面中的对应接口。两者
都由角色和用户组授权产生，当前用户的有效授权仍是角色授权和用户组授权的并集。

当前权限拦截不需要额外的 `targetKey`：服务端已经通过
`pageKey + method + URL -> datasourceKey` 定位 datasource，`datasourceKey` 同时作为授权资源身份。
没有 DATASOURCE grant 不能调用接口；有 DATASOURCE grant 但没有 `constraintDefinition` 时，表示
管理员没有为该授权配置额外的数据范围限制，系统不得擅自增加限制条件。

如果后续查询适配器需要知道业务查询模型，应在查询适配器注册表中按 `datasourceKey` 配置映射，或
届时增加语义明确的可选 `queryAdapterKey`。该映射属于数据查询实现，不属于基础权限资源模型，当前
不引入 `targetKey`。

项目隔离、租户隔离等平台基础边界由项目数据访问基础设施负责，不属于本节的角色或用户组数据
范围默认值。若业务需要按部门、负责人、区域等范围限制，必须由管理员在管理端权限页面显式配置。

### 8.2 管理端数据范围约束配置

管理端在“角色/用户组 -> datasource -> 数据范围约束”页面维护约束。该配置属于角色或用户组对
datasource 授权关系的附加数据，保存到权限存储中，不写入 UiSpec 文件，也不由插件 Provider 预设
或写死在后端代码中。

页面配置的对象是“该角色或用户组访问该 datasource 时，后端需要附加的查询条件”，例如：

| 主体 | datasource | 查询字段 | 条件 | 条件值 |
|------|------------|----------|------|--------|
| 角色 `role_sales_manager` | `order-query` | `order.regionId` | 等于 | `CN-EAST` |
| 用户组 `group_sales_north` | `order-query` | `order.regionId` | 属于 | `CN-EAST`、`CN-NORTH` |

这些条件不是前端业务请求的参数，也不是前端可以随请求任意提交的值。前端只在权限管理页面
提交配置；普通页面请求仍然只携带页面 UiSpec 定义的请求参数。请求进入后，服务端根据当前主体、
`pageKey`、method、URL 和 datasourceKey 读取已保存的附加条件，未来在数据查询阶段与请求查询条件
一起使用，例如：

```text
业务查询条件
  AND
角色或用户组针对 datasource 保存的附加条件
```

具体条件如何映射到查询对象、如何处理等于/属于/范围/关联等不同查询逻辑，留待后续的数据查询
约束实现确定。本阶段只要求管理端能够新增、修改、查询、清空和展示该配置。

管理端保存权限时，将约束和资源授权一起提交到角色或用户组的权限全量替换接口。接口只修改已有的
授权集合，不会因为提交附加条件而隐式授予 datasource 权限。保存空条件表示该授权不增加额外数据
范围限制。

`constraintDefinition` 是授权记录中的可选持久化字段，用于保存管理端提交的结构化条件定义；它
不是 classpath 配置文件，不由页面 UiSpec 加载，也不由客户端业务请求传入。当前不固定条件定义的
具体序列化格式和查询执行算法，但不得保存 SQL、脚本、Java 类名或方法名。

### 8.3 约束保存和后续查询使用

当前主体可能通过多个角色或用户组获得同一 datasource 权限。权限拦截阶段仍按以下规则判断是否
可以调用：

```text
PAGE grant
  AND
DATASOURCE grant
```

每条 DATASOURCE grant 可以独立保存一份附加查询条件。角色和用户组的权限授权仍取并集；多条
附加条件在后续查询约束实现中的 AND/OR 合并、冲突处理和优先级暂不在本阶段确定，避免在权限存储
功能尚未完成时固化错误的查询语义。

后续实现数据查询约束时，执行位置应在服务端数据访问层或 datasource 对应的查询适配器：

1. 请求拦截适配器调用鉴权器解析实际请求并定位 datasource；
2. 后端从权限存储读取当前主体对该 datasource 的附加条件；
3. 查询适配器将附加条件与业务请求条件组合后执行查询；
4. 前端不能删除、覆盖或扩大服务端保存的附加条件。

本阶段鉴权器只负责权限判定和条件定义的读取/传递，不执行上述第 3 步的具体查询拼接。

### 8.4 约束配置保存校验

当前保存功能只做保证数据可归属、可追溯和可维护的基础校验：

- `subjectType` 只能是 `ROLE` 或 `GROUP`，且主体属于当前项目；
- `datasourceKey` 必须对应已激活页面 UiSpec 中的 datasource；
- 只有已存在的 DATASOURCE grant 才能保存附加查询条件；
- 配置定义必须能够被管理端保存、查询和回显，清空时将 `constraintDefinition` 置空；
- 保存记录必须保留主体、datasource、项目和更新时间等归属信息；
- 配置内容不得包含 SQL、脚本、Java 类名、方法名或其他可执行内容。

字段语义校验、条件类型校验、查询字段映射、不同查询逻辑的合并规则和运行时失败关闭策略，
在后续查询约束实现中补充，不在本阶段提前固化。

### 8.5 请求鉴权器与拦截适配

所有受保护 datasource 请求必须由实际请求拦截器或 Filter 调用统一的 `RequestAuthorizer`。
`RequestAuthorizer` 是框架无关的权限判定组件，只接收标准化请求上下文和当前主体信息，返回允许/拒绝
结果及规范化授权上下文，不负责网络转发，也不依赖具体 Web 框架：

```text
实际请求进入应用适配器
  -> 读取 method + URL + X-Nexus-Page-Key
  -> 调用 RequestAuthorizer
  -> 按 pageKey 查找页面并校验 PAGE 权限
  -> 按 method + URL 匹配唯一 datasourceKey
  -> 校验 DATASOURCE 权限
  -> 构造请求上下文和 AuthorizationContext
  -> ALLOW：继续执行 Filter/Resource/Endpoint 调用链
  -> DENY：直接返回无权限响应，不调用业务接口
  -> 记录授权来源和审计信息
```

请求适配器和鉴权器不得信任客户端额外提交的目标 URL 或 datasourceKey。真实 method 和 URL 必须来自实际
HTTP 请求，datasourceKey 必须由服务端根据页面 UiSpec 中已验证的 `(pageKey, method, URL)`
匹配得到；如果客户端额外提交 datasourceKey，只能作为一致性校验。可约束请求路径必须从服务端
已验证的 datasource 请求模板读取。

请求适配层必须保证受保护业务接口经过实际请求拦截器，并由拦截器调用 `RequestAuthorizer`。当前设计不绑定某个 Web 框架，应用可以提供
以下适配器：

1. Servlet `Filter`：从 `HttpServletRequest` 提取实际 method、URL 和 Header，调用 `RequestAuthorizer`；
2. Jakarta REST `ContainerRequestFilter`：在 Resource 方法调用前调用 `RequestAuthorizer`；
3. 其他运行时适配器：实现相同的请求上下文提取、允许继续、拒绝短路和授权上下文传递契约。

适配器发现受保护请求时必须调用统一鉴权器；如果同一业务接口存在未注册适配器的访问入口，
则该入口不能被纳入本页面 datasource 权限模型，必须由应用边界单独保护。仅在前端隐藏按钮或
修改请求参数不能形成有效的数据权限。

#### 8.5.1 框架无关的鉴权契约

`RequestAuthorizer` 只定义权限判定，不持有 Servlet、Jakarta REST 或其他运行时
类型依赖。其输入和输出可以抽象为：

```text
AuthorizationRequest
  - method
  - path
  - pageKey
  - headers / params / body
  - subject / project

AuthorizationDecision
  - allowed
  - datasourceKey
  - authorizationContext
  - denyReason
```

鉴权调用约定是：

1. 适配器从实际请求提取 `method`、路径、`X-Nexus-Page-Key` 和请求参数，构造
   `AuthorizationRequest`；
2. `RequestAuthorizer` 完成页面权限和 datasource 权限判定；已保存的附加查询条件只
   作为授权上下文中的可选引用，不在本阶段执行查询拼接；
3. 返回 `allowed=false` 时，适配器按照运行时规范写入无权限响应，短路请求链，Endpoint 和业务
   方法均不执行；
4. 返回 `allowed=true` 时，适配器把 `authorizationContext` 传递到后续请求上下文，然后继续原有
   请求链；后续查询适配器再决定如何使用管理端保存的附加条件；
5. 适配器不重新实现权限规则，只负责请求转换、响应转换、上下文传递和请求链控制。

因此 Kernel 的权限实现与实际拦截器适配彼此解耦。后续可以分别提供 Servlet `Filter`、Jakarta REST
`ContainerRequestFilter` 或其他标准运行时适配器，而权限规则、资源匹配和约束执行保持同一套实现。

### 8.6 查询、更新和删除

- 查询接口的附加数据条件由后续查询适配器根据 `AuthorizationContext` 处理，当前鉴权器只
  负责权限判定和上下文传递；
- 按 ID 查询、更新和删除不能仅依赖列表参数，应先验证对象的 owner、group、project 等最小投影；
- 批量操作必须逐项或按安全集合约束，不能只检查第一个 ID；
- 下游如使用 MyBatis 自动过滤，应消费结构化授权上下文，不拼接原始 SQL；
- 无法识别参数、对象归属或数据适配器时默认拒绝。

## 9. 业务规则边界

页面权限和数据权限不替代业务规则。权限体系仍按以下边界划分：

| 层级 | 示例 | 执行位置 |
|------|------|----------|
| 页面权限 | 是否可以进入订单页面 | UiSpec 加载前 |
| action 权限 | 是否显示审批按钮 | UiSpec 裁剪；实际调用仍由 DATASOURCE 权限控制 |
| 数据权限 | 可以查看或操作哪些订单 | 后续查询适配器及数据访问层 |
| 业务授权 | 大额订单只能由高级审批人操作 | 请求继续前的注册 Handler |
| 领域不变量 | 已取消订单不能审批 | 业务领域内部 |

复杂业务授权通过稳定 `handlerKey` 注册 Java `AuthorizationHandler`。UiSpec 或数据库只保存
`handlerKey` 和类型化配置，不保存类名、方法名或可执行代码。

业务 Service 不出现角色、用户组、页面或 action 判断；领域代码只维护任何调用方都不能绕过的
业务不变量。

## 10. 管理数据模型

### 10.1 合并结论

首期只需要两张核心业务表：

| 实体 | 表名 | 作用 |
|------|------|------|
| `PermissionResourceEntity` | `nx_permission_resource` | 保存插件 Contribution 发现的 MODULE、MENU、PAGE、ACTION、DATASOURCE 资源 |
| `PermissionGrantEntity` | `nx_permission_grant` | 统一保存角色和用户组的功能授权，以及 datasource 附加查询条件 |

不再单独保留 `PermissionEntity`。在新模型中，每个稳定资源本身就是可授权能力，不需要先创建
`permissionCode`，再用另一张表把 permission 映射到 resource。

角色和用户组共用 `PermissionGrantEntity`，通过 `subjectType` 区分。功能授权与 datasource
附加查询条件也共用该表，通过被引用资源的 `resourceType` 区分：

- MENU、PAGE、ACTION 记录表示功能授权；
- DATASOURCE 记录表示该主体对数据源的调用权限，并可保存管理端附加查询条件；
- MODULE 不直接产生授权记录，只作为资源树根节点；
- DATASOURCE 都需要独立授权记录；调用时还必须通过 PAGE 权限，与是否存在后续查询适配器无关。

这两张表不能继续合并为一张表。资源目录由插件和 UiSpec 版本驱动，授权记录由管理员频繁修改，
并且一个资源会对应多个角色和用户组。把授权集合保存到资源目录定义中会导致大字段并发覆盖、无法
按主体查询、无法建立唯一约束，也会把插件资源同步与管理授权耦合。

### 10.2 公共字段

两个实体都继承 `WorkspaceBaseEntity`，自动包含：

| 字段 | Java 类型 | 说明 |
|------|-----------|------|
| `tenantId` | `String` | 租户隔离字段，由运行时上下文自动填充 |
| `workspaceId` | `String` | Workspace 隔离字段，由运行时上下文自动填充 |
| `createdAt` | `LocalDateTime` | 创建时间 |
| `updatedAt` | `LocalDateTime` | 最后更新时间 |
| `createdBy` | `String` | 创建人 |
| `updatedBy` | `String` | 最后修改人 |

所有唯一索引都必须包含 `workspace_id`。DAO 查询也必须显式或通过拦截器限定当前 Workspace。

### 10.3 PermissionResourceEntity

`PermissionResourceEntity` 是权限目录的唯一事实投影。一条记录对应一个稳定资源，展示信息和
UiSpec 执行信息都属于该资源的元数据。

#### 字段定义

| 字段 | Java 类型 | 数据库建议 | 必填 | 说明 |
|------|-----------|------------|------|------|
| `resourceId` | `String` | `varchar(32)` | 是 | 资源主键，建议前缀 `prs` |
| `ownerPluginId` | `String` | `varchar(128)` | 是 | 来源插件 ID |
| `moduleKey` | `String` | `varchar(128)` | 是 | 所属模块 key |
| `resourceType` | `String` | `varchar(32)` | 是 | `PermissionResourceType` 的持久化值 |
| `resourceKey` | `String` | `varchar(256)` | 是 | 项目内稳定资源 key |
| `parentResourceId` | `String` | `varchar(32)` | 否 | 资源所有权父节点，不表示菜单到页面的引用关系 |
| `pageKey` | `String` | `varchar(256)` | 否 | 资源所属或引用的页面 key，用于页面资源关联查询 |
| `displayName` | `String` | `varchar(256)` | 否 | 目录展示名称；完整多语言内容仍由插件或 UiSpec 提供 |
| `sortOrder` | `Integer` | `int` | 是 | 同级资源排序，默认 0 |
| `status` | `String` | `varchar(32)` | 是 | `BasicStatus` 的持久化值；移除的插件或 UiSpec 资源改为 DISABLED |
| `datasourceKey` | `String` | `varchar(128)` | 否 | DATASOURCE 所属页面内的 datasource key |
| `routePath` | `String` | `varchar(512)` | 否 | PAGE 的页面路由模板 |
| `requestMethod` | `String` | `varchar(16)` | 否 | DATASOURCE 的 HTTP method |
| `requestUrl` | `String` | `varchar(512)` | 否 | DATASOURCE 的 HTTP URL 路径模板 |

推荐索引：

```text
PRIMARY KEY (resource_id)
UNIQUE (workspace_id, resource_key)
INDEX (workspace_id, owner_plugin_id, module_key, resource_type, status)
INDEX (workspace_id, parent_resource_id, sort_order)
```

`resourceKey` 已包含资源类型前缀，因此只需要 `(workspace_id, resource_key)` 唯一索引，不必再把
`resource_type` 放入唯一索引。

#### 父子关系

`parentResourceId` 表示资源所有权树：

```text
MODULE
  -> MENU
      -> MENU
  -> PAGE
      -> PAGE
      -> ACTION
      -> DATASOURCE
```

MENU 到 PAGE 的导航引用由插件菜单声明和资源实体的 `pageKey` 维护；一个 PAGE 可以被多个菜单引用，
也可以不进入菜单，不把菜单引用塞入通用定义文本中。ACTION 和 DATASOURCE 的所属页面同样使用
`pageKey` 关联。

#### 来源元数据

`PermissionResourceEntity` 是权限目录索引，不是插件或 UiSpec 的替代事实源。它只保存管理端展示、
授权关联和同步所需的规范化字段。完整的页面内容、action 定义和请求模板仍由插件 Contribution 与 UiSpec
提供。为支持请求拦截，datasource 的 method、URL 路径和页面内 key 会同步到独立字段；这些字段是
运行索引，不是 UiSpec 快照。

因此当前不需要 `resourceDefinition` 字段，也不需要把每种资源的完整定义再次保存为 YAML。同步时
直接比较当前插件与 UiSpec 生成的规范化字段；请求运行时从已加载并校验的 UiSpec manifest 按
`pageKey + method + URL` 找到 `datasourceKey`，再通过资源 key 查询授权。

如果未来需要在插件不可用时保留完整页面快照，应另行设计版本化的页面快照存储，不把快照混入当前
权限资源实体，也不把它作为权限判断的必要输入。

#### Java 实体结构

```java
@Getter
@Setter
@Entity
@TableName("nx_permission_resource")
public class PermissionResourceEntity extends WorkspaceBaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String resourceId;

    private String ownerPluginId;
    private String moduleKey;
    private String resourceType;
    private String resourceKey;
    private String parentResourceId;
    private String pageKey;
    private String datasourceKey;
    private String routePath;
    private String requestMethod;
    private String requestUrl;
    private String displayName;
    private Integer sortOrder;
    private String status;

    @Override
    public String idPrefix() {
        return "prs";
    }
}
```

实体保存结构化文本，业务代码应通过 Converter 和结构化领域 record 转换，不在 Service 中手工
读取或拼装 `Map<String, Object>`。

### 10.4 PermissionGrantEntity

`PermissionGrantEntity` 统一表示“某个角色或用户组对某个资源的一条授权路径”。记录存在即表示
授权有效，不额外保存 `allowed=true`，也不保存 DENY。

#### 字段定义

| 字段 | Java 类型 | 数据库建议 | 必填 | 说明 |
|------|-----------|------------|------|------|
| `grantId` | `String` | `varchar(32)` | 是 | 授权主键，建议前缀 `pgr` |
| `subjectType` | `String` | `varchar(32)` | 是 | `PermissionSubjectType` 的持久化值：ROLE 或 GROUP |
| `subjectId` | `String` | `varchar(32)` | 是 | 角色 ID 或用户组 ID |
| `resourceId` | `String` | `varchar(32)` | 是 | 指向 `PermissionResourceEntity` |
| `constraintDefinition` | `String` | `text` | 否 | 管理端为 DATASOURCE grant 配置的附加查询条件；为空表示无额外限制 |

推荐索引：

```text
PRIMARY KEY (grant_id)
UNIQUE (workspace_id, subject_type, subject_id, resource_id)
INDEX (workspace_id, subject_type, subject_id)
INDEX (workspace_id, resource_id)
```

`subjectId` 是多态引用，数据库无法同时外键到角色表和用户组表。保存授权时由 Service 按
`subjectType` 批量校验对应主体存在且属于当前项目。资源查询和主体查询分开执行，再在 Service
内存组装，符合 DAO 单表访问约束。

#### 不同资源类型的字段规则

| resourceType | 记录含义 | constraintDefinition |
|--------------|----------|----------------------|
| MODULE | 禁止创建 | 不适用 |
| MENU | 菜单功能授权 | 必须为 null |
| PAGE | 页面功能授权 | 必须为 null |
| ACTION | 按钮功能授权 | 必须为 null |
| DATASOURCE | datasource 调用授权 | 可选，由管理端显式配置 |

页面被菜单引用时，至少存在一条完整已授权菜单路径才可从菜单进入；未被菜单引用的独立页面只
校验 PAGE。保存 ACTION 授权时必须同时存在所属 PAGE 授权。DATASOURCE grant 不代表脱离页面
调用的能力，调用时必须同时具备所属 PAGE 权限；该记录可附带管理端配置的数据参数限制。

全量替换功能授权时，管理端应明确提交 PAGE、ACTION 和 DATASOURCE grant：

1. 没有 DATASOURCE grant 时，即使拥有 PAGE 或 ACTION 权限也不能调用该 datasource；
2. 管理端在角色或用户组权限全量替换时提交 DATASOURCE grant 及其约束；
3. 没有提交约束时 `constraintDefinition` 保持为空，不生成任何默认数据范围；
4. 删除 PAGE 或 ACTION 授权后，管理端可以同步删除不再需要的 DATASOURCE grant；
5. 同一主体对同一 datasource 始终只有一条记录，由唯一索引保证。

#### constraintDefinition 存储规则

`constraintDefinition` 保存管理端页面提交的附加查询条件定义。它是授权记录的可选业务数据，
不是 classpath 配置文件，也不是 UiSpec 的一部分。当前只要求支持保存、修改、查询、清空和回显，
不在权限存储阶段固化查询条件的具体操作符、字段映射、组合方式或执行引擎。

附加条件应能表达“当前主体访问某个 datasource 时，后端需要额外增加的查询条件”，例如
`order.regionId = CN-EAST`。条件最终应由后端查询适配器拼接到业务查询条件中，不能由前端业务请求
传入、覆盖或删除。具体条件模型和查询拼接机制在后续实现中确定，但存储内容不能包含 SQL、脚本、
Java 类名或方法名。

#### Java 实体结构

```java
@Getter
@Setter
@Entity
@TableName("nx_permission_grant")
public class PermissionGrantEntity extends WorkspaceBaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String grantId;

    private String subjectType;
    private String subjectId;
    private String resourceId;
    private String constraintDefinition;

    @Override
    public String idPrefix() {
        return "pgr";
    }
}
```

### 10.5 示例数据

资源目录示例：

| resourceId | resourceType | resourceKey | parentResourceId |
|------------|--------------|-------------|------------------|
| `prs_mod` | MODULE | `module:sales` | null |
| `prs_menu` | MENU | `menu:sales.order-management` | `prs_mod` |
| `prs_page` | PAGE | `page:sales.order-list` | `prs_mod` |
| `prs_export` | ACTION | `action:sales.order-list.export` | `prs_page` |
| `prs_query_ds` | DATASOURCE | `datasource:sales.order-list.order-query` | `prs_page` |
| `prs_export_ds` | DATASOURCE | `datasource:sales.order-list.order-export` | `prs_page` |

角色 `role_sales_manager` 的授权示例：

| subjectType | subjectId | resourceId | constraintDefinition | 含义 |
|-------------|-----------|------------|----------------------|------|
| ROLE | `role_sales_manager` | `prs_menu` | null | 可见订单菜单 |
| ROLE | `role_sales_manager` | `prs_page` | null | 可进入订单页面 |
| ROLE | `role_sales_manager` | `prs_export` | null | 可使用导出按钮 |
| ROLE | `role_sales_manager` | `prs_query_ds` | `order.regionId = CN-EAST` | 可查询东区订单 |
| ROLE | `role_sales_manager` | `prs_export_ds` | `order.regionId = CN-EAST` | 只能导出东区订单 |

用户组 `group_west_region` 可以对同一 datasource 资源配置另一组显式约束。当前用户同时属于该角色
和用户组时，功能资源取并集，datasource 的约束路径按第 8.3 节的 OR 规则合并。

### 10.6 可复用策略是否单独建表

首期不建议创建 `AuthorizationPolicyEntity`，也不在 `PermissionResourceEntity` 中增加通用的
`resourceDefinition` 或 YAML 规则字段。主体差异化数据约束保存在
`PermissionGrantEntity.constraintDefinition`，已经能覆盖当前的保存需求。

仅当出现以下需求时，再增加 `AuthorizationPolicyEntity`：

- 同一策略需要被大量资源或授权重复引用；
- 策略需要独立启停、版本管理和审批；
- 管理端需要维护策略模板库；
- 策略生命周期与资源 UiSpec 生命周期不同。

可选策略实体字段为：

```text
policyId
policyKey
policyName
policyType          DATA | BUSINESS
queryAdapterKey
definitionType     CONDITION | HANDLER
definition
version
status
```

资源定义或授权记录只保存稳定 `policyKey` 列表，不需要为角色和用户组分别创建 policy 关联表。
策略删除前检查引用，停用策略后所有引用路径失败关闭。

### 10.7 审计是否单独建表

权限实体继承的创建人、修改人和时间只能回答“当前记录由谁最后修改”，不能完整回答“授权从什么
值变成什么值”。权限变更应发布以下审计事件并接入平台统一审计能力：

- `PermissionCatalogSyncedEvent`
- `RolePermissionReplacedEvent`
- `GroupPermissionReplacedEvent`
- `DatasourceAuthorizationDeniedEvent`

因此首期不创建 `PermissionAuditEntity`。如果平台尚无统一审计存储，可临时增加
`nx_permission_audit`，但它属于审计基础设施的适配，不参与权限判定，也不能成为授权事实源。

### 10.8 旧模型合并映射

| 旧模型 | 新模型 | 处理方式 |
|--------|--------|----------|
| `PermissionEntity` | `PermissionResourceEntity` | 删除中间权限能力层，权限名称、状态并入资源 |
| `PermissionResourceEntity` | `PermissionResourceEntity` | 扩充为完整 MODULE/MENU/PAGE/ACTION/DATASOURCE 目录 |
| `RolePermissionGrantEntity` | `PermissionGrantEntity` | `subjectType=ROLE` |
| `GroupPermissionGrantEntity` | `PermissionGrantEntity` | `subjectType=GROUP` |
| `RolePermissionPolicyEntity` | `PermissionGrantEntity.constraintDefinition` | 合并到角色 datasource 授权附加查询条件 |
| `GroupPermissionPolicyEntity` | `PermissionGrantEntity.constraintDefinition` | 合并到用户组 datasource 授权附加查询条件 |
| `PermissionMandatoryPolicyEntity` | 后续独立的资源策略关联 | 不并入 `PermissionResourceEntity` 的通用定义字段 |
| `AuthorizationPolicyEntity` | 首期删除，可选恢复 | 只有需要策略复用和独立生命周期时才保留 |

最终核心关系为：

```text
PermissionResourceEntity 1
  <- N PermissionGrantEntity
       subjectType + subjectId -> ROLE 或 GROUP
```

该结构在保持资源目录与授权记录分离的前提下，将原来的权限、资源、角色授权、用户组授权和四类
策略关联收敛为两张核心表。

### 10.9 枚举定义

```java
public enum PermissionResourceType {
    MODULE,
    MENU,
    PAGE,
    ACTION,
    DATASOURCE
}

public enum PermissionSubjectType {
    ROLE,
    GROUP
}
```

Entity 按仓库现有约定使用 `String` 持久化枚举值，领域 request、model 和 service 边界使用枚举。
权限模型不定义 SELF、GROUP、GROUP_TREE、PROJECT 等固定数据范围枚举；是否存在数据约束以及
约束内容完全由管理端配置决定。

### 10.10 结构化字段与生命周期规则

使用结构化文本字段的边界：

- 资源定义和附加查询条件只保存低频修改、通常整体读取的元数据；
- 需要建立索引或参与高频筛选的 `resourceType`、`resourceKey`、`subjectType`、
  `subjectId` 和 `resourceId` 必须是独立列；
- `constraintDefinition` 是管理端保存的附加查询条件定义；当前只校验归属、格式和可回显性，查询字段、
  条件类型和组合语义由后续查询约束实现补充；
- 约束定义不允许保存 SQL、表达式脚本、Java 类名或方法名；
- 运行时权限拦截只读取并传递已保存定义，不在核心授权流程中执行具体查询拼接。

资源同步事务：

1. 加载并完整校验插件、菜单、页面和 UiSpec manifest；
2. 在一个事务中按 `resourceKey` 插入或更新资源；
3. 当前版本已移除的插件或 UiSpec 资源改为 DISABLED；
4. 禁止级联删除其授权记录，保留关系用于审计和插件重新启用；
5. 授权快照只加载 ENABLED 资源，失效资源的历史 grant 不产生权限。

角色或用户组授权全量替换事务：

1. 批量校验主体、资源、父子授权和 datasource 附加查询条件的归属；
2. 校验管理端显式提交的 datasource grant 和约束定义；
3. 在一个事务中删除该主体不再需要的 grant，插入或更新期望 grant；
4. 提交后发布授权替换审计事件并失效该主体的授权缓存；
5. 任一资源或约束校验失败时整体回滚。

`PermissionGrantEntity` 不设置 `status` 字段。授权存在即生效，撤销即在全量替换事务中删除；角色、
用户组和资源自身的 ENABLED/DISABLED 状态分别由对应实体控制，避免四层状态组合产生歧义。

## 11. 管理接口

Kernel 提供 Jakarta REST 管理契约：

| 能力 | 建议接口 |
|------|----------|
| 加载权限树 | `GET /console/permissions/catalog` |
| 显式同步目录 | `POST /console/permissions/catalog/sync` |
| 替换角色权限 | `PUT /console/roles/{roleId}/permissions` |
| 替换用户组权限 | `PUT /console/groups/{groupId}/permissions` |
| 获取当前用户权限树 | `GET /console/me/permissions` |
| 加载裁剪后的页面 UiSpec | `GET /console/pages/{moduleKey}/{pageKey}` |
| 权限解释 | `POST /console/permissions/explain` |

角色、用户组权限及 datasource 附加查询条件统一使用权限全量替换语义，避免增量修改后遗留脏数据。
约束只能附着在同一次提交的 DATASOURCE grant 上，不自动创建权限。接口按角色和用户组分别暴露是
为了保持管理用例清晰，底层统一写入 `PermissionGrantEntity`，不再对应多套授权或约束表。

## 12. 完整运行流程

### 12.1 模块安装和同步

```text
发现插件 Provider
  -> 注册并激活插件
  -> 校验模块菜单和页面树
  -> 按 moduleKey + pageKey 加载 UiSpec
  -> 校验 action / datasource / URL / 请求模板
  -> 管理员显式同步权限目录
```

### 12.2 管理员授权

```text
加载插件资源树
  -> 选择角色或用户组
  -> 勾选 MENU / PAGE / ACTION / DATASOURCE
  -> 为已授权 datasource 单独配置附加查询条件，或明确保持无额外限制
  -> 服务端校验并全量替换
  -> 记录审计
```

### 12.3 用户访问页面

```text
解析当前主体
  -> 合并角色和用户组 MENU / PAGE / ACTION 权限
  -> 校验页面权限
  -> 裁剪未授权 action 和 datasource
  -> 返回 UiSpec 与页面执行上下文
```

### 12.4 用户执行 datasource

```text
进入受保护 datasource 请求
  -> 读取实际 method + URL + X-Nexus-Page-Key
  -> 校验 PAGE 权限
  -> 匹配唯一 datasourceKey 并校验 DATASOURCE 权限
  -> 读取角色和用户组已保存的附加查询条件引用
  -> 将授权上下文传递给后续 Endpoint 调用链
  -> 继续执行并记录审计
```

## 13. 失败关闭与安全约束

以下情况默认拒绝加载或执行：

- 插件、模块、页面、action 或 datasource 未注册或未激活；
- UiSpec 的 `pageInfo.pageId` 与插件 Contribution 的 `pageKey` 不一致；
- action 引用未知 datasource；
- 用户加载页面时缺少 PAGE 权限；
- 用户调用 datasource 时缺少 PAGE 或 DATASOURCE 权限；
- datasource 请求不符合已注册 schema；
- 管理端附加查询条件无法保存、读取或归属到有效的 datasource grant；
- 无法解析对象归属或批量资源边界；
- 客户端提供的 URL、method 与目录不一致；
- 页面执行上下文缺失、过期或被篡改；
- 受保护接口无法确认请求拦截适配器已经调用鉴权器。

安全约束：

- 不信任客户端传入的 URL、角色、用户组、workspaceId、ownerId 或 groupIds；
- 页面执行上下文必须由服务端签发并绑定用户、项目、页面和有效期；
- 后续查询约束实现不得接受客户端删除、覆盖或扩大管理端保存的附加条件；
- 所有资源 key 和 UiSpec 摘要参与缓存版本控制，授权变更后及时失效；
- 管理员可以拥有全部 MENU、PAGE、ACTION 和 DATASOURCE，但仍受项目隔离、管理端保存的附加条件、业务 Handler 和领域不变量限制；
- 权限拒绝响应不泄露其他项目、用户组或资源的存在性信息。

## 14. 推荐包结构

UiSpec 是跨运行时复用的页面规约，统一放在 Base：

```text
com.innospots.nexus.base.ui.spec
  action
  component
  config
  datasource
  form
  layout
  loader
  parser
  table
  validation
```

Console 不定义 UiSpec 模型、解析器、加载器或权限实现；本阶段不在 `console` 下创建 permission
包。应用运行时如需 Servlet Filter 或 Jakarta REST 适配器，应在应用适配层读取实际请求并调用 Kernel
提供的框架无关拦截契约。

Kernel 仍以 `permission` 为业务域根包，再按职责组织：

```text
com.innospots.nexus.kernel.permission
  endpoint
  dao
  domain
    entity
    enums
    request
    vo
  service
  authorization
```

建议移除或替换旧方案中的以下概念：

- `PageResourceRef`
- `PageResourceRefs`
- `RequiresPermission`
- `PolicyResourceId`
- `EndpointPermissionIntrospector`
- `PermissionMetadataConvention`
- `PermissionOperation`
- Endpoint 方法级授权拦截作为页面权限的主要入口

迁移后的主要入口应是：

- Core `PluginInstallationManager`：插件安装意图、缺失对账和运行时状态来源；
- Console `ConsoleContributionCatalog`：已激活模块、菜单和页面声明来源；
- `UiSpecLoader`：按模块和页面加载 UiSpec；
- `UiSpecParser` / `UiSpecValidator`：解析、校验 action 和 datasource 引用；
- `PermissionResourceSyncService`：从已激活插件 Contribution 和 UiSpec 同步规范化资源目录；
- `PermissionGrantService`：角色/用户组授权全量替换及 datasource 约束保存；
- `PermissionVisibilityService`：按角色/用户组并集裁剪菜单、页面、action 和 datasource；
- `RequestAuthorizer`：基于实际请求的页面、datasource 和数据约束判定；
- `AuthorizationScope`：向后续查询适配器传递已保存的约束定义；
- 后续查询适配器：根据 `AuthorizationContext` 将已保存条件附加到实际数据查询中。

## 15. 验证要求

### 15.1 资源发现

- 模块菜单、页面、action 和 datasource 能从插件 Contribution 与 UiSpec 唯一生成；
- Endpoint 注解不参与资源发现；
- URL 变化但 datasource key 不变时授权关系保持；
- 重复 key、未知引用和 UiSpec `pageInfo.pageId` 不一致时激活失败；
- 目录同步幂等，移除的插件资源只失效不物理删除。

### 15.2 功能授权

- 角色权限和用户组权限按并集合并；
- 无 PAGE 权限时不能加载 UiSpec；
- 未授权 action 从 UiSpec 中移除且服务端调用仍被拒绝；
- action 不允许脱离所属 PAGE 单独生效；
- 菜单自身未授权或没有可见后代页面时自动裁剪。

### 15.3 数据权限

- 管理端可以在角色或用户组的 datasource 权限页面新增、修改、查询、清空和回显附加查询条件；
- 附加查询条件保存到对应 `PermissionGrantEntity`，不写入 UiSpec 文件，也不由后端代码写死；
- 没有配置 `constraintDefinition` 时不生成任何默认数据范围；
- 保存条件不能隐式创建 DATASOURCE grant，也不能授予未选择的 datasource 权限；
- 普通页面请求不能携带、覆盖或删除角色和用户组保存的附加查询条件；
- 当前阶段验证条件数据能够正确归属、保存和回显；具体查询字段映射、条件组合和查询拼接留待后续实现；
- 按 ID、批量更新和删除的实际数据范围处理留待后续查询适配器实现；
- 客户端直接提供 URL 或绕过页面上下文时请求失败。

### 15.4 架构约束

- Endpoint 不包含页面或权限注解；
- Service 和 Domain 不包含显式页面、角色或用户组判断；
- 策略配置不接受 SQL、脚本、类名和方法名；
- 受保护 datasource 的真实 URL 只从服务端目录读取；
- Java 结构调整后运行 `mvn clean compile`；
- 最终运行 `mvn validate`、`mvn test`、`mvn -q help:effective-pom` 和 `git diff --check`。

## 16. 设计结论

调整后的权限体系以“插件 Console Contribution 和页面 UiSpec”为唯一页面资源事实源：

```text
插件 Console Contribution 定义模块菜单和页面
  + UiSpec 定义 action、datasource 和 URL
  + 角色/用户组授予 MENU、PAGE、ACTION、DATASOURCE
  + 管理端页面保存角色/用户组针对 datasource 的附加查询条件
  + 请求拦截器调用鉴权器执行最终授权，允许后继续请求链
```

这种方式消除了 Endpoint 与页面之间的注解耦合。页面权限随 UiSpec 自动发现，业务接口保持普通
REST 契约；角色和用户组负责功能及 datasource 授权，数据边界通过管理端页面保存的附加查询条件
表达，具体查询拼接机制由后续数据访问适配器实现。业务授权仍通过独立的稳定 Handler 契约，前端
获得裁剪后的页面体验，服务端仍保留不可绕过的最终控制。
