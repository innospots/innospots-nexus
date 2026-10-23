# 包 `sitemap.config`

## `SitemapYamlConfig`

**类型：** class（final）

Classpath meta-sitemap 资源定位配置。

### 静态工厂

#### `metaSitemapDefaults() → SitemapYamlConfig`

默认资源路径 `console/meta-sitemap.yaml`。

#### `ofResourcePath(String resourcePath) → SitemapYamlConfig`

使用自定义 classpath 路径；空白值抛 `IllegalArgumentException`。

### 方法

#### `resourcePath() → String`

返回不含前导 `/` 的资源路径。
