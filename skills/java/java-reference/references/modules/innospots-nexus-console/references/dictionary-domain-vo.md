# 包 `dictionary.domain.vo`

## DictionaryItemVo

**类型：** record

管理控制台字典项视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `dictionaryItemId` | `String` | item 标识符 |
| `typeCode` | `String` | 父类型编码 |
| `itemValue` | `String` | 稳定的字典项值 |
| `itemName` | `String` | 显示名称 |
| `securityRealm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `status` | `BasicStatus` | 生命周期状态 |
| `sortOrder` | `Integer` | 显示顺序 |
| `builtIn` | `Boolean` | 字典项是否由系统管理 |
| `createdAt` | `LocalDateTime` | 创建时间 |
| `updatedAt` | `LocalDateTime` | 最后更新时间 |


## DictionaryTypeOptionVo

**类型：** record

用于选择器的字典类型精简选项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `dictionaryTypeId` | `String` | type 标识符 |
| `typeCode` | `String` | 稳定的类型编码 |
| `typeName` | `String` | 显示名称 |


## DictionaryTypeVo

**类型：** record

管理控制台字典类型视图。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `dictionaryTypeId` | `String` | type 标识符 |
| `typeCode` | `String` | 稳定的类型编码 |
| `typeName` | `String` | 显示名称 |
| `securityRealm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `status` | `BasicStatus` | 生命周期状态 |
| `sortOrder` | `Integer` | 显示顺序 |
| `builtIn` | `Boolean` | 类型是否由系统管理 |
| `createdAt` | `LocalDateTime` | 创建时间 |
| `updatedAt` | `LocalDateTime` | 最后更新时间 |
