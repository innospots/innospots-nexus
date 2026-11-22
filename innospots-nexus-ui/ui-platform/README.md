# ui-platform

运营管理平台前端（Nexus Console）的 Pactor apps-shell 工程：

```text
ui-platform/
├── ui-platform-shell/      # 统一 Web 宿主（Next.js，:9020）
└── ui-platform-app/        # console UI 模块（:9011 独立调试）
```

- `ui-platform-app`：模块 id `console`，URL 前缀 `/console`；页面 DSL、组件、
  BFF 与语言包随 `dist-module/` 交付。
- `ui-platform-shell`：以 `file:` 安装 app 打出的 tgz，`pactor-shell assemble` 生成路由 /
  菜单 / 注册表，运行期通过 `/api/runtime/*` 渲染。

## 快速开始

```bash
cd ui-platform-app
pnpm install
pnpm build:module && pnpm pack:module

cd ../ui-platform-shell
pnpm install
pnpm assemble:modules
pnpm dev            # http://localhost:9020/console
```

独立调试控制台：`cd ui-platform-app && pnpm dev`（:9011）。
