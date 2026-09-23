# 包 `watcher.runtime`

## WatcherSupervisor

**类型：** class

管理后台 {@link IWatcher} 线程池的监督器。

### 方法

#### `register(IWatcher watcher) → void`

- **说明：** 在线程池中注册并启动 Watcher。
- **参数：**
  - `watcher` — 待注册的 Watcher

#### `activeCount() → int`

- **说明：** 返回当前已注册的 Watcher 数量。

#### `close() → void`

- **说明：** 优雅关闭：停止全部 Watcher，等待 1 秒后关闭线程池。
