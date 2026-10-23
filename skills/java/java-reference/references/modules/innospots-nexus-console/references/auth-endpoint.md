# 包 `auth.endpoint`

## AuthSessionEndpoint

**类型：** interface，`@EndpointModule("auth")`

已登录会话 JAX-RS 契约。类级 `@Path` 由 portal/platform 的业务实现绑定；实现类必须重复
方法级 JAX-RS、参数绑定与 OpenAPI 注解供构建期扫描，但不得重复 `@EndpointModule` /
`@EndpointGrant`。

### 端点

| 方法 | 相对路径 | 返回 |
|------|----------|------|
| POST `refresh` | `/refresh` | `R<AuthTokenVo>` |
| POST `logout` | `/logout` | `R<Void>` |
| POST `changePassword` | `/password/change` | `R<Void>` |

## PublicAuthEndpoint

**类型：** interface，`@EndpointModule(value = "auth", ignoreAuth = true)`

匿名认证 JAX-RS 契约。

### 端点

| 方法 | 相对路径 | 返回 |
|------|----------|------|
| GET `getPasswordEncryptionPublicKey` | `/rsa-public-key` | `R<AuthRsaPublicKeyVo>` |
| POST `issueLoginCaptcha` | `/captcha/issue` | `R<AuthCaptchaVo>` |
| POST `login` | `/login` | `R<AuthTokenVo>` |

## PublicPasswordResetEndpoint

**类型：** interface，`@EndpointModule(value = "auth", ignoreAuth = true)`

匿名密码重置 JAX-RS 契约。

### 端点

| 方法 | 相对路径 | 返回 |
|------|----------|------|
| POST `resetPassword` | `/reset` | `R<Void>` |
