# Nexus Core 轻量级插件扩展体系设计方案

## 1. 文档定位

本文定义适配当前 Innospots Nexus 工程的通用 Java 插件扩展体系。设计参考《Java 轻量级无框架
插件扩展体系设计方案》的核心理念，但根据当前工程实际进一步简化为：

```text
Java PluginDefinition
        +
单一 Plugin SPI
        +
Classpath ServiceLoader 全量发现
        +
Capability + Tags 路由
```

插件不再使用 `nexus-plugin.yaml`，Capability Provider 也不再使用各自独立的 SPI 文件。插件的
身份、版本、Tags、Capability、依赖和配置定义全部由顶层 `Plugin` 实现类以不可变 Java 对象声明。

本方案的实现全部归属 `innospots-nexus-core`。`innospots-nexus-console` 本阶段不参与插件发现、
加载、配置、Capability 注册、路由或生命周期实现。Console 将来如需管理插件，只能调用 Core
提供的管理接口和只读诊断模型。

本文同时作为 `innospots-nexus-core` 插件运行时的实现依据。当前 V1 代码已覆盖本文定义的
Plugin SPI、Classpath 发现、静态目录快照、配置解析、Capability 注册/路由、事件总线、资源托管
和生命周期管理；后续扩展仍需遵守本文的模块边界与失败语义。

## 2. 当前工程约束

### 2.1 模块边界

当前工程依赖方向为：

```text
innospots-nexus-base
        ↓
innospots-nexus-core
        ↓
innospots-nexus-console
        ↓
kernel / platform
```

插件内核属于业务中立的平台运行能力，必须放在 Core。它可以复用 Base 的异常、状态码和线程基础，
但不能依赖 Console、Kernel、Platform、Spring Boot 自动配置或具体业务模块。

### 2.2 已有能力

当前工程已经具备：

- Base 中的 `NexusException` 和 `NexusStatusCode`；
- Base 中的配置值对象 `NexusConfig`；
- Base 中的静态进程级 `EventBus`；
- Core 中的线程、Watcher、服务生命周期和持久化基础；
- Core 中面向管理页面的 `core.extension` 声明；
- Console 中现存的 Console Extension 发现、安装记录和启停逻辑。

新插件内核使用独立的 `com.innospots.nexus.core.plugin` 包。它不继续扩建现有 Console
Extension Runtime，也不把管理页面、菜单、权限或 Endpoint 注册纳入通用插件内核。

### 2.3 核心设计决定

- 不新增 `plugin-api`、`plugin-kernel`、`plugin-testkit` Maven 模块；
- 不引入 PF4J、OSGi、Spring、反射扫描器或 Maven Resolver；
- 不使用 YAML、JSON、XML 或注解描述插件；
- Classpath 中的插件通过标准 Java `ServiceLoader<Plugin>` 全量发现；
- 每个 Plugin 实现通过 `PluginDefinition` 声明所有静态信息；
- Plugin 通过类型安全的 `CapabilityContribution` 直接声明 `CapabilityProviderFactory`；
- 不再为每个 Capability 创建 `META-INF/services/<Capability API>`；
- 业务代码只依赖 Capability API 和 `CapabilityManager`；
- V1 支持启动时发现、显式启动和安全停止，不支持运行时修改 classpath。

## 3. 为什么取消 YAML

YAML 方案需要同时维护：

```text
nexus-plugin.yaml
Plugin SPI
Capability Provider SPI
```

在 classpath 全量加载模式下，还必须根据 JAR URL 或 `CodeSource` 反推 YAML、Plugin 实现和
Capability Provider 的归属。这增加了以下复杂度：

- 三处信息可能不一致；
- Provider 归属依赖 JAR/目录 URL 规范化；
- exploded classes、测试目录和 shaded JAR 的归属容易产生歧义；
- YAML 中的字符串 API 名需要再次反射加载并校验；
- 修改任何声明都仍然需要重新构建插件 JAR，YAML 并未提供真正的运行时灵活性。

改为 Java `PluginDefinition` 后：

- 编译器校验 Capability API 和 CapabilityProvider 类型；
- CapabilityProviderFactory 天然归属于声明它的 Plugin；
- Classpath 只需要一个标准 SPI 入口；
- 不需要 YAML Parser、JAR 资源枚举或 CodeSource 绑定；
- 全部 PluginDefinition 可以先汇总和校验，再执行任何插件初始化行为。

需要接受的取舍：插件元数据必须加载 Plugin 类后才能读取；修改声明必须重新编译 JAR；非 Java 工具
不能直接读取 YAML 建立插件清单。对当前纯 Java Nexus 工程，这些代价小于多套声明的维护成本。

## 4. 目标与非目标

### 4.1 V1 目标

1. 在 Core 中提供不依赖业务框架的 Plugin Runtime。
2. 使用 `ServiceLoader<Plugin>` 发现配置 ClassLoader 可见的所有插件。
3. 通过不可变 `PluginDefinition` 声明身份、版本、Tags、Capability、依赖和配置。
4. 支持同一 Capability 的多个实现，并通过不可变 Tags 确定性路由。
5. 支持 Capability 名称和主版本依赖检查。
6. 支持插件默认值、宿主配置、环境变量、系统属性和运行时变量的配置覆盖。
7. 支持配置类型、必填项和 Secret 标记校验。
8. 支持 Plugin、CapabilityProvider 和托管资源的原子启动与失败回滚。
9. 支持并发读取 Capability Registry。
10. 提供不泄露运行时可变对象的诊断快照。
11. 单个普通插件失败不破坏其他插件；宿主指定的必需插件失败可阻断应用启动。
12. 具备可测试、可逐阶段提交的实现结构。

### 4.2 V1 非目标

V1 不实现：

- `nexus-plugin.yaml` 或其他插件描述文件；
- Classpath 注解扫描；
- 运行时向 classpath 安装新 JAR；
- 插件目录监听和自动安装；
- 自动热重载和字节码替换；
- 在线插件市场、下载和签名分发；
- Maven 依赖解析或插件 `lib/` 目录；
- 配置中心、配置动态刷新、KMS 或 Vault；
- Provider 权重、优先级、轮询或负载均衡；
- 调用引用计数和完整的请求排空；
- 插件状态数据库持久化；
- Console 管理 Endpoint、页面和菜单；
- REST Endpoint 自动注册；
- 分布式 EventBus；
- 不可信代码安全沙箱；
- OSGi 级包导入导出和模块隔离。

## 5. 总体架构

```text
Application Classpath
        │
        ├── plugin-a.jar
        │     └── META-INF/services/...Plugin
        ├── plugin-b.jar
        │     └── META-INF/services/...Plugin
        └── plugin-c.jar
              └── META-INF/services/...Plugin
                        │
                        ▼
               ServiceLoader<Plugin>
                        │
                        ▼
                 PluginDefinition
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
       Identity    Capability     Config/Tags
                        │
                        ▼
               DefaultPluginManager
          ├── ConfigurationManager
          ├── DependencyResolver
          ├── CapabilityRegistry
          ├── CapabilityRouter
          ├── PluginEventBus
          └── ResourceScope
                        │
                        ▼
                CapabilityManager
                        │
                        ▼
                Application Service
```

职责边界：

| 组件 | 回答的问题 | 不负责 |
|------|------------|--------|
| Plugin SPI | Classpath 中有哪些插件实现 | Capability 路由 |
| `PluginDefinition` | 插件是什么、提供和依赖什么 | 运行时状态和资源实例 |
| `Plugin` | 插件级定义和生命周期行为 | 具体业务 Capability |
| `CapabilityContribution` | Capability 由哪个工厂创建 CapabilityProvider | 业务选择哪个能力提供者 |
| `CapabilityManager` | 业务调用时选择哪个 ACTIVE Provider | Plugin 发现和生命周期 |
| `ConfigurationManager` | 插件最终获得什么配置 | 应用配置文件协议 |
| `PluginManager` | 插件何时启动、停止和回滚 | UI、持久化和业务管理规则 |
| `ResourceScope` | 插件副作用何时释放 | JVM 安全隔离 |

## 6. Core 包结构

