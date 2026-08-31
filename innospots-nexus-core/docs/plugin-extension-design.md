# Nexus 统一插件扩展体系设计

## 1. 文档定位

本文定义 Nexus 唯一的 Java 插件扩展体系，统一原 Console Extension 与 Core Plugin 的身份、发现、
配置、依赖、启停、生命周期和诊断能力，同时保留两类不同的扩展语义：

- **Capability**：可调用、可依赖、可按 Tags 路由的运行时服务能力；
- **Contribution**：由宿主模块解释、校验和发布的声明型贡献，例如 Console 模块、菜单、页面和 Endpoint。

本文是后续插件工程变更的现行设计依据。原设计已移入 [`archive/`](archive/README.md)，仅用于历史追溯。
本次变更只确定目标模型、职责边界、生命周期事务和迁移原则，不修改 Java、数据库或 Maven 配置。

## 2. 核心结论

Nexus 对插件开发者只提供一个顶层概念：`Plugin`。

```text
一个 Plugin SPI
        +
一个 PluginDefinition
        +
一个 PluginManager Runtime
        +
Capability + Contribution 两类扩展面
```

必须遵守以下决策：

1. 每个插件只通过 `ServiceLoader<Plugin>` 发现，不再使用 `ConsoleExtensionProvider` SPI；
2. `pluginId` 是安装、配置、启停、诊断和持久化关联的唯一插件身份；
3. 插件版本只在 `PluginDefinition` 声明一次；
4. Core `PluginManager` 是实际运行状态的唯一事实源；
5. Console 只保存管理员启停意图和历史安装快照，不维护第二套运行状态机；
6. Capability 和 Console Contribution 参加同一个插件启动与停止事务；
7. Core 不理解菜单、页面、权限和 Jakarta REST 语义，具体 Contribution 由所属模块处理；
8. 插件的所有资源通过统一可用性门控发布，外部不能观察到半激活插件。

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
- 保留 Console 模块、页面、菜单、UiSpec、Endpoint 和权限资源模型；
- 允许插件只有 Capability、只有 Contribution，或同时提供两者；
- 允许未来增加其他 Contribution，而不持续膨胀 PluginDefinition；
- 保证单插件激活和停止的原子可见性与失败回滚；
- 保持 Core 和 Console 业务中立，Kernel 与 Platform 平行且互不依赖；
- 提供可分阶段执行的兼容迁移路径。

### 4.2 非目标

本设计不引入：

- PF4J、OSGi、Spring Boot 自动配置或 classpath 反射扫描；
- YAML、JSON 或 XML 插件描述文件；
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

Core 不依赖 Console，不解释 Console Contribution，也不注册菜单、页面、权限或 Endpoint。

### 5.3 Console

Console 拥有业务中立管理平台扩展面：

- `ConsolePluginContribution` 及模块、页面、菜单、Endpoint 声明；
- Console Contribution 全局校验和激活处理器；
- 页面路径、UiSpec、页面 URL 权限和 Endpoint 适配边界；
- 插件安装快照、管理员启停意图和 MISSING 对账；
- PluginManager 的管理查询与命令适配。

Console 不重新发现或实例化 Plugin，不解析 Capability 依赖，也不维护第二套真实运行状态。

### 5.4 Kernel、Platform 与应用适配器

Kernel 和 Platform 可以提供实现 `Plugin` 的业务插件，需要管理页面时声明 Console Contribution，
但不得互相依赖或各自实现插件内核。最终应用负责选择 ClassLoader、发现 Catalog、对账启停意图、
注册 ContributionHandler，并装配 REST、UiSpec 和权限适配器。

## 6. 总体架构

```text
Application Classpath
        │
        └── META-INF/services/...Plugin
                        │
                        ▼
               ServiceLoader<Plugin>
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
├── capability contributions
└── plugin contributions
    └── ConsolePluginContribution
        ├── modules
        ├── page trees
        ├── menu trees
        └── endpoint types
```

## 7. 唯一 SPI 与发现

每个插件 JAR 只需要：

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
```

不再需要 ConsoleExtensionProvider SPI 或各 Capability API 的 SPI。发现流程为：

1. 枚举 Plugin Provider 类型并记录来源；
2. 实例化 Plugin；
3. 每个 Plugin 只调用一次无副作用的 `definition()`；
4. 对 PluginDefinition 做防御性快照；
5. 校验 pluginId、API 版本、Capability 类型和 Contribution 类型；
6. 按 pluginId 形成稳定排序的不可变 PluginCatalog；
7. 在任何 initialize、Factory 或 Handler prepare 前完成全局预检。

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

## 9. Capability 与 Contribution 的区别

Capability 继续通过以下方式使用：

```text
CapabilityType + Tags
        ↓
CapabilityManager
        ↓
