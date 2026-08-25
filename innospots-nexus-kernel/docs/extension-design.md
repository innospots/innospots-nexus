# Nexus 扩展机制设计

## 1. 文档定位

本文定义 Nexus 管理平台扩展机制。它说明扩展如何开发、被系统发现和注册、
由管理端启用或停用，以及扩展贡献的模块、菜单、页面、前端资源、API 和页面操作如何
形成一棵可校验、可加载、可授权的资源树。

## 2. 设计原则

1. **代码是扩展事实源。** 菜单、页面和接口资源通过 Java 接口、发现注解或 Java SPI
   暴露；配置文件不作为业务资源的主要定义方式。
2. **安装与启停分离。** 把扩展 JAR 加入应用依赖或运行时 classpath 即完成安装；系统
   启动时发现并注册扩展，管理端控制是否启用。
3. **扩展与模块分层。** 一个扩展是安装和启停单元，一个扩展可以贡献多个
   `moduleKey`；模块是资源归属和权限目录的一级边界。
4. **所属关系显式。** 扩展拥有模块，模块分别拥有页面树和菜单树；页面树表达页面领域
   关系，菜单页面节点通过 `pageKey` 引用模块页面，API/操作通过注解引用页面。
5. **声明与渲染解耦。** 扩展只声明 `pageKey` 和 `pagePath`。渲染模块通过
   `moduleKey + pageKey` 定位 DSL 文件，扩展不保存 DSL 内容或文件路径。
6. **资源声明与权限分配分离。** 扩展只声明资源身份、结构和展示元数据，不声明权限码、
   角色、用户组或授权策略。权限模块在管理端统一配置和分配。
7. **显式、稳定、失败关闭。** 所有资源使用稳定 key；重复、悬空引用或无法解析的加载
   入口会阻止该扩展激活，不采用后者覆盖前者。

## 3. 总体结构

```text
ExtensionDescriptor
├── extensionKey
├── version
└── modules
    ├── ExtensionModuleDeclaration (moduleKey = "sales")
    │   ├── page tree
    │   │   └── PageDslDeclaration
    │   │       ├── pageKey
    │   │       ├── pagePath
    │   │       └── children
    │   ├── menu tree
    │   │   └── MenuDeclaration
    │   │       ├── children
    │   │       └── optional pageKey reference
    │   └── endpoints
    │       ├── @ApiResource
    │       └── @PageActionResource
    └── ExtensionModuleDeclaration (moduleKey = "inventory")
        ├── page tree
        ├── menu tree
        └── endpoints
```

关系方向固定：

```text
extensionKey -> moduleKey -> resource key
module -> page tree
page -> child pages
module -> menu tree
menu -> child menu nodes or pageKey reference
moduleKey + pageKey -> DSL renderer
endpoint operation -> API resource -> optional page references
endpoint operation -> page action -> required page reference
```

页面先归属于模块，菜单再通过 `pageKey` 引用模块页面。`pagePath` 只负责路由匹配，不参与
菜单和页面的关联。

## 4. 扩展与模块模型

### 4.1 扩展描述

`ExtensionDescriptor` 描述一个可安装、可启停的扩展：

```java
public record ExtensionDescriptor(
        String extensionKey,
        String version,
        I18nObject displayName,
        I18nObject description,
        List<ExtensionModuleDeclaration> modules
) {
}
```

字段约束：

| 字段 | 约束 |
|------|------|
| `extensionKey` | 全局唯一、发布后稳定，建议使用反向域名，如 `com.innospots.erp` |
| `version` | 扩展版本，用于诊断和兼容性校验，不参与资源身份 |
| `displayName` | 管理端展示名，可国际化，可随版本变化 |
| `description` | 国际化扩展用途说明，不参与运行时判定 |
| `modules` | 扩展拥有的模块，不得为空；同一扩展内 `moduleKey` 不得重复 |

`extensionKey` 只用于发现、登记、启停、版本和诊断，不直接替代资源的 `moduleKey`。

