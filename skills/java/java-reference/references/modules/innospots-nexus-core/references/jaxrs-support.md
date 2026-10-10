# 包 `jaxrs.support`

## HttpHeaderNames

**类型：** class（final，常量容器）

Jakarta REST 请求使用的共享 HTTP 头名称。

### 常量

| 常量 | 说明 |
|------|------|
| `REQUEST_ID = "X-Request-Id"` | 错误响应携带的请求 ID 头 |
| `AUTHORIZATION = "Authorization"` | 认证头 |

## RequestProperties

**类型：** class（final，常量容器）

Jakarta REST 请求属性键（存入 `ContainerRequestContext` 属性）。

### 常量

| 常量 | 说明 |
|------|------|
| `REQUEST_ID` | 请求 ID 属性键（`RequestProperties` 类名 + `.requestId`） |
| `AUTHORIZATION_CONTEXT` | 授权上下文属性键（`RequestProperties` 类名 + `.authorizationContext`） |

## RequestScope

**类型：** class（final）

当前 JAX-RS 请求的 `ThreadLocal` 绑定，供 `ExceptionMapper` 读取请求元数据；
由宿主过滤器在请求入口 `bind`、结束时 `clear`。

### 方法

#### `bind(ContainerRequestContext requestContext)`（static）
- **说明：** 在请求入口绑定上下文
- **参数：**
  - `requestContext` — 当前请求

#### `current() → ContainerRequestContext`（static）
- **说明：** 返回当前请求上下文
- **返回：** 无绑定时为 `null`

#### `clear()`（static）
- **说明：** 请求结束时清理绑定