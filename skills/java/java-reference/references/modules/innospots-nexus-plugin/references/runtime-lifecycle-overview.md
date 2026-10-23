# 运行时与生命周期

| 包 / 类型 | 说明 |
|-----------|------|
| `contract.Plugin` / `PluginContext` | 唯一插件 SPI 与启动周期上下文 |
| `declaration.*` | `PluginDefinition`、`PluginManifest`、来源与标签 |
| `runtime.*` | `PluginManager`、`DefaultPluginManager`、`PluginRuntimeConfig` |
| `lifecycle.*` | `PluginState`、`PluginRuntimeInfo`、可用性门控 |
| `contribution.*` | 声明、解码、全局校验、事务提交与快照 |
| `event.*` | 运行时本地事件总线与启停观测事件 |
| `resource.*` | 单次启动周期的逆序资源释放 |
| `bootstrap.*` | 宿主启动任务与属性 |

包级参考：[contract.md](contract.md)、[runtime.md](runtime.md)、[lifecycle.md](lifecycle.md)、
[contribution.md](contribution.md)、[event.md](event.md)、[resource.md](resource.md)、
[bootstrap.md](bootstrap.md)。
