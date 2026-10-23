# Quarkus 宿主扩展指南

## 1. 使用已有模块

Quarkus 宿主优先使用：

- `innospots-nexus-quarkus-core`：配置映射、`ConfigSource` 收集、启动任务与安装管理器。
- `innospots-nexus-quarkus-console`：console Contribution 注册表与目录服务。

不要在 CDI 构造器中直接调用 `PluginHostBootstrap.enable(...)`。数据源、MyBatis 和
其他运行时依赖可能尚未就绪；使用已有的 `PluginHostStartupTask`。

## 2. 配置

```properties
nexus.plugin.auto-install=true
nexus.plugins.required=com.example.reports.console
nexus.plugins.disabled=

plugins.com.example.reports.console.endpoint=https://reports.example.com
plugins.com.example.reports.console.token=${REPORTS_TOKEN}
```

`PluginHostConfigBinder` 会把 `plugins.*` 扁平化为 `PluginRuntimeConfig.hostConfig()`。

## 3. 动态配置来源

```java
@ApplicationScoped
public class DatabasePluginConfigSource implements ConfigSource {

    private final PluginConfigRepository repository;

    @Inject
    public DatabasePluginConfigSource(PluginConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public String name() {
        return "database:nx_plugin_runtime_config";
    }

    @Override
    public Map<String, String> values() {
        return repository.loadAll();
    }
}
```

`PluginRuntimeConfigFactory` 会通过 `Instance<ConfigSource>` 自动收集。

## 4. console Contribution

`PluginContributionBeans` 已注册：

- `ConsolePluginContributionDecoder`
- `ConsolePluginContributionHandler`
- `ConsolePluginContributionSnapshotter`
- `ConsoleContributionCatalog`

通常不需要重复声明。只有新增自定义 Contribution 类型时才注册额外 Bean：

```java
@Produces
@Singleton
PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
    return PluginContributionDecoderRegistry.builder()
            .register(new ConsolePluginContributionDecoder())
            .register(new MyContributionDecoder())
            .build();
}

@Produces
@Singleton
PluginContributionHandlers pluginContributionHandlers(
        ConsoleContributionCatalog consoleCatalog,
        MyContributionStore store) {
    return new PluginContributionHandlers(List.of(
            new ConsolePluginContributionHandler(
                    consoleCatalog,
                    new ReservedPluginResourceCatalog(List.of("platform", "portal"))),
            new MyContributionHandler(store)));
}

@Produces
@Singleton
PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
    return PluginContributionSnapshotterRegistry.builder()
            .register(new ConsolePluginContributionSnapshotter())
            .register(new MyContributionSnapshotter())
            .build();
}
```

`PluginContributionHandlers` 是 CDI 包装类型，避免将参数化 `List<PluginContributionHandler<?>>`
直接作为 Bean 类型。

## 5. 安装 DAO

宿主提供 `PluginInstallationDao` 的 MyBatis Mapper 或等价实现。启动任务会把它注入
`PluginHostBootstrapRequest`。

## 6. 启动与关闭

```text
Quarkus StartupEvent
  -> PluginHostStartupTask
  -> PluginHostBootstrap.enable(request)
  -> PluginInstallationManagerHolder.setManager(...)

Quarkus ShutdownEvent / 容器销毁
  -> PluginInstallationManager.close()
```

不要手动创建第二个 `PluginInstallationManager` 或 `PluginManager`。

## 7. Entry 与配置边界

| 内容 | 归属 |
|------|------|
| 配置 schema | `ConfigDefinition` / YAML `spec.config` |
| MODULE/MENU/`entryPageKey` | `ConsoleContributeNode` / YAML `contributions` |
| 多 MODULE 常量 | `XxxModuleDescriptors` |
| pluginId 与 MODULE 列表绑定 | `XxxEntryPluginDescriptor` |
| 业务 REST | Quarkus 应用模块的 Jakarta REST 资源 |

具体规范见
[`java:develop` → plugin-entry-and-configuration.md](../../../../skills/java/java-develop/references/plugin-entry-and-configuration.md)。
