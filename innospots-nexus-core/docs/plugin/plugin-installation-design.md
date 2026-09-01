# Nexus Plugin Installation 设计

## 1. 文档定位

本文定义 Plugin 的登记、安装、启停意图、Catalog 对账、MISSING 和持久化模型。安装能力是业务中立的平台
基础设施，全部位于 `innospots-nexus-core`。Console 只提供管理接口和展示模型。

## 2. 核心决策

1. `PluginInstallationManager` 是安装事实和管理命令唯一入口；
2. `PluginManager` 保持纯运行时，不直接访问数据库；
3. `autoInstall` 只影响首次发现的新 pluginId；
4. 默认只登记、不自动安装；
5. 已安装且 desiredEnabled=true 的插件在重启或 MISSING 恢复后自动尝试启动；
6. 运行失败不回滚管理员安装和启用意图；
7. 最终删除 Extension 安装表、Repository、Registry 和 ExtensionState。

## 3. 架构

```text
PluginDiscoveryReport
        │ validCatalog
        ▼
PluginInstallationManager
├── PluginInstallationRepository ── nx_plugin_installation
├── PluginRuntimeFactory
└── PluginManager
        ├── runtimeState
        ├── Capability Registry
        └── Contribution Handlers
```

`PluginInstallationManager` 负责创建并拥有 PluginManager。`PluginRuntimeFactory` 使用已经计算好的 Catalog、
RuntimeConfig 和 Handler 创建纯运行时，避免循环依赖和 setter 注入。

## 4. 正交事实

### 4.1 持久化事实

| 字段 | 含义 |
|------|------|
| `presence` | 当前 Catalog 是否发现 pluginId：PRESENT/MISSING |
| `installed` | 是否完成安装确认 |
| `desiredEnabled` | 管理员是否期望启用 |
| `lastRuntimeState` | 最近一次运行结果，仅用于历史诊断 |

### 4.2 运行事实

STARTING、ACTIVE、STOPPING、STOPPED、WAITING 和 FAILED 只由当前 PluginManager 持有。
`lastRuntimeState` 不参与启动决策。

### 4.3 管理派生状态

| presence | installed | desiredEnabled | 管理状态 | 启动资格 |
|----------|-----------|----------------|----------|----------|
| PRESENT | false | false | REGISTERED | 否 |
| PRESENT | true | false | DISABLED | 否 |
| PRESENT | true | true | ENABLED | 是 |
| MISSING | false | false | MISSING | 否 |
| MISSING | true | false | MISSING_DISABLED | 否 |
| MISSING | true | true | MISSING_ENABLED | 否，恢复后重试 |

`installed=false && desiredEnabled=true` 非法，必须在实体、Repository 和命令边界拒绝。

## 5. 表结构

新表：

```text
nx_plugin_installation
```

| 字段 | 类型/长度 | 约束 | 含义 |
|------|-----------|------|------|
| `installation_id` | varchar(32) | PK | 安装记录 ID，前缀 `plg` |
| `plugin_id` | varchar(256) | NOT NULL, UNIQUE | 稳定插件身份 |
| `plugin_version` | varchar(64) | NOT NULL | 最近发现版本 |
| `source_type` | varchar(16) | NOT NULL | JAVA/YAML |
| `source_location` | varchar(1024) | NULL | 声明来源诊断 |
| `presence` | varchar(16) | NOT NULL | PRESENT/MISSING |
| `installed` | boolean | NOT NULL | 是否完成安装确认 |
| `desired_enabled` | boolean | NOT NULL | 管理员启用意图 |
| `definition_snapshot` | LOB | NULL | 可序列化静态摘要 |
| `last_runtime_state` | varchar(32) | NULL | 最近运行结果 |
| `last_error` | LOB | NULL | 最近对账或运行错误 |
| `first_discovered_at` | timestamp | NOT NULL | 首次发现时间 |
| `last_discovered_at` | timestamp | NOT NULL | 最近发现时间 |
| `installed_at` | timestamp | NULL | 首次安装确认时间 |
| `enabled_at` | timestamp | NULL | 最近启用时间 |
| `disabled_at` | timestamp | NULL | 最近停用时间 |
| `missing_at` | timestamp | NULL | 最近进入 MISSING 时间 |

索引：

```text
uk_nx_plugin_installation_plugin_id(plugin_id)
idx_nx_plugin_installation_presence(presence)
idx_nx_plugin_installation_enablement(installed, desired_enabled)
```

该表是平台全局记录，实体继承 `BaseEntity`，不包含 tenantId 或 workspaceId。

## 6. Core 类型边界

建议包结构：

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

职责：

- DAO：只访问 `nx_plugin_installation`；
- Repository：实体转换、幂等 upsert、MISSING 对账和意图写入；
- Manager：安装策略、命令串行化、PluginManager 协调和组合查询；
- RuntimeFactory：创建纯 PluginManager；
- Snapshot：只保存可序列化静态摘要。

