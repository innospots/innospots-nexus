# 包 `credential.otp.service`

## CaptchaChallengeService

**类型：** class

图形验证码（captchaCode）发放与校验门面，底层复用 OtpChallengeService 挑战表。 图片由 Hutool 同步生成，不发布 com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent。

### 方法

#### `issue(CaptchaIssueCommand command) → CaptchaIssueResult`
- **说明：** 生成图形验证码并持久化哈希，返回 Base64 图片。
- **参数：**
  - `command` — 发放命令
- **返回：** 挑战 ID、图片与过期时间

#### `verify(CaptchaVerifyCommand command) → boolean`
- **说明：** 校验 captchaCode；失败返回 false。 / public boolean verify(CaptchaVerifyCommand command)

#### `verifyOrThrow(CaptchaVerifyCommand command) → void`
- **说明：** 校验 captchaCode；失败抛出 com.innospots.nexus.console.credential.otp.status.OtpStatusCode。 / public void verifyOrThrow(CaptchaVerifyCommand command)

#### `invalidate(CaptchaVerifyCommand command) → void`
- **说明：** 作废当前客户端键下未消费的图形验证码挑战。 / public void invalidate(CaptchaVerifyCommand command)


## OtpChallengeService

**类型：** class

OTP 挑战的唯一样式入口：发放、校验、作废，并在发放后发布 OtpSendRequestedEvent。 持久化仅保存 OtpChallengeEntity 的 BCrypt 哈希；明文码只出现在进程内事件中， 由邮件/SMS 等 adapter 订阅下发。校验成功默认不自动消费挑战，改密成功后须调用 {@link #invalidate} 或 {@link #expirePasswordResetCode} 作废，避免验证码被重复使用。

### 方法

#### `issue(OtpIssueCommand command) → String`
- **说明：** 写入 OtpChallengeEntity 的哈希方案标识（与密码 BCrypt 共用工具，语义独立）。 / private static final String CODE_HASH_ALGORITHM = "otp-bcrypt@v1"; private final OtpChallengeDao challengeDao; private final OtpPolicy policy; /** 使用 OtpPolicy。
- **参数：**
  - `challengeDao` — 挑战表 Mapper
  - `policy` — 码长、有效期、重发冷却、最大错误次数
  - `command` — 安全域、用途、通道、原始地址与模板语言
- **返回：** 新插入行的 challengeId（与事件中一致）

#### `issueCaptchaCode(OtpIssueCommand command, String rawCode) → String`
- **说明：** 持久化外部生成的验证码（图形 captcha）；不发布发送事件。
- **参数：**
  - `command` — 须使用 com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel
  - `rawCode` — 明文验证码（图形码在入库前会规范化大小写）
- **返回：** 新挑战 ID

#### `findChallengeExpiresAt(String challengeId) → LocalDateTime`
- **说明：** 查询挑战过期时间，供图形验证码 API 返回。
- **参数：**
  - `challengeId` — 挑战主键
- **返回：** 过期时间

#### `verify(OtpVerifyCommand command) → boolean`
- **说明：** 校验用户提交的验证码；任何业务失败均吞掉异常并返回 false。 调用场景：需要布尔结果的门面；明确错误码时请用 {@link #verifyOrThrow}。
- **参数：**
  - `command` — 须与发放时相同的 realm、purpose、channel 与地址（规范化规则一致）
- **返回：** 校验通过时为 true

#### `verifyOrThrow(OtpVerifyCommand command) → void`
- **说明：** 校验验证码；失败抛出对应 OtpStatusCode。 调用场景：REST 或需要向客户端返回精确错误码的路径。 码错误时递增 OtpChallengeEntity，达到 OtpChallengeEntity 后作废挑战；校验成功不修改 consumedAt。
- **参数：**
  - `command` — 校验命令

#### `invalidate(SecurityRealm securityRealm, OtpPurpose purpose, OtpChannel channel, String destination) → void`
- **说明：** 将当前「未消费」的有效挑战标记为已使用，不再校验码本身。 调用场景：业务已成功完成（如密码已写入）后防止 OTP 被二次使用。
- **参数：**
  - `securityRealm` — 与发放一致的安全域
  - `purpose` — 与发放一致的用途
  - `channel` — 与发放一致的通道
  - `destination` — 原始或规范化前的地址（内部会按通道规范化）

#### `sendPasswordResetCode(SecurityRealm realm, String identity, VerificationType type, String locale) → void`
- **说明：** 向邮箱或手机发送「忘记密码」验证码。 调用场景：OtpPasswordVerificationOperator。
- **参数：**
  - `realm` — 通常为 SecurityRealm
  - `identity` — 邮箱或手机号
  - `type` — VerificationType 或 VerificationType
  - `locale` — 消息模板语言，可为 null

#### `verifyPasswordResetCode(SecurityRealm realm,
            String identity,
            VerificationType type,
            String code) → boolean`
- **说明：** 校验忘记密码场景下的验证码。 调用场景：portal 写新密码前的 OTP 校验。
- **参数：**
  - `realm` — 安全域
  - `identity` — 与发送时相同的邮箱或手机
  - `type` — 通道类型
  - `code` — 用户输入
- **返回：** 有效且未过期、未作废时为 true

#### `expirePasswordResetCode(SecurityRealm realm, String identity, VerificationType type) → void`
- **说明：** 作废当前忘记密码挑战。 调用场景：密码重置成功写入后，OtpPasswordVerificationOperator。
- **参数：**
  - `realm` — 安全域
  - `identity` — 邮箱或手机
  - `type` — 通道类型


## OtpPasswordVerificationOperator

**类型：** class

将 PasswordVerificationOperator 映射到 OTP 子域，且用途固定为 com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose。 portal 用户改密流程只依赖该端口，无需直接引用 OtpChallengeService。

### 方法

#### `sendVerificationCode(String identity, VerificationType type) → void`
- **说明：** 租户域下的忘记密码 OTP（默认 SecurityRealm）。
- **参数：**
  - `otpChallengeService` — OTP 挑战服务
  - `securityRealm` — 平台或租户安全域

#### `verifyVerificationCode(String identity, VerificationType type, String code) → boolean`
- **说明：** {@inheritDoc} 调用场景：提交新密码前校验邮箱/短信验证码。 / public boolean verifyVerificationCode(String identity, VerificationType type, String code)

#### `expireVerificationCode(String identity, VerificationType type) → void`
- **说明：** {@inheritDoc} 调用场景：新密码写入成功后作废 OTP，防止重复改密。 / public void expireVerificationCode(String identity, VerificationType type)
