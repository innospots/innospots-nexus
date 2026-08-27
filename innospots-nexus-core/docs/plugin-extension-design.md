# Nexus Core 轻量级插件扩展体系设计方案

## 1. 文档定位

本文给出适配当前 Innospots Nexus 工程的通用插件扩展体系设计。设计参考《Java
轻量级无框架插件扩展体系设计方案》的核心理念，但以当前工程边界、已有基础能力和可分阶段
实现为约束，不机械复制参考文档的模块划分、类型数量或生命周期状态。

本方案的实现全部归属 `innospots-nexus-core`。`innospots-nexus-console` 在本阶段不参与插件
发现、加载、配置、Capability 注册、路由和生命周期实现。Console 将来如需提供插件管理功能，
只能调用 Core 暴露的运行时管理接口和只读诊断模型。

本文是开发设计依据，不代表所有类型已经实现。

## 2. 当前工程事实与设计约束

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

插件内核是业务中立的平台运行能力，属于 Core。它可以依赖 Base 中的异常、状态码、JSON/YAML
解析和线程基础，但不能依赖 Console、Kernel、Platform、Spring Boot 自动配置或具体业务模块。

### 2.2 已有能力

当前工程已经具备：

- Base 中的 `NexusException` 和 `NexusStatusCode`；
- Base 中的 Jackson JSON/YAML 依赖；
- Base 中的配置值对象 `NexusConfig`；
- Base 中的静态进程级 `EventBus`；
- Core 中的线程、Watcher、服务生命周期和持久化基础；
- Core 中面向管理页面的 `core.extension` 声明；
- Console 中现存的 Console Extension 发现、安装记录和启停逻辑。

通用插件内核不会继续扩建现有 Console Extension Runtime，也不会把管理页面、菜单、权限或
Endpoint 注册纳入插件内核。新内核使用独立的 `com.innospots.nexus.core.plugin` 包，避免将
通用底层 Capability 与管理控制台扩展语义混在一起。

### 2.3 设计决定

采用“Core 单模块、包级分层”的实现方式：

- 不新增 `plugin-api`、`plugin-kernel`、`plugin-testkit` Maven 模块；
- 不引入 PF4J、OSGi、Spring 或 Maven Resolver；
- 外部插件是一个普通 JAR；
- 插件静态身份由 `META-INF/nexus-plugin.yaml` 声明；
- Plugin 和 Capability Provider 均通过 Java `ServiceLoader` 发现；
- 外部插件一 JAR 一 ClassLoader、一 Plugin 实例、一套 Tags、配置和生命周期；
- 业务代码只依赖 Capability API 和 `CapabilityManager`；
- V1 支持启动加载、显式启动和安全停止，不实现目录监听和自动热更新。

## 3. 目标与非目标

### 3.1 V1 目标

1. 在 Core 中提供不依赖业务框架的 Plugin Runtime。
2. 从显式插件目录发现带有 `META-INF/nexus-plugin.yaml` 的 JAR。
3. 通过 Java SPI 加载唯一的顶层 `Plugin` 和声明的 Capability Provider。
4. 支持同一 Capability 的多个实现，并通过不可变 Tags 确定性路由。
5. 支持 Capability 名称和主版本依赖检查。
6. 支持插件默认值、宿主配置、环境变量、系统属性和运行时变量的配置覆盖。
7. 支持配置类型、必填项和 Secret 标记校验。
8. 支持 Plugin、Extension 和托管资源的原子启动与失败回滚。
9. 支持并发读取 Capability Registry。
10. 提供不泄露运行时可变对象的诊断快照。
11. 单个普通插件失败不破坏其他插件；宿主指定的必需插件失败可阻断应用启动。
12. 具备可测试、可逐阶段提交的实现结构。

### 3.2 V1 非目标

V1 不实现：

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

## 4. 总体架构

```text
plugins/*.jar
     │
     ├── META-INF/nexus-plugin.yaml
     └── META-INF/services/...
               │
               ▼
       PluginArchiveDiscovery
               │
               ▼
        PluginDescriptorParser
               │
               ▼
         PluginClassLoader
               │
        ┌──────┴────────┐
        ▼               ▼
   Plugin SPI      Capability SPI
        │               │
        └──────┬────────┘
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
| `nexus-plugin.yaml` | 插件是什么、提供和依赖什么能力 | 实现类、Tags、运行配置 |
| `Plugin` | 插件运行实例的 Tags、配置定义和生命周期 | 具体业务能力 |
| Capability SPI | 哪个 Java 类实现能力 | 业务选择哪个实现 |
| `CapabilityManager` | 业务调用时选择哪个实现 | 插件发现和生命周期 |
| `ConfigurationManager` | 插件最终获得什么配置 | 业务配置中心协议 |
| `PluginManager` | 插件何时加载、启动、停止和回滚 | UI、持久化和业务管理规则 |
| `ResourceScope` | 插件副作用何时释放 | JVM 安全隔离 |

## 5. Core 包结构

```text
com.innospots.nexus.core.plugin
├── contract
│   ├── Plugin
│   ├── Extension
│   ├── PluginContext
│   └── ExtensionContext
├── declaration
│   ├── PluginDescriptor
│   ├── ProvidedCapability
│   └── RequiredCapability
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
│   ├── PluginArchive
│   ├── PluginArchiveDiscovery
│   ├── PluginDescriptorParser
│   ├── JacksonPluginDescriptorParser
│   └── PluginClassLoader
├── dependency
│   ├── DependencyResolver
│   └── DependencyResolution
├── event
│   ├── PluginEventBus
│   └── Subscription
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
`DefaultXxx` 名称，不创建没有边界意义的 `impl` 子包。

