# 包 `contribution`

## `PluginContribution`

**类型：** interface

插件向宿主提交的一类不可变静态扩展声明；`type()` 返回类型与主版本。

## `PluginContributionContext`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `owner` | `ProviderRef` | 声明 Contribution 的插件身份 |
| `config` | `PluginConfig` | 插件只读配置 |
| `availability` | `PluginAvailability` | 对外可见性门控 |

另提供省略 `availability` 的兼容构造器，使用新的未激活门控。

## `PluginContributionDecoder<T>`

**类型：** interface

提供 `type()` 与 `decode(Map<String, Object>)`。

## `PluginContributionDecoderRegistry`

**类型：** class（final）

不可变 Decoder 表；`builder()` 构建，`find(type)` 查询，`snapshot()` 返回只读映射。

## `PluginContributionEntry<T>`

**类型：** record

`owner: ProviderRef` + `contribution: T`，用于全目录校验。

## `PluginContributionHandler<T>`

**类型：** interface

提供 `type()`、`validate(PluginCatalog, List<PluginContributionEntry<T>>)` 与
`prepare(PluginContributionContext, T)`。

## `PluginContributionInventory`

**类型：** interface

Contribution 可选实现，声明扩展规模：

- `moduleCount() → int`
- `componentCount() → int`

## `PluginContributionInventories`

**类型：** class（final）

静态统计门面。

| 方法 | 说明 |
|------|------|
| `moduleCount(PluginContribution)` | 未实现 inventory 时返回 0 |
| `componentCount(PluginContribution)` | 未实现 inventory 时返回 0 |
| `summarize(List<PluginContribution>)` | 返回 `Summary` |

### `PluginContributionInventories.Summary`

**类型：** record

`contributionCount`、`moduleCount`、`componentCount`。

## `PluginContributionSnapshotter<T>`

**类型：** interface

提供 `type()` 与 `snapshot(T) → Map<String, Object>`，输出不含 Class、Handler 或 Secret
的安全摘要。

## `PluginContributionSnapshotterRegistry`

**类型：** class（final）

不可变 Snapshotter 表；`builder()`、`find(type)`、`snapshot()`。

## `PluginContributionType<T>`

**类型：** record

`name` + `majorVersion`。名称使用 lowercase dotted/dashed 形式，`majorVersion >= 1`；
`toString()` 返回 `name@major`。

## `PreparedPluginContribution`

**类型：** interface（extends `AutoCloseable`）

Contribution 事务句柄，提供幂等的 `stage()`、`commit()`、`rollback()`、`close()`。
调用顺序为 stage → commit → close，失败时回滚。
