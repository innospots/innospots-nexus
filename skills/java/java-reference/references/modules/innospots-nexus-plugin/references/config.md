# 包 `config`

## ConfigDefinition

**类型：** interface

一个插件允许使用的配置键不可变 schema。

### 方法

#### `string(String key) → ItemBuilder`
- **说明：** 按声明顺序返回配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `integer(String key) → ItemBuilder`
- **说明：** 开始声明整数配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `longNumber(String key) → ItemBuilder`
- **说明：** 开始声明长整数配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `bool(String key) → ItemBuilder`
- **说明：** 开始声明布尔配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `duration(String key) → ItemBuilder`
- **说明：** 开始声明时长配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `decimal(String key) → ItemBuilder`
- **说明：** 开始声明十进制定点数配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `uri(String key) → ItemBuilder`
- **说明：** 开始声明绝对 URI 配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `enumeration(String key, String... enumValues) → ItemBuilder`
- **说明：** 开始声明枚举配置项。
- **参数：**
  - `key` — 插件本地配置键
  - `enumValues` — 允许取值列表
- **返回：** 配置项构建器

#### `secret(String key) → ItemBuilder`
- **说明：** 开始声明密文配置项。
- **参数：**
  - `key` — 插件本地配置键
- **返回：** 配置项构建器

#### `build() → ConfigDefinition`
- **说明：** 按声明顺序构建不可变 schema。
- **返回：** 不可变配置 schema

#### `defaultValue(String defaultValue) → ItemBuilder`
- **说明：** 设置在配置解析时转换的文本默认值。
- **参数：**
  - `defaultValue` — 文本默认值
- **返回：** 当前配置项构建器

#### `secret() → ItemBuilder`
- **说明：** 将该配置项的诊断信息标记为密文。
- **返回：** 当前配置项构建器

#### `description(String description) → ItemBuilder`
- **说明：** 设置面向用户的配置项说明。
- **参数：**
  - `description` — 展示说明
- **返回：** 当前配置项构建器

#### `enumValues(String... enumValues) → ItemBuilder`
- **说明：** 设置枚举配置项允许的值。
- **参数：**
  - `enumValues` — 允许取值列表；null 视为空列表
- **返回：** 当前配置项构建器

#### `end() → Builder`
- **说明：** 完成当前配置项并返回 schema 构建器。
- **返回：** 所属 schema 构建器


## ConfigItemDefinition

**类型：** record

描述一个插件本地配置键的不可变 schema 项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `key` | `String` | 插件本地配置键；小写驼峰，最长 128 字符 |
| `type` | `ConfigType` | 配置值类型 |
| `required` | `boolean` | 是否必须提供值 |
| `defaultValue` | `String` | 可选文本默认值；SECRET 类型禁止声明默认值 |
| `secret` | `boolean` | 诊断信息是否必须遮罩该值；SECRET 类型会强制为 true |
| `description` | `String` | 面向用户的配置说明 |
| `enumValues` | `List<String>` | ENUM 类型允许的值；非 ENUM 类型必须为空 |


## ConfigSource

**类型：** interface

宿主侧动态配置来源扩展点。


## ConfigType

**类型：** enum

插件配置支持的值类型。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `STRING` | — |
| `INTEGER` | — |
| `LONG` | — |
| `BOOLEAN` | — |
| `DECIMAL` | — |
| `DURATION` | — |
| `URI` | — |
| `ENUM` | — |
| `SECRET` | — |


## ConfigurationManager

**类型：** class

解析插件默认值、宿主配置、动态配置来源、环境变量、系统属性和运行时覆盖值。

### 方法

#### `standard(Map<String, String> hostConfig,
            Map<String, String> runtimeVariables) → ConfigurationManager`
- **说明：** 创建一个按宿主到运行时优先级排列配置来源的解析器。
- **参数：**
  - `hostConfig` — 宿主提供的扁平化配置
  - `configSources` — 宿主动态配置来源；按列表顺序叠加
  - `environment` — 进程环境变量
  - `systemProperties` — JVM 系统属性
  - `runtimeVariables` — 优先级最高的运行时覆盖值
- **返回：** 使用当前进程配置来源的解析器

#### `standard(Map<String, String> hostConfig,
            List<ConfigSource> configSources,
            Map<String, String> runtimeVariables) → ConfigurationManager`
- **说明：** 创建一个使用当前进程环境变量、系统属性和宿主动态配置来源的解析器。
- **参数：**
  - `hostConfig` — 宿主提供的扁平化配置
  - `configSources` — 宿主动态配置来源
  - `runtimeVariables` — 优先级最高的运行时覆盖值
- **返回：** 使用当前进程配置来源的解析器

#### `resolve(PluginDefinition definition) → PluginConfig`
- **说明：** 解析并校验一个插件隔离的配置快照。
- **参数：**
  - `definition` — 待解析 schema 的不可变插件声明
- **返回：** 限定在插件命名空间内的类型化配置

#### `resolveProvider(PluginDefinition definition,
            CapabilityContribution<T> contribution) → PluginConfig`
- **说明：** 解析某个 Provider 的私有配置。私有配置和插件共享配置使用不同命名空间，避免相互覆盖。
- **参数：**
  - `definition` — 插件定义
  - `contribution` — Provider 声明
  - `<T>` — Provider 类型
- **返回：** Provider 私有配置视图

#### `environmentName(String pluginId, String itemKey) → String`
- **说明：** 将一个完整插件配置键映射为确定性的环境变量名。
- **参数：**
  - `pluginId` — 稳定的插件标识
  - `itemKey` — 插件本地配置键
- **返回：** 确定性的环境变量名

#### `validateEnvironmentNames(Collection<PluginDefinition> definitions) → void`
- **说明：** 校验全部已发现插件定义的环境变量映射。
- **参数：**
  - `definitions` — 作为一个 classpath 快照校验的插件声明


## DefaultPluginConfig

**类型：** class

一个插件的不可变类型化配置快照。


## PluginConfig

**类型：** interface

限定在一个插件命名空间内的不可变已校验配置视图。


## SecretValue

**类型：** class

可关闭的内存密文包装器，其文本表示始终为遮罩值。

### 方法

#### `of(String value) → SecretValue`
- **说明：** 从明文创建密文副本。
- **参数：**
  - `value` — 非空白明文
- **返回：** 新的密文包装器

#### `use(java.util.function.Function<char[], T> operation) → synchronized <T> T`
- **说明：** 使用字符的临时防御性副本执行操作。
- **参数：**
  - `operation` — 消费临时字符副本的函数
  - `<T>` — 操作结果类型
- **返回：** 操作结果

#### `copy() → synchronized SecretValue`
- **说明：** 返回可独立关闭的副本。
- **返回：** 与当前实例隔离的新密文包装器

#### `close() → synchronized void`
- **说明：** 清零当前保留的字符缓冲区。 关闭后不得再调用 {@link #use}；重复关闭保持幂等。 / public synchronized void close()