## 6. 插件包和发现协议

### 6.1 插件 JAR 结构

```text
message-wecom.jar
├── META-INF
│   ├── nexus-plugin.yaml
│   └── services
│       ├── com.innospots.nexus.core.plugin.contract.Plugin
│       └── com.example.message.MessagePushProvider
└── com/example/plugin/wecom/...
```

JAR 只有同时满足以下条件才是有效插件：

1. 位于宿主显式配置的插件目录；
2. 是普通、可读、非符号链接的 `.jar` 文件；
3. 包含唯一的 `META-INF/nexus-plugin.yaml`；
4. 描述文件通过完整校验；
5. 包含且只包含一个本 JAR ClassLoader 加载的 `Plugin` SPI 实现。

普通依赖 JAR 即使位于应用 classpath，只要不由插件目录发现，也不会被当作插件。V1 不扫描
整个 classpath，避免发现范围不可控以及一个 ClassLoader 下多个描述文件和多个 Plugin SPI
无法可靠归属的问题。

### 6.2 发现顺序

`PluginArchiveDiscovery` 按规范化绝对路径读取插件目录，对 JAR 文件名进行字典序排序后发现。
稳定顺序只用于诊断和测试，不能作为 Capability 路由优先级。

发现阶段只允许：

- 检查文件；
- 读取 JAR 元数据；
- 解析并校验 YAML；
- 计算来源路径、大小和可选摘要；
- 建立声明索引。

发现阶段不创建 Plugin 类、不建立网络连接、不启动线程，也不执行 Capability Provider 代码。

### 6.3 描述文件格式

V1 使用以下最小格式：

```yaml
id: message-wecom
name: WeCom Message Plugin
version: 1.0.0
apiVersion: 1

capabilities:
  provides:
    - name: message.push
      version: 1
      spi: com.example.message.MessagePushProvider

  requires:
    - name: http.client
      version: 1
      required: true
```

字段规则：

| 字段 | 规则 |
|------|------|
| `id` | 全局唯一；`[a-z][a-z0-9]*(?:-[a-z0-9]+)*`；发布后不可变 |
| `name` | 非空展示名；不参与运行时身份 |
| `version` | 插件发布版本；V1 只保留和展示，不进行 SemVer 范围运算 |
| `apiVersion` | 插件协议主版本；V1 只接受 `1` |
| `provides.name` | 小写点分能力名，如 `message.push` |
| `provides.version` | 正整数主版本 |
| `provides.spi` | 宿主父 ClassLoader 可见的 Capability API 全限定名 |
| `requires.name` | 依赖的逻辑 Capability 名 |
| `requires.version` | 要求完全相等的主版本 |
| `requires.required` | 缺省为 `true`；`false` 表示可选依赖 |

YAML 不允许出现：

- Plugin 或 Provider 实现类；
- Tags；
- 默认路由；
- 真实配置值或 Secret；
- ClassLoader 选项；
- Endpoint、页面、菜单或权限；
- 任意未识别顶层字段。

Jackson YAML 解析器必须开启未知字段失败，禁止 YAML 原生类型携带任意 Java 类型信息。解析结果
先进入内部 DTO，再转换为经过校验的不可变声明 record，不能直接反序列化为运行时对象。

### 6.4 描述模型

```java
public record PluginDescriptor(
        String id,
        String name,
        String version,
        int apiVersion,
        List<ProvidedCapability> provides,
        List<RequiredCapability> requires
) {
}

public record ProvidedCapability(
        String name,
        int version,
        String spi
) {
}

public record RequiredCapability(
        String name,
        int version,
        boolean required
) {
}
```

所有集合在构造时使用 `List.copyOf`。一个插件内不得重复声明相同 `name + version` 的提供能力
或依赖能力；同一个 Capability API 不应映射到多个不同逻辑能力。

## 7. ClassLoader 和依赖策略

### 7.1 V1 ClassLoader 模型

每个有效外部插件创建一个可关闭的 `PluginClassLoader`，其父加载器由宿主显式传入，缺省为
创建 `PluginManager` 的 ClassLoader。

```text
Host ClassLoader
├── JDK
├── innospots-nexus-base
├── innospots-nexus-core Plugin API
└── 宿主共享的 Capability API
        ▲
        │ parent-first
PluginClassLoader(message-wecom.jar)
└── Plugin 实现、Provider 实现和插件私有依赖
```

V1 使用 JDK `URLClassLoader` 的 parent-first 语义，不实现 child-first 包规则。这样可以优先保证
共享 API 类型一致，避免同名 `Capability API != Capability API`。

### 7.2 共享 API 约束

以下类型必须由父 ClassLoader 加载：

