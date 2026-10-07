# 包 `event`

## DefaultPluginEventBus

**类型：** class

实例本地同步事件总线，隔离观察者失败。

### 方法

#### `subscribe(Class<E> eventType, Consumer<E> handler) → Subscription`
- **说明：** 注册一个同步观察者，直到其订阅或所属资源作用域关闭。
- **参数：**
  - `eventType` — Handler 接受的事件类型
  - `handler` — 在发布线程执行的观察者
  - `<E>` — 事件类型
- **返回：** 可幂等取消的订阅句柄

#### `publish(PluginEvent event) → void`
- **说明：** 向稳定的观察者快照发布事件，并隔离观察者失败。
- **参数：**
  - `event` — 待发布事件；null 将被忽略

#### `close() → void`
- **说明：** 关闭当前运行时本地总线并释放全部订阅引用。 public void close()

#### `scoped(ResourceScope scope) → PluginEventBus`
- **说明：** 返回一个视图，其创建的订阅自动归属于指定插件资源作用域。
- **参数：**
  - `scope` — 拥有该视图创建的全部订阅的资源作用域
- **返回：** 作用域事件总线视图


## PluginEvent

**类型：** interface

进程内不可变插件运行观测事件的标记契约。


## PluginEventBus

**类型：** interface

限定在一个插件管理器内的同步尽力而为事件通道。


## PluginFailedEvent

**类型：** record

不包含异常对象或配置值的插件失败观测事件。


## PluginStartedEvent

**类型：** record

所有 Capability 对外可见后发布的插件激活观测事件。


## PluginStoppedEvent

**类型：** record

资源释放后发布的插件停止观测事件。


## Subscription

**类型：** interface

可幂等取消的事件订阅句柄。
