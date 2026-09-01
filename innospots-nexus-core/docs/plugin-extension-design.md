# Nexus 统一插件扩展体系设计

## 1. 文档定位

本文定义 Nexus 唯一的插件扩展体系，统一原 Console Extension 与 Core Plugin 的身份、发现、
配置、依赖、启停、生命周期和诊断能力。插件可用 Java 代码或 YAML DSL 声明，但必须编译为同一份
`PluginDefinition`。同时保留两类不同的扩展语义：

- **Capability**：可调用、可依赖、可按 Tags 路由的运行时服务能力；
- **Contribution**：由宿主模块解释、校验和发布的声明型贡献，例如 Console 模块、菜单和页面。

本文是后续插件工程变更的现行设计依据。原设计已移入 [`archive/`](archive/README.md)，仅用于历史追溯。
本次变更只确定目标模型、职责边界、生命周期事务和迁移原则，不修改 Java、数据库或 Maven 配置。

## 2. 核心结论

Nexus 对插件开发者只提供一个顶层概念：`Plugin`。

```text
一个 Plugin 规范
        +
Java SPI 或 YAML DSL（两种声明表面）
        +
一个 PluginDefinition
        +
一个 PluginManager Runtime
        +
Capability + Contribution 两类扩展面
```

必须遵守以下决策：

1. 每个插件通过 Java `ServiceLoader<Plugin>` 或 YAML DSL 发现，不再使用 `ConsoleExtensionProvider` SPI；
2. `pluginId` 是安装、配置、启停、诊断和持久化关联的唯一插件身份；
3. 插件版本只在 `PluginDefinition` 声明一次；
4. Core `PluginManager` 是实际运行状态的唯一事实源；
5. Console 只保存管理员启停意图和历史安装快照，不维护第二套运行状态机；
6. Capability 和 Console Contribution 参加同一个插件启动与停止事务；
7. Core 不理解菜单、页面、权限和 Jakarta REST 语义，具体 Contribution 由所属模块处理；
8. 插件的所有资源通过统一可用性门控发布，外部不能观察到半激活插件；
9. Java 与 YAML 是同一规范的两种声明表面：Java 服务 JVM 实现，YAML 服务跨语言安装包，Catalog 只接受编译后的 `PluginDefinition`。

## 3. 术语

| 术语 | 定义 | 不是 |
|------|------|------|
| Plugin | 发现、配置、依赖解析、启停和失败隔离单元 | 一个具体业务能力实现 |
| PluginDefinition | 无副作用、不可变的插件静态声明 | 运行时状态容器 |
| Capability | 业务代码可调用的类型安全服务契约 | 页面或菜单描述 |
| CapabilityProvider | Capability 的具体运行时实现 | 插件发现入口 |
| Contribution | 提交给宿主模块解释的声明型扩展面 | Tags 路由的服务 Provider |
| ContributionHandler | 一类 Contribution 的校验、暂存、发布和撤出适配器 | 第二个 PluginManager |
| desiredEnabled | 管理员持久化的期望启用状态 | 当前 JVM 真实状态 |
| PluginState | PluginManager 持有的本次运行真实状态 | 数据库安装状态 |
| MISSING | 历史安装记录存在，但当前 PluginCatalog 未发现插件 | Runtime 状态 |

本文不再使用 `Extension` 表示独立安装或生命周期单元。原 Console Extension 的领域含义由
`ConsolePluginContribution` 承接，顶层统一称为插件。

## 4. 目标与非目标

### 4.1 目标

- 消除双 SPI、双身份、双版本和双状态机；
- 保留 Capability 类型安全、依赖、Tags 路由、配置和资源托管；
- 保留 Console 模块、页面、菜单、UiSpec 和权限资源模型；
- 保留 Capability 对外 exposures（HTTP / Command / MCP）；
- 允许插件只有 Capability、只有 Contribution，或同时提供两者；
- Contribution 机制可扩展，但不为工作台、设置、对话等页面形态预置新类型；那些属于页面 DSL；
- 保证单插件激活和停止的原子可见性与失败回滚；
- 保持 Core 和 Console 业务中立，Kernel 与 Platform 平行且互不依赖；
- 提供可分阶段执行的兼容迁移路径。

### 4.2 非目标

本设计不引入：

- PF4J、OSGi、Spring Boot 自动配置或 classpath 反射扫描；
- JSON 或 XML 插件描述文件（YAML 只作为与 Java 对等的声明表面，见第 8.3 节）；
- 运行时下载、安装、替换或卸载 JAR；
- 独立 ClassLoader、依赖隔离、安全沙箱或热重载；
- 插件市场、签名和供应链信任模型；
- 动态 REST 容器和 UiSpec 渲染器的具体实现；
- 具体角色、用户、授权策略或数据权限规则；
- 本文之外的工程代码和表结构变更。

## 5. 模块职责

依赖方向保持：

```text
innospots-nexus-base
        ↓
innospots-nexus-core
        ↓
innospots-nexus-console
        ↓
kernel / platform
```

### 5.1 Base

Base 提供异常、状态码、国际化值对象、UiSpec 契约等无中间件基础能力，不包含插件状态和发现。

### 5.2 Core

Core 拥有业务中立插件内核：

- `Plugin` 唯一 SPI、PluginDefinition、PluginCatalog；
- Classpath 发现、配置、Capability、Tags 和依赖；
- 通用 Contribution 类型与 ContributionHandler 生命周期契约；
- PluginManager、PluginState、PluginRuntimeInfo；
- 统一可用性门控、ResourceScope、事件和回滚协调。

Core 不依赖 Console，不解释 Console Contribution，也不注册菜单、页面或权限。Capability exposures
由 Core / 应用适配器按声明挂出，不经过 Console Contribution。

### 5.3 Console

Console 拥有业务中立管理平台扩展面：

- `ConsolePluginContribution` 及模块、页面、菜单声明；
- Console Contribution 全局校验和激活处理器；
- 页面路径、UiSpec、页面 URL 权限适配边界；
- 插件安装快照、管理员启停意图和 MISSING 对账；
- PluginManager 的管理查询与命令适配。

Console 不重新发现或实例化 Plugin，不解析 Capability 依赖，不维护第二套真实运行状态，也不登记
Capability exposures 或 Endpoint Class。

### 5.4 Kernel、Platform 与应用适配器

Kernel 和 Platform 可以提供实现 `Plugin` 的业务插件，需要管理页面时声明 Console Contribution，
但不得互相依赖或各自实现插件内核。最终应用负责选择 ClassLoader、发现 Catalog、对账启停意图、
注册 ContributionHandler，并装配 REST/Command/MCP exposure 适配器、UiSpec 和权限适配器。

## 6. 总体架构

```text
Application Classpath / 安装包
        │
        ├── META-INF/services/...Plugin          Java 声明
        └── META-INF/nexus/plugin.yaml           YAML 声明
                        │
                        ▼
               统一编译为 PluginDefinition
                        │
                        ▼
                  PluginCatalog
                        │
          ┌─────────────┴─────────────┐
          ▼                           ▼
  Console 安装意图对账          Core 定义全局预检
          └─────────────┬─────────────┘
                        ▼
                 PluginManager
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
    Capability      Contribution   ResourceScope
     Registry         Handlers      / EventBus
          │             │
          ▼             ▼
  Application API   Console 资源视图
```

```text
PluginDefinition
├── identity / metadata / version
├── tags / config / requirements
├── capabilities
│   ├── type@majorVersion / tags
│   ├── bind（实现适配：Java Factory ⇒ inprocess；YAML 显式声明）
│   └── exposures[]（可选对外调用入口）
│       ├── http
│       ├── command
│       └── mcp
└── contributions
    └── console@1
        ├── modules
        ├── page trees
        └── menu trees
```

## 7. 唯一 SPI 与发现

