# innospots-nexus-plugin — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：0.1.0-SNAPSHOT

## 模块概览

基于 `innospots-nexus-core` 的 classpath 插件运行时：发现与声明编译、安装与生命周期、
Contribution 解码/校验/快照，以及 `console@2` 贡献契约。

**能力一览：**

| 能力 | 说明 |
|------|------|
| **Plugin SPI** | `Plugin`、`PluginDefinition`、`PluginContext` |
| **发现** | `ClasspathPluginDiscovery`、`PluginCatalog`、`PluginDefinitionCompiler` |
| **安装** | `PluginInstallationManager`、`PluginInstallationEntity`、状态机 |
| **生命周期** | `PluginState`、`PluginRuntimeInfo`、事件总线 |
| **贡献** | `PluginContribution*` 注册表、decoder/snapshotter |
| **Console 贡献** | `ConsoleContributions`、`ConsoleContributeNode`、`ConsolePluginContributionHandler` |
| **资源作用域** | `ResourceScope`、`ResourceRegistration` |

**不包含：** Spring/Quarkus 自动配置、控制台 catalog 表持久化（`nx_console_catalog_resource` 归属 **console**）、
管理 REST 端点、用户/角色业务。

## 模块边界

| 归属 plugin | 归属 console | 归属 core |
|-------------|--------------|-----------|
| 发现、安装、生命周期、Contribution 处理 | catalog 索引持久化与同步 REST | 持久化基类、审计、`OwnershipEntity` |
| `console@2` 声明、Handler、贡献快照 | `ConsoleCatalogSyncService` | Quartz、server、watcher |
| — | 权限运行时、导航组装 | `MetaResourceService` |

Java 包名：`com.innospots.nexus.core.plugin.*`（兼容既有 import）。Maven artifact：`innospots-nexus-plugin`。

## 扩展指南

| 需求 | 使用 plugin 中的 | 不要放在 plugin 中 |
|------|------------------|---------------------|
| 新 classpath 插件 | 实现 `Plugin` + `PluginDefinition` | HTTP 端点、租户业务 |
| 控制台 entry 贡献 | `ConsoleContributions.entryPlugin(...)` + `ConsoleContributeNode` | catalog 表 DAO（console） |
| 安装/启用/禁用 | `PluginInstallationManager`、`PluginRuntimeFactory` | console 管理 REST（委托 manager） |
| Contribution 扩展 | `PluginContributionDecoder`、Handler、Snapshotter 注册表；`PluginContributionInventory` 统计扩展规模 | 业务域服务实现 |
| 宿主启动 | `PluginHostBootstrap`、`NexusStartupTask` 注册 | Spring `@Configuration` |

深度设计见模块内文档：`innospots-nexus-plugin/docs/plugin/design/`。
手册：`innospots-nexus-plugin/docs/plugin/manual/README.md`。

配置接入、Entry 组装与 platform `ModuleDescriptors` / `EntryPluginDescriptor` 的
实现规范见
[`java:develop` → plugin-entry-and-configuration.md](../../../../java-develop/references/plugin-entry-and-configuration.md)。
内置控制台 entry 与稳定 `entryPageKey` 约定见
[console-entry-and-configuration.md](../../console-entry-and-configuration.md)。

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

### 包 `config`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConfigDefinition` | `interface` | 一个插件允许使用的配置键不可变 schema |
| `ConfigItemDefinition` | `record` | 描述一个插件本地配置键的不可变 schema 项 |
| `ConfigSource` | `interface` | 宿主侧动态配置来源扩展点 |
| `ConfigType` | `enum` | 插件配置支持的值类型 |
| `ConfigurationManager` | `class` | 解析插件默认值、宿主配置、动态配置来源、环境变量、系统属性和运行时覆盖值 |
| `PluginConfig` | `interface` | 限定在一个插件命名空间内的不可变已校验配置视图 |
| `SecretValue` | `class` | 可关闭的内存密文包装器，其文本表示始终为遮罩值 |

