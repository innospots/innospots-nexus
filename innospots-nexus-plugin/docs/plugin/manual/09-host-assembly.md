# 宿主装配说明

Core 不内置最终 Application 启动类；宿主负责注入 DAO、注册表与管理器。本文给出最小装配契约。

## 1. 必备组件与类用途

| 类 | 实例用途 | 使用阶段 | 谁构造 |
|----|----------|----------|--------|
| `PluginHostBootstrap` | 宿主**唯一启用入口**；内部串联发现 → 对账 → 启动 | 应用启动 | 静态调用，无需实例化 |
| `PluginHostBootstrapRequest` | 打包传入 `enable()` 的全部依赖 | 应用启动 | 宿主在启动代码中 `new` |
| `PluginInstallationDao` | 读写 `nx_plugin_installation` 安装事实表 | 对账、管理命令、运行诊断回写 | 应用 MyBatis Mapper |
| `PluginRuntimeConfig` | 宿主运行时策略：配置项、默认路由、required/disabled 插件、类加载器 | 运行时装配与 `start()` | 宿主从配置中心组装 |
| `PluginInstallationConfig` | 安装策略：首次发现是否 `autoInstall` | 对账 `register()` | 宿主从 `nexus.plugin.auto-install` 解析 |
| `PluginContributionDecoderRegistry` | 将 YAML `contributions` 段解码为强类型 `PluginContribution` | **classpath 发现** | 宿主注册各 Contribution 类型的 Decoder |
| `ConsolePluginContributionDecoder` | 解析 `console@1` YAML/Java 声明为 `ConsolePluginContribution` | 发现（Decoder 注册表内） | Console 模块，宿主 `new` 后注册 |
| `List<PluginContributionHandler<?>>` | 插件 `start()` 时执行 Contribution 的 prepare/stage/commit | **插件运行时** | 宿主组装；每项对应一种 contribution 类型 |
| `ConsolePluginContributionHandler` | 校验并发布 Console 菜单/页面到活动目录；停止时撤回 | 插件运行时 | Console 模块，注入 `ConsoleContributionCatalog` |
| `ConsoleContributionCatalog` | 持有当前 **ACTIVE** 插件发布的 Console 模块/菜单/页面 | 运行时 + 前端查询 | 宿主单例 Bean，Handler 写入 |
| `ReservedPluginResourceCatalog` | 声明宿主保留的 moduleKey/pagePath 等，防止插件冲突占用 | Handler 全局校验 | 宿主按平台规范填充 |
| `PluginContributionSnapshotterRegistry` | 对账时把 Contribution 转为可持久化的安全 JSON 摘要 | **对账写库** | 宿主注册各类型的 Snapshotter |
| `ConsolePluginContributionSnapshotter` | 生成 `console@1` 的安装表定义快照字段 | 对账写库 | Console 模块，注册进 Snapshotter 表 |
| `ClassLoader` | 扫描 `META-INF/services/...Plugin` 与 `plugin.yaml` | classpath 发现 | 通常为线程上下文 CL 或应用 CL |
| `PluginInstallationManager` | **enable() 返回值**；安装意图、启停命令、聚合查询、Capability 入口 | 启动后至 `close()` | `PluginHostBootstrap.enable()` 创建 |
| `CapabilityManager` | 业务代码查询/路由 Capability Provider | 插件 ACTIVE 后 | `installationManager.capabilities()` |

内部由 Bootstrap 创建、宿主一般**不直接**实例化的类：

| 类 | 用途 |
|----|------|
| `ClasspathPluginDiscovery` | 扫描 SPI + YAML，产出发现报告 |
| `PluginDiscoveryReport` | `validCatalog` + `rejectedDefinitions` |
| `PluginInstallationRepository` | DAO 之上的对账/意图/诊断领域仓储 |
| `PluginRuntimeFactory` | 按 eligible 集合创建 `DefaultPluginManager` |
| `DefaultPluginManager` | JVM 内插件生命周期与 Capability 注册表 |
| `PluginManagementEndpoint` | Console REST 管理面（注入 `PluginInstallationManager`） |

## 2. 宿主启用（推荐）

应用宿主只需调用统一入口，无需关心发现、对账与启动顺序：

