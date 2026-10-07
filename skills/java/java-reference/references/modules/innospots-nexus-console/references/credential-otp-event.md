# 包 `credential.otp.event`

## OtpSendRequestedEvent

**类型：** record

OTP 明文码投递请求；adapter 订阅后按 {@link #channel} 与 {@link #templateId} 渲染并发送。 明文仅存在于事件载荷，禁止写入 nx_otp_challenge 或应用日志。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `challengeId` | `String` | 挑战主键，与库表 challenge_id 一致，供投递侧关联与审计 |
| `purpose` | `OtpPurpose` | 业务用途，决定消息模板族 |
| `channel` | `OtpChannel` | 下发通道，影响 {@link #eventType()} 与投递实现选择 |
| `destination` | `String` | 已规范化的投递地址（与库表 destination 一致） |
| `plaintextCode` | `String` | 一次性数字验证码明文 |
| `templateId` | `String` | 稳定模板键，形如 credential.otp.. |
| `eventType(` | `String locale
) implements DomainEvent {

    /**
     * 按通道区分的类型标识，供 {@link com.innospots.nexus.base.events.EventHandler} 过滤。
     * <p>例如邮箱为 {@code credential.otp.send.email}。</p>
     *
     * @return 小写通道后缀的事件类型名
     */
    @Override
    public String` | — |

### 方法

#### `eventType() → String`
- **说明：** 按通道区分的类型标识，供 com.innospots.nexus.base.events.EventHandler 过滤。 例如邮箱为 credential.otp.send.email。
- **返回：** 小写通道后缀的事件类型名
