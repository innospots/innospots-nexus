# 包 `thread`

## NexusThreadPoolExecutor

**类型：** class

自定义 {@link ThreadPoolExecutor}，在提交线程与工作线程之间捕获并传播 {@link TLC} 上下文。

### 方法

#### `poolName() → String`

- **说明：** 返回可读性良好的线程池名称。
- **返回：** 线程池名称

#### `hasAvailableThread() → boolean`

- **说明：** 判断是否至少有一个线程可立即处理任务。
- **返回：** 有可用线程时返回 {@code true}

#### `availableThreadCount() → int`

- **说明：** 返回当前未在执行任务的线程数。
- **返回：** 可用线程数

#### `execute(Runnable command) → void`


#### `submit(Runnable task) → Future<?>`


#### `submit(Callable<T> task) → Future<T>`

## Scope

**类型：** record

可自动关闭的作用域，在 close 时恢复先前的上下文。 由 {@link TLC#scope(Map)} 内部使用。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `previous` | `Map<String, Object>` | 关闭时需恢复的先前上下文 |

### 方法

#### `close() → void`
