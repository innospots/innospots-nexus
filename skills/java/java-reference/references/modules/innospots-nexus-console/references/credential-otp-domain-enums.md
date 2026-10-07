# 包 `credential.otp.domain.enums`

## OtpChannel

**类型：** enum

OTP 下发通道；决定地址规范化规则、com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent 后缀，以及与 legacy VerificationType 的互转（仅 EMAIL/MOBILE）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `EMAIL` | — |
| `MOBILE` | — |
| `WEBHOOK` | — |
| `IN_APP` | — |

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| ` ` | `* 电子邮件；地址规范化为 trim + 小写。
     */
    EMAIL,

    /**
     * 短信/语音等手机号通道；地址规范化为去空格。
     */
    MOBILE,

    /**
     * 向租户配置的 Webhook URL 投递（扩展通道）。
     */
    WEBHOOK,

    /**
     * 应用内消息（站内信等），无 legacy {@link VerificationType} 映射。
     */
    IN_APP,

    /**
     * 图形验证码；{@code destination} 为客户端会话键，明文码经 API 以图片返回而非事件投递。` | 电子邮件；地址规范化为 trim + 小写。 / EMAIL, /** 短信/语音等手机号通道；地址规范化为去空格。 / MOBILE, /** 向租户配置的 Webhook URL 投递（扩展通道）。 / WEBHOOK, /** 应用内消息（站内信等），无 legacy VerificationType 映射。 / IN_APP, /** 图形验证码；destination 为客户端会话键，明文码经 API 以图片返回而非事件投递。 / CAPTCHA; |

### 方法

#### `fromVerificationType(VerificationType type) → OtpChannel`
- **说明：** 电子邮件；地址规范化为 trim + 小写。 / EMAIL, /** 短信/语音等手机号通道；地址规范化为去空格。 / MOBILE, /** 向租户配置的 Webhook URL 投递（扩展通道）。 / WEBHOOK, /** 应用内消息（站内信等），无 legacy VerificationType 映射。 / IN_APP, /** 图形验证码；destination 为客户端会话键，明文码经 API 以图片返回而非事件投递。 / CAPTCHA; /** 从 portal 使用的 VerificationType 转为 OTP 通道。 调用场景：忘记密码流程仅支持邮箱与手机。
- **参数：**
  - `type` — 验证类型
- **返回：** 对应通道

#### `toVerificationType() → VerificationType`
- **说明：** 转回 VerificationType，供与旧 API 互操作。
- **返回：** EMAIL 或 MOBILE

#### `normalizeDestination(String destination) → String`
- **说明：** 规范化投递地址，作为挑战表查询键与重发冷却键。 调用场景：com.innospots.nexus.console.credential.otp.service.OtpChallengeService 发放与校验前统一调用。
- **参数：**
  - `destination` — 用户输入的邮箱或手机号等
- **返回：** 规范化后的地址


## OtpPurpose

**类型：** enum

OTP 业务用途；持久化为 com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity 字符串。 校验时必须与发放时一致，防止用「忘记密码」码完成「登录 step-up」等跨场景复用。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PASSWORD_RESET` | — |
| `LOGIN_STEP_UP` | — |
| `BIND_CONTACT` | — |
| `CAPTCHA` | — |
