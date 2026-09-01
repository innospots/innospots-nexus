# Nexus Plugin YAML DSL v1 规范

## 1. 状态与适用范围

本文是 Nexus Plugin YAML DSL v1 的跨系统规范。它定义可交换 YAML 文档的语法、字段、身份、校验、
兼容性和宿主一致性要求。

本文中的“必须（MUST）”“不得（MUST NOT）”“应（SHOULD）”“不应（SHOULD NOT）”和“可以（MAY）”
具有规范含义。

机器可读结构由 [plugin-dsl-v1.schema.json](plugin-dsl-v1.schema.json) 定义。JSON Schema 负责结构校验，
本文额外定义跨字段、全局身份、宿主能力和运行语义。两者冲突时，以本文为准，并必须修正 Schema。

## 2. DSL 版本与宿主能力

DSL 版本由顶层字段声明：

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
```

DSL v1 完整定义以下类型：

| 类别 | v1 类型 |
|------|---------|
| bind | `java`, `http`, `process`, `mcp`, `contract` |
| exposure | `http`, `command`, `mcp` |
| contribution | `console@1` |

宿主不必实现全部可选类型，但必须发布能力档案：

```yaml
dslVersions: [nexus.plugin/v1]
bindKinds: [java]
exposureKinds: []
contributionTypes: [console@1]
```

结构和语义合法、但宿主未实现的类型不是 DSL 语法错误。宿主必须返回：

```text
UNSUPPORTED_BIND_KIND
UNSUPPORTED_EXPOSURE_KIND
UNSUPPORTED_CONTRIBUTION_TYPE
```

Nexus Core Minimal V1 档案是：

```text
bindKinds: java
exposureKinds: none
contributionTypes: console@1
```

## 3. 文档编码与解析

插件描述文件默认位于：

```text
META-INF/nexus/plugin.yaml
```

要求：

- 使用 UTF-8；
- 使用 YAML 1.2；
- 一个文件只包含一个 YAML document；
- 顶层必须是 mapping；
- 键大小写敏感；
- 禁止重复键；
- 禁止自定义 YAML tag；
- 宿主可以拒绝 anchor 和 alias；Nexus 默认宿主必须拒绝，避免别名展开攻击；
- 未知字段必须拒绝；
- 除明确允许的可选字段外，显式 `null` 必须拒绝；
- 字符串不执行模板替换、环境变量替换或表达式求值。

实例配置中的 `${ENV_NAME}` 由宿主 ConfigurationManager 处理，不属于 plugin.yaml 语法。

## 4. 顶层结构

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.message-wecom
  version: "1.2.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 企业微信消息
    en-US: WeCom Messaging
  capabilities:
    - type: message.sender
      majorVersion: 1
      providerId: wecom
      bind:
        kind: java
        class: com.example.message.WeComMessageSender
```

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| `apiVersion` | string | 是 | 固定 `nexus.plugin/v1` |
| `kind` | string | 是 | 固定 `Plugin` |
| `metadata` | object | 是 | 插件身份和版本 |
| `spec` | object | 是 | 插件规范 |

至少必须声明一个 Capability 或一个 Contribution。空插件非法。

## 5. 通用标识符

### 5.1 pluginId

格式：

```regex
^[a-z][a-z0-9]*(?:-[a-z0-9]+)*(?:\.[a-z][a-z0-9]*(?:-[a-z0-9]+)*)+$
```

规则：

- 至少两个点分段；
- 每段以小写字母开头；
- 可以包含小写字母、数字和单连字符分组；
- 不包含版本、环境、租户、workspace 或部署实例；
- 发布后不得复用给不同插件。

合法：

```text
com.example.message-wecom
io.acme.erp
```

非法：

```text
message-wecom
Com.Example.Plugin
com.example.-plugin
```

### 5.2 type

Capability 和 Contribution 的逻辑类型使用小写点分名称：

```regex
^[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*$
```

例如：

```text
message.sender
artifact.viewer
console
```

### 5.3 providerId

格式：

```regex
^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$
```

