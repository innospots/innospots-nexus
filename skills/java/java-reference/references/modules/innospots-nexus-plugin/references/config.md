# 包 `config`

插件配置 schema、宿主配置来源、类型化读取和密文包装。

## `ConfigDefinition`

**类型：** interface

一个插件允许使用的配置键不可变 schema。

### 方法

#### `items() → Collection<ConfigItemDefinition>`

按声明顺序返回不可变配置项集合。

#### `empty() → ConfigDefinition`

返回空 schema。

#### `builder() → ConfigDefinition.Builder`

创建流式 schema 构建器。

## `ConfigDefinition.Builder`

**类型：** class（public static final）

按声明顺序构建不可变 schema；重复 key 会抛出 `NexusException`。

### 方法

| 方法 | 说明 |
|------|------|
| `string(String key)` | 开始声明 `STRING` 项 |
| `integer(String key)` | 开始声明 `INTEGER` 项 |
| `longNumber(String key)` | 开始声明 `LONG` 项 |
| `bool(String key)` | 开始声明 `BOOLEAN` 项 |
| `duration(String key)` | 开始声明 ISO-8601 `DURATION` 项 |
| `decimal(String key)` | 开始声明 `DECIMAL` 项 |
| `uri(String key)` | 开始声明绝对 `URI` 项 |
| `enumeration(String key, String... enumValues)` | 开始声明 `ENUM` 项并设置允许值 |
| `secret(String key)` | 开始声明 `SECRET` 项 |
| `build()` | 返回不可变 schema 快照 |

## `ConfigDefinition.ItemBuilder`

**类型：** class（public static final）

单个配置项的流式构建器。

### 方法

| 方法 | 说明 |
|------|------|
| `required()` | 标记为必填 |
| `defaultValue(String defaultValue)` | 设置文本默认值；解析时按类型转换，`SECRET` 禁止设置 |
| `secret()` | 标记值在诊断信息中遮罩 |
| `description(String description)` | 设置面向用户的说明 |
| `enumValues(String... enumValues)` | 设置枚举允许值；非 `ENUM` 不得设置 |
| `end()` | 完成当前项并返回所属 `Builder` |

## `ConfigItemDefinition`

**类型：** record

描述一个插件本地配置键的不可变 schema 项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `key` | `String` | 小写驼峰，最长 128 字符 |
| `type` | `ConfigType` | 配置值类型 |
| `required` | `boolean` | 是否必须提供值 |
| `defaultValue` | `String` | 可选文本默认值；`SECRET` 禁止设置 |
| `secret` | `boolean` | 诊断信息是否遮罩；`SECRET` 强制为 `true` |
| `description` | `String` | 面向用户的说明；`null` 归一为空串 |
| `enumValues` | `List<String>` | `ENUM` 允许值；其它类型必须为空 |

### 构造方法

#### `ConfigItemDefinition(String key, ConfigType type, boolean required, String defaultValue, boolean secret, String description, List<String> enumValues)`

完整构造；校验 key、类型、枚举值、密文默认值等约束，不满足时抛出 `NexusException`。

#### `ConfigItemDefinition(String key, ConfigType type, boolean required, String defaultValue, boolean secret, String description)`

兼容构造；等价于声明空 `enumValues`。

## `ConfigSource`

**类型：** interface

宿主侧动态配置来源扩展点。实现由宿主提供，不得由插件 JAR 提供；每次插件启动解析配置时，
每个实例的 `values()` 调用一次。

### 方法

#### `name() → String`

返回诊断用来源名称。

#### `values() → Map<String, String>`

返回 `plugins.<pluginId>.<localKey>` 形式的不可变扁平快照。

## `ConfigType`

**类型：** enum

