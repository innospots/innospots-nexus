# Exposure 与 Contribution

## 1. Exposure（对外暴露能力）— V1 未实现

设计中的 `exposures` 用于把 Capability **映射**为 HTTP、Command、MCP 等对外入口，例如：

```yaml
# 设计示例 — 当前运行时不会接受
exposures:
  - kind: http
    path: /api/plugins/message/send
    capability: message.sender
```

**当前实现（Minimal V1）**

- `PluginDefinitionCompiler` 仅接受 `bind.kind: java`
- 无 `exposures` 字段解析；YAML 出现未支持 bind/exposure 返回 `UNSUPPORTED_*`
- 无 `CapabilityBindingAdapter` 实现类

若需对外 HTTP API，请在**宿主应用**自行编写 Controller，内部调用 `CapabilityManager`（见 [06-capability-usage.md](06-capability-usage.md)）。

未来实现 exposure 时，将与 Capability 共用同一 `PluginAvailability` 门控。设计参考：[`../design/plugin-runtime-design.md`](../design/plugin-runtime-design.md) §8。

---

## 2. Contribution — `console@1`（V1 支持）

Contribution 描述**静态**扩展（菜单、页面树等），不参与 Capability 路由。

### 2.1 边界

| Core | Console 模块 |
|------|----------------|
| `PluginContribution` 接口 | `ConsolePluginContribution` 实现 |
| `PluginContributionDecoder` | `ConsolePluginContributionDecoder` |
| `PluginContributionHandler` | `ConsolePluginContributionHandler` |
| 通用 prepare/stage/commit | 菜单/页面发布逻辑 |

Core **不**解析 console 字段语义；Console 注册 Decoder/Handler/Snapshotter。

### 2.2 YAML 声明示例

```yaml
spec:
  contributions:
    - type: console
      majorVersion: 1
      modules:
        - moduleKey: sales
          displayName: { zh-CN: 销售 }
          description: { zh-CN: 销售管理 }
          pages:
            - pageKey: order-list
              pagePath: /sales/orders
              children:
                - pageKey: order-detail
                  pagePath: /sales/orders/{orderId}
          menuTree:
            - menuKey: sales-root
              title: { zh-CN: 销售 }
              icon: shopping-cart
              orderIndex: 10
              children:
                - menuKey: order-list
                  title: { zh-CN: 订单列表 }
                  pageKey: order-list
```

Java 插件通过 `PluginDefinition.Builder.contribute(...)` 附加等价对象。

### 2.3 Java 声明示例

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

### 2.4 页面树与 UiSpec

- `pages` 是页面树根节点列表，`children` 表达父子页面关系。
- `pagePath` 只用于路由匹配和变量提取，不用于推导页面父子关系。
- `pageKey` 必须与 `ui-spec/<moduleKey>/<pageKey>.yaml` 中的 `pageInfo.pageId` 一致。
- 带变量的页面，例如 `/sales/orders/{orderId}`，可以声明为 PAGE，但不能直接进入静态菜单。

### 2.5 菜单树规则

- 目录节点：`children` 非空，`pageKey` 为空。
- 页面入口节点：`pageKey` 非空，`children` 为空。
- `pageKey` 必须引用同 module 的已声明页面。
- 一个页面最多被一个静态菜单节点引用；未进入菜单的页面仍是合法 PAGE 资源。
- `orderIndex` 只影响同级展示顺序，不参与资源身份。

### 2.6 生命周期（与 Capability 同事务）

```text
Handler.validate(catalog, allEntries)   # 启动前全局校验
  → prepare(context, contribution)      # 启动事务内
  → stage()
  → commit()                            # 与 capability.registerAll 同批
```

失败则 rollback，不留下可见菜单/页面资源。Console 查询侧应检查 `PluginAvailability.isActive()`。

### 2.7 与权限

插件安装的菜单/页面资源携带 `ownerPluginId`（Console 权限模块使用）。详见 [`../design/plugin-console-contribution-design.md`](../design/plugin-console-contribution-design.md)。

### 2.8 仅 Contribution、无 Capability 的插件

合法。例如纯控制台扩展包：

```yaml
spec:
  apiVersion: 1
  displayName: { en: Admin Extension }
  contributions:
    - type: console
      majorVersion: 1
      modules: [...]
```

仍需宿主注册 `console@1` Decoder 与 Handler。