```text
com.innospots.nexus.core.plugin
├── contract
│   ├── Plugin
│   ├── CapabilityProvider
│   ├── CapabilityProviderFactory
│   ├── PluginContext
│   └── CapabilityProviderContext
├── declaration
│   ├── PluginDefinition
│   ├── CapabilityContribution
│   └── CapabilityRequirement
├── capability
│   ├── CapabilityKey
│   ├── CapabilityType
│   ├── CapabilityRegistration
│   ├── CapabilityManager
│   ├── CapabilityRegistry
│   ├── CapabilityRouter
│   ├── Tag
│   └── Tags
├── config
│   ├── ConfigDefinition
│   ├── ConfigItemDefinition
│   ├── ConfigType
│   ├── PluginConfig
│   ├── ConfigSource
│   ├── ConfigurationManager
│   └── SecretValue
├── discovery
│   ├── ClasspathPluginDiscovery
│   ├── DiscoveredPlugin
│   └── PluginCatalog
├── dependency
│   ├── DependencyResolver
│   └── DependencyResolution
├── event
│   ├── PluginEvent
│   ├── PluginEventBus
│   ├── Subscription
│   ├── PluginStartedEvent
│   ├── PluginStoppedEvent
│   ├── PluginFailedEvent
│   └── DefaultPluginEventBus
├── lifecycle
│   ├── PluginState
│   ├── ManagedPlugin
│   └── PluginRuntimeInfo
├── resource
│   ├── ResourceRegistration
│   ├── ResourceScope
│   └── DefaultResourceScope
├── status
│   └── PluginStatusCode
└── runtime
    ├── PluginManager
    ├── DefaultPluginManager
    └── PluginRuntimeConfig
```

只在实际开发阶段创建已进入对应阶段的包和类型，不预先生成空包。默认实现使用清晰的
`DefaultXxx` 名称，不创建没有责任边界的 `impl` 子包。

## 7. 唯一的插件发现入口

### 7.1 SPI 文件

每个插件 JAR 至少注册一个 `Plugin` 实现：

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
```

示例内容：

```text
com.example.wecom.WeComPlugin
```

这是每个插件唯一必需的配置文件。不再需要：

```text
META-INF/nexus-plugin.yaml
META-INF/services/<Capability API>
```

推荐一个 JAR 只提供一个 Plugin 实现，便于版本和发布管理；Runtime 以 PluginDefinition 为运行
单元，不强制一个 JAR 只能包含一个实现。

### 7.2 Classpath 全量发现

```java
ServiceLoader<Plugin> loader = ServiceLoader.load(
        Plugin.class,
        runtimeConfig.pluginClassLoader());
```

Java `ServiceLoader` 会聚合指定 ClassLoader 可见范围内所有 JAR 和 classes 目录中的 Plugin SPI
文件，因此一次调用即可发现 classpath 上全部插件，不需要自行枚举 JAR 或资源 URL。

`ClasspathPluginDiscovery` 使用 `ServiceLoader.stream()` 分两步处理：

1. 枚举全部 `ServiceLoader.Provider<Plugin>`；
2. 记录实现类型和来源诊断；
3. 逐个实例化 Plugin；
4. 每个 Plugin 只调用一次无副作用的 `definition()` 并缓存结果；
5. 汇总全部 PluginDefinition；
6. 完成全局唯一性和兼容性校验；
7. 校验通过后才允许进入配置和启动阶段。

发现阶段可以加载 Plugin 类并执行其公共无参构造函数与 `definition()`，但不执行
`initialize/start`，也不调用任何 CapabilityProviderFactory。

### 7.3 Discovery 与全局管理的静态性边界

这里的“静态管理”需要区分三种含义：

| 层次 | 是否静态 | 说明 |
|------|----------|------|
| PluginDefinition 声明 | 是 | 由插件实现类通过 Java 代码固定声明，随插件 JAR 发布；修改后重新构建 JAR |
| 一次启动的发现结果 | 是 | `ClasspathPluginDiscovery` 在一个 Runtime 首次初始化时执行一次；通过全局校验后形成不可变的 `DiscoveredPlugin` 快照 |
| JVM 级 Discovery/PluginManager 单例 | 否 | 不使用静态字段保存 ClassLoader、Plugin、Provider、Context、Registry 或生命周期状态 |

因此，Discovery 是**无状态的实例操作**，由 `DefaultPluginManager` 在第一次 `start()`、`plugins()` 或
`plugin(...)` 调用时创建并执行。发现结果只在当前 `PluginManager` 实例内缓存；同一个 ClassLoader 创建
两个 Runtime 时，两个 Runtime 各自拥有独立的发现快照和生命周期，不共享状态。

本文所说的“全局校验”和“全局能力注册”中的“全局”，均指**单个 PluginManager Runtime 内全局**：

- 覆盖该 Runtime 发现的全部 PluginDefinition；
- 覆盖该 Runtime 内全部 CapabilityProvider；
- 用于保证 plugin id、API、默认路由和依赖关系的一致性；
- 不跨越不同 PluginManager、不同 ClassLoader 或不同应用进程。

Discovery 快照可以视为启动期间的静态目录，但 CapabilityRegistry、PluginEventBus、ResourceScope、
PluginState 和 Provider 实例仍然是运行时对象，必须由 `PluginManager` 实例管理。静态 JVM 单例会带来
ClassLoader 强引用泄漏、测试之间相互污染、无法同时运行隔离 Runtime 以及关闭后状态残留等问题，因此
V1 不提供 `Plugins.get(...)`、静态 Registry 或静态 Manager。

如果宿主希望整个应用只运行一套插件 Runtime，可以把一个 `PluginManager` 交给应用生命周期容器按
application scope 管理；这属于宿主的对象生命周期配置，不改变 Core 中“实例化、显式传递、可关闭”的
设计。V1 不支持运行中修改 Classpath；新增、删除或替换插件需要创建新的 Runtime（通常伴随进程重启）。

需要在宿主启动流程中显式完成发现时，可以使用静态风格的工厂方法创建目录快照。该快照会复用
Classpath Discovery 的 plugin id、API version、Capability API 和默认路由前置校验：

```java
PluginCatalog catalog = PluginCatalog.discover(Application.class.getClassLoader());

try (PluginManager plugins = DefaultPluginManager.create(runtimeConfig, catalog)) {
    plugins.start();
}
```

该方法只是一次性的便捷入口，不建立 JVM 全局缓存；`PluginCatalog` 由调用方持有，并且只能交给
一个 `PluginManager` Runtime 使用。需要测试或自定义发现结果时，可使用 `PluginCatalog.of(...)`，
该工厂同样执行 plugin id、API version 和 Capability API 的全局校验。

### 7.4 发现失败规则

以下情况在任何插件初始化前终止本次发现：

- Service Provider 配置非法；
- Plugin 无公共无参构造能力；
- Plugin 构造或 `definition()` 抛出异常；
- `definition()` 返回空；
- 重复 plugin id；
- 不支持的 Plugin API version；
- PluginDefinition 内部声明冲突；
- required plugin id 未被发现。

ServiceLoader 返回顺序不能作为启动优先级、默认 Provider 或冲突消解依据。Runtime 按 plugin id
排序形成稳定诊断顺序，实际启动顺序由 Capability 依赖决定。

## 8. Plugin 和 PluginDefinition

### 8.1 Plugin 接口

```java
public interface Plugin {

    PluginDefinition definition();

    default void initialize(PluginContext context) {
    }

    default void start() {
    }

    default void stop() {
    }
}
```

约束：

- Plugin 必须可由 ServiceLoader 实例化；
- 构造函数和 `definition()` 必须是无副作用、确定性的描述操作；
- Runtime 对每个 Plugin 只调用一次 `definition()`，后续生命周期始终使用该不可变快照；
- 构造和定义阶段不得读取插件配置、创建线程、建立连接、写文件或调用其他 Capability；
- 网络连接、后台线程和资源注册只能在 `initialize` 或 `start` 阶段发生；
- Plugin 不直接实现消息、存储、脚本等具体 Capability。

### 8.2 PluginDefinition

```java
public record PluginDefinition(
        String id,
        String name,
        String version,
        int apiVersion,
        Tags tags,
        List<CapabilityContribution<?>> capabilities,
        List<CapabilityRequirement> requirements,
        ConfigDefinition config
) {

    public static Builder builder(String id);
}
```

字段规则：

| 字段 | 规则 |
|------|------|
| `id` | 全局唯一；小写 kebab-case；发布后不可变 |
| `name` | 非空展示名，不参与运行时身份 |
| `version` | 插件发布版本；V1 只展示，不计算 SemVer 范围 |
| `apiVersion` | Plugin Core 协议主版本；Builder 缺省为 V1 当前版本 |
| `tags` | Plugin 实现身份；至少一个 Tag |
| `capabilities` | 本插件提供的类型安全 CapabilityProviderFactory，可为空 |
| `requirements` | required/optional Capability 依赖，可为空 |
| `config` | 配置结构和默认值，缺省为空定义 |

所有集合和 Map 在构造时防御性复制。Builder 只用于提高插件作者可读性，最终输出必须是不可变
PluginDefinition。

### 8.3 完整定义示例

一个 Plugin 可以通过多次调用 `provide` 声明多个 CapabilityProvider。它们应当属于相同的实现
身份，复用 Plugin 的 Tags、配置和生命周期，但分别实现不同的 Capability API。

```java
public final class WeComPlugin implements Plugin {

