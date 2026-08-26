# Nexus 扩展机制设计

## 1. 文档定位

本文定义 Nexus 管理平台扩展机制。它说明扩展如何开发、被系统发现和注册、
由管理端启用或停用，以及扩展贡献的模块、菜单、页面和页面接口如何形成可校验、
可加载、可授权的资源结构。

## 2. 设计原则

1. **代码是扩展结构事实源。** 扩展、模块、页面和菜单通过 Java 接口、发现注解或
   Java SPI 暴露；页面使用的接口 URL 来自对应页面 DSL。
2. **安装与启停分离。** 把扩展 JAR 加入应用依赖或运行时 classpath 即完成安装；系统
   启动时发现并注册扩展，管理端控制是否启用。
3. **扩展与模块分层。** 一个扩展是安装和启停单元，一个扩展可以贡献多个
   `moduleKey`；模块是资源归属和权限目录的一级边界。
4. **所属关系显式。** 扩展拥有模块，模块分别拥有页面树和菜单树；页面树表达页面领域
   关系，菜单页面节点通过 `pageKey` 引用模块页面，页面 DSL 中的 URL 引用归属于该页面。
5. **声明与渲染解耦。** 扩展只声明 `pageKey` 和 `pagePath`。渲染模块通过
   `moduleKey + pageKey` 定位 DSL 文件，扩展不保存 DSL 内容或文件路径。
6. **资源声明与权限分配分离。** 扩展和页面 DSL 只提供资源事实，不声明角色、用户组或
   授权策略。权限模块在管理端统一为页面及页面 URL 分配角色权限。
7. **显式、稳定、失败关闭。** 模块、菜单和页面使用稳定 key，页面接口使用明确的页面
   与 URL 复合身份；重复、悬空引用或无法解析的加载入口会阻止该扩展激活。

## 3. 总体结构

```text
ConsoleExtensionProvider
├── ExtensionDescriptor
│   ├── extensionKey
│   ├── version
│   └── modules
│       ├── ExtensionModuleDeclaration (moduleKey = "sales")
│       │   ├── page tree
│       │   │   └── PageDslDeclaration
│       │   │       ├── pageKey
│       │   │       ├── pagePath
│       │   │       └── children
│       │   └── menu tree
│       │       └── MenuDeclaration
│       │           ├── children
│       │           └── optional pageKey reference
│       └── ExtensionModuleDeclaration (moduleKey = "inventory")
│           ├── page tree
│           └── menu tree
└── endpointTypes (standard Jakarta REST)
```

关系方向固定：