```java
// 【用途】统一启用入口；内部完成发现、对账、启动，返回安装管理器
PluginInstallationManager installationManager = PluginHostBootstrap.enable(
        new PluginHostBootstrapRequest(  // 【用途】打包全部宿主依赖
                pluginInstallationDao,   // 【用途】安装表 DAO
                baseRuntimeConfig,         // 【用途】运行时配置与路由策略
                PluginInstallationConfig.from(...), // 【用途】是否 autoInstall
                contributionDecoders,      // 【用途】YAML contribution 解码表
                handlers,                  // 【用途】运行时 Contribution Handler 列表
                snapshotters,              // 【用途】对账 contribution 快照表
                classLoader));             // 【用途】SPI/YAML 扫描类加载器

// 【用途】业务代码 require/find Capability Provider
CapabilityManager capabilities = installationManager.capabilities();

// 【用途】按启动逆序停止插件并释放运行时
installationManager.close();
```

## 3. 宿主依赖实例化示例

以下示例展示 `PluginHostBootstrapRequest` 各字段**在应用启动时如何构造**。建议将注册表与 Console 组件做成**进程级单例**（如 Spring `@Bean`），`enable()` 只负责串联发现与启动。

### 3.1 场景对照：需要注册什么

| classpath 上的插件 | `PluginContributionDecoderRegistry` | `contributionHandlers` | `contributionSnapshotters` |
|--------------------|-------------------------------------|------------------------|----------------------------|
| 仅 Java SPI，无 YAML | 空表即可 | `List.of()` | 空表即可 |
| YAML 声明 capability | 空表即可（类型由插件 `api` 字段自描述） | 视是否有 contribution | 视是否有 contribution |
| YAML / Java 含 `console@1` | 注册 `ConsolePluginContributionDecoder` | 注册 `ConsolePluginContributionHandler` | 注册 `ConsolePluginContributionSnapshotter` |

### 3.2 最小示例：仅 Java SPI 插件

适用于测试或 classpath 上只有 `META-INF/services/...Plugin`、无 `plugin.yaml`、无 Contribution 的宿主。

```java
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

```java
// 【用途】安装表 DAO：对账时 register/markMissing，管理命令后 updateRuntime
PluginInstallationDao installationDao = applicationContext.getBean(PluginInstallationDao.class);

// 【用途】运行时策略：解析 plugins.{id}.{key} 配置、默认 Capability 路由、required/disabled
PluginRuntimeConfig runtimeConfig = new PluginRuntimeConfig(
        Set.of(),                          // requiredPluginIds：未 ACTIVE 则整批 start 失败
        Set.of(),                          // disabledPluginIds：永不自动启动
        Map.of(),                          // hostConfig
        Map.of(),                          // runtimeVariables（覆盖 hostConfig）
        Map.of(),                          // defaultRoutes：CapabilityKey → Tags
        null);                             // pluginClassLoader；null 则用线程 CL

// 【用途】安装策略：首次发现是否自动 installed + desiredEnabled
PluginInstallationConfig installationConfig =
        PluginInstallationConfig.from(System.getProperty(PluginInstallationConfig.AUTO_INSTALL_KEY));

// 【用途】YAML contribution 解码表；无 YAML contribution 可空表
PluginContributionDecoderRegistry contributionDecoders =
        PluginContributionDecoderRegistry.builder().build();
// 【用途】运行时 Contribution 处理器列表；无 contribution 可空列表
List<com.innospots.nexus.core.plugin.contribution.PluginContributionHandler<?>> handlers =
        List.of();
// 【用途】对账写库时 contribution 摘要；无 contribution 可空表
PluginContributionSnapshotterRegistry contributionSnapshotters =
        PluginContributionSnapshotterRegistry.builder().build();

// 【用途】统一启用入口；返回安装管理器（管理面 + Capability 查询 + close）
PluginInstallationManager installationManager = PluginHostBootstrap.enable(
        new PluginHostBootstrapRequest(
                installationDao,             // 见上
                runtimeConfig,
                installationConfig,
                contributionDecoders,
                handlers,
                contributionSnapshotters,
                Thread.currentThread().getContextClassLoader())); // 【用途】SPI/YAML 扫描范围

// 【用途】业务侧按类型/标签查找 Capability Provider
// CapabilityManager capabilities = installationManager.capabilities();
```

### 3.3 完整示例：管理台 + YAML Capability + console@1

适用于最终应用（依赖 `innospots-nexus-console`），classpath 上可能有 YAML 插件与 Console 菜单贡献。

```java
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;

public final class PluginHostAssembly {

  // 宿主与业务模块共享的 Capability API 契约（示例）
  public interface MessageSender extends CapabilityProvider {
      void send(String to, String body);
  }

