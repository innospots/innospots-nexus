# 包 `setting.status`

## SystemSettingStatusCode

**类型：** enum，实现 `StatusCode`

系统设置域状态码（模块 `SET`），HTTP 400。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `SETTING_VALUE_INVALID`（0001） | 取值不符合 `SettingValueType` 对应格式 |
| `SETTING_VALUE_TOO_LONG`（0002） | 超过 `SystemSettingEntity.SETTING_VALUE_MAX_LENGTH` |
| `SETTING_VALUE_TYPE_MISMATCH`（0003） | 库内 `value_type` 与 `SettingKey` 声明不一致 |

### 方法

#### `advice() → I18nObject`
- **说明：** 固定建议「Check setting value format / 请检查设置项取值格式」