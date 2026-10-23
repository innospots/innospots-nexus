# 包 `logger.support`

## AuditLogInvocationState

**类型：** record

宿主在响应阶段组装的不可变审计调用元数据。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `grant` | `ResolvedEndpointGrant` | 已解析的 endpoint grant |
| `className` | `String` | 资源实现类名 |
| `methodName` | `String` | 被审计方法名 |
| `httpMethod` | `String` | HTTP 方法 |
| `httpPath` | `String` | 请求路径 |
| `startTimeMillis` | `long` | 请求开始时间 |

## AuditLogRecorder

**类型：** class（final）

将 resolved grant 与调用结果组装为 `InvocationLogContext`，再交给 `InvocationLogHandler`。