```text
extensionKey -> moduleKey -> resource key
module -> page tree
page -> child pages
module -> menu tree
menu -> child menu nodes or pageKey reference
moduleKey + pageKey -> DSL renderer
page DSL -> referenced URLs
qualified pageKey + request URL -> role permission check
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

模块、菜单和页面资源 ID 由资源类型和稳定 key 组成，子资源 key 必须包含 `moduleKey`：

| 类型 | 示例 | 含义 |
|------|------|------|
| MODULE | `module:sales` | 模块资源边界 |
| MENU | `menu:sales.order` | 菜单目录或页面导航入口 |
| PAGE | `page:sales.order-list` | 可加载页面 |

推荐在代码内部使用 `ResourceId(type, key)` 值对象，而不是到处传递拼接后的字符串。
序列化和管理端展示时再输出 `type:key`。

页面接口权限使用 `(moduleKey, pageKey, urlPattern)` 作为唯一身份。URL 发生变化时视为
新的页面接口权限项。

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
6. 渲染模块校验 DSL 文件内部 `pageKey`，登记该页面引用的接口 URL，并注入
   `pathVariables`；
7. DSL 文件不存在、pageKey 不一致、URL 引用无效或渲染失败时，记录模块、页面和路径
   变量诊断。

## 6. 页面接口权限

### 6.1 页面 URL 权限项

页面 DSL 中的 datasource 和 action 可以引用后端接口 URL。系统加载页面 DSL 后，按页面
登记这些 URL，并在管理端提供角色权限配置。本文只定义权限协作方式，不定义 DSL 内部
字段结构。

一个页面 URL 权限项由以下内容唯一确定：

- `moduleKey`；
- `pageKey`；
- 规范化后的 URL 模板。

例如 `sales` 模块的 `order-list` 页面引用 `/sales/orders`，对应的权限身份为：

```text
(sales, order-list, /sales/orders)
```

同一 URL 被不同页面引用时，分别形成各自页面下的权限项。一个页面中的 datasource 和
action 如果引用同一 URL，则共用同一权限项。权限只按 URL 路径区分，HTTP method、
Java 方法和 action 类型不参与权限身份。

URL 模板使用与接口路由兼容的路径变量。实际请求路径先规范化并匹配模板；查询参数、
片段和域名不参与权限身份。例如 `/sales/orders/ORD-1001` 可以匹配页面登记的
`/sales/orders/{orderId}`。

### 6.2 页面来源 Header

页面发起接口请求时必须在 Header 中携带 URL 来源页面。由于 `pageKey` 只在模块内唯一，
Header 使用 `<moduleKey>.<pageKey>` 完整页面标识：

```http
X-Nexus-Page-Key: sales.order-list
```

Header 只声明请求来源，不能直接作为授权依据。拦截器必须确认扩展和页面处于活动
状态，并确认实际请求 URL 被该页面 DSL 引用。这样不能通过伪造其他页面的 `pageKey`
绕过权限。

### 6.3 统一拦截流程

页面接口统一经过权限拦截器：

1. 从登录会话取得当前用户及其角色，不接受客户端传入角色；
2. 读取 `X-Nexus-Page-Key`，解析 `moduleKey + pageKey`；
3. 规范化实际请求 URL，并在该页面的活动 URL 引用中匹配 URL 模板；
4. 使用 `(moduleKey, pageKey, urlPattern)` 查询管理端配置的角色权限；
5. 当前用户拥有任一授权角色时放行，否则拒绝；
6. 权限通过后，Jakarta REST 容器继续调用实际 Endpoint。

需要鉴权的页面请求缺少 Header、页面不存在、扩展未激活或 URL 不属于该页面时，一律
拒绝。登录、健康检查等不属于页面的公共接口由平台白名单单独管理，不伪造页面来源。

扩展 Endpoint 只使用标准 Jakarta REST 注解完成注册和路由，Endpoint 方法不声明权限
元数据。

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

扩展入口类型使用专用发现注解标记。注解只标记入口和必要身份，不把复杂菜单、页面
清单全部塞进注解属性；发现器获得实例后仍调用统一 Provider 接口。

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

注册阶段保存扩展身份、版本、来源、模块清单摘要、发现时间和期望启用状态。禁用扩展
仍可以在扩展管理中查看，但其菜单、页面、Endpoint 和活动资源不进入运行时。

### 8.3 原子激活

激活顺序固定：

1. 校验扩展描述和兼容版本；
2. 校验全部 `moduleKey` 及资源 ID 唯一性；
3. 构建页面树并校验 `pageKey`、父子关系和 `pagePath` 模板；
4. 构建菜单树并校验菜单引用的 `pageKey`；
5. 注册只包含标准 Jakarta REST 注解的 Endpoint；
6. 由渲染模块校验 `moduleKey + pageKey` 可定位 DSL 且内部 pageKey 一致；
7. 校验页面引用的 URL，并构建活动页面 URL 权限索引；
8. 原子发布扩展资源到活动注册表。

任一步失败，整个扩展保持 `FAILED`，不发布部分菜单或部分接口。其他已激活扩展不受
影响。

### 8.4 停用与重新启用

停用扩展时：

- 从活动页面、菜单、路径模板、页面 URL 权限索引和 Endpoint 注册表撤出该扩展；
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
- 页面 DSL 引用的 URL 及其所属页面；
- 扩展是否处于活动状态。

扩展不得声明：

- 权限码或内建角色；
- 哪个用户、角色或用户组可以访问；
- 默认授权、拒绝策略或数据范围；
- 因部署方式而变化的权限规则。

权限模块通过管理端统一把 MODULE、MENU、PAGE 和页面 URL 权限项与角色建立关系。
`module:<moduleKey>` 使管理端可以按模块组织或批量配置资源；是否支持模块级继承和批量
授权等管理能力，属于权限模块设计，不由扩展决定。页面 URL 没有匹配的角色授权时固定
拒绝访问。

扩展停用时不删除权限分配；重新启用并发现相同资源身份后可以继续使用既有分配。

## 10. 模块与运行时职责

| 模块/适配层 | 职责 |
|-------------|------|
| `innospots-nexus-console` | 扩展、模块、菜单、页面和 Provider 契约；保持业务中立 |
| `innospots-nexus-kernel` | 扩展登记、启停管理、资源目录、角色权限配置和页面 URL 权限拦截 |
| 应用/运行时适配器 | 接口装配、扩展入口发现、Java SPI、REST 注册和 DSL 渲染模块接入 |
| DSL 渲染模块 | 定位 DSL、登记页面 URL 引用、注入路径变量并渲染页面 |
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

### 11.4 Endpoint 与页面接口鉴权

```java
@Path("/sales/orders")
public class OrderEndpoint {

