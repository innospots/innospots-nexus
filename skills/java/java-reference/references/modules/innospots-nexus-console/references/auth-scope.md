# 认证、凭证与作用域

控制台定义**机制**和**契约**；kernel 和 platform 提供**身份数据**。

## 安全域

```text
SecurityRealm.PLATFORM  →  /platform/auth/**
SecurityRealm.TENANT    →  /tenant/auth/**, /tenant/scope/**
```

`AuthUser`、`TokenClaims` 和 `AuthTokenVo` 携带 `realm`，使一套认证栈同时服务运维和租户控制台。

## 令牌会话阶梯（TENANT）

```text
register/login     → identity token (may list multiple tenants)
select-tenant      → tenant business token
select-workspace   → workspace-scoped token
select-project     → project-scoped token
refresh / logout   → pair rotation / revocation
```

`ScopeFacade` 和 `AuthFacade` 编排转换；`AuthTokenPairIssuer` 从 `TokenClaims` 签发令牌对。

## 端口接口（在上层模块中实现）

| 端口 | 职责 | 典型归属 |
|------|----------------|---------------|
| `UserDirectory` | `findByLogin(realm, identity)` | kernel（TENANT）、platform（PLATFORM） |
| `MembershipDirectory` | 多租户登录的租户成员关系 | kernel |
| `CredentialStore` | 存储的凭证哈希/材料 | kernel / platform |
| `PasswordDecryptor` | 解密客户端载荷 | adapter |
| `PasswordVerificationOperator` | 验证解密后的密码 | kernel / platform |
| `TenantScopeDirectory` | `TenantScope` 快照 | kernel |
| `WorkspaceScopeDirectory` | `WorkspaceSnapshot` | kernel |
| `ProjectScopeDirectory` | `ProjectSnapshot` | kernel |

控制台**不得**为这些端口持久化用户或租户业务实体。

## 会话绑定

认证或作用域选择成功后：

```text
AuthFacade / ScopeFacade
    → SessionScopeBinder.bindAfterAuth(user, scope)
    → SessionContext + TLC (base)
```

`TenantScope` 组合 `TenantSnapshot` + `OrganizationSnapshot`（来自 base 的传输形态）。
当 `AuthSessionScope` 中存在 ID 时，通过作用域目录加载工作空间/项目快照。

## 关键类型

| 类型 | 角色 |
|------|------|
| `AuthLoginRequest` | `login` + `encryptedPassword` |
| `TenantRegisterRequest` | 身份注册（尚无 `TenantMember`） |
| `SelectTenantRequest` | 用身份令牌交换租户令牌 |
| `SelectWorkspaceRequest` | `tenantId` + `workspaceId` |
| `SelectProjectRequest` | `tenantId` + `workspaceId` + `projectId` |
| `AuthTokenVo` | 访问 + 刷新令牌、过期元数据 |
| `TokenClaims` | 签名前的内部声明 |
| `CredentialRecord` | 存储的凭证材料模型 |

## 密码流程

- 登录/注册接受来自客户端的**加密**密码。
- `PasswordChangeRequest` / `PasswordResetRequest` 使用相同的加密契约。
- `PasswordValidator`（console）在持久化委托给 `CredentialStore` 之前执行本地密码策略规则。

## 适配器职责（不在 console 中）

- JWT/会话 Cookie 签发与验证
- 从令牌填充 `AuthorizationSubjectResolver`
- 在 servlet/JAX-RS 过滤器中调用 `RequestAuthorizer`（见 catalog-permission.md）
- 将 `UserDirectory` 等连接到 kernel/platform Bean
