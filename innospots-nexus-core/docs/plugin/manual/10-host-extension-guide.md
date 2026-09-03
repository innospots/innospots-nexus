# 应用宿主扩展与实现指南

Core 只定义**契约与运行时**；最终应用（Spring Boot、Quarkus 等）负责把基础设施适配成 `PluginHostBootstrapRequest` 中的依赖。本文说明每类宿主组件**扩展什么、如何实现、何时需要**。

> **模块边界**：插件规范约束（`console@1` 声明模型、Decoder/Handler/Snapshotter、活动目录）在 **`innospots-nexus-core`**（`core.plugin.contribution.console`）；**`innospots-nexus-console`** 仅提供管理 REST（如 `PluginManagementEndpoint`）及 VO/Converter。

> 装配入口见 [09-host-assembly.md](09-host-assembly.md)；插件配置键与加载见 [08-configuration.md](08-configuration.md)。

## 1. 总览：宿主负责什么

```text
应用基础设施                宿主适配层                    Core 运行时
─────────────────          ─────────────────            ─────────────
MyBatis / JDBC      →      PluginInstallationDao  →     对账 / 安装意图
Spring/Nacos 配置   →      PluginRuntimeConfig      →     配置解析 / 启停策略
（无）              →      Contribution 注册表      →     YAML 发现 / 菜单发布
classpath           →      ClassLoader              →     SPI + plugin.yaml 扫描
```

**原则**

| 原则 | 说明 |
|------|------|
| Core 不读文件 | 不直接读 `application.properties`、Nacos、K8s ConfigMap |
| 宿主先合并 | 各配置源在应用层合并为 `Map<String, String>`，再传入 Core |
| 注册表显式扩展 | 新 Contribution 类型 = 注册 Decoder + Handler + Snapshotter |
| 进程单例 | Decoder/Handler/Snapshotter/Catalog 建议 `@Bean` 单例复用 |

## 2. 扩展点一览

| 组件 | 是否必须实现 | 扩展方式 | 典型实现方 |
|------|--------------|----------|------------|
| `PluginInstallationDao` | 生产必须 | MyBatis `BaseMapper` 子接口 | 应用 `mapper` 包 |
| `PluginRuntimeConfig` | 必须 | 从配置中心/文件组装 `Map` | `@Configuration` |
| `PluginInstallationConfig` | 必须 | 读 `nexus.plugin.auto-install` | 启动配置 |
| `PluginContributionDecoderRegistry` | 有 YAML contribution 时 | `builder().register(decoder)` | Console 模块 |
| `List<PluginContributionHandler<?>>` | 插件声明 contribution 时 | 注入 Handler 列表 | Console 模块 |
| `PluginContributionSnapshotterRegistry` | 插件声明 contribution 时 | `builder().register(snapshotter)` | Console 模块 |
| `ConsoleContributionCatalog` | 有 console 插件时 | 宿主单例 Bean | Console 模块 |
| `ClassLoader` | 可选 | 显式传入扫描 CL | 默认线程上下文 CL |

Capability 类型**无需**宿主注册表：由插件声明（YAML `api` 或 Java `CapabilityType`）在发现阶段自动登记。

## 3. PluginInstallationDao

### 3.1 职责

读写 `nx_plugin_installation`：安装事实、期望启用状态、运行诊断快照。

### 3.2 如何实现

Core 只声明接口；应用提供 MyBatis Mapper：

```java
@Mapper
public interface PluginInstallationMapper extends PluginInstallationDao {
}
```

启动时注入 Mapper，传入 `PluginHostBootstrapRequest.installationDao()`。

### 3.3 扩展边界

- **不要**在 DAO 层实现业务启停逻辑；启停由 `PluginInstallationManager` 协调。
- **可以**在应用层包装 DAO（审计、多租户过滤），但需保持 `PluginInstallationDao` 契约。

## 4. PluginRuntimeConfig

### 4.1 字段说明

```java
new PluginRuntimeConfig(
        Set<String> requiredPluginIds,      // 必须 ACTIVE，否则整批 start 失败
        Set<String> disabledPluginIds,      // 永不自动启动（与安装层 disabled 合并）
        Map<String, String> hostConfig,     // 插件配置扁平 Map（见 08-configuration）
        Map<String, String> runtimeVariables, // 最高优先级覆盖（测试/管理 API）
        Map<CapabilityKey, Tags> defaultRoutes, // Capability 默认路由
        ClassLoader pluginClassLoader       // null → 回退线程上下文 CL
);
```

### 4.2 如何实现 hostConfig

Core **不**解析配置文件。宿主在启动时把各来源合并后传入 `hostConfig`：

