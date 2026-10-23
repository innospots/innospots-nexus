# 包 `plugin.entry`

插件管理主页面入口。

## `PluginManagementEntryPlugin`

**类型：** class（final，implements `Plugin`）

贡献 `plugin` MODULE：

| 项 | 值 |
|----|----|
| pluginId | `BuiltinConsoleEntryPlugins.PLUGIN_MANAGEMENT` |
| domainKey | `nexus` |
| moduleKey | `plugin` |
| entryPageKey | `nexus-plugin-main` |
| icon | `appstore` |
| orderIndex | `60` |

`definition()` 返回一次构建的不可变 `PluginDefinition`，内部使用：

```text
ConsoleContributions.entryPlugin(...)
  + ConsoleContributeNode.singlePageModule(...)
  + ConsoleContributions.discoveryTags(...)
```

**SPI 注册：**
`META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin`

## 相关

- [entry.md](entry.md)
- [contribution-console-entry-assembly.md](../../innospots-nexus-plugin/references/contribution-console-entry-assembly.md)
- [console-entry-and-configuration.md](../../../console-entry-and-configuration.md)
