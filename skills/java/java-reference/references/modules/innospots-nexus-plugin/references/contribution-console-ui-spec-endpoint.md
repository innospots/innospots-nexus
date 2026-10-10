# 包 `console.ui.endpoint`（及 `console.ui.domain.request`）

## DefaultPageDslEndpoint

**类型：** class（final），`@Path("/api/public/pages/{pageKey}")`，`@Tag(name = "UiPage")`

默认 PageDslEndpoint 实现：从 classpath 加载页面 DSL 文档，并通过已配置的过滤器链处理。
公共开放 API（`ConsoleConstant.PUBLIC_API_PREFIX`，免 Bearer 鉴权）；页面本身的权限由
Sitemap / DSL `permission` 声明与会话裁剪控制。

### 构造方法

#### `DefaultPageDslEndpoint(PageDslLoader loader, PageDslFilterChain filterChain)`
- **参数：**
  - `loader` — classpath DSL 加载器
  - `filterChain` — 渲染前过滤器链

### 端点

#### `GET render(@BeanParam PageDslRenderRequest request, @Context UriInfo uriInfo) → R<PageDsl>`
- **说明：** `pageKey` 为复合键 `{domainKey}-{moduleKey}-{xxx}`（`PageDslPageRef.decode` 解析为
  domain/module/pageSuffix）；其余查询参数（`?key=value`）绑定到页面 state
- **返回：** `R.ok(...)` 包装的 PageDsl 文档

## PageDslEndpoint

**类型：** interface

加载与准备页面 DSL 文档的渲染时 API。

## 包 `console.ui.domain.request`

## PageDslRenderRequest

**类型：** class（final，`@Schema`），`@BeanParam` 绑定

页面 DSL 渲染路径参数：复合 `pageKey`（`{domainKey}-{moduleKey}-{xxx}`；domain/module
不含连字符）。由 `DefaultPageDslEndpoint` 从 `UriInfo` 收集查询参数，不放在本类型中。

### 构造方法

#### `PageDslRenderRequest()`
- **说明：** JAX-RS `@BeanParam` 绑定用默认构造器

### 方法

#### `pageKey() → String`
- **说明：** 返回复合 pageKey，如 `nexus-role-main`