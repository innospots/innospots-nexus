# 包 `logger`

## AuditLog

**类型：** annotation

标记方法为审计日志操作。 框架无关标记：本包自身不执行拦截。 拦截器适配器（AspectJ、Byte Buddy、CDI 等）位于包外； portal 读取本注解以驱动共享 LogExecutor 例程。


## InvocationLogHandler

**类型：** interface

通用拦截处理器端口。 接收完整组装的 InvocationLogContext，负责将其持久化或转发。 实现保持框架无关，例如可通过 AuditLogDao 写入 AuditLogEntity， 或将上下文发布到其他接收端。


## LogExecutor

**类型：** class

所有框架适配器共享的可复用拦截例程。 使用 Callback 包装调用，记录耗时与结果，并委托给 InvocationLogHandler。框架适配器（AspectJ、Byte Buddy、 CDI 等）在标注方法周围调用 {@link #execute}；本类从不 依赖任何拦截框架。

### 方法

#### `execute(AuditLog auditLog,
            String className,
            String methodName,
            String actor,
            Object[] arguments,
            Callback callback) → Object`
- **说明：** 创建委托给定处理器的执行器。
- **参数：**
  - `handler` — 目标处理器，不可为 null
  - `auditLog` — 声明捕获内容的注解
  - `className` — 被拦截方法的声明类名
  - `methodName` — 被拦截的方法名
  - `actor` — 操作用户身份；未知时为 null
  - `arguments` — 被拦截的方法参数；无则为 null
  - `callback` — 实际的方法调用
- **返回：** 回调结果
