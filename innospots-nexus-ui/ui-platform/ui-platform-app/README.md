# ui-platform-app（Nexus 管理控制台模块）

`@innospots/ui-platform-app` 是 `ui-platform` 下的 **Pactor UI 模块**：页面 DSL、
自定义组件 / Actions、静态资源与业务 BFF 均随模块交付，由
`ui-platform-shell` 在构建期装配、运行期渲染。

- 模块 id：`console`，URL 前缀：`/console`（`globalPageId` 形如 `console:role-list`）
- 模块约定参考 `pactor/starters/apps-shell-starter/pactor-apps/general-app`
- `src/app/` 只用于本地开发与调试，**不进入** `dist-module/`

## 目录

```text
ui-platform-app/
├── module/
│   ├── client.ts            # extensions（components / actions）→ dist-module/client
│   ├── client.d.ts          # Shell 装配期类型契约
│   ├── server.ts            # 模块 API handlers → dist-module/server
│   └── server.d.ts
├── public/
│   ├── i18n/                # 语言包（同步自后端 i18n/console）+ 可翻译字段
│   ├── skins/               # 主题 CSS
│   └── themes/
├── src/
│   ├── app/                 # 仅本地开发：[[...slug]]、layout、API 调试入口
│   ├── backend/
│   │   ├── api/             # 模块 BFF（dev route 与 server entry 共用）
│   │   ├── auth/            # 平台会话 cookie
│   │   ├── pages/           # sitemap、i18n、页面 / 操作级鉴权
│   │   └── mock/
│   └── frontend/            # 组件、Actions、i18n、lib、独立运行时的 App
├── pactor.module.json       # 模块打包配置（id / basePath / 入口 / apiPrefixes）
└── tests/                   # node:test 契约测试
```

## 本地开发

```bash
pnpm install
pnpm dev            # http://localhost:9011，独立 Next 调试入口
pnpm typecheck
pnpm test
```

数据接口默认转发到 `http://localhost:8082`（`NEXT_PUBLIC_API_BASE_URL`）。

## 模块构建

```bash
pnpm build:module   # pactor module build → dist-module/
pnpm pack:module    # pactor module pack --no-bump → *.tgz
```

产物结构：

```text
dist-module/
├── manifest.json          # Manifest V1（id=console、basePath=/console、serverEntry…）
├── dsl/                   # sitemap.yaml + 29 个页面 DSL
├── assets/                # public/ 复制（i18n、skins、themes…）
├── client/
│   ├── index.js           # extensions ESM bundle
│   └── index.css          # Tailwind 预编译 + 全局样式
├── server/index.mjs       # 模块 BFF handlers
└── module-assets.json
```

模块契约入口：`package.json` → `exports`（`./manifest`、`./client`、`./server`、
`./styles.css`）。`pactor.module.json` 声明的 API 逻辑前缀为
`/api/d`、`/api/auth`、`/api/i18n`；打包时 DSL 中的 URL 会自动改写为
`/api/modules/console/*`。

## 与 ui-platform-shell 联调

`ui-platform-shell` 以 `file:` 引用本工程打出的 tgz（与 apps-shell
starter 一致），保证模块在 Shell 的 `node_modules` 内解析，Pactor / React 只有
一份实例：

```bash
# 1. 本工程产出并打包模块
cd ui-platform/ui-platform-app
pnpm build:module && pnpm pack:module

# 2. Shell 安装 tgz、装配并启动
cd ../ui-platform-shell
pnpm install
pnpm assemble:modules
pnpm dev            # http://localhost:9020/console
```

模块改动后重复「pack:module → Shell `pnpm install` → `assemble:modules`」。

Shell 侧的兼容层（`ui-platform-shell/src/app/api/{d,i18n,auth/platform-login}`）把模块
客户端仍在使用的旧相对路径转发到 `/api/modules/console/*`，业务实现只保留在
`module/server.ts` 一处。后续可直接改调挂载路径并删除该兼容层。

## 新建页面

1. 在 `src/backend/pages/yaml/<域>/<pageId>.yaml` 新增页面 DSL；
2. 在 `src/backend/pages/yaml/sitemap.yaml` 注册 `pages`；
3. 在 Shell 的 `src/shell/sitemap.yaml` 增加导航（`pageId: console:<pageId>`，
   `path: /console<页面相对路径>`）；
4. `pnpm build:module && pnpm pack:module` → Shell `pnpm install && pnpm assemble:modules`。

> `public/i18n/{zh-CN,en-US}/app.yaml` 目前由
> `innospots-nexus-platform/src/main/resources/i18n/console/**/messages*.properties`
> 汇总生成，供 Shell 运行时与模块自定义组件消费；接入后端 i18n 服务后可改为运行时拉取。