Java 插件通过唯一 SPI 发现：

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
```

跨语言或非 JVM 安装包通过 YAML DSL 发现（默认路径 `META-INF/nexus/plugin.yaml`，见第 8.3 节）。YAML 在进入 Catalog 前编译为同一份 `PluginDefinition`，并由宿主提供的适配 `Plugin` 承接生命周期。不再需要 ConsoleExtensionProvider SPI 或各 Capability API 的 SPI。发现流程为：

1. 枚举声明源（Java `Plugin` SPI 与 YAML DSL）并记录来源；
2. Java 源实例化 Plugin；YAML 源编译为 `PluginDefinition` 并包装为适配 Plugin；
3. 每个 Plugin 只调用一次无副作用的 `definition()`；
4. 对 PluginDefinition 做防御性快照；
5. 校验 pluginId、API 版本、Capability 类型和 Contribution 类型；
6. 按 pluginId 形成稳定排序的不可变 PluginCatalog；
7. 在任何 initialize、Factory 或 Handler prepare 前完成全局预检。

同一个 pluginId 只允许一种声明源。同一安装包不得同时提供会生成该 pluginId 的 Java `Plugin` 与 `plugin.yaml`。

构造函数和 definition 不得读取动态配置、连接外部系统、启动线程、写文件或调用其他能力。重复 pluginId、
API 主版本不兼容、重复 CapabilityKey、重复 ContributionType 或类型映射冲突都会终止本 Runtime 的发现。
SPI 顺序不用于启动排序、默认 Provider 选择或冲突消解。

## 8. 插件身份与定义

### 8.1 pluginId

统一后只使用 pluginId，不再新增 extensionKey。新 ID 使用小写反向域名式稳定命名空间：

```text
com.innospots.erp
com.example.message-wecom
```

pluginId 至少包含两个点分段，每段以小写字母开头，可包含小写字母、数字和连字符。它不包含版本、
环境、租户、workspace 或部署实例。现有稳定身份必须按第 20 节迁移，不能机械重命名。

### 8.2 PluginDefinition

目标定义概念如下：

```java
public record PluginDefinition(
        String pluginId,
        String version,
        int apiVersion,
        I18nObject displayName,
        I18nObject description,
        Tags tags,
        List<CapabilityContribution<?>> capabilities,
        List<CapabilityRequirement> requirements,
        ConfigDefinition config,
        List<PluginContribution> contributions
) {
}
```

pluginId 和 version 只声明一次；国际化名称和说明用于统一插件管理；tags 是 Capability Provider 的默认
路由身份，无路由需求时可为空；每种 ContributionType 每个插件至多一个声明。所有集合和 Map 防御性
复制。定义中不得保存状态、配置值、Provider、Handler、Factory、ClassLoader 或可写 Registry。

Plugin 继续提供 `definition/initialize/start/stop` 生命周期，不自行注册 Capability 或 Contribution。

### 8.3 两种声明表面，一份规范

插件只有一份规范模型：`PluginDefinition`。Java 与 YAML 是两种**声明表面**，不是两套插件内核。

| 表面 | 适用 | 源文件 / 入口 | Catalog 之前 |
|------|------|----------------|--------------|
| Java 代码 | JVM 内的 Java 模块与宿主 | `Plugin` SPI + `PluginDefinition` Builder | `definition()` 即规范对象 |
| YAML DSL | 跨语言、跨运行时、可分发安装包 | `META-INF/nexus/plugin.yaml` | 编译为同一份 `PluginDefinition` |

必须遵守：

1. **字段、校验、身份、生命周期和配置命名空间相同。** YAML 不得发明 Java 没有的插件身份或状态机。
2. **运行时只消费 `PluginDefinition`。** PluginManager、依赖解析、Handler 和配置解析不区分声明来自 Java 还是 YAML。
3. **Java 适配 JVM 实现细节。** Capability 用类型安全 API 与 `CapabilityProviderFactory` 绑定（隐含 `bind.kind=inprocess`）；Contribution 用 Java 类型声明；对外入口用 `exposures`。
4. **YAML 适配跨语言契约。** Capability 与 Contribution 用稳定类型名（`name@majorVersion`）声明；实现适配写在 `capabilities[].bind`，对外入口写在同 Capability 的 `exposures[]`；YAML 本身不加载 Class。
5. **配置分两层且两种表面共用：** 声明层是 `ConfigDefinition`（schema）；实例层是 `plugins.<pluginId>.<localKey>`。`bind` / `exposures` 是声明，不是实例配置。
6. **一个 pluginId 一种声明源。** 禁止同一身份双声明；需要给注册表分发时，可由 Java `definition()` 导出 YAML，导出物不得再作为第二次发现源。

```text
Java Plugin.definition()  ──┐
                            ├──► PluginDefinition ──► PluginCatalog ──► PluginManager
YAML plugin.yaml 编译     ──┘
```

### 8.4 Java 声明与配置示例

Java 插件实现唯一 SPI，用 Builder 声明身份、配置 schema、Capability（含可选 exposures）和 Console Contribution。
Factory 必须无副作用。`bind` 由 Factory 隐含为 `inprocess`，不写在 Builder 里。

```java
public final class WeComMessagePlugin implements Plugin {

    public static final CapabilityType<MessageSender> MESSAGE_SENDER =
            CapabilityType.of("message.sender", 1, MessageSender.class);

    public static final CapabilityType<ToolInvoker> TOOL_INVOKE =
            CapabilityType.of("tool.invoke", 1, ToolInvoker.class);

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("com.example.message-wecom")
                .version("1.2.0")
                .apiVersion(1)
                .displayName(I18nObject.of("zh-CN", "企业微信消息", "en-US", "WeCom Messaging"))
                .description(I18nObject.of("zh-CN", "向企业微信发送通知", "en-US", "Send WeCom notifications"))
                .tags(Tags.of("provider:wecom", "channel:im"))
                .config(ConfigDefinition.builder()
                        .string("baseUrl")
                                .required()
                                .description("WeCom API base URL")
                                .end()
                        .duration("timeout")
                                .defaultValue("PT10S")
                                .description("HTTP timeout")
                                .end()
                        .secret("appSecret")
                                .required()
                                .description("Application secret")
                                .end()
                        .build())
                .require(CapabilityType.of("model.provider", 1, ModelProvider.class), false)
                .provide(MESSAGE_SENDER, WeComMessageSender::new)
                .provide(TOOL_INVOKE, WeComToolInvoker::new)
                        .exposure(HttpExposure.of("POST", "/api/wecom/messages/send"))
                        .exposure(CommandExposure.of(
                                "wecom.send-message",
                                ParamSchema.object()
                                        .requiredString("toUser")
                                        .requiredString("content")
                                        .build()))
                        .exposure(McpExposure.of(
                                "wecom_send_message",
                                ParamSchema.object()
                                        .requiredString("toUser")
                                        .requiredString("content")
                                        .build()))
                        .end()
                .contribute(new ConsolePluginContribution(
                        List.of(new ConsoleModuleDeclaration(
                                "wecom",
                                I18nObject.of("zh-CN", "企业微信", "en-US", "WeCom"),
                                I18nObject.of("zh-CN", "企业微信管理", "en-US", "WeCom admin"),
                                List.of(new UiSpecPageDeclaration(
                                        "settings",
                                        "/wecom/settings",
                                        List.of(new UiSpecPageDeclaration(
                                                "message-detail",
                                                "/wecom/messages/{messageId}",
                                                List.of())))),
                                List.of(MenuDeclaration.directory(
                                        "wecom",
                                        I18nObject.of("zh-CN", "企业微信", "en-US", "WeCom"),
                                        "wecom",
                                        10,
                                        List.of(MenuDeclaration.page(
                                                "settings",
                                                I18nObject.of("zh-CN", "设置", "en-US", "Settings"),
                                                "settings",
                                                10,
                                                "settings"))))))))
                .build();
    }
}
```

对应实现类（进程内 bind，由 Factory 创建；配置从 `PluginConfig` 读取）：

```java
public final class WeComMessageSender implements MessageSender, CapabilityProvider {

    private final String baseUrl;
    private final Duration timeout;
    private final SecretValue appSecret;

    public WeComMessageSender(PluginContext context) {
        PluginConfig config = context.config();
        this.baseUrl = config.getString("baseUrl");
        this.timeout = config.getDuration("timeout");
        this.appSecret = config.getSecret("appSecret");
    }

    @Override
    public void send(String toUser, String content) {
        // 使用 baseUrl / timeout / appSecret 调用企业微信 API
    }
}

public final class WeComToolInvoker implements ToolInvoker, CapabilityProvider {

    private final WeComMessageSender sender;

    public WeComToolInvoker(PluginContext context) {
        // 可注入同插件内其它依赖，或自行读 config
        this.sender = new WeComMessageSender(context);
    }

