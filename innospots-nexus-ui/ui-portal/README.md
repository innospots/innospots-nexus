# ui-portal

租户用户门户前端（Nexus Portal）的 Pactor apps-shell 工程：

```text
ui-portal/
├── ui-portal-shell/      # 统一 Web 宿主（Next.js，:9030）
└── ui-portal-app/        # portal UI 模块（:9031 独立调试）
```

- `ui-portal-app`：模块 id `portal`，URL 前缀 `/portal`；提供登录、注册、
  工作台与个人设置页面；注册对接独立的 `/tenant/registration/*` 领域，
  登录与令牌对接 `/tenant/auth/*`。
- `ui-portal-shell`：以 `file:` 安装 app 打出的 tgz，`pactor-shell assemble`
  生成路由 / 菜单 / 注册表，运行期通过 `/api/runtime/*` 渲染。

## 后端

```bash
cd innospots-nexus/innospots-nexus-sample
mvn -pl innospots-nexus-sample-spring-portal -am -DskipTests package
cd dist && tar -xzf innospots-nexus-sample-spring-portal.tar.gz
cd innospots-nexus-sample-spring-portal && ./bin/startup.sh
```

后端默认 `http://localhost:8081`，可用 `NEXT_PUBLIC_API_BASE_URL` 覆盖。

## 快速开始

```bash
cd ui-portal-app
pnpm install
pnpm build:module && pnpm pack:module

cd ../ui-portal-shell
pnpm install
pnpm assemble:modules
pnpm dev            # http://localhost:9030/portal
```

独立调试模块：`cd ui-portal-app && pnpm dev`（:9031）。

## 当前页面

| 路径 | 页面 | 访问 |
|------|------|------|
| `/portal/login` | 登录 | 公开 |
| `/portal/register` | 注册 | 公开 |
| `/portal` | 工作台 | 登录后 |
| `/portal/settings/profile` | 个人设置 | 登录后 |
| `/portal/settings/appearance` | 界面设置 | 登录后 |
| `/portal/settings/ai` | AI 设置 | 登录后 |

导航为「左轨一级菜单 + 二级菜单」：左轨承载工作台等一级入口，左下角齿轮进入设置，
设置二级菜单包含个人设置 / 界面设置 / AI 设置。
