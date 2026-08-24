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
4. **所属关系显式。** 扩展拥有模块，模块拥有菜单树和无菜单页面，目录菜单拥有子菜单，
   页面菜单拥有页面；API/操作通过注解显式引用模块及页面。
5. **页面与部署方式解耦。** 页面声明只引用逻辑入口。页面独立部署还是位于 JAR 静态
   资源中，由页面入口解析器处理，不改变页面、菜单或权限资源模型。
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
    │   ├── menu tree
    │   │   └── directory menu
    │   │       ├── directory menu
    │   │       └── page menu
    │   │           └── page
    │   │               └── pageEntryKey
    │   ├── non-menu pages
    │   └── endpoints
    │       ├── @ApiResource
    │       └── @PageActionResource
    └── ExtensionModuleDeclaration (moduleKey = "inventory")
        ├── menu tree
        ├── non-menu pages
        └── endpoints
```

关系方向固定：

```text
extensionKey -> moduleKey -> resource key
module -> menu tree
directory menu -> child menu nodes
page menu -> exactly one page
module -> page -> page entry -> resource location
endpoint operation -> API resource -> optional page references
endpoint operation -> page action -> required page reference
```

菜单与页面的关系由对象所属结构直接表达，不通过 path 相等或多个 key 的组合进行推断。

## 4. 扩展与模块模型

### 4.1 扩展描述

`ExtensionDescriptor` 描述一个可安装、可启停的扩展：

```java
public record ExtensionDescriptor(
        String extensionKey,
        String version,
        I18nObject displayName,
        String description,
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
| `description` | 扩展用途说明，不参与运行时判定 |
| `modules` | 扩展拥有的模块，不得为空；同一扩展内 `moduleKey` 不得重复 |

`extensionKey` 只用于发现、登记、启停、版本和诊断，不直接替代资源的 `moduleKey`。

### 4.2 模块描述

一个扩展可以包含多个模块：

```java
public record ExtensionModuleDeclaration(
        String moduleKey,
        I18nObject displayName,
        String description,
        List<MenuNodeDeclaration> menuTree,
        List<ConsolePageDeclaration> nonMenuPages
) {
}
```

`menuTree` 是模块的导航树。目录菜单通过 `children` 直接拥有下级菜单，页面菜单直接拥有
页面。`nonMenuPages` 保存不出现在菜单中的页面，例如详情页、弹窗页或内部跳转页。

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

## 5. 菜单、页面与加载入口

### 5.1 菜单节点类型

菜单是一棵由模块直接拥有的树。菜单节点只分为两种类型：

```java
public sealed interface MenuNodeDeclaration
        permits MenuDirectoryDeclaration, MenuPageDeclaration {

    String menuKey();

    I18nObject title();

    String icon();

    int orderIndex();
}
```

- `MenuDirectoryDeclaration` 是父菜单或分组，只能包含子菜单，不对应页面；
- `MenuPageDeclaration` 是页面菜单，必须直接包含一个页面，不能再包含子菜单。

这种类型约束保证“父菜单没有页面、叶子菜单拥有页面”。父子关系和菜单页面关系都由
对象嵌套结构表达，不使用额外字符串进行二次关联。

### 5.2 目录菜单与子菜单

目录菜单通过 `children` 直接包含下级菜单：

```java
public record MenuDirectoryDeclaration(
        String menuKey,
        I18nObject title,
        String icon,
        int orderIndex,
        List<MenuNodeDeclaration> children
) implements MenuNodeDeclaration {
}
```

`children` 可以包含下一层目录菜单，也可以包含页面菜单，因此菜单树可以表达任意合理的
目录深度。同级节点按 `orderIndex`、`menuKey` 稳定排序。目录节点必须至少包含一个有效
子节点，不能通过 route 或 page 字段指向页面。

父子关系由对象包含关系确定：模块 `menuTree` 中的节点是根菜单，
`MenuDirectoryDeclaration.children` 中的节点以该目录作为直接父菜单。注册表递归展开
菜单树时，为每个子节点生成 `parentMenuResourceId`；扩展作者不需要重复填写父菜单 key。

例如：

```text
订单管理（目录菜单，不对应页面）
├── 订单列表（页面菜单） -> OrderListPage
└── 退款管理（目录菜单，不对应页面）
    ├── 退款单（页面菜单） -> RefundListPage
    └── 退款规则（页面菜单） -> RefundRulePage
```

### 5.3 页面菜单与页面

页面菜单直接包含 `ConsolePageDeclaration`，对应关系在对象结构中一次完成：

```java
public record MenuPageDeclaration(
        String menuKey,
        I18nObject title,
        String icon,
        int orderIndex,
        ConsolePageDeclaration page
) implements MenuNodeDeclaration {
}
```

```java
public record ConsolePageDeclaration(
        String pageKey,
        I18nObject title,
        String routePath,
        String pageEntryKey
) {
}
```

| 页面字段 | 作用 |
|----------|------|
| `pageKey` | 模块内稳定页面 key，完整资源为 `page:<moduleKey>.<pageKey>` |
| `title` | 页面标题，可以与菜单展示标题不同 |
| `routePath` | Console 内部路由，由所属页面唯一声明 |
| `pageEntryKey` | 逻辑页面入口，由 `PageEntryResolver` 解析为实际资源位置 |

关系和约束如下：

- 一个 `MenuPageDeclaration` 必须且只能包含一个页面；
- 一个菜单树页面只能归属于一个页面菜单，不能在树中重复挂载；
- `menuKey` 和 `pageKey` 分别是菜单身份和页面身份，可以不同；
- 菜单点击后直接使用其页面的 `routePath`，不再声明独立 route；
- 页面加载时使用 `pageEntryKey`，与菜单层级无关；
- 页面菜单是叶子节点，不能再拥有 `children`。

例如 `menu:sales.order-list` 直接包含 `page:sales.order-list`。运行时从页面菜单即可得到
菜单标题、页面路由和页面入口，不需要查询另一张关联表。

注册后的规范化关系如下：

```text
menu:sales.order
  type = DIRECTORY
  parent = null

menu:sales.order-list
  type = PAGE
  parent = menu:sales.order
  page = page:sales.order-list
```

### 5.4 无菜单页面

详情页、弹窗页、向导页等不需要显示在导航树中的页面，由模块的 `nonMenuPages`
直接持有。无菜单页面仍具有 PAGE 资源、路由和页面入口，可以被 API/ACTION 引用，但不会
产生 MENU 资源。

### 5.5 页面资源位置

页面声明不直接保存 JAR 路径或远程 URL。扩展另外提供 `PageEntryResolver`，把
`pageEntryKey` 解析为实际位置：

```java
public interface PageEntryResolver {

    Optional<PageResourceLocation> resolve(String pageEntryKey);
}
```

`PageResourceLocation` 至少支持：

- `EmbeddedPageResource`：位于扩展 JAR 中的静态资源入口；
- `RemotePageResource`：独立部署的前端入口；
- 后续新增的加载协议，由适配器扩展，不修改页面声明。

因此同一个 `ConsolePageDeclaration` 可以在不同部署环境解析为不同位置。环境配置只可
参与逻辑入口到实际部署位置的映射，不负责新增业务页面或重写页面身份。

### 5.6 前端加载流程

1. Console 前端请求项目的活动扩展清单；
2. 服务端递归展开目录菜单和页面菜单，并合并无菜单页面的路由；
3. 前端根据管理端权限模块返回的可见资源集合过滤菜单和路由；
4. 用户进入路由时，页面加载适配器解析 `pageEntryKey`；
5. JAR 静态资源由内嵌资源适配器加载，独立部署页面由远程资源适配器加载；
6. 加载失败只影响目标页面，并记录 `extensionKey/moduleKey/pageKey` 诊断信息。

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

    Collection<PageEntryResolver> pageEntryResolvers();
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
3. 构建并校验菜单树；
4. 递归校验目录/页面菜单结构、页面 route 和 `pageEntryKey`；
5. 注册 Endpoint，并读取 API/ACTION 注解；
6. 校验 API/ACTION 的模块和页面引用；
7. 解析页面入口，生成 Console 活动清单；
8. 原子发布扩展资源到活动注册表。

任一步失败，整个扩展保持 `FAILED`，不发布部分菜单或部分接口。其他已激活扩展不受影响。

### 8.4 停用与重新启用

停用扩展时：

- 从活动菜单、路由、页面入口和 Endpoint 注册表撤出该扩展；
- 从活动资源视图撤出其模块资源；
- 保留扩展登记、资源快照和权限模块中的既有分配；
- 新请求不能再进入已停用扩展，执行中的请求按运行时适配器策略完成或终止。

重新启用必须重新执行完整激活校验。只有原子发布成功后，状态才变为 `ACTIVE`。
若底层 REST 运行时或静态资源容器不支持安全的动态卸载，管理端仍保存启停状态，但界面
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
| 应用/运行时适配器 | 接口装配、注解索引、Java SPI、Jakarta REST 注册、JAR 静态资源挂载、远程页面加载 |
| 业务扩展 JAR | Provider 实现、模块声明、页面入口解析器、Endpoint 与前端产物 |

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
                "ERP console extension",
                List.of(salesModule(), inventoryModule()));
    }

    private ExtensionModuleDeclaration salesModule() {
        return new ExtensionModuleDeclaration(
                "sales",
                I18nObject.of("en", "Sales", "zh", "销售"),
                "Sales management",
                List.of(new MenuDirectoryDeclaration(
                        "order",
                        I18nObject.of("en", "Orders", "zh", "订单"),
                        "orders",
                        20,
                        List.of(new MenuPageDeclaration(
                                "order-list",
                                I18nObject.of("en", "Order List", "zh", "订单列表"),
                                "list",
                                10,
                                new ConsolePageDeclaration(
                                        "order-list",
                                        I18nObject.of("en", "Order List", "zh", "订单列表"),
                                        "/sales/orders",
                                        "sales.order-list"))))),
                List.of(new ConsolePageDeclaration(
                        "order-detail",
                        I18nObject.of("en", "Order Detail", "zh", "订单详情"),
                        "/sales/orders/:orderId",
                        "sales.order-detail")));
    }

    private ExtensionModuleDeclaration inventoryModule() {
        return new ExtensionModuleDeclaration(
                "inventory",
                I18nObject.of("en", "Inventory", "zh", "库存"),
                "Inventory management",
                List.of(),
                List.of());
    }
}
```

### 11.2 页面资源

同一逻辑入口可以由不同 Provider 解析：

```java
// JAR 内置静态资源
EmbeddedPageResource.of("sales.order-list", "/META-INF/nexus/pages/sales/index.html");

