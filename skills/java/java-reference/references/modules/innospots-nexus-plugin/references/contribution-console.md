# 包 `contribution.console`

`console@2` 的 MODULE/MENU 贡献模型、活动目录、解码、全局校验、事务准备与安全快照。

## `ConsoleContributionCatalog`

**类型：** class（final）

按 owner pluginId 保存已准备/提交的 Console Contribution，并与 `PluginAvailability` 联合
决定可见性。

### 方法

- `activeContributions() → List<ActiveConsoleContribution>`
- `activeContribution(String pluginId) → Optional<ActiveConsoleContribution>`

### `ConsoleContributionCatalog.ActiveConsoleContribution`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `ownerPluginId` | `String` | 所属插件 |
| `contribution` | `ConsolePluginContribution` | 不可变贡献 |
| `generation` | `long` | 活动目录提交代次 |

## `ConsoleContributeNode`

**类型：** record

递归 MODULE/MENU 节点。

| 组件 | 类型 | 说明 |
|------|------|------|
| `moduleKey` | `String` | MODULE 键；MENU 为 `null` |
| `displayName` | `I18nObject` | 必填名称 |
| `description` | `I18nObject` | 可选描述 |
| `orderIndex` | `int` | 同级排序 |
| `entryPageKey` | `String` | 叶子 MODULE/MENU 的稳定入口键 |
| `icon` | `String` | 图标 |
| `viewGroups` | `List<ConsolePermissionGroupDeclaration>` | 页面访问分组 |
| `operationGroups` | `List<ConsolePermissionGroupDeclaration>` | 操作分组 |
| `children` | `List<ConsoleContributeNode>` | 嵌套 MODULE/MENU |

规则：

- MODULE 必须有 `moduleKey`，且必须有 `entryPageKey` 或非空 `children`，二者不能同时存在。
- MENU 无 `moduleKey`，必须有 `entryPageKey`。
- 自定义分组必须包含 `default`。

### 静态工厂

- `module(...)`
- `menu(...)`（带描述与无描述两种）
- `singlePageModule(...)`

### 实例方法

- `isModule()` / `isMenu()`
- `moduleResourceKey() → "module:{moduleKey}"`
- `menuResourceKey(ownerModuleKey) → "menu:{ownerModuleKey}.{entryPageKey}"`

## `ConsoleContributions`

**类型：** class（final）

Entry 插件组装门面。

| 方法 | 说明 |
|------|------|
| `entryPlugin(pluginId, version, tags, displayName, description, domainKey, modules)` | 生成仅含一份 `console@2` Contribution 的 `PluginDefinition` |
| `discoveryTags(pluginId, domainKey, moduleKeys)` | 生成 `kind=entry`、`domain`、`module` 标签 |
| `discoveryTags(pluginId, contribution)` | 从 Contribution 提取 domain/module 后委托 |

`module` 标签为排序后的逗号列表；超过 64 字符时回退为 pluginId 最后一段。

## `ConsolePermissionGroupDeclaration`

**类型：** record

MODULE 页面访问或操作分组的本地化声明。

| 组件 | 类型 | 说明 |
|------|------|------|
| `group` | `String` | 分组键；必须为 lowercase kebab-case |
| `label` | `I18nObject` | 必填的分组展示文本 |

## `ConsolePluginContribution`

**类型：** record，implements `PluginContribution`、`PluginContributionInventory`

| 组件 | 类型 | 说明 |
|------|------|------|
| `domainKey` | `String` | 控制台领域键，例如 `nexus`、`platform` |
| `displayName` | `I18nObject` | 可选贡献名称 |
| `description` | `I18nObject` | 可选描述 |
| `orderIndex` | `int` | 贡献排序 |
| `modules` | `List<ConsoleContributeNode>` | 非空顶层 MODULE 列表 |

`TYPE = PluginContributionType("console", 2)`。`moduleCount()` 返回顶层 MODULE 数；
`componentCount()` 返回 MODULE 下全部子节点数（递归包含 MENU 与嵌套 MODULE）。

## `ConsolePluginContributionDecoder`

**类型：** class（final，implements `PluginContributionDecoder<ConsolePluginContribution>`）

严格解码 `type: console`、`majorVersion: 2`、`domainKey`、展示字段和 `modules[]`；
拒绝未知字段、根级 `moduleKey` / `children` / 非空 `entryPageKey` 与非法节点形态。

## `ConsolePluginContributionHandler`

**类型：** class（final，implements `PluginContributionHandler<ConsolePluginContribution>`）

构造器接收 `ConsoleContributionCatalog` 与 `ReservedPluginResourceCatalog`。

- `validate(...)`：拒绝插件内重复 `moduleKey`、模块内重复 `entryPageKey`、跨插件资源冲突，
  以及历史保留身份被其它插件占用。
- `prepare(...)`：返回绑定 availability 的活动目录事务句柄。
- `catalog()`：返回活动目录。

## `ConsolePluginContributionSnapshotter`

**类型：** class（final，implements `PluginContributionSnapshotter<ConsolePluginContribution>`）

快照仅保留 `type`、`domainKey`、MODULE/MENU 稳定身份（`moduleKey` /
`resourceKey` / `entryPageKey` / `icon`）及递归 `children`，不包含 I18n、分组或运行时对象。

## `ReservedPluginResourceCatalog`

**类型：** class（final）

由历史安全快照构建的不可变资源身份保留表。

### 方法

- `isReserved(ownerPluginId, resourceType, resourceKey) → boolean`
- `ownerOf(resourceType, resourceKey) → Optional<String>`
- `resources() → List<ReservedResource>`

### `ReservedPluginResourceCatalog.ReservedResource`

**类型：** record

`ownerPluginId`、`resourceType`、`resourceKey`。
