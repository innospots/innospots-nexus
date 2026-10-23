# 动态插件配置（ConfigSource）

## 1. 适用场景

`ConfigSource` 适合在每次插件启动时从数据库、配置中心或密钥服务读取配置。

不适合 ACTIVE 期间实时热更新。当前实现不支持运行中刷新 `PluginConfig`。

`ConfigSource` 由宿主实现和注册，不由插件 JAR 提供。

## 2. 接口

```java
public interface ConfigSource {

    String name();

    Map<String, String> values();
}
```

`values()` 返回完整扁平键：

```text
plugins.<pluginId>.<localKey>
```

返回映射应不可变。每个插件启动周期只调用一次。

## 3. 合并优先级

从低到高：

```text
1. schema 默认值
2. hostConfig
3. ConfigSource（按注册顺序叠加）
4. 环境变量 NEXUS_PLUGIN_*
5. JVM System Property
6. runtimeVariables
```

## 4. 数据库示例

表结构可以按宿主需要设计，例如：

```sql
CREATE TABLE nx_plugin_runtime_config (
    plugin_id    VARCHAR(128) NOT NULL,
    config_key   VARCHAR(128) NOT NULL,
    config_value TEXT         NOT NULL,
    secret       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (plugin_id, config_key)
);
```

`config_key` 保存插件 local key，例如 `endpoint`、`token`。

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

仓储负责转换为完整键：

```java
String fullKey = "plugins." + pluginId + "." + configKey;
```

## 5. 注册

### 5.1 Spring

将 `ConfigSource` 注册为 Bean。`PluginHostConfiguration` 会自动收集：

```java
@Bean
ConfigSource pluginConfigSource(PluginConfigRepository repository) {
    return new DatabasePluginConfigSource(repository);
}
```

### 5.2 Quarkus

实现为 CDI Bean：

```java
@ApplicationScoped
public class DatabasePluginConfigSource implements ConfigSource {
    // ...
}
```

`PluginRuntimeConfigFactory` 通过 `Instance<ConfigSource>` 收集全部实现。

## 6. 插件侧读取

插件只声明 schema，不关心值来自哪里：

```java
@Override
public void initialize(PluginContext context) {
    String endpoint = context.config().require("endpoint");
    try (SecretValue token = context.config().requireSecret("token")) {
        token.use(chars -> initializeClient(endpoint, chars));
    }
}
```

## 7. 刷新策略

| 需求 | 当前做法 |
|------|----------|
| 修改配置后重启插件 | `disable(pluginId)` 后 `enable(pluginId)` |
| 重建整个运行时 | `close()` 后重新 `PluginHostBootstrap.enable(...)` |
| 每次业务调用读取外部配置 | 由宿主业务服务提供，不使用 `PluginConfig` |

## 8. 排错

| 现象 | 原因 |
|------|------|
| 未声明键错误 | `ConfigSource` 返回了 schema 之外的键 |
| 必填项缺失 | 所有来源合并后仍无值 |
| 修改数据库后不生效 | 插件未重新启动 |
| 密文出现在日志中 | 代码直接输出原值；应使用 `SecretValue` |