唯一 ACTIVE CapabilityProvider
```

它是可调用服务，支持多个实现、路由和依赖。Contribution 是需要聚合全部活动插件的声明资源，具有宿主
特定的冲突、持久化和撤出规则。页面、菜单和 Endpoint 不得包装成普通 CapabilityProvider，否则无法在
插件启动前完成全局冲突校验，也会错误地把累加型资源当成可替换服务。

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

V1 的 `console@1` 是 required Contribution。

## 11. ConsolePluginContribution

```java
public record ConsolePluginContribution(
        List<ConsoleModuleDeclaration> modules,
        Collection<Class<?>> endpointTypes
) implements PluginContribution {
}
```

它不再声明 pluginId、version、displayName 或 description，这些信息来自所属 PluginDefinition。没有管理
页面的插件可不声明它；只有 Console Contribution、没有 Capability 的插件仍然合法。

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

页面树继续表达页面领域归属，菜单树表达导航关系，菜单通过 pageKey 引用同模块页面。页面无需出现在
菜单中；一个页面至多被一个静态菜单节点引用；带必填路径变量的页面不能作为静态菜单入口；pagePath
只参与路径匹配；UiSpec 继续通过 `moduleKey + pageKey` 定位。

endpointTypes 只允许标准 Jakarta REST Endpoint，不携带角色、权限码或授权策略。运行时不支持安全动态
卸载时，管理 API 必须明确返回“重启后生效”，不得伪装成已热停用。

## 12. Console 全局校验

Console Handler 在任何插件 initialize 前校验：

- moduleKey、资源 ID 及 ownerPluginId 全局唯一；
- 同模块 pageKey、menuKey 唯一；
- 页面树无循环，每页最多一个父页面；
- pagePath 合法且不存在静态/变量模板歧义；
- 菜单字段组合合法且没有悬空 pageKey；
- UiSpec 可由 moduleKey + pageKey 唯一定位，pageInfo.pageId 一致；
- Endpoint 可被目标 REST 适配器注册；
- 页面引用 URL 可规范化且归属明确；
- 当前声明不冒用 MISSING 插件保留的资源身份。

禁用插件仍参加身份和资源所有权冲突校验，避免启用顺序改变资源归属。运行时装配、外部连接、UiSpec
动态加载和 Endpoint 实际注册推迟到启动阶段。

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

第 9 步是外部可见性的线性化点。Capability 查询、菜单、页面、Endpoint 和权限资源都必须检查同一份
PluginAvailability 快照。门控切换前，内部 staged 内容不得被外部读取或调用。

### 13.4 回滚与停止

启动失败时保持 availability 不可用，逆序撤销 staged 资源、销毁 Provider、调用 plugin.stop、关闭
PreparedPluginContribution 和 ResourceScope，清除强引用，最后记录 FAILED、phase 和根因。清理失败
作为 suppressed cause 或诊断保留，不能覆盖首个失败。

停止时先 `ACTIVE → STOPPING` 并关闭 availability，拒绝新查询和请求；随后撤出活动索引，逆序销毁
Provider、停止 Plugin、关闭 Contribution 与 ResourceScope，清除引用并进入 STOPPED。V1 不提供完整
调用引用计数，已取得 Provider 或已进入 Endpoint 的调用可能与停止并发。

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

配置统一使用：

```text
nexus.plugins.<pluginId>.<localKey>
```

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

插件开发者只实现一个 Plugin，并通过 PluginDefinition Builder 同时 `.provide(...)` Capability 和
`.contribute(...)` ConsolePluginContribution。二者自动归属于同一 pluginId、version 和生命周期。

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
- 页面、菜单、UiSpec、Endpoint 和权限规则保持；
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
| 进程隔离 | 需要运行不完全可信第三方插件 |

这些能力必须分别补充设计，不能通过随意增加 PluginDefinition 字段或 Handler 回调解决。

## 21. 验收标准

- 只有一个 Plugin SPI、pluginId、version 和实际状态机；
- PluginManager 是真实运行状态唯一事实源；
- Console 只保存启停意图和安装历史；
- Capability 与 Contribution 语义清晰分离；
- Console Contribution 不反向污染 Core；
- Capability、页面、菜单和 Endpoint 共享同一可用性门控；
- 启动失败不留下部分活动资源，停止先拒绝新请求再释放资源；
- DISABLED、FAILED、STOPPED 和 MISSING 含义无歧义；
- 迁移覆盖身份、配置、持久化、资源所有权和 SPI；
- Kernel 与 Platform 保持平行；
- 未引入当前需求不需要的插件目录、ClassLoader 隔离或热重载。

## 22. 最终结论

Nexus 采用“一个 Plugin、多个扩展面”的统一模型：

```text
Plugin
├── Capability：可调用、可依赖、可路由
└── Contribution：可声明、可聚合、由宿主解释
```

Console 页面扩展不再是第二类插件，而是 Plugin 的 `console@1` Contribution。Core 统一负责发现、配置、
依赖、生命周期、可用性和诊断；Console 负责管理意图、Console 声明校验、资源装配和权限协作。

该模型消除双 SPI、双身份和双状态机，同时避免把页面、菜单和 REST 语义硬编码进通用插件内核，符合
Nexus 轻量、业务中立、依赖单向和可逐步迁移的工程约束。
