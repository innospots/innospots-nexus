# 包 `logger`

## Callback

**类型：** interface

包装被拦截的方法体。

## InvocationLogHandler

**类型：** interface

通用拦截处理器端口。 实现保持框架无关，例如可通过 {@code AuditLogDao} 写入 {@code AuditLogEntity}， 或将上下文发布到其他接收端。</p>
