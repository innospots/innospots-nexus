# 包 `declaration`

## CapabilityContribution

**类型：** record

将一个 Capability API、Provider 身份、路由标签和配置绑定到插件工厂。


## CapabilityRequirement

**类型：** record

声明插件所需的 Capability 及其路由标签。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `key` | `CapabilityKey` | Capability 逻辑身份 |
| `requiredTags` | `Tags` | Provider 必须包含的标签子集 |
| `required` | `boolean` | 缺少匹配 Provider 时是否阻止启动 |


## JacksonPluginManifestParser

**类型：** class

基于 Jackson YAML 的严格 DSL 解析器，限制输入大小、深度和 YAML 扩展语法。

### 方法

#### `mapper() → ObjectMapper`
- **说明：** DSL 单文档最大字节数。 public static final int MAX_DOCUMENT_BYTES = 1024 * 1024; /** DSL 最大嵌套深度。 public static final int MAX_NESTING_DEPTH = 64; private static final Pattern YAML_EXTENSION = Pattern.compile( "(?m)(?:^|\\s)(?:[&*][A-Za-z_][A-Za-z0-9_-]*|![A-Za-z_][A-Za-z0-9_:/.-]*)"); private final ObjectMapper mapper; /** 使用固定约束创建解析器。 public JacksonPluginManifestParser() { YAMLFactory factory = YAMLFactory.builder() .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) .streamReadConstraints(StreamReadConstraints.builder() .maxNestingDepth(MAX_NESTING_DEPTH) .maxDocumentLength(MAX_DOCUMENT_BYTES) .build()) .build(); mapper = new ObjectMapper(factory) .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS); } /** 返回用于测试或扩展绑定的严格 ObjectMapper。 public ObjectMapper mapper()

#### `parse(InputStream input) → PluginManifest`
- **说明：** 解析一个 UTF-8 YAML 输入流。
- **参数：**
  - `input` — UTF-8 YAML 文档输入流
- **返回：** 严格校验后的插件清单


## PluginDefinition

**类型：** record

已通过静态校验的不可变插件运行时定义。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 插件反向域名稳定身份 |
| `version` | `String` | 插件发布版本 |
| `apiVersion` | `int` | 插件协议主版本 |
| `displayName` | `I18nObject` | 插件本地化显示名称 |
| `description` | `I18nObject` | 插件本地化描述 |
| `tags` | `Tags` | 插件级路由标签 |
| `config` | `ConfigDefinition` | 插件共享配置定义 |
| `capabilities` | `List<CapabilityContribution<?>>` | 插件提供的 Capability 声明 |
| `requirements` | `List<CapabilityRequirement>` | 插件依赖的 Capability 声明 |
| `contributions` | `List<PluginContribution>` | 插件提交给宿主的通用扩展声明 |

### 方法

#### `description() → I18nObject`
- **说明：** 返回描述的防御性副本。
- **返回：** 本地化描述副本

#### `builder(String pluginId) → Builder`
- **说明：** 创建声明构建器。
- **参数：**
  - `pluginId` — 插件反向域名稳定身份
- **返回：** 可变构建器

#### `name(String name) → Builder`
- **说明：** 设置单语言显示名称的便捷形式。
- **参数：**
  - `name` — 插件显示名称
- **返回：** 当前构建器

#### `description(I18nObject description) → Builder`
- **说明：** 设置本地化描述。
- **参数：**
  - `description` — 插件描述
- **返回：** 当前构建器

#### `version(String version) → Builder`
- **说明：** 设置发布版本。
- **参数：**
  - `version` — 插件发布版本
- **返回：** 当前构建器

#### `apiVersion(int apiVersion) → Builder`
- **说明：** 设置插件协议主版本。
- **参数：**
  - `apiVersion` — 插件协议主版本
- **返回：** 当前构建器

#### `tags(Tags tags) → Builder`
- **说明：** 设置由所有 Provider 继承的插件标签。
- **参数：**
  - `tags` — 插件路由标签
- **返回：** 当前构建器

#### `provide(CapabilityType<T> type,
                CapabilityProviderFactory<? extends T> factory) → Builder`
- **说明：** 添加一个使用默认 Provider 标识的 Java 工厂。
- **参数：**
  - `type` — Capability API 类型
  - `factory` — Provider 工厂
  - `<T>` — Provider 契约类型
- **返回：** 当前构建器

#### `provide(CapabilityType<T> type,
                String providerId,
                Tags tags,
                ConfigDefinition config,
                CapabilityProviderFactory<? extends T> factory) → Builder`
- **说明：** 添加一个带完整身份、标签和配置的 Provider 工厂。
- **参数：**
  - `type` — Capability API 类型
  - `providerId` — 插件内唯一 Provider 标识
  - `tags` — Provider 标签
  - `config` — Provider 配置定义
  - `factory` — Provider 工厂
  - `<T>` — Provider 契约类型
- **返回：** 当前构建器

#### `provide(CapabilityType<T> type,
                String providerId,
                CapabilityProviderFactory<? extends T> factory) → Builder`
- **说明：** 添加一个无标签、无 Provider 配置的完整身份工厂。
- **参数：**
  - `type` — Capability API 类型
  - `providerId` — 插件内唯一 Provider 标识
  - `factory` — Provider 工厂
  - `<T>` — Provider 契约类型
- **返回：** 当前构建器

#### `require(String name, int majorVersion, boolean required) → Builder`
- **说明：** 添加无标签要求的 Capability 依赖。
- **参数：**
  - `name` — Capability 逻辑名称
  - `majorVersion` — API 主版本
  - `required` — 缺少依赖时是否阻止启动
- **返回：** 当前构建器

#### `require(String name, int majorVersion, Tags requiredTags, boolean required) → Builder`
- **说明：** 添加带标签要求的 Capability 依赖。
- **参数：**
  - `name` — Capability 逻辑名称
  - `majorVersion` — API 主版本
  - `requiredTags` — Provider 必须包含的标签
  - `required` — 缺少依赖时是否阻止启动
- **返回：** 当前构建器

#### `require(CapabilityType<?> type, boolean required) → Builder`
- **说明：** 添加无标签要求的 Capability 依赖。
- **参数：**
  - `type` — 依赖的 Capability 类型
  - `required` — 缺少依赖时是否阻止启动
- **返回：** 当前构建器

#### `require(CapabilityType<?> type, Tags requiredTags, boolean required) → Builder`
- **说明：** 添加带标签要求的 Capability 依赖。
- **参数：**
  - `type` — 依赖的 Capability 类型
  - `requiredTags` — Provider 必须包含的标签
  - `required` — 缺少依赖时是否阻止启动
- **返回：** 当前构建器

#### `config(ConfigDefinition config) → Builder`
- **说明：** 设置插件共享配置定义。
- **参数：**
  - `config` — 插件配置定义
- **返回：** 当前构建器

#### `contribute(PluginContribution contribution) → Builder`
- **说明：** 添加一份插件级 Contribution 声明。 public Builder contribute(PluginContribution contribution)

#### `build() → PluginDefinition`
- **说明：** 构建并返回不可变定义。 public PluginDefinition build()


## PluginManifest

**类型：** record

YAML DSL 的纯数据模型。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `apiVersion` | `String` | 文档头协议版本，例如 nexus.plugin/v1 |
| `kind` | `String` | 文档类型，例如 Plugin |
| `metadata` | `Metadata` | 插件稳定身份与发布版本 |
| `spec` | `Spec` | 插件协议、名称、配置和扩展声明 |

### 方法

#### `Metadata(String pluginId, String version) → record`
- **说明：** 插件稳定身份和发布版本。
- **参数：**
  - `pluginId` — 反向域名插件标识
  - `version` — 发布版本文本

#### `Spec(Integer apiVersion,
            Map<String, String> displayName,
            Map<String, String> description,
            Map<String, String> tags,
            List<ConfigItem> config,
            List<Requirement> requirements,
            List<Capability> capabilities,
            List<Map<String, Object>> contributions) → record`
- **说明：** 插件协议、名称、配置和扩展声明。
- **参数：**
  - `apiVersion` — 插件协议主版本
  - `displayName` — 本地化显示名称
  - `description` — 本地化描述；可为空映射
  - `tags` — 插件级路由标签
  - `config` — 插件共享配置项
  - `requirements` — Capability 依赖
  - `capabilities` — Capability Provider 声明
  - `contributions` — 通用 Contribution 声明

#### `ConfigItem(String key,
            String type,
            Boolean required,
            @JsonProperty("default") → record`
- **说明：** 对缺省可选字段提供空集合，避免下游出现可变空值。 public Spec { description = description == null ? Map.of() : Map.copyOf(description); tags = tags == null ? Map.of() : Map.copyOf(tags); config = config == null ? List.of() : List.copyOf(config); requirements = requirements == null ? List.of() : List.copyOf(requirements); capabilities = capabilities == null ? List.of() : List.copyOf(capabilities); contributions = contributions == null ? List.of() : List.copyOf(contributions); } } /** 一个插件或 Provider 的配置项。
- **参数：**
  - `key` — 配置键
  - `type` — 配置类型名称，对应 com.innospots.nexus.core.plugin.config.ConfigType
  - `required` — 是否必填
  - `defaultValue` — 默认值；SECRET 类型不得设置
  - `description` — 人类可读说明
  - `enumValues` — ENUM 类型的合法取值

#### `defaultText() → String`
- **说明：** 对缺省枚举和值描述提供稳定默认值。 public ConfigItem { enumValues = enumValues == null ? List.of() : List.copyOf(enumValues); } /** 返回适用于当前配置模型的文本默认值。 public String defaultText()

#### `Requirement(String type,
            @JsonProperty("majorVersion") → record`
- **说明：** 一个 Capability 依赖声明。
- **参数：**
  - `type` — Capability 逻辑名称
  - `majorVersion` — API 主版本
  - `required` — 缺少依赖时是否阻止启动；缺省为 true
  - `tags` — Provider 必须包含的路由标签

#### `Capability(String type,
            Integer majorVersion,
            String providerId,
            String api,
            Map<String, String> tags,
            List<ConfigItem> config,
            Bind bind,
            List<Map<String, Object>> exposures) → record`
- **说明：** 规范化缺省依赖字段。 public Requirement { tags = tags == null ? Map.of() : Map.copyOf(tags); } } /** 一个 Capability Provider 的显式 Java 绑定声明。
- **参数：**
  - `type` — Capability 逻辑名称
  - `majorVersion` — API 主版本
  - `providerId` — 插件内唯一 Provider 标识
  - `api` — Provider 实现的 Java API 接口全名
  - `tags` — Provider 专属路由标签
  - `config` — Provider 专属配置项
  - `bind` — 绑定方式；V1 仅支持 kind=java
  - `exposures` — 预留的对外暴露声明；V1 不支持

#### `Bind(String kind, @JsonProperty("class") → record`
- **说明：** 规范化缺省 Provider 字段。 public Capability { tags = tags == null ? Map.of() : Map.copyOf(tags); config = config == null ? List.of() : List.copyOf(config); exposures = exposures == null ? List.of() : List.copyOf(exposures); } } /** Capability 的绑定方式；V1 仅实现 kind=java。
- **参数：**
  - `kind` — 绑定种类
  - `className` — Java 实现类的完全限定名


## PluginManifestParser

**类型：** interface

将 UTF-8 YAML 文档解析为严格的 PluginManifest。


## PluginSource

**类型：** record

描述插件定义的声明来源和发现时间。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `sourceType` | `String` | 来源类型标识，例如 JAVA 或 YAML |
| `location` | `String` | classpath 类名或资源路径 |
| `discoveredAt` | `Instant` | 发现时间戳 |

### 方法

#### `java(String location, Instant discoveredAt) → PluginSource`
- **说明：** / public PluginSource { if (sourceType == null || sourceType.isBlank() || location == null || location.isBlank() || discoveredAt == null) { throw NexusException.build( PluginStatusCode.PLUGIN_DISCOVERY_FAILED, "plugin source type, location and discovery time are required"); } } /** 创建 Java SPI 来源记录。
- **参数：**
  - `location` — 实现类的完全限定名
  - `discoveredAt` — 发现时间戳
- **返回：** Java SPI 来源记录

#### `yaml(String location, Instant discoveredAt) → PluginSource`
- **说明：** 创建 YAML 资源来源记录。
- **参数：**
  - `location` — YAML 资源路径
  - `discoveredAt` — 发现时间戳
- **返回：** YAML 资源来源记录