| 常量 | 说明 |
|------|------|
| `STRING` | 任意文本 |
| `INTEGER` | 32 位有符号整数 |
| `LONG` | 64 位有符号整数 |
| `BOOLEAN` | `true` / `false` |
| `DECIMAL` | 任意精度十进制数 |
| `DURATION` | ISO-8601 时长，例如 `PT30S` |
| `URI` | 绝对 URI |
| `ENUM` | 受 `enumValues` 限制的字符串 |
| `SECRET` | 敏感值；禁止默认值并以 `SecretValue` 读取 |

## `ConfigurationManager`

**类型：** class（final）

解析并校验一个插件命名空间内的配置。合并优先级从低到高：

```text
defaultValue
→ hostConfig
→ ConfigSource（按列表顺序）
→ NEXUS_PLUGIN_* 环境变量
→ JVM 系统属性
→ runtimeVariables
```

显式传入的 `hostConfig`、动态来源、系统属性和运行时映射若包含目标插件前缀下未声明
的 key，会抛出 `NexusException`。

### 构造方法

#### `ConfigurationManager(Map<String, String> hostConfig, List<ConfigSource> configSources, Map<String, String> environment, Map<String, String> systemProperties, Map<String, String> runtimeVariables)`

使用显式来源创建解析器；参数为 `null` 时归一为空集合，集合元素或键值不得为 `null`。

#### `ConfigurationManager(Map<String, String> hostConfig, Map<String, String> environment, Map<String, String> systemProperties, Map<String, String> runtimeVariables)`

兼容构造；不注册动态 `ConfigSource`。

### 方法

#### `standard(Map<String, String> hostConfig, Map<String, String> runtimeVariables) → ConfigurationManager`

使用当前进程环境变量和系统属性，不注册动态来源。

#### `standard(Map<String, String> hostConfig, List<ConfigSource> configSources, Map<String, String> runtimeVariables) → ConfigurationManager`

使用当前进程环境变量、系统属性和宿主动态来源。

#### `resolve(PluginDefinition definition) → PluginConfig`

解析并校验插件共享配置快照。出现未知键、缺少必填值或类型转换失败时抛出
`NexusException`。

#### `environmentName(String pluginId, String itemKey) → String`

生成确定性的环境变量名，例如
`com.example.reports` + `apiToken` →
`NEXUS_PLUGIN_COM_EXAMPLE_REPORTS_APITOKEN`。

#### `validateEnvironmentNames(Collection<PluginDefinition> definitions) → void`

校验同一 classpath 快照中不同逻辑键是否映射到同一环境变量名；冲突时抛出
`NexusException`。

## `PluginConfig`

**类型：** interface

限定在一个插件命名空间内的不可变、已校验配置视图。

### 方法

| 方法 | 说明 |
|------|------|
| `get(String key) → Optional<String>` | 读取字符串 |
| `require(String key) → String` | 读取必填字符串 |
| `getInt(String key, int defaultValue) → int` | 读取整数 |
| `getLong(String key, long defaultValue) → long` | 读取长整数 |
| `getBoolean(String key, boolean defaultValue) → boolean` | 读取布尔值 |
| `getDecimal(String key, BigDecimal defaultValue) → BigDecimal` | 读取十进制数 |
| `getDuration(String key, Duration defaultValue) → Duration` | 读取时长 |
| `getUri(String key, URI defaultValue) → URI` | 读取绝对 URI |
| `getEnum(String key, String defaultValue) → String` | 读取枚举文本 |
| `requireSecret(String key) → SecretValue` | 返回独立副本；调用方负责关闭 |

## `SecretValue`

**类型：** class（final，implements `AutoCloseable`）

可关闭的内存密文包装器；`toString()` 始终返回 `******`。

### 方法

#### `of(String value) → SecretValue`

从非空白明文创建副本；空白值抛出 `NexusException`。

#### `use(Function<char[], T> operation) → synchronized <T> T`

把字符的临时防御性副本交给操作，返回后清零该副本。

#### `copy() → synchronized SecretValue`

返回可独立关闭的密文副本。

#### `close() → synchronized void`

清零内部字符缓冲区；重复关闭保持幂等。

#### `toString() → String`

始终返回遮罩值，不输出明文。