    @GET
    public R<PageResult<OrderVo>> list() {
        // ...
    }

    @POST
    @Path("/export")
    public R<Void> export() {
        // ...
    }
}
```

Endpoint 只声明标准 REST 路由。假设 `order-list` 页面 DSL 引用了 `/sales/orders/export`，
页面发起导出请求时携带：

```http
POST /sales/orders/export
X-Nexus-Page-Key: sales.order-list
```

拦截器匹配权限身份 `(sales, order-list, /sales/orders/export)`，再根据当前用户角色决定
是否放行。其他页面即使调用同一 URL，也必须使用自己的完整页面标识和权限配置。

### 11.5 安装和加载

1. 扩展工程实现 Provider，并通过接口装配、发现注解或 Java SPI 暴露；
2. 把扩展 JAR 加入应用依赖，随应用发布；
3. 应用启动时发现 Provider，登记 `com.innospots.erp`，首次默认启用；
4. 激活器校验 `sales`、`inventory` 模块及其全部资源；
5. REST 适配器注册 Endpoint，DSL 渲染模块校验页面并登记页面 URL 引用；
6. Console 获取活动页面树、菜单树、路径模板和页面 URL 权限项；
7. 管理员在权限模块中为页面及页面 URL 配置角色权限；
8. 管理员可在扩展管理中停用或重新启用整个扩展。

## 12. 校验与冲突处理

注册或激活阶段至少执行以下校验：

- `extensionKey`、版本和模块列表合法；
- 全局 `moduleKey` 唯一；
- 同一模块内 menu/page 局部 key 唯一；
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
- 页面引用的 URL 模板合法，可以规范化且路径变量结构无歧义；
- 同一页面下相同的规范化 URL 只生成一个权限项；
- 扩展版本与宿主契约版本兼容。

冲突一律失败关闭。错误信息必须包含来源、`extensionKey`、`moduleKey`、资源类型，以及
资源 key 或 URL 模板，以便扩展管理页面直接展示诊断。

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
- 页面 DSL 引用的 URL 可以按完整页面标识正确登记和去重。

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
- Endpoint 契约只包含标准 Jakarta REST 注解，不声明权限元数据；
- 同一 URL 被不同页面引用时形成相互独立的角色权限项；
- datasource 和 action 引用同一页面 URL 时共用一个权限项；
- 请求 Header 使用 `<moduleKey>.<pageKey>` 完整页面标识；
- Header 缺失、页面无效、扩展停用或 URL 不属于来源页面时拒绝访问；
- URL 路径变量正确匹配，查询参数不影响权限身份；
- 拦截器只从登录会话读取用户角色；
- 当前用户具有任一授权角色时放行，否则拒绝；
- 未知模块、未知页面和重复资源失败关闭；
- 权限分配不因扩展停用或短暂缺失而被删除；
- 恢复相同资源身份后可以继续关联既有分配。
