# 包 `sitemap.loader`

## `SitemapConfigLoader`

**类型：** class（final）

从 classpath 读取 meta-sitemap。

### 构造方法

#### `SitemapConfigLoader(SitemapYamlConfig config, SitemapYamlParser parser, ClassLoader classLoader)`

`config` / `parser` 为空，或 `classLoader` 为 `null` 且线程上下文类加载器也为空时，
抛 `NexusException`（`CONFIG_ERROR`）。显式 `classLoader` 为空时优先回退线程上下文
类加载器。

### 方法

#### `load() → SitemapConfig`

读取 `config.resourcePath()` 指向的 UTF-8 资源并交给 parser。资源不存在、读取失败或
解析失败时抛 `NexusException`（`CONFIG_ERROR`）。