### 4.2 模块描述

一个扩展可以包含多个模块：

```java
public record ExtensionModuleDeclaration(
        String moduleKey,
        I18nObject displayName,
        I18nObject description,
        List<PageDslDeclaration> pages,
        List<MenuDeclaration> menuTree
) {
}
```

`pages` 是模块的页面资源树，定义模块拥有哪些平台页面以及页面之间的单向父子关系。
`menuTree` 是模块的导航树，只引用 `pages` 中需要出现在菜单里的页面。页面是否出现在
菜单中，不影响其 MODULE/PAGE 资源归属。

扩展和模块的 `displayName`、`description` 都使用 `I18nObject`。注册表保留完整国际化
内容，管理端输出时再按请求语言解析，不在扩展发现阶段转换为单一字符串。

`moduleKey` 是全局稳定的资源命名空间。例如扩展 `com.innospots.erp` 可以贡献
`sales` 和 `inventory` 两个模块。系统首先登记模块资源：

```text
module:sales
module:inventory
```

模块只表达资源归属。它不携带权限码，也不说明哪个角色可以访问。

### 4.3 统一资源身份

资源 ID 由资源类型和稳定 key 组成，子资源 key 必须包含 `moduleKey`：

| 类型 | 示例 | 含义 |
|------|------|------|
| MODULE | `module:sales` | 模块资源边界 |
| MENU | `menu:sales.order` | 菜单目录或页面导航入口 |
| PAGE | `page:sales.order-list` | 可加载页面 |
| API | `api:sales.order.query` | 后端接口操作 |
| ACTION | `action:sales.order-list.export` | 页面操作 |

推荐在代码内部使用 `ResourceId(type, key)` 值对象，而不是到处传递拼接后的字符串。
序列化和管理端展示时再输出 `type:key`。

## 5. 页面 DSL、页面关系与菜单

### 5.1 模块页面声明

模块使用 `PageDslDeclaration` 声明平台页面：

```java
public record PageDslDeclaration(
        String pageKey,
        String pagePath,
        List<PageDslDeclaration> children
) {
}
```

| 字段 | 作用 |
|------|------|
| `pageKey` | 模块内唯一页面标识，同时对应 DSL 配置文件中的 `pageKey` |
| `pagePath` | Console 页面路径模板，用于匹配浏览器路径和提取变量 |
| `children` | 子页面声明，表达父页面到子页面的单向领域关系 |

模块内页面资源 ID 为 `page:<moduleKey>.<pageKey>`。`pageKey` 发布后保持稳定；
`pagePath` 可以随页面导航结构调整，但变更时必须避免与其他活动页面产生匹配冲突。

扩展声明不包含 DSL 内容，也不保存 `dslPath`。渲染模块通过 `moduleKey + pageKey` 定位唯一
DSL 文件，并校验文件内部的 `pageKey` 与声明一致。

DSL 文件既可以来自扩展 JAR 的静态资源，也可以由独立部署的页面服务提供。具体来源、
地址和加载协议由渲染运行时管理，不进入扩展声明；两种部署方式使用相同的
`moduleKey + pageKey` 页面身份和查询契约。

### 5.2 页面树与声明边界

模块 `pages` 中的节点是页面树根节点，`PageDslDeclaration.children` 直接包含子页面。
每个声明页面最多只有一个父页面；同一个 `pageKey` 不得在页面树中重复出现。注册表递归
展开页面树后，为子页面生成 `parentPageResourceId`。

页面父子关系表达领域归属，不从 `pagePath` 推导，也不强制子页面路径必须以前端父页面
路径为前缀。路径结构与领域关系分别校验。

```text
page:sales.order-list
  parent = null

page:sales.order-detail
  parent = page:sales.order-list

page:sales.order-edit
  parent = page:sales.order-detail
```

