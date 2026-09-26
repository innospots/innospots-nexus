# 包 `logger.handler`

## PersistenceInvocationLogHandler

**类型：** class

InvocationLogHandler 的持久化实现。 将已完成的 InvocationLogContext 适配到审计日志操作器， 后者将其存入审计日志领域。持久化失败会记录日志但 永不传播，因为审计日志不得干扰被审计 操作——本处理器从拦截器的 finally 块中调用。

### 方法

#### `handle(InvocationLogContext context) → void`
- **说明：** 处理。
- **参数：**
  - `context` — 调用上下文