    @Override
    public ToolResult invoke(ToolRequest request) {
        return ToolResult.of(sender.send(
                request.requiredString("toUser"),
                request.requiredString("content")));
    }
}
```

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
com.example.wecom.WeComMessagePlugin
```

实例配置（与 YAML 插件相同命名空间）：

```properties
plugins.com.example.message-wecom.baseUrl=https://qyapi.weixin.qq.com
plugins.com.example.message-wecom.timeout=PT15S
plugins.com.example.message-wecom.appSecret=${WECOM_APP_SECRET}
```

Java 表面用 Factory 绑定实现类；`exposures` 声明该 Capability 的对外入口。页面接口 URL 不在 Plugin 声明里，而在该页的 UiSpec 中。Console Contribution **不**声明 Endpoint Class。

### 8.5 YAML DSL 声明与配置示例

YAML 描述**同一份** PluginDefinition。Capability 用逻辑类型名，不写 Java Class。
`bind` 告诉宿主如何执行到实现；`exposures` 告诉宿主如何对外挂出调用入口。二者都挂在**同一个 Capability** 下。
Console Contribution 必须与 `ConsolePluginContribution` / `ConsoleModuleDeclaration` 同构：只声明模块、页面树和菜单树。文件默认位于：

```text
META-INF/nexus/plugin.yaml
```

#### 8.5.1 完整示例（Capability + http / command / mcp exposures + console）

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
    - provider:wecom
    - channel:im
  config:
    - key: baseUrl
      type: STRING
      required: true
      description: WeCom API base URL
    - key: timeout
      type: DURATION
      default: PT10S
      description: HTTP timeout
    - key: appSecret
      type: SECRET
      required: true
      description: Application secret
  requirements:
    - type: model.provider
      majorVersion: 1
      required: false
  capabilities:
    # 仅进程内被选用：无 exposures
    - type: message.sender
      majorVersion: 1
      tags:
        - provider:wecom
      bind:
        kind: process
        command: ["node", "dist/wecom-sender.js"]
        invoke: message.sender
    # 可被进程内选用，同时对外暴露 http / command / mcp
    - type: tool.invoke
      majorVersion: 1
      tags:
        - provider:wecom
        - channel:im
      bind:
        kind: process
        command: ["node", "dist/wecom-tool.js"]
        invoke: tool.invoke
      exposures:
        - kind: http
          method: POST
          path: /api/wecom/messages/send
        - kind: command
          commandId: wecom.send-message
          paramsSchema:
            type: object
            required: [toUser, content]
            properties:
              toUser: { type: string }
              content: { type: string }
        - kind: mcp
          name: wecom_send_message
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
          description:
            zh-CN: 企业微信管理
            en-US: WeCom admin
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

#### 8.5.2 分类型：仅 HTTP exposure

```yaml
capabilities:
  - type: webhook.receiver
    majorVersion: 1
    bind:
      kind: http
      baseUrl: http://127.0.0.1:8091
      invokePath: /internal/webhook
    exposures:
      - kind: http
        method: POST
        path: /api/integrations/wecom/webhook
```

含义：对外挂 `POST /api/integrations/wecom/webhook`；Capability 被选中或该入口被命中后，宿主按 `bind` 转发到 sidecar。

#### 8.5.3 分类型：仅 Command exposure

```yaml
capabilities:
  - type: ops.job
    majorVersion: 1
    tags: [job:rebuild-index]
    bind:
      kind: process
      command: ["python", "jobs/rebuild_index.py"]
    exposures:
      - kind: command
        commandId: search.rebuild-index
        paramsSchema:
          type: object
          properties:
            full: { type: boolean, default: false }
```

#### 8.5.4 分类型：仅 MCP exposure

```yaml
capabilities:
  - type: tool.invoke
    majorVersion: 1
    tags: [provider:docs]
    bind:
      kind: mcp
      server: docs-tools
      tool: search_docs
    exposures:
      - kind: mcp
        name: search_docs
        inputSchema:
          type: object
          required: [query]
          properties:
            query: { type: string }
```

`bind.kind=mcp`：实现本身挂在某个 MCP server 上。  
`exposures.kind=mcp`：把该能力挂进本平台对外的 MCP 工具列表。二者可同时出现，语义不同。

#### 8.5.5 分类型：纯进程内 Capability（无 exposure）

标准路径用 **Java** `.provide`，不要用跨语言 YAML 找 Class（详见 8.6.2）：

```java
.provide(ArtifactViewer.TYPE, MarkdownArtifactViewer::new)
// 无 .exposure(...) —— 仅 CapabilityManager 按 Tags 选用
```

跨语言包若只声明身份、实现另绑：

```yaml
capabilities:
  - type: artifact.viewer
    majorVersion: 1
    tags:
      - content-type:markdown
    bind:
      kind: contract
```

#### 8.5.6 Java 表面与 YAML 字段对照（Capability）

| 关注点 | Java | YAML |
|--------|------|------|
| 能力身份 | `CapabilityType.of("tool.invoke", 1, ToolInvoker.class)` | `type: tool.invoke` + `majorVersion: 1` |
| 实现绑定 | `CapabilityProviderFactory`（隐含 `inprocess`）；**无注解** | `bind.kind`；跨语言不用 Class；JVM 扩展可用 `bind.class` |
| 实现类 | Factory 构造 `WeComToolInvoker` 等 | 不出现 Class 名 |
| 对外 HTTP | `.exposure(HttpExposure.of(method, path))` | `exposures[].kind: http` |
| 对外命令 | `.exposure(CommandExposure.of(commandId, schema))` | `exposures[].kind: command` |
| 对外 MCP | `.exposure(McpExposure.of(name, inputSchema))` | `exposures[].kind: mcp` |
| 实例配置 | `plugins.<pluginId>.*` | 同左 |
| Console | `ConsolePluginContribution(modules)` | `contributions[console].modules` |

`console@1` 字段与 Java 声明一一对应：

| YAML | Java | 作用 |
|------|------|------|
| `modules[].moduleKey` | `ConsoleModuleDeclaration.moduleKey` | 管理资源命名空间 |
| `modules[].displayName` / `description` | 同名字段 | 模块国际化元数据 |
| `modules[].pages[]` | `List<UiSpecPageDeclaration>` | 页面树：领域归属 |
| `pages[].pageKey` | `UiSpecPageDeclaration.pageKey` | 模块内页面身份，等于 UiSpec `pageInfo.pageId` |
| `pages[].pagePath` | `UiSpecPageDeclaration.pagePath` | Console 路由模板，只做路径匹配 |
| `pages[].children[]` | `UiSpecPageDeclaration.children` | 子页面，单向领域关系 |
| `modules[].menuTree[]` | `List<MenuDeclaration>` | 导航树 |
| `menuTree[].menuKey` / `title` / `icon` / `orderIndex` | 同名字段 | 菜单节点 |
| `menuTree[].pageKey` xor `children` | 同名字段组合 | 页面入口或目录，二者不能同时有或同时无 |

YAML **不声明**下列内容：

| 不出现在 YAML / Contribution | 原因 |
|------------------------------|------|
| `endpointTypes` / 独立 `endpoints` 清单 | 已废除：对外 HTTP 属于所属 Capability 的 `exposures`；页面接口只在 UiSpec |
| Java Class / Factory 名 | YAML 用 `bind`；Class 只属于 Java 表面 |
| UiSpec 正文、组件树、标题 | 页面声明只含 `pageKey` + `pagePath` + `children` |
| 页面接口 URL | 来自该页 UiSpec；权限身份是 `(moduleKey, pageKey, normalizedUrlPattern)` |
| 角色、权限码、授权策略 | Contribution 只声明资源事实 |

因此 `message-detail` 出现在页面树中，成为 `page:wecom.message-detail`，但不进入菜单：带路径变量的页面不能作为静态菜单入口。对应 UiSpec 仍是独立文件，例如：

```text
ui-spec/wecom/settings.yaml
ui-spec/wecom/message-detail.yaml
```

```yaml
# ui-spec/wecom/settings.yaml  （不是 plugin.yaml）
pageInfo:
  pageId: settings
  title:
    zh-CN: 设置
    en-US: Settings
pageType: form
datasources:
  config:
    method: GET
    url: /api/wecom/settings
```

实例配置与 Java 插件相同：

```properties
plugins.com.example.message-wecom.baseUrl=https://qyapi.weixin.qq.com
plugins.com.example.message-wecom.timeout=PT15S
plugins.com.example.message-wecom.appSecret=${WECOM_APP_SECRET}
```