// 独立部署资源
RemotePageResource.of("sales.order-list", URI.create("https://sales.example.com/entry.js"));
```

页面和菜单声明无需因部署方式变化而修改。

### 11.3 Endpoint 资源

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

### 11.4 安装和加载

1. 扩展工程实现 Provider，并通过接口装配、发现注解或 Java SPI 暴露；
2. 把扩展 JAR 加入应用依赖，随应用发布；
3. 应用启动时发现 Provider，登记 `com.innospots.erp`，首次默认启用；
4. 激活器校验 `sales`、`inventory` 模块及其全部资源；
5. REST 适配器注册 Endpoint，页面入口适配器注册逻辑入口；
6. Console 获取活动清单并加载菜单、路由和页面；
7. 管理员在权限模块中为资源配置权限并分配给角色或用户组；
8. 管理员可在扩展管理中停用或重新启用整个扩展。

## 12. 校验与冲突处理

注册或激活阶段至少执行以下校验：

- `extensionKey`、版本和模块列表合法；
- 全局 `moduleKey` 唯一；
- 同一模块内 menu/page/api/action 局部 key 唯一；
- 规范化后的 `type:key` 全局唯一；
- 目录菜单至少包含一个子节点，菜单树无循环且排序稳定；
- 页面菜单直接包含且只包含一个页面，不能同时拥有子节点；
- 菜单树页面不能重复挂载，也不能与 `nonMenuPages` 中的页面重复；
- 页面 routePath 合法且在活动页面中唯一；
- `pageEntryKey` 可被且只能被一个活动页面入口解析器解析；
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
- 多级目录菜单、页面菜单和无菜单页面正确展开；
- 目录菜单不能对应页面，页面菜单必须对应一个页面；
- API 和 ACTION 的页面引用正确解析；
- Embedded/Remote 页面资源解析产生相同逻辑页面身份。

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
