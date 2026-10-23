# 包 `sitemap.service`

## `SitemapService`

**类型：** class（final）

加载 classpath meta-sitemap，不执行 HTTP 渲染、权限裁剪或会话字段注入。

### 构造方法

#### `SitemapService(SitemapConfigLoader configLoader)`

参数为空时抛 `NexusException`（`CONFIG_ERROR`）。

### 方法

#### `load() → SitemapConfig`

委托 loader 读取并解析文档；资源缺失或解析失败时抛 `NexusException`
（`CONFIG_ERROR`）。

菜单的角色/隐藏裁剪与 catalog 合并由 `navigation.service` 的
`MetaSitemapMenuFilter`、`MetaSitemapNavigationConverter` 和
`NavigationMenuComposer` 完成。
