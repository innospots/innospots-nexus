# 包 `credential.otp.status`

## OtpStatusCode

**类型：** enum

OTP 子域业务状态码（模块标识 OTP，完整码形如 OTP-0001）。 由 com.innospots.nexus.console.credential.otp.service.OtpChallengeService 在挑战生命周期各阶段抛出。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `CHALLENGE_NOT_FOUND` | — |
| `CHALLENGE_EXPIRED` | — |
| `CHALLENGE_CONSUMED` | — |
| `CODE_INVALID` | — |