只有具有独立 DSL `pageKey`、需要成为独立 PAGE 资源的页面才进入页面树。一个 DSL 页面
内部的弹窗、抽屉、页签、局部组件和内部视图不需要声明，也不产生 PAGE 资源。页面之间
通过前端链接跳转，不要求它们出现在菜单中。

### 5.3 `pagePath` 路径模板

`pagePath` 使用命名路径变量，不使用 `*` 或 `**`：

```text
/sales/orders
/sales/orders/{orderId}
/sales/orders/{orderId}/items/{itemId}
```

路径模板约束：

- 必须以 `/` 开头；
- `{variableName}` 必须占据完整路径段；
- 变量名不能为空，同一模板内不能重复；
- 静态路径优先于变量模板，例如 `/orders/create` 优先于 `/orders/{orderId}`；
- `/orders/{id}` 与 `/orders/{orderId}` 具有相同结构，视为冲突；
- 具有相同匹配优先级且可能匹配同一路径的模板视为冲突；
- 路径变量经过一次 URL 解码后，以只读 `Map<String, String>` 传给渲染模块。

例如访问 `/sales/orders/ORD-1001` 时，页面注册表匹配
`/sales/orders/{orderId}`，得到：

```text
moduleKey = sales
pageKey = order-detail
pathVariables = {
  orderId: "ORD-1001"
}
```

### 5.4 菜单树与页面引用

菜单树使用一个递归声明类型。目录节点通过 `children` 包含子菜单，页面节点通过
`pageKey` 引用模块 `pages` 中已经声明的页面：

```java
public record MenuDeclaration(
        String menuKey,
        I18nObject title,
        String icon,
        int orderIndex,
        String pageKey,
        List<MenuDeclaration> children
) {
}
```

`pageKey` 是可选页面引用；`children` 缺省时规范化为空列表。节点类型不再使用接口、枚举
或不同 record 表达，而是由字段组合确定。注册表可以派生 `DIRECTORY` 或 `PAGE` 类型，
但扩展作者不需要声明该类型：

| `pageKey` | `children` | 节点语义 |
|-----------|------------|----------|
| 空 | 非空 | 目录节点，只负责组织子菜单 |
| 非空 | 空 | 页面节点，引用一个模块页面 |
| 空 | 空 | 非法，没有导航意义 |
| 非空 | 非空 | 非法，不能同时作为目录和页面入口 |

页面和菜单是两棵独立结构：

- 模块 `pages` 决定页面资源归属和页面父子关系；
- 模块 `menuTree` 决定导航父子关系；
- `MenuDeclaration.pageKey` 必须引用同一模块中存在的页面；
- 没有菜单引用的页面仍是合法 PAGE 资源，可由其他页面链接进入；
- 一个页面可以被至多一个菜单页面节点引用，避免产生重复导航入口；
- 带必填路径变量的页面不能直接作为静态菜单入口，因为静态菜单无法提供变量值。

规范化关系示例：

```text
menu:sales.order
  type = DIRECTORY
  parent = null

menu:sales.order-list
  type = PAGE
  parent = menu:sales.order
  page = page:sales.order-list
```

### 5.5 DSL 查询与渲染

页面注册表以 `(moduleKey, pageKey)` 为唯一键保存 `PageDslDeclaration`。匹配页面路径后，
宿主向渲染模块提交：

```java
public record PageRenderRequest(
        String moduleKey,
        String pageKey,
        Map<String, String> pathVariables
) {
}
```

`PageDslDeclaration` 只定义资源身份、领域关系和路径模板。页面标题、描述等展示元数据由
渲染模块读取 DSL 后提供给资源目录，不在扩展声明中重复维护。

渲染顺序：

1. Console 获取已激活扩展的模块、页面树和菜单树；
2. 权限模块根据 PAGE/MENU 资源过滤用户可访问的导航与页面；
3. 页面注册表匹配 `pagePath`，确定 `moduleKey + pageKey`；
4. 注册表提取命名路径变量，构造 `PageRenderRequest`；
5. 渲染模块按 `moduleKey + pageKey` 查询唯一 DSL 文件；
6. 渲染模块校验 DSL 文件内部 `pageKey`，并注入 `pathVariables`；
7. DSL 文件不存在、pageKey 不一致或渲染失败时，记录模块、页面和路径变量诊断。

