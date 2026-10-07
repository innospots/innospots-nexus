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
