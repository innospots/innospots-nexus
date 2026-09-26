# 包 `domain.dictionary`

## DictionaryItem

**类型：** record

字典类型内的单个键值条目。显示名称与类型名称均支持国际化（I18nObject）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `value` | `String` | 字典值 |
| `name` | `I18nObject` | 国际化显示名称 |
| `type` | `String` | 字典类型编码 |
| `typeName` | `I18nObject` | 国际化类型名称 |
| `status` | `BasicStatus` | 状态 |


## DictionaryType

**类型：** record

字典类型（如 "gender"、"country"），用于分组相关的 DictionaryItem 条目。 显示名称支持国际化。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `code` | `String` | 字典类型编码 |
| `name` | `I18nObject` | 国际化显示名称 |
| `status` | `BasicStatus` | 状态 |
