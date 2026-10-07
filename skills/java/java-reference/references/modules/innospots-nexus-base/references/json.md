# 包 `json`

## I18nModule

**类型：** class

激活 I18nObject 序列化/反序列化及 @I18n 字段契约的 Jackson Module。 将此模块注册到 ObjectMapper 后，Jsons 默认可读写 I18nObject 的 单语言字符串与多语言对象两种 JSON 形态。


## I18nObjectDeserializer

**类型：** class

I18nObject 的 Jackson 反序列化器。 支持 JSON 字符串（单语言默认值）与 locale → value 的 JSON 对象两种形态。

### 方法

#### `getInstance() → I18nObjectDeserializer`
- **说明：** 供 Jackson 或组合反序列化器复用的单例实例。
- **返回：** 反序列化器实例


## I18nObjectSerializer

**类型：** class

I18nObject 的 Jackson 序列化器。 默认按当前线程 I18nConverter 输出单个本地化字符串； 当 I18nConverter 为 true 时，按 locale → value 映射输出 JSON 对象。

### 方法

#### `getInstance() → I18nObjectSerializer`
- **说明：** 供 Jackson 或组合序列化器复用的单例实例。
- **返回：** 序列化器实例

#### `serialize(I18nObject value, JsonGenerator generator) → void`
- **说明：** 将 I18nObject 写入 JSON。
- **参数：**
  - `value` — 待序列化的对象；为 null 时写入 JSON null
  - `generator` — JSON 生成器


## Jsons

**类型：** class

基于 Jackson 的中央 JSON 工具门面。提供两个 ObjectMapper 实例： 默认映射器，以及注册了 MaskingModule 的 {@link #maskedMapper()}， 支持字段级值转换与脱敏。

### 方法

#### `maskedMapper() → ObjectMapper`
- **说明：** 返回注册了 MaskingModule 的 ObjectMapper， 标注了 ValueConverter 或 MaskValue 的字段在序列化时自动转换。
- **返回：** 支持 @ValueConverter 和 @MaskValue 注解的映射器

#### `toMaskedJson(Object value) → String`
- **说明：** 将对象序列化为 JSON，对标注了 @ValueConverter 或 @MaskValue 的字段应用转换与脱敏。
- **参数：**
  - `value` — 待序列化的对象
- **返回：** JSON 字符串

#### `toJson(Object value) → String`
- **说明：** 将对象序列化为 JSON。
- **参数：**
  - `value` — 待序列化的对象
- **返回：** JSON 字符串

#### `fromJson(String json, Class<T> type) → T`
- **说明：** 将 JSON 字符串反序列化为指定类型的对象。
- **参数：**
  - `json` — JSON 文本
  - `type` — 目标类型
  - `<T>` — 目标类型参数
- **返回：** 反序列化后的对象

#### `fromJsonList(String json, Class<T> elementType) → List<T>`
- **说明：** 将 JSON 字符串反序列化为类型化 List。
- **参数：**
  - `json` — JSON 文本
  - `elementType` — 元素类型
  - `<T>` — 元素类型参数
- **返回：** 反序列化后的列表

#### `fromJsonSet(String json, Class<T> elementType) → Set<T>`
- **说明：** 将 JSON 字符串反序列化为类型化 Set。
- **参数：**
  - `json` — JSON 文本
  - `elementType` — 元素类型
  - `<T>` — 元素类型参数
- **返回：** 反序列化后的集合

#### `toMap(String json) → Map<String, Object>`
- **说明：** 将 JSON 字符串反序列化为 Map。
- **参数：**
  - `json` — JSON 文本
- **返回：** 键值映射

#### `fromJson(String json, TypeReference<T> type) → T`
- **说明：** 将 JSON 字符串反序列化为由 TypeReference 描述的类型化值。
- **参数：**
  - `json` — JSON 文本
  - `type` — 目标类型引用
  - `<T>` — 目标类型
- **返回：** 反序列化后的值


## MaskStrategy

**类型：** enum

JSON 序列化时敏感数据的预定义脱敏策略。 每种策略定义字符串值的转换方式：保留固定数量的前导和/或尾部字符， 其余部分用星号遮蔽。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PASSWORD` | — |
| `HIDE` | — |

### 方法

#### `apply(String value) → String`
- **说明：** 138****1234 — 保留前 3 位、后 4 位 PHONE(s -> mask(s, 3, 4)), /** a***@example.com — 保留 @ 前第 1 位，域名完整保留 EMAIL(s -> { if (s == null || !s.contains("@")) { return "***"; } int at = s.indexOf('@'); String local = s.substring(0, at); String domain = s.substring(at); return (local.isEmpty() ? "" : local.charAt(0)) + "***" + domain; }), /** 320***********1234 — 保留前 3 位、后 4 位 ID_CARD(s -> mask(s, 3, 4)), /** 6222****1234 — 保留前 4 位、后 4 位 BANK_CARD(s -> mask(s, 4, 4)), /** 张* / 张三* — 保留首字（长度 > 2 时保留前两个），其余遮蔽 NAME(s -> { if (s == null || s.isEmpty()) { return "***"; } if (s.length() == 2) { return s.charAt(0) + "*"; } return s.charAt(0) + s.substring(1, 2) + "*".repeat(Math.max(1, s.length() - 2)); }), /** ****** — 完全遮蔽 PASSWORD(s -> "******"), /** 不保留任何字符，全部替换为 *** HIDE(s -> "***"), /** 自定义策略 — 使用 MaskValue 和 MaskValue CUSTOM(s -> s); private final Function masker; MaskStrategy(Function masker) { this.masker = masker; } /** 对给定值应用脱敏转换。
- **参数：**
  - `value` — 原始字符串值（可为 null）
- **返回：** 脱敏后的字符串；输入为 null 时返回 null


## MaskValue

**类型：** annotation

标记字段或访问器在 JSON 序列化时进行脱敏。 仅当 MaskingModule 注册到 Jackson ObjectMapper 时脱敏才生效。 未注册模块时，该注解被忽略，字段以原始值序列化。


## MaskedSerializer

**类型：** class

应用字段级脱敏的 Jackson 序列化器。 此序列化器由 MaskingModule 自动装配，不应直接引用。


## MaskingModule

**类型：** class

激活字段级值转换与脱敏的 Jackson Module。 将此模块注册到 ObjectMapper 以在序列化时自动转换标注了 @ValueConverter 的字段并脱敏标注了 @MaskValue 的字段。 未注册此模块时，这些注解被忽略。


## ValueConverter

**类型：** annotation

标记字段或访问器在 JSON 序列化时进行值转换。 转换器类必须提供公共无参构造函数。 它接收原始字段值并返回 Jackson 应序列化的值。 转换后的值可选择通过委托序列化器（如 MaskedSerializer）进行链式处理。


## ValueConvertingSerializer

**类型：** class

应用字段级值转换的 Jackson 序列化器。 可提供委托序列化器来序列化转换后的值，例如在转换后再应用脱敏。
