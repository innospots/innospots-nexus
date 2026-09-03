# Nexus Console Plugin Contribution 设计

## 1. 文档定位

本文定义 `console@1` Contribution 的职责、声明模型、校验、发布和权限资源协作。通用 Contribution 生命周期
属于 Core，Console 只实现自己的声明类型和 Handler。

## 2. 边界

Console Contribution 表达管理平台的静态资源事实：

- 管理模块；
- 页面树；
- 菜单树；
- 页面和菜单的稳定资源身份。

它不表达：

- Plugin 安装、启停和运行状态；
- Endpoint Java Class；
- 角色、用户、默认授权和数据权限；
- UiSpec 正文和组件树；
- Capability Provider；
- HTTP、Command 或 MCP capability exposure。

## 3. 模型

```java
public record ConsolePluginContribution(
        List<ConsoleModuleDeclaration> modules
) implements PluginContribution {
}
```

```java
public record ConsoleModuleDeclaration(
        String moduleKey,
        I18nObject displayName,
        I18nObject description,
        List<UiSpecPageDeclaration> pages,
        List<MenuDeclaration> menuTree
) {
}
```

```java
public record UiSpecPageDeclaration(
        String pageKey,
        String pagePath,
        List<UiSpecPageDeclaration> children
) {
}
```

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

Contribution 不重复 pluginId、version、displayName 或 description，这些来自所属 PluginDefinition。

一个插件最多声明一份 `console@1`，但可以包含多个 module。没有管理页面的插件可以不声明；只有
Console Contribution、没有 Capability 的插件也合法。

### 3.1 YAML 声明形状

```yaml
contributions:
  - type: console
    majorVersion: 1
    modules:
      - moduleKey: sales
        displayName:
          zh-CN: 销售
          en-US: Sales
        description:
          zh-CN: 销售管理
          en-US: Sales administration
        pages:
          - pageKey: order-list
            pagePath: /sales/orders
            children:
              - pageKey: order-detail
                pagePath: /sales/orders/{orderId}
        menuTree:
          - menuKey: sales-root
            title:
              zh-CN: 销售
              en-US: Sales
            icon: shopping-cart
            orderIndex: 10
            children:
              - menuKey: order-list
                title:
                  zh-CN: 订单列表
                  en-US: Order List
                icon: list
                orderIndex: 10
                pageKey: order-list
```

声明层面只负责：

- 标识模块身份 `moduleKey`；
- 建立页面树 `pages`；
- 建立静态菜单树 `menuTree`；
- 把页面身份关联到独立 UiSpec 文件。

声明层面不负责：

- 内嵌 UiSpec 组件树；
- 声明页面 Java Endpoint；
- 在 Contribution 中写权限主体、角色或授权策略。

### 3.2 Java 声明形状

```java
PluginDefinition.builder("com.example.sales-console")
        .name("Sales Console")
        .version("1.0.0")
        .contribute(new ConsolePluginContribution(List.of(
                new ConsoleModuleDeclaration(
                        "sales",
                        I18nObject.of("zh-CN", "销售"),
                        I18nObject.of("zh-CN", "销售管理"),
                        List.of(new UiSpecPageDeclaration(
                                "order-list",
                                "/sales/orders",
                                List.of(new UiSpecPageDeclaration(
                                        "order-detail",
                                        "/sales/orders/{orderId}",
                                        List.of())))),
                        List.of(MenuDeclaration.directory(
                                "sales-root",
                                I18nObject.of("zh-CN", "销售"),
                                "shopping-cart",
                                10,
                                List.of(MenuDeclaration.page(
                                        "order-list",
                                        I18nObject.of("zh-CN", "订单列表"),
                                        "list",
                                        10,
                                        "order-list")))))))
        .build();
```

Java 和 YAML 的语义必须完全一致。宿主在发现阶段可以同时处理 Java SPI 和 YAML 插件，但最终进入
运行时的都是同一组 `ConsolePluginContribution`、`ConsoleModuleDeclaration`、
`UiSpecPageDeclaration` 和 `MenuDeclaration` 不可变声明对象。

## 4. 资源身份

`moduleKey` 是管理资源的全局稳定命名空间，不替代 pluginId。

