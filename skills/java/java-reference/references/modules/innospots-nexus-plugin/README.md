# innospots-nexus-plugin — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：0.1.0-SNAPSHOT

## 模块概览

基于 `innospots-nexus-core` 的 classpath 插件运行时：发现与声明编译、安装与生命周期、Capability 路由、贡献解码/校验/快照，以及 `console@1` 与 **Pactor Page DSL 1.0**。

**能力一览：**

| 能力 | 说明 |
|------|------|
| **Plugin SPI** | `Plugin`、`PluginDefinition`、`PluginContext` |
| **发现** | `ClasspathPluginDiscovery`、`PluginCatalog`、`PluginDefinitionCompiler` |
| **安装** | `PluginInstallationManager`、`PluginInstallationEntity`、状态机 |
| **生命周期** | `PluginState`、`PluginRuntimeInfo`、事件总线 |
| **Capability** | `CapabilityRouter`、`CapabilityTypeRegistry`、`ProviderRef` |
| **贡献** | `PluginContribution*` 注册表、decoder/snapshotter |
| **Console 贡献** | `ConsolePluginContributionHandler`、`console@1` 模型 |
| **Page DSL** | YAML 解析、节点/数据源/动作/权限 DSL 类型 |
| **资源作用域** | `ResourceScope`、`ResourceRegistration` |

**不包含：** Spring/Quarkus 自动配置、控制台 catalog 表持久化（`nx_console_catalog_resource` 归属 **console**）、
管理 REST 端点、用户/角色业务。

## 模块边界

| 归属 plugin | 归属 console | 归属 core |
|-------------|--------------|-----------|
| 发现、安装、生命周期、Capability 路由 | catalog 索引持久化与同步 REST | 持久化基类、审计、`OwnershipEntity` |
| `console@1` 声明、Handler、贡献快照 | `ConsoleCatalogSyncService` | Quartz、server、watcher |
| **Pactor Page DSL 1.0** 解析/校验/加载 | 权限运行时、导航组装 | `MetaResourceService` |

Java 包名：`com.innospots.nexus.core.plugin.*`（兼容既有 import）。Maven artifact：`innospots-nexus-plugin`。

## 扩展指南

| 需求 | 使用 plugin 中的 | 不要放在 plugin 中 |
|------|------------------|---------------------|
| 新 classpath 插件 | 实现 `Plugin` + `PluginDefinition` | HTTP 端点、租户业务 |
| 控制台 UI 贡献 | `console@1` + `ConsolePluginContributionHandler` | catalog 表 DAO（console） |
| 页面 YAML | `PageDslLoader` / `JacksonPageDslParser` | 前端渲染（产品侧） |
| 安装/启用/禁用 | `PluginInstallationManager`、`PluginRuntimeFactory` | console 管理 REST（委托 manager） |
| 能力暴露 | `CapabilityRouter`、`CapabilityRegistration` | 业务域服务实现 |
| 宿主启动 | `PluginHostBootstrap`、`NexusStartupTask` 注册 | Spring `@Configuration` |

深度设计见模块内文档：`innospots-nexus-plugin/docs/plugin/design/`。
手册：`innospots-nexus-plugin/docs/plugin/manual/README.md`。

**Pactor Page DSL 1.0（页面 YAML）规范索引：**
`skills/java/java-reference/references/modules/innospots-nexus-plugin-ui-spec/README.md`
（与 `plugin.yaml` 分离；默认路径 `ui-pages/{moduleKey}/{pageKey}.yaml`）。

## 技能用法（java:reference）

```text
java:reference → module-ownership.md + this README
java:design     → plugin/design 与贡献契约
java:develop    → 本模块或 *EntryPlugin 宿主
java:check      → mvn -pl innospots-nexus-plugin -am test
```


## 类参考

### 包 `bootstrap`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginHostStartupTask` | `class` | 内置启动任务：启用插件子系统 |
| `PluginStartupAttributes` | `class` | 插件子系统的启动属性键与辅助方法 |

### 包 `capability`

