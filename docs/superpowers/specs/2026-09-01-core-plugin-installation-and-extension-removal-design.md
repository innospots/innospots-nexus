# Core Plugin Installation and Extension Removal Design

> 本文保留为改造决策记录。现行规范已经拆分到
> `innospots-nexus-core/docs/plugin-installation-design.md`、
> `plugin-runtime-design.md` 和 `plugin-console-contribution-design.md`；如有冲突，以拆分后的现行规范为准。

## 1. 文档定位

本文记录 Nexus 将 Console Extension 安装存储迁移为 Core Plugin 安装体系的原始改造方案。

最终实现只保留 Plugin 概念和 PluginManager 运行时，不保留 Extension 发现、身份、状态机、安装表或
Registry。Console 只提供插件管理接口和 Console Contribution 处理能力，不持有插件安装数据。

## 2. 已确认决策

1. 插件安装实体、DAO、Repository、安装策略和启动协调全部归 `innospots-nexus-core`。
2. `DefaultPluginManager` 保持纯运行时，不直接读写数据库。
3. Core 新增 `PluginInstallationManager`，负责 Catalog 对账、安装意图和 PluginManager 命令协调。
4. Console 不再发现、登记或持久化 Extension，不维护第二套运行状态机。
5. `autoInstall` 只影响第一次发现的新插件，不覆盖已有安装记录和管理员启停意图。
6. `autoInstall=false` 时，新插件只登记数据库；管理员执行“安装并上线”后才启动。
7. 已安装且 `desiredEnabled=true` 的插件在应用重启、版本更新或从 MISSING 恢复后自动尝试启动。
8. 最终删除 `core.extension`、`console.extension`、`ConsoleExtensionProvider`、`ExtensionRegistry`、
   `ExtensionState`、`extensionKey` 安装身份和旧安装表。
9. Console 模块、菜单和页面改为 Plugin 的 `ConsolePluginContribution`，由活动 Contribution Catalog 提供
   给权限资源同步，不再读取 ExtensionRegistry。

## 3. 目标架构

```text
Application ClassLoader / YAML packages
                  │
                  ▼
            PluginCatalog
                  │ definitions
                  ▼
       PluginInstallationManager
          │                 │
          │                 ├── PluginInstallationRepository
          │                 │          └── nx_plugin_installation
          │                 │
          │ eligible IDs    └── installation intent / diagnostics
          ▼
            PluginManager
          │         │
          │         └── Capability Registry
          │
          └── Contribution Handlers
                    └── ConsoleContributionCatalog
                                  └── Kernel permission sync
```

模块职责：

| 模块 | 职责 |
|------|------|
| Core | Plugin 发现、安装记录、安装策略、依赖、启停、运行状态和通用 Contribution 生命周期 |
| Console | 插件管理 Endpoint/VO、Console Contribution 声明、校验和活动资源目录 |
| Kernel | 基于活动 Console Contribution 和 UiSpec 同步权限资源，不管理插件生命周期 |
| 应用装配 | 绑定系统配置、DAO 实现、ContributionHandler 和最终启动顺序 |

## 4. 安装事实与运行事实分离

### 4.1 持久化事实

数据库只持久化跨 JVM 重启仍有意义的事实：

- 当前 Catalog 是否发现该 pluginId；
- 是否已完成安装确认；
- 管理员是否期望启用；
- 最近发现的定义和版本；
- 最近一次运行结果，仅作为历史诊断。

### 4.2 运行事实

当前 JVM 的 STARTING、ACTIVE、STOPPING、STOPPED、WAITING 和 FAILED 只由 PluginManager 持有。
`lastRuntimeState` 不参与运行决策，不能替代 PluginManager 查询。

### 4.3 派生状态

安装管理视图按正交事实派生状态，不再持久化一个混合语义的 ExtensionState：

| presence | installed | desiredEnabled | 管理视图 | 启动资格 |
|----------|-----------|----------------|----------|----------|
| PRESENT | false | false | REGISTERED | 否 |
| PRESENT | true | false | DISABLED | 否 |
| PRESENT | true | true | ENABLED | 是 |
| MISSING | false | false | MISSING | 否 |
| MISSING | true | false | MISSING_DISABLED | 否 |
| MISSING | true | true | MISSING_ENABLED | 否，恢复后重试 |