`providerId` 在一个 plugin.yaml 内全局唯一，不只是同一 CapabilityKey 下唯一。

### 5.4 localKey

配置 localKey 使用 lowerCamelCase：

```regex
^[a-z][A-Za-z0-9]*$
```

不得包含 `plugins.` 前缀、点、连字符、环境变量名或 providerId。

### 5.5 版本

`metadata.version` 是非空、不含空白的发布版本字符串，长度不超过 64。DSL v1 不强制 SemVer；宿主按不透明
字符串展示和记录，不得据此推断兼容性。兼容性由 DSL apiVersion、Plugin spec.apiVersion 和 Capability
majorVersion 决定。

## 6. metadata

```yaml
metadata:
  pluginId: com.example.message-wecom
  version: "1.2.0"
```

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| `pluginId` | string | 是 | 第 5.1 节 |
| `version` | string | 是 | 1–64 字符，不含空白 |

metadata 不允许 labels、annotations、name 或 namespace 等未定义字段。

## 7. spec 基础字段

| 字段 | 类型 | 必填 | 默认值 |
|------|------|------|--------|
| `apiVersion` | integer | 是 | 无，v1 必须为 1 |
| `displayName` | I18n map | 是 | 无 |
| `description` | I18n map | 否 | 空 |
| `tags` | string map | 否 | 空 map |
| `config` | ConfigItem[] | 否 | 空列表 |
| `requirements` | Requirement[] | 否 | 空列表 |
| `capabilities` | Capability[] | 条件必填 | 空列表 |
| `contributions` | Contribution[] | 条件必填 | 空列表 |

`capabilities` 与 `contributions` 不能同时为空。

### 7.1 I18n map

```yaml
displayName:
  zh-CN: 企业微信消息
  en-US: WeCom Messaging
```

- 至少一个条目；
- key 必须是规范化 BCP 47 language tag；
- value 必须是 1–256 字符的非空字符串；
- 同一 language tag 大小写规范化后不能重复；
- 宿主不得把任一固定语言设为协议必填项。

### 7.2 tags

```yaml
tags:
  channel: im
  region: cn
```

- key 和 value 都是 1–64 字符字符串；
- key 使用小写点分或连字符名称；
- Tags 是静态路由身份，不存放 Secret、URL、版本或动态健康状态；
- Provider Tags 与 Plugin Tags 合并，同名不同值非法。

## 8. 配置 Schema

插件共享配置位于 `spec.config`，Provider 私有配置位于 capability 的 `config`。

```yaml
config:
  - key: timeout
    type: DURATION
    required: false
    default: PT10S
    description: Request timeout
  - key: appSecret
    type: SECRET
    required: true
    description: Application secret
```

### 8.1 ConfigItem 字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `key` | string | 是 | localKey |
| `type` | enum | 是 | 配置类型 |
| `required` | boolean | 否 | 默认 false |
| `default` | scalar | 否 | SECRET 禁止 |
| `description` | string | 否 | 1–1024 字符 |
| `enumValues` | string[] | ENUM 必填 | 非空且唯一 |

支持类型：

| type | 实例值 | default 类型 |
|------|--------|--------------|
| `STRING` | string | string |
| `BOOLEAN` | boolean | boolean |
| `INTEGER` | 32-bit integer | integer |
| `LONG` | 64-bit integer | integer |
| `DECIMAL` | decimal | number 或十进制字符串 |
| `DURATION` | ISO-8601 duration | string |
| `URI` | absolute URI | string |
| `ENUM` | enumValues 中一个值 | string |
| `SECRET` | 宿主 SecretValue | 禁止 |

规则：

- 同一 config 数组内 key 唯一；
- 插件共享 key 与 Provider 私有 key 可以同名，因为作用域不同；
- `required=true` 与 default 可以同时存在，表示宿主未覆盖时默认值满足 required；
- SECRET 不允许 default，也不得进入日志、诊断和 DefinitionSnapshot；
- 非 ENUM 类型不得声明 enumValues；
- 所有 default 必须通过对应类型转换；
- 未知配置 key 由宿主拒绝。