    private static final PluginDefinition DEFINITION =
            PluginDefinition.builder("message-wecom")
                    .name("WeCom Message Plugin")
                    .version("1.0.0")
                    .tags(Tags.of("channel", "wecom")
                            .and("provider", "tencent"))
                    .provide(
                            MessagePushProvider.CAPABILITY,
                            WeComMessagePushProvider::new)
                    .provide(
                            MessageTemplateProvider.CAPABILITY,
                            WeComMessageTemplateProvider::new)
                    .provide(
                            MessageCallbackVerifier.CAPABILITY,
                            WeComMessageCallbackVerifier::new)
                    .require(HttpClientProvider.CAPABILITY, true)
                    .config(ConfigDefinition.builder()
                            .string("corpId")
                                .required()
                                .end()
                            .secret("corpSecret")
                                .required()
                                .end()
                            .integer("timeout")
                                .defaultValue("5000")
                                .end()
                            .build())
                    .build();

    @Override
    public PluginDefinition definition() {
        return DEFINITION;
    }
}
```

该实现类同时确定：

- 插件身份和版本；
- 路由 Tags；
- 提供的 Capability；
- CapabilityProvider 创建方式和所属 Plugin；
- 依赖的 Capability；
- 配置结构和默认值。

三个 `provide` 分别产生三个 `CapabilityContribution`。这不是注册三个 Plugin，也不是声明三个
ServiceLoader SPI 实现；SPI 中仍然只有 `WeComPlugin`。Plugin Runtime 启动该 Plugin 时，才通过
三个 Factory 创建并初始化三个具体 CapabilityProvider。

## 9. CapabilityProvider 与创建工厂

### 9.1 CapabilityProvider 的明确含义

`CapabilityProvider` 是由一个 Plugin 创建和拥有、实现一个明确 Capability API、经过 Plugin
Runtime 初始化后注册到 CapabilityRegistry，并随所属 Plugin 停止而销毁的运行时能力提供者。

它不是泛化扩展点，也不表示任意 Hook、Listener、Endpoint、后台任务或插件内部组件。只有需要被
业务代码通过 `CapabilityManager` 查找和调用的能力实现，才属于 CapabilityProvider。

本文后续单独使用“Provider”时均指 CapabilityProvider；`ServiceLoader.Provider` 会始终使用完整
类型名，避免与能力提供者混淆。

约束：

- 必须实现一个由应用和插件共同可见的 Capability API；
- 一个实例只归属于一个 Plugin 和一个 CapabilityKey；
- 只能由所属 PluginDefinition 中的 CapabilityProviderFactory 创建；
- 创建后由 Runtime 注入 CapabilityProviderContext；
- 初始化成功且所属 Plugin 启动成功后才能进入 CapabilityRegistry；
- 通过 CapabilityType + Tags 被业务代码选择；
- Plugin 停止时先从 Registry 撤出，再调用 `destroy()`；
- Plugin 重新启动时创建新实例，不复用已销毁实例。

接口定义：

```java
public interface CapabilityProvider {

    default void initialize(CapabilityProviderContext context) {
    }

    default void destroy() {
    }
}
```

简单 CapabilityProvider 可以只实现 Capability 方法。需要配置、依赖能力或资源的 Provider 在
`initialize` 中取得上下文，并把副作用登记到 `ResourceScope`。

例如 `MessagePushProvider` 是 Capability API，`WeComMessagePushProvider` 是具体的
CapabilityProvider：

```java
public final class WeComMessagePushProvider implements MessagePushProvider {

    @Override
    public void initialize(CapabilityProviderContext context) {
        // 读取本 Plugin 配置、获取依赖能力并登记托管资源。
    }

    @Override
    public PushResult send(PushMessage message) {
        // 执行企业微信消息发送。
    }

    @Override
    public void destroy() {
        // 清理 CapabilityProvider 自身的非托管状态。
    }
}
```

完整类型关系如下：

```text
Capability API（业务可见的能力契约）
        ↑ implements
具体 CapabilityProvider（功能实现类）
        ↑ create
CapabilityProviderFactory（无副作用的延迟创建函数）
        ↓ 与 CapabilityType 组成
CapabilityContribution（一个能力声明）
        ↓ 收集到
PluginDefinition（一个插件的完整静态定义）
        ↓ Plugin Runtime 初始化并原子发布
CapabilityRegistry / CapabilityManager（业务查询和调用入口）
```

各类型的职责边界：

| 类型 | 回答的问题 | 示例 |
|------|------------|------|
| `MessagePushProvider` | 业务能够调用什么能力 | `send(PushMessage)` |
| `WeComMessagePushProvider` | 企业微信如何实现该能力 | 调用企业微信发送接口 |
| `CapabilityProviderFactory` | Runtime 如何创建一个新的实现实例 | `WeComMessagePushProvider::new` |
| `CapabilityContribution` | 哪个 CapabilityType 绑定哪个 Factory | `message.push@1 -> factory` |
| `WeComPlugin` | 哪些能力、配置和依赖组成一个可管理单元 | 消息发送、模板渲染、回调校验 |

Capability API 不包含插件身份、配置读取和路由逻辑；具体 Provider 不负责把自己注册到 Registry；
Plugin 也不实现具体业务能力。三者分开后，业务模块只依赖 Capability API，Runtime 统一负责实例
创建、上下文注入、发布和销毁。

### 9.2 CapabilityProviderFactory

```java
@FunctionalInterface
public interface CapabilityProviderFactory<T extends CapabilityProvider> {

    T create();
}
```

Factory 必须只创建尚未初始化的 CapabilityProvider 实例，不得建立网络连接、启动线程或向全局
状态注册对象。
所有需要失败回滚的行为放入 `CapabilityProvider.initialize()`。

使用 Factory 而不是在 `PluginDefinition` 中直接保存 Provider 实例，主要用于：

- 延迟创建：发现和校验全部 PluginDefinition 时不实例化具体功能实现；
- 生命周期隔离：每次从 STOPPED 重新启动都创建新的 Provider；
- 失败回滚：实例创建后仍由 Runtime 统一控制 initialize、destroy 和资源释放；
- 类型安全：Builder 的泛型保证 Factory 产物能够实现对应 Capability API。

Builder 的核心方法定义为：

```java
public <T extends CapabilityProvider> Builder provide(
        CapabilityType<T> type,
        CapabilityProviderFactory<? extends T> factory
);
```

因此下面的声明在编译期即可建立明确关系：

```java
.provide(
        MessagePushProvider.CAPABILITY,
        WeComMessagePushProvider::new)
```

其含义是：`MessagePushProvider.CAPABILITY` 要求一个能够创建 `MessagePushProvider` 的 Factory，
而 `WeComMessagePushProvider` 实现了 `MessagePushProvider`，所以构造方法引用满足该类型约束。
若误传一个只实现 `MessageTemplateProvider` 的构造方法引用，代码不能通过编译。

### 9.3 CapabilityContribution

```java
public record CapabilityContribution<T extends CapabilityProvider>(
        CapabilityType<T> type,
        CapabilityProviderFactory<? extends T> factory
) {
}
```

Runtime 调用 factory 后必须校验：

- 返回值非空；
- CapabilityProvider 是 `type.api()` 的实例；
- 同一个 PluginDefinition 中不存在重复 CapabilityKey；
- CapabilityProvider 实例只属于当前 ManagedPlugin；
- Plugin 停止后不复用旧 CapabilityProvider 实例。

CapabilityProviderFactory 直接由 PluginDefinition 持有，因此不需要 Capability SPI，也不需要根据 JAR
`CodeSource` 判断 Provider 属于哪个 Plugin。

### 9.4 一个 Plugin 提供多个 CapabilityProvider

下面示例展示两个 Capability API、两个具体功能实现类，以及它们如何加入同一个 Plugin。

```java
public interface MessagePushProvider extends CapabilityProvider {

    CapabilityType<MessagePushProvider> CAPABILITY = CapabilityType.of(
            "message.push",
            1,
            MessagePushProvider.class);

    PushResult send(PushMessage message);
}
```

```java
public interface MessageTemplateProvider extends CapabilityProvider {

    CapabilityType<MessageTemplateProvider> CAPABILITY = CapabilityType.of(
            "message.template",
            1,
            MessageTemplateProvider.class);

    RenderedMessage render(
            String templateCode,
            Map<String, Object> variables);
}
```

```java
public final class WeComMessagePushProvider implements MessagePushProvider {

    @Override
    public void initialize(CapabilityProviderContext context) {
        // 读取所属 Plugin 的配置，并获取共享 HTTP 能力。
    }

    @Override
    public PushResult send(PushMessage message) {
        // 调用企业微信消息发送接口。
    }
}
```

```java
public final class WeComMessageTemplateProvider implements MessageTemplateProvider {

