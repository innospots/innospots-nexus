# 包 `discovery`

## `ClasspathPluginDiscovery`

**类型：** class

通过 Java SPI 与 classpath `plugin.yaml` 发现插件，并使用宿主注册的
`PluginContributionDecoderRegistry` 编译为统一的 `Plugin` 目录。

## `DiscoveredPlugin`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `plugin` | `Plugin` | ServiceLoader 或 YAML 适配实例 |
| `definition` | `PluginDefinition` | 编译后的不可变定义 |
| `discoveredAt` | `Instant` | 发现时间 |
| `source` | `PluginSource` | 声明来源 |

## `ManifestPlugin`

**类型：** class

把 YAML 编译定义适配为统一 `Plugin` SPI。

## `PluginCatalog`

**类型：** class（final）

单个运行时的不可变发现快照。构造时校验 pluginId 唯一与插件 API 主版本兼容。

### 方法

- `of(List<DiscoveredPlugin>) → PluginCatalog`
- `plugins() → List<DiscoveredPlugin>`
- `plugin(String pluginId) → Optional<DiscoveredPlugin>`
- `definitions() → List<PluginDefinition>`

## `PluginDefinitionCompiler`

**类型：** class（final）

将 `PluginManifest` 编译为无副作用的 `PluginDefinition`。

### 构造方法

#### `PluginDefinitionCompiler(PluginContributionDecoderRegistry contributionDecoders, ClassLoader classLoader)`

### 方法

#### `compile(PluginManifest manifest, PluginSource source) → PluginDefinition`

校验 DSL 头、API 版本、配置 schema 与 Contribution，并至少要求一份 Contribution。
非空 `spec.capabilities` / `spec.requirements` 会被明确拒绝。

## `PluginDiscoveryReport`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `validCatalog` | `PluginCatalog` | 通过校验的插件目录 |
| `rejectedDefinitions` | `List<RejectedPluginDefinition>` | 被拒绝的声明及其诊断 |

## `RejectedPluginDefinition`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `source` | `PluginSource` | 声明来源 |
| `claimedPluginId` | `String` | 声明中声称的插件标识；无法解析时可为空 |
| `diagnostics` | `List<String>` | 拒绝原因快照；`null` 归一为空列表 |