| 类 | 类型 | 说明 |
|------|------|------|
| `CapabilityKey` | `record` | Capability API 主版本的稳定逻辑身份 |
| `CapabilityManager` | `interface` | 用于选择活动 Capability Provider 的类型安全读取边界 |
| `CapabilityRegistration` | `record` | 插件启动成功后才发布的不可变 Provider 注册记录 |
| `CapabilityRegistry` | `class` | 通过写时复制向并发读者提供完整不可变快照的 Capability 注册表 |
| `CapabilityRouter` | `class` | 应用显式标签、配置默认路由和唯一 Provider 回退，不依赖注册顺序选择 Provider |
| `CapabilityType` | `record` | Capability 逻辑身份与 Java API 之间的类型安全关联 |
| `CapabilityTypeRegistry` | `class` | Capability 逻辑身份到 Java API 的映射表，不注册 Provider 实现类 |
| `ProviderRef` | `record` | 插件内 Provider 的稳定身份，由所属插件标识和插件内唯一编号组成 |
| `Tag` | `record` | 一个不可变的路由标签属性 |
| `Tags` | `class` | 不可变且按名称排序的路由标签 |

### 包 `config`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConfigDefinition` | `interface` | 一个插件允许使用的配置键不可变 schema |
| `ConfigItemDefinition` | `record` | 描述一个插件本地配置键的不可变 schema 项 |
| `ConfigSource` | `interface` | 宿主侧动态配置来源扩展点 |
| `ConfigType` | `enum` | 插件配置支持的值类型 |
| `ConfigurationManager` | `class` | 解析插件默认值、宿主配置、动态配置来源、环境变量、系统属性和运行时覆盖值 |
| `DefaultPluginConfig` | `class` | 一个插件的不可变类型化配置快照 |
| `PluginConfig` | `interface` | 限定在一个插件命名空间内的不可变已校验配置视图 |
| `SecretValue` | `class` | 可关闭的内存密文包装器，其文本表示始终为遮罩值 |

### 包 `contract`

| 类 | 类型 | 说明 |
|------|------|------|
| `CapabilityProvider` | `interface` | 运行时管理的 Capability 实现所遵循的标记和生命周期契约 |
| `CapabilityProviderContext` | `interface` | 面向一个已声明 Capability Provider 的专用插件上下文 |
| `CapabilityProviderFactory` | `interface` | 无副作用地创建一个全新的、尚未初始化的 Capability Provider |
| `Plugin` | `interface` | 用于声明插件及其插件级生命周期的唯一 classpath SPI |
| `PluginContext` | `interface` | 一次插件启动周期内向插件暴露的只读运行时服务 |

### 包 `contribution`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginContribution` | `interface` | 插件向宿主提交的一类不可变静态扩展声明 |
| `PluginContributionContext` | `record` | Contribution Handler 使用的只读插件上下文 |
| `PluginContributionDecoder` | `interface` | 将已完成结构校验的通用 YAML 字段解码为具体 Contribution |
| `PluginContributionDecoderRegistry` | `class` | 宿主显式注册的通用 Contribution Decoder 表 |
| `PluginContributionEntry` | `record` | 将 Contribution 与声明它的插件绑定，供 Handler 做全局校验 |
| `PluginContributionHandler` | `interface` | 负责一种 Contribution 全局校验和事务准备的宿主处理器 |
| `PluginContributionSnapshotter` | `interface` | 将 Contribution 转为不含类、Handler 或 Secret 的安全快照 |
| `PluginContributionSnapshotterRegistry` | `class` | 宿主显式注册的安全 Contribution 快照器表 |
| `PluginContributionType` | `record` | Contribution 的稳定类型标识，例如 console@1 |
| `PreparedPluginContribution` | `interface` | 一个已准备但尚未对外可见的 Contribution 事务句柄 |

### 包 `contribution.console`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleContributionCatalog` | `class` | Console Contribution 的活动资源目录，不保存安装事实或 PluginState |
| `ConsoleI18n` | `class` | Console 静态声明共用的本地化文本校验与防御性复制工具 |
| `ConsoleModuleDeclaration` | `record` | 一个 Console 管理模块及其页面树、菜单树 |
| `ConsolePluginContribution` | `record` | console@1 管理模块、页面和菜单的静态资源贡献 |
| `ConsolePluginContributionDecoder` | `class` | 将 YAML 的通用 map 严格解码为不可变 console@1 声明 |
| `ConsolePluginContributionHandler` | `class` | Console Contribution 的全局资源校验器和活动目录事务适配器 |
| `ConsolePluginContributionSnapshotter` | `class` | 仅保存 Console 模块、页面、菜单稳定身份的安全快照器 |
| `MenuDeclaration` | `record` | Console 菜单树节点；目录和页面入口互斥 |
| `ReservedPluginResourceCatalog` | `class` | 由 Core 安全快照提供的历史插件资源身份保留目录 |
| `UiSpecPageDeclaration` | `record` | 由 PageDsl page.id 唯一对应的页面身份声明 |