## 7. 系统配置

```java
public record PluginInstallationConfig(boolean autoInstall) {
}
```

```properties
nexus.plugin.auto-install=false
```

| 记录情况 | autoInstall | 行为 |
|----------|-------------|------|
| 首次发现 | false | installed=false，desiredEnabled=false |
| 首次发现 | true | installed=true，desiredEnabled=true，加入启动集合 |
| 已登记未安装 | 任意 | 保持未安装 |
| 已安装启用 | 任意 | PRESENT 时加入启动集合 |
| 已安装停用 | 任意 | 保持停用 |
| MISSING 恢复 | 任意 | 沿用原安装和启停意图 |

将 autoInstall 从 false 改为 true 不会批量安装历史 REGISTERED 插件。批量安装必须是显式管理命令。

## 8. 启动对账

```text
1. 发现并校验 PluginCatalog
2. 读取全部安装记录
3. reconcile(catalog)
4. 为新插件按 autoInstall 创建记录
5. 更新已存在插件的 version/source/snapshot/lastDiscoveredAt
6. 把 Catalog 中不存在的历史记录标记为 MISSING
7. 计算 PRESENT && installed && desiredEnabled 的 pluginIds
8. 创建 PluginManager
9. 启动 eligible plugins
10. 单独记录最近运行结果
```

无效声明不能写入安装表。DiscoveryReport 中的 rejected definition 通过启动诊断展示，但不形成可管理安装
记录，避免数据库保存无法确定身份或无法解析的定义。

## 9. 管理命令

### 9.1 installAndStart

前置条件：记录存在、presence=PRESENT、Catalog 中存在同一 pluginId。

```text
持久化 installed=true, desiredEnabled=true
        ↓ 提交事务
PluginManager.start(pluginId)
        ↓
成功：写 lastRuntimeState=ACTIVE，清除 lastError
失败：保留安装和启用意图，写 FAILED 和 lastError
```

重复调用幂等；ACTIVE 插件直接返回当前组合视图。

### 9.2 enable

要求 installed=true 且 presence=PRESENT。先提交 desiredEnabled=true，再调用 start。启动失败不回滚意图。

### 9.3 disable

要求 installed=true。先提交 desiredEnabled=false，再调用 stop。停止失败时仍保留 false，并记录管理员意图
与当前 JVM 状态暂时不一致。

### 9.4 查询

```text
PluginInstallationRepository ── presence / installed / desiredEnabled / history
PluginManager                  ── runtimeState / phase / dependencies / timings
                                  │
                                  ▼
                         PluginManagementView
```

不得把以上字段重新合并成一个含糊 state。

## 10. 事务和并发

- 管理命令按 pluginId 串行；
- 数据库唯一索引是首次登记并发兜底；
- 不得持有数据库事务调用未知 Plugin 代码；
- 命令统一采用“提交意图 → 执行运行时动作 → 单独写诊断”；
- Repository 写入失败时不得调用 PluginManager；
- PluginManager 失败不能伪装成持久化回滚成功；
- Manager close 时负责关闭其创建的 PluginManager。

## 11. MISSING

历史记录未出现在 Catalog 时：

- 设置 presence=MISSING 和 missingAt；
- 保留 installed、desiredEnabled、definitionSnapshot 和权限关联；
- 不创建运行时 Plugin；
- 不删除资源归属和历史授权。

恢复相同 pluginId 后更新定义快照并清除 missingAt。原记录已安装启用时自动尝试启动；原记录未安装或停用
时保持原意图。

## 12. Extension 数据迁移

最终删除：

```text
core.extension
console.extension
ConsoleExtensionProvider
ExtensionProviderDiscovery
ExtensionRegistry
ExtensionInstallationEntity
ExtensionInstallationRepository
ExtensionState
nexus_extension_installation
```

需要历史数据时必须提供显式映射：

```text
legacy extensionKey -> canonical pluginId
```

禁止根据字符串、包名或展示名猜测。旧 enabled 映射为 installed=true 和
desiredEnabled=enabled；旧 ACTIVE/FAILED 只进入 lastRuntimeState 作为诊断。

## 13. 测试要求

至少覆盖：

- autoInstall=false 首次发现只登记；
- autoInstall=true 首次发现安装并启动；
- 配置变化不安装历史 REGISTERED 插件；
- 已安装启用插件重启后启动；
- 已安装停用插件保持停用；
- MISSING 保留安装和启停意图；
- MISSING 恢复按原意图处理；
- 启动或停止失败保留管理员意图；
- 非法事实组合被拒绝；
- 并发首次登记只生成一条记录；
- PluginManager 当前状态覆盖历史 lastRuntimeState；
- 查询清楚区分安装事实和运行事实；
- Console 模块不再持有安装 Entity、DAO、Repository 或状态机。
