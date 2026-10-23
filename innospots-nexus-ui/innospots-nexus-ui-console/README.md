# Nexus 管理控制台（`@innospots/ui-console`）

Next.js + Pactor 控制台：**同一套源码**支持两种交付方式，互不替代。

| 模式 | 用途 | 构建命令 | 部署物 |
| --- | --- | --- | --- |
| **独立应用** | 单独域名/进程运行完整控制台 | `npm run build:app`（或 `npm run build`） | `.next/` + `public/` + `node_modules`（或 standalone 镜像） |
| **UI 模块** | 装配进 InnoSpots Web Shell | `npm run build:module` | 仅 `dist-module/`（npm pack / 私有 registry） |

设计原则：**`app/`、`.next/` 永不进入 npm 模块包**；`build:module` **不会**执行 `next build`，也不会改动 `.next`。

---

## 独立发布与独立部署（默认能力，必须保持）

面向「只部署本控制台、不经过 Shell」的场景，行为与模块化改造前一致。

### 开发

```bash
npm install
npm run dev          # http://localhost:9011
```

- 页面 DSL：`src/server/yaml/`
- Sitemap / 会话裁剪：`src/server/sitemap.ts` → `GET /api/sitemap`
- 懒加载页：`GET /api/public/pages/{id}`
- 数据转发：`app/api/d/[...parts]` → `NEXT_PUBLIC_API_BASE_URL`
- 客户端入口：`app/layout.tsx` 内 `PactorApp`（`src/pactor/App.tsx`）

### 生产构建与启动

```bash
npm run build:app    # 等价 npm run build；按 APP_ENV 打 production 包
npm run start        # next start -p 9011
```

其他环境：

```bash
npm run build:app:dev
npm run build:app:test
```

### 部署说明

- 发布的是 **本仓库整应用**（Git/CI 构建），**不是** `npm publish` 的 `files` 字段内容。
- `package.json` 中 `"files": ["dist-module"]` **只限制 npm 模块包**体积，不影响 Docker/K8s 对 `next build` 产物的部署。
- 接真实后端：调整 `next.config.js` 的 `API_BASE_URLS` / 环境变量，**无需**为 Shell 改 YAML 路径（独立模式下 URL 仍为 `/api/d/*` 等同源路径）。

演示账号见 `src/server/accounts.ts`。

---

## 模块打包（供 Web Shell 装配）

面向「多个 UI 包合并进统一 Shell」的场景。

### 构建与本地验证

```bash
npm run build:module   # 产出 dist-module/
npm run pack:module    # 构建 + dry-run + 生成 *.tgz
```

### 发布到 registry（可选）

```bash
npm run publish:module
```

需配置 npm 登录；`private: true` 时请先按需调整发布策略。

### 模块产物结构

```text
dist-module/
├── manifest.json
├── dsl/                 # 自 src/server/yaml 复制
├── assets/              # 自 public/ 复制
├── client/index.js      # extensions（components + actions）
├── styles.css
└── module-assets.json
```

契约入口：`package.json` → `exports`（`./manifest`、`./client`、`./styles.css`）。

### 与 Shell 联调

```bash
# 本仓库
npm run build:module

# Web Shell（frontend_template）
npm install file:../innospots-nexus-ui-console   # 或 install *.tgz
# module-list.json 启用 @innospots/ui-console，basePath 如 /console
npm run assemble:modules && npm run dev
```

Shell 侧 URL 带 `basePath` 前缀（如 `/console/system/roles`）；模块内 `sitemap.yaml` 的 `path` 仍为**模块内路径**（如 `/system/roles`）。

---

## 双模式如何共用源码

| 资产 | 独立应用 | UI 模块 |
| --- | --- | --- |
| 页面 YAML | `src/server/yaml/**` | `build:module` → `dist-module/dsl/**` |
| 自定义组件 / Actions | `src/pactor/components`、`actions.ts` | `module/client.ts` → esbuild → `dist-module/client/` |
| 静态资源 | `public/` | `dist-module/assets/` |
| 运行时接线 | `src/pactor/App.tsx` + `app/api/*` | **由 Shell** 提供 Runtime API 与 `PactorHost` |
| Next 路由 | `app/[[...slug]]` + layout 挂 PactorApp | 不打进模块包 |

**开发建议**：日常功能在独立应用下 `npm run dev` 验证；合入 Shell 前执行 `build:module` 并在 Shell 中 `assemble:modules` 做集成验证。

---

## 工程内容（独立应用）

- **公共与业务页面**：`src/server/yaml/`（含 `sitemap.yaml`）
- **接线**：`src/pactor/`（SitemapApp、组件、动作、HTTP 钩子）
- **服务端**：`src/server/sitemap.ts`、会话与 yaml 加载
- **API**：`app/api/sitemap`、`public/pages`、`d` 转发、`auth` 等
- **皮肤**：`public/skins/`

## 新建业务时

1. 在 `src/server/yaml/` 按域新增 `{pageId}.yaml`，并在 `sitemap.yaml` 注册 `pages` / `menus`
2. 若 DSL 引用新组件，在 `src/pactor/components/index.ts` 注册
3. 独立应用 `npm run dev` 验证后，需要进 Shell 时执行 `npm run build:module`

---

## 相关文档

- Web Shell 装配与路由：`innospots-gen/frontend_template/README.md`
- 模块化设计：InnoSpots 前端模块化技术方案（Build-time Composition）
