# 运行期动态配置实践（ConfigSource）

本文说明如何在**插件启动时**从数据库、配置中心等非静态来源加载配置，并通过 Core 提供的 `ConfigSource` 扩展接口接入。

> 静态文件 / Nacos 汇总见 [08-configuration.md](08-configuration.md)；宿主组件总览见 [10-host-extension-guide.md](10-host-extension-guide.md)。

## 1. 适用场景

| 场景 | 是否适合 ConfigSource |
|------|------------------------|
| `appKey`、密钥存在业务库，按租户/环境不同 | ✓ |
| 运维在管理台修改插件参数，重启插件后生效 | ✓ |
| 插件运行中每秒热更新（不重启） | ✗ 见 §6 |
| 插件 JAR 自带默认值 | 用 schema `defaultValue` |

**原则**：`ConfigSource` 由**应用宿主**实现并注册，**不由插件 JAR 提供**，避免配置加载与 `PluginManager` 启动循环依赖。

## 2. 扩展接口

```java
public interface ConfigSource {

    String name();

    /** 每次插件启动解析配置时调用，应返回不可变 Map */
    Map<String, String> values();
}
```

键名必须是完整扁平键：

```text
plugins.<pluginId>.<localKey>
plugins.<pluginId>.providers.<providerId>.<localKey>
```

与 [08-configuration.md](08-configuration.md) §2 一致。

## 3. 合并优先级（完整六级）

从低到高：

```text
1. Schema 默认值（插件定义）
2. hostConfig（application.yml 等静态宿主配置）
3. ConfigSource（按注册列表顺序依次叠加）  ← 动态来源
4. 环境变量 NEXUS_PLUGIN_*
5. JVM System Property
6. runtimeVariables（最高）
```

同一插件键在多个 `ConfigSource` 中出现时，**列表靠后的覆盖靠前的**。

## 4. 注册方式

在 `PluginRuntimeConfig` 中注册（推荐在 Spring `@Bean` 中组装）：

```java
@Bean
PluginRuntimeConfig pluginRuntimeConfig(
        PluginConfigRepository configRepository,
        @Value("${nexus.plugins.required:}") List<String> required) {

    ConfigSource databaseSource = new DatabasePluginConfigSource(configRepository);

    return new PluginRuntimeConfig(
            Set.copyOf(required),
            Set.of(),
            Map.of(),                    // hostConfig：可为空，仅用数据库
            List.of(databaseSource),     // configSources
            Map.of(),                    // runtimeVariables
            Map.of(),
            null);
}
```

传入 `PluginHostBootstrap.enable(...)` 的 `PluginHostBootstrapRequest.runtimeConfig()` 即可，无需改 Request 结构。

## 5. 实践：appKey 存数据库

### 5.1 表结构示例

```sql
CREATE TABLE nx_plugin_runtime_config (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    plugin_id    VARCHAR(128) NOT NULL,
    config_key   VARCHAR(128) NOT NULL,
    config_value TEXT         NOT NULL,
    secret       TINYINT(1)   NOT NULL DEFAULT 0,
  UNIQUE KEY uk_plugin_key (plugin_id, config_key)
);

-- 示例数据
INSERT INTO nx_plugin_runtime_config (plugin_id, config_key, config_value, secret) VALUES
('com.example.message-wecom', 'corpId', 'ww123456', 0),
('com.example.message-wecom', 'secret', 'encrypted-or-plain', 1),
('com.example.message-wecom', 'providers.wecom.agentId', '1000002', 0);
```

说明：

- `config_key` 为插件内 **localKey**；Provider 级用 `providers.<providerId>.<localKey>` 路径（与 Core 命名空间一致，不含 `plugins.` 前缀）。
- 生产环境密文应加密存储；读出后仍通过 `SECRET` schema 项以 `SecretValue` 交给插件。

### 5.2 仓储接口

```java
public interface PluginConfigRepository {

    /**
     * @return 扁平化完整键 plugins.{pluginId}.{localKey} → value
     */
    Map<String, String> loadAll();
}
```

```java
@Repository
public class JdbcPluginConfigRepository implements PluginConfigRepository {

    private final JdbcTemplate jdbc;

    public JdbcPluginConfigRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<String, String> loadAll() {
        return jdbc.query(
                """
                SELECT plugin_id, config_key, config_value
                FROM nx_plugin_runtime_config
                """,
                rs -> {
                    Map<String, String> values = new LinkedHashMap<>();
                    while (rs.next()) {
                        String fullKey = "plugins." + rs.getString("plugin_id") + "."
                                + rs.getString("config_key");
                        values.put(fullKey, rs.getString("config_value"));
                    }
                    return Map.copyOf(values);
                });
    }
}
```

