# 包 `credential.otp.domain`

## CaptchaIssueCommand

**类型：** record

发放图形验证码。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `securityRealm` | `SecurityRealm` | 安全域 |
| `purpose` | `OtpPurpose` | 业务用途（建议 OtpPurpose） |
| `clientKey` | `String` | 客户端会话键（浏览器 tab、登录事务 ID 等），作为挑战表的 destination |
| `captchaPolicy` | `CaptchaPolicy` | 绘制参数，null 时使用 CaptchaPolicy |


## CaptchaIssueResult

**类型：** record

图形验证码发放结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `challengeId` | `String` | 挑战主键，校验时与 clientKey 一起定位记录 |
| `imageBase64` | `String` | PNG/GIF 的 Base64（不含 data URI 前缀） |
| `imageMimeType` | `String` | 建议的 MIME，例如 image/png 或 image/gif |
| `style` | `CaptchaStyle` | 实际使用的绘制样式 |
| `expiresAt` | `LocalDateTime` | 挑战过期时间（服务端时钟） |


## CaptchaVerifyCommand

**类型：** record

校验用户提交的图形验证码。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `securityRealm` | `SecurityRealm` | 与发放一致的安全域 |
| `purpose` | `OtpPurpose` | 与发放一致的用途 |
| `clientKey` | `String` | 与发放一致的客户端键 |
| `captchaCode` | `String` | 用户输入的图形验证码 |


## OtpIssueCommand

**类型：** record

发放 OTP 的不可变命令对象；由 API 层组装后交给 com.innospots.nexus.console.credential.otp.service.OtpChallengeService。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `securityRealm` | `SecurityRealm` | 决定 com.innospots.nexus.console.scope.ConsoleOwnership 归属列 |
| `purpose` | `OtpPurpose` | 与校验、模板键绑定的业务场景，不可与校验时混用 |
| `channel` | `OtpChannel` | 下发通道，决定地址规范化与事件类型 |
| `destination` | `String` | 用户输入的邮箱、手机号等（服务内调用 OtpChannel) |
| `locale` | `String` | 可选的模板语言（如 zh、en） |


## OtpVerifyCommand

**类型：** record

校验 OTP 的不可变命令；查找挑战时使用与发放相同的四维键： realm + purpose + channel + 规范化 destination。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `securityRealm` | `SecurityRealm` | 与发放时相同的安全域 |
| `purpose` | `OtpPurpose` | 与发放时相同的 OtpPurpose |
| `channel` | `OtpChannel` | 与发放时相同的 OtpChannel |
| `destination` | `String` | 与发放时相同的原始地址（将经相同规范化规则） |
| `code` | `String` | 用户输入的数字验证码 |
