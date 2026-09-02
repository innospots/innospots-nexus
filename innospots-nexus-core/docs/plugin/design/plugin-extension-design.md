# Nexus 插件体系总览

## 1. 文档定位

本文是 Nexus 插件体系的总览和规范索引，只定义稳定概念、模块边界、核心决策和各子规范的关系。
具体字段、生命周期、安装存储和 YAML 语法由独立文档定义，避免在一份文档中混合运行时、持久化、
Console 资源和跨系统协议。

本设计最终只保留 `Plugin`，不再保留独立的 Extension 身份、SPI、状态机、Registry 或安装表。

## 2. 规范文档

| 文档 | 责任 | 规范性 |
|------|------|--------|
| [plugin-extension-design.md](plugin-extension-design.md) | 总体架构、术语、边界和决策 | 规范 |
| [plugin-runtime-design.md](plugin-runtime-design.md) | Plugin、Capability、Contribution、生命周期和并发 | 规范 |
| [plugin-installation-design.md](plugin-installation-design.md) | Core 安装存储、对账、启停意图和 MISSING | 规范 |
| [plugin-console-contribution-design.md](plugin-console-contribution-design.md) | Console 模块、页面、菜单和权限资源 | 规范 |
| [plugin-dsl-spec.md](plugin-dsl-spec.md) | 跨系统 Plugin YAML DSL v1 | 公开协议规范 |
| [plugin-dsl-v1.schema.json](plugin-dsl-v1.schema.json) | DSL 的机器可读结构校验 | 公开协议规范 |
| [plugin-v1-implementation-plan.md](plugin-v1-implementation-plan.md) | Core Minimal V1 的实施范围和阶段 | 实施依据 |

如总览与子规范冲突，以职责更具体的子规范为准。DSL 文档定义完整语言能力，宿主是否实现某个可选
`bind.kind` 或 `exposure.kind` 由宿主能力档案决定。

历史设计保存在 [archive](archive/README.md)，只用于追溯，不作为实施依据。

## 3. 核心模型

Nexus 对插件开发者只提供一个顶层概念：`Plugin`。

```text
Plugin
├── identity                pluginId + version
├── capabilities[]          可调用、可依赖、可路由的服务能力
│   ├── CapabilityKey       type + majorVersion
│   ├── providerId          插件内全局唯一的实现身份
│   ├── tags                运行时路由条件
│   ├── bind                实现绑定
│   └── exposures[]         可选对外调用入口
└── contributions[]         宿主解释的声明型资源，V1 只有 console@1
```

Java 与 YAML 是两种声明表面：

```text
Java Plugin.definition() ──┐
                           ├── ResolvedPluginDefinition ── PluginCatalog
YAML PluginManifest ───────┘
          │
          └── PluginDefinitionSnapshot ── nx_plugin_installation
```

三个模型不得混用：

| 模型 | 用途 | 可以包含 | 不得包含 |
|------|------|----------|----------|
| `PluginManifest` | YAML 解析和跨系统交换 | 纯数据声明 | Class、Factory、实例配置和 Secret |
| `ResolvedPluginDefinition` | Catalog 和 Runtime | 已解析类型、Binding、Factory | Provider 实例和运行状态 |
| `PluginDefinitionSnapshot` | 数据库存储和管理展示 | 静态摘要和资源身份 | Class、Factory、Handler、配置值和 Secret |

## 4. 稳定身份

### 4.1 Plugin

`pluginId` 是安装、配置、启停、诊断和资源归属的唯一插件身份。新 ID 使用小写反向域名：

```text
com.innospots.erp
com.example.message-wecom
```

`pluginId` 不包含版本、环境、租户、workspace 或部署实例。

### 4.2 Capability

Capability API 的稳定身份是：

```text
CapabilityKey = type + majorVersion
```

例如：

```text
message.sender@1
```

`CapabilityTypeRegistry` 显式建立 `CapabilityKey -> Java API`，不得根据 YAML 字符串猜测、拼接或扫描
Java 接口。

### 4.3 Provider

`providerId` 在一个插件内全局唯一。完整身份是：

```text
ProviderRef = pluginId/providerId
```

`providerId` 用于配置、管理、日志和诊断；Tags 用于运行时路由；Java 类名和 YAML 顺序都不承担稳定
身份，也不能用于消除路由歧义。

Provider 私有配置使用：

```text
plugins.<pluginId>.providers.<providerId>.<localKey>
```

插件内全局唯一的 providerId 保证该命名空间不会在不同 Capability 之间冲突。

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

提供异常、状态码、国际化对象、UiSpec 等无中间件基础契约，不包含插件发现、安装和运行状态。

### 5.2 Core

Core 拥有业务中立插件基础设施：

- Plugin SPI、Manifest 编译、Catalog 和定义校验；
- Capability API、Provider、Tags 路由、配置和依赖；
- 通用 Contribution 生命周期契约；
- PluginManager、PluginState、ResourceScope 和统一可用性门控；
- PluginInstallationManager、安装持久化、对账和管理员启停意图。

Core 不解释菜单、页面、权限和 Jakarta REST 语义，也不绑定 Spring Boot 自动配置。

### 5.3 Console

Console 拥有管理平台扩展面：

- `ConsolePluginContribution`；
- 模块、页面树和菜单树声明；
- Console Contribution 校验、暂存和活动资源目录；
- 插件管理 Endpoint 和组合视图。

