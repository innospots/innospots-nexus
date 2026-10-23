# 插件发现与系统启动

## 1. 两类入口

| 场景 | 入口 | 是否写安装表 |
|------|------|--------------|
| 生产 / 带安装管理 | `PluginHostBootstrap.enable(request)` | 是 |
| 测试 / 纯运行时 | `DefaultPluginManager.create(...)` | 否 |

生产路径由应用宿主组装 `PluginHostBootstrapRequest`。Core 只提供契约与启动协调，
不绑定 Spring Boot 或 Quarkus。

## 2. 核心组件

```text
classpath
├── ClasspathPluginDiscovery          # 扫描 Plugin SPI 与全部 plugin.yaml
├── PluginDefinitionCompiler          # YAML -> PluginDefinition
├── JacksonPluginManifestParser       # YAML 文本解析
├── ManifestPlugin                    # YAML 插件的 Plugin 适配器
├── PluginCatalog                     # 全局身份与定义校验
└── PluginDiscoveryReport             # validCatalog + rejectedDefinitions

安装层
├── PluginHostBootstrap               # 发现 -> 对账 -> 创建运行时 -> 启动
├── PluginInstallationManager         # 安装意图、管理命令、运行诊断
├── PluginInstallationRepository      # nx_plugin_installation 读写
└── PluginRuntimeFactory              # 计算 eligible/disabled/required 后创建运行时

运行时
├── DefaultPluginManager              # 生命周期协调与运行快照
├── ManagedPlugin                     # 单插件启动/停止事务
├── ConsoleContributionCatalog        # 已提交且 ACTIVE 的 console@2 目录
└── PluginAvailabilityIndex           # Contribution 可见性门控
```

**发现资源位置**

| 类型 | 位置 |
|------|------|
| Java 插件 | `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin` |
| YAML 插件 | `META-INF/nexus/plugin.yaml` |

发现阶段只调用 `Plugin.definition()` 或编译 YAML，不执行插件生命周期方法。

## 3. 生产启动序列

```text
PluginHostBootstrap.enable(request)
  1. ClasspathPluginDiscovery.discoverReport()
  2. PluginCatalog 校验 pluginId 唯一与定义合法
  3. PluginInstallationRepository.register(...) / markMissing(...)
  4. 计算 installed=true 且 desiredEnabled=true 的 eligible 插件
  5. PluginRuntimeFactory.create(catalog, eligible)
  6. DefaultPluginManager.start()
  7. 回写 runtimeState / runtimeError
  8. 返回 PluginInstallationManager
```

`PluginInstallationConfig.autoInstall()` 控制首次发现的默认安装意图。未进入 eligible
集合的插件仍存在于运行时目录，但会被视为 disabled，不会自动启动。

## 4. 单插件启动阶段

```text
config-resolve
  -> contribution-prepare
  -> plugin.initialize(context)
  -> plugin.start()
  -> contribution-stage
  -> contribution-commit
  -> availability.activate
ACTIVE
```

任一阶段失败都会回滚已准备的 Contribution 与资源作用域，并将插件标记为 `FAILED`。

## 5. 管理入口

```java
PluginInstallationManager manager = PluginHostBootstrap.enable(request);

manager.plugins();
manager.installAndStart("com.example.reports.console");
manager.disable("com.example.reports.console");
manager.retryStart("com.example.reports.console");
manager.close();
```

运行时状态只存在于内存；安装意图持久化在 `nx_plugin_installation`。`runtimeState`
与 `runtimeError` 仅用于诊断，不参与下次启动判定。

## 6. 纯运行时

```java
PluginDiscoveryReport report = new ClasspathPluginDiscovery(
        classLoader,
        decoderRegistry).discoverReport();

PluginManager manager = DefaultPluginManager.create(
        runtimeConfig,
        report.validCatalog(),
        contributionHandlers);
manager.start();
```

## 7. 失败边界

| 情况 | 行为 |
|------|------|
| 单个 SPI / YAML 定义非法 | 进入 `rejectedDefinitions`，不阻断其他插件 |
| 重复 `pluginId` | Catalog 构建失败 |
| 缺少 Contribution Decoder / Handler / Snapshotter | 启动预检失败 |
| 非 required 插件启动失败 | 隔离为 `FAILED`，其他插件继续 |
| required 插件未 ACTIVE | 回滚本次启动集合并抛错 |

相关测试：`ClasspathPluginDiscoveryTest`、`PluginInstallationManagerTest`、
`PluginHostBootstrapTest`、`PluginHostAssemblyTest`。
