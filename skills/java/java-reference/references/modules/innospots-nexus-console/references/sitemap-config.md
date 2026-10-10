# 包 `sitemap.config`

## SitemapYamlConfig

**类型：** class（final）

Classpath sitemap YAML 资源定位配置。默认路径：`ui-pages/{domainKey}/sitemap.yaml`。

### 构造方法

#### `SitemapYamlConfig()`
- **说明：** 使用默认根目录 `ui-pages` 与文件名 `sitemap.yaml`

#### `SitemapYamlConfig(String resourceRoot, String fileName)`
- **说明：** 自定义根段与文件名；任一为空白时抛 `IllegalArgumentException`
- **参数：**
  - `resourceRoot` — classpath 根段（如 `ui-pages`）
  - `fileName` — sitemap 文件名

### 方法

#### `defaults() → SitemapYamlConfig`（static）
- **说明：** 默认配置实例

#### `resourcePath(String domainKey) → String`
- **说明：** 解析 classpath 资源路径（`{resourceRoot}/{domainKey}/{fileName}`，不含前导 `/`）
- **参数：**
  - `domainKey` — 领域键（`ui-pages` 首段目录名）
- **异常：** `IllegalArgumentException` — domainKey 空白时