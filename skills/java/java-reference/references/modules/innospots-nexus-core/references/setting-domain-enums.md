# 包 `setting.domain.enums`

## SettingScope

**类型：** enum

系统设置作用范围（持久化为 `setting_scope`）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `GLOBAL` | 实例级全局设置（`scope_id` 为空串）；后续可扩展租户、工作区等范围并配合非空 `scope_id` |

## SettingValueType

**类型：** enum

设置值逻辑类型（持久化为 `nx_system_setting.value_type` 字符串码）。取值均存于
`setting_value` 文本列；类型仅约束格式校验与 UI/管理端展示语义。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `STRING` | 普通字符串 |
| `NUMBER` | 数值（十进制，存为文本） |
| `BOOLEAN` | `true` / `false`（不区分大小写） |
| `ENCRYPTED` | 加密或脱敏后的密文字符串（opaque，不做明文语义校验） |
| `JSON` | JSON 对象或数组文本 |