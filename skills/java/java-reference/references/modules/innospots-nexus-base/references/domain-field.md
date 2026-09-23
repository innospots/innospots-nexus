# 包 `domain.field`

## DomainField

**类型：** class

描述领域模式或数据结构中的字段。携带标识符、显示名称、程序化编码、值类型、作用域、可选注释以及可选项列表。

### 方法

#### `named(String name, String code, String valueType) → DomainField`

- **说明：** 使用给定显示名称、程序化编码与值类型名称创建字段。

#### `fieldId() → String`


#### `fieldId(String fieldId) → DomainField`


#### `name() → String`


#### `code() → String`


#### `valueType() → String`


#### `scope() → FieldScope`


#### `scope(FieldScope scope) → DomainField`


#### `comment() → String`


#### `comment(String comment) → DomainField`


#### `options() → List<SelectOption>`


#### `option(SelectOption option) → DomainField`

- **说明：** 向此字段添加可选项（如用于下拉框）。

## FieldScope

**类型：** enum

字段在领域模式中的角色或归属边界。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `INPUT` | 输入字段 |
| `OUTPUT` | 输出字段 |
| `PARAMETER` | 参数字段 |
| `METADATA` | 元数据字段 |

## FieldValueType

**类型：** enum

领域字段支持的值类型。每种类型映射到 Java {@link Class}，并提供 {@link #convert(Object)} 方法进行字符串到类型化值的强制转换。

## ParamField

**类型：** class

具有特定 {@link FieldValueType}、必填标志与可选默认值的参数字段。其作用域自动设为 {@link FieldScope#PARAMETER}。

### 方法

#### `of(String code, FieldValueType valueType) → ParamField`


#### `required() → boolean`


#### `fieldValueType() → FieldValueType`


#### `required(boolean required) → ParamField`


#### `defaultValue() → Object`


#### `defaultValue(Object defaultValue) → ParamField`

## SelectOption

**类型：** record

具有存储值与显示标签的可选项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `value` | `String` | 存储值 |
| `label` | `String` | 显示标签 |