### 8.6 Capability：定义、配置、使用

Capability 是插件对外提供**可路由能力**的单元。对外可调用入口（HTTP / Command / MCP）作为该 Capability 的可选 `exposures`，不单独顶层列表，也不放进 Console Contribution。

#### 8.6.1 定义（声明期 → PluginDefinition）

| 字段 | 含义 | 必填 |
|------|------|------|
| `type` + `majorVersion` | 能力逻辑身份，格式 `name@majorVersion` | 是 |
| `tags` | Provider 路由身份；可继承 Plugin tags | 否 |
| `bind` | 实现适配：被选中或 exposure 命中后如何执行到实现 | YAML 必填；Java 由 Factory 隐含 `inprocess` |
| `exposures[]` | 对外暴露通道；每条归属本 Capability | 否 |

V1 禁止“无 Capability 的 orphan exposure”。没有可路由实现却要对外 HTTP 时，必须先声明 Capability。页面 DSL 中的 URL **不是** exposure，不要求也不允许登记到 Capability。

`bind.kind`（实现适配）与 `exposures[].kind`（暴露通道）不得混用：

| `bind.kind` | 含义 |
|-------------|------|
| `inprocess` | 同 JVM；**标准路径是 Java Factory**；YAML 见 8.6.2 |
| `http` | 宿主 HTTP 客户端调 sidecar / 远程实现 |
| `process` | 宿主拉起或附着进程/sidecar |
| `mcp` | 实现挂在外部 MCP server/tool 上 |
| `contract` | 仅登记能力身份，实现由安装环境绑定 |

| `exposures[].kind` | 含义 | 关键字段 |
|--------------------|------|----------|
| `http` | 对外 HTTP API | `method`, `path` |
| `command` | 对外可执行命令 | `commandId`, `paramsSchema` |
| `mcp` | 挂入平台 MCP 工具表 | `name`, `inputSchema` |

同一 Runtime 内 `http` 的 method+path、`commandId`、`mcp.name` 全局唯一；冲突在 Catalog 预检失败。禁用插件仍参加冲突校验。

#### 8.6.2 纯进程内：type / 版本 / tags 如何落到实现类

**结论先说清楚：**

1. **不靠注解扫描。** 实现类**不需要** `@Capability`、`@Provider`、`@Service` 之类标注来被发现。
2. **匹配关系在 `PluginDefinition` 里显式写死：** `CapabilityType(type, majorVersion, API)` → `CapabilityProviderFactory` → 具体实现类。
3. **`type` + `majorVersion` 只决定“哪一类能力”；`tags` 只决定“多个实现里选哪一个”；二者都不编码 Class 名。**
4. **YAML 跨语言规范不通过 Class 名加载 JVM 实现。** 纯进程内 Java 插件用 Java SPI + `.provide(...)`。若安装包只有 YAML、却要跑同 JVM 实现，见文末“JVM 专用 YAML 扩展”，那是宿主适配，不是 Core 反射扫包。

##### 类型关系（与现行 Runtime 一致）

```text
Capability API 接口（如 MessageSender）
        ↑ implements（编译期 / 运行期 isInstance 校验）
具体实现类（如 WeComMessageSender）—— 无发现用注解
        ↑ create()
CapabilityProviderFactory          —— Plugin.definition() 里显式传入
        ↓ 与 CapabilityType 组成
CapabilityContribution             —— 写入 PluginDefinition
        ↓ Plugin 启动成功后发布
CapabilityRegistration(type, provider, pluginId, tags)
        ↓
CapabilityRegistry / CapabilityRouter / CapabilityManager
```

| 概念 | 回答什么 | 示例 |
|------|----------|------|
| `CapabilityKey` | 逻辑身份 | `message.sender@1`（name + majorVersion） |
| `CapabilityType` | Key + Java API 接口 | `CapabilityType.of("message.sender", 1, MessageSender.class)` |
| Plugin / Capability `tags` | 路由维度 | `provider=wecom`, `channel=im` |
| `CapabilityProviderFactory` | 如何 new 实现 | `WeComMessageSender::new` |
| `CapabilityRegistration` | ACTIVE 后可被路由选中的一条记录 | type + 实例 + pluginId + tags |

##### 实现类要满足什么（不是“标注什么”）

实现类只需：

1. **实现**对应 Capability API 接口（该接口必须 extends `CapabilityProvider`）；
2. **可被 Factory 无副作用地创建**（构造阶段不联网、不启动线程、不注册全局状态）；
3. 初始化副作用放在 `CapabilityProvider.initialize()`（由 Runtime 调用）。

**没有**强制注解。可选的文档性注解（若工程自行加）不参与发现与路由。

```java
/** 能力契约：放在宿主或共享 API 模块，业务只依赖它。 */
public interface MessageSender extends CapabilityProvider {
    void send(String toUser, String content);
}

/**
 * 具体实现：普通 Java 类，无发现注解。
 * 与 CapabilityType 的关联不在本类上声明，而在 Plugin.definition().provide(...) 中声明。
 */
public final class WeComMessageSender implements MessageSender {

    private final String baseUrl;
    private final SecretValue appSecret;

    public WeComMessageSender(PluginContext context) {
        PluginConfig config = context.config();
        this.baseUrl = config.getString("baseUrl");
        this.appSecret = config.getSecret("appSecret");
    }

    @Override
    public void initialize() {
        // 允许失败回滚的初始化（校验配置、预热客户端等）
    }

    @Override
    public void send(String toUser, String content) {
        // 使用 baseUrl / appSecret 调用外部 API
    }
}
```

##### Java：显式绑定（纯进程内标准路径）

```java
public final class WeComMessagePlugin implements Plugin {

    public static final CapabilityType<MessageSender> MESSAGE_SENDER =
            CapabilityType.of("message.sender", 1, MessageSender.class);

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("com.example.message-wecom")
                .version("1.2.0")
                .apiVersion(1)
                .tags(Tags.of("provider", "wecom").and("channel", "im"))
                .config(/* baseUrl, appSecret, ... */)
                // type+version+API  与  实现类 Factory  在此绑死
                .provide(MESSAGE_SENDER, ctx -> new WeComMessageSender(ctx))
                .build();
    }
}
```

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
com.example.wecom.WeComMessagePlugin
```

启动时 Runtime：

1. `ServiceLoader` 只发现 **Plugin**，不扫描 Capability 实现类；
2. 调用一次 `definition()`，得到 `CapabilityContribution(MESSAGE_SENDER, factory)`；
3. 全局预检：同 Plugin 内 `CapabilityKey` 不重复；Factory 产物必须是 `type.api()` 的实例；
4. 插件启动事务里 `factory.create()` → `initialize()` → 发布
   `CapabilityRegistration(MESSAGE_SENDER, instance, "com.example.message-wecom", pluginTags)`。

因此：**实现类是被 Plugin 的 `.provide` 点名的，不是被 YAML 或 ClassPath 扫出来的。**

##### Tags 路由（多个同 type@version 实现时如何选中）

假设同时 ACTIVE：

| pluginId | CapabilityKey | tags |
|----------|---------------|------|
| `com.example.message-wecom` | `message.sender@1` | `provider=wecom`, `channel=im` |
| `com.example.message-dingtalk` | `message.sender@1` | `provider=dingtalk`, `channel=im` |

业务调用：

```java
MessageSender sender = capabilities.require(
        MessageSender.TYPE,
        Tags.of("provider", "wecom"));
sender.send(user, text);
```

路由规则（与现行 `CapabilityRouter` 一致）：

1. 先按 **`CapabilityKey`（type + majorVersion）** 取出所有 ACTIVE Registration；
2. 再按 Tags：**请求 Tags ⊆ Provider Tags**（`provider.tags.matches(required)`）；
3. 请求 Tags 为空时，改用宿主为该 Key 配置的 **default route**；仍空则若仅有一个 ACTIVE Provider 则选它，多个则歧义失败；
4. 匹配 0 个 → 未找到；匹配 ≥2 个 → `CAPABILITY_AMBIGUOUS`，**不用 SPI 顺序决胜**。

```text
require(message.sender@1, {provider=wecom})
        ↓
Registry: 该 Key 下全部 ACTIVE
        ↓
filter: tags 包含 provider=wecom
        ↓
