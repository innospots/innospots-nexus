# 包 `util`

## BeanUtils

**类型：** class

Bean 属性拷贝与转换工具，封装 Hutool BeanUtil。 支持单对象与批量拷贝、Bean 与 Map 互转，以及可选的驼峰/下划线命名转换。

### 方法

#### `copyProperties(Object source, Object target) → void`
- **说明：** 将源对象属性拷贝到目标对象，忽略 null 值与拷贝错误。
- **参数：**
  - `source` — 源对象
  - `target` — 目标对象

#### `copyProperties(Object source, Class<T> targetClass) → T`
- **说明：** 将源对象拷贝为指定类型的新实例。
- **参数：**
  - `source` — 源对象
  - `targetClass` — 目标类型
  - `<T>` — 目标类型参数
- **返回：** 新实例，源为 null 时返回 null

#### `copyProperties(Collection<S> sourceCollection, Class<T> targetClass) → List<T>`
- **说明：** 批量将集合元素拷贝为指定类型列表。
- **参数：**
  - `sourceCollection` — 源集合
  - `targetClass` — 目标元素类型
  - `<S>` — 源元素类型
  - `<T>` — 目标元素类型
- **返回：** 拷贝后的列表，源为空时返回空列表

#### `toMap(Object source) → Map<String, Object>`
- **说明：** 将 Bean 转换为 Map，不转下划线且忽略 null。
- **参数：**
  - `source` — Bean 对象
- **返回：** 属性映射

#### `toMap(Object source, boolean underscore, boolean ignoreNull) → Map<String, Object>`
- **说明：** 将 Bean 转换为 Map，可配置下划线命名与 null 忽略策略。
- **参数：**
  - `source` — Bean 对象
  - `underscore` — 是否将键转为下划线命名
  - `ignoreNull` — 是否忽略 null 属性
- **返回：** 属性映射

#### `toBean(Map<String, Object> source, Class<T> targetClass) → T`
- **说明：** 将 Map 转换为 Bean，默认不自动转驼峰。
- **参数：**
  - `source` — 属性映射
  - `targetClass` — 目标类型
  - `<T>` — 目标类型参数
- **返回：** Bean 实例

#### `toBean(Map<String, Object> source, Class<T> targetClass, boolean underscore) → T`
- **说明：** 将 Map 转换为 Bean，可配置下划线键自动转驼峰。
- **参数：**
  - `source` — 属性映射
  - `targetClass` — 目标类型
  - `underscore` — 是否自动将下划线键转为驼峰属性
  - `<T>` — 目标类型参数
- **返回：** Bean 实例

#### `toBean(Collection<Map<String, Object>> sourceCollection, Class<T> targetClass) → List<T>`
- **说明：** 批量将 Map 集合转换为 Bean 列表。
- **参数：**
  - `sourceCollection` — Map 集合
  - `targetClass` — 目标类型
  - `<T>` — 目标类型参数
- **返回：** Bean 列表，源为空时返回空列表


## Checks

**类型：** class

前置条件校验工具，校验失败时抛出携带 NexusStatusCode 的 NexusException。

### 方法

#### `notNull(T value, String name) → T`
- **说明：** 要求值非 null，否则抛出异常。
- **参数：**
  - `value` — 待校验值
  - `name` — 参数名，用于错误消息
  - `<T>` — 值类型
- **返回：** 原值

#### `notBlank(String value, String name) → String`
- **说明：** 要求字符串非空白，否则抛出异常。
- **参数：**
  - `value` — 待校验文本
  - `name` — 参数名，用于错误消息
- **返回：** 原值

#### `isTrue(boolean expression, String message) → void`
- **说明：** 要求表达式为 true，否则抛出异常。
- **参数：**
  - `expression` — 必须为 true 的条件
  - `message` — 条件为 false 时的错误消息

#### `positive(long value, String name) → long`
- **说明：** 要求数值严格为正，否则抛出异常。
- **参数：**
  - `value` — 待校验数值
  - `name` — 参数名，用于错误消息
