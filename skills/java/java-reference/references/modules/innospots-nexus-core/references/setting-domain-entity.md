# 包 `setting.domain.entity`

## SystemSettingEntity

**类型：** class，继承 `BaseEntity`

持久化系统设置项（表 `nx_system_setting`）。由
`setting_domain + setting_scope + scope_id + setting_key` 唯一标识一条配置；
`value_type` 在插入后不变，仅 `setting_value` 可更新。

唯一约束：`uk_nx_system_setting_scope_key`；索引：`idx_nx_system_setting_domain`。

### 常量

| 常量 | 说明 |
|------|------|
| `TABLE_NAME = "nx_system_setting"` | 表名 |
| `SETTING_VALUE_MAX_LENGTH = 2048` | `settingValue` 最大字符数（VARCHAR 阶梯扩展档） |
| `GLOBAL_SCOPE_ID = ""` | 全局范围时 `scopeId` 使用空串，规避 MySQL 唯一索引 NULL 差异 |

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `settingId` | `String` | 主键（`IdType.ASSIGN_UUID`，长度 32） |
| `settingDomain` | `String` | 设置域，由上层模块约定（如 `platform`）；长度 64 |
| `settingScope` | `String` | `SettingScope#name()`；长度 32 |
| `scopeId` | `String` | 范围实例 ID；`GLOBAL` 时为 `GLOBAL_SCOPE_ID`；长度 32 |
| `settingKey` | `String` | 域内设置键；长度 128 |
| `valueType` | `String` | `SettingValueType#name()`（列 `value_type`）；长度 32 |
| `settingValue` | `String` | 序列化后的设置值（长度受 `SETTING_VALUE_MAX_LENGTH` 约束） |

### 方法

#### `idPrefix() → String`
- **说明：** 返回主键前缀 `"set"`（`DbPrimaryGenerator` ULID 使用）

#### `settingScopeEnum() → SettingScope`
- **说明：** 返回作用范围枚举

#### `valueTypeEnum() → SettingValueType`
- **说明：** 返回值类型枚举