恰好 1 个 → 返回 WeComMessageSender 实例
```

**注意：** tags 不负责“找到 Class”；Class 在注册时已经由 Factory 创建好了。tags 只在**已注册实例集合**里做子集过滤。

##### YAML：纯进程内如何“找到实现”

跨语言 `plugin.yaml` **不得**把 Java FQCN 当作规范必填字段，也**不得**靠注解/包扫描加载实现。对应关系如下：

| 场景 | YAML 怎么写 | 实现从哪来 |
|------|-------------|------------|
| JVM 插件、纯进程内 | **不要用 YAML 描述该 Plugin**；用 Java `Plugin` + `.provide` | Factory 直接 `new` 实现类 |
| 非 JVM / sidecar | `bind.kind: process\|http\|mcp` | 宿主适配器按 bind 调用外部进程，无 Java Class |
| 仅占位身份 | `bind.kind: contract` | 安装环境另行绑定实现 |
| 例外：JVM 宿主想用 YAML 描述同进程插件 | 见下方 **JVM 专用扩展** | 宿主按显式 `class` 加载，仍不扫描注解 |

**推荐的“纯进程内”YAML 形态：不要写。** 同能力的跨语言包用 `process`/`http`；JVM 包用 Java 表面。

若产品坚持“一份 YAML 也能挂 JVM 实现”，允许**仅 JVM 宿主**识别的扩展（导出给注册表时可剥离），且必须显式 class，禁止扫包：

```yaml
# 仅 JVM 宿主扩展；不是跨语言规范必填
capabilities:
  - type: message.sender
    majorVersion: 1
    tags:
      - provider:wecom
      - channel:im
    bind:
      kind: inprocess
      class: com.example.wecom.WeComMessageSender   # 显式 FQCN
      # 可选：要求实现的 API，供宿主校验
      api: com.example.api.MessageSender
```

宿主编译该 YAML 时的步骤：

```text
读 plugin.yaml
  → Class.forName(bind.class, false, pluginClassLoader)
  → 校验实现 api / CapabilityProvider
  → 生成与 Java .provide 等价的 CapabilityProviderFactory
       () -> (MessageSender) constructor.newInstance(pluginContext)
  → 写入同一份 PluginDefinition
  → 之后路由与纯 Java 插件完全相同（CapabilityRouter + Tags）
```

**仍然没有注解参与。** `class` 是显式配置；找不到类、不是接口实现、无合适构造函数 → Catalog / 启动失败。

等价对照：

```text
Java:
  .provide(MESSAGE_SENDER, ctx -> new WeComMessageSender(ctx))

JVM-YAML 扩展:
  bind: { kind: inprocess, class: com.example.wecom.WeComMessageSender }
        ↓ 宿主编译
  同一 CapabilityContribution(MESSAGE_SENDER, factory)
```

##### 端到端示例（两插件 + 路由）

```java
// 插件 A
PluginDefinition.builder("com.example.message-wecom")
    .tags(Tags.of("provider", "wecom").and("channel", "im"))
    .provide(MessageSender.TYPE, ctx -> new WeComMessageSender(ctx))
    .build();

// 插件 B
PluginDefinition.builder("com.example.message-dingtalk")
    .tags(Tags.of("provider", "dingtalk").and("channel", "im"))
    .provide(MessageSender.TYPE, ctx -> new DingTalkMessageSender(ctx))
    .build();
```

```java
// 业务侧：只认 CapabilityType + Tags，不认实现类名
MessageSender wecom = capabilities.require(
        MessageSender.TYPE, Tags.of("provider", "wecom"));

MessageSender anyIm = capabilities.require(
        MessageSender.TYPE, Tags.of("channel", "im"));
// ↑ 若 wecom 与 dingtalk 都 ACTIVE，这里歧义失败 —— 调用方必须加 provider 或依赖宿主 default route
```

宿主 default route 示例（实例配置 / RuntimeConfig，不是 plugin.yaml 的 bind）：

```properties
# 概念示意：当 require 未传 tags 时，message.sender@1 默认选 provider=wecom
nexus.capability.defaults.message.sender.1=provider=wecom
```

#### 8.6.3 配置（运行期实例）

只使用插件配置命名空间，**不**用配置改 `bind.kind`、Class 名或增删 exposures：

```text
plugins.<pluginId>.<localKey>
```

```properties
plugins.com.example.message-wecom.baseUrl=https://qyapi.weixin.qq.com
plugins.com.example.message-wecom.appSecret=${WECOM_APP_SECRET}
```

Factory / YAML 适配器通过只读 `PluginConfig` 读取。Secret 规则与第 15 节相同。
**配置不参与“找到哪个实现类”；** 只注入已被 `.provide` / `bind.class` 选定的那个实例。

#### 8.6.4 使用（调用期）

```text
进程内业务
  → CapabilityManager.require|find(type@majorVersion, tags)
  → CapabilityRouter 子集匹配
  → 唯一 ACTIVE CapabilityRegistration.provider
  → 已是具体实现类实例（Java Factory 或 JVM-YAML class 适配器创建）

外部 HTTP / Command / MCP
  → 命中某 Capability.exposures[] 条目
  → 进入同一 Capability 执行路径（同一 Provider / bind）
  → 必须通过所属 Plugin 的可用性门控
```

ACTIVE 前，Provider 与 exposures 不可被外部观察到或调用；停止时先关门控再撤出注册与暴露索引。

#### 8.6.5 YAML 编译规则与字段对照

| YAML 字段 | PluginDefinition |
|-----------|------------------|
| `metadata.pluginId` | `pluginId` |
| `metadata.version` | `version` |
| `spec.apiVersion` | `apiVersion` |
| `spec.displayName` / `description` | 国际化元数据 |
| `spec.tags` | `Tags` |
| `spec.config[]` | `ConfigDefinition` |
| `spec.requirements[]` | `CapabilityRequirement` |
| `spec.capabilities[].type@majorVersion` | Capability 逻辑身份 |
| `spec.capabilities[].tags` | Provider Tags |
| `spec.capabilities[].bind` | 实现适配，不进入 Core 状态机字段机 |
| `spec.capabilities[].bind.class` | **仅 JVM 扩展** → Factory；跨语言包不得依赖 |
| `spec.capabilities[].exposures[]` | 该 Capability 的对外入口 |
| `spec.contributions[]` | `PluginContribution`，按 `type@majorVersion` 分发给 Handler |
| `contributions[console].modules` | `ConsolePluginContribution.modules` |

`nexus.plugin/v1` 与 Java `PluginDefinition.CURRENT_API_VERSION = 1` 对齐。未知 `bind.kind` / `exposures.kind`、未知 config `type`、非法 pluginId、非法页面树/菜单树、exposure 冲突、或 Contribution 类型宿主未注册，均在 Catalog 预检失败，语义与 Java 定义非法相同。

跨语言 YAML 不得包含 Class 名、ServiceLoader 项或可执行脚本作为规范必填字段。纯进程内 Java 实现应使用 Java 声明表面（`.provide`），而不是把发现建立在注解或 ClassPath 扫描上。

## 9. Capability 与 Contribution 的区别

Capability 继续通过以下方式使用：

```text
CapabilityType + Tags
        ↓
CapabilityManager
        ↓
唯一 ACTIVE CapabilityProvider
```

它是可调用服务，支持多个实现、路由和依赖。Capability 可通过可选 `exposures` 对外挂出 HTTP /
Command / MCP 入口；暴露索引与 Capability 同生命周期。Contribution 是需要聚合全部活动插件的声明
资源，具有宿主特定的冲突、持久化和撤出规则。页面、菜单不得包装成普通 CapabilityProvider，否则无法在
插件启动前完成全局冲突校验，也会错误地把累加型资源当成可替换服务。对外 API 入口属于 Capability.exposures，
不属于 Console Contribution。

## 10. 通用 Contribution 模型

Core 提供最小类型化契约：

```java
public interface PluginContribution {

    PluginContributionType<? extends PluginContribution> type();
}

public record PluginContributionType<T extends PluginContribution>(
        String name,
        int majorVersion,
        Class<T> declarationType
) {
}
```

类型身份为 `name@majorVersion`，例如 `console@1`。Core 校验类型身份、Java 类型一致性及插件内唯一性，
但不反射解释具体字段。

宿主模块显式注册处理器：

```java
public interface PluginContributionHandler<T extends PluginContribution> {

    PluginContributionType<T> type();

