# 包 `domain.condition`

## DatabaseFactorStatement

**类型：** class

将 {@link Factor} 渲染为 SQL 表达式字符串。

### 方法

#### `normalizeValue(Object value, FieldValueType valueType, Operator operator) → String`

- **说明：** 将值规范化为 SQL 安全字符串表示。 数值与布尔类型不加引号；日期/时间类型使用 ISO 格式化；字符串使用单引号并去除撇号。
- **参数：**
  - `value` — 待规范化的原始值
  - `valueType` — 期望的值类型
  - `operator` — 运算符（SQL 模式下未使用，保留以符合接口契约）
- **返回：** SQL 安全的字符串表示

#### `normalizeValue(Factor factor) → Object`


#### `statement(Factor factor) → String`

- **说明：** 将单个因子渲染为完整 SQL 表达式，如 {@code field = 'value'} or {@code field IN (v1, v2)}.

## EmbedCondition

**类型：** class

支持嵌套的条件——每个 {@link EmbedCondition} 可递归包含子条件。最终语句将嵌套组包裹在括号中。

### 方法

#### `create(Mode mode, Relation relation) → EmbedCondition`

- **说明：** 创建支持嵌套的可嵌入条件。

#### `factor(Factor factor) → EmbedCondition`


#### `embeds() → List<EmbedCondition>`


#### `addCondition(EmbedCondition condition) → EmbedCondition`

- **说明：** 添加嵌套子条件。重建时，嵌入条件被包裹 在括号中并由父关系连接。

#### `merge(SimpleCondition other) → void`


#### `rebuild(IFactorStatement fs) → StringBuilder`


#### `referFields() → Set<String>`

## Factor

**类型：** class

由字段编码、运算符、值及可选值类型组成的单个过滤条件。

### 方法

#### `of(String code, Operator operator, Object value) → Factor`

- **说明：** 根据值的 Java 类型推断值类型并创建因子。
- **参数：**
  - `code` — 字段标识符
  - `operator` — 比较运算符
  - `value` — 比较值
- **返回：** 自动检测值类型的新因子

#### `of(String code, Operator operator, Object value, FieldValueType valueType) → Factor`

- **说明：** 使用显式指定的值类型创建因子。
- **参数：**
  - `code` — 字段标识符
  - `operator` — 比较运算符
  - `value` — 比较值
  - `valueType` — 值类型
- **返回：** 新因子

#### `name() → String`

- **说明：** 返回显示名称。
- **返回：** 名称

#### `name(String name) → Factor`

- **说明：** 设置显示名称。
- **参数：**
  - `name` — 名称
- **返回：** 当前因子

#### `code() → String`

- **说明：** 返回字段编码。
- **返回：** 字段编码

#### `operator() → Operator`

- **说明：** 返回比较运算符。
- **返回：** 运算符

#### `value() → Object`

- **说明：** 返回原始比较值。
- **返回：** 比较值

#### `valueType() → FieldValueType`

- **说明：** 返回值类型。
- **返回：** 值类型

#### `value(Map<String, Object> input) → Object`

