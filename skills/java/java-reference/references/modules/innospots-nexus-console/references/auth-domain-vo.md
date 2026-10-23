# 包 `auth.domain.vo`

## AuthCaptchaVo

**类型：** record

登录图形验证码发放结果（REST 形状）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `challengeId` | `String` | 挑战 ID |
| `clientKey` | `String` | 校验登录时需回传的客户端键（与发放时一致） |
| `imageBase64` | `String` | 图片 Base64（不含 data URI 前缀） |
| `imageMimeType` | `String` | 建议 MIME |
| `style` | `CaptchaStyle` | 绘制样式 |
| `expiresAt` | `LocalDateTime` | 过期时间 |

### 方法

#### `from(CaptchaIssueResult result, String clientKey) → AuthCaptchaVo`
- **说明：** 从凭据域发放结果构造；clientKey 与发放命令一致。 / public static AuthCaptchaVo from(CaptchaIssueResult result, String clientKey)


## AuthTokenVo

**类型：** record

登录、注册、刷新或租户选择后返回的令牌对。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `realm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `tokenType` | `String` | IDENTITY 或 BUSINESS |
| `accessToken` | `String` | 访问令牌 |
| `refreshToken` | `String` | 刷新令牌 |
| `tenantId` | `String` | 在 TENANT 业务令牌上设置 |
| `tenantMemberId` | `String` | 在 TENANT 业务令牌上设置 |
| `workspaceId` | `String` | 工作区活跃时设置 |
| `projectId` | `String` | 项目活跃时设置 |

## AuthRsaPublicKeyVo

**类型：** record

登录密码 RSA 加密用公钥。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `publicKeyBase64` | `String` | X.509 SubjectPublicKeyInfo 公钥 Base64，不含 PEM 头尾 |

## PublicRegistrationPolicyVo

**类型：** record

匿名注册入口与 OTP 校验策略，供登录/注册页决定路由与表单项显隐。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `registrationMode` | `RegistrationMode` | 当前自助注册模式 |
| `otpVerification` | `RegistrationOtpVerification` | 注册 OTP 校验方式 |
| `openRegistrationEnabled` | `boolean` | 是否开放 `/register/open` |
| `inviteRegistrationEnabled` | `boolean` | 是否开放邀请码注册 |
| `approvalRegistrationEnabled` | `boolean` | 是否开放待审核注册 |
