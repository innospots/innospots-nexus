# 包 `event`

## `DefaultPluginEventBus`

**类型：** class（final，implements `PluginEventBus`）

实例本地、同步、尽力而为的事件总线；观察者异常被隔离。

### 方法

- `subscribe(Class<E>, Consumer<E>) → Subscription`
- `publish(PluginEvent event)`：`null` 与总线关闭后忽略。
- `scoped(ResourceScope scope) → PluginEventBus`：订阅自动注册到资源作用域。
- `close()`：关闭并清空订阅，幂等。

## `PluginEvent`

**类型：** interface

进程内插件运行观测事件标记。

## `PluginEventBus`

**类型：** interface

`subscribe(...)` 与 `publish(...)`。

## `PluginFailedEvent`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 插件标识 |
| `phase` | `String` | 失败阶段 |
| `errorCode` | `String` | 脱敏错误码；不包含异常对象或配置值 |
| `occurredAt` | `Instant` | 事件发生时间 |

## `PluginStartedEvent`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 插件标识 |
| `version` | `String` | 已启动插件版本 |
| `contributions` | `List<String>` | 已提交的 `type@major` 列表 |
| `occurredAt` | `Instant` | 事件发生时间 |

## `PluginStoppedEvent`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 插件标识 |
| `occurredAt` | `Instant` | 事件发生时间 |

## `Subscription`

**类型：** interface（extends `AutoCloseable`）

幂等取消订阅。