  // 【用途】默认路由键；业务 require("message.sender", 1, tags) 未传 tags 时使用
  private static final CapabilityKey MESSAGE_SENDER = new CapabilityKey("message.sender", 1);

  // 【用途】运行时 Console 活动目录：Handler commit 后前端/权限可读取
  private final ConsoleContributionCatalog consoleCatalog = new ConsoleContributionCatalog();

  /** 进程级单例：应用 @Configuration 中 @Bean 暴露此方法返回值亦可。 */
  public PluginInstallationManager enable(
          PluginInstallationDao installationDao,
          Map<String, String> hostConfig,
          Map<String, String> runtimeVariables,
          boolean autoInstall
  ) {
      // 【用途】发现阶段：把 YAML contributions 中的 console@1 解码为 ConsolePluginContribution
      PluginContributionDecoderRegistry contributionDecoders =
              PluginContributionDecoderRegistry.builder()
                      .register(new ConsolePluginContributionDecoder())
                      .build();

      // 【用途】运行时：插件 start 时校验并发布菜单/页面；stop 时从 catalog 撤出
      ConsolePluginContributionHandler consoleHandler = new ConsolePluginContributionHandler(
              consoleCatalog,
              // 【用途】声明平台保留的 module/page 前缀，防止插件占用冲突
              new ReservedPluginResourceCatalog(List.of()));
      List<PluginContributionHandler<?>> handlers = List.of(consoleHandler);

      // 【用途】对账阶段：将 console contribution 序列化为安装表 definition_snapshot 的安全字段
      PluginContributionSnapshotterRegistry contributionSnapshotters =
              PluginContributionSnapshotterRegistry.builder()
                      .register(new ConsolePluginContributionSnapshotter())
                      .build();

      PluginRuntimeConfig runtimeConfig = new PluginRuntimeConfig(
              Set.of(),
              Set.of(),
              hostConfig,
              runtimeVariables,
              Map.of(
                      // 【用途】业务 require/find 未传 Tags 时的默认路由
                      MESSAGE_SENDER.key(), Tags.of("channel", "wecom")
              ),
              Thread.currentThread().getContextClassLoader());

      PluginInstallationConfig installationConfig = new PluginInstallationConfig(autoInstall);

      // 【用途】返回已 enable 的安装管理器，供 REST / Capability / 关闭使用
      return PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
              installationDao,
              runtimeConfig,
              installationConfig,
              contributionDecoders,
              handlers,
              contributionSnapshotters,
              null));
  }

  // 【用途】供 Console 前端或 Kernel 权限同步读取已发布贡献
  public ConsoleContributionCatalog consoleCatalog() {
      return consoleCatalog;
  }
}
```

**`PluginInstallationDao` 实例化**：Core 只定义 MyBatis `BaseMapper` 接口，由应用模块提供 Mapper 实现，例如：

```java
@Mapper
public interface PluginInstallationMapper extends PluginInstallationDao {
}
```

启动时注入 `PluginInstallationMapper`（或包装类）作为 `installationDao` 传入即可。

### 3.4 Spring 风格装配示意（可选）

将「进程级单例」与「每次 enable」分开，便于注入 `PluginManagementEndpoint` 与业务 Service：

```java
@Configuration
class PluginHostConfiguration {

    // 【用途】Console 活动贡献目录，Handler 写入、前端/权限读取
    @Bean
    ConsoleContributionCatalog consoleContributionCatalog() {
        return new ConsoleContributionCatalog();
    }

    // 【用途】发现：YAML console@1 contribution 解码
    @Bean
    PluginContributionDecoderRegistry contributionDecoderRegistry() {
        return PluginContributionDecoderRegistry.builder()
                .register(new ConsolePluginContributionDecoder())
                .build();
    }

    // 【用途】运行时：Console contribution stage/commit
    @Bean
    List<PluginContributionHandler<?>> pluginContributionHandlers(
            ConsoleContributionCatalog consoleCatalog) {
        return List.of(new ConsolePluginContributionHandler(
                consoleCatalog,
                new ReservedPluginResourceCatalog(List.of())));
    }

    // 【用途】对账：console contribution 安装表快照
    @Bean
    PluginContributionSnapshotterRegistry contributionSnapshotterRegistry() {
        return PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
    }

