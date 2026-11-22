# ui-platform-shell

基于 Next.js App Router + Pactor 的统一 Web 宿主：构建期装配 `ui-platform-app`
（模块 id `console`），运行期通过 Runtime DSL API 与 `SitemapApp` 渲染页面。

```text
ui-platform/
├── ui-platform-shell/   # 本工程（:9020）
└── ui-platform-app/     # console UI 模块（:9011 独立调试）
```

## 命令

```bash
# 首次或模块更新后：先在 ../ui-platform-app 执行 build:module && pack:module
pnpm install            # 安装 tgz 依赖（file:../ui-platform-app/*.tgz）
pnpm assemble:modules   # 读取 module-list + 模块 manifest → .generated/pactor
pnpm validate:modules   # 校验路由 / globalPageId / 菜单引用 / 模块 API 前缀
pnpm dev                # http://localhost:9020 → /console
pnpm build              # assemble → validate → theme sync → next build
pnpm start              # next start -p 9020
```

模块升级流程：在 `ui-platform-app` 执行 `pnpm build:module && pnpm pack:module`，
回到本工程 `pnpm install && pnpm assemble:modules` 后重启（`file:` 引用 tgz）。

## 结构

```text
ui-platform-shell/
├── src/
│   ├── app/
│   │   ├── (workspace)/[...slug]   # 必选 catch-all，按装配路由表校验
│   │   ├── (workspace)/layout      # WorkspaceShell + 模块样式
│   │   └── api/
│   │       ├── runtime/            # bootstrap / pages / fragments
│   │       ├── modules/            # /api/modules/<moduleId>/* 通用分发
│   │       ├── d|i18n|auth/        # 兼容层 → 模块挂载路径（过渡）
│   │       └── auth/               # 宿主登录 / 登出
│   ├── backend/
│   │   ├── session.ts              # 平台 token cookie + 演示会话兜底
│   │   ├── module-compat.ts        # 旧相对路径 → /api/modules/console/*
│   │   └── cors.ts
│   ├── frontend/WorkspaceShell.tsx # AppShell + SitemapApp（透传模块语言包）
│   ├── modules/module-list.json    # 启用模块
│   └── shell/sitemap.yaml          # 品牌、登录入口、全量导航
├── .generated/pactor/              # 装配产物（勿手改）
├── public/modules/                 # 模块静态资源 <moduleId>/<version>/
└── public/themes/                  # pactor-shell theme sync 产物
```

## 模块接入

`src/modules/module-list.json`：

```json
{ "modules": [{ "package": "@innospots/ui-platform-app", "enabled": true, "menuMode": "hidden" }] }
```

- `menuMode: hidden`：模块路由保留、模块自带菜单不进入导航；导航统一由
  `src/shell/sitemap.yaml` 定义（引用 `console:<pageId>` 与 `/console/...` 路径）。
- 未登录跳转 `auth.loginPath = /console/login`（控制台平台登录）。
- 会话：控制台登录写入 `nexus_platform_access_token` / `nexus_platform_login`，
  Shell 的 `resolveSessionUser` 优先读取平台 token，演示会话仅作兜底。

## 已知边界（后续迭代）

- 页面授权仍由 Shell 按 `pages[].permission` 裁剪；控制台 DSL 中的
  `access-mode` / grant 校验尚未在 Shell 运行时生效。
- 模块语言包 `public/i18n/{zh-CN,en-US}/app.yaml` 由后端
  `i18n/console/**/messages*.properties` 汇总生成；Shell 在 layout 中读取并透传给
  `SitemapApp`，页面 DSL 与模块组件共用同一套词条。接入后端 i18n 服务后可改为运行时拉取。
- `src/app/api/{d,i18n,auth/platform-login}` 是过渡兼容层，模块客户端改为挂载路径后可删除。
