# Meta Sitemap（`console.sitemap`）

从 classpath `console/meta-sitemap.yaml` 加载默认站点元数据与菜单，供
`NavigationMenuComposer` 与插件 catalog 导航合并。它**不是**前端工程的
`sitemap.yaml`，也不提供 JAX-RS 端点。

## 分层

```text
SitemapService
  └─ SitemapConfigLoader
       ├─ SitemapYamlConfig（默认 console/meta-sitemap.yaml）
       └─ SitemapYamlParser（Jackson；忽略未知字段）
            ↓
       SitemapConfig / SitemapMenuItem
            ↓
NavigationMenuComposer
  ├─ MetaSitemapMenuFilter（hidden / roles）
  ├─ MetaSitemapNavigationConverter
  └─ NavigationMenuAssembler（catalog 菜单）
```

## 与 catalog 的关系

| 关注点 | 归属 |
|--------|------|
| 默认菜单、标题、图标、角色限制 | `meta-sitemap.yaml` |
| 插件 MODULE/MENU 与权限目录 | `console@2` 贡献 → `nx_console_catalog_resource` |
| 页面访问授权 | catalog 叶子 MODULE/MENU 的 `entryPageKey` |

catalog 只同步 MODULE / MENU；页面资源由外部页面工程或宿主自行管理。

详细 API 见 [sitemap-config.md](sitemap-config.md)、
[sitemap-domain-config.md](sitemap-domain-config.md)、
[sitemap-domain-model.md](sitemap-domain-model.md)、
[sitemap-loader.md](sitemap-loader.md)、[sitemap-parser.md](sitemap-parser.md) 与
[sitemap-service.md](sitemap-service.md)。