## 6. API 与页面操作资源

### 6.1 专用声明注解

API 资源必须通过独立的 `@ApiResource` 显式声明：

```java
@ApiResource(
        moduleKey = "sales",
        apiKey = "order.query",
        name = "Query orders",
        pages = {"order-list"}
)
@GET
@Path("/orders")
public R<PageResult<OrderVo>> list(...) {
    // ...
}
```

页面操作使用独立的 `@PageActionResource`：

```java
@PageActionResource(
        moduleKey = "sales",
        pageKey = "order-list",
        actionKey = "export",
        name = "Export orders"
)
```

一个 Endpoint 方法可以同时声明 API 和页面操作资源。二者的区别是：

- API 表示一次可识别、可授权的后端调用；
- ACTION 表示页面上可见或可执行的交互能力；
- `pages`、`pageKey` 只表达归属关系，不代表已获得访问权限；
- 独立 DSL 子页面使用自己的 `pageKey`；DSL 内部组件使用所属页面的 `pageKey`；
- 不允许根据 HTTP method、URL 或 Java 方法名自动生成资源 key；
- 未声明 `@ApiResource` 的 Endpoint 操作不会作为 API 资源进入权限资源目录。

### 6.2 接口注册与调用

扩展贡献源同时对外提供 Endpoint 类型或 Endpoint 实例。运行时适配器负责把它们注册到
Jakarta REST 容器。资源扫描器读取 Endpoint 方法上的专用注解，形成 API/ACTION 资源，
并校验其 `moduleKey` 和 `pageKey`。

页面调用接口时仍使用标准 HTTP API。`@ApiResource` 的职责是提供稳定资源身份、建立
页面关联，并为权限拦截器提供查找入口；它不替代 Jakarta REST 路由，也不生成前端客户
端。权限拦截器根据被调用的 Endpoint 操作解析出 API 资源 ID，再查询权限模块的分配结果。

## 7. 三种代码发现入口

系统支持接口装配、发现注解和 Java SPI 三种入口。它们只解决“如何找到扩展贡献源”，
最终必须归一化为同一个 `ConsoleExtensionProvider` 契约，不能形成三套资源模型。

```java
public interface ConsoleExtensionProvider {

    ExtensionDescriptor descriptor();

    Collection<Class<?>> endpointTypes();
}
```

### 7.1 接口装配

应用或运行时适配器直接把 `ConsoleExtensionProvider` 实例交给发现器。适用于内建模块、
测试和已有依赖注入容器的应用。

### 7.2 发现注解

扩展入口类型使用专用发现注解标记。注解只标记入口和必要身份，不把复杂菜单、页面清单
全部塞进注解属性；发现器获得实例后仍调用统一 Provider 接口。

注解扫描由应用适配器或构建期索引完成。`innospots-nexus-console` 不绑定 Spring Boot
自动配置，也不依赖不受控的全 classpath 反射扫描。

### 7.3 Java SPI

独立扩展 JAR 可通过 `ServiceLoader<ConsoleExtensionProvider>` 发布 Provider。JAR 被加入
应用依赖后，系统启动时即可发现。SPI 是默认的跨运行时插件入口。

SPI 文件位于扩展 JAR 的
`META-INF/services/com.innospots.nexus.console.extension.ConsoleExtensionProvider`，内容为
Provider 实现类的全限定名。推荐使用构建期工具生成该文件，避免手写类名漂移。

### 7.4 去重规则

三种入口可能发现同一个 Provider：

- 相同 Provider 实例或相同实现类型只归一化一次；
- 同一 `extensionKey` 对应内容等价的重复来源只保留一份并记录来源；
- 同一 `extensionKey` 对应不同描述符时视为冲突，相关扩展不得激活；
- 不允许通过“后发现覆盖先发现”解决冲突。

