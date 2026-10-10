# 包 `setting.service`

## SystemSettingService

**类型：** class

运行时读取与更新持久化系统设置（按设置域、范围与 `SettingKey` 管理）。首次读库无行时可
引导插入；写入时校验长度、值类型与基础格式。事务边界由调用方（如 platform service）声明。

### 构造方法

#### `SystemSettingService(SystemSettingOperator settingOperator)`
- **参数：**
  - `settingOperator` — `nx_system_setting` 表读写操作器

### 方法

#### `getStringOrBootstrap(String domain, SettingScope scope, String scopeId, String settingKey, String deploymentDefault) → String`
- **说明：** 读取字符串设置；无行时用部署默认值引导插入（类型固定为 `STRING`）
- **参数：**
  - `domain` — 设置域
  - `scope` — 作用范围
  - `scopeId` — 范围 ID；GLOBAL 时可 null
  - `settingKey` — 设置键
  - `deploymentDefault` — 库内无行时的默认值
- **返回：** 当前持久化字符串
- **异常：** `NexusException` — 超长或库内类型非 `STRING` 时

#### `<T> T getOrBootstrap(SettingKey<T> settingKey, String scopeId, T deploymentDefault)`
- **说明：** 读取类型化设置；无行时用 `deploymentDefault` 引导插入
- **参数：**
  - `settingKey` — 注册键（含值类型与解析器）
  - `scopeId` — 范围 ID
  - `deploymentDefault` — 缺省业务值
- **返回：** 解析后的当前值
- **异常：** `NexusException` — 格式、类型或解析失败时

#### `getUpdatedAtOrBootstrap(SettingKey<?> settingKey, String scopeId, String deploymentDefaultRaw) → LocalDateTime`
- **说明：** 返回设置项 `updated_at`（必要时引导插入默认行）
- **参数：**
  - `settingKey` — 注册键
  - `scopeId` — 范围 ID
  - `deploymentDefaultRaw` — 引导插入用的原始字符串（与 `SettingKey#valueType()` 格式一致）
- **返回：** 最后更新时间

#### `putString(String domain, SettingScope scope, String scopeId, String settingKey, String settingValue) → String`
- **说明：** 写入字符串设置（`STRING`）；值未变则跳过 UPDATE
- **参数：**
  - `domain` — 设置域
  - `scope` — 作用范围
  - `scopeId` — 范围 ID
  - `settingKey` — 设置键
  - `settingValue` — 新值
- **返回：** 持久化后的字符串

#### `putString(String domain, SettingScope scope, String scopeId, String settingKey, SettingValueType valueType, String settingValue) → String`
- **说明：** 写入字符串设置并显式声明值类型；值未变则跳过 UPDATE
- **参数：**
  - `valueType` — 值类型（须与已存在行一致）
- **返回：** 持久化后的字符串
- **异常：** `NexusException` — 格式错误、类型不匹配或超长时

#### `<T> T put(SettingKey<T> settingKey, String scopeId, T value)`
- **说明：** 写入类型化设置并返回解析结果；写路径在值变化时落库
- **参数：**
  - `settingKey` — 注册键
  - `scopeId` — 范围 ID
  - `value` — 新业务值
- **返回：** 写入后解析的值
- **异常：** `NexusException` — 格式、类型或解析失败时

### 行为备注

- 值格式为轻量校验（非 JSON Schema 级）：`BOOLEAN` 校验 true/false（不区分大小写）、
  `NUMBER` 用 `BigDecimal` 校验、`JSON` 仅校验 `{`/`[` 前缀；`STRING`/`ENCRYPTED` 仅做长度校验。
- 解析失败统一映射为 `SystemSettingStatusCode.SETTING_VALUE_INVALID`。