# 包 `domain.data`

## DataBody

**类型：** class

自带计时的数据载荷。构造时自动记录开始时间，调用 {@link #end()} 时计算耗时毫秒数。携带可选 DataSchema 元数据与自由格式 meta 映射。

### 方法

#### `of(T data) → DataBody<T>`
- **说明：** 将数据包装为自动记录开始时间的新 DataBody。 / public static DataBody of(T data)

#### `end() → DataBody<T>`
- **说明：** 停止计时并记录自构造以来的耗时毫秒数。 / public DataBody end()


## DataOperation

**类型：** enum

对目标数据源执行的数据操作类型。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `QUERY` | — |
| `CREATE` | — |
| `UPDATE` | — |
| `DELETE` | — |
| `EXECUTE` | — |


## DataPage

**类型：** record

不可变的分页数据容器。在构造时校验分页边界，并提供分页导航便捷方法。

### 方法

#### `of(List<T> records, long pageNo, long pageSize, long total) → DataPage<T>`
- **说明：** 紧凑构造器校验分页参数，并确保记录列表永不为 null。 / public DataPage { if (pageNo < 1) { throw new IllegalArgumentException("pageNo must be greater than 0"); } if (pageSize < 1) { throw new IllegalArgumentException("pageSize must be greater than 0"); } if (total < 0) { throw new IllegalArgumentException("total must not be negative"); } records = records == null ? List.of() : List.copyOf(records); } /** 创建自动计算总页数的分页结果。
- **参数：**
  - `records` — 分页记录（null 安全）
  - `pageNo` — 页码（从 1 开始）
  - `pageSize` — 每页记录数
  - `total` — 全部记录总数

#### `empty(long pageNo, long pageSize) → DataPage<T>`
- **说明：** 返回指定页码与大小的空分页结果。 / public static DataPage empty(long pageNo, long pageSize)


## DataRequest

**类型：** class

对命名目标数据源执行 DataOperation 的请求。支持可选请求体、分页参数、自由格式查询映射及可扩展元数据映射。

### 方法

#### `create(String target, DataOperation operation) → DataRequest<T>`
- **说明：** 为给定目标与操作创建数据请求。
- **参数：**
  - `target` — 数据源或实体标识
  - `operation` — 要执行的操作类型

#### `credentialKey() → String`
- **说明：** 用于对目标数据源进行身份认证的凭据键。 / public String credentialKey()

#### `page(int pageNo, int pageSize) → DataRequest<T>`
- **说明：** 设置分页参数。两个值必须为正数。 / public DataRequest page(int pageNo, int pageSize)


## DataResponse

**类型：** class

数据操作的通用响应信封。通过 success 标志区分成功与失败，并携带可选 DataSchema、数据载荷与元数据。

### 方法

#### `ok(T data) → DataResponse<T>`
- **说明：** 创建携带给定数据载荷的成功响应。 / public static DataResponse ok(T data)

#### `fail(String code, String message) → DataResponse<T>`
- **说明：** 创建带错误码与消息的失败响应。 / public static DataResponse fail(String code, String message)

#### `meta(Map<String, Object> meta) → DataResponse<T>`
- **说明：** 将给定映射合并到现有 meta 中。 / public DataResponse meta(Map meta)


## DataSchema

**类型：** class

描述数据载荷结构：com.innospots.nexus.base.domain.field.DomainField 列表加自由格式配置项。用于在数据响应中传递字段元数据。

### 方法

#### `named(String code, String name) → DataSchema`
- **说明：** 使用程序化编码与显示名称创建模式。 / public static DataSchema named(String code, String name)

#### `field(DomainField field) → DataSchema`
- **说明：** 向模式定义添加字段。 / public DataSchema field(DomainField field)

#### `field(String code) → Optional<DomainField>`
- **说明：** Looks up a field by its programmatic code. / public Optional field(String code)

#### `config(String key, Object value) → DataSchema`
- **说明：** 在此模式上设置配置属性。 / public DataSchema config(String key, Object value)

#### `config(String key) → Object`
- **说明：** 按键获取配置属性。 / public Object config(String key)