    void validate(PluginCatalog catalog, List<PluginContributionEntry<T>> entries);

    PreparedPluginContribution prepare(
            PluginContributionContext context,
            T contribution);
}
```

处理器语义：

- validate 无副作用，负责同类型全局冲突校验；
- prepare 可以读取当前插件已校验的只读配置，但不发布可见资源；
- 返回句柄负责 stage、commit、rollback 和 close；
- Handler 不创建第二套插件状态，不直接把插件标记为 ACTIVE；
- commit 只发布已完整准备的内容，不再执行高风险 I/O；
- 未注册 required Handler 时，声明该 Contribution 的插件启动失败并给出明确诊断。

V1 冻结的 Contribution 类型只有 `console@1`。未声明该类型的插件仍然合法。声明了 `console@1` 但宿主未注册对应 Handler 时，该插件启动失败。

### 10.1 何时用 Contribution，何时用 Capability，何时用页面 DSL

| 用 Contribution | 用 Capability | 用页面 DSL / UiSpec |
|-----------------|---------------|---------------------|
| 需要聚合全部活动插件的资源身份 | 业务按类型 + Tags 选择一个实现 | 某一页如何布局、渲染和配表单 |
| 启动前必须做全局冲突校验 | 运行时路由，歧义才失败；可选 exposures 对外暴露 | 只作用于该 `moduleKey + pageKey` |
| 停用后从模块、菜单、页面目录撤出 | 停用后从 CapabilityRegistry 与 exposure 索引撤出 | 随所属页面一起消失 |

页面、菜单不得做成 CapabilityProvider。模型、Agent Runtime、Tool、MCP、数字成果查看器/编辑器是 Capability。
HTTP / Command / MCP 对外入口是 Capability 的 `exposures`，不是独立 Contribution。

工作台、设置、对话、右侧看板不是新的 Contribution 类型。它们是**不同的页面**，差异在该页的布局结构，由页面端 DSL（UiSpec）描述，仍然挂在 `console@1` 已声明的 `pageKey` 上。

### 10.2 现行 Contribution 只有 console@1

身份格式与 Capability 相同：`name@majorVersion`。Core 只登记类型名和 Java 声明类型，不解释字段。

| 类型 | 阶段 | 拥有模块 | 声明内容 | YAML |
|------|------|----------|----------|------|
| `console@1` | V1 冻结 | console | 模块、页面树、菜单树 | 只映射 modules / pages / menuTree |

`console@1` 覆盖管理端与开发工作台：二者都是 Console 页面体系。Kernel 与 Platform 平行贡献同一类型，用不同 `moduleKey` 区分，资源身份仍全局唯一。

一个插件每种 ContributionType 至多一份。新增 Contribution 的前提是：出现**不是页面、不是页面 DSL、也不是可路由 Capability** 的聚合声明，并且必须在启动前做跨插件冲突校验。在该前提出现前，**不预留、不规划其他 Contribution 类型名**。

### 10.3 明确不是 Contribution 的对象

| 对象 | 归属 | 原因 |
|------|------|------|
| 工作台壳、标签页、左侧导航、右侧看板 | `console@1` 的页面 + 菜单，布局在页面 DSL | 本质是页面，不是第二套贡献面 |
| 设置表单、对话界面、卡片展示 | 该页 UiSpec / 页面 DSL | 页面内配置，不进 PluginDefinition |
| `model.provider` / `agent.runtime` / `tool.invoke` / `mcp.server` | Capability | 可路由的运行时实现 |
| `artifact.viewer` / `artifact.editor` | Capability | 按内容类型选择一个查看器/编辑器 |
| HTTP / Command / MCP 对外入口 | 所属 Capability 的 `exposures` | 能力对外调用面，不是 Contribution |
| UiSpec 正文、组件树、pageType | Base 页面契约 | 由 `moduleKey + pageKey` 加载 |
| 权限分配、角色、数据范围 | 权限模块 | Contribution 只提供资源事实 |
| 页面 datasource/action URL | 该页 UiSpec | 页面接口约束，不登记为 Capability exposure |
| `ConsoleExtensionProvider.endpointTypes` | **废除** | 旧 SPI 字段；不再迁入 ConsolePluginContribution |

## 11. ConsolePluginContribution

```java
public record ConsolePluginContribution(
        List<ConsoleModuleDeclaration> modules
) implements PluginContribution {
}
```

它不再声明 pluginId、version、displayName、description 或 endpointTypes；身份与元数据来自所属
PluginDefinition，对外 HTTP API 来自 Capability `exposures`。没有管理页面的插件可不声明它；只有
Console Contribution、没有 Capability 的插件仍然合法。

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

moduleKey 是管理资源全局稳定命名空间，不替代 pluginId。资源身份保持：

| 类型 | 示例 |
|------|------|
| MODULE | `module:sales` |
| MENU | `menu:sales.order` |
| PAGE | `page:sales.order-list` |
| PAGE URL | `(sales, order-list, /sales/orders/{orderId})` |

每个资源快照同时记录 ownerPluginId，防止另一个插件在原插件停用或 MISSING 后接管稳定资源身份。

页面树继续表达页面领域归属，菜单树表达导航关系，菜单通过 pageKey 引用同模块页面。归档扩展设计中的页面/菜单/权限规则在统一后仍然有效，不得在 YAML 或 Java 示例中简化掉：

- 一个插件可贡献多个 `moduleKey`；模块是资源命名空间，不替代 pluginId。
- `pages` 是页面树根列表；`children` 表达单向领域父子，不从 `pagePath` 推导。
- 只有独立 UiSpec `pageInfo.pageId` 的页面进入页面树；弹窗、抽屉、页签、局部视图不产生 PAGE 资源。
- `pagePath` 以 `/` 开头，`{var}` 占完整路径段；静态路径优先于变量模板；结构相同的模板视为冲突。
- 菜单节点要么是目录（`children` 非空、无 pageKey），要么是页面入口（`pageKey` 非空、无 children）。
- 未被菜单引用的页面仍是合法 PAGE；一个页面至多被一个静态菜单节点引用。
- 带必填路径变量的页面不能作为静态菜单入口。
- UiSpec 不进 Plugin 声明；默认 `ui-spec/{moduleKey}/{pageKey}.yaml`，校验 `pageInfo.pageId == pageKey`。
- 页面接口 URL 来自该页 UiSpec 的 datasource/action；HTTP method 不参与权限身份。
- 同一 URL 被不同页面引用时形成各自权限项；同一页面内重复 URL 去重为一项。
- 请求 Header 为 `X-Nexus-Page-Key: <moduleKey>.<pageKey>`，不能单独作为授权依据。
- 匹配页面路径后构造 `PageRenderRequest(moduleKey, pageKey, pathVariables)` 交给渲染模块。

对外 HTTP / Command / MCP 不在 Console Contribution 中声明。页面引用的 URL 继续来自 UiSpec；
Capability `exposures` 由 Core 在插件启动事务中挂出。运行时不支持安全动态卸载时，管理 API 必须明确
返回“重启后生效”，不得伪装成已热停用。

## 12. Console 全局校验

Console Handler 在任何插件 initialize 前校验：

- moduleKey、资源 ID 及 ownerPluginId 全局唯一；
- 同模块 pageKey、menuKey 唯一；
- 页面树无循环，每页最多一个父页面；
- pagePath 合法且不存在静态/变量模板歧义；
- 菜单字段组合合法且没有悬空 pageKey；
- UiSpec 可由 moduleKey + pageKey 唯一定位，pageInfo.pageId 一致；
- 页面引用 URL 可规范化且归属明确；
- 当前声明不冒用 MISSING 插件保留的资源身份。

Capability exposure（method+path / commandId / mcp name）的全局唯一性由 Core Catalog 预检，不由
Console Handler 解释。禁用插件仍参加身份和资源所有权冲突校验，避免启用顺序改变资源归属。运行时装配、
外部连接和 UiSpec 动态加载推迟到启动阶段。

## 13. 统一生命周期

### 13.1 状态

```text
DISCOVERED → DESCRIBED
DESCRIBED  → DISABLED | WAITING | STARTING | FAILED
WAITING    → STARTING | FAILED
STARTING   → ACTIVE | FAILED
ACTIVE     → STOPPING
STOPPING   → STOPPED | FAILED
STOPPED    → STARTING | DISABLED
DISABLED   → DESCRIBED
```

REGISTERED 不再是 Runtime 状态；安装登记属于 Console 持久化。MISSING 也不是 PluginState，因为当前
Runtime 不存在对应 Plugin 实例。

### 13.2 启动前流程

1. Core 发现 PluginCatalog 并完成通用定义预检；
2. Console 与安装快照对账，得到 desiredEnabled 和 MISSING；
3. 应用构造 disabledPluginIds、requiredPluginIds 和 Handler 集合；
4. PluginManager 调用全部 Handler 全局 validate；
5. 依赖解析器确定可启动、等待和失败插件；
6. 进入逐插件启动事务。

### 13.3 单插件启动事务

```text
1. 校验 PluginConfig 和 required Capability
2. 创建 ResourceScope、Context 和事件视图
3. ContributionHandler.prepare（外部不可见）
4. 创建 CapabilityProvider
5. plugin.initialize
6. capabilityProvider.initialize
7. plugin.start
8. Contribution 与 Capability 进入 staged 状态
9. 统一可用性门控切换为 ACTIVE
10. 发布 PluginStartedEvent
```

第 9 步是外部可见性的线性化点。Capability 查询、Capability exposures、菜单、页面和权限资源都必须检查
同一份 PluginAvailability 快照。门控切换前，内部 staged 内容不得被外部读取或调用。

### 13.4 回滚与停止

启动失败时保持 availability 不可用，逆序撤销 staged 资源、销毁 Provider、调用 plugin.stop、关闭
PreparedPluginContribution 和 ResourceScope，清除强引用，最后记录 FAILED、phase 和根因。清理失败
作为 suppressed cause 或诊断保留，不能覆盖首个失败。

停止时先 `ACTIVE → STOPPING` 并关闭 availability，拒绝新查询和请求；随后撤出活动索引，逆序销毁
Provider、停止 Plugin、关闭 Contribution 与 ResourceScope，清除引用并进入 STOPPED。V1 不提供完整
调用引用计数，已取得 Provider 或已进入 exposure / 页面接口调用的请求可能与停止并发。

## 14. 安装、启用和 MISSING

| 事实 | 来源 | 生命周期 |
|------|------|----------|
| installed | 当前 PluginCatalog 是否发现 pluginId | 每次启动重建 |
| desiredEnabled | 管理员启停意图 | Console 持久化 |
| runtimeState | 实际启动、等待、失败或停止状态 | PluginManager 实例 |

首次发现 pluginId 时创建安装快照并默认启用。禁用后，下次启动继续生成 disabledPluginIds。JAR 消失时
记录进入 MISSING，但保留 pluginId、最后版本、desiredEnabled、定义与资源摘要、权限分配、
ownerPluginId、最后发现时间和诊断。相同 pluginId 恢复并声明相同资源身份后可继续关联原权限。

管理端 enable/disable 先持久化 desiredEnabled，再调用 PluginManager start/stop，最后同时返回期望状态
和真实状态。若只能重启生效，必须明确展示差异。

## 15. 配置、依赖与权限

配置统一使用归档已实现的命名空间，不以 `nexus.plugins` 另起一套：

```text
plugins.<pluginId>.<localKey>
```

当前 Java Runtime 的 plugin `id` 仍是 kebab-case（如 `message-wecom`）。目标 `pluginId` 改为反向域名后，
完整 key 仍按点分段书写，环境变量把 `.` 与 `-` 压成 `_`：

```text
plugins.com.example.message-wecom.http.connectTimeout
        ↓
