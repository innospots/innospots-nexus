# 包 `domain.response`

## PageResult

**类型：** record

分页 API 响应包装器。在构造时校验分页边界，并根据总记录数与每页大小计算总页数。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `records` | `List<T>` | 记录列表 |
| `pageNo` | `long` | 从 1 开始的页码 |
| `pageSize` | `long` | 每页记录数 |
| `total` | `long` | 总记录数 |
| `pages` | `long` | — |

### 构造方法

#### `PageResult()`

- **说明：** 紧凑构造器校验分页参数并确保记录列表永不为 null。

### 方法

#### `of(List<T> records, long pageNo, long pageSize, long total) → PageResult<T>`

- **说明：** 创建自动计算总页数的分页结果。
- **参数：**
  - `records` — 记录列表
  - `pageNo` — 从 1 开始的页码
  - `pageSize` — 每页记录数
  - `total` — 总记录数
- **返回：** 分页结果

#### `empty(long pageNo, long pageSize) → PageResult<T>`

- **说明：** 返回指定页码与大小的空分页结果。
- **参数：**
  - `pageNo` — 页码
  - `pageSize` — 每页记录数
- **返回：** 空分页结果

#### `hasNext() → boolean`

- **说明：** 判断是否有下一页。
- **返回：** 有下一页时返回 {@code true}

#### `hasPrevious() → boolean`

- **说明：** 判断是否有上一页。
- **返回：** 有上一页时返回 {@code true}

## R

**类型：** record

通用 API 响应包装器，包含成功/失败状态、结果码、消息、可选数据载荷， 以及失败时供前端渲染的国际化展示消息。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `success` | `boolean` | — |
| `code` | `String` | 错误码 |
| `message` | `String` | 错误消息 |
| `data` | `T` | 响应数据 |
| `display` | `I18nObject` | 国际化展示信息 |

### 方法

#### `ok() → R<T>`

- **说明：** 返回数据为 null 的成功响应。
- **返回：** 成功响应

#### `ok(T data) → R<T>`

- **说明：** 返回包装给定数据的成功响应。
- **参数：**
  - `data` — 响应数据
- **返回：** 成功响应

#### `fail(String code, String message) → R<T>`

- **说明：** 返回带错误码与消息的失败响应。
- **参数：**
  - `code` — 错误码
  - `message` — 错误消息
- **返回：** 失败响应

#### `fail(String code, String message, T data) → R<T>`

- **说明：** 返回带错误码、消息与数据载荷的失败响应。
- **参数：**
  - `code` — 错误码
  - `message` — 错误消息
  - `data` — 数据载荷
- **返回：** 失败响应

#### `fail(String code, String message, I18nObject display) → R<T>`

- **说明：** 返回带错误码、消息与前端展示信息的失败响应。
- **参数：**
  - `code` — 错误码
  - `message` — 错误消息
  - `display` — 国际化展示信息
- **返回：** 失败响应

#### `fail(String code, String message, T data, I18nObject display) → R<T>`

- **说明：** 返回带错误码、消息、数据与展示信息的失败响应。
- **参数：**
  - `code` — 错误码
  - `message` — 错误消息
  - `data` — 数据载荷
  - `display` — 国际化展示信息
- **返回：** 失败响应

#### `fail(StatusCode statusCode) → R<T>`

- **说明：** 基于 {@link StatusCode} 返回失败响应。
- **参数：**
  - `statusCode` — 提供码、摘要与展示信息的状态码
- **返回：** 无数据的失败响应

#### `fail(StatusCode statusCode, T data) → R<T>`

- **说明：** 基于 {@link StatusCode} 返回带载荷的失败响应。
- **参数：**
  - `statusCode` — 提供码、摘要与展示信息的状态码
  - `data` — 可选数据载荷
- **返回：** 失败响应

#### `from(NexusException exception) → R<T>`

- **说明：** 将 {@link NexusException} 映射为失败响应。
- **参数：**
  - `exception` — 平台异常
- **返回：** 携带异常码、消息与展示信息的失败响应
