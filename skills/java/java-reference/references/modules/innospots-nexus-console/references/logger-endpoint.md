# 包 `logger.endpoint`

## AuditLogEndpoint

**类型：** interface，`@EndpointModule("logger")`

审计日志查询 JAX-RS 契约；类级 `@Path` 由 platform 实现绑定。

### 端点

| 方法 | 相对路径 | 返回 |
|------|----------|------|
| GET `pageAuditLogs` | `/` | `R<PageResult<AuditLogVo>>` |
| GET `getAuditLog` | `/{auditLogId}` | `R<AuditLogVo>` |
