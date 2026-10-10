# 包 `sitemap.endpoint`

## SitemapEndpoint

**类型：** interface

Sitemap 渲染端口（非 JAX-RS）；HTTP 由 `NexusSitemapEndpoint` 暴露。

### 方法

#### `render() → SitemapResource`
- **说明：** 渲染内置 nexus 领域 sitemap
- **返回：** sitemap 资源

## NexusSitemapEndpoint

**类型：** class（final），`@Path("/api/public/sitemap/nexus")`，`@Produces(application/json)`，`@Tag(name = "NexusSitemap")`

内置 `nexus` 领域 sitemap 端点：从 `ui-pages/nexus/sitemap.yaml` 加载并渲染。
当前返回 YAML 全量结构；后续按登录用户与权限裁剪 pages / menus。
公共开放 API（免 Bearer 鉴权，见 `ConsolePublicApiPaths`）。

### 构造方法

#### `NexusSitemapEndpoint(SitemapService sitemapService)`
- **参数：**
  - `sitemapService` — sitemap 渲染服务

### 端点

#### `GET renderHttp() → R<SitemapResource>`
- **说明：** 加载并渲染内置 nexus Sitemap（operationId `uiSitemapRenderNexus`；固定加载
  `ui-pages/nexus/sitemap.yaml`，当前返回配置全量）
- **返回：** `R.ok(...)` 包装的 sitemap 资源

#### `render() → SitemapResource`
- **说明：** 实现 `SitemapEndpoint`；委托 `SitemapService.render(BUILTIN_DOMAIN_KEY)`