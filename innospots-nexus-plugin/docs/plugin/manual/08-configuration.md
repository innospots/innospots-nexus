# 插件配置说明

## 1. 两层配置

| 层次 | 定义位置 | 值来源 | Core 类型 |
|------|----------|--------|-----------|
| Schema | `plugin.yaml` 的 `spec.config` 或 Java `ConfigDefinition` | 插件 JAR，随发现加载 | `ConfigItemDefinition` |
| 运行值 | 不在插件 JAR 中 | 宿主静态配置、`ConfigSource`、环境变量、系统属性、运行时覆盖 | `PluginRuntimeConfig` → `ConfigurationManager` |

```text
PluginDefinition.config()
        + hostConfig / ConfigSource / env / system properties / runtimeVariables
        -> ConfigurationManager.resolve()
        -> PluginConfig
        -> PluginContext.config()
```

Core 不直接读取 `application.yml`、Nacos、Kubernetes ConfigMap 或其他配置文件。
宿主必须先汇总为扁平 `plugins.*` 键。

## 2. 配置键

| 配置 | 完整键 |
|------|--------|
| 插件共享配置 | `plugins.<pluginId>.<localKey>` |
| 安装策略 | `nexus.plugin.auto-install` |
| 启停策略 | `nexus.plugins.required` / `nexus.plugins.disabled` |

当前插件定义没有 Provider 私有配置层。所有插件配置都使用
`plugins.<pluginId>.<localKey>`。

## 3. Schema 声明

### 3.1 YAML

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.reports.console
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 报表
  config:
    - key: endpoint
      type: URI
      required: true
    - key: token
      type: SECRET
      required: true
    - key: timeout
      type: INTEGER
      default: 30
  contributions:
    - type: console
      majorVersion: 2
      domainKey: reports
      modules:
        - moduleKey: reports
          displayName:
            zh-CN: 报表
          entryPageKey: reports-reports-main
          children: []
```

### 3.2 Java

```java
ConfigDefinition config = ConfigDefinition.builder()
        .uri("endpoint").required().end()
        .secret("token").required().end()
        .integer("timeout").defaultValue("30").end()
        .build();

PluginDefinition definition = PluginDefinition.builder("com.example.reports.console")
        .version("1.0.0")
        .name("报表")
        .config(config)
        .contribute(contribution)
        .build();
```

### 3.3 类型

| `ConfigType` | YAML `type` | 说明 |
|--------------|-------------|------|
| `STRING` | `STRING` | 文本 |
| `INTEGER` / `LONG` | `INTEGER` / `LONG` | 整数 |
| `BOOLEAN` | `BOOLEAN` | 布尔 |
| `DECIMAL` | `DECIMAL` | 十进制定点 |
| `DURATION` | `DURATION` | ISO-8601 duration，如 `PT30S` |
| `URI` | `URI` | 绝对 URI |
| `ENUM` | `ENUM` | 必须声明 `enumValues` |
| `SECRET` | `SECRET` | 密文，不得声明默认值 |

## 4. 合并优先级

对每个已声明键按从低到高覆盖：

1. schema 默认值
2. `PluginRuntimeConfig.hostConfig`
3. `PluginRuntimeConfig.configSources`，按列表顺序叠加
4. 环境变量
5. JVM 系统属性
6. `PluginRuntimeConfig.runtimeVariables`

```text
default -> hostConfig -> configSources -> environment -> systemProperties -> runtimeVariables
```

`ConfigSource.values()` 在每个插件启动周期只调用一次，形成不可变配置快照。

## 5. 环境变量

Core 自动读取以下命名：

```text
NEXUS_PLUGIN_<PLUGIN_ID_AND_KEY>
```

插件 ID 与 local key 中的 `.` 和 `-` 替换为 `_`，并转为大写。例如：

```text
plugins.com.example.reports.console.endpoint
-> NEXUS_PLUGIN_COM_EXAMPLE_REPORTS_CONSOLE_ENDPOINT
```

生成 API 为 `ConfigurationManager.environmentName(pluginId, key)`。

## 6. 宿主接入

### 6.1 Spring

`innospots-nexus-spring-core` 的 `PluginHostConfigBinder` 会枚举 Spring Environment
中以 `plugins.` 开头的键。业务方通常只需提供配置值：

```yaml
plugins:
  com.example.reports.console:
    endpoint: https://reports.example.com
    token: ${REPORTS_TOKEN}
    timeout: 30
```

### 6.2 Quarkus

`innospots-nexus-quarkus-core` 的 `PluginHostConfigBinder` 会枚举 MicroProfile Config
中的 `plugins.*` 键。

### 6.3 动态来源

数据库或配置中心通过 `ConfigSource` 接入，见
[11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md)。

## 7. 代码读取

```java
@Override
public void initialize(PluginContext context) {
    URI endpoint = context.config().getUri("endpoint", null);
    int timeout = context.config().getInt("timeout", 30);
    try (SecretValue token = context.config().requireSecret("token")) {
        token.use(chars -> initializeClient(endpoint, chars));
    }
}
```

常用方法：

| 方法 | 用途 |
|------|------|
| `get` / `require` | 字符串 |
| `getInt` / `getLong` / `getBoolean` | 基础类型 |
| `getDecimal` / `getDuration` / `getUri` / `getEnum` | 结构化类型 |
| `requireSecret` | 可关闭的 `SecretValue` |

`PluginConfig` 是启动周期快照。ACTIVE 期间修改外部配置不会自动生效，需停用并重新启用插件。

## 8. 错误

| 错误 | 原因 |
|------|------|
| `unknown plugin config key` | 来源中出现 schema 未声明的键 |
| `required plugin config is missing` | 合并后必填项仍为空 |
| `plugin config environment name conflict` | 两个逻辑键映射到同一环境变量 |
| `secret config cannot have default` | YAML `SECRET` 声明了默认值 |
