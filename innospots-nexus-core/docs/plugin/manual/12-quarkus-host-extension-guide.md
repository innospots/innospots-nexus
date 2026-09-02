# Quarkus 宿主扩展实践指南

本文说明如何在 **Quarkus** 应用中装配 Nexus 插件子系统：把 JDBC、MicroProfile Config、CDI 生命周期适配为 `PluginHostBootstrapRequest` 依赖。

> 扩展点语义与 Spring 版一致，见 [10-host-extension-guide.md](10-host-extension-guide.md)。  
> 动态配置（数据库 `appKey`）见 [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md)。  
> 装配入口见 [09-host-assembly.md](09-host-assembly.md)。

## 1. 模块依赖

在最终应用模块（如 `innospots-nexus-app`）的 `pom.xml` 中引入：

```xml
<dependencies>
    <!-- Nexus 插件运行时 -->
    <dependency>
        <groupId>com.innospots.nexus</groupId>
        <artifactId>innospots-nexus-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.innospots.nexus</groupId>
        <artifactId>innospots-nexus-console</artifactId>
    </dependency>

    <!-- Quarkus 基础 -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-arc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-agroal</artifactId>
    </dependency>

    <!-- 任选其一：MyBatis 或纯 JDBC -->
    <dependency>
        <groupId>io.quarkiverse.mybatis</groupId>
        <artifactId>quarkus-mybatis</artifactId>
    </dependency>
</dependencies>
```

**原则**：Core / Console 模块本身不依赖 Quarkus；所有 Quarkus 适配代码放在**应用模块**。

## 2. 与 Spring 的对照

| 职责 | Spring Boot | Quarkus |
|------|-------------|---------|
| 单例 Bean | `@Bean` / `@Component` | `@ApplicationScoped` |
| 配置绑定 | `@ConfigurationProperties` | `@ConfigMapping` / `@ConfigProperty` |
| 启动钩子 | `@PostConstruct` / `ApplicationRunner` | `StartupEvent` + `void onStart(@Observes StartupEvent)` |
| 关闭钩子 | `@PreDestroy` | `ShutdownEvent` + `void onStop(@Observes ShutdownEvent)` |
| REST 资源 | `@RestController` 或注册 Bean | `@Path` 类 + CDI Bean（或 `@RegisterRestClient` 不适用此处） |
| 数据源 | `DataSource` Bean | Agroal（`quarkus.datasource.*`） |
| MyBatis Mapper | `@MapperScan` | `quarkus.mybatis.xmlconfig` + `@Mapper` 接口 |

## 3. application.properties 示例

```properties
# 数据源
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=nexus
quarkus.datasource.password=secret
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/nexus

# 安装策略（非 plugins.* 命名空间）
nexus.plugin.auto-install=false

# 必须 ACTIVE 的插件（逗号分隔）
nexus.plugins.required=com.example.message-wecom

# 永不自动启动
nexus.plugins.disabled=

# 静态插件配置（扁平键，见 08-configuration.md）
plugins.com.example.message-wecom.corpId=ww-from-properties
```

Quarkus 会把 `plugins.*` 键暴露为 `Config` API 可枚举的属性；宿主在启动时汇总为 `hostConfig` Map（见 §5）。

## 4. 配置映射（ConfigMapping）

```java
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "nexus")
public interface NexusPluginHostConfig {

    @WithName("plugin.auto-install")
    @WithDefault("false")
    boolean autoInstall();

    Plugins plugins();

    interface Plugins {
        @WithDefault("")
        List<String> required();

        @WithDefault("")
        List<String> disabled();
    }
}
```

`plugins.com.example.*` 等扁平键**不适合**全部映射到强类型接口；推荐在启动时从 `Config` 枚举：

```java
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public final class PluginHostConfigBinder {

    private static final String PLUGIN_PREFIX = "plugins.";

    private PluginHostConfigBinder() {
    }

    public static Map<String, String> flattenPluginConfig() {
        Config config = ConfigProvider.getConfig();
        Map<String, String> hostConfig = new LinkedHashMap<>();
        for (String name : config.getPropertyNames()) {
            if (name.startsWith(PLUGIN_PREFIX)) {
                config.getOptionalValue(name, String.class)
                        .ifPresent(value -> hostConfig.put(name, value));
            }
        }
        return Map.copyOf(hostConfig);
    }
}
```

## 5. PluginInstallationDao（MyBatis）

Core 只声明接口；应用提供 Mapper：

```java
import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;

@Mapper
public interface PluginInstallationMapper extends PluginInstallationDao {
}
```

`application.properties` 补充：

```properties
quarkus.mybatis.xmlconfig=mybatis-config.xml
```

确保 `mybatis-config.xml` 扫描到 `PluginInstallationMapper` 所在包。Mapper 由 Quarkus MyBatis 扩展自动注册为 CDI Bean，可直接 `@Inject PluginInstallationDao`。