| 资源 | 身份示例 |
|------|----------|
| MODULE | `module:sales` |
| MENU | `menu:sales.order` |
| PAGE | `page:sales.order-list` |
| PAGE URL | `(sales, order-list, /sales/orders/{orderId})` |

每个活动或持久化资源快照必须记录 `ownerPluginId`。另一个插件不能接管 MISSING 或停用插件保留的资源身份。

## 5. 页面树

页面树表达页面领域归属：

- `pages` 是根列表；
- `children` 是单向父子关系，不从 pagePath 推导；
- pageKey 在 module 内唯一；
- 页面最多有一个父页面；
- 页面树不能循环；
- 只有拥有独立 UiSpec `pageInfo.pageId` 的页面进入页面树；
- 弹窗、抽屉、页签和局部视图不产生 PAGE 资源。

pagePath 规则：

- 必须以 `/` 开头；
- `{variable}` 必须占完整路径段；
- 静态路径优先于变量模板；
- 仅变量名不同但结构相同的模板视为冲突；
- 路径规范化后参加全局冲突校验。

例如以下两个路径冲突：

```text
/orders/{orderId}
/orders/{id}
```

## 6. 菜单树

菜单节点只能是以下一种：

```text
目录：children 非空，pageKey 为空
页面入口：pageKey 非空，children 为空
```

规则：

- menuKey 在 module 内唯一；
- pageKey 必须引用同 module 的已声明页面；
- 一个页面最多被一个静态菜单节点引用；
- 未被菜单引用的页面仍是合法 PAGE；
- 含必填路径变量的页面不能作为静态菜单入口；
- orderIndex 只影响同级展示顺序，不参与身份。

## 7. UiSpec

UiSpec 不进入 PluginDefinition。默认资源路径：

```text
ui-spec/<moduleKey>/<pageKey>.yaml
```

Handler 或 UiSpec Loader 必须验证：

```text
uiSpec.pageInfo.pageId == pageKey
```

工作台、设置、对话、看板等差异由页面 DSL 表达，不创建新的 Contribution 类型。

页面 datasource/action URL 来自 UiSpec。页面权限身份是：

```text
(moduleKey, pageKey, normalizedUrlPattern)
```

HTTP method 不参与当前页面权限身份。同一个 URL 被不同页面引用时产生不同权限项；同一页面重复引用同一
规范化 URL 时去重。

### 7.1 页面与 UiSpec 的映射约束

每个进入 `pages` 树的节点都必须能唯一定位一个 UiSpec 文件：

```text
ui-spec/<moduleKey>/<pageKey>.yaml
```

例如：

```text
moduleKey = sales
pageKey = order-list
=> ui-spec/sales/order-list.yaml
```

该 UiSpec 文件中的：

```text
pageInfo.pageId
```

必须与 `pageKey` 严格一致，否则声明无效。这样页面身份、资源权限和前端渲染入口都以同一稳定键对齐。

## 8. YAML 示例

```yaml
contributions:
  - type: console
    majorVersion: 1
    modules:
      - moduleKey: wecom
        displayName:
          zh-CN: 企业微信
          en-US: WeCom
        description:
          zh-CN: 企业微信管理
          en-US: WeCom administration
        pages:
          - pageKey: settings
            pagePath: /wecom/settings
            children:
              - pageKey: message-detail
                pagePath: /wecom/messages/{messageId}
        menuTree:
          - menuKey: wecom
            title:
              zh-CN: 企业微信
              en-US: WeCom
            icon: wecom
            orderIndex: 10
            children:
              - menuKey: settings
                title:
                  zh-CN: 设置
                  en-US: Settings
                icon: settings
                orderIndex: 10
                pageKey: settings
```

`message-detail` 是合法 PAGE，但不进入静态菜单，因为路径包含必填变量。

## 8.1 Java 示例

