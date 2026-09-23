# 包 `domain.data`

## DataBody

**类型：** class

自带计时的数据载荷。构造时自动记录开始时间，调用 {@link #end()} 时计算耗时毫秒数。携带可选 {@link DataSchema} 元数据与自由格式 meta 映射。

### 方法

#### `of(T data) → DataBody<T>`

- **说明：** 将数据包装为自动记录开始时间的新 DataBody。

#### `data() → T`


#### `data(T data) → DataBody<T>`


#### `schema() → DataSchema`


#### `schema(DataSchema schema) → DataBody<T>`


#### `message() → String`


#### `message(String message) → DataBody<T>`


#### `elapsedMillis() → long`


#### `end() → DataBody<T>`

- **说明：** 停止计时并记录自构造以来的耗时毫秒数。

#### `meta(String key, Object value) → DataBody<T>`

## DataOperation

**类型：** enum

对目标数据源执行的数据操作类型。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `QUERY` | 查询 |
| `CREATE` | 创建 |
| `UPDATE` | 更新 |
| `DELETE` | 删除 |
| `EXECUTE` | 执行 |

## DataPage

**类型：** record

不可变的分页数据容器。在构造时校验分页边界，并提供分页导航便捷方法。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `records` | `List<T>` | 分页记录列表 |
| `pageNo` | `long` | 页码（从 1 开始） |
| `pageSize` | `long` | 每页记录数 |
| `total` | `long` | 全部记录总数 |
| `pages` | `long` | 总页数（自动计算） |

### 构造方法

#### `DataPage()`

- **说明：** 紧凑构造器校验分页参数，并确保记录列表永不为 null。

### 方法

#### `of(List<T> records, long pageNo, long pageSize, long total) → DataPage<T>`

- **说明：** 创建自动计算总页数的分页结果。
- **参数：**
  - `records` — 分页记录（null 安全）
  - `pageNo` — 页码（从 1 开始）
  - `pageSize` — 每页记录数
  - `total` — 全部记录总数

#### `empty(long pageNo, long pageSize) → DataPage<T>`

- **说明：** 返回指定页码与大小的空分页结果。

#### `hasNext() → boolean`


#### `hasPrevious() → boolean`

## DataRequest

**类型：** class

对命名目标数据源执行 {@link DataOperation} 的请求。支持可选请求体、分页参数、自由格式查询映射及可扩展元数据映射。

### 方法

#### `create(String target, DataOperation operation) → DataRequest<T>`

- **说明：** 为给定目标与操作创建数据请求。
- **参数：**
  - `target` — 数据源或实体标识
  - `operation` — 要执行的操作类型

#### `target() → String`


#### `operation() → DataOperation`


#### `credentialKey() → String`

- **说明：** 用于对目标数据源进行身份认证的凭据键。

#### `credentialKey(String credentialKey) → DataRequest<T>`


#### `body() → T`


#### `body(T body) → DataRequest<T>`


#### `pageNo() → int`


#### `pageSize() → int`


#### `page(int pageNo, int pageSize) → DataRequest<T>`

- **说明：** 设置分页参数。两个值必须为正数。

#### `query(String key) → Object`


#### `query(String key, Object value) → DataRequest<T>`


#### `meta(String key, Object value) → DataRequest<T>`

## DataResponse

**类型：** class

数据操作的通用响应信封。通过 {@code success} 标志区分成功与失败，并携带可选 {@link DataSchema}、数据载荷与元数据。

### 方法

#### `ok(T data) → DataResponse<T>`

- **说明：** 创建携带给定数据载荷的成功响应。

#### `fail(String code, String message) → DataResponse<T>`

- **说明：** 创建带错误码与消息的失败响应。

#### `success() → boolean`


#### `code() → String`


#### `message() → String`


#### `data() → T`


#### `schema() → DataSchema`


#### `schema(DataSchema schema) → DataResponse<T>`


#### `meta(String key, Object value) → DataResponse<T>`


#### `meta(Map<String, Object> meta) → DataResponse<T>`

- **说明：** 将给定映射合并到现有 meta 中。

## DataSchema

**类型：** class

描述数据载荷结构：{@link com.innospots.nexus.base.domain.field.DomainField} 列表加自由格式配置项。用于在数据响应中传递字段元数据。

### 方法

#### `named(String code, String name) → DataSchema`

- **说明：** 使用程序化编码与显示名称创建模式。

#### `code() → String`


#### `name() → String`


#### `fields() → List<DomainField>`


#### `field(DomainField field) → DataSchema`

- **说明：** 向模式定义添加字段。

#### `field(String code) → Optional<DomainField>`

- **说明：** Looks up a field by its programmatic code.

#### `config(String key, Object value) → DataSchema`

- **说明：** 在此模式上设置配置属性。

#### `config(String key) → Object`

- **说明：** 按键获取配置属性。