`installed=false && desiredEnabled=true` 非法，必须在实体、Repository 和管理命令边界拒绝。

## 5. Core 持久化模型

### 5.1 表结构

新表名：

```text
nx_plugin_installation
```

建议字段：

| 字段 | 类型/长度 | 约束 | 含义 |
|------|-----------|------|------|
| `installation_id` | varchar(32) | PK | 安装记录 ID，前缀 `plg` |
| `plugin_id` | varchar(256) | NOT NULL, UNIQUE | 唯一稳定插件身份 |
| `plugin_version` | varchar(64) | NOT NULL | 最近发现版本 |
| `source_type` | varchar(16) | NOT NULL | `JAVA` 或 `YAML` |
| `presence` | varchar(16) | NOT NULL | `PRESENT` 或 `MISSING` |
| `installed` | boolean | NOT NULL | 是否完成安装确认 |
| `desired_enabled` | boolean | NOT NULL | 管理员期望启用状态 |
| `definition_snapshot` | LOB | NULL | 最近不可变定义摘要 |
| `last_runtime_state` | varchar(32) | NULL | 最近运行状态诊断 |
| `last_error` | LOB | NULL | 最近启动、停止或对账错误 |
| `first_discovered_at` | timestamp | NOT NULL | 首次发现时间 |
| `last_discovered_at` | timestamp | NOT NULL | 最近发现时间 |
| `installed_at` | timestamp | NULL | 首次安装确认时间 |
| `enabled_at` | timestamp | NULL | 最近设为启用时间 |
| `disabled_at` | timestamp | NULL | 最近设为停用时间 |
| `missing_at` | timestamp | NULL | 最近进入 MISSING 时间 |

索引：

```text
uk_nx_plugin_installation_plugin_id(plugin_id)
idx_nx_plugin_installation_presence(presence)
idx_nx_plugin_installation_enablement(installed, desired_enabled)
```

该表是平台全局记录，实体继承 `BaseEntity`，不包含 tenantId 或 workspaceId。

### 5.2 Core 类型

目标包：

```text
com.innospots.nexus.core.plugin.installation
├── config
│   └── PluginInstallationConfig
├── dao
│   └── PluginInstallationDao
├── domain
│   ├── entity
│   │   └── PluginInstallationEntity
│   ├── enums
│   │   ├── PluginPresence
│   │   └── PluginSourceType
│   └── model
│       ├── PluginInstallation
│       ├── PluginDefinitionSnapshot
│       └── PluginEnablement
├── repository
│   └── PluginInstallationRepository
└── service
    ├── PluginInstallationManager
    └── PluginRuntimeFactory
```

`PluginInstallationDao` 只访问 `nx_plugin_installation`。Repository 负责实体读写、幂等 upsert、MISSING
对账和意图持久化。Manager 负责安装策略和调用 PluginManager，不复制 DAO 查询逻辑。

`PluginDefinitionSnapshot` 是专用、可序列化的静态摘要，包含 pluginId、版本、来源、CapabilityKey、
providerId、bind/exposure 摘要和 Contribution 资源身份。不得直接序列化 PluginDefinition，因为其中可能
包含 Factory、Class、Handler 或其它运行时对象；快照也不得包含配置值、Secret、Provider 实例或
ClassLoader。

## 6. 系统配置

Core 定义业务中立配置：

```java
public record PluginInstallationConfig(boolean autoInstall) {
}
```

建议系统配置键：

```properties
nexus.plugin.auto-install=false
```

默认值为 false。该值只在数据库不存在 pluginId 记录时生效：

| 记录情况 | autoInstall | 结果 |
|----------|-------------|------|
| 首次发现 | false | `installed=false`, `desiredEnabled=false` |
| 首次发现 | true | `installed=true`, `desiredEnabled=true`，加入启动集合 |
| 已登记未安装 | 任意 | 保持未安装，不因配置变化自动安装 |
| 已安装启用 | 任意 | PRESENT 时加入启动集合 |
| 已安装停用 | 任意 | 保持停用 |
| MISSING 后恢复 | 任意 | 恢复 PRESENT，沿用原安装与启停意图 |

