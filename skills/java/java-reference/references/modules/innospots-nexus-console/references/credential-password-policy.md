# 包 `credential.password.policy`

## LoginLockPolicy

**类型：** record

登录密码失败后的锁定策略。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `maxFailedAttempts` | `int` | 连续失败达到该次数后写入 lockedUntil |
| `lockDurationMinutes` | `int` | 锁定时长（分钟） |
