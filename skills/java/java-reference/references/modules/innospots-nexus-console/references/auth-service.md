# 包 `auth.service`

## AuthFacade

**类型：** class

编排域登录、租户选择与令牌刷新。 用户行保留在 platform 或 portal；本门面仅使用目录端口。

### 方法

#### `login(SecurityRealm realm, AuthLoginRequest request) → AuthTokenVo`

- **说明：** 认证域用户并签发令牌对。
- **参数：**
  - `realm` — PLATFORM 或 TENANT
  - `request` — 登录身份与加密密码
- **返回：** issued 令牌对

#### `selectTenant(String tenantUserId, SelectTenantRequest request) → AuthTokenVo`

- **说明：** 将租户身份交换为绑定单一成员关系的业务令牌。
- **参数：**
  - `tenantUserId` — tenant-realm user 标识符
  - `request` — 待激活的租户
- **返回：** TENANT 业务令牌

#### `refresh(SecurityRealm realm, TokenRefreshRequest request) → AuthTokenVo`

- **说明：** 从同域刷新令牌签发新令牌对。
- **参数：**
  - `realm` — 期望的安全域
  - `request` — 刷新令牌
- **返回：** new 令牌对

#### `logout() → void`

- **说明：** 完成登出。紧凑令牌在过期前仍然有效。

## AuthSessionScope

**类型：** record

已签发认证令牌对携带的作用域。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tokenType` | `String` | IDENTITY 或 BUSINESS |
| `tenantId` | `String` | BUSINESS 令牌上的租户 |
| `tenantMemberId` | `String` | BUSINESS 令牌上的租户成员 |
| `workspaceId` | `String` | 作用域 BUSINESS 令牌上的活跃工作区 |
| `projectId` | `String` | 作用域 BUSINESS 令牌上的活跃项目 |

## AuthTokenPairIssuer

**类型：** class

从会话作用域签发访问与刷新令牌对。

### 方法

#### `issue(SecurityRealm realm, String userId, AuthSessionScope scope) → AuthTokenVo`

- **说明：** 为给定域用户与会话作用域签发令牌对。
- **参数：**
  - `realm` — PLATFORM 或 TENANT
  - `userId` — platform or tenant user 标识符
  - `scope` — 编码进声明的会话作用域
- **返回：** issued 令牌对

## TokenIssuer

**类型：** class

签发并解析 AES-GCM 紧凑令牌。Console 不持久化用户。

### 方法

#### `issue(TokenClaims claims) → String`

- **说明：** 将声明加密为紧凑令牌字符串。
- **参数：**
  - `claims` — 令牌声明
- **返回：** AES-GCM 紧凑令牌

#### `parse(String token) → TokenClaims`

- **说明：** 将紧凑令牌解密为声明。
- **参数：**
  - `token` — 紧凑令牌
- **返回：** parsed 声明

#### `accessTokenTtlSeconds() → long`

- **说明：** 返回访问令牌有效期（秒）。
- **返回：** access TTL

#### `refreshTokenTtlSeconds() → long`

- **说明：** 返回刷新令牌有效期（秒）。
- **返回：** refresh TTL
