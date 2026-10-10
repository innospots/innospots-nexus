# 包 `credential.otp.domain.enums`

## OtpChannel

**类型：** enum

OTP 下发通道；决定地址规范化规则、`OtpSendRequestedEvent#eventType()` 后缀，
以及与 legacy `VerificationType` 的互转（仅 EMAIL/MOBILE）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `EMAIL` | 电子邮件；地址规范化为 trim + 小写 |
| `MOBILE` | 短信/语音等手机号通道；地址规范化为去空格 |
| `WEBHOOK` | 向租户配置的 Webhook URL 投递（扩展通道） |
| `IN_APP` | 应用内消息（站内信等），无 legacy `VerificationType` 映射 |
| `CAPTCHA` | 图形验证码；`destination` 为客户端会话键，明文码经 API 以图片返回而非事件投递 |

### 方法

#### `fromVerificationType(VerificationType type) → OtpChannel`（static）
- **说明：** 从 portal 使用的 `VerificationType` 转为 OTP 通道。调用场景：忘记密码流程仅支持邮箱与手机
- **参数：**
  - `type` — 验证类型
- **返回：** 对应通道
- **异常：** `IllegalArgumentException` — 非 EMAIL/MOBILE 时

#### `toVerificationType() → VerificationType`
- **说明：** 转回 `VerificationType`，供与旧 API 互操作
- **返回：** `EMAIL` 或 `MOBILE`
- **异常：** `IllegalArgumentException` — WEBHOOK、IN_APP 等无对应类型时

#### `normalizeDestination(String destination) → String`
- **说明：** 规范化投递地址，作为挑战表查询键与重发冷却键。调用场景：
  `OtpChallengeService` 发放与校验前统一调用
- **参数：**
  - `destination` — 用户输入的邮箱或手机号等
- **返回：** 规范化后的地址


## OtpPurpose

**类型：** enum

OTP 业务用途；持久化为 `OtpChallengeEntity.getPurpose()` 字符串。校验时必须与发放时
一致，防止用「忘记密码」码完成「登录 step-up」等跨场景复用。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PASSWORD_RESET` | 忘记密码 / 重置密码 |
| `LOGIN_STEP_UP` | 登录二次校验（短信/邮件 OTP） |
| `BIND_CONTACT` | 绑定或变更联系方式 |
| `CAPTCHA` | 图形人机校验（登录前、发送短信 OTP 前等） |
| `PLATFORM_INVITE_DELIVERY` | 平台邀请在线交付（新建/重发邀请时通知受邀人） |
| `PLATFORM_ACCESS_VERIFY` | 平台主动注册申请时的身份验证码 |
| `PLATFORM_INVITE_ACCEPT` | 平台接受邀请页的身份验证码 |
| `PLATFORM_OPEN_REGISTRATION` | 平台完全开放自助注册时的身份验证码 |