## 6. ConfigSource：数据库动态配置

实现与框架无关，注册在 `PluginRuntimeConfig.configSources()`：

```java
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.innospots.nexus.core.plugin.config.ConfigSource;

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

`PluginConfigRepository` 可用 Agroal `DataSource` + JDBC 实现（示例见 [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md) §5.2）。  
在 Quarkus 中标注 `@ApplicationScoped` 即可注入。

## 7. Contribution 注册表（CDI 单例）

```java
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.plugin.contribution.ConsoleContributionCatalog;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionDecoder;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionHandler;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.console.plugin.contribution.ReservedPluginResourceCatalog;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;

@ApplicationScoped
public class PluginContributionBeans {

  @Produces
  @Singleton
  ConsoleContributionCatalog consoleContributionCatalog() {
      return new ConsoleContributionCatalog();
  }

  @Produces
  @Singleton
  PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
      return PluginContributionDecoderRegistry.builder()
              .register(new ConsolePluginContributionDecoder())
              .build();
  }

  @Produces
  @Singleton
  PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
      return PluginContributionSnapshotterRegistry.builder()
              .register(new ConsolePluginContributionSnapshotter())
              .build();
  }

  @Produces
  @Singleton
  List<PluginContributionHandler<?>> pluginContributionHandlers(
          ConsoleContributionCatalog catalog) {
      return List.of(new ConsolePluginContributionHandler(
              catalog,
              new ReservedPluginResourceCatalog(List.of("platform", "kernel"))));
  }
}
```

也可直接在类上使用 `@ApplicationScoped` + 构造器注入，不必全部 `@Produces`。

## 8. PluginRuntimeConfig 组装

```java
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

@ApplicationScoped
public class PluginRuntimeConfigFactory {

    private final NexusPluginHostConfig hostConfig;
    private final DatabasePluginConfigSource databaseConfigSource;

    @Inject
    public PluginRuntimeConfigFactory(
            NexusPluginHostConfig hostConfig,
            DatabasePluginConfigSource databaseConfigSource) {
        this.hostConfig = hostConfig;
        this.databaseConfigSource = databaseConfigSource;
    }

    public PluginRuntimeConfig create() {
        return new PluginRuntimeConfig(
                Set.copyOf(hostConfig.plugins().required()),
                Set.copyOf(hostConfig.plugins().disabled()),
                PluginHostConfigBinder.flattenPluginConfig(),
                List.of(databaseConfigSource),
                Map.of(),
                Map.of(),
                null);
    }
}
```

合并优先级见 [08-configuration.md](08-configuration.md) 与 [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md)。

## 9. 启动与关闭（StartupEvent / ShutdownEvent）

**不要在** CDI 构造器中调用 `PluginHostBootstrap.enable()`：此时数据源、MyBatis 可能尚未就绪。

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

@ApplicationScoped
public class PluginHostLifecycle {

    private final PluginInstallationDao installationDao;
    private final PluginRuntimeConfigFactory runtimeConfigFactory;
    private final NexusPluginHostConfig hostConfig;
    private final PluginContributionDecoderRegistry decoders;
    private final PluginContributionSnapshotterRegistry snapshotters;
    private final java.util.List<PluginContributionHandler<?>> handlers;

    private volatile PluginInstallationManager installationManager;

    @Inject
    public PluginHostLifecycle(
            PluginInstallationDao installationDao,
            PluginRuntimeConfigFactory runtimeConfigFactory,
            NexusPluginHostConfig hostConfig,
            PluginContributionDecoderRegistry decoders,
            PluginContributionSnapshotterRegistry snapshotters,
            java.util.List<PluginContributionHandler<?>> handlers) {
        this.installationDao = installationDao;
        this.runtimeConfigFactory = runtimeConfigFactory;
        this.hostConfig = hostConfig;
        this.decoders = decoders;
        this.snapshotters = snapshotters;
        this.handlers = handlers;
    }

    void onStart(@Observes StartupEvent event) {
        installationManager = PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfigFactory.create(),
                PluginInstallationConfig.from(
                        hostConfig.autoInstall() ? "true" : "false"),
                decoders,
                handlers,
                snapshotters,
                Thread.currentThread().getContextClassLoader()));
    }

    void onStop(@Observes ShutdownEvent event) {
        if (installationManager != null) {
            installationManager.close();
        }
    }

    public PluginInstallationManager installationManager() {
        PluginInstallationManager manager = installationManager;
        if (manager == null) {
            throw new IllegalStateException("plugin subsystem has not started yet");
        }
        return manager;
    }
}
```