### 5.3 实现 ConfigSource

```java
public final class DatabasePluginConfigSource implements ConfigSource {

    private final PluginConfigRepository repository;

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

### 5.4 插件侧声明（不变）

YAML：

```yaml
spec:
  config:
    - key: corpId
      type: STRING
      required: true
    - key: secret
      type: SECRET
      required: true
  capabilities:
    - type: message.sender
      majorVersion: 1
      providerId: wecom
      api: com.example.contract.MessageSender
      config:
        - key: agentId
          type: STRING
          required: true
      bind:
        kind: java
        class: com.example.WeComSender
```

Java 读取：

```java
@Override
public void initialize(PluginContext context) {
    String corpId = context.config().require("corpId");
    try (SecretValue secret = context.config().requireSecret("secret")) {
        secret.use(chars -> initClient(corpId, chars));
    }
}
```

### 5.5 调用时机

```text
PluginInstallationManager.start()
  → DefaultPluginManager.start()
    → ManagedPlugin.start()
      → config-resolve
        → ConfigurationManager.resolve()
          → 对每个 ConfigSource 调用 values()   ← 此时查库
          → 生成 PluginConfig 快照
      → plugin.initialize(context)
```

因此：

- **每次插件 start / retryStart** 都会重新查库；
- 同一启动周期内 `context.config()` 是**不可变快照**，多次读取不会再次查库。

## 6. 运行中热更新（V1 边界）

V1 **不支持**插件 ACTIVE 期间自动刷新 `PluginConfig`。可选方案：

| 方案 | 做法 |
|------|------|
| **重启插件（推荐）** | 管理台更新 DB → `installationManager.disable(id)` → `enable(id)` |
| **业务服务注入** | 插件 `initialize` 时从宿主注入 `TenantCredentialService`，每次业务能力调用时查库（绕过 PluginConfig） |
| **重建运行时** | 修改 DB 后 `installationManager.close()` 再 `PluginHostBootstrap.enable()` |

若采用业务服务注入，敏感逻辑应放在宿主模块，插件只依赖接口，不把 JDBC 打进插件 JAR。

## 7. 多来源组合示例

```java
return new PluginRuntimeConfig(
        required,
        disabled,
        propertiesFromApplicationYaml(),           // 静态默认
        List.of(
                new DatabasePluginConfigSource(repo),
                new NacosPluginConfigSource(nacosClient)), // 后者覆盖前者
        Map.of(),                                  // 测试覆盖
        defaultRoutes,
        null);
```

Nacos 实现同样实现 `ConfigSource`，在 `values()` 中拉取并扁平化为 `plugins.*` 键。

## 8. 校验与排错

| 现象 | 原因 |
|------|------|
| `unknown plugin config key` | DB 中有 schema 未声明的键 |
| `required ... missing` | DB 缺必填项，且 hostConfig/环境变量也未提供 |
| 改了 DB 但插件仍用旧值 | 插件未 restart；`PluginConfig` 是启动快照 |
| 两个插件环境变量冲突 | `validateEnvironmentNames` 失败；检查 pluginId 命名 |

开启 DEBUG 日志可在 `ManagedPlugin` 的 `config-resolve` 阶段确认配置来源是否执行。

## 9. 与安装策略配置的区别

| 配置 | 存储 | 接入方式 |
|------|------|----------|
| `nexus.plugin.auto-install` | 应用配置 | `PluginInstallationConfig` |
| 插件 `corpId` / `appKey` | DB / Nacos | `ConfigSource` → `PluginRuntimeConfig` |

不要把插件业务配置写入 `nexus.plugin.*` 安装策略键。

## 10. 相关文档

| 文档 | 内容 |
|------|------|
| [08-configuration.md](08-configuration.md) | 键命名、静态来源、优先级 |
| [10-host-extension-guide.md](10-host-extension-guide.md) | PluginRuntimeConfig 字段说明 |
| [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md) | Quarkus 中注册 ConfigSource 与启动顺序 |
| [05-runtime-lifecycle.md](05-runtime-lifecycle.md) | config-resolve 在启动顺序中的位置 |
