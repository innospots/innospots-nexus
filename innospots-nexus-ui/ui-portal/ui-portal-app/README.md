# ui-portal-app

Nexus 用户门户 UI 模块（Pactor module id `portal`，basePath `/portal`）。

## 页面

| 页面 id | 路径 | 访问模式 | 组件 |
|---------|------|----------|------|
| `login` | `/login` | public | `PortalLoginForm` |
| `register` | `/register` | public | `PortalRegisterForm` |
| `dashboard` | `/` | authenticated | DSL 工作台 |
| `settings-profile` | `/settings/profile` | authenticated | `PortalProfileSettings` |
| `settings-appearance` | `/settings/appearance` | authenticated | `PortalAppearanceSettings` |
| `settings-ai` | `/settings/ai` | authenticated | `PortalAiSettings` |

导航为左轨一级菜单 + 二级菜单；左下角齿轮进入设置，二级菜单为
个人设置 / 界面设置 / AI 设置。

## 后端接口

BFF 同源路由：

- `POST /api/auth/login` → `/tenant/auth/login`
- `POST /api/auth/logout` → `/tenant/auth/logout`
- `GET /api/auth/session`（读取门户 cookie）
- `GET /api/auth/rsa-public-key` → `/tenant/auth/rsa-public-key`
- `POST /api/registration/register` → `/tenant/registration/register`（独立注册领域）
  → 成功后调用 `/tenant/auth/login` 完成自动登录

密码在浏览器用 RSA-OAEP(SHA-256) 加密后提交；access token 存在 httpOnly
cookie `nexus_portal_access_token`。

## 开发

```bash
pnpm install
pnpm dev              # :9031，后端默认 http://localhost:8081
pnpm build:module
pnpm pack:module
```
