# 包 `http`

## HttpResult

**类型：** record

HTTP 请求结果的不可变记录。包含状态码、原因短语、响应体与响应头。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `statusCode` | `int` | HTTP 状态码 |
| `reasonPhrase` | `String` | 原因短语 |
| `body` | `String` | 响应体 |
| `headers` | `Map<String, List<String>>` | 响应头（多值） |

### 方法

#### `isSuccessful() → boolean`

- **说明：** 判断响应是否成功（状态码 2xx）。
- **返回：** 成功时返回 {@code true}