| 注意 | 说明 |
|------|------|
| 启动顺序 | `StartupEvent` 在 Quarkus 应用就绪后触发，适合 enable |
| 类加载器 | 传 `Thread.currentThread().getContextClassLoader()` 或 `null` |
| 热重载 dev 模式 | `quarkus:dev` 可能多次触发；生产环境只执行一次 |
| 失败快速退出 | `enable()` 抛错时 Quarkus 启动失败，符合「required 插件必须 ACTIVE」语义 |

## 10. 注入业务代码与 REST

### 10.1 Capability 查询

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.innospots.nexus.core.plugin.capability.CapabilityManager;
import com.innospots.nexus.core.plugin.capability.Tags;

@ApplicationScoped
public class MessageDispatchService {

    private final PluginHostLifecycle pluginHost;

    @Inject
    public MessageDispatchService(PluginHostLifecycle pluginHost) {
        this.pluginHost = pluginHost;
    }

    public void send(String to, String body) {
        CapabilityManager capabilities = pluginHost.installationManager().capabilities();
        var sender = capabilities.require("message.sender", 1, Tags.empty());
        // 调用 sender ...
    }
}
```

### 10.2 PluginManagementEndpoint（JAX-RS）

`PluginManagementEndpoint` 已是 Jakarta REST 类，注册为 CDI Bean 即可被 Quarkus 扫描：

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;

@ApplicationScoped
public class PluginManagementResourceProducer {

  @Produces
  @ApplicationScoped
  PluginManagementEndpoint pluginManagementEndpoint(PluginHostLifecycle pluginHost) {
      return new PluginManagementEndpoint(pluginHost.installationManager());
  }
}
```

或在 `application.properties` 中确保 REST 扫描包含 `com.innospots.nexus.console.plugin.endpoint` 包。

### 10.3 ConsoleContributionCatalog

前端路由 / 权限同步需要读取已发布菜单时，注入 `ConsoleContributionCatalog`（§7 已 `@Produces`）。

## 11. 完整装配时序

```text
Quarkus 启动
  → Agroal / MyBatis 就绪
  → StartupEvent
      → PluginRuntimeConfigFactory.create()
          → flattenPluginConfig()          # application.properties
          → DatabasePluginConfigSource     # 查库（每次插件 start 时 values()）
      → PluginHostBootstrap.enable(request)
          → ClasspathPluginDiscovery
          → reconcile (PluginInstallationDao)
          → start eligible plugins
  → REST / 业务 Service 可用
      → installationManager.capabilities()
      → PluginManagementEndpoint

ShutdownEvent
  → installationManager.close()
```

## 12. 测试

### 12.1 @QuarkusTest

```java
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

@QuarkusTest
class PluginHostQuarkusTest {

    @Inject
    PluginHostLifecycle pluginHost;

    @Test
    void pluginsAreEnabledAfterStartup() {
        PluginInstallationManager manager = pluginHost.installationManager();
        assertThat(manager.plugins()).isNotEmpty();
    }
}
```

测试 profile 可用 `src/test/resources/application.properties` 覆盖 `nexus.plugin.auto-install=true`，并通过 `runtimeVariables` 或测试专用 `ConfigSource` 注入配置（见 [08-configuration.md](08-configuration.md) §7）。

### 12.2 无 Quarkus 的单元测试

Core 自带 `PluginHostBootstrapTest`；不启动 Quarkus 时可直接 `new PluginHostBootstrapRequest(...)` 做嵌入式测试（见 [09-host-assembly.md](09-host-assembly.md) §7）。

## 13. 常见问题

| 现象 | 原因与处理 |
|------|------------|
| `plugin subsystem has not started yet` | 在 `StartupEvent` 之前注入了 `PluginHostLifecycle`；改为事件后访问或 `@Startup` 顺序 |
| MyBatis Mapper 未注入 | 检查 `quarkus.mybatis` 配置与 `@Mapper` 包扫描 |
| `plugins.*` 未进入 hostConfig | `flattenPluginConfig()` 未执行或键名不以 `plugins.` 开头 |
| 改了 DB 配置不生效 | V1 需 `disable` → `enable` 重启插件；见 [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md) §6 |
| dev 模式重复 enable | 使用 `@Singleton` 持有 `PluginInstallationManager`，或在 `onStart` 中判断已初始化 |
| Native 镜像 | 插件 SPI / `plugin.yaml` 需注册 `reflect-config` / `resource-config`；V1 以 JVM 模式为主 |

## 14. 相关文档

| 文档 | 内容 |
|------|------|
| [10-host-extension-guide.md](10-host-extension-guide.md) | 扩展点语义（框架无关） |
| [09-host-assembly.md](09-host-assembly.md) | `PluginHostBootstrapRequest` 字段说明 |
| [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md) | 数据库 ConfigSource 实践 |
| [08-configuration.md](08-configuration.md) | 配置键与优先级 |