```java
@Configuration
class PluginHostConfiguration {

    @Bean
    PluginRuntimeConfig pluginRuntimeConfig(
            Environment env,
            @Value("${nexus.plugins.required:}") List<String> required,
            @Value("${nexus.plugins.disabled:}") List<String> disabled) {

        Map<String, String> hostConfig = new LinkedHashMap<>();

        // 1) Spring application.properties / yaml
        bindPluginKeys(env, "plugins.", hostConfig);

        // 2) Nacos / 配置中心（示例：已同步到 Environment）
        // bindPluginKeys(nacosPropertySource, hostConfig);

        return new PluginRuntimeConfig(
                Set.copyOf(required),
                Set.copyOf(disabled),
                Map.copyOf(hostConfig),
                Map.of(),                    // runtimeVariables：默认空
                defaultCapabilityRoutes(),   // 见 §4.4
                null);
    }

    private static void bindPluginKeys(Environment env, String prefix, Map<String, String> target) {
        for (var name : ((AbstractEnvironment) env).getPropertySources()) {
            // 简化示例：遍历所有以 plugins. 开头的属性
        }
        // 实际项目可用 ConfigurationProperties、自定义 Binder 或 Nacos @NacosValue 汇总
    }
}
```

详细合并规则与优先级见 [08-configuration.md](08-configuration.md)。

### 4.3 runtimeVariables 何时使用

| 场景 | 用法 |
|------|------|
| 单元测试 | 传入覆盖值，不污染全局 Environment |
| 管理 API 临时覆盖 | 重建 `PluginManager` 前写入 |
| 动态刷新（V1） | Core 不自动刷新；宿主需 `close()` 后重新 `enable()` 或自定义重建运行时 |

### 4.4 defaultRoutes

业务 `require(name, major, Tags.empty())` 时使用的默认标签：

```java
Map<CapabilityKey, Tags> defaultCapabilityRoutes() {
    return Map.of(
            new CapabilityKey("message.sender", 1),
            Tags.of("channel", "wecom"));
}
```

### 4.5 required / disabled 插件

- `requiredPluginIds`：安装层 eligible 且在此集合中的插件必须成功 ACTIVE。
- `disabledPluginIds`：与「未安装 / 未启用」合并后排除启动。
- `PluginRuntimeFactory` 会再把「不在 eligible 集合」的插件加入 disabled。

## 5. PluginInstallationConfig

### 5.1 职责

控制**首次 classpath 发现**时是否自动写入安装表并 `desiredEnabled=true`。

### 5.2 如何实现

```java
PluginInstallationConfig.from(
        environment.getProperty(PluginInstallationConfig.AUTO_INSTALL_KEY)); // nexus.plugin.auto-install
```

| 值 | 行为 |
|----|------|
| `false`（默认） | 新发现插件仅出现在发现报告，需管理命令安装 |
| `true` | 自动 `register` 为已安装且期望启用 |

此键属于**宿主安装策略**，不走 `plugins.*` 命名空间。

## 6. Contribution 三连：Decoder / Handler / Snapshotter

Console 菜单类插件需要同时注册三种组件；缺一会导致发现或启动失败。

### 6.1 Decoder（发现阶段）

**时机**：`ClasspathPluginDiscovery` 编译 YAML `contributions` 段。

```java
PluginContributionDecoderRegistry contributionDecoders =
        PluginContributionDecoderRegistry.builder()
                .register(new ConsolePluginContributionDecoder())
                .build();
```

**扩展新类型**：实现 `PluginContributionDecoder<T>`，在 `decode(Map)` 中把 YAML Map 转为强类型 `PluginContribution`。

### 6.2 Handler（运行阶段）

**时机**：`ManagedPlugin.start()` 内 prepare → stage → commit。

```java
ConsoleContributionCatalog catalog = new ConsoleContributionCatalog();
ConsolePluginContributionHandler handler = new ConsolePluginContributionHandler(
        catalog,
        new ReservedPluginResourceCatalog(List.of("platform", "kernel"))); // 保留前缀

List<PluginContributionHandler<?>> handlers = List.of(handler);
```

**扩展新类型**：实现 `PluginContributionHandler<T>`，声明 `type()`，实现 `validate` / `prepare` / `stage` / `commit` / `rollback`。

### 6.3 Snapshotter（对账阶段）

**时机**：`PluginInstallationManager.reconcile()` 写安装表 `definition_snapshot`。

```java
PluginContributionSnapshotterRegistry snapshotters =
        PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
```

**扩展新类型**：实现 `PluginContributionSnapshotter<T>`，输出可安全持久化的 JSON 摘要（不含 Secret）。

### 6.4 注册表一致性检查

`PluginRuntimeFactory.create()` 会校验：catalog 中每个 contribution 类型必须同时存在 Handler 与 Snapshotter。

## 7. ConsoleContributionCatalog

### 7.1 职责

保存当前 **ACTIVE** 插件发布的模块、页面、菜单树；供前端路由与权限同步读取。

### 7.2 如何实现

