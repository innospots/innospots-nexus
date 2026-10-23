# 包 `status`

## `PluginStatusCode`

**类型：** enum（implements `StatusCode`）

插件运行时状态码，模块标识为 `PLG`。

| 常量 | 本地码 | HTTP | 说明 |
|------|--------|------|------|
| `PLUGIN_DISCOVERY_FAILED` | `0001` | 500 | 插件发现失败 |
| `PLUGIN_DEFINITION_INVALID` | `0002` | 500 | 插件定义无效 |
| `PLUGIN_DUPLICATE` | `0003` | 500 | pluginId 重复 |
| `PLUGIN_API_INCOMPATIBLE` | `0004` | 500 | 插件 API 版本不兼容 |
| `PLUGIN_CONFIG_INVALID` | `0005` | 500 | 插件配置无效 |
| `PLUGIN_DEPENDENCY_MISSING` | `0006` | 500 | 插件依赖缺失 |
| `PLUGIN_DEPENDENCY_CYCLE` | `0007` | 500 | 插件依赖无法解析 |
| `PLUGIN_START_FAILED` | `0008` | 500 | 插件启动失败 |
| `PLUGIN_STOP_FAILED` | `0009` | 500 | 插件停止失败 |
| `PLUGIN_IN_USE` | `0010` | 409 | 插件仍被依赖 |
| `CAPABILITY_NOT_FOUND` | `0011` | 404 | Capability 不存在 |
| `CAPABILITY_AMBIGUOUS` | `0012` | 409 | Capability 选择不唯一 |
| `CAPABILITY_TYPE_MISMATCH` | `0013` | 500 | Capability 类型不匹配 |
| `PROVIDER_DUPLICATE` | `0014` | 500 | Provider 标识重复 |
| `DSL_SYNTAX_INVALID` | `0015` | 400 | 插件 DSL 语法无效 |
| `DSL_STRUCTURE_INVALID` | `0016` | 400 | 插件 DSL 结构无效 |
| `CAPABILITY_TYPE_UNKNOWN` | `0017` | 400 | Capability 类型未知 |
| `UNSUPPORTED_BIND_KIND` | `0018` | 400 | Bind 类型不支持 |
| `UNSUPPORTED_EXPOSURE_KIND` | `0019` | 400 | Exposure 类型不支持 |
| `UNSUPPORTED_CONTRIBUTION_TYPE` | `0020` | 400 | Contribution 类型不支持 |
| `RESOURCE_CONFLICT` | `0021` | 409 | 插件资源标识冲突 |
| `PLUGIN_CONCURRENCY_CONFLICT` | `0022` | 409 | 插件命令并发冲突 |
| `PLUGIN_NOT_INSTALLED` | `0023` | 409 | 插件未安装 |
| `PLUGIN_MISSING` | `0024` | 404 | 已安装插件在当前目录中缺失 |
| `PLUGIN_PERSISTENCE_FAILED` | `0025` | 500 | 插件安装事实或快照持久化失败 |

### 方法

- `module() → String`：固定返回 `PLG`
- `category() → StatusCategory`
- `localCode() → String`
- `message() → I18nObject`
- `advice() → I18nObject`
- `httpStatusCode() → int`
