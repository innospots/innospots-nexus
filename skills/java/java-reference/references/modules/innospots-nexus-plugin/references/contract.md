# 包 `contract`

## `Plugin`

**类型：** interface

声明插件及其插件级生命周期的唯一 classpath SPI。

| 方法 | 说明 |
|------|------|
| `definition() → PluginDefinition` | 返回无副作用的不可变定义 |
| `initialize(PluginContext context)` | 初始化一次启动周期；默认空操作 |
| `start()` | 在初始化和 Contribution 准备后启动插件行为；默认空操作 |
| `stop()` | 在资源作用域关闭前停止插件行为；默认空操作 |

## `PluginContext`

**类型：** interface

一次插件启动周期内暴露的只读运行时服务。

| 方法 | 说明 |
|------|------|
| `definition() → PluginDefinition` | 当前插件定义 |
| `config() → PluginConfig` | 当前插件作用域的已校验配置 |
| `events() → PluginEventBus` | 绑定当前资源作用域的事件总线 |
| `resources() → ResourceScope` | 当前启动周期的资源所有权边界 |
| `logger() → System.Logger` | 使用插件身份命名的日志器 |