### 包 `contribution.console.ui.spec`

| 类 | 类型 | 说明 |
|------|------|------|
| `HttpRequest` | `class` | HTTP 数据源与动态 DSL 源使用的 HTTP 请求定义 |
| `LifecycleConfig` | `class` | 以动作序列表达的页面生命周期钩子 |
| `PageDsl` | `class` | Pactor Page DSL 1.0 的根文档 |
| `PageMeta` | `class` | 由 PageDsl 声明的页面标识与展示元数据 |
| `PaginationConfig` | `class` | 返回分页结果的数据源的分页绑定配置 |
| `RequiresConfig` | `class` | 由 PageDsl 声明的运行时与组件能力要求 |

### 包 `contribution.console.ui.spec.action`

| 类 | 类型 | 说明 |
|------|------|------|
| `ActionConfig` | `class` | 在运行时动作注册表中注册的一次动作调用 |
| `ActionOrList` | `record` | 单个动作或有序动作序列 |

### 包 `contribution.console.ui.spec.config`

| 类 | 类型 | 说明 |
|------|------|------|
| `PageDslConfig` | `record` | 定位与解析 Pactor 页面 DSL 文件的不可变配置 |

### 包 `contribution.console.ui.spec.datasource`

| 类 | 类型 | 说明 |
|------|------|------|
| `ComputedDataSource` | `class` | 协议预留的计算数据源；运行时语义由实现定义 |
| `DataSourceConfig` | `interface` | 命名页面数据源配置 |
| `HttpDataSource` | `class` | HTTP 后端数据源；页面需直接调用具体 HTTP 端点时使用 |
| `OptionMappingDataSource` | `class` | 具体数据源类型共享的选项映射与自动加载字段 |
| `ResourceDataSource` | `class` | 协议预留的资源数据源；运行时语义由实现定义 |
| `ServiceDataSource` | `class` | 服务注册表支持的数据源；业务页面首选，因 DSL 不直接绑定 HTTP 端点 |
| `StaticDataSource` | `class` | 文档内静态数据源，用于枚举、固定配置与演示数据 |

### 包 `contribution.console.ui.spec.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `DefaultPageDslEndpoint` | `class` | 默认 PageDslEndpoint 实现：从 classpath 加载页面 DSL 文档，并通过已配置的过滤器链处理 |
| `PageDslEndpoint` | `interface` | 加载与准备页面 DSL 文档的渲染时 API |

### 包 `contribution.console.ui.spec.filter`

| 类 | 类型 | 说明 |
|------|------|------|
| `PageDslFilter` | `interface` | 在渲染时准备阶段转换页面 DSL 文档 |
| `PageDslFilterChain` | `class` | 有序的 PageDslFilter 实例链 |
| `PageDslRenderContext` | `class` | 在 PageDslFilterChain 中传递的渲染时上下文 |
| `StateBindingPageDslFilter` | `class` | 将请求参数绑定到页面 DSL 的 state 映射 |

### 包 `contribution.console.ui.spec.jackson`

| 类 | 类型 | 说明 |
|------|------|------|
| `ActionOrListDeserializer` | `class` | 将单个动作或动作数组反序列化为 ActionOrList |
| `ActionOrListMapDeserializer` | `class` | 反序列化命名动作映射 |
| `ChildrenDeserializer` | `class` | 从 YAML 数组或单个 com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef 对象反序列化 com.innospots.nexus.core.plugin.contribution.console.ui.spec… |
| `DslNodeDeserializer` | `class` | 反序列化 DslNode 实例 |
| `DslRenderableDeserializer` | `class` | 反序列化一个可渲染 DSL 片段 |
| `DslRenderableMapDeserializer` | `class` | 反序列化命名可渲染组件映射 |
| `EventMapDeserializer` | `class` | 反序列化组件事件映射 |
| `ExpressionOrBooleanDeserializer` | `class` | 反序列化表达式字符串或布尔字面量 |

### 包 `contribution.console.ui.spec.loader`

