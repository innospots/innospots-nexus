# 包 `domain.field`

## DomainField

**类型：** class

描述领域模式或数据结构中的字段。携带标识符、显示名称、程序化编码、值类型、作用域、可选注释以及可选项列表。

### 方法

#### `named(String name, String code, String valueType) → DomainField`
- **说明：** 使用给定显示名称、程序化编码与值类型名称创建字段。 / public static DomainField named(String name, String code, String valueType)

#### `option(SelectOption option) → DomainField`
- **说明：** 向此字段添加可选项（如用于下拉框）。 / public DomainField option(SelectOption option)


## FieldScope

**类型：** enum

字段在领域模式中的角色或归属边界。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `INPUT` | — |
| `OUTPUT` | — |
| `PARAMETER` | — |
| `METADATA` | — |


## FieldValueType

**类型：** enum

领域字段支持的值类型。每种类型映射到 Java Class，并提供 {@link #convert(Object)} 方法进行字符串到类型化值的强制转换。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `STRING` | — |
| `INTEGER` | — |
| `LONG` | — |
| `DOUBLE` | — |
| `DECIMAL` | — |
| `BOOLEAN` | — |
| `DATE` | — |
| `TIME` | — |
| `DATE_TIME` | — |
| `OBJECT` | — |

### 方法

#### `convert(Object value) → Object`
- **说明：** 将原始值（通常为字符串）转换为目标 Java 类型。 若值已是目标类型实例，则直接返回。
- **参数：**
  - `value` — 原始输入值
- **返回：** 转换后的类型化值，输入为 null 时返回 null


## ParamField

**类型：** class

具有特定 FieldValueType、必填标志与可选默认值的参数字段。其作用域自动设为 FieldScope。


## SelectOption

**类型：** record

具有存储值与显示标签的可选项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `value` | `String` | 存储值 |
| `label` | `String` | 显示标签 |