- **说明：** 根据运行时输入映射解析因子值。 <ul> <li>占位符字符串（{@code ${key}} 或 {@code %{key}}）从输入映射提取对应条目。</li> <li>{@link FieldValueType#OBJECT} 类型将原始值本身作为输入映射的键。</li> <li>其他值原样返回。</li> </ul>
- **参数：**
  - `input` — 运行时键值输入映射
- **返回：** 解析后的值；输入缺失时返回 null

#### `valueKey() → String`

- **说明：** 从占位符值字符串提取占位符键。 例如 {@code "${userId}"} 返回 {@code "userId"}。 若值不是占位符，返回字符串表示。
- **返回：** 占位符键或值的字符串表示

#### `checkNull() → boolean`

- **说明：** 检查此因子是否有任何关键属性为 null（operator、valueType 或 code）。
- **返回：** 存在 null 关键属性时返回 {@code true}

#### `checkNull(List<Factor> list) → boolean`

- **说明：** 检查列表中是否有任何因子的关键属性为 null。
- **参数：**
  - `list` — 因子列表
- **返回：** 存在 null 关键属性时返回 {@code true}

#### `toString() → String`

## IFactorStatement

**类型：** interface

将 {@link Factor} 渲染为模式特定表达式字符串（SQL、脚本或 Java）的策略接口。 实现负责值引用、类型转换与运算符特定格式化。

## Mode

**类型：** enum

条件语句的目标输出模式。 <ul> <li>{@link #DB} — SQL 兼容表达式</li> <li>{@link #SCRIPT} — 脚本/表达式语言</li> <li>{@link #JAVA} — Java 布尔表达式（与 SCRIPT 相同）</li> </ul>

### 枚举常量

| 常量 | 说明 |
|------|------|
| `DB` | 数据库 SQL 模式 |
| `SCRIPT` | 脚本表达式模式 |
| `JAVA` | Java 表达式模式 |

## Operator

**类型：** enum

过滤条件中使用的比较运算符。每个运算符在 SQL（{@code dbSymbol}）与脚本（{@code scriptSymbol}）上下文中携带独立符号。

## Relation

**类型：** enum

用于连接多个 {@link Factor} 条件的逻辑组合符。每种关系在 SQL 与脚本上下文中各有符号。

## ScriptFactorStatement

**类型：** class

将 {@link Factor} 渲染为脚本/表达式语言（如 MVEL、SpEL）字符串。数值不加引号；字符串使用单引号。 {@code IN}/{@code NOT_IN} 使用 {@code include(...)}/{@code notInclude(...)}；{@code LIKE} 使用 {@code regexMatch(...)}。

### 方法

#### `normalizeValue(Object value, FieldValueType valueType, Operator operator) → Object`

- **说明：** 将值规范化为脚本/表达式语言输出。 {@code M} 后缀表示某些表达式语言（如 MVEL）中的 BigDecimal 字面量；带此后缀的值原样传递。
- **参数：**
  - `value` — 待规范化的原始值
  - `valueType` — 期望的值类型，可为 null
  - `operator` — 运算符，用于识别范围运算符
- **返回：** 规范化后的值（字符串加引号，数值不加引号）

#### `normalizeValue(Factor factor) → Object`


#### `statement(Factor factor) → String`

- **说明：** 将单个因子渲染为脚本表达式。 <ul> <li>Comparison operators: {@code field == value}</li> <li>IN/NOT_IN: {@code include(seq.set(v1,v2), field)}</li> <li>LIKE: {@code regexMatch(pattern, field)}</li> <li>BETWEEN: {@code field >= start && field <= end}</li> </ul>

## SimpleCondition

**类型：** class

由单一 {@link Relation} 连接的 {@link Factor} 平面列表。条件在首次通过 {@link #statement()} 访问时渲染为语句字符串并缓存，直至因子变更。

### 方法

#### `create(Mode mode, Relation relation) → SimpleCondition`

- **说明：** 使用指定输出模式与逻辑关系创建条件。

#### `mode() → Mode`


#### `relation() → Relation`


#### `factors() → List<Factor>`


#### `factor(Factor factor) → SimpleCondition`

- **说明：** 添加因子并使缓存的语句失效。

#### `displayName() → String`


#### `displayName(String displayName) → SimpleCondition`


#### `statement() → String`

- **说明：** 返回渲染后的条件语句，必要时延迟生成。

#### `initialize() → void`

- **说明：** 根据当前模式初始化合适的 {@link IFactorStatement} 并渲染条件语句。

#### `ensureReady() → void`

- **说明：** 校验条件至少有一个因子且关系非 null。

#### `rebuild(IFactorStatement fs) → StringBuilder`

- **说明：** 使用关系符号连接因子语句。 单因子条件省略关系连接符。

#### `merge(SimpleCondition other) → void`

- **说明：** 将另一条件的因子合并到当前条件，并使 缓存的语句失效。

#### `referFields() → Set<String>`

- **说明：** 收集此条件因子引用的所有字段编码。

#### `toString() → String`