| 类 | 类型 | 说明 |
|------|------|------|
| `ClasspathPageDslLoader` | `class` | 使用 com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig 从 classpath 加载页面 DSL 文档 |
| `PageDslLoader` | `interface` | 从后端存储加载页面 DSL 文档 |

### 包 `contribution.console.ui.spec.node`

| 类 | 类型 | 说明 |
|------|------|------|
| `Children` | `record` | 以数组或单个动态源引用声明的子可渲染节点 |
| `ComponentNode` | `class` | 以注册表 type 声明的内联组件节点 |
| `ComponentReferenceNode` | `class` | 对 com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl 中声明的命名组件的引用 |
| `DslHttpSource` | `class` | HTTP 支持的动态 DSL 源 |
| `DslNode` | `interface` | 以 type 或 component 声明的 UI 树节点 |
| `DslRenderable` | `interface` | 可渲染 DSL 片段：内联节点或动态源 |
| `DslServiceSource` | `class` | 服务支持的动态 DSL 源 |
| `DslSource` | `interface` | DslSourceRef 的动态 DSL 源定义 |
| `DslSourceRef` | `class` | 从服务或 HTTP 源加载的动态 DSL 片段 |

### 包 `contribution.console.ui.spec.parser`

| 类 | 类型 | 说明 |
|------|------|------|
| `JacksonPageDslParser` | `class` | 基于 Jackson 的 Pactor 页面 DSL 严格 YAML 解析器 |
| `PageDslParser` | `interface` | 解析与序列化 Pactor 页面 DSL 文档 |

### 包 `contribution.console.ui.spec.permission`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionConfig` | `class` | 权限声明，支持单个代码、任一匹配代码数组或详细对象形式 |
| `PermissionConfigDeserializer` | `class` | 从字符串、数组或对象形式反序列化 PermissionConfig |
| `PermissionDenied` | `enum` | 未授予访问权限时的拒绝行为 |

### 包 `contribution.console.ui.spec.validation`

| 类 | 类型 | 说明 |
|------|------|------|
| `PageDslValidator` | `class` | 校验页面 DSL 文档中的结构与交叉引用规则 |

### 包 `declaration`

| 类 | 类型 | 说明 |
|------|------|------|
| `CapabilityContribution` | `record` | 将一个 Capability API、Provider 身份、路由标签和配置绑定到插件工厂 |
| `CapabilityRequirement` | `record` | 声明插件所需的 Capability 及其路由标签 |
| `JacksonPluginManifestParser` | `class` | 基于 Jackson YAML 的严格 DSL 解析器，限制输入大小、深度和 YAML 扩展语法 |
| `PluginDefinition` | `record` | 已通过静态校验的不可变插件运行时定义 |
| `PluginManifest` | `record` | YAML DSL 的纯数据模型 |
| `PluginManifestParser` | `interface` | 将 UTF-8 YAML 文档解析为严格的 PluginManifest |
| `PluginSource` | `record` | 描述插件定义的声明来源和发现时间 |

### 包 `dependency`

| 类 | 类型 | 说明 |
|------|------|------|
| `DependencyResolution` | `record` | 一条 Capability 依赖声明的不可变诊断快照 |
| `DependencyResolver` | `class` | 解析已声明且当前可用的 Capability 依赖，不绑定到具体 Provider 实例 |

### 包 `discovery`

| 类 | 类型 | 说明 |
|------|------|------|
| `ClasspathPluginDiscovery` | `class` | 通过 Java SPI 和全部 plugin.yaml 资源发现插件，并统一编译为 Plugin |
| `DiscoveredPlugin` | `record` | 一个已发现的插件实例及其缓存的不可变定义 |
| `ManifestPlugin` | `class` | 将 YAML 编译定义适配为唯一 Plugin SPI，避免创建第二套运行时 |
| `PluginCatalog` | `class` | 一个插件运行时使用的不可变发现快照 |
| `PluginDefinitionCompiler` | `class` | 将 YAML 纯数据模型编译为无副作用的运行时 PluginDefinition |
| `PluginDiscoveryReport` | `record` | 表示发现阶段成功目录与被拒绝定义的分离结果 |
| `RejectedPluginDefinition` | `record` | 单个插件定义被拒绝时保留的来源和安全诊断 |

### 包 `event`