## 8. 安装、注册、启停与激活

### 8.1 生命周期

```text
JAR 加入依赖/classpath（安装）
              │
              ▼
启动发现 DISCOVERED
              │
              ▼
登记 REGISTERED ── 首次发现默认 enabled=true
              │
       读取管理端期望状态
        ┌─────┴─────┐
        ▼           ▼
    DISABLED     校验与装配
                    │
              ┌─────┴─────┐
              ▼           ▼
           ACTIVE        FAILED
```

“已安装”“期望启用”和“实际激活”是三个不同事实：

- **已安装**：启动时能在运行时依赖中发现扩展；
- **期望启用**：扩展管理中保存的 `enabled` 状态；
- **实际激活**：本次运行中发现、校验和装配全部成功。

首次发现一个 `extensionKey` 时自动登记并默认启用。若管理端曾将其停用，下次启动即使
仍发现 JAR，也必须保持停用。若 JAR 被移除，登记记录标记为 `MISSING`，不能直接删除，
以保留诊断信息和已有权限分配关系。

### 8.2 注册快照

注册阶段保存扩展身份、版本、来源、模块清单摘要、发现时间和期望启用状态。禁用扩展仍
可以在扩展管理中查看，但其菜单、页面、Endpoint 和活动资源不进入运行时。

### 8.3 原子激活

激活顺序固定：

1. 校验扩展描述和兼容版本；
2. 校验全部 `moduleKey` 及资源 ID 唯一性；
3. 构建页面树并校验 `pageKey`、父子关系和 `pagePath` 模板；
4. 构建菜单树并校验菜单引用的 `pageKey`；
5. 注册 Endpoint，并读取 API/ACTION 注解；
6. 校验 API/ACTION 的模块和页面引用；
7. 由渲染模块校验 `moduleKey + pageKey` 可定位 DSL 且内部 pageKey 一致；
8. 原子发布扩展资源到活动注册表。

任一步失败，整个扩展保持 `FAILED`，不发布部分菜单或部分接口。其他已激活扩展不受影响。

### 8.4 停用与重新启用

停用扩展时：

- 从活动页面、菜单、路径模板和 Endpoint 注册表撤出该扩展；
- 从活动资源视图撤出其模块资源；
- 保留扩展登记、资源快照和权限模块中的既有分配；
- 新请求不能再进入已停用扩展，执行中的请求按运行时适配器策略完成或终止。

重新启用必须重新执行完整激活校验。只有原子发布成功后，状态才变为 `ACTIVE`。
若底层 REST 运行时或 DSL 渲染模块不支持安全的动态卸载，管理端仍保存启停状态，但界面
必须明确提示“重启后生效”，不能伪装成已完成热停用。

## 9. 权限模块边界

扩展只向统一资源目录提供以下事实：

- 资源 ID、类型、名称和所属 `moduleKey`；
- 菜单父子关系、页面与菜单关系；
- API 与页面关系、ACTION 与页面关系；
- 扩展是否处于活动状态。

扩展不得声明：

- 权限码或内建角色；
- 哪个用户、角色或用户组可以访问；
- 默认授权、拒绝策略或数据范围；
- 因部署方式而变化的权限规则。

权限模块通过管理端统一把 MODULE、MENU、PAGE、API、ACTION 资源与权限及授权主体建立
关系。`module:<moduleKey>` 使管理端可以按模块组织或批量配置资源，但是否支持模块级继承、
默认拒绝或其他授权语义，属于权限模块设计，不由扩展决定。

扩展停用时不删除权限分配；重新启用并发现相同稳定资源 ID 后可以继续使用既有分配。

## 10. 模块与运行时职责

