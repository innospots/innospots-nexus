# 包 `watcher.runtime`

## AbstractWatcher

**类型：** class

后台 Watcher 的模板方法基类。 循环执行 {@link #check()} → {@link #execute()} → 休眠，直至调用 {@link #stop()} 或 runningCondition 返回 false。具体工作由子类通过 {@link #execute()} 定义。

### 方法

#### `run() → void`
- **说明：** /
- **参数：**
  - `name` — Watcher 名称，用于日志
  - `checkIntervalMillis` — 周期间隔休眠时间（毫秒）
  - `runningCondition` — 每轮检查的运行条件；为 false 时退出循环

#### `stop() → void`
- **说明：** 将运行标志设为 false；循环将在当前轮结束后退出。 public void stop()


## WatcherSupervisor

**类型：** class

管理后台 IWatcher 线程池的监督器。 实现 AutoCloseable，可通过 try-with-resources 优雅关闭。

### 方法

#### `register(IWatcher watcher) → void`
- **说明：** /
- **参数：**
  - `maxSize` — 最大并发 Watcher 数 / 线程池大小
  - `name` — 线程池名称，用于线程命名
  - `watcher` — 待注册的 Watcher

#### `activeCount() → int`
- **说明：** 返回当前已注册的 Watcher 数量。 public int activeCount()

#### `close() → void`
- **说明：** 优雅关闭：停止全部 Watcher，等待 1 秒后关闭线程池。 / public void close()
