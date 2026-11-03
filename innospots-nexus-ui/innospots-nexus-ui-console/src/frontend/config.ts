/**
 * 入口配置（design/dynamic-page.md）：开发者在此配置一个或多个
 * 返回 sitemap 对象的接口 URL。多源时 pages / menus / layouts 按键合并
 * （id / path 冲突后源覆盖前源并告警），app / user / permissions / auth
 * 取第一个源。
 *
 * 外部接口：sources 只写相对路径，由 apiBaseUrl 补全为外部绝对地址；
 * 服务端已开通 CORS（cookie 会话见 src/backend/cors.ts），浏览器直连，
 * 无需项目内代理。
 */
export const sitemapSources: string[] = ["/api/sitemap"];

/**
 * 懒加载页面端点（SitemapApp pageEndpoint）。
 * 框架缺省为 /api/pages/{id}；本仓库服务端路由在 src/app/api/public/pages/[id]，
 * 需显式覆盖，否则客户端仍会请求框架缺省路径。
 */
export const pageEndpoint = (pageId: string) =>
  `/api/public/pages/${pageId}`;

/** 页面切换前 catalog 访问校验（BFF → 平台 page-access/verify） */
export const pageAccessEndpoint = "/api/page-access/verify";

/**
 * 客户端接口基准地址（SitemapApp baseUrl）。
 * 数据接口全部同源经转发层（src/app/api/d/[...parts]）调用，故为空字符串
 * （相对路径由浏览器按同源解析）。
 */
export const apiBaseUrl = "";

/** 左轨导航：悬停时展开显示模块标题。 */
export const navigationConfig = { expandRailOnHover: false };