    @Override
    public RenderedMessage render(
            String templateCode,
            Map<String, Object> variables) {
        // 执行企业微信模板渲染。
    }
}
```

在 Plugin 中连续声明即可加入多个实现类：

```java
PluginDefinition.builder("message-wecom")
        .name("WeCom Message Plugin")
        .version("1.0.0")
        .tags(Tags.of("channel", "wecom")
                .and("provider", "tencent"))
        .provide(
                MessagePushProvider.CAPABILITY,
                WeComMessagePushProvider::new)
        .provide(
                MessageTemplateProvider.CAPABILITY,
                WeComMessageTemplateProvider::new)
        .build();
```

`capabilities` 列表保留声明顺序。Runtime 按声明顺序创建和初始化 Provider，Plugin 成功启动后一次性
发布全部 Capability；停止时按相反顺序销毁。任一 Factory 创建失败或任一 Provider 初始化失败，
Runtime 都会逆序销毁此前已初始化的 Provider，且不会发布该 Plugin 的任何 Capability。

### 9.5 同一 Capability 的多个具体实现

V1 允许整个 Runtime 中存在同一 CapabilityKey 的多个实现，但同一个 PluginDefinition 内禁止重复
CapabilityKey。下面的定义不合法：

```java
PluginDefinition.builder("message-wecom")
        .provide(
                MessagePushProvider.CAPABILITY,
                WeComAppMessageProvider::new)
        .provide(
                MessagePushProvider.CAPABILITY,
                WeComRobotMessageProvider::new)
        .build();
```

原因是 V1 的 Provider 只继承 Plugin Tags，不支持 Provider 局部 Tags。上述两个 Provider 会拥有
完全相同的 `CapabilityKey + Tags`，业务路由无法确定应该选择哪一个。

根据实现是否需要被业务独立选择，采用两种处理方式：

1. **需要独立路由选择**：拆成两个 Plugin 实现，分别声明不同 Plugin Tags，例如
   `mode=app` 和 `mode=robot`。两个 Plugin 都提供 `MessagePushProvider.CAPABILITY`，调用方通过
   Tags 选择实现。
2. **只是插件内部实现策略**：只注册一个 `WeComMessagePushProvider`，由它在内部组合
   `WeComAppMessageSender` 和 `WeComRobotMessageSender`。内部策略不是 CapabilityProvider，
   不进入 CapabilityRegistry。

拆成两个可路由 Plugin 的示例：

```java
public final class WeComAppPlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("message-wecom-app")
                .name("WeCom App Message Plugin")
                .version("1.0.0")
                .tags(Tags.of("channel", "wecom")
                        .and("mode", "app"))
                .provide(
                        MessagePushProvider.CAPABILITY,
                        WeComAppMessageProvider::new)
                .build();
    }
}
```

```java
public final class WeComRobotPlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        return PluginDefinition.builder("message-wecom-robot")
                .name("WeCom Robot Message Plugin")
                .version("1.0.0")
                .tags(Tags.of("channel", "wecom")
                        .and("mode", "robot"))
                .provide(
                        MessagePushProvider.CAPABILITY,
                        WeComRobotMessageProvider::new)
                .build();
    }
}
```

对应 SPI 文件可以同时列出这两个 Plugin 实现类：

```text
com.example.wecom.WeComAppPlugin
com.example.wecom.WeComRobotPlugin
```

这样多个实现仍通过唯一的 Plugin SPI 被发现，CapabilityProvider 本身不需要新增 SPI 文件。

## 10. Tags 和 Capability 模型

### 10.1 Tags

```java
public record Tag(String name, String value) {
}

public final class Tags {

    public static Tags of(String name, String value);

    public static Tags from(Map<String, String> values);

    public Tags and(String name, String value);

    public Optional<String> get(String name);

    public boolean matches(Tags required);

    public Map<String, String> asMap();
}
```

Tag name 和 value 使用小写 kebab-case。Tags 内部按 key 排序并保持不可变；同名不同值不能覆盖，
构造时直接失败。

匹配规则固定为：请求 Tags 是 Provider Tags 的约束子集。例如 Provider Tags 为
`channel=wecom, provider=tencent` 时，请求 `channel=wecom` 可以匹配，请求
`provider=custom` 不能匹配。

V1 中 Provider 继承 Plugin 的全部 Tags，不增加 Provider 局部 Tags 和覆盖规则。一个 Plugin 应当
只聚合具有相同实现身份、配置和生命周期的 Capability。

### 10.2 Capability 身份

```java
public record CapabilityKey(String name, int majorVersion) {
}

public record CapabilityType<T extends CapabilityProvider>(
        CapabilityKey key,
        Class<T> api
) {

    public static <T extends CapabilityProvider> CapabilityType<T> of(
            String name,
            int majorVersion,
            Class<T> api
    );
}
```

Capability API 发布类型安全常量：

```java
public interface MessagePushProvider extends CapabilityProvider {

    CapabilityType<MessagePushProvider> CAPABILITY = CapabilityType.of(
            "message.push",
            1,
            MessagePushProvider.class);

    PushResult send(PushMessage message);
}
```

Capability API 及其请求、结果模型必须由应用 ClassLoader 共享。插件不得在自己的 JAR 中复制 Core
公共契约或定义一个只有插件自身可见、业务应用无法引用的 Capability API。

### 10.3 CapabilityRequirement

```java
public record CapabilityRequirement(
        CapabilityKey key,
        boolean required
) {
}
```

依赖只绑定逻辑 CapabilityKey，不绑定具体插件和 Tags。Tags 负责运行时实现选择，不承担插件启动
依赖解析。

### 10.4 注册模型

```java
public record CapabilityRegistration<T extends CapabilityProvider>(
        CapabilityType<T> type,
        T provider,
        String pluginId,
        Tags tags
) {
}
```

注册对象只在 Plugin 和全部 CapabilityProvider 启动成功后发布。Registry 内部至少建立：

- `CapabilityKey -> immutable registrations`；
- `pluginId -> registered CapabilityKey`。

### 10.5 CapabilityManager

```java
public interface CapabilityManager {

    <T extends CapabilityProvider> T require(
            CapabilityType<T> type,
            Tags requiredTags);

    <T extends CapabilityProvider> Optional<T> find(
            CapabilityType<T> type,
            Tags requiredTags);

    <T extends CapabilityProvider> List<T> findAll(
            CapabilityType<T> type);
}
```

业务代码不依赖 plugin id 和实现类：

```java
MessagePushProvider provider = capabilities.require(
        MessagePushProvider.CAPABILITY,
        Tags.of("channel", "wecom"));
```

### 10.6 路由规则

Provider 选择顺序：

1. 调用方提供非空 Tags：按调用方 Tags 匹配；
2. 调用方未提供 Tags：使用宿主为该 CapabilityKey 配置的默认 Tags；
3. 仍未指定 Tags：只有一个 ACTIVE Provider 时返回该 Provider；
4. 没有匹配项：抛出 Capability Not Found；
5. 多个匹配项：抛出 Capability Ambiguous。

Plugin 发现顺序、SPI 文件顺序和注册顺序均不得用于打破歧义。V1 不支持 Provider 自报
`default=true`、`priority` 或权重。

## 11. ClassLoader 边界

### 11.1 V1 模型

V1 只发现 `PluginRuntimeConfig.pluginClassLoader()` 可见的 Plugin：

```text
Application ClassLoader
├── innospots-nexus-base
├── innospots-nexus-core
├── Capability API
├── plugin-a.jar
├── plugin-b.jar
└── plugin-c.jar
```

PluginManager 不创建、不修改也不关闭该 ClassLoader。插件停止只撤出 Capability、销毁
CapabilityProvider、停止 Plugin 和释放 ResourceScope，不能从 JVM 卸载 classpath 中的类。

### 11.2 类型一致性

Runtime 校验每个 CapabilityContribution：

1. Capability API 是接口；
2. Capability API 继承 `CapabilityProvider`；
3. CapabilityProvider 是该 API 的实例；
4. 相同 CapabilityKey 在全局使用相同 API `Class`；
5. 同名、同主版本但 API Class 不同属于全局冲突。

这些规则避免多个插件用相同逻辑能力名声明不兼容的 Java 契约。

### 11.3 外部 JAR 的接入方式

V1 外部插件必须在应用启动前加入运行 Classpath。应用可以通过构建依赖、启动参数或自己创建的
`URLClassLoader` 组装 Classpath，再把最终 ClassLoader 传给 PluginRuntimeConfig。

如果 ClassLoader 由应用创建，它的关闭仍由应用负责。PluginManager 不推断所有权。运行时新增
JAR、独立 Plugin ClassLoader 和热卸载属于 V2，必须另行设计。

## 12. Capability 依赖解析

### 12.1 声明级与运行时级

依赖有两个层次：

```text
DECLARED：至少一个 PluginDefinition 声明对应 CapabilityContribution
AVAILABLE：CapabilityRegistry 中至少一个对应 ACTIVE Provider
```

插件只有在全部 required 依赖达到 AVAILABLE 后才能启动。optional 依赖缺失不阻止启动，插件
通过 `CapabilityManager.find` 自行降级。

### 12.2 启动算法

`PluginManager.start()`：

1. ServiceLoader 发现并实例化全部 Plugin；
2. 读取并校验全部 PluginDefinition；
3. 建立 `CapabilityKey -> provider plugin ids` 声明索引；
4. 标记声明级完全缺失的 required 依赖；
5. 反复选择 required 依赖已经 AVAILABLE 的插件启动；
6. 每个插件成功后重新评估 WAITING 插件；
7. 一轮没有任何插件取得进展时停止迭代；
8. 剩余插件保持 WAITING，记录缺失能力或循环等待；
9. 宿主 required plugin 未 ACTIVE 时，关闭本轮已启动插件并使整体启动失败。

同一 Capability 有多个 Provider 时，依赖满足只要求至少一个 ACTIVE Provider。

### 12.3 停止约束

显式停止单个插件前，Manager 计算停止后的 Capability 可用性。如果某个 ACTIVE 插件的 required
依赖将失去最后一个 Provider，则拒绝停止，并返回依赖插件和 CapabilityKey。

V1 不自动级联停止，也不把 ACTIVE 插件自动切换为 WAITING。应用整体关闭时，Manager 按实际启动
顺序逆序停止全部插件，不执行单插件依赖保护。

## 13. 配置体系

### 13.1 配置定义

插件配置结构属于 PluginDefinition：

```java
public interface ConfigDefinition {

