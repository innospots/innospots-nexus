# 包 `sitemap.loader`

## SitemapConfigLoader

**类型：** class（final）

从 classpath `ui-pages/{domainKey}/sitemap.yaml` 加载 sitemap 配置。

### 构造方法

#### `SitemapConfigLoader(SitemapYamlConfig config, SitemapYamlParser parser, ClassLoader classLoader)`
- **说明：** `config`/`parser` 为 `null` 或 classLoader 解析为 `null` 时抛 `NexusException`
  （`CONFIG_ERROR`）；`classLoader` 为 `null` 时回退当前线程上下文 ClassLoader
- **参数：**
  - `config` — 资源定位配置
  - `parser` — YAML 解析器
  - `classLoader` — 可选类加载器

### 方法

#### `load(String domainKey) → SitemapConfig`
- **说明：** 加载指定领域的 sitemap 配置；`resourceType` 非 `sitemap`（显式声明时）抛 `CONFIG_ERROR`
- **参数：**
  - `domainKey` — `ui-pages` 首段目录名
- **返回：** 解析后的配置文档
- **异常：** `NexusException`（`CONFIG_ERROR`）— 资源未找到、读取或解析失败时