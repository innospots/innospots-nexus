# 包 `credential.otp.policy`

## OtpPolicy

**类型：** record

OTP 发放与校验的可配置策略（不可变 record）。 生产默认见 {@link #DEFAULT}；单测可构造更短 TTL/零冷却以加速用例。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `codeLength` | `int` | 数字验证码位数（4–10） |
| `ttl` | `Duration` | 自创建起挑战有效时长 |
| `resendCooldown` | `Duration` | 同一投递键两次 issue 的最小间隔 |
| `maxVerifyAttempts` | `int` | 单挑战允许连续错误校验次数，用尽后挑战作废 |
