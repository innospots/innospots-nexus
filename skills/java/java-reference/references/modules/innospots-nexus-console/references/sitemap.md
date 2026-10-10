# Sitemap（`console.sitemap`）

动态页面 Sitemap：从 classpath `ui-pages/{domainKey}/sitemap.yaml` 加载配置并渲染为对外
sitemap 资源。当前按固定 YAML 全量返回（`GET /api/public/sitemap/nexus`）；后续按登录
用户与权限裁剪 pages / menus。OpenAPI Tag：`NexusSitemap`。

## 分层

```text
NexusSitemapEndpoint         JAX-RS：GET /api/public/sitemap/{domainKey}（实现 SitemapEndpoint）
        ↓
SitemapService               编排加载与映射（当前全量返回）
   ├─ SitemapConfigLoader    classpath 读取 + resourceType 校验（必须为 sitemap）
   │    └─ SitemapYamlParser Jackson YAML 解析（宽松：FAIL_ON_UNKNOWN_PROPERTIES=false）
   └─ SitemapMapper          配置 → 对外资源（暂不裁剪、不注入会话字段）
        ↓
SitemapConfig（会话无关超集） → SitemapResource（预留会话动态字段：user、permissions、updatedAt）
```

## 与 Page DSL 的关系

- sitemap YAML 与页面 YAML 同属 `ui-pages/{domainKey}/`；页面声明（`SitemapPageDescriptor`）
  的 `pageDsl` 字段为内联 **Pactor Page DSL** 文档（非懒加载时下发）。
- 页面/菜单路由与权限裁剪字段（`publicAccess`、`permission`、`lazy`）为会话渲染预留。

详细 API 见 [`sitemap-endpoint.md`](sitemap-endpoint.md)、[`sitemap-service.md`](sitemap-service.md)、
[`sitemap-loader.md`](sitemap-loader.md)、[`sitemap-parser.md`](sitemap-parser.md)、
[`sitemap-config.md`](sitemap-config.md)、[`sitemap-domain-config.md`](sitemap-domain-config.md)、
[`sitemap-domain-model.md`](sitemap-domain-model.md)、[`sitemap-domain-vo.md`](sitemap-domain-vo.md)。