实例命名空间：

```text
plugins.<pluginId>.<localKey>
plugins.<pluginId>.providers.<providerId>.<localKey>
```

## 9. Capability requirements

```yaml
requirements:
  - type: model.provider
    majorVersion: 1
    required: false
    tags:
      family: general
```

| 字段 | 类型 | 必填 | 默认 |
|------|------|------|------|
| `type` | string | 是 | 无 |
| `majorVersion` | positive integer | 是 | 无 |
| `required` | boolean | 否 | true |
| `tags` | string map | 否 | 空 |

同一插件内不能重复完全相同的 `(type, majorVersion, tags)` requirement。Requirement 只依赖能力，不直接
依赖 pluginId 或 providerId。

## 10. Capability

```yaml
capabilities:
  - type: message.sender
    majorVersion: 1
    providerId: wecom
    tags:
      provider: wecom
      channel: im
    bind:
      kind: java
      class: com.example.message.WeComMessageSender
```

| 字段 | 类型 | 必填 | 默认 |
|------|------|------|------|
| `type` | string | 是 | 无 |
| `majorVersion` | positive integer | 是 | 无 |
| `providerId` | string | 是 | 无 |
| `tags` | string map | 否 | 空 |
| `config` | ConfigItem[] | 否 | 空 |
| `bind` | Bind | 是 | 无 |
| `exposures` | Exposure[] | 否 | 空 |

规则：

- providerId 在整个插件内唯一；
- type@majorVersion 必须存在于宿主 CapabilityTypeRegistry；
- Java API、bind 和 exposure 的兼容性由该 CapabilityType 注册的 codec/adapter 校验；
- Java 类名不是稳定身份；
- V1 禁止无 Capability 的 orphan exposure。

## 11. bind

bind 描述 Capability 的实现位于何处，以及宿主如何连接到实现。每个 bind 只允许其 kind 定义的字段。

### 11.1 java

```yaml
bind:
  kind: java
  class: com.example.message.WeComMessageSender
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `java` |
| `class` | 是 | Java 全限定类名 |

实现类必须：

- 对声明源 ClassLoader 可见；
- 是 public concrete class；
- 实现 `CapabilityProvider`；
- 实现 CapabilityType 对应 Java API；
- 提供 public 无参数构造函数；
- 构造函数无联网、线程启动、文件写入和全局注册副作用。

`java` 是 Core Minimal V1 唯一实现的 bind.kind。

### 11.2 http

```yaml
bind:
  kind: http
  baseUrl: http://127.0.0.1:8091
  method: POST
  invokePath: /internal/messages/send
  operation: send
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `http` |
| `baseUrl` | 是 | absolute http/https URL，不含 user-info、query、fragment |
| `method` | 是 | GET/POST/PUT/PATCH/DELETE |
| `invokePath` | 是 | 以 `/` 开头的绝对路径模板 |
| `operation` | 是 | Capability codec 注册的操作 ID |

`baseUrl` 是安装包的静态实现绑定，不允许被实例配置覆盖。凭证、Secret、超时和重试不得写入 bind，
必须使用实例配置。需要按环境变化的远程地址应使用稳定网关或等待后续地址引用规范，不能在 v1 中隐式
插值。宿主只有注册
`(CapabilityKey, http)` Binding Adapter 和对应 operation codec 时才能接受该声明。

### 11.3 process

```yaml
bind:
  kind: process
  command: ["node", "dist/wecom-tool.js"]
  protocol: nexus-json-rpc/v1
  invoke: tool.invoke
  operation: invoke
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `process` |
| `command` | 是 | 非空字符串数组 |
| `protocol` | 是 | 宿主注册的 `<name>/v<major>` 协议 ID，例如 `nexus-json-rpc/v1` |
| `invoke` | 是 | 远程服务/方法标识 |
| `operation` | 是 | Capability codec 操作 ID |

`command` 不经过 shell 拼接。宿主只能以参数数组启动进程，必须控制工作目录、环境变量、继承句柄和 Secret
注入。进程在 Plugin 启动事务中创建并登记到 ResourceScope。

### 11.4 mcp

```yaml
bind:
  kind: mcp
  server: docs-tools
  tool: search_docs
  operation: invoke
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `mcp` |
| `server` | 是 | 宿主 MCP server 引用 |
| `tool` | 是 | MCP tool 名 |
| `operation` | 是 | Capability codec 操作 ID |

