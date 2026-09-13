# YAML 插件手册

## 1. 文件位置

```text
META-INF/nexus/plugin.yaml
```

一个文件一个插件文档；classpath 上可存在多份，由 `ClasspathPluginDiscovery` 全部枚举。

## 2. 最小示例

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.message-wecom
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 企业微信消息
    en-US: WeCom Messaging
  tags:
    vendor: wecom
  config:
    - key: corpId
      type: STRING
      required: true
    - key: secret
      type: SECRET
      required: true
  capabilities:
    - type: message.sender
      majorVersion: 1
      providerId: wecom
      api: com.example.contract.MessageSender
      tags:
        channel: wecom
      config:
        - key: agentId
          type: STRING
          required: true
      bind:
        kind: java
        class: com.example.message.WeComMessageSender
```

编译类：`PluginDefinitionCompiler`；运行时包装：`ManifestPlugin`（对宿主表现为普通 `Plugin`）。

## 3. 顶层字段

| 字段 | 必填 | 说明 |
|------|------|------|
| `apiVersion` | 是 | 固定 `nexus.plugin/v1` |
| `kind` | 是 | 固定 `Plugin` |
| `metadata.pluginId` | 是 | 反向域名，见下节 |
| `metadata.version` | 是 | 插件版本 |
| `spec.apiVersion` | 是 | 固定 `1` |
| `spec.displayName` | 是 | 多语言 map 或单键 |
| `spec.capabilities` | 二选一 | 至少一个 capability 或 contribution |
| `spec.contributions` | 二选一 | 见 [07-exposure-and-contribution.md](07-exposure-and-contribution.md) |

可选：`spec.description`、`spec.tags`、`spec.config`、`spec.requirements`。

## 4. 标识符规则

**pluginId**（与 Java 相同）：

```text
com.example.message-wecom   ✓
message-wecom               ✗（缺域名段）
```

**providerId**：插件内全局唯一，小写连字符：`wecom`、`primary-channel`。

**Capability type**：点分小写，如 `message.sender`；与 `majorVersion`、`api` 一起在插件声明中自描述。

## 5. Capability 块

```yaml
capabilities:
  - type: message.sender
    majorVersion: 1
    providerId: wecom
    api: com.example.contract.MessageSender
    tags: { channel: wecom }
    config: [ ... ]
    bind:
      kind: java
      class: com.example.message.WeComMessageSender
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `type` | 是 | Capability 逻辑名称 |
| `majorVersion` | 是 | API 主版本 |
| `providerId` | 是 | 插件内 Provider 标识 |
| `api` | 是 | Capability API 接口全限定类名 |
| `bind` | 是 | 实现绑定；V1 仅 `kind: java` |

| bind.kind | V1 |
|-----------|-----|
| `java` | ✓ 支持 |
| `http` / `process` / `mcp` / `contract` | ✗ 返回 `UNSUPPORTED_BIND_KIND` |

Java 类要求：public、具体类、实现 Capability API 与 `CapabilityProvider`、public 无参构造。

## 6. 配置项 type

| type | 说明 |
|------|------|
| `STRING` | 文本 |
| `INTEGER` / `LONG` / `BOOLEAN` | 数值/布尔 |
| `DECIMAL` | 十进制 |
| `DURATION` | 如 `30s`、`5m` |
| `URI` | 绝对 URI |
| `ENUM` | 需 `enumValues` |
| `SECRET` | 密文；**不得**有 `defaultValue` |

运行时配置不写在本文件，见 [08-configuration.md](08-configuration.md)。

## 7. 依赖声明

```yaml
requirements:
  - type: message.sender
    majorVersion: 1
    required: true
    tags:
      channel: wecom
```

## 8. `console@1` 页面与菜单声明

YAML 插件可以通过 `spec.contributions` 声明控制台模块、页面树和菜单树：

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.sales-console
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 销售控制台
    en-US: Sales Console
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

字段要点：

- `modules[].pages` 必填且非空，表示页面树根节点列表。
- `pages[].children` 表示页面父子关系，不从 `pagePath` 自动推导。
- `modules[].menuTree` 可选；未进入菜单的页面仍然是合法 PAGE 资源。
- 菜单节点二选一：目录节点使用 `children`，页面入口节点使用 `pageKey`。
- `pageKey`、`menuKey` 在各自 module 内必须唯一；`moduleKey` 在运行时全局唯一。

路径与 UiSpec 约束：

- `pagePath` 必须以 `/` 开头，不能带 query 或 fragment。
- `{variable}` 必须占完整路径段，例如 `/orders/{orderId}` 合法，`/orders/{orderId}.html` 非法。
- 仅变量名不同但模板结构相同的路径视为冲突，例如 `/orders/{id}` 与 `/orders/{orderId}`。
- 默认 UiSpec 文件路径为 `ui-spec/<moduleKey>/<pageKey>.yaml`。
- UiSpec 中的 `pageInfo.pageId` 必须与声明里的 `pageKey` 一致。

静态菜单限制：

- 带必填路径变量的页面不能直接作为静态菜单入口。
- 一个页面最多被一个静态菜单节点引用。
- `orderIndex` 只影响同级显示顺序，不参与资源身份。

## 9. 发现与拒绝

- 结构/绑定错误：该 YAML 进入 `rejectedDefinitions`，其他插件不受影响。
- 与 Java SPI 重复 `pluginId`：整个 Catalog 失败。

完整 DSL 规范见 [`../design/plugin-dsl-spec.md`](../design/plugin-dsl-spec.md)。

参考测试：`PluginDefinitionCompilerTest`。
