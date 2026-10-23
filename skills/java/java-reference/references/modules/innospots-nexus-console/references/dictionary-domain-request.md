# 包 `dictionary.domain.request`

## DictionaryItemCreateRequest

**类型：** record

在字典类型下创建或全量替换字典项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `itemValue` | `String` | 类型内唯一的稳定字典项值 |
| `itemName` | `String` | 显示名称 |
| `sortOrder` | `Integer` | 显示顺序 |

## DictionaryTypeCreateRequest

**类型：** record

创建字典类型。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `typeCode` | `String` | 归属范围内唯一的稳定类型编码 |
| `typeName` | `I18nObject` | 多语言名称，须含 `zh-CN` |
| `sortOrder` | `Integer` | 显示顺序 |
| `items` | `List<DictionaryItemCreateRequest>` | 可选的初始字典项 |

## DictionaryTypePageRequest

**类型：** class（final），`@BeanParam`

分页字典类型查询。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 类型名称或编码的模糊匹配 |
| `status` | `BasicStatus` | 可选状态 |
| `builtIn` | `Boolean` | 可选内置类型过滤 |
| `pageNo` | `long` | 页码，默认 1 |
| `pageSize` | `long` | 分页大小，默认 20 |

## DictionaryTypeStatusUpdateRequest

**类型：** record

启用或禁用字典类型。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `status` | `BasicStatus` | 目标类型状态 |

## DictionaryTypeUpdateRequest

**类型：** record

更新可变字典类型字段；类型编码不可变。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `typeName` | `I18nObject` | 多语言名称补丁 |
| `sortOrder` | `Integer` | 显示顺序 |
| `items` | `List<DictionaryItemCreateRequest>` | 非 null 时全量替换字典项 |
