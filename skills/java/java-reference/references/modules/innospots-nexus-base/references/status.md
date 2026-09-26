# 包 `status`

## NexusStatusCode

**类型：** enum

平台级状态码枚举，提供双语（EN/ZH）消息与建议，按 StatusCategory 分组。 每个码的完整码格式为 AIO + category(2) + local(4)。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `SUCCESS` | — |
| `INVALID_PARAMETER` | — |
| `CONFIG_ERROR` | — |
| `SERIALIZATION_FAILED` | — |
| `DATA_NOT_FOUND` | — |
| `RESOURCE_NOT_FOUND` | — |
| `NO_PERMISSION` | — |
| `AUTHENTICATION_FAILED` | — |
| `PASSWORD_ERROR` | — |
| `USER_NOT_FOUND` | — |
| `EXECUTION_FAILED` | — |
| `BUSINESS_ERROR` | — |
| `LIMIT_EXCEEDED` | — |
| `RETRY_FAILED` | — |
| `OPTIMISTIC_LOCK_FAILED` | — |
| `COMPILE_FAILED` | — |
| `INITIALIZATION_FAILED` | — |
| `SYSTEM_ERROR` | — |

### 方法

#### `findByFullCode(String fullCode) → Optional<NexusStatusCode>`
- **说明：** 按完整状态码字符串查找对应枚举常量（如 AIO000000）。
- **参数：**
  - `fullCode` — 完整 7 位状态码
- **返回：** 匹配的枚举常量，未找到时返回空


## StatusCategory

**类型：** enum

按关注领域对状态码进行分类。每个分类在完整状态码字符串中嵌入 2 位数字码， 并附带优先级级别（L=低、M=中、H=高、B=阻塞、C=严重）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `GENERAL` | — |
| `INPUT_VALIDATION` | — |
| `BUSINESS_RULE` | — |
| `INTERNAL_ERROR` | — |
| `PERMISSION_SECURITY` | — |
| `TRANSACTION_CONFLICT` | — |
| `EXTERNAL_FAILURE` | — |
| `DATA_CONSISTENCY` | — |
| `CONFIGURATION` | — |
| `COMPLIANCE` | — |
| `RESOURCE_LIMIT` | — |
| `BATCH_JOB` | — |
| `CHANNEL_INTERACTION` | — |
| `RESOURCE_DATA` | — |
| `DATA_OPERATION` | — |
| `FILE_OPERATION` | — |
| `MIDDLEWARE` | — |
| `CRYPTO` | — |
| `SCRIPT` | — |
| `DATA_CONNECTION` | — |
| `DATA_SCHEMA` | — |

### 方法

#### `code() → String`
- **说明：** 通用 GENERAL("00", "General", "L"), /** 输入校验 INPUT_VALIDATION("01", "Input validation", "H"), /** 业务规则 BUSINESS_RULE("02", "Business rule", "H"), /** 内部错误 INTERNAL_ERROR("03", "Internal error", "C"), /** 权限或安全 PERMISSION_SECURITY("04", "Permission or security", "C"), /** 事务冲突 TRANSACTION_CONFLICT("05", "Transaction conflict", "M"), /** 外部故障 EXTERNAL_FAILURE("06", "External failure", "C"), /** 数据一致性 DATA_CONSISTENCY("07", "Data consistency", "C"), /** 配置 CONFIGURATION("08", "Configuration", "H"), /** 合规 COMPLIANCE("09", "Compliance", "B"), /** 资源限制 RESOURCE_LIMIT("10", "Resource limit", "C"), /** 批处理作业 BATCH_JOB("11", "Batch job", "H"), /** 渠道交互 CHANNEL_INTERACTION("12", "Channel interaction", "M"), /** 资源数据 RESOURCE_DATA("13", "Resource data", "M"), /** 数据操作 DATA_OPERATION("14", "Data operation", "M"), /** 文件操作 FILE_OPERATION("15", "File operation", "M"), /** 中间件 MIDDLEWARE("16", "Middleware", "C"), /** 密码学 CRYPTO("17", "Cryptography", "C"), /** 脚本 SCRIPT("18", "Script", "C"), /** 数据连接 DATA_CONNECTION("19", "Data connection", "C"), /** 数据模式 DATA_SCHEMA("20", "Data schema", "C"), /** SQL 执行 SQL_EXECUTION("21", "SQL execution", "C"); private final String code; private final String label; private final String priority; StatusCategory(String code, String label, String priority) { this.code = code; this.label = label; this.priority = priority; } /** 返回 2 位数字分类码。
- **返回：** 分类码

#### `label() → String`
- **说明：** 返回分类标签。
- **返回：** 标签

#### `priority() → String`
- **说明：** 返回优先级级别。
- **返回：** 优先级（L/M/H/B/C）


## StatusCode

**类型：** interface

可组合状态码接口，由 3 字母模块码、StatusCategory（2 位数字）和 4 位本地码组成。 完整码格式为 module + category + localCode。


## StatusCodeRules

**类型：** class

状态码格式校验规则。 模块码：恰好 3 个大写字母（如 AIO） 分类：非 null 的 StatusCategory 本地码：恰好 4 位数字（如 0001）

### 方法

#### `requireValid(String module, StatusCategory category, String localCode) → void`
- **说明：** 校验状态码各组成部分的格式合法性。
- **参数：**
  - `module` — 模块码
  - `category` — 状态分类
  - `localCode` — 本地码