- `Plugin`、`Extension` 和所有 Plugin Core 公共契约；
- Capability API 接口及业务调用需要共享的请求和结果模型；
- Base 中作为公共契约使用的类型。

加载 `provides.spi` 后必须校验：

1. 类型是接口；
2. 类型继承 `Extension`；
3. 类型对父 ClassLoader 可见；
4. 父加载器解析出的类型与插件加载器解析出的类型是同一个 `Class` 对象。

插件不得把 `innospots-nexus-base`、`innospots-nexus-core` 或共享 Capability API 打入 shaded
JAR。发现重复 API 类时以父加载器类型为准，并记录明确诊断。

### 7.3 插件私有依赖

V1 不解析 Maven 坐标，也不加载 `plugins/<id>/lib`。插件有第三方依赖时由插件构建负责形成
单一 shaded JAR。插件作者必须处理依赖冲突和 ServiceLoader 资源合并。

ClassLoader 隔离是类型和资源加载边界，不是安全沙箱。不可信插件必须运行在独立进程或容器中。

### 7.4 SPI 归属校验

顶层 Plugin 使用：

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
```

Capability Provider 使用其 API 全限定名作为 SPI 文件名。运行时使用 `ServiceLoader.stream()`
先检查 `Provider.type().getClassLoader()`，只实例化由当前 `PluginClassLoader` 加载的实现，避免把
父 ClassLoader 中的 Provider 错误归属到当前插件。

一个插件 JAR：

- 必须有且只有一个本地 `Plugin` Provider；
- 每个 `provides` 声明必须有且只有一个本地 Capability Provider；
- 未被 YAML 声明的本地 Capability Provider 不会自动注册；
- 声明与 SPI 缺失或多实现均使整个插件启动失败。

## 8. Plugin、Extension 和上下文契约

### 8.1 Plugin

```java
public interface Plugin {

    String id();

    Tags tags();

    default ConfigDefinition configDefinition() {
        return ConfigDefinition.empty();
    }

    default void initialize(PluginContext context) {
    }

    default void start() {
    }

    default void stop() {
    }
}
```

约束：

- 公共无参构造函数、`id()`、`tags()` 和 `configDefinition()` 必须是无副作用描述操作；
- `id()` 必须与 YAML `id` 完全一致；
- 每个 Plugin 至少提供一个 Tag；
- 网络连接、线程和外部资源只能在 `initialize` 或 `start` 阶段创建；
- Plugin 不实现消息、存储、脚本等具体 Capability。

### 8.2 Extension

```java
public interface Extension {

    default void initialize(ExtensionContext context) {
    }

    default void destroy() {
    }
}
```

简单 Provider 可以只实现 Capability 方法。需要配置、依赖能力或资源的 Provider 在
`initialize` 中取得上下文，并把副作用登记到 `ResourceScope`。

### 8.3 上下文

```java
public interface PluginContext {

    PluginDescriptor descriptor();

    Tags tags();

    PluginConfig config();

    CapabilityManager capabilities();

    PluginEventBus events();

    ResourceScope resources();

    System.Logger logger();
}

public interface ExtensionContext extends PluginContext {

    CapabilityKey capability();
}
```

上下文只暴露稳定契约，不暴露 `ManagedPlugin`、ClassLoader、Registry 可写接口或其他插件的
运行时对象。插件不能自行注册 Capability，也不能修改自己的状态。

## 9. Tags 和 Capability 模型

### 9.1 Tags

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

Tag name 和 value 使用小写 kebab-case。`Tags` 内部按 key 排序并保持不可变；同名不同值不能
覆盖，构造时直接失败。

匹配规则固定为：请求 Tags 是 Provider Tags 的约束子集。例如 Provider Tags 为
`channel=wecom, provider=tencent` 时，请求 `channel=wecom` 可以匹配，请求
`provider=custom` 不能匹配。

### 9.2 Capability 身份

```java
public record CapabilityKey(String name, int majorVersion) {
}

public record CapabilityType<T extends Extension>(
        CapabilityKey key,
        Class<T> api
) {

    public static <T extends Extension> CapabilityType<T> of(
            String name,
            int majorVersion,
            Class<T> api
    );
}
```

Capability API 推荐发布类型安全常量：

```java
public interface MessagePushProvider extends Extension {

    CapabilityType<MessagePushProvider> CAPABILITY = CapabilityType.of(
            "message.push",
            1,
            MessagePushProvider.class);

    PushResult send(PushMessage message);
}
```

业务代码不依赖插件 ID 和实现类：

```java
MessagePushProvider provider = capabilities.require(
        MessagePushProvider.CAPABILITY,
        Tags.of("channel", "wecom"));
```

### 9.3 注册模型

```java
public record CapabilityRegistration<T extends Extension>(
        CapabilityType<T> type,
        T provider,
        String pluginId,
        Tags tags
) {
}
```

注册对象只在 Provider 完成初始化后发布。Registry 内部至少建立：

- `CapabilityKey -> immutable registrations`；
- `pluginId -> registered CapabilityKey`。

### 9.4 CapabilityManager

```java
public interface CapabilityManager {

    <T extends Extension> T require(
            CapabilityType<T> type,
            Tags requiredTags);

