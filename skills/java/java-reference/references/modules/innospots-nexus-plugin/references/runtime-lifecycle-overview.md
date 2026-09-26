# 运行时与生命周期

| 包 / 类型 | 说明 |
|-----------|------|
| `contract.Plugin` | classpath 插件唯一 SPI |
| `declaration.*` | `PluginDefinition`、能力需求、贡献声明 |
| `runtime.*` | 插件运行时组装 |
| `lifecycle.*` | `PluginState`、`PluginRuntimeInfo` |
| `event.*` | `PluginEventBus`、启动/停止事件 |
| `capability.*` | `CapabilityRouter`、注册与路由 |
| `resource.*` | `ResourceScope`、插件资源注册 |
| `bootstrap.*` | 宿主侧引导钩子 |

包级参考：`contract.md`、`runtime.md`、`lifecycle.md`、`capability.md`、`event.md`、`resource.md`。
