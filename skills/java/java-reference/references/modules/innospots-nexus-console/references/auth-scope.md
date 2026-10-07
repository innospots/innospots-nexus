# 认证、作用域与凭据

## 平台域认证（console）

| 类型 | 说明 |
|------|------|
| `AuthFacade` | 平台域登录、刷新、登出编排 |
| `AuthTokenPairIssuer` | 从 claims 签发访问/刷新令牌对 |
| `UserDirectory` | 按域查找登录身份（portal/platform 实现） |
| `MembershipDirectory` | 成员关系解析（portal） |

租户域 `TenantAuthFacade` 与 `/tenant/auth` **不在** console 模块。

## 作用域与归属

| 类型 | 说明 |
|------|------|
| `ConsoleOwnership` / `ConsoleOwnershipLevel` | 控制台持久化归属三元组 |
| `ConsoleOwnershipScope` | 查询/写入归属列 |
| `ConsoleOwnershipGuard` | 从会话解析 TENANT/WORKSPACE 归属 |
| `SessionScopeBinder` | 端口：绑定用户与快照到 `SessionContext`/`TLC` |
| `SessionScopeTlc` | TLC 写入辅助 |

## 凭据（`credential`）

- **password**：`CredentialService`、`UserCredentialEntity`、`PasswordVerificationOperator`
- **otp**：挑战、验证码、captcha 策略
- **totp**：注册与校验适配器

详见包级文件：`auth-api.md`、`auth-service.md`、`scope.md`、`scope-service.md`、`credential-*.md`。
