# 包 `dictionary.domain.request`

## DictionaryItemCreateRequest

**类型：** record

在类型编码下创建字典项的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `itemValue` | `String` | 类型内唯一的稳定字典项值 |
| `itemName` | `String` | 显示名称 |
| `sortOrder` | `Integer` | 显示顺序 |

## DictionaryItemPageRequest

**类型：** record

由管理控制台查询参数绑定的分页字典项查询。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 字典项名称或值的模糊匹配 |
| `status` | `BasicStatus` | 可选 生命周期状态 |
| `pageNo` | `long` | 从 1 开始的页码 |
| `pageSize` | `long` | 分页大小 |

### 构造方法

#### `DictionaryItemPageRequest()`

## DictionaryItemStatusUpdateRequest

**类型：** record

启用或禁用字典项的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `status` | `BasicStatus` | 目标字典项状态 |

## DictionaryItemUpdateRequest

**类型：** record

更新可变字典项字段的请求；项值不可变。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `itemName` | `String` | 显示名称 |
| `sortOrder` | `Integer` | 显示顺序 |

## DictionaryTypeCreateRequest

**类型：** record

创建字典类型的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `typeCode` | `String` | 工作区与安全域内唯一的稳定类型编码 |
| `typeName` | `String` | 显示名称 |
| `securityRealm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `sortOrder` | `Integer` | 显示顺序 |

## DictionaryTypePageRequest

**类型：** record

由管理控制台查询参数绑定的分页字典类型查询。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 类型名称或编码的模糊匹配 |
| `status` | `BasicStatus` | 可选 生命周期状态 |
| `builtIn` | `Boolean` | 可选 built-in type filter |
| `pageNo` | `long` | 从 1 开始的页码 |
| `pageSize` | `long` | 分页大小 |

### 构造方法

#### `DictionaryTypePageRequest()`

## DictionaryTypeStatusUpdateRequest

**类型：** record

启用或禁用字典类型的请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `status` | `BasicStatus` | 目标类型状态 |

## DictionaryTypeUpdateRequest

**类型：** record

更新可变字典类型字段的请求；类型编码不可变。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `typeName` | `String` | 显示名称 |
| `sortOrder` | `Integer` | 显示顺序 |
