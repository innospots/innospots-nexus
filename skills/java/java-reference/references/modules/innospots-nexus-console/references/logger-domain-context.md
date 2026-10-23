# 包 `logger.domain.context`

## InvocationLogContext

**类型：** record

单次被拦截调用的框架无关描述。由过滤器或拦截器适配器在
`@EndpointGrant(auditLog = true)` 方法周围组装，并交给
`InvocationLogHandler` 持久化。不含框架类型，任意 Java 运行时均可产生并消费。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `className` | `String` | 被拦截方法的声明类名 |
| `methodName` | `String` | 被拦截的方法名 |
| `action` | `String` | 由 `EndpointGrant.action()` 声明的操作类型 |
| `arguments` | `Object[]` | 捕获的方法参数；未记录时为空数组 |
| `result` | `Object` | 捕获的返回值；未记录或失败时为 null |
| `exception` | `Throwable` | 抛出的异常；成功时为 null |
| `startTime` | `long` | 调用开始时间（epoch 毫秒） |
| `endTime` | `long` | 调用结束时间（epoch 毫秒） |
| `actor` | `String` | 操作用户身份；未知时为空字符串 |
| `operationMessage` | `String` | 注解声明的操作说明（按当前语言环境解析） |
| `httpPath` | `String` | HTTP 方法与路径；非 HTTP 调用时可为空 |
| `target` | `String` | 由 `EndpointGrant.name()` 声明的操作对象 |

### 方法

#### `elapsedMillis() → long`
- **说明：** 执行耗时（毫秒）。
- **返回：** endTime endTime 减 startTime

#### `success() → boolean`
- **说明：** 调用是否未抛异常完成。
- **返回：** true 未捕获异常时为 true
