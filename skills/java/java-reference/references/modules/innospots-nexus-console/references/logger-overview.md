# 审计与调用日志

| 类型 | 说明 |
|------|------|
| `EndpointGrant` / `ResolvedEndpointGrant` | `auditLog`、`recordArgs` 与操作名称/文案 |
| `ConsoleAuditLogFilter` | 从请求绑定读取 grant，在响应阶段记录审计 |
| `AuditLogRecorder` / `InvocationLogHandler` | 将调用元数据交由持久化处理器 |
| `AuditLogEntity` / `AuditLogVo` | `nx_audit_log` 持久化与查询视图 |

包级参考：`logger.md`、`logger-converter.md`、`logger-domain-*.md`、`logger-endpoint.md`、
`logger-handler.md`、`logger-operator.md`、`logger-status.md`、`logger-support.md`。
