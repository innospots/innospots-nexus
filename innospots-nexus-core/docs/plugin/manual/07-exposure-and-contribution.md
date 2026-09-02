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
          pages:
            - pageKey: order-list
              pagePath: /sales/orders
              displayName: { zh-CN: 订单列表 }
          menuTree:
            - menuKey: sales-root
              displayName: { zh-CN: 销售 }
              children: [...]
```

Java 插件通过 `PluginDefinition.Builder.contribute(...)` 附加等价对象。

### 2.3 生命周期（与 Capability 同事务）

```text
Handler.validate(catalog, allEntries)   # 启动前全局校验
  → prepare(context, contribution)      # 启动事务内
  → stage()
  → commit()                            # 与 capability.registerAll 同批
```

失败则 rollback，不留下可见菜单/页面资源。Console 查询侧应检查 `PluginAvailability.isActive()`。

### 2.4 与权限

插件安装的菜单/页面资源携带 `ownerPluginId`（Console 权限模块使用）。详见 [`../design/plugin-console-contribution-design.md`](../design/plugin-console-contribution-design.md)。

### 2.5 仅 Contribution、无 Capability 的插件

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
