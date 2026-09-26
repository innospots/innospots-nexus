# 包 `logger.domain.context`

## InvocationLogContext

**类型：** record

单次被拦截调用的框架无关描述。 由拦截器适配器在 @AuditLog 标注方法周围组装，并交给 com.innospots.nexus.console.logger.InvocationLogHandler 用于持久化。 不含框架类型，任意 Java 运行时均可产生并消费。本记录是从拦截器边界传入 审计日志操作器的标准领域上下文。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `className` | `String` | 被拦截方法的声明类名 |
| `methodName` | `String` | 被拦截的方法名 |
| `action` | `String` | 由 com.innospots.nexus.console.logger.AuditLog 注解声明的业务动作编码 |
| `arguments` | `Object[]` | 捕获的方法参数；未记录时为空数组 |
| `result` | `Object` | 捕获的返回值；未记录或失败时为 null |
| `exception` | `Throwable` | 抛出的异常；成功时为 null |
| `startTime` | `long` | 调用开始时间（epoch 毫秒） |
| `endTime` | `long` | 调用结束时间（epoch 毫秒） |
| `actor` | `String` | 操作用户身份；未知时为空字符串 |

### 方法

#### `elapsedMillis() → long`
- **说明：** 执行耗时（毫秒）。
- **返回：** endTime endTime 减 startTime

#### `success() → boolean`
- **说明：** 调用是否未抛异常完成。
- **返回：** true 未捕获异常时为 true