连接、鉴权、超时和重试来自实例配置。bind.kind=mcp 表示调用外部 MCP 工具，不等于向外发布 MCP exposure。

### 11.5 contract

```yaml
bind:
  kind: contract
  resolver: artifact-viewer-default
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `contract` |
| `resolver` | 是 | 安装环境注册的解析器 ID |

contract 只用于分发能力身份并由安装环境显式解析。它不是其它 kind 的回退，也不能在没有 resolver 结果时
进入 ACTIVE。Core Minimal V1 不实现 contract。

## 12. exposures

Exposure 把 Capability 的一个已注册 operation 暴露到宿主外部入口。Exposure 不改变 Provider 身份，且与
所属 Plugin 共享 availability。

### 12.1 http exposure

```yaml
- kind: http
  method: POST
  path: /api/wecom/messages/send
  operation: send
  requestSchema:
    type: object
    required: [toUser, content]
    properties:
      toUser: { type: string }
      content: { type: string }
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `http` |
| `method` | 是 | GET/POST/PUT/PATCH/DELETE |
| `path` | 是 | 以 `/` 开头的宿主 API 路径模板 |
| `operation` | 是 | Capability codec 操作 ID |
| `requestSchema` | 否 | JSON Schema object |
| `responseSchema` | 否 | JSON Schema object |

同一 Runtime 内规范化 `(method, path)` 全局唯一。

### 12.2 command exposure

```yaml
- kind: command
  commandId: wecom.send-message
  operation: send
  paramsSchema:
    type: object
    required: [toUser, content]
    properties:
      toUser: { type: string }
      content: { type: string }
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `command` |
| `commandId` | 是 | 全局稳定点分 ID |
| `operation` | 是 | Capability codec 操作 ID |
| `paramsSchema` | 否 | JSON Schema object |

commandId 在同一 Runtime 内全局唯一。

### 12.3 mcp exposure

```yaml
- kind: mcp
  name: wecom_send_message
  operation: send
  description: Send a WeCom message
  inputSchema:
    type: object
    required: [toUser, content]
    properties:
      toUser: { type: string }
      content: { type: string }
```

| 字段 | 必填 | 约束 |
|------|------|------|
| `kind` | 是 | 固定 `mcp` |
| `name` | 是 | MCP tool name，全局唯一 |
| `operation` | 是 | Capability codec 操作 ID |
| `description` | 否 | 1–1024 字符 |
| `inputSchema` | 是 | JSON Schema object |

## 13. console@1 Contribution

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
                orderIndex: 10
                pageKey: settings
```

### 13.1 Contribution 字段

| 字段 | 必填 | 约束 |
|------|------|------|
| `type` | 是 | 固定 `console` |
| `majorVersion` | 是 | 固定 1 |
| `modules` | 是 | 非空数组 |

### 13.2 module

| 字段 | 必填 | 约束 |
|------|------|------|
| `moduleKey` | 是 | 全局唯一 kebab ID |
| `displayName` | 是 | I18n map |
| `description` | 否 | I18n map |
| `pages` | 是 | 非空页面根列表 |
| `menuTree` | 否 | 菜单根列表 |

### 13.3 page

| 字段 | 必填 | 约束 |
|------|------|------|
| `pageKey` | 是 | module 内唯一 kebab ID |
| `pagePath` | 是 | 以 `/` 开头 |
| `children` | 否 | page[] |

页面树不能循环；同一页面不能有多个父节点；规范化路径模板不能冲突。

### 13.4 menu

| 字段 | 必填 | 约束 |
|------|------|------|
| `menuKey` | 是 | module 内唯一 kebab ID |
| `title` | 是 | I18n map |
| `icon` | 否 | 1–128 字符 |
| `orderIndex` | 否 | integer，默认 0 |
| `pageKey` | 条件必填 | 页面入口使用 |
| `children` | 条件必填 | 目录使用，非空 |