    Collection<ConfigItemDefinition> items();

    static ConfigDefinition empty();

    static Builder builder();
}

public record ConfigItemDefinition(
        String key,
        ConfigType type,
        boolean required,
        String defaultValue,
        boolean secret,
        String description
) {
}

public enum ConfigType {
    STRING,
    INTEGER,
    LONG,
    BOOLEAN,
    DURATION,
    SECRET
}
```

`defaultValue` 统一使用字符串保存，在配置阶段按 ConfigType 转换。Secret 不允许定义默认值。配置
key 使用点分小驼峰路径，例如 `http.connectTimeout`，不得携带 `plugins.<id>` 前缀。

### 13.2 配置命名空间

完整配置 key 固定为：

```text
plugins.<plugin-id>.<item-key>
```

例如：

```text
plugins.message-wecom.corpId
plugins.message-wecom.http.connectTimeout
```

插件内部的 PluginConfig 只使用局部 key，防止插件读取其他插件配置。

### 13.3 V1 配置来源和优先级

从低到高：

```text
Plugin Default
    < Host Map
    < Environment
    < System Property
    < Runtime Variables
```

| 来源 | 说明 |
|------|------|
| Plugin Default | ConfigDefinition 的非 Secret 默认值 |
| Host Map | 宿主从自身配置文件或配置系统解析后传入的扁平 Map |
| Environment | 根据已知 PluginDefinition 计算环境变量名 |
| System Property | 直接使用完整配置 key |
| Runtime Variables | Builder 显式传入，主要用于启动参数和测试 |

Core V1 不实现宿主配置文件格式。宿主可使用 YAML、JSON、数据库或其他方式，统一转换为 Map 后
交给 Runtime；这里的 YAML 是宿主配置选择，与插件描述文件无关。

环境变量映射示例：

```text
plugins.message-wecom.http.connectTimeout
        ↓
NEXUS_PLUGIN_MESSAGE_WECOM_HTTP_CONNECT_TIMEOUT
```

环境变量名只对已声明配置 key 生成。若两个不同 key 生成同一环境变量名，配置阶段直接失败。

### 13.4 PluginConfig

```java
public interface PluginConfig {

    Optional<String> get(String key);

    String require(String key);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    boolean getBoolean(String key, boolean defaultValue);

    Duration getDuration(String key, Duration defaultValue);

    SecretValue requireSecret(String key);
}
```

配置合并后：

1. 拒绝当前插件命名空间中的未知 key；
2. 校验 required；
3. 按声明类型转换；
4. 校验 Secret 不为空；
5. 生成不可变 PluginConfig；
6. 诊断只显示 key、来源和是否存在，不显示 Secret 值。

`SecretValue.toString()`、日志和运行时快照固定输出掩码。V1 只解决 Secret 标记、读取和防误输出，
不承诺堆内加密，也不解析 `secret:` 引用。

### 13.5 ConfigSource 扩展边界

V1 提供内部 ConfigSource 抽象，但不通过普通 Plugin 加载 ConfigSource，避免
ConfigurationManager 与 PluginManager 形成循环启动依赖。未来需要 Nacos、Apollo 或 Vault 时，
应设计独立 bootstrap SPI。

## 14. PluginContext 和 CapabilityProviderContext

```java
public interface PluginContext {

    PluginDefinition definition();

    PluginConfig config();

    CapabilityManager capabilities();

    PluginEventBus events();

    ResourceScope resources();

    System.Logger logger();
}

public interface CapabilityProviderContext extends PluginContext {

    CapabilityKey capability();
}
```

Tags 通过 `definition().tags()` 获取，不在 Context 中重复保存。Context 不暴露 ManagedPlugin、
ClassLoader、Registry 可写接口或其他插件的运行时对象。插件不能自行注册 Capability 或修改状态。

## 15. PluginEventBus 和 ResourceScope

这两个类型解决不同问题：

```text
PluginEventBus
    → 已经发生的状态如何被其他 Plugin 观察

ResourceScope
    → Plugin 启动周期内创建的外部资源如何保证被释放
```

它们都由 Plugin Runtime 创建和管理，不是业务 Capability，也不能通过 CapabilityManager 路由。

### 15.1 ResourceScope 的用途

`ResourceScope` 是 Plugin Runtime 为每个 Plugin 启动周期创建的资源释放栈。它托管 Plugin 和其
全部 CapabilityProvider 在初始化和运行期间产生的外部资源，确保正常停止与启动失败回滚都能按
逆序、幂等地释放资源。

它解决的核心问题是“部分初始化资源泄漏”。例如一个 Provider 已经创建线程池和 HTTP Client，
随后另一个 Provider 初始化失败。此时 Plugin 尚未 ACTIVE，但前面创建的资源仍必须被关闭。

典型托管对象：

- ExecutorService、Scheduler 和后台 Worker；
- HTTP Client、连接池和可关闭 Session；
- 文件或配置监听器；
- Event Subscription；
- 定时任务、回调或路由 Registration；
- 临时目录、临时文件清理函数；
- 其他实现 `AutoCloseable` 的外部资源。

接口：

```java
public interface ResourceScope extends AutoCloseable {

    <T extends AutoCloseable> T manage(T resource);

    ResourceRegistration add(Runnable disposer);

    @Override
    void close();
}

public interface ResourceRegistration extends AutoCloseable {

    @Override
    void close();
}
```

`manage` 用于直接托管 AutoCloseable；`add` 用于登记没有 AutoCloseable 对象的注销行为。返回的
ResourceRegistration 可以提前执行一次注销，Scope 关闭时不会重复执行同一 disposer。

示例：

```java
public final class WeComMessagePushProvider implements MessagePushProvider {

    @Override
    public void initialize(CapabilityProviderContext context) {
        ExecutorService executor = context.resources().manage(
                Executors.newVirtualThreadPerTaskExecutor());

        Callback callback = this::receiveCallback;
        callbackRegistry.register(callback);
        context.resources().add(
                () -> callbackRegistry.unregister(callback));
    }
}
```

所有权和生命周期：

```text
Plugin 每次进入 STARTING
        ↓
Runtime 创建新的 ResourceScope
        ↓
Plugin / CapabilityProvider initialize
        ↓
ACTIVE
        ↓
CapabilityProvider.destroy()
        ↓
Plugin.stop()
        ↓
ResourceScope.close()
```

启动失败时同样关闭：

```text
初始化异常
    ↓
已初始化 CapabilityProvider 逆序回滚
    ↓
Plugin 回滚
    ↓