Console 不持有插件安装 Entity、DAO、Repository 或第二套状态机，不发现和实例化 Plugin。

### 5.4 Kernel 和 Platform

Kernel、Platform 可以提供 Plugin 和 Console Contribution。Kernel 权限模块从活动的
`ConsoleContributionCatalog` 同步资源。两个模块保持平行，不互相依赖，也不实现插件内核。

## 6. 安装事实与运行事实

插件管理必须区分：

| 事实 | 所有者 | 含义 |
|------|--------|------|
| `presence` | Core 安装仓库 | 当前 Catalog 是否发现插件 |
| `installed` | Core 安装仓库 | 是否完成安装确认 |
| `desiredEnabled` | Core 安装仓库 | 管理员是否期望启用 |
| `runtimeState` | PluginManager | 当前 JVM 的实际运行状态 |

`DISABLED`、`REGISTERED`、`MISSING_ENABLED` 等是管理视图的派生状态，不进入 PluginState。

默认配置为：

```properties
nexus.plugin.auto-install=false
```

该配置只影响第一次发现的新 pluginId，不覆盖已有安装记录和管理员意图。

## 7. Runtime 状态

PluginState 只表示当前 JVM 生命周期：

```text
DISCOVERED → DESCRIBED
DESCRIBED  → WAITING | STARTING | FAILED
WAITING    → STARTING | FAILED
STARTING   → ACTIVE | FAILED
ACTIVE     → STOPPING
STOPPING   → STOPPED | FAILED
STOPPED    → STARTING
```

MISSING、REGISTERED 和 DISABLED 都不是 PluginState。

PluginManager 是运行状态唯一事实源；PluginInstallationManager 是安装事实和管理命令唯一入口。

## 8. Capability 与 Contribution

Capability 是可调用服务：

```text
CapabilityType + Tags
        ↓
CapabilityManager
        ↓
唯一 ACTIVE CapabilityProvider
```

Contribution 是由宿主模块聚合、校验和发布的声明型资源。页面和菜单不能包装成普通 CapabilityProvider，
否则会把累加型资源误建模成可替换服务。

V1 冻结的 Contribution 类型只有：

```text
console@1
```

Core 只理解 `PluginContribution` 和 Handler 生命周期，不解释 Console 字段。

## 9. 完整 DSL 与最小实现

Plugin DSL v1 是跨系统稳定协议，完整定义：

- `java`、`http`、`process`、`mcp`、`contract` bind；
- `http`、`command`、`mcp` exposure；
- `console@1` Contribution；
- 配置 schema、requirements、Tags 和严格校验。

DSL 合法不代表每个宿主都实现所有可选能力。宿主必须发布能力档案，并对未实现的合法类型返回
`UNSUPPORTED_BIND_KIND`、`UNSUPPORTED_EXPOSURE_KIND` 或 `UNSUPPORTED_CONTRIBUTION_TYPE`。

Core Minimal V1 只实现：

- Java Plugin SPI；
- YAML `bind.kind=java` 和显式 `class`；
- CapabilityTypeRegistry、Provider Factory 和 Tags 路由；
- `console@1`；
- Core 安装持久化和 PluginManager 生命周期。

远程 bind、Capability exposures、独立 ClassLoader、动态 JAR 安装和热重载不进入最小 V1。

## 10. 非目标与信任边界

最小 V1 不引入：

- PF4J、OSGi 或 Spring Boot 自动配置；
- 注解扫描、包扫描或 Capability 实现 SPI；
- 独立插件 ClassLoader 和依赖隔离；
- 运行时下载、替换和卸载 JAR；
- 安全沙箱、插件市场和供应链签名；
- 完整热卸载和调用引用计数。

因此 V1 是受信任应用 classpath 上的模块化插件框架，不是运行不可信第三方代码的沙箱。DSL 中的 Java 类
和 process 命令只能来自受信任安装包；生产宿主应默认关闭自动安装和 process bind。

## 11. 统一实施顺序

1. 统一 Plugin 身份、ProviderRef 和三种定义模型；
2. 将安装存储和协调迁移到 Core；
3. 将 Console Extension 转换为 `console@1` Contribution；
4. 迁移 Kernel 权限资源的 `extensionKey` 为 `ownerPluginId`；
5. 删除 `core.extension`、`console.extension` 和旧安装表；
6. 实现 YAML `java` bind；
7. 按真实需求逐种实现远程 Binding Adapter 和 exposures。

任何阶段都不得让 ExtensionRegistry 与 PluginManager 同时管理同一个插件实例。

## 12. 总体验收标准

- 一个 pluginId、一个 Plugin SPI、一个运行状态机；
- 安装持久化全部位于 Core；
- PluginManager 不读写数据库；
- providerId 在插件内全局唯一；
- PluginDefinitionSnapshot 不包含运行时对象；
- Console 不持有安装数据和生命周期状态；
- Capability 与 Contribution 参加同一启动和停止事务；
- ACTIVE 前外部看不到 Provider、exposure、页面或菜单；
- DSL 文档与 JSON Schema 一致；
- Minimal V1 对未实现 DSL 能力返回明确的不支持错误；
- 最终生产代码不再引用 Extension SPI、ExtensionRegistry 或 extensionKey 安装身份。