| 模块/适配层 | 职责 |
|-------------|------|
| `innospots-nexus-console` | 扩展、模块、菜单、页面、资源注解和 Provider 契约；保持业务中立 |
| `innospots-nexus-kernel` | 扩展登记、启停管理、资源目录、权限管理及相应管理 Endpoint |
| 应用/运行时适配器 | 接口装配、注解索引、Java SPI、Jakarta REST 注册和 DSL 渲染模块接入 |
| DSL 渲染模块 | 按 `moduleKey + pageKey` 定位 DSL、校验 DSL pageKey、注入路径变量并渲染页面 |
| 业务扩展 JAR | Provider 实现、模块/页面/菜单声明和 Endpoint |

`innospots-nexus-core` 可以提供业务中立的生命周期、注册表或资源解析基础设施，但不能
内置具体扩展管理业务，也不能绑定 Spring Boot 自动配置。

## 11. 完整接入示例

### 11.1 扩展贡献

```java
@ConsoleExtensionEntry
public final class ErpConsoleExtension implements ConsoleExtensionProvider {

    @Override
    public ExtensionDescriptor descriptor() {
        return new ExtensionDescriptor(
                "com.innospots.erp",
                "1.0.0",
                I18nObject.of("en", "ERP", "zh", "企业资源管理"),
                I18nObject.of(
                        "en", "ERP console extension",
                        "zh", "企业资源管理控制台扩展"),
                List.of(salesModule(), inventoryModule()));
    }

    private ExtensionModuleDeclaration salesModule() {
        return new ExtensionModuleDeclaration(
                "sales",
                I18nObject.of("en", "Sales", "zh", "销售"),
                I18nObject.of(
                        "en", "Sales management",
                        "zh", "销售管理"),
                List.of(new PageDslDeclaration(
                        "order-list",
                        "/sales/orders",
                        List.of(new PageDslDeclaration(
                                "order-detail",
                                "/sales/orders/{orderId}",
                                List.of(new PageDslDeclaration(
                                        "order-edit",
                                        "/sales/orders/{orderId}/edit",
                                        List.of())))))),
                List.of(new MenuDeclaration(
                        "order",
                        I18nObject.of("en", "Orders", "zh", "订单"),
                        "orders",
                        20,
                        null,
                        List.of(new MenuDeclaration(
                                "order-list",
                                I18nObject.of("en", "Order List", "zh", "订单列表"),
                                "list",
                                10,
                                "order-list",
                                List.of())))));
    }

    private ExtensionModuleDeclaration inventoryModule() {
        return new ExtensionModuleDeclaration(
                "inventory",
                I18nObject.of("en", "Inventory", "zh", "库存"),
                I18nObject.of(
                        "en", "Inventory management",
                        "zh", "库存管理"),
                List.of(),
                List.of());
    }
}
```

### 11.2 页面树与路径变量

上述模块声明形成页面资源树：

```text
page:sales.order-list
└── page:sales.order-detail
    └── page:sales.order-edit
```

访问 `/sales/orders/ORD-1001/edit` 时匹配 `order-edit` 页面，渲染请求为：

```text
moduleKey = sales
pageKey = order-edit
pathVariables = {
  orderId: "ORD-1001"
}
```

渲染模块使用 `sales + order-edit` 定位 DSL 文件，并把 `orderId` 注入渲染上下文。菜单只
引用 `order-list`；详情和编辑页面无需菜单，也不影响其 PAGE 资源归属。

### 11.3 页面内部视图

如果订单 DSL 内部还有弹窗、抽屉、页签或不具备独立 DSL `pageKey` 的视图，这些内容不
进入 `PageDslDeclaration.children`，也不生成 PAGE 资源。

### 11.4 Endpoint 资源

```java
@Path("/sales/orders")
public class OrderEndpoint {

    @GET
    @ApiResource(
            moduleKey = "sales",
            apiKey = "order.query",
            name = "Query orders",
            pages = {"order-list"})
    public R<PageResult<OrderVo>> list() {
        // ...
    }

    @POST
    @Path("/export")
    @ApiResource(
            moduleKey = "sales",
            apiKey = "order.export",
            name = "Export orders",
            pages = {"order-list"})
    @PageActionResource(
            moduleKey = "sales",
            pageKey = "order-list",
            actionKey = "export",
            name = "Export orders")
    public R<Void> export() {
        // ...
    }
}
```

