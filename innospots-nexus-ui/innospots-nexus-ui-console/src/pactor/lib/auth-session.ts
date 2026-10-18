/**
 * 登录 / 登出后须整页导航：{@link SitemapApp} 的 sitemap 仅在挂载时拉取一次，
 * 匿名快照不会随 cookie 自动刷新（框架缺省 {@code app.reload} 亦用 location.assign）。
 */
export function reloadAfterAuth(to: string = "/"): void {
    if (typeof window !== "undefined") {
        window.location.assign(to);
    }
}
