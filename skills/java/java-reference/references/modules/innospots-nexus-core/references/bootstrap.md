# 包 `bootstrap`

## NexusStartup

**类型：** class

启动后初始化编排入口；宿主在配置期组装任务，运行期仅调用 {@link #run()}。

### 方法

#### `builder() → Builder`
- **说明：** 创建启动编排构建器。 public static Builder builder()

#### `run() → void`
- **说明：** 按 NexusStartupTask 顺序执行全部任务；任一步失败则中止。 / public void run()

#### `build() → NexusStartup`
- **说明：** 构建启动编排实例。
- **返回：** 不可变启动编排


## NexusStartupContext

**类型：** class

启动任务执行过程中跨步骤传递的轻量上下文。

### 方法

#### `putAttribute(String key, Object value) → void`
- **说明：** 存储启动属性。
- **参数：**
  - `key` — 属性键
  - `value` — 属性值；为 null 时移除该键

#### `getAttribute(String key, Class<T> type) → Optional<T>`
- **说明：** 读取指定类型的启动属性。
- **参数：**
  - `key` — 属性键
  - `type` — 期望的值类型
  - `<T>` — 值类型
- **返回：** 存在且类型匹配时的属性值


## NexusStartupTask

**类型：** interface

框架无关的启动后初始化步骤。