因此，将 autoInstall 从 false 改为 true 不会批量安装历史 REGISTERED 插件。批量安装必须是单独、明确的
管理命令，不能作为系统配置变化的隐式副作用。

## 7. 启动与对账流程

### 7.1 启动顺序

```text
1. 发现并全局校验 PluginCatalog
2. PluginInstallationRepository 读取全部安装记录
3. PluginInstallationManager.reconcile(catalog)
4. 为新插件按 autoInstall 创建记录
5. 更新已存在插件的 version/source/snapshot/lastDiscoveredAt
6. 把 Catalog 中不存在的历史记录标记为 MISSING
7. 计算 PRESENT && installed && desiredEnabled 的 pluginIds
8. 构造 PluginRuntimeConfig.disabledPluginIds
9. 创建并启动 PluginManager
10. 记录每个插件最近运行结果
11. 发布活动 Capability 和 Contribution
```

装配采用两阶段、单所有者方式：

```text
PluginInstallationManager.create(repository, installationConfig, catalog, runtimeFactory)
        ├── reconcile catalog and build PluginEnablement
        ├── use PluginEnablement to create PluginRuntimeConfig
        ├── create the internal PluginManager through runtimeFactory
        └── expose capabilities, runtime queries and installation commands
```

`PluginRuntimeFactory` 只负责使用已计算的 PluginRuntimeConfig、Catalog 和 ContributionHandlers 创建纯运行时
PluginManager。PluginInstallationManager 随后拥有该 PluginManager 的关闭责任，避免先构造半初始化 Manager
再通过 setter 注入运行时。

Catalog 全局定义校验必须先完成；无效安装包不能写入数据库，避免把不可解析定义登记成可管理插件。

### 7.2 新插件自动安装

`autoInstall=true` 时，新记录与管理员手动安装使用同一安装命令语义：先持久化 installed 和
desiredEnabled，再调用 PluginManager。启动失败不撤销安装意图，记录 FAILED 和 lastError，等待配置修复
或人工重试。

### 7.3 新插件只登记

`autoInstall=false` 时，新记录进入 REGISTERED 派生状态，不创建 Provider、不执行 Plugin 生命周期、不发布
Contribution。管理端仍可查看定义快照、版本、来源和静态资源摘要。

### 7.4 MISSING

历史记录未出现在当前 Catalog 时设置 `presence=MISSING` 和 missingAt，保留 installed、desiredEnabled、
definitionSnapshot 和权限关联。MISSING 不是 PluginState。恢复相同 pluginId 后更新定义快照并清除
missingAt；若原记录已安装启用，则自动尝试启动。

## 8. 管理命令与事务语义

### 8.1 `installAndStart(pluginId)`

前置条件：记录存在、presence=PRESENT、Catalog 中存在同一 pluginId。

```text
持久化 installed=true, desiredEnabled=true, installedAt/enabledAt
        ↓ 提交事务
PluginManager.start(pluginId)
        ↓
成功：记录 lastRuntimeState=ACTIVE，清除 lastError
失败：保留安装和启用意图，记录 FAILED 和 lastError
```

重复调用必须幂等；ACTIVE 插件直接返回当前视图。

### 8.2 `enable(pluginId)`

要求 installed=true、presence=PRESENT。先持久化 desiredEnabled=true，再调用 start。启动失败不回滚
管理员意图。

### 8.3 `disable(pluginId)`

要求 installed=true。先持久化 desiredEnabled=false，再调用 stop。停止失败时仍保留 false，记录错误，
表示管理员意图与当前 JVM 状态暂时不一致。

### 8.4 查询

管理查询组合两类信息：

- Repository：安装、presence、desiredEnabled、版本和历史错误；
- PluginManager：当前 JVM runtimeState、phase、依赖和启动时间。

查询 DTO 必须明确区分 `desiredEnabled` 与 `runtimeState`，不能重新组合成一个含糊的 state。

### 8.5 并发与事务