ResourceScope.close()
```

`destroy()/stop()` 与 ResourceScope 的职责不同：

| 机制 | 负责 | 示例 |
|------|------|------|
| `CapabilityProvider.destroy()` | Provider 自身逻辑收尾 | 停止接收任务、刷新本地缓冲 |
| `Plugin.stop()` | Plugin 级逻辑收尾 | 停止插件协调器、撤销插件状态 |
| `ResourceScope.close()` | 兜底释放已登记外部资源 | 关闭线程池、连接、监听和订阅 |

先执行 `destroy()/stop()`，再关闭 Scope，是为了让逻辑收尾仍可使用已登记资源。即使
`destroy()/stop()` 失败，Scope 仍必须继续关闭。

ResourceScope 不负责：

- 创建资源或进行依赖注入；
- 保存业务数据或持久化状态；
- 创建、注册或销毁 CapabilityProvider 对象；
- 自动发现未登记的线程、连接和静态资源；
- 代替 CapabilityProvider.destroy() 和 Plugin.stop()；
- 管理其他 Plugin 的资源；
- 跨进程资源协调。

实现规则：

- 每个 Plugin 的每次启动周期独占一个 Scope；
- Plugin 和其全部 CapabilityProvider 共享该 Scope；
- Scope 按登记逆序释放；
- 单个 disposer 失败不阻止其余资源释放；
- 首个异常作为 NexusException cause，其余异常作为 suppressed；
- Scope、ResourceRegistration 和 disposer 执行都必须幂等；
- Scope 关闭后继续登记资源立即失败；
- Runtime 不允许一个 Plugin 获取另一个 Plugin 的 Scope。

### 15.2 PluginEventBus 的用途

`PluginEventBus` 是一个 PluginManager 实例内部共享的、进程内、同步、尽力而为的状态通知通道。
它让 Plugin 观察“已经发生的事情”，而不直接依赖事件发布方的实现类。

适用场景：

- Plugin 启动、停止或失败后的观察通知；
- CapabilityProvider 上线或下线后的观察通知；
- 消息发送完成后的审计和指标统计；
- 配置应用完成通知；
- 缓存失效通知；
- 非关键监控、日志和诊断扩展。

V1 由 Runtime 发布三个内置事件：

| 事件 | 发布时机 | 最小内容 |
|------|----------|----------|
| `PluginStartedEvent` | 状态成功切换为 ACTIVE 后 | plugin id、version、CapabilityKey、时间 |
| `PluginStoppedEvent` | 资源释放完成并切换为 STOPPED 后 | plugin id、时间 |
| `PluginFailedEvent` | 启动或停止失败并记录 FAILED 后 | plugin id、phase、error code、时间 |

内置失败事件不携带 Throwable、配置值或运行时对象。完整异常只进入 Core 日志和内部诊断。

事件必须是不可变值对象，并实现标记接口：

```java
public interface PluginEvent {
}

public record MessageSentEvent(
        String channel,
        String messageId
) implements PluginEvent {
}
```

接口：

```java
public interface PluginEventBus {

    <E extends PluginEvent> Subscription subscribe(
            Class<E> eventType,
            Consumer<E> handler);

    void publish(PluginEvent event);
}

public interface Subscription extends AutoCloseable {

    @Override
    void close();
}
```

一个 PluginManager 拥有一个 DefaultPluginEventBus。PluginContext 和 CapabilityProviderContext
暴露的是绑定当前 Plugin 的事件总线视图，而不是底层可写订阅表。

发布示例：

```java
context.events().publish(
        new MessageSentEvent("wecom", messageId));
```

订阅示例：

```java
public final class MetricsPlugin implements Plugin {

    @Override
    public void initialize(PluginContext context) {
        context.events().subscribe(
                MessageSentEvent.class,
                this::recordMetric);
    }
}
```

PluginContext 的订阅操作自动把 Subscription 登记到当前 ResourceScope：

```text
context.events().subscribe(...)
        ↓
创建 Subscription
        ↓
自动登记到当前 Plugin ResourceScope
        ↓
Plugin 停止或启动回滚
        ↓
Subscription 自动关闭
```

返回 Subscription 只用于 Plugin 主动提前退订。即使 Plugin 忘记保存返回值，Scope 仍能保证清理。

### 15.3 Event 与 Capability 的选择规则

```text
需要返回值，失败必须反馈调用方
        → CapabilityManager

事情已经发生，希望其他 Plugin 观察
        → PluginEventBus

需要可靠、持久、跨进程传递
        → 消息中间件或 Outbox
```

PluginEventBus 不用于：

- 请求—响应、查询或命令执行；
- 消息发送、文件存储、脚本执行等同步业务能力；
- 替代 CapabilityManager；
- 表达 required Capability 依赖；
- 可靠消息、事务事件或失败重试；
- 跨 JVM、跨进程或跨服务通信；
- 工作流编排；
- 在订阅者之间传递可变共享状态。

### 15.4 V1 事件语义

- 事件在发布线程同步分发，不创建内部异步线程池；
- 使用订阅快照分发，发布过程不持有 Plugin 生命周期锁；
- 一个处理器异常被隔离并记录，不能阻止其他订阅者；
- `publish` 不因观察者失败而使发布方失败；
- 订阅者执行顺序不属于契约；
- 不保证事务一致性、持久化、重试或至少一次投递；
- 一个 PluginManager 的事件不会传播到另一个 PluginManager；
- PluginEventBus 不与 Base 静态 EventBus 自动桥接；
- Runtime 生命周期事件只在对应状态转换成功后发布；
- Event 对象不得包含 Plugin、Context、Registry、连接或其他可变运行时对象。

## 16. 生命周期和状态机

### 16.1 精简状态

```text
DISCOVERED
DESCRIBED
WAITING
STARTING
ACTIVE
STOPPING
STOPPED
FAILED
```

具体步骤通过 `PluginRuntimeInfo.phase` 和 `lastError` 诊断，不把每个内部步骤都建模为公开状态。

有效转换：

```text
DISCOVERED -> DESCRIBED
DESCRIBED -> WAITING | STARTING | FAILED
WAITING    -> STARTING | FAILED
STARTING   -> ACTIVE | FAILED
ACTIVE     -> STOPPING
STOPPING   -> STOPPED | FAILED
STOPPED    -> STARTING
```

V1 没有持久化 DISABLED。宿主要禁用插件，在 `PluginRuntimeConfig.disabledPluginIds` 中配置。

### 16.2 单插件启动顺序

```text
1. 读取已经校验的 PluginDefinition
2. 获取并校验 PluginConfig
3. 校验 required Capability 已 AVAILABLE
4. 创建 ResourceScope、PluginEventBus 视图和 PluginContext
5. 按 definition.capabilities 顺序调用全部 CapabilityProviderFactory
6. 校验 CapabilityProvider 类型
7. plugin.initialize(context)
8. capabilityProvider.initialize(context)，按声明顺序执行
9. plugin.start()
10. 一次性向 CapabilityRegistry 发布全部 registrations
11. 状态切换为 ACTIVE
12. 发布 PluginStartedEvent
```

Capability 只有在 Plugin 和全部 CapabilityProvider 初始化、启动成功后才原子发布，其他线程不会
看到半激活插件。

### 16.3 启动失败回滚

任一步骤失败时：

```text
从 Registry 移除当前插件全部 Capability（如果已经发布）
        ↓
逆序调用已初始化 CapabilityProvider.destroy()
        ↓
调用 Plugin.stop()（仅当 initialize 已开始）
        ↓
关闭 ResourceScope
        ↓
清除 CapabilityProvider、Context 和配置强引用
        ↓
记录 FAILED、失败阶段和根因
```

回滚尽最大努力执行全部步骤。清理错误作为 suppressed 异常保留，不能覆盖原始启动异常。

### 16.4 停止顺序

```text
1. 状态 ACTIVE -> STOPPING
2. 原子撤出该插件全部 Capability，拒绝新查找
3. 逆序调用 CapabilityProvider.destroy()
4. 调用 Plugin.stop()
5. 关闭 ResourceScope
6. 清除 CapabilityProvider、Context 和配置强引用
7. 状态 -> STOPPED
8. 发布 PluginStoppedEvent
```

V1 没有调用引用计数。撤出 Registry 前已经取得 Provider 引用的调用可能与停止并发，因此单插件
停止属于维护操作，调用方应先停止接收相关业务请求。

从 STOPPED 重新启动时，Runtime 复用由 ServiceLoader 创建的 Plugin 实例，但重新创建
PluginConfig、Context、ResourceScope 和全部 CapabilityProvider。Plugin 实现必须支持
initialize/start/stop 生命周期重复执行；若未来发现这一约束不合理，再引入 PluginFactory SPI，
不在 V1 提前设计。

### 16.5 PluginManager

```java
public interface PluginManager extends AutoCloseable {

    void start();

    void start(String pluginId);

    void stop(String pluginId);

    List<PluginRuntimeInfo> plugins();

    Optional<PluginRuntimeInfo> plugin(String pluginId);

    CapabilityManager capabilities();