### 包 `contract`

| 类 | 类型 | 说明 |
|------|------|------|
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
| `PluginContributionInventories` | `class` | 汇总 Contribution 的模块数与组件数，供发现与启动日志使用 |
| `PluginContributionInventory` | `interface` | 各 Contribution 声明自身扩展规模的只读契约 |
| `PluginContributionSnapshotter` | `interface` | 将 Contribution 转为不含类、Handler 或 Secret 的安全快照 |
| `PluginContributionSnapshotterRegistry` | `class` | 宿主显式注册的安全 Contribution 快照器表 |
| `PluginContributionType` | `record` | Contribution 的稳定类型标识，例如 console@2 |
| `PreparedPluginContribution` | `interface` | 一个已准备但尚未对外可见的 Contribution 事务句柄 |

### 包 `contribution.console`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleContributeNode` | `record` | `console@2` 递归 MODULE / MENU 节点与静态工厂 |
| `ConsoleContributionCatalog` | `class` | 按插件保存已提交的活动 Contribution |
| `ConsoleContributions` | `class` | `entryPlugin(...)` 与发现标签组装门面 |
| `ConsolePermissionGroupDeclaration` | `record` | MODULE 页面访问/操作分组声明 |
| `ConsolePluginContribution` | `record` | `console@2` MODULE/MENU 静态资源贡献 |
| `ConsolePluginContributionDecoder` | `class` | 将 YAML 通用 map 严格解码为 `console@2` |
| `ConsolePluginContributionHandler` | `class` | 全局资源校验和活动目录事务适配器 |
| `ConsolePluginContributionSnapshotter` | `class` | 仅保存 MODULE/MENU 稳定身份的安全快照器 |
| `ReservedPluginResourceCatalog` | `class` | 历史插件资源身份保留目录 |

### 包 `declaration`

| 类 | 类型 | 说明 |
|------|------|------|
| `JacksonPluginManifestParser` | `class` | 基于 Jackson YAML 的严格 DSL 解析器，限制输入大小、深度和 YAML 扩展语法 |
| `PluginDefinition` | `record` | 已通过静态校验的不可变插件运行时定义 |
| `PluginManifest` | `record` | YAML DSL 的纯数据模型 |
| `PluginManifestParser` | `interface` | 将 UTF-8 YAML 文档解析为严格的 PluginManifest |
| `ProviderRef` | `record` | Contribution 等扩展条目的稳定身份 |
| `PluginSource` | `record` | 描述插件定义的声明来源和发现时间 |
| `Tag` | `record` | 单个不可变标签属性 |
| `Tags` | `class` | 不可变且按名称排序的标签集合 |

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
| `PluginStartedEvent` | `record` | Contribution 提交与插件激活完成后发布的观测事件 |
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
| `ManagedPlugin` | `class` | 持有一个插件实例、上下文、Contribution 与资源，并执行原子生命周期转换 |
| `PluginAvailability` | `class` | Contribution 提交后控制其是否对外可见的原子门控 |
| `PluginAvailabilityIndex` | `class` | 将插件标识映射到插件可用性门控 |
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
| `DefaultPluginManager` | `class` | 轻量级插件运行时，使用一把串行生命周期锁，不持有全局单例状态 |
| `PluginManager` | `interface` | 宿主侧插件运行时入口，负责批量/单项启停、诊断和关闭 |
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
| 贡献与 console@2 | [contribution-console-overview.md](references/contribution-console-overview.md) |
| Entry 组装 | [contribution-console-entry-assembly.md](references/contribution-console-entry-assembly.md) |

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
| `config` | [`references/config.md`](references/config.md) |
| `contract` | [`references/contract.md`](references/contract.md) |
| `contribution` | [`references/contribution.md`](references/contribution.md) |
| `contribution.console` | [`references/contribution-console.md`](references/contribution-console.md) |
| `declaration` | [`references/declaration.md`](references/declaration.md) |
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