| 类 | 类型 | 说明 |
|------|------|------|
| `DefaultPluginEventBus` | `class` | 实例本地同步事件总线，隔离观察者失败 |
| `PluginEvent` | `interface` | 进程内不可变插件运行观测事件的标记契约 |
| `PluginEventBus` | `interface` | 限定在一个插件管理器内的同步尽力而为事件通道 |
| `PluginFailedEvent` | `record` | 不包含异常对象或配置值的插件失败观测事件 |
| `PluginStartedEvent` | `record` | 所有 Capability 对外可见后发布的插件激活观测事件 |
| `PluginStoppedEvent` | `record` | 资源释放后发布的插件停止观测事件 |
| `Subscription` | `interface` | 可幂等取消的事件订阅句柄 |

### 包 `installation.bootstrap`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginHostBootstrap` | `class` | 应用宿主启用插件子系统的唯一入口 |
| `PluginHostBootstrapRequest` | `record` | 应用宿主启用插件子系统所需的全部依赖 |

### 包 `installation.config`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginInstallationConfig` | `record` | 插件安装策略配置；默认开启首次发现自动安装 |

### 包 `installation.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginInstallationDao` | `interface` | 只访问 nx_plugin_installation 单表的 DAO |

### 包 `installation.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginInstallationEntity` | `class` | Core 持有的全局插件安装事实表映射，不包含租户或工作区字段 |

### 包 `installation.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginPresence` | `enum` | 插件定义在当前有效目录中的存在性 |
| `PluginSourceType` | `enum` | 插件定义来源类型 |

### 包 `installation.domain.model`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginDefinitionSnapshot` | `record` | 可持久化的插件静态摘要，不包含 Factory、Class、Handler、配置值或 Secret |
| `PluginDefinitionSnapshotMapper` | `class` | 在运行时定义与持久化摘要之间执行显式、安全映射 |
| `PluginInstallation` | `record` | 插件安装事实与管理员意图的不可变领域模型 |
| `PluginManagementView` | `record` | 安装事实、管理员意图与当前 JVM 运行事实的聚合只读视图 |

### 包 `installation.repository`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginInstallationRepository` | `class` | 安装表领域仓储，维护登记、对账、MISSING 和管理员意图 |

### 包 `installation.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginInstallationManager` | `class` | Core 插件安装管理器 |
| `PluginRuntimeFactory` | `class` | Core 运行时工厂，集中组装唯一的 PluginManager，不访问数据库或 Console |

### 包 `lifecycle`

| 类 | 类型 | 说明 |
|------|------|------|
| `ManagedPlugin` | `class` | 持有一个插件实例、Provider、上下文、资源以及原子生命周期转换 |
| `PluginAvailability` | `class` | Capability 与 Contribution 共用的原子可用性门控 |
| `PluginAvailabilityIndex` | `class` | 将插件标识映射到运行时可用性门控，供 Capability 查询过滤未激活 Provider |
| `PluginRuntimeInfo` | `record` | 不可变且已脱敏的运行快照，不保留运行时对象或配置值 |
| `PluginState` | `enum` | 对外暴露的插件粗粒度生命周期状态 |

### 包 `resource`

| 类 | 类型 | 说明 |
|------|------|------|
| `DefaultResourceScope` | `class` | 线程安全的资源栈，按注册逆序执行每个释放器且每个释放器最多执行一次 |
| `ResourceRegistration` | `interface` | 一个资源释放器的幂等注册句柄 |
| `ResourceScope` | `interface` | 每次启动周期的资源所有权边界，按注册逆序释放资源 |

### 包 `runtime`

| 类 | 类型 | 说明 |
|------|------|------|
| `DefaultPluginManager` | `class` | 轻量级依赖感知插件运行时，使用一把串行生命周期锁，不持有全局单例状态 |
| `PluginManager` | `interface` | 宿主侧插件运行时入口，负责依赖感知生命周期、诊断和 Capability 查询 |
| `PluginRuntimeConfig` | `record` | 一个独立插件管理器实例使用的不可变宿主配置 |

### 包 `status`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginStatusCode` | `enum` | 轻量级插件运行时产生的状态码 |


## 包参考

