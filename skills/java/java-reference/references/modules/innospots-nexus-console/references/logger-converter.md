# 包 `logger.converter`

## AuditLogConverter

**类型：** class（final，静态工具）

将 `AuditLogEntity` 转为列表和详情共用的 `AuditLogVo`。

### 方法

#### `toVo(AuditLogEntity entity) → AuditLogVo`（static）
- **说明：** 格式化操作时间；历史记录缺少 target 时回退 action 字段
