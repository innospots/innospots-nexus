# 包 `execution`

## ExecutionContext

**类型：** class

单次运行的执行上下文。包含不可变输入参数（{@code inputs}）与执行器可在运行期间读写的可变工作内存（{@code context}）。

### 方法

#### `create(String executionId) → ExecutionContext`

- **说明：** 使用给定执行 ID 及空 inputs/context 映射创建上下文。

#### `executionId() → String`


#### `input(String key, Object value) → ExecutionContext`

- **说明：** 设置输入参数。输入属于不可变的执行边界。

#### `getInput(String key) → Object`

- **说明：** 按键获取输入参数。

#### `getInputString(String key) → String`

- **说明：** 将输入参数作为 String 获取，不存在时返回 null。

#### `getInputInteger(String key) → Integer`

- **说明：** 将输入参数作为 Integer 获取，不存在时返回 null。

#### `getInputLong(String key) → Long`

- **说明：** 将输入参数作为 Long 获取，不存在时返回 null。

#### `put(String key, Object value) → ExecutionContext`

- **说明：** 向可变工作上下文写入值。与 inputs 不同，context 可在执行期间被执行器读写。
- **参数：**
  - `key` — 上下文键
  - `value` — 上下文值
- **返回：** 当前上下文实例，支持链式调用

#### `get(String key) → Object`

- **说明：** 按键获取上下文值。

#### `getString(String key) → String`

- **说明：** 将上下文值作为 String 获取，不存在时返回 null。

#### `getInteger(String key) → Integer`

- **说明：** 将上下文值作为 Integer 获取，不存在时返回 null。

#### `getLong(String key) → Long`

- **说明：** 将上下文值作为 Long 获取，不存在时返回 null。

## ExecutionRecord

**类型：** class

已完成执行的不变记录。捕获执行 ID、执行器标识、状态、时间、上下文快照、输出及状态消息。

### 方法

#### `executionId() → String`

- **说明：** 返回执行 ID。
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

#### `message() → String`

- **说明：** 返回状态消息。
- **返回：** 状态消息

## ExecutionStatus

**类型：** enum

执行从创建到完成的生命周期状态。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `CREATED` | 已创建 |
| `STARTING` | 启动中 |
| `READY` | 就绪 |
| `PENDING` | 等待中 |
| `RUNNING` | 运行中 |
| `STOPPING` | 停止中 |
| `STOPPED` | 已停止 |
| `SUCCESS` | 成功 |
| `FAILED` | 失败 |

## Executor

**类型：** interface

核心执行单元接口。每个执行器拥有唯一标识符， 接收 {@link ExecutionContext} 并产出输出结果。
