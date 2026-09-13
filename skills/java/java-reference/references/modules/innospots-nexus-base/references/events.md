# 包 `events`

## DomainEvent

**Type:** interface

所有领域事件的基接口。

## EventBus

**Type:** class

内存事件总线。按事件类型存储处理器；子类匹配使用
`Class.isAssignableFrom`。

| 方法 | 说明 |
|--------|-------------|
| `subscribe(eventType, handler)` | 注册处理器 |
| `unsubscribe(eventType, handler)` | 移除处理器；返回是否已移除 |
| `publish(event)` | 即发即忘的异步通知 |
| `publishSync(event)` | 阻塞；返回最后一个处理器的结果 |
| `clear()` | 移除所有处理器（测试 / 关闭） |

## EventHandler

**Type:** interface

特定 `DomainEvent` 子类型的函数式处理器。