    <T extends Extension> Optional<T> find(
            CapabilityType<T> type,
            Tags requiredTags);

    <T extends Extension> List<T> findAll(
            CapabilityType<T> type);
}
```

可以增加无 Tags 的便利重载，但其规则必须固定，不能隐式随机选择。

### 9.5 路由规则

Provider 选择顺序：

1. 调用方提供非空 Tags：按调用方 Tags 匹配；
2. 调用方未提供 Tags：使用宿主为该 `CapabilityKey` 配置的默认 Tags；
3. 仍未指定 Tags：只有一个 ACTIVE Provider 时返回该 Provider；
4. 没有匹配项：抛出 `NexusException`，状态码为 Capability Not Found；
5. 多个匹配项：抛出 `NexusException`，状态码为 Capability Ambiguous。

发现顺序、插件文件名、注册顺序均不得用于打破歧义。V1 不支持 Provider 自报
`default=true` 或 `priority`。

## 10. Capability 依赖解析

### 10.1 声明级与运行时级

依赖有两个层次：

```text
DECLARED：至少一个已发现描述声明 provides(name, version)
AVAILABLE：CapabilityRegistry 中至少一个对应 ACTIVE Provider
```

插件只有在全部 required 依赖达到 AVAILABLE 后才能启动。optional 依赖缺失不阻止启动，插件
通过 `CapabilityManager.find` 自行降级。

### 10.2 启动算法

`PluginManager.start()` 的顺序：

1. 完成全部 JAR 发现和 Descriptor 校验；
2. 建立 `CapabilityKey -> provider plugin ids` 声明索引；
3. 标记声明级完全缺失的 required 依赖；
4. 反复选择 required 依赖已经 AVAILABLE 的插件启动；
5. 每个插件成功后重新评估 WAITING 插件；
6. 一轮没有任何插件取得进展时停止迭代；
7. 剩余插件保持 WAITING，并输出缺失能力或循环等待诊断；
8. 宿主 required plugin 未 ACTIVE 时，启动整体失败并关闭本轮已启动插件。

同一 Capability 有多个 Provider 时，依赖满足只要求至少一个 ACTIVE Provider，不在依赖声明中
绑定 Tags 或具体插件。Tags 只用于业务调用时选择实现。

### 10.3 停止约束

显式停止单个插件前，Manager 计算停止后的 Capability 可用性。如果某个 ACTIVE 插件的 required
依赖将失去最后一个 Provider，则拒绝停止，并返回包含依赖插件和 CapabilityKey 的错误。

V1 不自动级联停止，也不把 ACTIVE 插件自动切换为 WAITING。应用整体关闭时，Manager 按实际启动
顺序的逆序停止全部插件，不执行单插件依赖保护。

这个规则使 V1 的状态变化确定且易于测试，后续再按真实需求增加自动依赖联动。

## 11. 配置体系

### 11.1 配置定义

插件通过 Java 定义配置结构和默认值，不在 `nexus-plugin.yaml` 中放运行配置：

```java
public interface ConfigDefinition {

    Collection<ConfigItemDefinition> items();

