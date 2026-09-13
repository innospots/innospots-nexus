# Watchers（`core.watcher`）

带 TLC 传播线程池的后台轮询循环。

## IWatcher

**类型：** interface，继承 `Runnable`

| 方法 | 说明 |
|--------|-------------|
| `name()` | 日志标签 |
| `isRunning()` | 循环是否活跃 |
| `check()` | 周期前门禁（默认 `true`） |
| `execute()` | 单周期；返回睡眠毫秒数（≤0 → 默认间隔） |
| `stop()` | 当前周期结束后停止 |

## AbstractWatcher

**类型：** class

模板实现：

```text
while (running && runningCondition)
    if check() → execute() → sleep(interval)
```

仅子类化 `execute()`；将名称、间隔和停止条件传给构造函数。

## WatcherSupervisor

**类型：** class，实现 `AutoCloseable`

- 通过 `ThreadPoolBuilder` 构建 `NexusThreadPoolExecutor`
- `register(IWatcher)` — 提交到线程池
- `close()` — 关闭线程池并停止 watchers

在宿主启动时使用 try-with-resources 实现干净关闭。

## 扩展模式

1. 在所属上层模块（非 core）中子类化 `AbstractWatcher`。
2. 保持 watcher 逻辑业务中立，或在该模块中实现领域特定逻辑。
3. 在启动期间将实例注册到共享的 `WatcherSupervisor`。