NEXUS_PLUGIN_COM_EXAMPLE_MESSAGE_WECOM_HTTP_CONNECT_TIMEOUT
```

局部 key 不得自带 `plugins.<id>` 前缀。配置来源优先级（低到高）保持归档定义：

```text
Plugin Default < Host Map < Environment < System Property < Runtime Variables
```

未知 key、缺 required、类型转换失败、Secret 带默认值、两个 key 映射同一环境变量，均使该插件配置非法。
Secret 不出现在日志、诊断和 `toString()`。五级来源的具体解析仍由 Core `ConfigurationManager` 完成；
YAML 只声明 schema，不替代宿主配置文件。

Tags 路由规则保持归档定义：请求 Tags 是 Provider Tags 的子集；Provider 继承 Plugin Tags，V1 无
Provider 局部 Tags。选择顺序为：调用方 Tags → 宿主默认 Tags → 唯一 ACTIVE Provider → 未找到 /
多匹配失败。同一 PluginDefinition 内禁止重复 CapabilityKey。

显式停止单个插件前，若某个 ACTIVE 插件的 required Capability 将失去最后一个 Provider，则拒绝停止。
应用整体关闭时按启动逆序停止，不走单插件依赖保护。

ConfigDefinition、Secret 掩码和 PluginConfig 由 Core 管理。Handler 只能读取所属插件的只读配置。依赖
包括 Plugin ID 依赖、CapabilityRequirement 和宿主是否支持 ContributionType；Console 不建立另一套
依赖解析器。

Console Contribution 只声明资源事实，不声明用户、角色、默认授权、数据范围或内建权限码。页面接口
权限身份保持 `(moduleKey, pageKey, normalizedUrlPattern)`，请求继续使用：

```http
X-Nexus-Page-Key: sales.order-list
```

拦截器必须确认 ownerPluginId ACTIVE、页面属于活动 Contribution、URL 属于该页面 UiSpec，并从登录会话
校验角色。插件停用或 MISSING 时不删除权限分配，但固定拒绝资源访问。

## 16. 并发、错误和诊断

PluginManager 串行协调 discover/start/stop/close，但不在持有内部锁时调用未知插件或外部高风险代码。
Capability、Contribution 和 availability 使用不可变快照及原子替换；单次查询读取同一 availability 版本；
停止先关闭门控再释放资源。

插件错误统一使用 NexusException 和插件技术状态码。PluginRuntimeInfo 应展示 pluginId、version、state、
phase、desiredEnabled、Capabilities、Contributions、依赖、发现与启动时间和最后错误，不得暴露 Plugin、
Provider、Handler、ClassLoader、Factory、配置值、Secret 或运行时资源对象。

批量启动不是全局事务。普通插件失败不回滚其他普通插件；required plugin 失败时，宿主可以关闭本轮已
启动插件并使应用启动失败。

## 17. 宿主接入

目标装配流程：

```java
PluginCatalog catalog = PluginCatalog.discover(applicationClassLoader);
PluginEnablement enablement = consolePluginRepository.reconcile(catalog);
PluginRuntimeConfig config = runtimeConfigFactory.create(catalog, enablement);

