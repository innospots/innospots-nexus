# 日志与审计

## 审计持久化

| 表 | 实体 | 基类 |
|-------|--------|------|
| `nx_audit_log` | `AuditLogEntity` | `BaseEntity` |

按 `operated_time`、`action`、`actor`、`execution_result` 建立索引。

## 调用管道

| 类型 | 角色 |
|------|------|
| `InvocationLogContext` | 捕获单次调用的调用点元数据 |
| `InvocationLogOperator` | 持久化结构化日志行 |
| `PersistenceInvocationLogHandler` | 桥接到 DAO 的处理器 |
| `LogExecutor` | 日志写入的异步/同步分发 |
| `AuditLogDao` | MyBatis `BaseMapper` |

## 入口插件

`LoggerEntryPlugin` — 插件 ID `com.innospots.nexus.console.logger`。

提供审计日志浏览的控制台 UI 入口；查询端点可在 kernel 实现列表契约时添加，
遵循相同的 `R<PageResult<>>` 模式。

## 边界

- 控制台定义存储形态和日志 SPI 钩子。
- **审计什么**（业务操作、PII 策略）由 kernel/platform 决定。
- 发出审计事件的 Servlet/请求过滤器属于适配器模块。
