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

## 8. 发现与拒绝

- 结构/绑定错误：该 YAML 进入 `rejectedDefinitions`，其他插件不受影响。
- 与 Java SPI 重复 `pluginId`：整个 Catalog 失败。

完整 DSL 规范见 [`../design/plugin-dsl-spec.md`](../design/plugin-dsl-spec.md)。

参考测试：`PluginDefinitionCompilerTest`。
