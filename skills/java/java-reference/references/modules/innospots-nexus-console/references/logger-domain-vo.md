# 包 `logger.domain.vo`

## AuditLogVo

**类型：** record

审计日志列表与详情视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `logId` | `String` | 日志 ID |
| `occurredAt` | `String` | ISO 本地时间 |
| `operatorName` | `String` | 操作者显示名 |
| `operatorId` | `String` | 操作者 ID |
| `securityRealm` | `String` | 安全域 |
| `operationType` | `String` | EndpointGrant action |
| `target` | `String` | EndpointGrant name |
| `method` | `String` | HTTP 方法 + 路径 |
| `result` | `String` | `SUCCESS` / `FAILURE` |
| `durationMs` | `Long` | 耗时毫秒 |
| `errorCode` | `String` | HTTP 或业务状态码 |
| `summary` | `String` | EndpointGrant 操作说明 |
| `failureDetail` | `String` | 失败详情 |
| `requestPayload` | `String` | 参数 JSON |