### 11.5 安装和加载

1. 扩展工程实现 Provider，并通过接口装配、发现注解或 Java SPI 暴露；
2. 把扩展 JAR 加入应用依赖，随应用发布；
3. 应用启动时发现 Provider，登记 `com.innospots.erp`，首次默认启用；
4. 激活器校验 `sales`、`inventory` 模块及其全部资源；
5. REST 适配器注册 Endpoint，DSL 渲染模块校验模块页面；
6. Console 获取活动页面树、菜单树和路径模板注册表；
7. 管理员在权限模块中为资源配置权限并分配给角色或用户组；
8. 管理员可在扩展管理中停用或重新启用整个扩展。

## 12. 校验与冲突处理

注册或激活阶段至少执行以下校验：

- `extensionKey`、版本和模块列表合法；
- 全局 `moduleKey` 唯一；
- 同一模块内 menu/page/api/action 局部 key 唯一；
- 规范化后的 `type:key` 全局唯一；
- 页面树无循环，每个页面最多一个父页面；
- `pageKey` 与 DSL 文件内部 pageKey 一致；
- `pagePath` 以 `/` 开头，路径变量合法且不重复；
- 静态/变量模板匹配优先级确定，不存在结构相同或同优先级歧义模板；
- 菜单节点必须在“目录节点”和“页面节点”两种有效字段组合中选择一种；
- 菜单树无循环、目录节点至少包含一个子节点且同级排序稳定；
- 页面节点引用的 `pageKey` 存在于同一模块，且一个页面最多被一个菜单引用；
- 包含必填路径变量的页面不能直接作为静态菜单入口；
- Endpoint 可以被 REST 运行时注册；
- `@ApiResource.moduleKey` 指向所属扩展拥有的模块；
- API 的 `pages` 和 ACTION 的 `pageKey` 指向已声明页面；
- 同一 Endpoint 操作不存在冲突资源声明；
- 扩展版本与宿主契约版本兼容。

冲突一律失败关闭。错误信息必须包含来源、`extensionKey`、`moduleKey`、资源类型和资源 key，
以便扩展管理页面直接展示诊断。

## 13. 测试要求

### 13.1 契约测试

- Provider 返回集合不可变，必填字段校验明确；
- 一个扩展可贡献多个模块；
- 资源 ID 规范化稳定；
- 页面树正确生成单向父子资源关系；
- 菜单树正确引用模块页面，未被菜单引用的页面仍保留 PAGE 资源；
- 目录节点和页面节点的有效字段组合可以正确识别，其他组合激活失败；
- DSL 内部组件和视图不产生独立 PAGE 资源；
- 静态路径优先于变量模板；
- 路径变量正确提取、URL 解码并以不可变 Map 传递；
- 结构相同或同优先级歧义的路径模板失败关闭；
- 渲染模块可以通过 `moduleKey + pageKey` 唯一定位 DSL；
- 声明 pageKey 与 DSL 内部 pageKey 不一致时激活失败；
- API 和 ACTION 的页面引用正确解析。

### 13.2 发现与生命周期测试

- 接口、发现注解和 Java SPI 均归一化为相同 Provider；
- 多入口重复发现同一扩展只登记一次；
- 首次发现默认启用；
- 已持久化停用状态在重启后保持；
- 激活失败不发布部分资源；
- 停用撤出活动资源但保留登记和权限分配；
- JAR 缺失时登记记录进入 `MISSING`。

### 13.3 资源与权限边界测试

- 扩展声明不包含权限码、角色或授权规则；
- API/ACTION 仅从专用注解产生；
- 未知模块、未知页面和重复资源失败关闭；
- 权限分配不因扩展停用或短暂缺失而被删除；
- 恢复相同资源 ID 后可以继续关联既有分配。
