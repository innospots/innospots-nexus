# 包 `exception`

## NexusException

**类型：** class

平台基础运行时异常。携带机器可读的错误码（见 StatusCode）、 人类可读的消息以及可选的国际化展示信息供前端渲染。 请使用静态工厂方法（{@link #build(StatusCode)} 和 {@link #build(String, String)}）而非直接调用构造函数。

### 方法

#### `build(StatusCode statusCode) → NexusException`
- **说明：** 基于 StatusCode 构建异常，使用其完整码与摘要消息。
- **参数：**
  - `statusCode` — 状态码
- **返回：** 异常实例

#### `build(StatusCode statusCode, I18nObject display) → NexusException`
- **说明：** 基于 StatusCode 构建异常，附带国际化展示消息。
- **参数：**
  - `statusCode` — 状态码
  - `display` — 国际化展示信息
- **返回：** 异常实例

#### `build(StatusCode statusCode, I18nObject display, Throwable cause) → NexusException`
- **说明：** 基于 StatusCode 构建异常，附带展示消息与原始原因。
- **参数：**
  - `statusCode` — 状态码
  - `display` — 国际化展示信息
  - `cause` — 原始异常
- **返回：** 异常实例

#### `build(StatusCode statusCode, String message) → NexusException`
- **说明：** 基于 StatusCode 构建异常，使用覆盖消息。
- **参数：**
  - `statusCode` — 状态码，提供机器可读错误码
  - `message` — 人类可读消息；空白时回退到状态码摘要
- **返回：** 携带状态码完整码的异常

#### `build(StatusCode statusCode, Throwable cause) → NexusException`
- **说明：** 基于 StatusCode 构建异常，附带原始原因。
- **参数：**
  - `statusCode` — 状态码，提供机器可读错误码与摘要
  - `cause` — 原始失败原因
- **返回：** 携带状态码完整码的异常

#### `build(String code, String message) → NexusException`
- **说明：** 使用机器可读错误码与人类可读消息构建异常。
- **参数：**
  - `code` — 错误码
  - `message` — 消息
- **返回：** 异常实例

#### `build(String code, String message, Throwable cause) → NexusException`
- **说明：** 使用错误码、消息与原始原因构建异常。
- **参数：**
  - `code` — 错误码
  - `message` — 消息
  - `cause` — 原始异常
- **返回：** 异常实例

#### `code() → String`
- **说明：** 返回机器可读错误码。
- **返回：** 错误码

#### `display() → I18nObject`
- **说明：** 返回国际化展示消息，可能为 null。
- **返回：** 展示信息