```java
new ConsolePluginContribution(List.of(
        new ConsoleModuleDeclaration(
                "wecom",
                I18nObject.of("zh-CN", "企业微信"),
                I18nObject.of("zh-CN", "企业微信管理"),
                List.of(new UiSpecPageDeclaration(
                        "settings",
                        "/wecom/settings",
                        List.of(new UiSpecPageDeclaration(
                                "message-detail",
                                "/wecom/messages/{messageId}",
                                List.of())))),
                List.of(MenuDeclaration.directory(
                        "wecom",
                        I18nObject.of("zh-CN", "企业微信"),
                        "wecom",
                        10,
                        List.of(MenuDeclaration.page(
                                "settings",
                                I18nObject.of("zh-CN", "设置"),
                                "settings",
                                10,
                                "settings")))))))
```

推荐优先使用 `MenuDeclaration.directory(...)` 和 `MenuDeclaration.page(...)` 工厂方法，让目录节点与
页面入口节点的互斥关系在构造时更直观。

## 9. Handler 和活动目录

Console 提供：

```text
ConsolePluginContributionHandler
ConsoleContributionCatalog
```

Handler 行为：

1. `validate` 对完整 PluginCatalog 执行 Console 全局校验；
2. `prepare` 加载和校验当前插件 UiSpec，但不发布；
3. `stage` 构造不可变资源快照；
4. `commit` 把带 pluginId/generation 的资源放入活动索引；
5. Plugin availability 激活后资源才对外可见；
6. 停止时先关闭 availability，再撤出活动索引。

`ConsoleContributionCatalog` 只保存活动资源的不可变内存快照：

- 不保存 desiredEnabled；
- 不持久化安装数据；
- 不创建 Plugin；
- 不维护 PluginState；
- 不是第二个 PluginManager。

## 10. 全局校验

在任何 Plugin initialize 前校验：

- moduleKey 和全局资源身份唯一；
- ownerPluginId 与资源归属一致；
- pageKey、menuKey 唯一；
- 页面树无环且每页最多一个父节点；
- pagePath 合法且无模板歧义；
- 菜单目录/页面入口字段互斥；
- 菜单没有悬空 pageKey；
- UiSpec 可以唯一定位且 pageInfo.pageId 一致；
- 页面 URL 可以规范化；
- 当前声明不冒用历史 MISSING 插件保留的资源身份。

全局资源身份预留需要读取 Core 安装快照中的历史 ownerPluginId 摘要，但 Console Handler 不直接访问安装
DAO。应用通过只读 `ReservedPluginResourceCatalog` 向 Handler 提供所需事实。

## 11. 权限同步

Kernel 的 `PermissionResourceSyncService` 改为读取：

```text
ConsoleContributionCatalog.activeContributions()
```

每条活动 Contribution 携带 ownerPluginId。权限资源表中的：

```text
extension_key
```

迁移为：

```text
owner_plugin_id
```

插件停用或 MISSING 时不删除历史权限分配，只将资源标记为不可用。相同 pluginId 和资源 key 恢复后可以
复用原关联。

请求可以携带：

```http
X-Nexus-Page-Key: sales.order-list
```

但 Header 不能单独作为授权依据。拦截器还必须确认：

- ownerPluginId 当前 ACTIVE；
- 页面属于活动 Console Contribution；
- URL 属于对应页面 UiSpec；
- 登录会话拥有对应角色权限。

## 12. Extension 移除

以下类型不再保留兼容双轨：

- `ConsoleExtensionProvider`；
- `ExtensionDescriptor`；
- `ExtensionModuleDeclaration`；
- `ExtensionProviderDiscovery`；
- `ExtensionRegistry`；
- `ExtensionRegistration`；
- `ExtensionState`。

迁移期间同一插件只能由 PluginManager 管理。不得让 ExtensionRegistry 和 ConsoleContributionCatalog 同时
发布同一资源。

## 13. 测试要求

至少覆盖：

- 多模块、嵌套页面和菜单合法；
- pageKey、menuKey、moduleKey 冲突；
- 页面树循环和多父节点失败；
- 路径模板结构冲突；
- 菜单字段互斥和悬空引用失败；
- 带变量页面不能进入静态菜单；
- UiSpec 缺失或 pageId 不一致失败；
- prepare/commit 失败不发布部分资源；
- availability 激活前 Catalog 不可见；
- 停止后活动资源原子撤出；
- MISSING 资源身份不能被其他插件接管；
- 权限同步记录 ownerPluginId；
- 停用和 MISSING 不删除历史授权；
- Kernel 不再依赖 ExtensionRegistry。
