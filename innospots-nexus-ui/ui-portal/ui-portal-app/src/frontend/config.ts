/**
 * 入口配置：SitemapApp 从本模块的 `/api/sitemap` 读取页面清单，
 * 数据接口统一经 `/api/d/*` 转发到 spring-portal。
 */
export const sitemapSources: string[] = ["/api/sitemap"];

/** 懒加载页面端点。 */
export const pageEndpoint = (pageId: string) => `/api/public/pages/${pageId}`;

/** 客户端接口基准地址：同源相对路径。 */
export const apiBaseUrl = "";