- **返回：** 原值

#### `notEmpty(T value, String name) → <T extends Collection<?>> T`
- **说明：** 要求集合非 null 且非空，否则抛出异常。
- **参数：**
  - `value` — 待校验集合
  - `name` — 参数名，用于错误消息
  - `<T>` — 集合类型
- **返回：** 原集合


## CryptoUtils

**类型：** class

密码学工具类：密码哈希（BCrypt）、对称加密（AES-GCM）与不对称加密（RSA/OAEP）。 RSA 操作支持大块数据的分块加密模式。

### 方法

#### `sha256Hex(String value) → String`
- **说明：** 计算给定字符串的 SHA-256 十六进制摘要。
- **参数：**
  - `value` — 待摘要的字符串
- **返回：** SHA-256 十六进制字符串

#### `encryptPassword(String rawPassword) → String`
- **说明：** 使用 BCrypt 加密原始密码。
- **参数：**
  - `rawPassword` — 明文密码（不得为 null）
- **返回：** BCrypt 哈希字符串

#### `generatePasswordSalt() → String`
- **说明：** 生成用于密码哈希的 BCrypt 盐值。
- **返回：** BCrypt 盐值字符串

#### `encryptPassword(String rawPassword, String salt) → String`
- **说明：** 使用外部提供的盐值，以 BCrypt 加密原始密码。
- **参数：**
  - `rawPassword` — 明文密码（不得为 null）
  - `salt` — 外部提供的 BCrypt 盐值（不得为空白）
- **返回：** BCrypt 哈希字符串

#### `matchesPassword(String rawPassword, String encryptedPassword) → boolean`
- **说明：** 校验原始密码是否与 BCrypt 哈希匹配。
- **参数：**
  - `rawPassword` — 明文密码
  - `encryptedPassword` — BCrypt 哈希字符串
- **返回：** 匹配时返回 true

#### `encryptAesGcm(String plaintext, String secret) → String`
- **说明：** 使用 AES-GCM 及随机 12 字节 IV 加密明文。 IV 前置拼接在密文之前，结果经 Base64 编码。
- **参数：**
  - `plaintext` — 待加密的明文
  - `secret` — AES 密钥字节
- **返回：** Base64 编码的 IV + 密文

