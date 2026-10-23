# 包 `sitemap.parser`

## `SitemapYamlParser`

**类型：** class（final）

基于 Jackson YAML 的 meta-sitemap 解析器。禁用文档起始标记、
`FAIL_ON_UNKNOWN_PROPERTIES = false`，并关闭日期时间戳序列化。

### 构造方法

#### `SitemapYamlParser()`

构建内置 `ObjectMapper`。

### 方法

#### `parse(String content) → SitemapConfig`

内容为空或解析失败时抛 `NexusException`（`CONFIG_ERROR`）。
