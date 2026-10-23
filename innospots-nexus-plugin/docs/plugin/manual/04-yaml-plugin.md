# YAML 插件手册

## 1. 文件位置

```text
META-INF/nexus/plugin.yaml
```

一个文件一个插件文档；classpath 上可存在多份，由 `ClasspathPluginDiscovery` 枚举。

## 2. 当前最小示例

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.reports
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 报表插件
    en-US: Reports
  tags:
    vendor: example
  config:
    - key: endpoint
      type: URI
      required: true
    - key: token
      type: SECRET
      required: true
  contributions:
    - type: console
      majorVersion: 2
      domainKey: reports
      displayName:
        zh-CN: 报表
        en-US: Reports
      orderIndex: 0
      modules:
        - moduleKey: reports
          displayName:
            zh-CN: 报表
            en-US: Reports
          orderIndex: 10
          viewGroups:
            - group: default
              label:
                zh-CN: 默认
                en-US: Default
          operationGroups:
            - group: default
              label:
                zh-CN: 默认
                en-US: Default
          entryPageKey: reports-reports-main
          icon: chart
          children: []
```

编译器：`PluginDefinitionCompiler`；运行时包装：`ManifestPlugin`。

## 3. 顶层字段

| 字段 | 必填 | 说明 |
|------|------|------|
| `apiVersion` | 是 | 当前固定 `nexus.plugin/v1` |
| `kind` | 是 | 固定 `Plugin` |
| `metadata.pluginId` | 是 | 小写反向域名 |
| `metadata.version` | 是 | 插件版本 |
| `spec.apiVersion` | 是 | 当前固定 `1` |
| `spec.displayName` | 是 | 多语言 map 或单键 |
| `spec.config` | 否 | 插件配置 schema |
| `spec.contributions` | 是 | 至少一份贡献；当前支持 `console@2` |

`spec.capabilities`、`spec.requirements`、`console@1`、`pages[]`、`menuTree` 会被拒绝。

## 4. 标识符规则

- `pluginId`：`com.example.reports`；缺域名段非法。
- `domainKey`、`moduleKey`、`group`、`entryPageKey`：小写 kebab-case。
- `entryPageKey` 是稳定不透明键；推荐 `{domainKey}-{moduleKey}-{pageSuffix}`，运行时不得拆分解析。

## 5. 配置项 type

| type | 说明 |
|------|------|
| `STRING` | 文本 |
| `INTEGER` / `LONG` / `BOOLEAN` | 数值/布尔 |
| `DECIMAL` | 十进制 |
| `DURATION` | 如 `30s`、`5m` |
| `URI` | 绝对 URI |
| `ENUM` | 需 `enumValues` |
| `SECRET` | 密文；**不得**有 `defaultValue` |

运行时配置值不写在本文件，见 [08-configuration.md](08-configuration.md)。

## 6. `console@2` 贡献

扩展根使用 `modules[]`；MODULE/MENU 使用递归 `children[]`：

| 层级 | 规则 |
|------|------|
| 扩展根 | `domainKey` + 非空 `modules[]`；无 `moduleKey`；`entryPageKey` 为空 |
| MODULE | 必须有 `moduleKey`；有 `entryPageKey` 或非空 `children`，二者不能同时存在 |
| MENU | 无 `moduleKey`；必须有 `entryPageKey`；`children: []` |

MODULE 的 `viewGroups` / `operationGroups` 必须包含 `default`。分组标题字段为 `label`
（`I18nObject`），不是 `displayName`。

完整字段与校验规则见
[`../design/plugin-dsl-spec.md`](../design/plugin-dsl-spec.md) 与
[`../design/plugin-extensions-v2-design.md`](../design/plugin-extensions-v2-design.md) §6.1。

## 7. 发现与拒绝

- 结构或配置错误：该 YAML 进入 `rejectedDefinitions`，其他插件不受影响。
- 与 Java SPI 重复 `pluginId`：整个 Catalog 失败。
- 未声明配置键、类型错误、密文带默认值：拒绝。

参考测试：`PluginDefinitionCompilerTest`、`ConsolePluginContributionDecoderTest`。
