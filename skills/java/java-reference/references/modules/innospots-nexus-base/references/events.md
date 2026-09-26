# 包 `events`

## DomainEvent

**类型：** interface

所有领域事件的基础接口。每个事件拥有唯一 ID、类型标识符和发生时间戳。


## EventBus

**类型：** class

简单的内存事件总线。订阅者通过 {@link #subscribe} 注册特定事件类型； 发布者通过 {@link #publish}（即发即弃）或 {@link #publishSync}（阻塞，返回最后处理器结果）触发事件。 处理器存储在 CopyOnWriteArrayList 中以支持安全的并发迭代。 事件类型匹配使用 Class，因此处理器可匹配已注册类型的子类。

### 方法

#### `subscribe(Class<E> eventType, EventHandler<E> handler) → void`
- **说明：** 订阅指定类型的事件。
- **参数：**
  - `eventType` — 事件类型
  - `handler` — 事件处理器
  - `<E>` — 事件类型参数

#### `unsubscribe(Class<E> eventType, EventHandler<E> handler) → boolean`
- **说明：** 取消订阅指定类型的事件处理器。
- **参数：**
  - `eventType` — 事件类型
  - `handler` — 事件处理器
  - `<E>` — 事件类型参数
- **返回：** 取消成功时返回 true

#### `publish(DomainEvent event) → void`
- **说明：** 异步发布事件（即发即弃）。
- **参数：**
  - `event` — 领域事件

#### `publishSync(DomainEvent event) → Object`
- **说明：** 同步发布事件，阻塞直到所有处理器完成，返回最后一个处理器的结果。
- **参数：**
  - `event` — 领域事件
- **返回：** 最后一个处理器的返回值

#### `clear() → void`
- **说明：** 清除所有已注册的处理器。 / public static void clear()


## EventHandler

**类型：** interface

领域事件处理器的函数式接口。