`pageKey` 与 `children` 必须且只能出现一个。pageKey 必须引用同 module 页面。带必填路径变量的页面不能
作为静态菜单入口。

## 14. 完整示例

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.message-wecom
  version: "1.2.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 企业微信消息
    en-US: WeCom Messaging
  description:
    zh-CN: 向企业微信发送通知
    en-US: Send WeCom notifications
  tags:
    channel: im
  config:
    - key: timeout
      type: DURATION
      default: PT10S
      description: HTTP timeout
  requirements:
    - type: model.provider
      majorVersion: 1
      required: false
  capabilities:
    - type: message.sender
      majorVersion: 1
      providerId: wecom
      tags:
        provider: wecom
      config:
        - key: baseUrl
          type: URI
          required: true
        - key: appSecret
          type: SECRET
          required: true
      bind:
        kind: java
        class: com.example.message.WeComMessageSender
    - type: tool.invoke
      majorVersion: 1
      providerId: wecom-tool
      tags:
        provider: wecom
      bind:
        kind: process
        command: ["node", "dist/wecom-tool.js"]
        protocol: nexus-json-rpc/v1
        invoke: tool.invoke
        operation: invoke
      exposures:
        - kind: http
          method: POST
          path: /api/wecom/messages/send
          operation: invoke
        - kind: command
          commandId: wecom.send-message
          operation: invoke
          paramsSchema:
            type: object
            required: [toUser, content]
            properties:
              toUser: { type: string }
              content: { type: string }
        - kind: mcp
          name: wecom_send_message
          operation: invoke
          description: Send a WeCom message
          inputSchema:
            type: object
            required: [toUser, content]
            properties:
              toUser: { type: string }
              content: { type: string }
  contributions:
    - type: console
      majorVersion: 1
      modules:
        - moduleKey: wecom
          displayName:
            zh-CN: 企业微信
            en-US: WeCom
          pages:
            - pageKey: settings
              pagePath: /wecom/settings
          menuTree:
            - menuKey: settings
              title:
                zh-CN: 设置
                en-US: Settings
              orderIndex: 10
              pageKey: settings
