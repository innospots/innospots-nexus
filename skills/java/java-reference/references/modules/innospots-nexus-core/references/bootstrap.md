# Bootstrap（`core.bootstrap`）

框架中立的启动后编排。宿主应用在配置时组装任务，并在运行时调用一次 `NexusStartup.run()`。

## NexusStartupTask

**类型：** interface

| 方法 | 说明 |
|--------|-------------|
| `name()` | 任务标签，用于日志 |
| `order()` | 数值越小越先执行 |
| `run(NexusStartupContext)` | 单步启动；失败则中止整个链 |

## NexusStartupContext

**类型：** class

跨步骤属性容器：

| 方法 | 说明 |
|--------|-------------|
| `putAttribute(key, value)` | 存储；`null` 表示移除 |
| `getAttribute(key, Class<T>)` | 类型化可选读取 |

插件特定键（如已安装插件列表）应作为命名常量放在上层模块中——不在 core 类型中定义。

## NexusStartup

**类型：** class

```text
NexusStartup.builder()
    .task(myTask)
    .task(anotherTask)
    .build()
    .run();
```

任务按 `order()` 然后 `name()` 排序。每次 `run()` 创建新的上下文。