```java
@Bean
ConsoleContributionCatalog consoleContributionCatalog() {
    return new ConsoleContributionCatalog();
}
```

- Handler `commit()` 写入；`stop()` / rollback 撤出。
- 业务模块注入 Catalog 只读查询，**不要**绕过 Handler 直接修改。

## 8. ClassLoader

### 8.1 默认行为

`PluginHostBootstrapRequest.resolvedClassLoader()` 顺序：

1. `pluginClassLoader` 参数
2. `PluginRuntimeConfig.pluginClassLoader()`
3. `Thread.currentThread().getContextClassLoader()`
4. Core 模块 CL

### 8.2 何时显式指定

| 场景 | 建议 |
|------|------|
| 标准 Spring Boot  fat jar | 通常 `null`，使用应用 CL |
| 测试隔离 | `URLClassLoader` 只含测试插件 JAR |
| 未来独立插件目录（非 V1） | 专用 CL，父加载器为应用 CL |

## 9. 扩展新的 Contribution 类型（ checklist）

以假设的 `report@1` 为例：

1. **契约模块**：定义 `ReportPluginContribution implements PluginContribution`
2. **Decoder**：`ReportPluginContributionDecoder` → 注册到 `PluginContributionDecoderRegistry`
3. **Handler**：`ReportPluginContributionHandler` → 注册到 handlers 列表
4. **Snapshotter**：`ReportPluginContributionSnapshotter` → 注册到 snapshotter 表
5. **宿主状态**：Catalog 或等价存储（若需要运行时查询）
6. **插件声明**：YAML `contributions` 或 Java `contribute(...)`
7. **文档与测试**：`PluginDefinitionCompilerTest` 风格的最小样例

Core 不参与业务语义解析；所有字段校验在 Decoder/Handler 内完成。

## 10. ConfigSource（动态配置扩展）

`ConfigSource` 由宿主实现，注册在 `PluginRuntimeConfig.configSources()`。每次插件 **start** 解析配置时调用 `values()`，适合从数据库、Nacos 等加载 `appKey` 等运行期参数。

```java
return new PluginRuntimeConfig(
        required,
        disabled,
        staticFromYaml(),
        List.of(new DatabasePluginConfigSource(repository)),
        Map.of(),
        defaultRoutes,
        null);
```

完整实践（表结构、Repository、`appKey` 示例、热更新边界）见 [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md)。

## 11. 完整 Spring 装配骨架

```java
@Configuration
@EnableConfigurationProperties(NexusPluginProperties.class)
class NexusPluginAutoConfiguration {

    @Bean
    ConsoleContributionCatalog consoleContributionCatalog() {
        return new ConsoleContributionCatalog();
    }

    @Bean
    PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
        return PluginContributionDecoderRegistry.builder()
                .register(new ConsolePluginContributionDecoder())
                .build();
    }

    @Bean
    List<PluginContributionHandler<?>> pluginContributionHandlers(
            ConsoleContributionCatalog catalog) {
        return List.of(new ConsolePluginContributionHandler(
                catalog,
                new ReservedPluginResourceCatalog(List.of())));
    }

    @Bean
    PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
        return PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
    }

    @Bean
    PluginInstallationManager pluginInstallationManager(
            PluginInstallationDao installationDao,
            NexusPluginProperties properties,
            PluginContributionDecoderRegistry decoders,
            List<PluginContributionHandler<?>> handlers,
            PluginContributionSnapshotterRegistry snapshotters) {

        PluginRuntimeConfig runtimeConfig = new PluginRuntimeConfig(
                properties.requiredPluginIds(),
                properties.disabledPluginIds(),
                properties.flattenedPluginConfig(),
                List.of(new DatabasePluginConfigSource(pluginConfigRepository)),
                Map.of(),
                properties.defaultCapabilityRoutes(),
                null);

        return PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfig,
                PluginInstallationConfig.from(properties.autoInstall()),
                decoders,
                handlers,
                snapshotters,
                null));
    }

    @PreDestroy
    void shutdown(PluginInstallationManager manager) {
        manager.close();
    }
}
```

`NexusPluginProperties` 由应用自行定义，负责把 `application.yml`、Nacos 等汇总为 Core 需要的结构。

Quarkus 宿主（CDI、`@ConfigMapping`、`StartupEvent` / `ShutdownEvent`、JAX-RS 注册）见 [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md)。

## 12. 相关文档

| 文档 | 内容 |
|------|------|
| [08-configuration.md](08-configuration.md) | 插件配置键、来源、优先级、示例 |
| [09-host-assembly.md](09-host-assembly.md) | `enable()` 入口与 Request 字段 |
| [07-exposure-and-contribution.md](07-exposure-and-contribution.md) | `console@1` 声明与生命周期 |
| [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md) | Quarkus 宿主装配实践 |