```

该示例符合完整 DSL v1，但 Core Minimal V1 会因 `process` bind 和 exposures 返回明确的不支持诊断。删除
第二个 Capability 或改为 `java` bind 且移除 exposures 后，可由 Minimal V1 执行。

## 15. 跨字段和全局校验

JSON Schema 通过后，宿主还必须执行：

1. pluginId 在全部声明源中唯一；
2. providerId 在本插件内全局唯一；
3. CapabilityKey 存在且 Java API 映射一致；
4. Plugin Tags 与 Provider Tags 无冲突；
5. 配置 key、default 和环境变量映射合法；
6. bind kind 与字段严格互斥；
7. operation 存在于 Capability codec；
8. exposure 全局身份唯一；
9. Contribution 类型已注册并受宿主支持；
10. Console 页面树、菜单树、路径和资源归属合法；
11. 当前资源不冒用历史 MISSING 插件保留身份；
12. 至少一个 Capability 或 Contribution；
13. 声明源和实现类来源符合宿主信任策略。

## 16. 规范化

宿主用于唯一性比较和 Snapshot 时必须：

- 保留 pluginId、providerId、type、moduleKey、pageKey 和 menuKey 原值；
- 不自动改写非法大小写或空白；
- 对 BCP 47 language tag 使用规范 casing；
- 对 HTTP method 使用大写；
- 对 URI 进行语法规范化但不得改变语义；
- 对路径删除重复 `/`，禁止 `.`、`..` 和编码后的路径穿越；
- 将 `{任意变量名}` 规范化为 `{}` 后比较路径模板冲突；
- map 顺序不参与语义；
- capabilities、requirements、contributions 和资源树的列表顺序只在字段明确声明排序语义时有效。

## 17. 兼容性

### 17.1 向后兼容

同一 `nexus.plugin/v1` 内允许：

- 增加具有明确默认值的可选字段；
- 增加宿主可选实现的新 CapabilityType；
- 增加新的宿主能力档案。

但 v1 解析器仍然必须拒绝未知字段。因此新增 DSL 字段需要先发布更新后的 v1 Schema 和解析器，再允许
生产者输出。跨组织长期演进优先发布 `nexus.plugin/v2`，避免同版本漂移。

### 17.2 不兼容变更

以下变更需要新 DSL major version：

- 删除或重命名字段；
- 改变字段类型或默认值；
- 改变身份唯一范围；
- 改变 bind/exposure 的执行语义；
- 放宽后会产生不同解释的规范化规则。

### 17.3 Capability 兼容

Capability 不兼容 API 变更必须增加 majorVersion。增加可选字段或兼容 operation 可以保持 majorVersion，
具体规则由 CapabilityType 自身契约定义。

## 18. 安全要求

- plugin.yaml 是代码部署描述，不是普通用户输入；
- java bind 可以执行任意 JVM 代码，只能加载受信任安装包；
- process bind 可以启动本地进程，生产宿主必须显式启用并配置 allowlist；
- command 不经过 shell；
- http URL 禁止 user-info，凭证必须来自 Secret 配置；
- Secret 不得出现在 manifest、Snapshot、日志和错误详情；
- JSON Schema 输入必须设置大小、深度、数组长度和字符串长度限制；
- YAML parser 必须限制文档大小、节点数和递归深度；
- 默认 `nexus.plugin.auto-install=false`；
- DSL 规范不提供安全沙箱或供应链签名保证。

## 19. 错误分类

宿主至少区分：

| 错误 | 含义 |
|------|------|
| `PLUGIN_DSL_SYNTAX_INVALID` | YAML 语法、重复键、tag 或 alias 非法 |
| `PLUGIN_DSL_SCHEMA_INVALID` | JSON Schema 不通过 |
| `PLUGIN_DEFINITION_INVALID` | 跨字段或语义规则失败 |
| `PLUGIN_DUPLICATE` | pluginId 冲突 |
| `PROVIDER_DUPLICATE` | providerId 插件内冲突 |
| `CAPABILITY_TYPE_UNKNOWN` | CapabilityTypeRegistry 不存在类型 |
| `CAPABILITY_TYPE_MISMATCH` | Java API 或实现类型不匹配 |
| `UNSUPPORTED_BIND_KIND` | DSL 合法但宿主未实现 bind |
| `UNSUPPORTED_EXPOSURE_KIND` | DSL 合法但宿主未实现 exposure |
| `UNSUPPORTED_CONTRIBUTION_TYPE` | DSL 合法但宿主未实现 Contribution |
| `PLUGIN_RESOURCE_CONFLICT` | exposure、module、page、menu 等身份冲突 |

诊断必须包含 sourceLocation、JSON Pointer/YAML 路径和安全的错误说明，不得包含 Secret。

## 20. 生产者一致性

一个 DSL v1 生产者必须：

- 输出 UTF-8 YAML 1.2；
- 输出固定 apiVersion/kind；
- 生成插件内唯一 providerId；
- 不输出未知字段和 null；
- 通过官方 JSON Schema；
- 执行本文跨字段校验；
- 声明所需宿主能力档案；
- 不假设 YAML 顺序决定 Provider 路由。

## 21. 宿主一致性

一个 DSL v1 宿主必须：

- 严格解析并拒绝重复键、未知字段和不安全 YAML 特性；
- 执行 Schema 和语义校验；
- 发布支持的 DSL、bind、exposure 和 Contribution 类型；
- 对合法但不支持的类型返回明确错误；
- 在 ACTIVE 前隐藏全部资源；
- 使用 PluginManager 作为运行状态事实源；
- 使用 PluginInstallationManager 管理安装意图；
- 不把 Java 类名、顺序或 providerId 当作 Tags 路由歧义决胜规则；
- 不直接序列化运行时定义到数据库。
