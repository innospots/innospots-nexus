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
- **说明：** 物理表名 nx_otp_challenge。 / public static final String TABLE_NAME = "nx_otp_challenge"; /** 挑战主键；前缀 och（见 {@link #idPrefix()}）。 / private String challengeId; /** Console 侧 ID 生成前缀，与 {@link #challengeId} 分配策略一致。
- **返回：** 固定值 och
