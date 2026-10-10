# 包 `sitemap.parser`

## SitemapYamlParser

**类型：** class（final）

基于 Jackson 的 sitemap YAML 解析器。使用 `YAMLFactory`（禁用文档起始标记）、
`FAIL_ON_UNKNOWN_PROPERTIES = false`（宽松未知字段）与日期非时间戳序列化。

### 构造方法

#### `SitemapYamlParser()`
- **说明：** 构建内置 `ObjectMapper`

### 方法

#### `parse(String content) → SitemapConfig`
- **说明：** 将 YAML 文本解析为配置文档
- **参数：**
  - `content` — YAML 内容
- **返回：** 配置根对象
- **异常：** `NexusException`（`CONFIG_ERROR`）— 内容为空或解析失败时