    @Override
    void close();
}
```

`start()` 完成全量 SPI 发现和批量启动；重复调用幂等。`close()` 按逆启动顺序关闭全部 ACTIVE
插件并保持幂等。`close()` 完成后 Manager 进入终态，释放发现、生命周期和事件订阅引用；后续
`start`、查询和 Capability 访问都会被拒绝，重复 `close()` 仍然安全无副作用。

## 17. 宿主接入

### 17.1 PluginRuntimeConfig

```java
public record PluginRuntimeConfig(
        Set<String> requiredPluginIds,
        Set<String> disabledPluginIds,
        Map<String, String> hostConfig,
        Map<String, String> runtimeVariables,
        Map<CapabilityKey, Tags> defaultRoutes,
        ClassLoader pluginClassLoader
) {
}
```

集合和 Map 防御性复制。required 与 disabled 不能相交。`pluginClassLoader` 缺省为创建
DefaultPluginManager 的 ClassLoader。

### 17.2 应用启动示例

```java
PluginRuntimeConfig config = new PluginRuntimeConfig(
        Set.of("storage-local"),
        Set.of(),
        applicationConfig,
        Map.of(),
        Map.of(
                MessagePushProvider.CAPABILITY.key(),
                Tags.of("channel", "wecom")),
        Application.class.getClassLoader());

try (PluginManager plugins = DefaultPluginManager.create(config)) {
    plugins.start();

    NotificationService service = new NotificationService(
            plugins.capabilities());
    service.run();
}
```

Core 不提供全局静态 `Plugins.get(...)`。应用通过构造参数显式传递 CapabilityManager，保证测试
可替换、生命周期清楚，并允许同一 JVM 创建多个 Runtime。

`DefaultPluginManager` 是一个 Runtime 内的应用级协调器：它只管理自己发现的插件、自己的
`CapabilityRegistry`、`PluginEventBus`、依赖解析器和生命周期状态。这里的“全局管理”是 Runtime
范围的全局管理，不是 JVM 范围的静态管理。宿主可以在应用容器中将它配置为单例 Bean，但 Core
本身不持有静态引用，也不自动把多个 Manager 合并成一个全局注册表。

## 18. 并发、一致性和失败隔离

### 18.1 并发模型

- PluginManager 使用单一生命周期锁串行执行 discover/start/stop/close；
- 不在持有生命周期锁时调用业务 Capability；
- CapabilityRegistry 使用 `AtomicReference` 保存不可变索引快照；
- 注册和撤出通过 copy-on-write 原子替换完整快照；
- CapabilityRouter 只读取一次快照并在该快照内匹配；
- 返回的集合和诊断模型均为不可变副本；
- Plugin、CapabilityProvider 自身线程安全由各 Capability 契约声明。

### 18.2 原子边界

- 全部 PluginDefinition 在任何 initialize 前完成全局校验；
- 单个插件的全部 Capability 一次性注册或撤出；
- 一个插件失败不能留下部分注册；
- 批量启动不是全局事务，普通插件失败不回滚其他普通插件；
- required plugin 失败时关闭本轮已启动插件并使整体启动失败；
- 全局定义冲突在执行任何 Plugin 生命周期行为前终止启动。

### 18.3 冲突规则

以下情况不允许启动：

- 重复 plugin id；
- 同一 PluginDefinition 重复 CapabilityKey；
- 同一 PluginDefinition 重复 Requirement；
- 相同 CapabilityKey 对应不同 API Class；
- CapabilityProviderFactory 返回 null 或错误类型；
- Plugin API version 不兼容；
- 默认路由命中多个 Provider；
- required 依赖缺失或循环等待；
- 配置 key、环境变量映射或类型转换冲突。

## 19. 错误模型和诊断

不创建一类错误一个异常子类。所有插件错误使用 NexusException，通过实现 StatusCode 的
`PluginStatusCode` 区分：

```text
PLUGIN_DISCOVERY_FAILED
PLUGIN_DEFINITION_INVALID
PLUGIN_DUPLICATE
PLUGIN_API_INCOMPATIBLE
PLUGIN_CONFIG_INVALID
PLUGIN_DEPENDENCY_MISSING
PLUGIN_DEPENDENCY_CYCLE
PLUGIN_START_FAILED
PLUGIN_STOP_FAILED
PLUGIN_IN_USE
CAPABILITY_NOT_FOUND
CAPABILITY_AMBIGUOUS
CAPABILITY_TYPE_MISMATCH
```

PluginStatusCode 放在 `com.innospots.nexus.core.plugin.status`，不把插件专用错误全部加入 Base 的
NexusStatusCode。

错误消息至少包含：

- plugin id 和 Plugin 实现类型；
- 当前生命周期阶段；
- CapabilityKey 或配置 key；
- 冲突候选 plugin ids；
- 原始 cause。

任何诊断不得包含 Secret 值。

### 19.1 PluginRuntimeInfo

```java
public record PluginRuntimeInfo(
        String id,
        String name,
        String version,
        String implementationClass,
        PluginState state,
        String phase,
        Tags tags,
        List<CapabilityKey> providedCapabilities,
        List<CapabilityRequirement> requirements,
        Map<CapabilityKey, DependencyResolution> dependencies,
        Instant discoveredAt,
        Instant startedAt,
        String lastError
) {
}
```

诊断只保留可展示的错误摘要；完整异常由 Core 日志记录。PluginRuntimeInfo 不持有 Plugin、
CapabilityProvider、Context、ClassLoader、配置值、Factory 或资源实例。

## 20. 日志和安全边界

### 20.1 日志

Core 默认实现使用项目现有 SLF4J。对插件只通过 PluginContext 暴露 JDK System.Logger，避免具体
日志实现成为插件公共契约。

至少记录：

- 发现的 Plugin 实现类型和 Definition 摘要；
- 状态转换；
- Capability 注册和撤出；
- 依赖等待原因；
- 配置来源命中，但不记录值；
- 启动、停止和资源释放失败。

### 20.2 安全

插件是宿主进程内可信代码，ClassLoader 不能限制其文件、网络、反射或进程权限。V1 的安全措施：

- 只发现宿主显式 ClassLoader 可见的 SPI；
- 不扫描任意目录和包；
- 不解析远程 URI；
- 不输出 Secret；
- 不从插件下载依赖；
- 不把可写 Registry 或 Runtime 内部对象暴露给插件；
- 在任何插件 initialize 前完成全部 Definition 校验。

不可信插件必须运行在独立进程或容器中。

## 21. 与现有 extension 代码的关系

当前仓库存在：

```text
core.extension
    当前 Console 扩展声明模型

console.extension
    当前 Console 扩展发现、安装记录和运行时逻辑

core.plugin
    本文设计的通用插件内核
