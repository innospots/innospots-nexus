# 包 `installation.config`

## `PluginInstallationConfig`

**类型：** record

插件安装策略；默认开启首次发现自动安装。

### 常量

| 名称 | 值 / 说明 |
|------|-----------|
| `AUTO_INSTALL_KEY` | `nexus.plugin.auto-install` |

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `autoInstall` | `boolean` | 首次发现时是否自动安装 |

### 方法

#### `from(String value) → PluginInstallationConfig`

解析 `AUTO_INSTALL_KEY` 的文本值。`null`、空白或未配置时按 `true` 处理；
仅接受忽略大小写的 `true` / `false`，其它文本抛出
`PluginStatusCode.PLUGIN_CONFIG_INVALID`。
