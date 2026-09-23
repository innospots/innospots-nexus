# 包 `status`

## NexusStatusCode

**类型：** enum

平台级状态码枚举，提供双语（EN/ZH）消息与建议，按 {@link StatusCategory} 分组。 每个码的完整码格式为 {@code AIO + category(2) + local(4)}。

## StatusCategory

**类型：** enum

按关注领域对状态码进行分类。每个分类在完整状态码字符串中嵌入 2 位数字码， 并附带优先级级别（L=低、M=中、H=高、B=阻塞、C=严重）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `GENERAL` | 通用 |
| `INPUT_VALIDATION` | 输入校验 |
| `BUSINESS_RULE` | 业务规则 |
| `INTERNAL_ERROR` | 内部错误 |
| `PERMISSION_SECURITY` | 权限或安全 |
| `TRANSACTION_CONFLICT` | 事务冲突 |
| `EXTERNAL_FAILURE` | 外部故障 |
| `DATA_CONSISTENCY` | 数据一致性 |
| `CONFIGURATION` | 配置 |
| `COMPLIANCE` | 合规 |
| `RESOURCE_LIMIT` | 资源限制 |
| `BATCH_JOB` | 批处理作业 |
| `CHANNEL_INTERACTION` | 渠道交互 |
| `RESOURCE_DATA` | 资源数据 |
| `DATA_OPERATION` | 数据操作 |
| `FILE_OPERATION` | 文件操作 |
| `MIDDLEWARE` | 中间件 |
| `CRYPTO` | 密码学 |
| `SCRIPT` | 脚本 |
| `DATA_CONNECTION` | 数据连接 |
| `DATA_SCHEMA` | 数据模式 |
| `SQL_EXECUTION` | SQL 执行 |

## StatusCode

**类型：** interface

可组合状态码接口，由 3 字母模块码、{@link StatusCategory}（2 位数字）和 4 位本地码组成。 完整码格式为 {@code module + category + localCode}。
