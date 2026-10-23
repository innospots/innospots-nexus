# 包 `logger.domain.request`

## AuditLogPageRequest

**类型：** class（final），`@BeanParam`

审计日志分页查询参数。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `timeRange` | `String` | `last24h`、`last7d`、`last30d` |
| `operator` | `String` | 操作者模糊匹配 |
| `operationType` | `String` | `EndpointGrantAction` 枚举名 |
| `result` | `String` | `SUCCESS` / `FAILURE` |
| `keyword` | `String` | 路径、动作或消息关键字 |
| `pageNo` | `long` | 页码，默认 1 |
| `pageSize` | `long` | 分页大小，默认 20 |
