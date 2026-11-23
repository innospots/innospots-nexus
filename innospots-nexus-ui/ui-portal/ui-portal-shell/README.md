# ui-portal-shell

Nexus 用户门户统一 Web 宿主（Next.js，:9030）。

- `src/shell/sitemap.yaml`：站点品牌与导航（工作台 / 设置）。
- `src/modules/module-list.json`：安装 `@innospots/ui-portal-app`。
- `src/app/(workspace)`：WorkspaceShell（AppShell + SitemapApp）。
- `src/app/api/runtime/*`：bootstrap / 页面 / fragment。
- `src/app/api/modules/*`：模块 API 分发。
- `src/app/api/auth/*`、`/api/d/*`：portal 模块兼容路径。

## 开发

```bash
pnpm install
pnpm assemble:modules
pnpm dev              # http://localhost:9030/portal
```
