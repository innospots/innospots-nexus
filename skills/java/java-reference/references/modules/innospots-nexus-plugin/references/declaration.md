# 包 `declaration`

插件身份、运行时定义、YAML 纯数据模型、标签与来源快照。

## `PluginDefinition`

**类型：** record

已通过静态校验的不可变插件运行时定义。定义只保存身份、声明和无副作用工厂，不保存运行状态
或配置值；同一 Contribution 类型只能出现一次。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 反向域名稳定身份 |
| `version` | `String` | 发布版本 |
| `apiVersion` | `int` | 插件协议主版本；当前为 `1` |
| `displayName` | `I18nObject` | 本地化显示名称 |
| `description` | `I18nObject` | 本地化描述 |
| `tags` | `Tags` | 插件级路由标签 |
| `config` | `ConfigDefinition` | 插件共享配置 schema |
| `contributions` | `List<PluginContribution>` | 提交给宿主的扩展声明 |

### 构造方法

#### `PluginDefinition(String pluginId, String version, int apiVersion, I18nObject displayName, I18nObject description, Tags tags, ConfigDefinition config, List<PluginContribution> contributions)`

规范构造；校验身份、版本、本地化文本、配置 schema 与 Contribution 唯一性。

#### `PluginDefinition(String pluginId, String name, String version, int apiVersion, Tags tags, ConfigDefinition config)`

兼容构造；将 `name` 转为单语言显示名称，描述为空，Contributions 为空。

### 方法

| 方法 | 说明 |
|------|------|
| `displayName()` | 返回本地化显示名称副本 |
| `description()` | 返回本地化描述副本 |
| `builder(String pluginId)` | 创建流式构建器 |

## `PluginDefinition.Builder`

**类型：** class（public static final）

### 方法

| 方法 | 说明 |
|------|------|
| `displayName(I18nObject displayName)` | 设置本地化显示名称 |
| `name(String name)` | 设置单语言显示名称 |
| `description(I18nObject description)` | 设置本地化描述 |
| `version(String version)` | 设置发布版本 |
| `apiVersion(int apiVersion)` | 设置协议主版本 |
| `tags(Tags tags)` | 设置插件级标签 |
| `config(ConfigDefinition config)` | 设置共享配置 schema |
| `contribute(PluginContribution contribution)` | 添加一份 Contribution |
| `build()` | 构建不可变定义 |

## `PluginManifest`

**类型：** record

YAML DSL 的纯数据模型，不保存 Java `Class`、工厂、类加载器或运行时配置值。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `apiVersion` | `String` | 文档头协议版本，例如 `nexus.plugin/v1` |
| `kind` | `String` | 文档类型，例如 `Plugin` |
| `metadata` | `Metadata` | 插件身份与发布版本 |
| `spec` | `Spec` | 协议、名称、配置和扩展声明 |

## `PluginManifest.Metadata`

**类型：** record

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 反向域名插件标识 |
| `version` | `String` | 发布版本 |

## `PluginManifest.Spec`

**类型：** record

| 名称 | 类型 | 说明 |
|------|------|------|
| `apiVersion` | `Integer` | 插件协议主版本 |
| `displayName` | `Map<String, String>` | 本地化显示名称 |
| `description` | `Map<String, String>` | 本地化描述 |
| `tags` | `Map<String, String>` | 插件级标签 |
| `config` | `List<ConfigItem>` | 共享配置项 |
| `requirements` | `List<Requirement>` | 旧版依赖数据槽；当前编译器拒绝非空值 |
| `capabilities` | `List<Capability>` | 旧版 Provider 数据槽；当前编译器拒绝非空值 |
| `contributions` | `List<Map<String, Object>>` | 通用 Contribution 声明；YAML 新插件只使用此项 |

## `PluginManifest.ConfigItem`

**类型：** record

| 名称 | 类型 | 说明 |
|------|------|------|
| `key` | `String` | 配置键 |
| `type` | `String` | `ConfigType` 名称 |
| `required` | `Boolean` | 是否必填 |
| `defaultValue` | `Object` | YAML `default` 字段；按声明类型校验 |
| `description` | `String` | 人类可读说明 |
| `enumValues` | `List<String>` | `ENUM` 允许值 |

### 方法

#### `defaultText() → String`

