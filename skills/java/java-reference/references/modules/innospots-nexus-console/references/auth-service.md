# 包 `auth.service`

## AuthFacade

**类型：** class

运维平台域登录与令牌刷新编排。租户域认证见 portal.auth.service.TenantAuthFacade。

### 方法

#### `realm() → SecurityRealm`
- **说明：** 所属安全域（构造时绑定）。 / public SecurityRealm realm()

#### `issueLoginCaptcha(String clientKey) → AuthCaptchaVo`
- **说明：** 发放登录用图形验证码。
- **参数：**
  - `clientKey` — 客户端事务键；空白时由服务端生成

#### `login(AuthLoginRequest request) → AuthTokenVo`
- **说明：** 认证域用户并签发令牌对。
- **参数：**
  - `request` — 登录身份与加密密码
- **返回：** issued 令牌对

#### `refresh(TokenRefreshRequest request) → AuthTokenVo`
- **说明：** 从同域刷新令牌签发新令牌对。
- **参数：**
  - `request` — 刷新令牌
- **返回：** new 令牌对

#### `logout() → void`
- **说明：** 完成登出。紧凑令牌在过期前仍然有效。 / public void logout()


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


## LoginCaptchaGate

**类型：** class

登录前图形验证码策略与编排；实现委托 CaptchaChallengeService。

### 方法

#### `isRequired(SecurityRealm realm) → boolean`
- **说明：** 当前安全域是否要求登录图形码。 / public boolean isRequired(SecurityRealm realm)

#### `issueForLogin(SecurityRealm realm, String clientKey) → AuthCaptchaVo`
- **说明：** 发放登录用图形验证码。
- **参数：**
  - `realm` — 平台或租户域
  - `clientKey` — 客户端事务键；空白时自动生成

#### `verifyIfRequired(SecurityRealm realm, AuthLoginRequest request) → void`
- **说明：** 若策略开启则校验登录请求中的图形码；失败对外统一为认证失败。 / public void verifyIfRequired(SecurityRealm realm, AuthLoginRequest request)


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