| 领域 | 文件 |
|------|------|
| 运行时与生命周期 | [runtime-lifecycle-overview.md](references/runtime-lifecycle-overview.md) |
| 发现与安装 | [discovery-installation-overview.md](references/discovery-installation-overview.md) |
| 贡献与 console@1 | [contribution-console-overview.md](references/contribution-console-overview.md) |
| Page DSL | [page-dsl-overview.md](references/page-dsl-overview.md) |
| Page DSL YAML 规范 | [innospots-nexus-plugin-ui-spec](../innospots-nexus-plugin-ui-spec/README.md) |

## 相关模块

| 主题 | 模块 |
|-------|--------|
| 共享基础 | `innospots-nexus-base` |
| 平台基础设施 | `innospots-nexus-core` |
| 目录索引与管理 API | `innospots-nexus-console` |
| 设计文档（非 API 索引） | `innospots-nexus-plugin/docs/plugin/` |

### 全部包（`references/`）

| 包 | 参考 |
|------|------|
| `bootstrap` | [`references/bootstrap.md`](references/bootstrap.md) |
| `capability` | [`references/capability.md`](references/capability.md) |
| `config` | [`references/config.md`](references/config.md) |
| `contract` | [`references/contract.md`](references/contract.md) |
| `contribution` | [`references/contribution.md`](references/contribution.md) |
| `contribution.console` | [`references/contribution-console.md`](references/contribution-console.md) |
| `contribution.console.ui.spec` | [`references/contribution-console-ui-spec.md`](references/contribution-console-ui-spec.md) |
| `contribution.console.ui.spec.action` | [`references/contribution-console-ui-spec-action.md`](references/contribution-console-ui-spec-action.md) |
| `contribution.console.ui.spec.config` | [`references/contribution-console-ui-spec-config.md`](references/contribution-console-ui-spec-config.md) |
| `contribution.console.ui.spec.datasource` | [`references/contribution-console-ui-spec-datasource.md`](references/contribution-console-ui-spec-datasource.md) |
| `contribution.console.ui.spec.endpoint` | [`references/contribution-console-ui-spec-endpoint.md`](references/contribution-console-ui-spec-endpoint.md) |
| `contribution.console.ui.spec.filter` | [`references/contribution-console-ui-spec-filter.md`](references/contribution-console-ui-spec-filter.md) |
| `contribution.console.ui.spec.jackson` | [`references/contribution-console-ui-spec-jackson.md`](references/contribution-console-ui-spec-jackson.md) |
| `contribution.console.ui.spec.loader` | [`references/contribution-console-ui-spec-loader.md`](references/contribution-console-ui-spec-loader.md) |
| `contribution.console.ui.spec.node` | [`references/contribution-console-ui-spec-node.md`](references/contribution-console-ui-spec-node.md) |
| `contribution.console.ui.spec.parser` | [`references/contribution-console-ui-spec-parser.md`](references/contribution-console-ui-spec-parser.md) |
| `contribution.console.ui.spec.permission` | [`references/contribution-console-ui-spec-permission.md`](references/contribution-console-ui-spec-permission.md) |
| `contribution.console.ui.spec.validation` | [`references/contribution-console-ui-spec-validation.md`](references/contribution-console-ui-spec-validation.md) |
| `declaration` | [`references/declaration.md`](references/declaration.md) |
| `dependency` | [`references/dependency.md`](references/dependency.md) |
| `discovery` | [`references/discovery.md`](references/discovery.md) |
| `event` | [`references/event.md`](references/event.md) |
| `installation.bootstrap` | [`references/installation-bootstrap.md`](references/installation-bootstrap.md) |
| `installation.config` | [`references/installation-config.md`](references/installation-config.md) |
| `installation.dao` | [`references/installation-dao.md`](references/installation-dao.md) |
| `installation.domain.entity` | [`references/installation-domain-entity.md`](references/installation-domain-entity.md) |
| `installation.domain.enums` | [`references/installation-domain-enums.md`](references/installation-domain-enums.md) |
| `installation.domain.model` | [`references/installation-domain-model.md`](references/installation-domain-model.md) |
| `installation.repository` | [`references/installation-repository.md`](references/installation-repository.md) |
| `installation.service` | [`references/installation-service.md`](references/installation-service.md) |
| `lifecycle` | [`references/lifecycle.md`](references/lifecycle.md) |
| `resource` | [`references/resource.md`](references/resource.md) |
| `runtime` | [`references/runtime.md`](references/runtime.md) |
| `status` | [`references/status.md`](references/status.md) |
