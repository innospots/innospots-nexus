# 包 `execution`

## ExecutionContext

**类型：** class

单次运行的执行上下文。包含不可变输入参数（inputs）与执行器可在运行期间读写的可变工作内存（context）。

### 方法

#### `create(String executionId) → ExecutionContext`
- **说明：** 使用给定执行 ID 及空 inputs/context 映射创建上下文。 / public static ExecutionContext create(String executionId)

#### `input(String key, Object value) → ExecutionContext`
- **说明：** 设置输入参数。输入属于不可变的执行边界。 / public ExecutionContext input(String key, Object value)

#### `getInput(String key) → Object`
- **说明：** 按键获取输入参数。 public Object getInput(String key)

#### `getInputString(String key) → String`
- **说明：** 将输入参数作为 String 获取，不存在时返回 null。 public String getInputString(String key)

#### `getInputInteger(String key) → Integer`
- **说明：** 将输入参数作为 Integer 获取，不存在时返回 null。 public Integer getInputInteger(String key)

#### `getInputLong(String key) → Long`
- **说明：** 将输入参数作为 Long 获取，不存在时返回 null。 public Long getInputLong(String key)

#### `inputs() → Map<String, Object>`
- **说明：** 返回所有输入参数的不可修改视图。 public Map inputs()

#### `put(String key, Object value) → ExecutionContext`
- **说明：** 向可变工作上下文写入值。与 inputs 不同，context 可在执行期间被执行器读写。
- **参数：**
  - `key` — 上下文键
  - `value` — 上下文值
- **返回：** 当前上下文实例，支持链式调用

#### `get(String key) → Object`
- **说明：** 按键获取上下文值。 public Object get(String key)

#### `getString(String key) → String`
- **说明：** 将上下文值作为 String 获取，不存在时返回 null。 public String getString(String key)

#### `getInteger(String key) → Integer`
- **说明：** 将上下文值作为 Integer 获取，不存在时返回 null。 public Integer getInteger(String key)

#### `getLong(String key) → Long`
- **说明：** 将上下文值作为 Long 获取，不存在时返回 null。 public Long getLong(String key)

#### `context() → Map<String, Object>`
- **说明：** 返回可变上下文映射的不可修改视图。 public Map context()


## ExecutionRecord

**类型：** class

已完成执行的不变记录。捕获执行 ID、执行器标识、状态、时间、上下文快照、输出及状态消息。

### 方法

#### `executionId() → String`
- **说明：** 创建执行记录。
- **参数：**
  - `executionId` — 执行 ID
  - `executorId` — 执行器 ID
  - `status` — 执行状态
  - `startTime` — 开始时间
  - `endTime` — 结束时间
  - `context` — 上下文快照
  - `output` — 输出快照
  - `message` — 状态消息
- **返回：** 执行 ID

#### `executorId() → String`
- **说明：** 返回执行器 ID。
- **返回：** 执行器 ID

#### `status() → ExecutionStatus`
- **说明：** 返回执行状态。
- **返回：** 执行状态

#### `startTime() → LocalDateTime`
- **说明：** 返回开始时间。
- **返回：** 开始时间

#### `endTime() → LocalDateTime`
- **说明：** 返回结束时间。
- **返回：** 结束时间

#### `context() → Map<String, Object>`
- **说明：** 返回上下文快照。
- **返回：** 上下文映射

#### `output() → Map<String, Object>`
- **说明：** 返回输出快照。
- **返回：** 输出映射

#### `message() → String`
- **说明：** 返回状态消息。
- **返回：** 状态消息


## ExecutionStatus

**类型：** enum

执行从创建到完成的生命周期状态。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `CREATED` | — |
| `STARTING` | — |
| `READY` | — |
| `PENDING` | — |
| `RUNNING` | — |
| `STOPPING` | — |
| `STOPPED` | — |
| `SUCCESS` | — |
| `FAILED` | — |


## Executor

**类型：** interface

核心执行单元接口。每个执行器拥有唯一标识符， 接收 ExecutionContext 并产出输出结果。
