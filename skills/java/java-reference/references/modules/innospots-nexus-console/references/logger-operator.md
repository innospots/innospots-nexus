# 包 `logger.operator`

## InvocationLogOperator

**类型：** class

将被拦截调用持久化到审计日志领域的数据操作器。 将其映射到 {@link AuditLogEntity} 并写入 {@link AuditLogDao}。该 Operator 不拥有业务工作流：它仅 将上下文转换为领域的持久化模型。</p>

### 方法

#### `record(InvocationLogContext context) → void`

- **说明：** 将被拦截调用持久化为审计日志记录。 被审计操作的结果而存在，因此本写入刻意在无 声明式事务中运行且从不加入外层事务。</p>
- **参数：**
  - `context` — 已组装的调用数据，永不为 null