    static ConfigDefinition empty();
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

`defaultValue` 统一使用字符串保存，在配置阶段按 `ConfigType` 转换。Secret 不允许定义默认值。
配置 key 使用点分小驼峰路径，例如 `http.connectTimeout`，不得携带 `plugins.<id>` 前缀。

### 11.2 配置命名空间

完整配置 key 固定为：

```text
plugins.<plugin-id>.<item-key>
```

例如：

```text
plugins.message-wecom.corpId
plugins.message-wecom.http.connectTimeout
```

插件内部的 `PluginConfig` 只使用局部 key，例如 `corpId`，防止插件越权读取其他插件配置。

### 11.3 V1 配置来源和优先级

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
| Plugin Default | `ConfigDefinition` 的非 Secret 默认值 |
| Host Map | 宿主从自身配置文件或配置系统解析后传入的扁平 Map |
| Environment | 根据已知配置定义计算环境变量名 |
| System Property | 直接使用完整配置 key |
| Runtime Variables | Builder 显式传入，主要用于启动参数和测试 |

Core V1 不再实现一套宿主配置文件格式。宿主可以使用 YAML、JSON、数据库或其他方式，统一转换为
Map 后交给 Runtime，从而避免插件内核和应用配置框架耦合。

环境变量映射示例：

```text
plugins.message-wecom.http.connectTimeout
        ↓
NEXUS_PLUGIN_MESSAGE_WECOM_HTTP_CONNECT_TIMEOUT
```

环境变量名只对 Descriptor 和 ConfigDefinition 中已经声明的 key 生成。若两个不同 key 生成同一
环境变量名，配置阶段直接失败，不能静默覆盖。

### 11.4 PluginConfig

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

配置合并完成后执行：

1. 拒绝当前插件命名空间中的未知 key；
2. 校验 required；
3. 按声明类型转换；
4. 校验 Secret 不为空；
5. 生成不可变 `PluginConfig`；
6. 生成诊断信息时只显示 key、来源和是否存在，不显示 Secret 值。

`SecretValue.toString()`、日志和运行时快照固定输出掩码。V1 只解决 Secret 值的标记、读取和防止
误输出，不承诺堆内加密，也不解析 `secret:` 引用。

### 11.5 ConfigSource 扩展边界

V1 提供内部 `ConfigSource` 抽象，但不通过普通 Plugin 加载 ConfigSource，避免
ConfigurationManager 与 PluginManager 的循环启动依赖。未来需要 Nacos、Apollo 或 Vault 时，
应设计独立的 bootstrap SPI，经单独方案评审后加入，不复用普通 Capability 生命周期。

## 12. 事件和资源生命周期

### 12.1 实例级 PluginEventBus

Base 当前 `EventBus` 是静态进程级总线，订阅返回 `void`，不适合直接绑定插件卸载生命周期。
插件内核提供实例级事件门面：

```java
public interface PluginEventBus {

    <E> Subscription subscribe(
            Class<E> eventType,
            Consumer<E> handler);

    void publish(Object event);
}

public interface Subscription extends AutoCloseable {

    @Override
    void close();
}
```

V1 同步、同进程发布，处理器异常按发布调用返回。它用于状态通知，不代替需要返回值的同步
Capability 调用，也不桥接 Base 静态 EventBus。

### 12.2 ResourceScope

```java
public interface ResourceScope extends AutoCloseable {

    <T extends AutoCloseable> T manage(T resource);

    ResourceRegistration add(Runnable disposer);

    @Override
    void close();
}
```

规则：

- 每个插件运行实例独占一个 Scope；
- 注册后立即返回原资源或可关闭 Registration；
- Scope 关闭时按注册逆序释放；
- 单个 disposer 失败不阻止后续资源释放；
- 所有释放异常聚合到一个 `NexusException`，首个异常作为 cause，其余作为 suppressed；
- Scope 关闭幂等；关闭后继续注册资源立即失败；
- Event Subscription 必须由 Scope 托管。

Plugin 和 Extension 的显式 `stop/destroy` 仍然保留，用于对象自身状态收尾；线程池、客户端、订阅
和回调等外部资源优先交给 Scope，确保初始化中途失败也能统一回滚。

## 13. 生命周期和状态机

### 13.1 精简状态

参考方案中的十余个状态对 V1 过细。Core V1 只公开：

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

具体阶段通过 `PluginRuntimeInfo.phase` 和 `lastError` 诊断，不把每个内部步骤都建模为持久状态。

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

V1 没有持久化 `DISABLED`。没有启动的已发现插件使用 DESCRIBED/STOPPED 表示；宿主要禁用某插件，
在 `PluginRuntimeConfig.disabledPluginIds` 中配置。将来若 Console 管理需要持久化启用意图，由
Console 或应用层保存配置并调用 Core，不把数据库实体放入插件内核。

### 13.2 单插件启动顺序

```text
1. 创建 PluginClassLoader
2. 加载并校验唯一 Plugin SPI
3. 校验 descriptor.id == plugin.id
4. 获取并校验 Tags、ConfigDefinition
5. 合并和校验 PluginConfig
6. 校验 required Capability 已 AVAILABLE
7. 为插件创建 ResourceScope、PluginEventBus 视图和 PluginContext
8. 加载并校验全部 Capability SPI Provider
9. plugin.initialize(context)
10. extension.initialize(context)，按 YAML provides 顺序执行
11. plugin.start()
12. 一次性向 CapabilityRegistry 发布全部 registrations
13. 状态切换为 ACTIVE
```

Capability 只有在 Plugin 和全部 Provider 初始化、启动成功后才一次性发布。其他线程不会看到
半激活插件。

### 13.3 启动失败回滚

任一步骤失败时：

```text
从 Registry 移除当前插件全部 Capability（如果已经发布）
        ↓
逆序调用已初始化 Extension.destroy()
        ↓
调用 Plugin.stop()（仅当 initialize 已开始）
        ↓
关闭 ResourceScope
        ↓
关闭 PluginClassLoader
        ↓
记录 FAILED、失败阶段和根因
```

回滚必须尽最大努力执行全部步骤。清理错误作为 suppressed 异常保留，不能覆盖原始启动异常。

### 13.4 停止顺序

```text
1. 状态 ACTIVE -> STOPPING
2. 原子撤出该插件全部 Capability，拒绝新查找
3. 逆序调用 Extension.destroy()
4. 调用 Plugin.stop()
5. 关闭 ResourceScope
6. 清除强引用
7. 关闭 PluginClassLoader
8. 状态 -> STOPPED
```

V1 没有调用引用计数。撤出 Registry 前已经取得 Provider 引用的调用可能与停止并发，因此单插件
停止属于维护操作，调用方应先停止接收相关业务请求。完整调用排空作为 V2 独立能力设计。

从 STOPPED 重新启动时，Manager 根据保留的 `PluginArchive` 和 Descriptor 重新创建 ClassLoader、
Plugin、Extension、Context 与 ResourceScope，不复用已经关闭的运行实例。

### 13.5 PluginManager

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

`start()` 完成发现和批量启动；重复调用必须幂等。`start(pluginId)` 只启动已发现、依赖满足且未
ACTIVE 的插件。`close()` 按逆启动顺序关闭全部 ACTIVE 插件并保持幂等。

## 14. 运行时配置和宿主接入

### 14.1 PluginRuntimeConfig

```java
public record PluginRuntimeConfig(
        Path pluginDirectory,
        Set<String> requiredPluginIds,
        Set<String> disabledPluginIds,
        Map<String, String> hostConfig,
        Map<String, String> runtimeVariables,
        Map<CapabilityKey, Tags> defaultRoutes,
        ClassLoader parentClassLoader
) {
}
```

集合和 Map 必须防御性复制。`requiredPluginIds` 与 `disabledPluginIds` 不能相交；默认路由只接受
已经合法化的 CapabilityKey 和 Tags。

### 14.2 应用启动示例

```java
PluginRuntimeConfig config = new PluginRuntimeConfig(
        Path.of("./plugins"),
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

Core 不提供全局静态 `Plugins.get(...)`。应用通过构造参数显式传递 `CapabilityManager`，保证测试
可替换、生命周期清楚，并允许同一 JVM 创建相互隔离的多个 Runtime。

## 15. 并发、一致性和失败隔离

### 15.1 并发模型

插件生命周期是低频写操作，Capability 查询是高频读操作：

- `PluginManager` 使用单一生命周期锁串行执行 discover/start/stop/close；
- 不在持有生命周期锁时调用业务 Capability；
- `CapabilityRegistry` 使用 `AtomicReference` 保存不可变索引快照；
- 注册和撤出通过 copy-on-write 原子替换完整快照；
- `CapabilityRouter` 只读取一次快照并在该快照内完成匹配；
- 返回的列表和诊断模型均为不可变副本；
- Plugin、Extension 自身线程安全由各 Capability 契约声明，Kernel 不自动串行化业务调用。

### 15.2 原子边界

- 单个插件的全部 Capability 一次性注册或一次性撤出；
- 一个插件失败不能留下部分注册；
- 批量启动不是全局事务，普通插件失败不回滚已成功的其他普通插件；
- required plugin 失败时，Manager 关闭本轮已经启动的插件并使整体启动失败；
- Descriptor 全局冲突属于发现失败，在执行任何 Plugin 代码前终止批量启动。

### 15.3 冲突规则

以下冲突失败关闭：

- 重复 plugin id；
- 同插件重复 Capability 声明；
- Plugin SPI 数量不是 1；
- Capability SPI 数量不是 1；
- Plugin id 与 Descriptor id 不一致；
- Capability API 不继承 Extension 或不由父 ClassLoader 共享；
- 同一个 plugin + capability + tags 重复注册；
- 默认路由命中多个 Provider；
- 必需依赖缺失或循环等待；
- 配置 key、环境变量映射或类型转换冲突。

## 16. 错误模型和诊断

不创建一类错误一个异常子类。所有运行时和配置错误使用 `NexusException`，通过实现
`StatusCode` 的 `PluginStatusCode` 区分：

```text
PLUGIN_DISCOVERY_FAILED
PLUGIN_DESCRIPTOR_INVALID
PLUGIN_DUPLICATE
PLUGIN_API_INCOMPATIBLE
PLUGIN_SPI_INVALID
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

`PluginStatusCode` 放在 `com.innospots.nexus.core.plugin.status`，不要把插件专用错误全部加入 Base
的 `NexusStatusCode`。

错误消息至少包含：

- plugin id 和来源 JAR；
- 当前阶段；
- CapabilityKey 或配置 key；
- SPI API；
- 冲突的候选 plugin ids；
- 原始 cause。

任何诊断不得包含 Secret 值。

### 16.1 PluginRuntimeInfo

```java
public record PluginRuntimeInfo(
        String id,
        String name,
        String version,
        PluginState state,
        String phase,
        Tags tags,
        List<CapabilityKey> providedCapabilities,
        List<RequiredCapability> requiredCapabilities,
        Map<CapabilityKey, DependencyResolution> dependencies,
        String source,
        Instant discoveredAt,
        Instant startedAt,
        String lastError
) {
}
```

诊断只保留可展示的错误摘要；完整异常由 Core 日志记录。`PluginRuntimeInfo` 不持有 Plugin、
Provider、Context、ClassLoader、配置值或资源实例。

## 17. 日志和安全边界

### 17.1 日志

Core 默认实现使用项目现有 SLF4J 记录运行时日志。对插件只通过 `PluginContext.logger()` 暴露
JDK `System.Logger`，避免把具体日志实现变成插件公共契约。

至少记录：

- 发现来源和 Descriptor 摘要；
- 状态转换；
- Capability 注册和撤出；
- 依赖等待原因；
- 配置来源命中，但不记录值；
- 启动、停止和资源释放失败；
- ClassLoader 关闭结果。

### 17.2 安全

插件是宿主进程内的可信代码，ClassLoader 不能限制其文件、网络、反射或进程权限。V1 的安全措施
只包括：

- 只扫描显式目录；
- 拒绝符号链接和非普通 JAR；
- 不解析远程 URI；
- YAML 禁止任意类型构造；
- 不输出 Secret；
- 不接受插件自定义 ClassLoader 策略；
- 不从插件下载依赖；
- 不把可写 Registry 或 Runtime 内部对象暴露给插件。

如果将来需要运行第三方不可信插件，必须采用独立进程、RPC 和操作系统权限隔离，不能在本内核上
追加一个布尔 `sandbox=true` 选项来宣称安全。

## 18. 与现有 extension 代码的关系

当前仓库的两组代码需要明确区分：

```text
core.extension
    当前是 Console 扩展声明模型

console.extension
    当前是 Console 扩展发现、安装记录和运行时逻辑

core.plugin
    本文设计的通用插件内核
```

V1 实现期间遵循：

1. 不让 `core.plugin` 依赖现有 `core.extension`；
2. 不让 `core.plugin` 依赖 `console.extension`；
3. 不在本阶段改造 Console 页面、菜单、Endpoint 和权限；
4. 不把 Console 的安装数据库实体移动到 Core；
5. 不让两套 Runtime 同时管理同一个插件 JAR；
6. 新增底层能力插件只接入 `core.plugin`；
7. 现有 Console Extension 代码停止新增通用插件能力。

后续 Console 管理支持应另行设计：Console 保存管理员的启停意图和展示数据，通过 Core
`PluginManager` 执行操作，并读取 `PluginRuntimeInfo`。运行状态事实仍由 Core Runtime 持有，
Console 不再实现第二套发现、依赖解析或生命周期状态机。

现有 `extension-design.md` 描述的是 Console 页面扩展，不应作为新通用插件内核的实现规范。待
Console 正式迁移时，再决定保留、改名或废弃该文档和对应代码；本方案不提前删除它们。

## 19. V1 测试策略

### 19.1 声明和发现测试

- 有效 YAML 解析为不可变 Descriptor；
- 未知字段、空字段、非法 id、非法能力名和非正版本失败；
- 重复 plugin id 在任何 Plugin 代码执行前失败；
- 无 Descriptor 的 JAR 被忽略或按配置记录诊断；
- 非 JAR、符号链接和不可读文件不进入加载流程；
- 发现顺序稳定但不影响路由；
- Plugin SPI 缺失、多实现、构造失败和 id 不一致失败；
- SPI 只归属当前插件 ClassLoader。

### 19.2 Capability 测试

- 类型安全的 CapabilityType 正确校验 name/version/api；
- Provider 自动继承 Plugin Tags；
- Tags 子集匹配正确；
- 显式 Tags、宿主默认 Tags、唯一 Provider 三层路由顺序正确；
- 无匹配和多匹配使用不同状态码；
- Registry 批量注册和撤出具有原子性；
- 并发查询只能看到注册前或注册后的完整快照；
- 不允许 Provider API 类型来自插件私有 ClassLoader。

### 19.3 配置测试

- 五级来源覆盖顺序正确；
- 未知 key、缺少 required、类型转换和环境变量映射冲突失败；
- Plugin 只能读取自己的局部 key；
- Secret 不允许默认值；
- Secret 的 `toString`、诊断和异常不泄露值；
- ConfigDefinition 和 PluginConfig 集合不可变。

### 19.4 生命周期测试

- required 依赖就绪后才启动；
- optional 依赖缺失仍可启动；
- 多 Provider 中一个 ACTIVE 即满足依赖；
- 缺失和循环等待诊断可区分；
- Plugin/Extension 初始化顺序和销毁逆序正确；
- 任一 Provider 初始化失败不发布部分 Capability；
- 回滚继续释放全部资源并保留 suppressed 异常；
- 停止先撤出 Capability 再销毁资源；
- 单插件停止不会破坏依赖它的 ACTIVE 插件；
- `start`、`stop` 和 `close` 的幂等性正确；
- 普通插件失败隔离，required plugin 失败回滚整体启动。

### 19.5 ClassLoader 和资源测试

- 每个外部插件使用不同 ClassLoader；
- 共享 API 由父加载器解析；
- 插件私有同名类互不影响；
- ClassLoader 在失败和停止后关闭；
- ResourceScope 逆序、幂等和聚合异常行为正确；
- Event Subscription 随 Scope 自动解除；
- 诊断快照不持有运行时对象。

### 19.6 测试插件构建方式

不新增 testkit Maven 模块。Core 测试在 `src/test/resources/plugin-fixtures` 保存最小插件源码和
描述资源，通过 Maven 测试阶段或测试辅助类生成独立 fixture JAR。至少准备：

- 正常单 Capability 插件；
- 多 Capability 插件；
- 缺少 Provider 插件；
- 重复 Provider 插件；
- 配置校验失败插件；
- 初始化和销毁失败插件；
- 依赖链和依赖循环插件；
- 两个同 Capability 不同 Tags 的插件。

fixture 不能引用 Console、Kernel 或 Platform。

## 20. 分阶段实现计划

### 阶段 1：公共契约和声明模型

实现：

- `Plugin`、`Extension` 和上下文接口；
- `PluginDescriptor`、Capability 声明；
- `CapabilityKey`、`CapabilityType`、`Tag`、`Tags`；
- `PluginStatusCode`；
- 构造校验和不可变性测试。

验收：Core 编译通过，所有公共类型具备完整 Javadoc，不依赖 Console。

### 阶段 2：Descriptor 和 JAR 发现

实现：

- `PluginArchiveDiscovery`；
- Jackson YAML 严格解析；
- `PluginClassLoader`；
- Plugin SPI 加载和归属校验；
- fixture JAR 测试基础。

验收：发现阶段不执行 Plugin 业务生命周期，所有全局声明冲突在加载前被检测。

### 阶段 3：配置

实现：

- 配置定义和 Builder；
- 内置五级 ConfigSource；
- 合并、类型转换、环境变量映射；
- `PluginConfig` 和 `SecretValue`；
- Secret 脱敏测试。

验收：插件只能取得校验后的本插件配置快照。

### 阶段 4：Capability 注册和路由

实现：

- Capability Provider SPI 加载；
- copy-on-write Registry；
- Router 和默认 Tags；
- 类型安全 `CapabilityManager`；
- 并发快照测试。

验收：业务代码可仅使用 CapabilityType + Tags 获得确定的 Provider。

### 阶段 5：资源、事件和生命周期

实现：

- `DefaultResourceScope`；
- 实例级 `PluginEventBus`；
- `ManagedPlugin` 和精简状态机；
- 原子启动、停止和失败回滚；
- `PluginRuntimeInfo`。

验收：失败插件无残留 Capability、资源或 ClassLoader 强引用。

### 阶段 6：依赖编排和宿主入口

实现：

- 声明索引和 `DependencyResolver`；
- 批量迭代启动；
- required/disabled plugin 配置；
- 单插件停止保护；
- `DefaultPluginManager` 对外入口；
- 完整集成测试。

验收：依赖链、替代 Provider、缺失依赖、循环等待和 required plugin 失败均符合本文规则。

每个阶段的 Java 修改完成后必须立即执行 `mvn clean compile`。阶段内按测试驱动方式先建立失败的
契约或行为测试，再实现生产代码。

## 21. 验证命令

实现阶段每批 Java 变更：

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

若本地 JDK 低于项目要求的 Java 25，只报告环境不匹配，不降低项目基线。

## 22. V1 验收标准

满足以下条件才视为 V1 完成：

- 所有生产代码位于 `innospots-nexus-core`，且 Core 不依赖 Console；
- 一个有效外部插件 JAR 可以从显式目录被发现、配置、启动和停止；
- Plugin 和每个声明的 Capability Provider 都通过标准 SPI 发现；
- 两个相同 Capability、不同 Tags 的 Provider 可以确定性路由；
- 多匹配和无匹配均失败关闭；
- required/optional 依赖语义和循环等待诊断正确；
- 插件失败不会留下部分注册和托管资源；
- Secret 不出现在日志、异常和诊断快照；
- 并发查询只看到完整 Registry 快照；
- 单插件停止受到依赖保护；
- Runtime 可以被同一 JVM 多实例化，不依赖全局静态状态；
- 测试覆盖有效插件、无效插件、依赖、配置、路由、回滚和 ClassLoader；
- Maven 编译和测试验证全部通过；
- 没有创建或更新任何模块 `SKILL.md` 或 `references/` 文档。

## 23. 后续演进触发条件

只有出现明确业务需求和可验证场景时才进入 V2：

| 能力 | 触发条件 |
|------|----------|
| 目录监听 | 生产环境确实要求无需重启发现新 JAR |
| Reload | 插件发布流程要求原 ClassLoader 下线并加载新版本 |
| 调用排空 | 存在长调用且维护停机窗口不可接受 |
| 配置动态刷新 | 有插件需要不停机应用配置变化 |
| 外部 ConfigSource | 已确定 Nacos/Apollo 等具体接入协议 |
| SecretSource | 已确定 KMS/Vault 和凭证轮换要求 |
| Provider 优先级 | Tags 仍无法表达真实选择规则 |
| 健康检查 | 运维需要区分 ACTIVE 与实际外部依赖健康度 |
| Console 管理 | 已明确启停授权、持久化意图和管理 API |
| 进程隔离 | 需要运行非完全可信的第三方插件 |

这些能力必须分别补充设计，不能通过扩展 V1 状态枚举或向 `nexus-plugin.yaml` 随意增加字段完成。

## 24. 最终结论

当前 Nexus 最合适的插件体系不是新增一组 Maven 框架模块，也不是继续放大 Console Extension，
而是在 `innospots-nexus-core` 内建立一个职责清晰、实例级、可关闭的通用 Plugin Runtime。

V1 的稳定开发模型为：

```text
META-INF/nexus-plugin.yaml
        +
Plugin SPI
        +
Plugin
        +
Capability API
        +
Capability Provider SPI
```

业务调用模型为：

```text
CapabilityType
        +
Tags
        ↓
CapabilityManager
        ↓
唯一 ACTIVE Provider
```

该方案保留参考设计中“YAML 声明、SPI 发现、Capability 解耦、Tags 路由、统一配置和明确
生命周期”的核心理念，同时删去当前工程尚不需要的动态安装、复杂版本、配置中心、热更新和
完整调用排空，使第一阶段可以在 Core 单模块内按六个独立阶段落地和验证。