try (PluginManager plugins = DefaultPluginManager.create(
        config,
        catalog,
        List.of(consolePluginContributionHandler))) {
    plugins.start();
    application.start(plugins.capabilities());
}
```

示例表示装配关系，不锁定未来方法签名。必须保持显式构造、实例级 Runtime、清晰资源所有权和无 JVM
静态 PluginManager。

Java 插件开发者只实现一个 Plugin，并通过 PluginDefinition Builder 同时 `.provide(...)` Capability
（可选 `.exposure(...)`）和 `.contribute(...)` ConsolePluginContribution。YAML 插件开发者只维护一份
`plugin.yaml`，由宿主编译为同一 PluginDefinition。二者自动归属于同一 pluginId、version 和生命周期。

## 18. 兼容与迁移

实施必须分阶段完成，任何阶段都不得让两套 Runtime 同时管理同一插件实例。

### 18.1 契约增量

- Core 增加 PluginContribution 和 Handler 契约；
- Console 增加 ConsolePluginContribution；
- PluginDefinition 增加国际化元数据和 contributions；
- 暂时保留旧 ConsoleExtensionProvider，并建立 pluginId 与 extensionKey 显式映射。

### 18.2 统一发现与目录

- Console 改读 PluginCatalog，不再调用独立 ServiceLoader；
- 安装快照以 pluginId 为主身份，旧 extensionKey 仅作只读迁移别名；
- 资源记录补充 ownerPluginId；
- 同一 JAR 同时声明新旧 SPI 时只选择新 Plugin 路径并报告重复入口。

### 18.3 统一生命周期

- Console Handler 参加 PluginManager 启停事务；
- ExtensionRegistry 移除实际启停状态职责，收敛为 Console 资源目录；
- 管理端命令调用 PluginManager，并分开展示 desiredEnabled 与 runtimeState。

### 18.4 移除旧入口

- 删除 ConsoleExtensionProvider SPI、extensionKey 运行时身份、旧 ExtensionState 和重复 Discovery；
- 迁移安装实体字段；
- 兼容期后拒绝只有旧 SPI 的插件并提供升级诊断。

已发布身份不得机械改名。实施前必须生成确定映射：

```text
legacy pluginId / extensionKey -> canonical pluginId
```

迁移覆盖安装记录、配置命名空间、required/disabled 配置、ownerPluginId、管理端引用、日志监控、审计、
测试 fixture 和 SPI。moduleKey、pageKey、menuKey 和页面 URL 权限身份无冲突时保持不变。

## 19. 测试与验证要求

至少验证：

- 一个 ClassLoader 下所有 Plugin SPI 被发现，definition 只读取一次；
- 不再需要 ConsoleExtensionProvider SPI；
- 重复身份、CapabilityKey、ContributionType 在初始化前失败；
- Core 不依赖 Console Contribution 类型；
- Console Handler 可全局校验多个插件，prepare 失败不留资源；
- disabled 和 MISSING 插件不发布活动资源；
- ownerPluginId 阻止资源身份被其他插件接管；
- ACTIVE 前不能查询 staged Capability 和 Console 资源；
- 任一步失败逆序回滚，停止后全部活动资源撤出；
- 页面、菜单、UiSpec、Capability exposures 和权限规则保持；
- 停用和 MISSING 不删除权限，恢复相同身份后可复用关联；
- close 幂等并释放 Runtime 强引用。

实施 Java 变更时每批修改后必须执行 `mvn clean compile`；结构或完整实现完成后执行 `mvn validate`、
`mvn test`、`mvn -q help:effective-pom` 和 `git diff --check`。本次仅修改设计文档，不触发 Java 编译门禁，
也不生成或更新模块 SKILL.md。

## 20. 后续演进触发条件

| 能力 | 触发条件 |
|------|----------|
| 独立插件目录 | 生产环境要求不修改主应用 classpath 安装插件 |
| 独立 ClassLoader | 插件依赖冲突、类卸载或隔离成为真实需求 |
| 热重载 | 需要旧 ClassLoader 下线并加载新版本 |
| 请求排空 | 长调用无法接受维护窗口并发停止 |
| 动态配置 | Plugin 需要不停机应用新配置 |
| 健康检查 | 需要区分 ACTIVE 与外部依赖健康状态 |
| Contribution 依赖 | 多个 Handler 之间出现真实、有向且可验证的依赖 |
| YAML 编译器与远程 bind / exposures 适配 | 非 JVM 安装包和 http/command/mcp/process 适配成为真实需求 |
| 进程隔离 | 需要运行不完全可信第三方插件 |

这些能力必须分别补充设计，不能通过随意增加 PluginDefinition 字段或 Handler 回调解决。

## 21. 验收标准

- 只有一个 Plugin SPI、pluginId、version 和实际状态机；
- PluginManager 是真实运行状态唯一事实源；
- Console 只保存启停意图和安装历史；
- Capability 与 Contribution 语义清晰分离；
- Console Contribution 不反向污染 Core；
- Capability、exposures、页面、菜单共享同一可用性门控；
- 启动失败不留下部分活动资源，停止先拒绝新请求再释放资源；
- DISABLED、FAILED、STOPPED 和 MISSING 含义无歧义；
- 迁移覆盖身份、配置、持久化、资源所有权和 SPI；
- Kernel 与 Platform 保持平行；
- Contribution 类型目录与 Capability 清单不混用；
- 未引入当前需求不需要的插件目录、ClassLoader 隔离或热重载。

## 22. 相对归档的保留、变更与补回

对照 [`archive/plugin-extension-design.md`](archive/plugin-extension-design.md) 与
[`archive/extension-design.md`](archive/extension-design.md)。统一文档收成目标模型后，不得把归档里
已经冻结的运行规则当成“可省略细节”。

### 22.1 有意变更

| 归档 | 现行 |
|------|------|
| 两套 SPI：Plugin + ConsoleExtensionProvider | 一个 Plugin，Console 变为 `console@1` |
| `ConsoleExtensionProvider.endpointTypes` | **废除**；对外 HTTP/Command/MCP 归属 Capability `exposures` |
| plugin `id` kebab-case；console `extensionKey` 反向域名 | 统一 `pluginId` 反向域名，旧 id 按第 18 节映射 |
| 展示名为单语 `name` | `displayName` / `description` 使用 I18nObject |
| 禁止插件 YAML | Java 仍是 JVM 主表面；YAML 只服务跨语言，并编译为同一 PluginDefinition |
| YAML `capabilities[].binding` 兼当实现与对外入口 | 拆为 `bind`（实现适配）与 `exposures[]`（对外通道） |
| 配置前缀 `plugins.<kebab-id>` | 前缀仍为 `plugins.`，id 迁到反向域名后按点分段，不用 `nexus.plugins` |
| V1 PluginState 无 DISABLED | 增加 DISABLED；期望状态在 Console，真实状态在 PluginManager |
| 安装 Runtime 曾规划在 Kernel | 启停意图与安装快照归 Console，执行仍在 Core |
| Tags 至少一个 | 无路由需求的插件 Tags 可为空 |

### 22.2 归档已有、统一稿曾收缩、现已重新列为规范的内容

- 配置五级覆盖、环境变量映射、未知 key 拒绝、Secret 无默认值。
- Tags 子集匹配、默认路由、唯一 Provider、禁止同 Plugin 重复 CapabilityKey。
- 单插件停止的 required 依赖保护；整体关闭逆序停止。
- 页面树 / 菜单树字段组合、pagePath 冲突、UiSpec 定位、内部视图不产生 PAGE。
- 页面 URL 权限身份、`X-Nexus-Page-Key`、method 不参与权限、同 URL 跨页面分项。
- `PageRenderRequest`、页面 URL 权限、Capability exposures 热卸载“重启后生效”。
- 废除 `ConsoleExtensionProvider.endpointTypes`；对外 HTTP 迁到 Capability `exposures`。
- ResourceScope、PluginEventBus、Capability 路由的语义仍以归档插件设计为准；本文不重复实现细节，但实现不得回退那些规则。

### 22.3 仍须在实施时显式处理的差异

| 差异 | 说明 |
|------|------|
| 现行 Java `PluginDefinition` 仍是 `id` + `name` + 必填 Tags，无 `contributions` | 第 18 节契约增量尚未落地 |
| 现行 `PluginState` 无 DISABLED | 与本文状态机不一致，实施时补齐 |
| 现行配置前缀 `plugins.` + kebab `id` | 身份迁移时必须同时迁移配置 key、环境变量、测试 fixture |
| YAML 发现、编译器、`bind` / `exposures` 适配器 | 归档明确未做；跨语言表面未实现前，Classpath 仍只走 Java SPI |
| Kernel 文档仍描述独立 ExtensionRegistry | 迁移完成前不得两套 Runtime 管同一实例 |

归档中“取消 YAML”的代价（多源不一致、Class 反射、JAR 归属）仍然成立。YAML 路径必须先编译再进 Catalog，禁止运行时把 YAML 字符串 API 名反射成 Java 类型。`bind` 与 `exposures` 必须在 Catalog 预检完成语义校验，不得把对外入口误写回 Console Contribution。

## 23. 最终结论

Nexus 采用“一个 Plugin、多个扩展面、两种声明表面”的统一模型：

```text
Plugin（唯一身份与生命周期）
├── 声明表面：Java SPI / YAML DSL  →  同一份 PluginDefinition
├── Capability：可调用、可依赖、可路由
│     ├── bind：实现适配（Java Factory / YAML bind）
│     └── exposures[]：可选对外入口（http / command / mcp）
└── Contribution：现行仅 console@1（模块 / 页面树 / 菜单树）
        └── 页面如何布局（工作台、设置、对话、看板）→ 该页 UiSpec / 页面 DSL
```

Console 页面扩展不再是第二类插件，而是 Plugin 的 `console@1` Contribution。Core 统一负责发现、配置、
依赖、生命周期、可用性和诊断；Console 负责管理意图、Console 声明校验、资源装配和权限协作。

该模型消除双 SPI、双身份和双状态机，同时避免把页面、菜单和 REST 语义硬编码进通用插件内核，符合
Nexus 轻量、业务中立、依赖单向和可逐步迁移的工程约束。