```

V1 实现期间：

1. `core.plugin` 不依赖现有 `core.extension`；
2. `core.plugin` 不依赖 `console.extension`；
3. 本阶段不改造 Console 页面、菜单、Endpoint 和权限；
4. 不把 Console 安装数据库实体移动到 Core；
5. 不让两套 Runtime 同时管理同一个 Plugin 实现；
6. 新增底层能力插件只接入 `core.plugin`；
7. 现有 Console Extension 停止新增通用插件能力。

后续 Console 管理支持另行设计：Console 保存管理员启停意图，通过 Core PluginManager 执行操作，
读取 PluginRuntimeInfo。运行状态事实仍由 Core 持有，Console 不实现第二套发现、依赖解析或状态机。

现有 `extension-design.md` 描述 Console 页面扩展，不作为本通用插件内核实现规范。本方案不提前删除
该文档或代码。

## 22. V1 测试策略

### 22.1 SPI 和定义测试

- 一个 ClassLoader 下多个 JAR 的 Plugin SPI 均被发现；
- classes 目录中的 SPI 也能被发现；
- 非 Plugin JAR 不产生任何插件；
- 非法 SPI、构造失败和 definition 失败被转换为 NexusException；
- 每个 Plugin 的 definition 只调用一次并被 Runtime 缓存；
- 重复 plugin id 在任何 initialize 前失败；
- 不兼容 apiVersion 失败；
- ServiceLoader 顺序不影响诊断和路由；
- Definition 集合不可变；
- CapabilityProviderFactory 在发现和定义校验阶段不会执行。

### 22.2 Capability 测试

- CapabilityType 正确校验 name/version/api；
- 同一 PluginDefinition 可以声明多个不同 CapabilityKey，并按声明顺序创建 Provider；
- 同一 PluginDefinition 声明重复 CapabilityKey 时在任何 Factory 执行前失败；
- `provide` 的 CapabilityType 与 Factory 产物保持泛型类型安全；
- CapabilityProvider 自动继承 Plugin Tags；
- Tags 子集匹配正确；
- 显式 Tags、默认 Tags、唯一 Provider 三层路由顺序正确；
- 无匹配和多匹配使用不同状态码；
- factory 返回 null 和错误类型失败；
- 相同 CapabilityKey 对应不同 API Class 失败；
- Registry 批量注册和撤出具有原子性；
- 并发查询只能看到注册前或注册后的完整快照。

### 22.3 配置测试

- 五级来源覆盖顺序正确；
- 未知 key、缺少 required、类型转换和环境变量冲突失败；
- Plugin 只能读取自己的局部 key；
- Secret 不允许默认值；
- Secret 的 toString、诊断和异常不泄露值；
- ConfigDefinition 和 PluginConfig 不可变。

### 22.4 生命周期测试

- required 依赖就绪后才启动；
- optional 依赖缺失仍可启动；
- 多 Provider 中一个 ACTIVE 即满足依赖；
- 缺失和循环等待诊断可区分；
- Plugin/CapabilityProvider 初始化顺序和销毁逆序正确；
- 任一 Provider 初始化失败不发布部分 Capability；
- 回滚继续释放全部资源并保留 suppressed 异常；
- 停止先撤出 Capability 再销毁资源；
- 单插件停止不会破坏依赖它的 ACTIVE 插件；
- start、stop 和 close 幂等；
- STOPPED 后重新创建 CapabilityProvider；
- 普通插件失败隔离，required plugin 失败回滚整体启动。

### 22.5 ClassLoader 和资源测试

- 自定义 URLClassLoader 中多个 fixture JAR 的 Plugin 均被发现；
- PluginManager 不关闭宿主传入的 ClassLoader；
- Runtime 关闭后不保留 CapabilityProvider、Context 和 Factory 的额外强引用；
- ResourceScope 的 manage 和 add 都按登记逆序释放；
- ResourceRegistration 提前关闭后不会被重复执行；
- CapabilityProvider.destroy() 和 Plugin.stop() 执行时托管资源仍可用；
- ResourceScope 幂等关闭并聚合全部释放异常；
- PluginEventBus 在发布线程同步分发；
- 单个事件处理器失败不会阻断其他订阅者或发布方；
- Event Subscription 自动绑定 Scope，并在停止和回滚时解除；
- 不同 PluginManager 的事件互不传播；
- PluginStartedEvent、PluginStoppedEvent 和 PluginFailedEvent 的发布时机正确；
- 诊断快照不持有运行时对象。

### 22.6 测试插件

不新增 testkit Maven 模块。Core 测试在 `src/test/resources/plugin-fixtures` 保存最小插件源码和 SPI
资源，通过测试辅助构建独立 fixture JAR。至少包括：

- 正常单 Capability Plugin；
- 多 Capability Plugin；
- 无 Capability 的后台 Plugin；
- 重复 plugin id；
- factory 返回错误类型；
- 配置校验失败；
- 初始化和销毁失败；
- 依赖链和依赖循环；
- 两个同 Capability、不同 Tags 的 Plugin。

fixture 不能引用 Console、Kernel 或 Platform。

## 23. 分阶段实现计划

### 阶段 1：公共契约和 Java 定义模型

实现：

- Plugin、CapabilityProvider、CapabilityProviderFactory 和 Context；
- PluginDefinition 和 Builder；
- CapabilityContribution、CapabilityRequirement；
- CapabilityKey、CapabilityType、Tag、Tags；
- PluginStatusCode；
- 构造校验和不可变性测试。

验收：Core 编译通过，公共类型具备完整 Javadoc，不依赖 Console。

### 阶段 2：Classpath SPI 全量发现

实现：

- ClasspathPluginDiscovery；
- ServiceLoader Provider 枚举和异常转换；
- Definition 全局校验；
- 多 fixture JAR 的自定义 ClassLoader 测试。

验收：一次发现获得指定 ClassLoader 可见的全部 Plugin，且不执行 CapabilityProviderFactory 或
生命周期。

### 阶段 3：配置

实现：

- 配置定义 Builder；
- 内置五级 ConfigSource；
- 合并、类型转换、环境变量映射；
- PluginConfig 和 SecretValue；
- Secret 脱敏测试。

验收：Plugin 只能取得校验后的本插件配置快照。

### 阶段 4：Capability 注册和路由

实现：

- CapabilityProviderFactory 调用和类型校验；
- copy-on-write Registry；
- Router 和默认 Tags；
- 类型安全 CapabilityManager；
- 并发快照测试。

验收：业务代码可仅使用 CapabilityType + Tags 获得确定 Provider。

### 阶段 5：资源、事件和生命周期

实现：

- DefaultResourceScope、ResourceRegistration 和逆序失败聚合；
- 实例级 PluginEventBus、Plugin 作用域订阅视图和三个内置生命周期事件；
- ManagedPlugin 和精简状态机；
- 原子启动、停止和失败回滚；
- PluginRuntimeInfo。

验收：失败 Plugin 无残留 Capability、CapabilityProvider、订阅或托管资源，观察者异常不改变 Plugin
生命周期结果。

### 阶段 6：依赖编排和宿主入口

实现：

- 声明索引和 DependencyResolver；
- 批量迭代启动；
- required/disabled plugin 配置；
- 单插件停止保护；
- DefaultPluginManager；
- 完整集成测试。

验收：依赖链、替代 Provider、缺失依赖、循环等待和 required plugin 失败均符合本文规则。

每阶段 Java 修改后立即运行 `mvn clean compile`。阶段内按测试驱动方式先建立失败测试，再实现生产
代码。

## 24. 验证命令

每批 Java 变更：

```bash
mvn clean compile
```

阶段完成后：

```bash
mvn -pl innospots-nexus-core -am test
mvn validate
mvn test
mvn -q help:effective-pom
git diff --check
```

若本地 JDK 低于 Java 25，只报告环境不匹配，不降低项目基线。

## 25. V1 验收标准

- 所有生产代码位于 innospots-nexus-core，Core 不依赖 Console；
- 不存在插件 YAML、Descriptor Parser 或 Capability Provider SPI；
- 指定 ClassLoader 可见的全部 Plugin SPI 实现均被发现；
- 每个 Plugin 通过 Java PluginDefinition 完整声明身份、Tags、能力、依赖和配置；
- CapabilityProviderFactory 与所属 Plugin 天然绑定；
- 两个相同 Capability、不同 Tags 的 Provider 可以确定性路由；
- 多匹配和无匹配均失败关闭；
- required/optional 依赖和循环等待诊断正确；
- Plugin 失败不会留下部分注册和托管资源；
- Secret 不出现在日志、异常和诊断；
- 并发查询只看到完整 Registry 快照；
- 单插件停止受到依赖保护；
- Runtime 不依赖全局静态状态；
- 测试覆盖 SPI、定义、依赖、配置、路由、回滚和 ClassLoader；
- Maven 编译和测试全部通过；
- 没有创建或更新模块 SKILL.md 或 references 文档。

## 26. 后续演进触发条件

| 能力 | 触发条件 |
|------|----------|
| 独立插件目录 | 生产环境要求不修改主应用 Classpath 即可安装插件 |
| 独立 ClassLoader | 需要插件依赖隔离或类卸载 |
| Reload | 要求旧 ClassLoader 下线并加载新版本 |
| 调用排空 | 存在长调用且维护窗口不可接受 |
| 配置动态刷新 | Plugin 需要不停机应用配置变化 |
| 外部 ConfigSource | 已确定 Nacos/Apollo 等具体协议 |
| SecretSource | 已确定 KMS/Vault 和凭证轮换要求 |
| Provider 优先级 | Tags 无法表达真实选择规则 |
| 健康检查 | 运维需要区分 ACTIVE 与外部依赖健康度 |
| Console 管理 | 已明确启停授权、持久化意图和管理 API |
| 进程隔离 | 需要运行非完全可信第三方插件 |

这些能力必须分别补充设计，不能通过向 PluginDefinition 随意增加字段完成。

## 27. 最终结论

当前 Nexus 最合适的插件开发模型为：

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
        +
Plugin 实现
        +
不可变 PluginDefinition
        +
CapabilityProviderFactory
```

Runtime 只使用一次 `ServiceLoader<Plugin>`，即可聚合指定 ClassLoader 可见范围内所有 JAR 和
classes 目录中的 Plugin。PluginDefinition 直接持有类型安全的 Capability 和
CapabilityProviderFactory，消除 YAML、字符串 SPI API、独立 Capability SPI 及 Provider 归属推断。

业务调用保持：

```text
CapabilityType
        +
Tags
        ↓
CapabilityManager
        ↓
唯一 ACTIVE Provider
```

该方案保留“标准 SPI 发现、Capability 解耦、Tags 路由、统一配置和明确生命周期”的核心理念，
同时把插件需要维护的配置入口缩减为一个标准 Plugin SPI 文件，符合当前工程轻量、可落地和避免
过度设计的要求。
