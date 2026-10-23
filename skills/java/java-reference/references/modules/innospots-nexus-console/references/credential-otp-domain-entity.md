# 包 `credential.otp.domain.entity`

## OtpChallengeEntity

**类型：** class

短生命周期 OTP 挑战行（表 {@value #TABLE_NAME}）；库内仅存 codeVerifier 哈希，明文码仅经 com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent 投递。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private LocalDateTime` | `* 消费时间；非 null 表示挑战已作废或已成功收尾。` | 消费时间；非 null 表示挑战已作废或已成功收尾。 / private LocalDateTime consumedAt; |

### 方法

#### `idPrefix() → String`
- **说明：** 返回 OTP 挑战实体的稳定主键前缀。
- **返回：** 固定值 `OCH`
