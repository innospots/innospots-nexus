# 包 `credential.totp.algorithm`

## TotpCredentialAlgorithm

**类型：** class

RFC 6238 TOTP（HMAC-SHA1，30 秒步长，8 位数字）；校验允许 ±1 个时间窗。

### 方法

#### `algorithmId() → String`
- **说明：** 持久化到凭据行的算法标识。
- **返回：** TotpCredentialAlgorithms

#### `generateAt(byte[] secret, Instant instant) → String`
- **说明：** 在指定时刻生成 TOTP（测试与向量校验用）。 调用场景：单元测试断言 RFC 6238 向量，非生产登录路径。
- **参数：**
  - `secret` — HMAC 密钥字节
  - `instant` — 时间锚点
- **返回：** 8 位数字动态码字符串

#### `verify(byte[] secret, String code, Instant instant) → boolean`
- **说明：** 校验动态码，允许前后各一个时间窗以容忍时钟漂移。 调用场景：com.innospots.nexus.console.credential.totp.service.TotpVerificationSupport。
- **参数：**
  - `secret` — HMAC 密钥字节
  - `code` — 用户输入
  - `instant` — 校验时刻（通常为 Instant)
- **返回：** 任一时间窗匹配时为 true


## TotpCredentialAlgorithms

**类型：** class

TOTP 算法标识。