把 YAML 默认值转换为 `ConfigItemDefinition` 使用的文本；无默认值时返回 `null`。

## `PluginManifest.Requirement`

**类型：** record

旧版 Capability 依赖的 YAML 数据形状。`PluginDefinitionCompiler` 当前拒绝非空列表。

| 名称 | 类型 | 说明 |
|------|------|------|
| `type` | `String` | Capability 逻辑名称 |
| `majorVersion` | `Integer` | API 主版本 |
| `required` | `Boolean` | 缺少依赖时是否阻止启动 |
| `tags` | `Map<String, String>` | Provider 标签要求 |

## `PluginManifest.Capability`

**类型：** record

旧版 Capability Provider 的 YAML 数据形状。`PluginDefinitionCompiler` 当前拒绝非空列表。

| 名称 | 类型 | 说明 |
|------|------|------|
| `type` | `String` | Capability 逻辑名称 |
| `majorVersion` | `Integer` | API 主版本 |
| `providerId` | `String` | 插件内唯一 Provider 标识 |
| `api` | `String` | Java API 接口全名 |
| `tags` | `Map<String, String>` | Provider 标签 |
| `config` | `List<ConfigItem>` | Provider 配置 |
| `bind` | `Bind` | Java 绑定 |
| `exposures` | `List<Map<String, Object>>` | 预留字段 |

## `PluginManifest.Bind`

**类型：** record

| 名称 | 类型 | 说明 |
|------|------|------|
| `kind` | `String` | 绑定种类 |
| `className` | `String` | YAML `class` 字段中的实现类全名 |

## `PluginManifestParser`

**类型：** interface

把 UTF-8 YAML 文档解析为严格校验的 `PluginManifest`。

### 方法

#### `parse(InputStream input) → PluginManifest`

解析单个 YAML 文档；输入为空、超出大小限制、语法或结构非法时抛出 `NexusException`。

## `JacksonPluginManifestParser`

**类型：** class（final，implements `PluginManifestParser`）

基于 Jackson YAML 的严格解析器：单文档最大 1 MiB、最大嵌套深度 64、拒绝未知字段与
YAML 锚点/别名/自定义标签。

### 常量

| 名称 | 值 | 说明 |
|------|----|------|
| `MAX_DOCUMENT_BYTES` | `1024 * 1024` | 单文档字节上限 |
| `MAX_NESTING_DEPTH` | `64` | 最大嵌套深度 |

### 构造方法

#### `JacksonPluginManifestParser()`

创建固定约束的解析器。

### 方法

#### `mapper() → ObjectMapper`

返回用于测试或扩展绑定的严格 `ObjectMapper` 副本。

#### `parse(InputStream input) → PluginManifest`

同接口定义。

## `PluginSource`

**类型：** record

插件定义的声明来源和发现时间。

| 名称 | 类型 | 说明 |
|------|------|------|
| `sourceType` | `String` | 例如 `JAVA` 或 `YAML` |
| `location` | `String` | 类名或资源路径 |
| `discoveredAt` | `Instant` | 发现时间 |

### 方法

| 方法 | 说明 |
|------|------|
| `java(String location, Instant discoveredAt)` | 创建 `JAVA` 来源 |
| `yaml(String location, Instant discoveredAt)` | 创建 `YAML` 来源 |

## `ProviderRef`

**类型：** record

Contribution 等扩展条目的稳定身份。

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 所属插件反向域名标识 |
| `providerId` | `String` | 插件内全局唯一、lowercase kebab-case 标识 |

## `Tag`

**类型：** record

一个不可变标签属性，名称与值均不超过 64 字符；名称为 lowercase dotted 或 kebab-case。

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 标签名 |
| `value` | `String` | 非空标签值 |

## `Tags`

**类型：** class（final）

不可变且按名称排序的标签集合。

### 方法

| 方法 | 说明 |
|------|------|
| `empty()` | 返回共享空实例 |
| `of(String name, String value)` | 创建单项标签集合 |
| `from(Map<String, String> values)` | 从映射创建 |
| `and(String name, String value)` | 返回新增标签后的副本；同名异值抛出异常 |
| `get(String name)` | 查询标签值 |
| `matches(Tags required)` | 判断是否包含全部请求标签 |
| `asMap()` | 返回不可变有序映射 |
| `isEmpty()` | 是否无标签 |
