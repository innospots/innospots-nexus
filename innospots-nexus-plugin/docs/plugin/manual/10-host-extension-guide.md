# Spring 宿主扩展指南

## 1. 使用已有装配

Spring 宿主优先引入 `innospots-nexus-spring-core` 与
`innospots-nexus-spring-console`，并使用：

```java
@EnableNexusPluginHost
```

该入口已经提供：

- `PluginRuntimeConfig`：合并 `nexus.plugin.*`、`nexus.plugins.*` 与 `plugins.*`。
- `PluginHostStartupTask`：按 Nexus 启动顺序执行 `PluginHostBootstrap.enable(...)`。
- `PluginInstallationManager`：启动完成后延迟暴露。
- console 的 Decoder、Handler、Snapshotter 与 `ConsoleContributionCatalog`。

应用不需要手写这些 Bean。

## 2. 配置命名空间

```yaml
nexus:
  plugin:
    auto-install: true
  plugins:
    required:
      - com.example.reports.console
    disabled: []

plugins:
  com.example.reports.console:
    endpoint: https://reports.example.com
    token: ${REPORTS_TOKEN}
```

| 键 | 用途 |
|----|------|
| `nexus.plugin.auto-install` | 首次发现是否自动安装 |
| `nexus.plugins.required` | 启动后必须 ACTIVE 的插件 |
| `nexus.plugins.disabled` | 排除自动启动的插件 |
| `plugins.<pluginId>.<key>` | 插件配置值 |

## 3. 增加动态配置来源

```java
@Bean
ConfigSource databasePluginConfigSource(PluginConfigRepository repository) {
    return new ConfigSource() {
        @Override
        public String name() {
            return "database:nx_plugin_runtime_config";
        }

        @Override
        public Map<String, String> values() {
            return repository.loadAll();
        }
    };
}
```

`PluginHostConfiguration` 会收集全部 `ConfigSource` Bean，并按声明顺序传入
`PluginRuntimeConfig.configSources()`。优先级见 [08-configuration.md](08-configuration.md)。

## 4. 增加自定义 Contribution

自定义类型必须同时提供 Decoder、Handler 和 Snapshotter Bean：

```java
@Bean
PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
    return PluginContributionDecoderRegistry.builder()
            .register(new ConsolePluginContributionDecoder())
            .register(new MyContributionDecoder())
            .build();
}

@Bean
List<PluginContributionHandler<?>> pluginContributionHandlers(
        ConsoleContributionCatalog consoleCatalog,
        MyContributionStore store) {
    return List.of(
            new ConsolePluginContributionHandler(
                    consoleCatalog,
                    new ReservedPluginResourceCatalog(List.of("platform", "portal"))),
            new MyContributionHandler(store));
}

@Bean
PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
    return PluginContributionSnapshotterRegistry.builder()
            .register(new ConsolePluginContributionSnapshotter())
            .register(new MyContributionSnapshotter())
            .build();
}
```

不要创建第二个同类型 Handler；运行时会在预检阶段拒绝重复注册。

## 5. 安装 DAO

宿主必须提供 `PluginInstallationDao`。它负责 `nx_plugin_installation` 的读写；
`PluginHostBootstrapRequest` 在缺少 DAO 时立即失败。

## 6. Entry 装配

多 MODULE 的宿主模块建议使用：

- `XxxModuleDescriptors`：集中声明 MODULE 常量与稳定顺序。
- `XxxEntryPluginDescriptor`：绑定 pluginId、显示文本和 MODULE 列表。
- `XxxModuleEntrySupport`：调用 `ConsoleContributions.entryPlugin(...)`。
- `XxxConsoleEntryPlugin`：实现 `Plugin.definition()`。

规范见
[`java:develop` → plugin-entry-and-configuration.md](../../../../skills/java/java-develop/references/plugin-entry-and-configuration.md)。

## 7. 验收

- 启动日志中出现一次插件宿主启用。
- 必需插件最终为 `ACTIVE`。
- `console@2` Contribution 已提交，且没有资源键冲突。
- `plugins.*` 中的未声明键在启动时被拒绝。
- `PluginConfig` 修改后通过停用/启用插件生效，不期待 ACTIVE 期间热更新。