安装命令按 pluginId 串行；数据库唯一索引是并发兜底。不得持有数据库事务调用未知 Plugin 代码。所有命令
遵循“先提交管理员意图，再执行运行时动作，再单独写诊断”的三段式流程。

## 9. Console Contribution 替换 Extension

### 9.1 Core 通用契约

Core 只定义业务中立的：

```text
PluginContribution
PluginContributionType
ContributionHandler
PreparedPluginContribution
```

Core 不定义菜单、页面、权限或 UiSpec 语义。

### 9.2 Console 所有的声明

原 `core.extension.declaration` 中的管理页面声明移动并重命名到 Console：

```text
com.innospots.nexus.core.plugin.contribution.console
├── ConsolePluginContribution
├── ConsoleModuleDeclaration
├── MenuDeclaration
├── UiSpecPageDeclaration
├── ConsolePluginContributionHandler
└── ConsoleContributionCatalog
```

`ConsolePluginContribution` 只包含 modules，不重复 pluginId、version、displayName 或 description；这些统一
来自所属 PluginDefinition。

`ConsoleContributionCatalog` 是活动资源的不可变内存快照，不是第二个 PluginManager，不存启停状态，也不
写插件安装表。Handler 在 Plugin 启动事务中 prepare，PluginManager 提交成功后原子发布；停止时先撤出。

### 9.3 Kernel 权限同步

`PermissionResourceSyncService` 改为读取 `ConsoleContributionCatalog.activeContributions()`，每条活动贡献
携带 ownerPluginId 和 ConsolePluginContribution。同步逻辑继续加载 UiSpec、校验页面和菜单，并把来源插件
记录为 ownerPluginId。

权限资源表中的：

```text
extension_key
```

改为：

```text
owner_plugin_id
```

对应 Java 字段由 `extensionKey` 改为 `ownerPluginId`。停用或 MISSING 不删除历史权限分配，只将不再活动的
资源标记为不可用；相同 pluginId 和资源 key 恢复后可复用关联。

## 10. Extension 删除清单

最终删除以下生产代码目录和类型：

```text
innospots-nexus-core/.../core/extension
innospots-nexus-console/.../console/extension
```

包括：

- `ConsoleExtensionProvider`；
- `ExtensionDescriptor`；
- `ExtensionModuleDeclaration`；
- `ExtensionProviderDiscovery`；
- `ExtensionRegistry`；
- `ExtensionInstallationEntity`；
- `ExtensionInstallationDao`；
- `ExtensionInstallationRepository`；
- `ExtensionRegistration`；
- `ExtensionState`。

对应测试、测试 SPI 文件和 package-info 同步删除或迁移。不得保留 deprecated 双轨入口；迁移完成后，
Classpath 只发现 Plugin。

## 11. 数据迁移策略

当前仓库没有 Flyway/Liquibase 迁移机制，因此本改造只定义目标实体与迁移约束，不在 Core 中引入数据库
迁移框架。

### 11.1 无历史数据环境

直接创建 `nx_plugin_installation`，删除 `nexus_extension_installation`，权限资源字段直接使用
`owner_plugin_id`。

### 11.2 需要保留历史数据

必须先提供经过审核的显式映射：

```text
legacy extensionKey -> canonical pluginId
```

禁止根据字符串、包名或展示名自动猜测。迁移顺序：

1. 创建 `nx_plugin_installation`；
2. 按映射迁移旧安装记录；
3. `enabled` 映射到 `installed=true` 和 `desired_enabled=enabled`；
4. 旧 MISSING 映射到 `presence=MISSING`，其它记录按当前 Catalog 决定 PRESENT/MISSING；
5. descriptorSnapshot 转换或由新 PluginDefinition 重新生成；
6. 权限资源的 extensionKey 按同一映射写入 ownerPluginId；
7. 对账记录数、唯一键和权限归属；
8. 删除旧 Extension 表和字段。

旧 ACTIVE/FAILED 等状态只迁入 `last_runtime_state` 作为诊断，不作为新 JVM 的启动事实。

## 12. 错误处理

Core 插件技术状态码需要覆盖：

- 安装记录冲突；
- 非法安装状态组合；
- 插件未发现；
- 插件未安装；
- 插件处于 MISSING；
- 安装或启停持久化失败；
- 自动安装启动失败；
- Console Contribution 校验或发布失败。