    // 【用途】已 enable 的安装管理器；注入 REST 与业务 Service
    @Bean
    PluginInstallationManager pluginInstallationManager(
            PluginInstallationDao installationDao,
            PluginContributionDecoderRegistry contributionDecoders,
            List<PluginContributionHandler<?>> handlers,
            PluginContributionSnapshotterRegistry snapshotters,
            @Value("${nexus.plugin.auto-install:false}") boolean autoInstall,
            NexusHostProperties hostProperties) {

        PluginRuntimeConfig runtimeConfig = new PluginRuntimeConfig(
                hostProperties.requiredPlugins(),
                hostProperties.disabledPlugins(),
                hostProperties.pluginHostConfig(),
                hostProperties.pluginRuntimeVariables(),
                hostProperties.defaultCapabilityRoutes(),
                null);

        return PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfig,
                new PluginInstallationConfig(autoInstall),
                capabilityTypes,
                contributionDecoders,
                handlers,
                snapshotters,
                null));
    }
}
```

应用关闭时调用 `pluginInstallationManager.close()`（可用 `@PreDestroy`）。

Quarkus 等价做法见 [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md)（`StartupEvent` / `ShutdownEvent`）。

### 3.5 各实例用途速查（按 Request 字段）

| 实例 | 用途（一句话） | 何时必须有内容 | 典型生命周期 |
|------|----------------|----------------|--------------|
| `PluginInstallationDao` | 持久化安装事实与运行诊断 | 生产始终需要 | 单例 Bean |
| `PluginRuntimeConfig` | 配置解析、路由、启停策略 | 始终需要 | 单例或每次 enable |
| `PluginInstallationConfig` | 控制首次发现是否自动安装启用 | 始终需要 | 单例 |
| `PluginContributionDecoderRegistry` | YAML contribution 解码 | 有 YAML contribution 时 | 进程单例 |
| `ConsolePluginContributionDecoder` | 解码 `console@1` | 有 console 插件时 | 注册进 Decoder 表 |
| `List<PluginContributionHandler<?>>` | 运行时发布/撤回 Contribution | 插件声明了 contribution 时 | 进程单例 |
| `ConsolePluginContributionHandler` | 发布 Console 菜单/页面 | 有 console 插件时 | 单例，依赖 Catalog |
| `ConsoleContributionCatalog` | 存放已发布 Console 资源 | 有 console 插件时 | 单例，Handler 写入 |
| `ReservedPluginResourceCatalog` | 平台保留资源，防冲突 | 使用 Console Handler 时 | 单例 |
| `PluginContributionSnapshotterRegistry` | 对账写库 contribution 摘要 | 插件有 contribution 时 | 进程单例 |
| `ConsolePluginContributionSnapshotter` | 生成 console 快照 JSON | 有 console 插件时 | 注册进 Snapshotter 表 |
| `ClassLoader` | 决定扫描哪些 SPI/YAML | 多 ClassLoader 环境时显式传 | 每次 enable 可选 |
| `PluginInstallationManager`（返回值） | 管理命令 + `capabilities()` + `close()` | enable 成功后 | enable 至 close |
| `CapabilityManager` | 业务 `require`/`find` Provider | 业务使用 Capability 时 | 随 InstallationManager |

### 3.6 三阶段与实例关系

```
发现阶段（ClasspathPluginDiscovery）
  ├── 插件声明（YAML api / Java CapabilityType）→ 自动登记 Capability 类型
  └── PluginContributionDecoderRegistry
        └── ConsolePluginContributionDecoder  ← YAML console 段 → ConsolePluginContribution

对账阶段（PluginInstallationManager.reconcile）
  ├── PluginInstallationDao      ← 读写 nx_plugin_installation
  ├── PluginInstallationConfig   ← autoInstall 策略
  └── PluginContributionSnapshotterRegistry
        └── ConsolePluginContributionSnapshotter  ← 定义快照 JSON

运行阶段（DefaultPluginManager / ManagedPlugin.start）
  ├── PluginRuntimeConfig        ← 配置项、默认路由、disabled/required
  ├── List<PluginContributionHandler<?>>
  │     └── ConsolePluginContributionHandler
  │           ├── ConsoleContributionCatalog      ← 活动菜单/页面
  │           └── ReservedPluginResourceCatalog   ← 保留资源校验
  └── （返回）PluginInstallationManager.capabilities() → CapabilityManager