#### `decryptAesGcm(String encrypted, String secret) → String`
- **说明：** 解密由 {@link #encryptAesGcm} 产生的 AES-GCM 密文。 期望前 12 字节为 IV。
- **参数：**
  - `encrypted` — Base64 编码的 IV + 密文
  - `secret` — AES 密钥字节
- **返回：** 解密后的明文

#### `generateRsaKeyPair() → AsymmetricKeyPair`
- **说明：** 生成默认 2048 位密钥长度的 RSA 密钥对。
- **返回：** Base64 编码的公钥/私钥对

#### `generateRsaKeyPair(int keySize) → AsymmetricKeyPair`
- **说明：** 生成指定密钥长度的 RSA 密钥对。
- **参数：**
  - `keySize` — 密钥长度（位），至少为 2048
- **返回：** Base64 编码的公钥/私钥对

#### `encryptRsa(String plaintext, String publicKey) → String`
- **说明：** 使用 RSA-OAEP（SHA-256）加密明文。 对超过密钥模数长度的数据支持分块加密模式。
- **参数：**
  - `plaintext` — 待加密的明文
  - `publicKey` — Base64 编码的 X.509 公钥
- **返回：** Base64 编码的密文


## DateTimeUtils

**类型：** class

日期时间格式化与解析工具类。支持多种预定义日期/时间模式，解析时按顺序尝试模式匹配。

### 方法

#### `format(LocalDateTime dateTime) → String`
- **说明：** 使用默认模式 yyyy-MM-dd HH:mm:ss 格式化 LocalDateTime。
- **参数：**
  - `dateTime` — 待格式化的日期时间
- **返回：** 格式化后的字符串

#### `format(LocalDateTime dateTime, String pattern) → String`
- **说明：** 使用指定模式格式化 LocalDateTime。
- **参数：**
  - `dateTime` — 待格式化的日期时间
  - `pattern` — 日期时间模式
- **返回：** 格式化后的字符串；输入为 null 时返回 null

#### `format(LocalDate date) → String`
- **说明：** 使用默认模式 yyyy-MM-dd 格式化 LocalDate。
- **参数：**
  - `date` — 待格式化的日期
- **返回：** 格式化后的字符串

#### `format(LocalDate date, String pattern) → String`
- **说明：** 使用指定模式格式化 LocalDate。
- **参数：**
  - `date` — 待格式化的日期
  - `pattern` — 日期模式
- **返回：** 格式化后的字符串；输入为 null 时返回 null

#### `formatDate(Date date, String pattern) → String`
- **说明：** 使用指定模式格式化遗留 Date（基于非线程安全的 SimpleDateFormat，仅适用于一次性调用）。
- **参数：**
  - `date` — 待格式化的日期
  - `pattern` — 日期时间模式
- **返回：** 格式化后的字符串；输入为 null 时返回 null

#### `parseLocalDateTime(String value) → LocalDateTime`
- **说明：** 解析日期时间字符串：优先尝试 ISO-8601 瞬时格式，再依次遍历所有支持的日期时间模式， 最后回退为仅日期解析（补零至午夜）。
- **参数：**
  - `value` — 待解析的日期时间字符串
- **返回：** 解析后的 LocalDateTime；无法解析时返回 null

#### `parseLocalDateTime(String value, String pattern) → LocalDateTime`
- **说明：** 使用指定模式解析日期时间字符串。
- **参数：**
  - `value` — 待解析的字符串
  - `pattern` — 日期时间模式
- **返回：** 解析后的 LocalDateTime；解析失败时返回 null

#### `parseLocalDate(String value) → LocalDate`
- **说明：** 使用支持的日期模式解析日期字符串。
- **参数：**
  - `value` — 待解析的日期字符串
- **返回：** 解析后的 LocalDate；解析失败时返回 null

#### `parseDate(String value, String pattern) → Date`
- **说明：** 使用指定模式及遗留 SimpleDateFormat 解析日期字符串。
- **参数：**
  - `value` — 待解析的字符串
  - `pattern` — 日期模式
- **返回：** 解析后的 Date；解析失败时返回 null

#### `toDate(LocalDateTime dateTime) → Date`
- **说明：** 将 LocalDateTime 转换为遗留 Date（系统默认时区）。
- **参数：**
  - `dateTime` — 待转换的日期时间
- **返回：** 转换后的 Date；输入为 null 时返回 null

#### `asDate(LocalDateTime dateTime) → Date`
- **说明：** 将 LocalDateTime 转换为 Date，输入为 null 时默认使用当前时间。
- **参数：**
  - `dateTime` — 待转换的日期时间
- **返回：** 转换后的 Date

#### `toLocalDateTime(Date date) → LocalDateTime`
- **说明：** 将遗留 Date 转换为 LocalDateTime（系统默认时区）。
- **参数：**
  - `date` — 待转换的日期
- **返回：** 转换后的 LocalDateTime；输入为 null 时返回 null

#### `toLocalDateTime(Long epochMillis) → LocalDateTime`
- **说明：** 将纪元毫秒数转换为 LocalDateTime。
- **参数：**
  - `epochMillis` — 纪元毫秒时间戳
- **返回：** 转换后的 LocalDateTime；输入为 null 时返回 null

#### `toDateTime(Long epochMillis) → LocalDateTime`
- **说明：** {@link #toLocalDateTime(Long)} 的别名。
- **参数：**
  - `epochMillis` — 纪元毫秒时间戳
- **返回：** 转换后的 LocalDateTime

#### `normalizeDateTime(Object value) → LocalDateTime`
- **说明：** 将多种日期/时间表示规范化为 LocalDateTime。 支持：LocalDateTime、LocalDate（补零至午夜）、Date、 Long（纪元毫秒）、String。
- **参数：**
  - `value` — 待规范化的值
- **返回：** 规范化后的 LocalDateTime；不支持的类型返回 null

#### `isToday(Date date) → boolean`
- **说明：** 判断遗留 Date 是否落在今天。
- **参数：**
  - `date` — 待判断的日期
- **返回：** 为今天时返回 true

#### `getDiffDay(Date day1, Date day2) → int`
- **说明：** 计算两个 Date 之间相差天数的绝对值。
- **参数：**
  - `day1` — 第一个日期
  - `day2` — 第二个日期
- **返回：** 相差天数；任一输入为 null 时返回 0

#### `consume(long startTime) → String`
- **说明：** 将自 startTime 至当前的耗时格式化为可读字符串。
- **参数：**
  - `startTime` — 起始时间戳（毫秒）
- **返回：** 可读的耗时描述

#### `consume(long endTime, long startTime) → String`
- **说明：** 将两个时间戳之间的耗时格式化为可读字符串，如 "1 hours, 2 minutes, 3 seconds, 456 ms."。
- **参数：**
  - `endTime` — 结束时间戳（毫秒）
  - `startTime` — 起始时间戳（毫秒）
- **返回：** 可读的耗时描述

#### `consume(LocalDateTime endTime, LocalDateTime startTime) → String`
- **说明：** 将两个 LocalDateTime 之间的耗时格式化为可读字符串。
- **参数：**
  - `endTime` — 结束时间
  - `startTime` — 起始时间
- **返回：** 可读的耗时描述；任一输入为 null 时返回 null

#### `prettyDuration(LocalDateTime endTime, LocalDateTime startTime) → String`
- **说明：** {@link #consume(LocalDateTime, LocalDateTime)} 的别名。
- **参数：**
  - `endTime` — 结束时间
  - `startTime` — 起始时间
- **返回：** 可读的耗时描述


## EnvUtils

**类型：** class

环境属性解析器，支持程序化覆盖。查找顺序为：覆盖值 → 系统属性 → 环境变量。

### 方法

#### `value(String key) → String`
- **说明：** 按键查找环境值，未找到时返回 null。
- **参数：**
  - `key` — 属性键
- **返回：** 解析到的值，或 null

#### `value(String key, String defaultValue) → String`
- **说明：** 按键查找环境值，未找到时返回默认值。
- **参数：**
  - `key` — 属性键
  - `defaultValue` — 未找到时的回退值
- **返回：** 解析到的值或默认值

#### `set(String key, String value) → void`
- **说明：** 设置程序化覆盖值；传入 null 时清除该键。
- **参数：**
  - `key` — 属性键
  - `value` — 覆盖值

#### `putAll(Map<String, ?> values) → void`
- **说明：** 批量写入程序化覆盖值，跳过 null 条目。
- **参数：**
  - `values` — 键值映射

#### `clear(String key) → void`
- **说明：** 清除指定键的程序化覆盖值。
- **参数：**
  - `key` — 属性键


## IdGenerator

**类型：** class

ID 生成工具，提供 Snowflake 分布式 ID、可配置字符集的随机 ID、 时间戳前缀 ID 以及批量生成能力。

### 方法

#### `of(long datacenterId, long workerId) → IdGenerator`
- **说明：** 创建指定数据中心与 worker ID 的生成器。 两个值均对 32 取模以兼容 Snowflake 节点范围。
- **参数：**
  - `datacenterId` — 数据中心 ID
  - `workerId` — worker ID
- **返回：** 生成器实例

#### `from(String address, int port) → IdGenerator`
- **说明：** 根据 IP 地址与端口派生节点 ID 并创建生成器。
- **参数：**
  - `address` — IP 地址
  - `port` — 端口号
- **返回：** 生成器实例

#### `configureGlobal(long datacenterId, long workerId) → void`
- **说明：** 重新配置全局单例生成器。
- **参数：**
  - `datacenterId` — 数据中心 ID
  - `workerId` — worker ID

#### `configureGlobal(String address, int port) → void`
- **说明：** 根据 IP 地址与端口重新配置全局单例生成器。
- **参数：**
  - `address` — IP 地址
  - `port` — 端口号

#### `next() → long`
- **说明：** 从全局生成器获取下一个 Snowflake ID。
- **返回：** Snowflake ID

#### `nextString() → String`
- **说明：** 从全局生成器获取下一个 Snowflake ID 的字符串形式。
- **返回：** Snowflake ID 字符串

#### `nextId() → synchronized long`
- **说明：** 生成下一个 Snowflake ID（线程安全）。
- **返回：** Snowflake ID

#### `nextIdString() → String`
- **说明：** 生成下一个 Snowflake ID 的字符串形式。
- **返回：** Snowflake ID 字符串

#### `random(String prefix) → String`
- **说明：** 生成 8 位随机字母数字 ID，可带前缀。
- **参数：**
  - `prefix` — 可选前缀
- **返回：** 随机 ID

#### `ulid(String prefix) → String`
- **说明：** 生成 ULID 字符串，可带前缀。
- **参数：**
  - `prefix` — 可选前缀
- **返回：** ULID 字符串

#### `monotonicUlid(String prefix) → String`
- **说明：** 生成单调递增 ULID 字符串，可带前缀。
- **参数：**
  - `prefix` — 可选前缀
- **返回：** 单调 ULID 字符串

#### `random(String prefix, Type type, int length) → String`
- **说明：** 按指定字符集类型与长度生成随机 ID。
- **参数：**
  - `prefix` — 可选前缀
  - `type` — 字符集类型
  - `length` — 随机部分长度
- **返回：** 随机 ID

#### `timestamp(String prefix, Type type, int randomLength, boolean includeMillis) → String`
- **说明：** 生成时间戳前缀 ID 并附加随机后缀。 格式：[prefix]yyyyMMddHHmmss[SSS][random]。
- **参数：**
  - `prefix` — 可选前缀
  - `type` — 随机部分字符集类型
  - `randomLength` — 随机部分长度
  - `includeMillis` — 是否包含毫秒
- **返回：** 时间戳 ID

#### `unique(String prefix, Type type) → String`
- **说明：** 使用当前毫秒时间戳加 6 位随机字符生成唯一 ID。
- **参数：**
  - `prefix` — 可选前缀
  - `type` — 随机部分字符集类型
- **返回：** 唯一 ID

#### `batch(String prefix, Type type, int length, int count) → List<String>`
- **说明：** 批量生成随机 ID。
- **参数：**
  - `prefix` — 可选前缀
  - `type` — 字符集类型
  - `length` — 每个 ID 的随机部分长度
  - `count` — 生成数量
- **返回：** ID 列表


## MetricsSnapshot

**类型：** record

指标计数器/计时器的时点快照，记录指标名称、标签、总次数及累计耗时（纳秒）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | — |
| `tags` | `Map<String, String>` | — |
| `count` | `long` | — |
| `totalNanos` | `long` | — |

### 方法

#### `totalMillis() → double`
- **说明：** 返回累计耗时的毫秒表示。
- **返回：** 总耗时（毫秒）


## MetricsUtils

**类型：** class

基于 Micrometer 的指标门面，提供计数器、计时器与仪表盘， 并自动规范化指标名称（小写、下划线分隔）。支持成功/失败/重试计数与耗时记录。

### 方法

#### `initialize(MeterRegistry registry) → void`
- **说明：** 初始化全局指标注册表。
- **参数：**
  - `registry` — Micrometer 注册表

#### `registry() → MeterRegistry`
- **说明：** 返回当前全局指标注册表。
- **返回：** 指标注册表

#### `increment(String name) → void`
- **说明：** 将指定指标计数加一。
- **参数：**
  - `name` — 指标名称

#### `increment(String name, Map<String, String> tags) → void`
- **说明：** 将带标签的指定指标计数加一。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射

#### `increment(String name, Map<String, String> tags, long amount) → void`
- **说明：** 将带标签的指定指标计数增加指定量。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `amount` — 增量（负值按零处理）

#### `record(String name, Callable<T> task) → T`
- **说明：** 执行可调用任务并记录耗时。
- **参数：**
  - `name` — 指标名称
  - `task` — 待执行任务
  - `<T>` — 返回值类型
- **返回：** 任务返回值

#### `record(String name, Map<String, String> tags, Callable<T> task) → T`
- **说明：** 执行带标签的可调用任务并记录耗时。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `task` — 待执行任务
  - `<T>` — 返回值类型
- **返回：** 任务返回值

#### `record(String name, Runnable task) → void`
- **说明：** 执行 Runnable 任务并记录耗时。
- **参数：**
  - `name` — 指标名称
  - `task` — 待执行任务

#### `record(String name, Map<String, String> tags, Runnable task) → void`
- **说明：** 执行带标签的 Runnable 任务并记录耗时。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `task` — 待执行任务

#### `recordWithStatus(String name, Callable<T> task) → T`
- **说明：** 执行任务并同时记录耗时与成功/失败状态。
- **参数：**
  - `name` — 指标名称
  - `task` — 待执行任务
  - `<T>` — 返回值类型
- **返回：** 任务返回值

#### `recordWithStatus(String name, Map<String, String> tags, Callable<T> task) → T`
- **说明：** 执行带标签的任务并同时记录耗时与成功/失败状态。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `task` — 待执行任务
  - `<T>` — 返回值类型
- **返回：** 任务返回值

#### `recordWithStatus(String name, Runnable task) → void`
- **说明：** 执行 Runnable 任务并同时记录耗时与成功/失败状态。
- **参数：**
  - `name` — 指标名称
  - `task` — 待执行任务

#### `recordWithStatus(String name, Map<String, String> tags, Runnable task) → void`
- **说明：** 执行带标签的 Runnable 任务并同时记录耗时与成功/失败状态。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `task` — 待执行任务

#### `recordDuration(String name, Map<String, String> tags, long nanos) → void`
- **说明：** 直接记录纳秒级耗时。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `nanos` — 耗时（纳秒）

#### `recordDuration(String name, Map<String, String> tags, Duration duration) → void`
- **说明：** 直接记录 Duration 耗时。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `duration` — 耗时，null 时按零处理

#### `countSuccess(String name, Map<String, String> tags) → void`
- **说明：** 记录成功次数。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射

#### `countFailure(String name, Map<String, String> tags) → void`
- **说明：** 记录失败次数。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射

#### `countRetry(String name, Map<String, String> tags) → void`
- **说明：** 记录重试次数。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射

#### `gauge(String name, Map<String, String> tags, Supplier<Number> supplier) → void`
- **说明：** 注册仪表盘指标。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
  - `supplier` — 数值供应器

#### `snapshot(String name) → MetricsSnapshot`
- **说明：** 获取指定指标的时点快照。
- **参数：**
  - `name` — 指标名称
- **返回：** 指标快照

#### `snapshot(String name, Map<String, String> tags) → MetricsSnapshot`
- **说明：** 获取带标签指标的时点快照。
- **参数：**
  - `name` — 指标名称
  - `tags` — 标签映射
- **返回：** 指标快照

#### `clear() → void`
- **说明：** 清空注册表中的所有指标。 / public static void clear()

#### `normalizeName(String name) → String`
- **说明：** 将指标名称规范化为小写下划线格式。
- **参数：**
  - `name` — 原始名称
- **返回：** 规范化后的名称


## StringUtils

**类型：** class

字符串工具类，提供空白判断、占位符替换（${key} 与 {{key}}）、 驼峰/下划线命名转换以及随机键生成等能力。