预期错误使用 `NexusException` 和类型化 `PluginStatusCode`。Repository 翻译数据库异常并保留 cause；Manager
补充 pluginId 和操作阶段，但不得在每层重复记录和包装同一异常。

## 13. 分阶段实施

### 阶段一：Core 安装持久化

- 先写实体、DAO、Repository 和安装策略契约测试；
- 新增 `nx_plugin_installation` 对应 Core 类型；
- 扩展发现结果以记录 JAVA/YAML 来源，生成不含运行时对象的 PluginDefinitionSnapshot；
- 实现首次登记、保持意图、MISSING 和恢复对账；
- 不接入旧 ExtensionRegistry。

### 阶段二：安装管理与 PluginManager 协调

- 增加 `PluginInstallationConfig`；
- 实现 autoInstall 两种启动路径；
- 实现 installAndStart、enable、disable 和组合查询；
- 验证运行失败不回滚管理员意图。

### 阶段三：通用 Contribution 和 Console Contribution

- Core 增加通用 Contribution 生命周期契约；
- Console 增加 ConsolePluginContribution、Handler 和 Catalog；
- PluginDefinition 支持 contributions；
- PluginManager 将 Capability 与 Contribution 放入同一启动/停止事务。

### 阶段四：Kernel 权限同步迁移

- PermissionResourceSyncService 改读 ConsoleContributionCatalog；
- `extensionKey` 来源字段改为 `ownerPluginId`；
- 更新权限资源实体、DAO 查询、测试和设计说明。

### 阶段五：删除 Extension

- 删除 `core.extension` 和 `console.extension`；
- 删除旧 SPI 及测试资源；
- 删除旧安装实体、Repository、Registry 和状态；
- 全仓检索并清除插件体系中的 Extension 引用。

### 阶段六：完整验证

- 每批 Java 修改后执行 `mvn clean compile`；
- 执行 Core、Console、Kernel 聚焦测试；
- 执行 `mvn validate`、`mvn test`、`mvn -q help:effective-pom`；
- 执行 `git diff --check`；
- 确认没有生成或修改模块 SKILL.md 和 references。

## 14. 测试矩阵

至少覆盖：

- autoInstall=false 首次发现只登记、不启动；
- autoInstall=true 首次发现安装并启动；
- 切换 autoInstall 不改变历史 REGISTERED 记录；
- 已安装启用插件重启后自动启动；
- 已安装停用插件重启后保持停用；
- MISSING 保留 installed 和 desiredEnabled；
- MISSING 恢复后按原意图启动或保持停用；
- 安装和 enable 启动失败保留 desiredEnabled=true；
- disable 停止失败保留 desiredEnabled=false；
- 非法 `installed=false && desiredEnabled=true` 被拒绝；
- 并发首次登记由唯一索引收敛为一条记录；
- PluginManager 当前状态覆盖 lastRuntimeState 历史诊断；
- Console Contribution prepare 失败不发布部分资源；
- 插件 ACTIVE 前权限同步看不到其 Console Contribution；
- 插件停止后 Console Contribution 原子撤出；
- 权限资源 ownerPluginId 正确且停用不删除授权；
- 全仓生产代码不再依赖 ConsoleExtensionProvider 或 ExtensionRegistry。

## 15. 验收标准

- 插件安装表和全部安装写逻辑位于 Core；
- Console 模块不存在插件安装 Entity、DAO、Repository 或状态机；
- PluginManager 是当前 JVM 运行状态唯一事实源；
- PluginInstallationManager 是持久化安装意图和管理命令唯一入口；
- autoInstall 行为只影响首次发现且默认关闭；
- 一个 pluginId 只有一条安装记录；
- MISSING、installed、desiredEnabled 和 runtimeState 含义无重叠；
- Console 页面/菜单通过 Plugin Contribution 发布；
- Kernel 权限同步不再依赖 ExtensionRegistry；
- `core.extension`、`console.extension` 和旧 Extension SPI 已删除；
- 旧表和 extensionKey 安装身份不再被新代码读写；
- Core、Console、Kernel 编译和测试全部通过。
