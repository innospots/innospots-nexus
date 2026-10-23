# 包 `support`

## `ConsoleExceptions`

**类型：** class（final）

Console 模块统一的 `NexusException` 构建入口。创建异常时记录 error 日志（不输出堆栈），
再返回异常供调用方抛出。

### 静态方法

- `build(StatusCode statusCode)`
- `build(StatusCode statusCode, I18nObject display)`
- `build(StatusCode statusCode, I18nObject display, Throwable cause)`
- `build(StatusCode statusCode, String message)`
- `build(StatusCode statusCode, Throwable cause)`
- `build(String code, String message)`
- `build(String code, String message, Throwable cause)`

日志定位使用 `StackWalker` 找到 `ConsoleExceptions` 之外的第一帧。