```

### 3.7 字段与构造方对照（原速查表）

| Request 字段 | 谁构造 | 典型生命周期 | 作用阶段 |
|--------------|--------|--------------|----------|
| `installationDao` | 应用持久化层（MyBatis Mapper） | 单例 Bean | 对账写 `nx_plugin_installation` |
| `runtimeConfig` | 应用配置（YAML/环境变量） | 每次 enable 或单例 | 插件配置解析、默认路由、required/disabled |
| `installationConfig` | `PluginInstallationConfig.from(...)` | 每次 enable 或单例 | 首次发现是否 `autoInstall` |
| `contributionDecoders` | 宿主注册各 Contribution 的 Decoder | 进程单例 | YAML 发现编译 contribution |
| `contributionHandlers` | 宿主注册各 Contribution 的 Handler | 进程单例；持有 Console Catalog 等状态 | 插件 `start()` 时 stage/commit |
| `contributionSnapshotters` | 宿主注册各 Contribution 的 Snapshotter | 进程单例 | 对账时序列化 contribution 摘要 |
| `pluginClassLoader` | 通常为 `null` 或显式 CL | 每次 enable 可选 | SPI / YAML 资源扫描 |

## 4. 手动装配（高级 / 测试）

仅在需要自定义发现结果或分步控制时使用：

```java
// 1. 宿主注册表（应用启动早期，单例）
PluginContributionDecoderRegistry decoders = ...;
List<PluginContributionHandler<?>> handlers = ...;
PluginContributionSnapshotterRegistry snapshotters = ...;

ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

// 2. 发现
ClasspathPluginDiscovery discovery = new ClasspathPluginDiscovery(
        classLoader, decoders);
PluginDiscoveryReport report = discovery.discoverReport();

// 3. 安装管理器
PluginInstallationConfig installConfig = PluginInstallationConfig.from(
        systemConfig.get(PluginInstallationConfig.AUTO_INSTALL_KEY));

PluginRuntimeFactory runtimeFactory = new PluginRuntimeFactory(
        baseRuntimeConfig, handlers, snapshotters);

PluginInstallationManager installationManager = new PluginInstallationManager(
        new PluginInstallationRepository(pluginInstallationDao),
        runtimeFactory,
        installConfig,
        report);

// 4. 对账 + 启动 eligible 插件
installationManager.reconcile();
installationManager.start();

// 5. 业务使用
CapabilityManager capabilities = installationManager; // 需从内部 runtime 暴露，或单独持有 PluginManager 引用
```

> 生产代码优先使用 `PluginHostBootstrap.enable`；依赖如何实例化见上文 §3；手动分步见下文 §4。

## 5. PluginRuntimeConfig 字段

```java
new PluginRuntimeConfig(
        Set<String> requiredPluginIds,   // 必须 ACTIVE 否则整批 start 失败
        Set<String> disabledPluginIds,   // 跳过启动（安装层也会合并非 eligible）
        Map<String, String> hostConfig,
        Map<String, String> runtimeVariables,
        Map<CapabilityKey, Tags> defaultRoutes,
        ClassLoader pluginClassLoader    // null 则用调用方 CL
);
```

## 6. 关闭顺序

```java
installationManager.close();  // 关闭内部 PluginManager
```

`PluginInstallationManager.close()` 负责释放其创建的 `PluginManager`；不得在持有 DB 事务时调用插件代码。

## 7. 无安装表的嵌入式用法

单元测试或嵌入式运行时，仍需先由宿主执行发现：

```java
PluginDiscoveryReport report = new ClasspathPluginDiscovery(
        classLoader, decoders).discoverReport();
PluginManager manager = DefaultPluginManager.create(runtimeConfig, report.validCatalog(), handlers);
manager.start();
```

见 `PluginHostAssemblyTest`。

## 8. Console 集成点

- `PluginManagementEndpoint` — REST 管理接口
- `ConsolePluginContributionHandler` — `console@1` 运行时
- 权限资源 `ownerPluginId` — Kernel 权限模块

Console 依赖 Core 契约，不把装配逻辑写入 Console 模块（见 V1 实现计划）。

## 9. 宿主扩展实现

各组件（DAO、运行时配置、Contribution 注册表、Nacos 接入等）的扩展方式见 [10-host-extension-guide.md](10-host-extension-guide.md)。  
Quarkus 应用见 [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md)。

## 10. 数据库

表 `nx_plugin_installation` DDL 见 [`../design/plugin-installation-design.md`](../design/plugin-installation-design.md) §5。  
Flyway/Liquibase 由最终应用模块维护，Core 仅提供实体 `PluginInstallationEntity` 与 DAO 接口。
