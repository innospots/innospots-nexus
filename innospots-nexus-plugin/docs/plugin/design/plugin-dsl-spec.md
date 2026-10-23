# Nexus Plugin YAML DSL 规范

## 1. 适用范围

本文描述插件 **YAML 交换格式**：

| 形态 | 说明 |
|------|------|
| **当前宿主实现** | 文档头 `nexus.plugin/v1` + `spec.contributions`（`console@2`） |
| **目标形态** | 顶层 `apiVersion: 2` + `spec.extensions[]`（见 [plugin-extensions-v2-design.md](plugin-extensions-v2-design.md) §6.1） |

`spec.capabilities`、`spec.requirements`、`console@1` **不得**再出现在新插件中；编译器对 capability 块 **拒绝**。

机器可读 JSON Schema：**规划**为 `plugin-dsl-v2.schema.json`（本目录，尚未提交）。

## 2. 当前文档编码（实现中）

### 2.1 文档头

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.my-plugin
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 示例插件
  description: {}
  tags: {}
  config: []
  contributions: []
```

- 文件路径默认：`META-INF/nexus/plugin.yaml`；
- UTF-8、YAML 1.2、单 document、顶层 mapping；
- 未知字段拒绝；Nexus 宿主默认拒绝 YAML anchor/alias；
- `metadata.pluginId`：小写反向域名；`spec.apiVersion` 须与 `PluginDefinition.CURRENT_API_VERSION` 一致（当前为 `1`）。

### 2.2 配置项 `spec.config`

与历史 v1 相同：`key`、`type`（`STRING`、`INTEGER`、`BOOLEAN`、`SECRET` 等）、`required`、`default`、`enumValues`（ENUM）。

环境变量映射：`NEXUS_PLUGIN_<PLUGIN_ID>_<KEY>`（见 `ConfigurationManager`）。

### 2.3 Contribution：`console@2`

至少声明 **一份** contribution；管理台插件通常仅一份 `console`。

```yaml
contributions:
  - type: console
    majorVersion: 2
    domainKey: nexus
    displayName:
      en: Platform
    orderIndex: 0
    modules:
      - moduleKey: tenant
        displayName:
          en: Tenants
        orderIndex: 0
        viewGroups:
          - group: default
            label:
              en: Views
        operationGroups:
          - group: default
            label:
              en: Operations
        children:
          - displayName:
              en: Tenant list
            orderIndex: 0
            entryPageKey: platform-tenant-list
            children: []
```

规则摘要（完整字段见 [plugin-extensions-v2-design.md](plugin-extensions-v2-design.md) §6.1.2）：

| 层级 | 字段 |
|------|------|
| 扩展根 | `domainKey`、`modules[]`（至少一项）；根 **无** `moduleKey`；`entryPageKey` 须为空 |
| MODULE | 必须有 `moduleKey`；有 `entryPageKey` 或非空 `children`，二者不能同时存在；`viewGroups` / `operationGroups` 须含 `default` |
| MENU | 无 `moduleKey`；有 `entryPageKey`；`children: []`；可选 `icon`；访问分组由请求侧 `grantGroup` 选择 |

分组元数据键名为 **`label`**（I18n），不是 `displayName`。

**不再支持**：`console@1`、`pages[]`、`menuTree`、模块内并列 `menus[]`。

### 2.4 显式禁止

```yaml
spec:
  capabilities: [...]   # 编译错误
  requirements: [...]   # 编译错误
```

## 3. 目标文档编码（规划）

顶层与 `spec` 使用 **`apiVersion: 2`**，扩展统一为 `extensions[]`：

```yaml
apiVersion: 2
metadata:
  pluginId: com.example.my-plugin
  version: "1.0.0"
  displayName: { "zh-CN": "示例" }
spec:
  config: []
  extensions:
    - kind: contribute
      type: console
      majorVersion: 2
      domainKey: nexus
      modules: [ ... ]
    - kind: provide
      functions:
        - name: example.notify
          description: { "zh-CN": "通知" }
    - kind: subscribe
      on: example.notify
```

Java `Plugin#definition()` 与 YAML 编译为同一 `PluginDefinition` 形状（实现待落地）。

## 4. 宿主能力档案（目标）

```text
dslVersions: [2]
extensionKinds: [contribute, provide, subscribe]
contributionTypes: [console@2]
bindKinds: none          # 无 Capability Java bind
exposureKinds: none
```

当前 Minimal 档案：

```text
dslDocument: nexus.plugin/v1
contributionTypes: [console@2]
capabilities: none
```

## 5. 错误语义

| 条件 | 典型状态码 |
|------|------------|
| 未知 contribution 类型 | `UNSUPPORTED_CONTRIBUTION_TYPE` |
| YAML 含 capabilities | `DSL_STRUCTURE_INVALID` |
| console 节点 MODULE/MENU 规则违反 | `PLUGIN_DEFINITION_INVALID` / `DSL_STRUCTURE_INVALID` |
| 文档头或 apiVersion 不匹配 | `DSL_STRUCTURE_INVALID` / `PLUGIN_API_INCOMPATIBLE` |

## 6. 与 Java 声明等价

Entry 插件通过 `PluginDefinition.builder(pluginId)` + `.contribute(ConsolePluginContribution)` 声明；
字段与 YAML `console@2` 同构。禁止 `.provide()` / `.require()`（已删除 API）。
