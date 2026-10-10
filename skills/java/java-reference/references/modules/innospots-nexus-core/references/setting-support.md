# 包 `setting.support`

## SettingKey&lt;T&gt;

**类型：** record

类型化设置项标识：业务模块在编译期声明域、键、范围、值类型与字符串解析器。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `domain` | `String` | 设置域，如 `platform`、`console` |
| `key` | `String` | 域内键，如 `registration.mode` |
| `scope` | `SettingScope` | 作用范围 |
| `valueType` | `SettingValueType` | 持久化 `value_type` 列 |
| `parser` | `Function<String, T>` | 将库内字符串解析为业务类型；失败时应抛异常以便映射为校验错误 |

### 构造方法

#### `SettingKey(String domain, String key, SettingScope scope, SettingValueType valueType, Function<String, T> parser)`
- **说明：** 标准记录构造器；配合 `SystemSettingService#getOrBootstrap` / `put` 使用

### 方法

#### `ofString(String domain, String key, SettingScope scope) → SettingKey<String>`（static）
- **说明：** 构造 `SettingValueType.STRING` 设置键（解析为原文字符串）
- **参数：**
  - `domain` — 设置域
  - `key` — 设置键
  - `scope` — 作用范围
- **返回：** 字符串设置键