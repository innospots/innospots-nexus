# 宿主装配

## 1. 推荐方式

优先使用仓库已有的框架聚合模块：

| 宿主 | 装配入口 | 已有组件 |
|------|----------|----------|
| Spring Boot | `@EnableNexusPluginHost` | `PluginRuntimeConfig`、启动任务、安装管理器持有器 |
| Quarkus | `innospots-nexus-quarkus-core` / `innospots-nexus-quarkus-console` | CDI 配置工厂、启动任务、console Contribution 注册表 |

不要在新宿主中重复创建已有 Bean。仅在需要自定义 `ConfigSource`、Contribution 类型
或数据库 DAO 时补充实现。

## 2. 启动请求

`PluginHostBootstrap.enable(...)` 需要：

| 字段 | 作用 |
|------|------|
| `PluginInstallationDao` | `nx_plugin_installation` 持久化 |
| `PluginRuntimeConfig` | required/disabled、hostConfig、ConfigSource、runtimeVariables、ClassLoader |
| `PluginInstallationConfig` | 首次发现是否自动安装 |
| `PluginContributionDecoderRegistry` | YAML Contribution 解码器 |
| `List<PluginContributionHandler<?>>` | Contribution 全局校验与事务准备 |
| `PluginContributionSnapshotterRegistry` | 安装定义快照 |
| `ClassLoader` | SPI 与 YAML 发现，可为空 |

```java
PluginHostBootstrapRequest request = new PluginHostBootstrapRequest(
        installationDao,
        runtimeConfig,
        new PluginInstallationConfig(true),
        decoderRegistry,
        contributionHandlers,
        snapshotterRegistry,
        Thread.currentThread().getContextClassLoader());

PluginInstallationManager manager = PluginHostBootstrap.enable(request);
```

## 3. console@2 必需注册表

只要插件声明 `console@2`，宿主就必须注册：

```java
ConsoleContributionCatalog catalog = new ConsoleContributionCatalog();

PluginContributionDecoderRegistry decoders = PluginContributionDecoderRegistry.builder()
        .register(new ConsolePluginContributionDecoder())
        .build();

PluginContributionSnapshotterRegistry snapshotters =
        PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();

List<PluginContributionHandler<?>> handlers = List.of(
        new ConsolePluginContributionHandler(
                catalog,
                new ReservedPluginResourceCatalog(List.of("platform", "portal"))));
```

Spring 的 `ConsolePluginConfiguration` 与 Quarkus 的 `PluginContributionBeans` 已提供这些
Bean，业务应用通常无需重复声明。

## 4. 自定义 Contribution

新增 Contribution 类型时同时提供：

1. 实现 `PluginContribution` 的不可变声明类型。
2. `PluginContributionDecoder<T>`，用于 YAML。
3. `PluginContributionHandler<T>`，用于全局校验与 `prepare`。
4. `PluginContributionSnapshotter<T>`，用于安装快照。
5. 框架模块中的注册代码。

Java 与 YAML 必须编译为同一种 Contribution 类型。缺少任一注册项时，运行时启动预检失败。

## 5. 配置接入

```java
PluginRuntimeConfig runtimeConfig = new PluginRuntimeConfig(
        Set.of("com.example.reports.console"),
        Set.of(),
        Map.of("plugins.com.example.reports.console.endpoint",
                "https://reports.example.com"),
        List.of(databaseConfigSource),
        Map.of(),
        null);
```

键命名、优先级和读取方式见 [08-configuration.md](08-configuration.md)。

## 6. 管理入口

```java
manager.plugins();
manager.installAndStart("com.example.reports.console");
manager.disable("com.example.reports.console");
manager.retryStart("com.example.reports.console");
manager.close();
```

`PluginInstallationManager.close()` 会关闭其拥有的运行时。宿主容器必须保证只关闭一次，
且应在所有依赖插件的业务 Bean 停止后执行。

## 7. Entry 与配置职责

| 内容 | 归属 |
|------|------|
| 插件配置 schema | `ConfigDefinition` / YAML `spec.config` |
| 菜单、MODULE、`entryPageKey`、权限分组 | `ConsoleContributeNode` / YAML `contributions` |
| 多模块 MODULE 常量与顺序 | `XxxModuleDescriptors` |
| pluginId、显示文本与 MODULE 列表绑定 | `XxxEntryPluginDescriptor` |
| 业务 REST | 宿主模块的 `endpoint` 包 |

具体写法见
[`java:develop` → plugin-entry-and-configuration.md](../../../../skills/java/java-develop/references/plugin-entry-and-configuration.md)。
