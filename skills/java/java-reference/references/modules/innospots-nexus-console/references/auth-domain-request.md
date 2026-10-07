# 包 `auth.domain.request`

## AuthCaptchaIssueRequest

**类型：** record

登录前图形验证码发放请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `clientKey` | `String` | 客户端事务键；为空时由服务端生成并在响应中返回 |


## AuthLoginRequest

**类型：** record

两个域共用的密码登录载荷。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `login` | `String` | user_name、email 或 mobile |
| `encryptedPassword` | `String` | 前端加密密码 |
| `captchaClientKey` | `String` | 登录图形码发放时的客户端键；策略关闭时可空 |
| `captchaCode` | `String` | 用户输入的图形验证码；策略关闭时可空 |


## PasswordChangeRequest

**类型：** record

已认证用户的密码修改。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `oldEncryptedPassword` | `String` | 当前前端加密密码 |
| `newEncryptedPassword` | `String` | 期望的前端加密密码 |


## PasswordResetRequest

**类型：** record

使用验证码重置密码。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `identity` | `String` | user_name、email 或 mobile |
| `verificationCode` | `String` | 一次性验证码 |
| `type` | `VerificationType` | EMAIL 或 MOBILE |
| `newEncryptedPassword` | `String` | 前端加密的新密码 |


## TokenRefreshRequest

**类型：** record

刷新令牌交换。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `refreshToken` | `String` | 同域签发的刷新令牌 |
