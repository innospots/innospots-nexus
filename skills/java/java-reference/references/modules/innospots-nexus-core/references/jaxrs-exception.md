# 包 `jaxrs.exception`

## JaxRsExceptionSupport

**类型：** class（final，工具型）

将 `NexusException` 转为 JAX-RS `Response`（legacy `R` 形态）；统一异常日志记录。

### 方法

#### `logWebApplicationFailure(WebApplicationException exception)`
- **说明：** 记录 JAX-RS `WebApplicationException`（含路由未匹配的 `NotFoundException`）；5xx 为 ERROR，其余 WARN
- **参数：**
  - `exception` — Web 应用异常

#### `toWebApplicationResponse(WebApplicationException exception) → Response`
- **说明：** 将 `WebApplicationException` 转为响应（不包装为 `R`）
- **参数：**
  - `exception` — Web 应用异常
- **返回：** 原生响应或 500 兜底

#### `logUnhandledFailure(Throwable failure)`
- **说明：** 记录未捕获异常（映射为 `NexusStatusCode.SYSTEM_ERROR` 前），含完整堆栈
- **参数：**
  - `failure` — 原始失败

#### `logNexusFailure(NexusException exception)`
- **说明：** 记录 `NexusException`；5xx 或存在 cause 时输出 ERROR 与堆栈，其余为 WARN
- **参数：**
  - `exception` — 平台异常

#### `toResponse(NexusException exception) → Response`
- **说明：** 将 `NexusException` 转为当前请求作用域下的响应
- **参数：**
  - `exception` — 平台异常
- **返回：** 统一错误响应

#### `toResponse(ContainerRequestContext requestContext, NexusException exception) → Response`
- **说明：** 将 `NexusException` 转为携带 requestId 头的统一错误响应
- **参数：**
  - `requestContext` — 当前请求（可为 `null`）
  - `exception` — 平台异常
- **返回：** 统一错误响应

## NexusExceptionMapper

**类型：** class，实现 `ExceptionMapper<NexusException>`，`@Provider`

将 `NexusException` 映射为统一 HTTP 错误响应。

### 构造方法

#### `NexusExceptionMapper(JaxRsExceptionSupport exceptionSupport)`
- **参数：**
  - `exceptionSupport` — 共享异常支持

### 方法

#### `toResponse(NexusException exception) → Response`
- **说明：** 先 `logNexusFailure` 记录，再委托 `JaxRsExceptionSupport.toResponse`
- **返回：** 统一错误响应

## WebApplicationExceptionMapper

**类型：** class，实现 `ExceptionMapper<WebApplicationException>`，`@Provider`

保留 JAX-RS 原生 HTTP 状态（如 404），避免被 `ThrowableExceptionMapper` 误映射为系统错误。

### 构造方法

#### `WebApplicationExceptionMapper(JaxRsExceptionSupport exceptionSupport)`
- **参数：**
  - `exceptionSupport` — 共享异常支持

### 方法

#### `toResponse(WebApplicationException exception) → Response`
- **说明：** 委托 `JaxRsExceptionSupport.toWebApplicationResponse`
- **返回：** 原生响应或 500 兜底

## ThrowableExceptionMapper

**类型：** class，实现 `ExceptionMapper<Throwable>`，`@Provider`

未捕获异常兜底映射。

### 构造方法

#### `ThrowableExceptionMapper(JaxRsExceptionSupport exceptionSupport)`
- **参数：**
  - `exceptionSupport` — 共享异常支持

### 方法

#### `toResponse(Throwable exception) → Response`
- **说明：** `WebApplicationException` 走原生响应；其余记录后包装为 `NexusException(SYSTEM_ERROR)` 再映射
- **参数：**
  - `exception` — 未捕获异常
- **返回：** 统一错误响应