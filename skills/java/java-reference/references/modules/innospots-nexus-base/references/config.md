# 包 `config`

## NexusConfig

**类型：** class

包装扁平键值映射的不可变配置存储。 键必须为非空白字符串；构造时跳过 null 值。 类型化访问方法（{@link #getBoolean}、{@link #getInt}）通过 Hutool 的 Convert 工具自动转换。

### 方法

#### `of(Map<String, ?> source) → NexusConfig`
- **说明：** 从源映射创建不可变配置。null 值被省略；空白键立即失败。
- **参数：**
  - `source` — 源键值对
- **返回：** 新的 NexusConfig 实例

#### `get(String key) → Optional<String>`
- **说明：** 返回指定键的原始值。
- **参数：**
  - `key` — 配置键
- **返回：** 包含值的 Optional，不存在时为空

#### `get(String key, String defaultValue) → String`
- **说明：** 返回指定键的值，不存在时回退到默认值。
- **参数：**
  - `key` — 配置键
  - `defaultValue` — 默认值
- **返回：** 配置值或默认值

#### `getBoolean(String key, boolean defaultValue) → boolean`
- **说明：** 返回指定键的布尔值，不存在时回退到默认值。 转换由 Convert 执行。
- **参数：**
  - `key` — 配置键
  - `defaultValue` — 默认值
- **返回：** 布尔值

#### `getInt(String key, int defaultValue) → int`
- **说明：** 返回指定键的整数值，不存在时回退到默认值。 转换由 Convert 执行。
- **参数：**
  - `key` — 配置键
  - `defaultValue` — 默认值
- **返回：** 整数值

#### `asMap() → Map<String, String>`
- **说明：** 返回底层配置映射的不可修改视图。
- **返回：** 配置映射
