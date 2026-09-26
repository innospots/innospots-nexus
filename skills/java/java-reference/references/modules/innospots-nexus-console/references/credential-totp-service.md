# 包 `credential.totp.service`

## DefaultTotpEnrollmentService

**类型：** class

默认 TOTP 注册：密钥经 com.innospots.nexus.console.credential.totp.TotpSecretProtector 加密后写入 com.innospots.nexus.console.credential.password.CredentialKind 行，待确认状态见 com.innospots.nexus.console.credential.totp.TotpCredentials。


## TotpEnrollmentService

**类型：** interface

TOTP 注册两阶段：{@link #beginEnrollment} 返回扫码材料，{@link #confirmEnrollment} 校验首码后激活行。


## TotpVerificationSupport

**类型：** class

TOTP 动态码校验（注册确认与登录 MFA 共用）；失败抛出 com.innospots.nexus.console.credential.totp.status.TotpStatusCode。

### 方法

#### `verifyCode(CredentialRecord record, String code) → void`
- **说明：** /
- **参数：**
  - `secretProtector` — 解密库中 verifier 并得到 HMAC 密钥字节
  - `record` — 含加密共享密钥的 TOTP 凭据行
  - `code` — 用户输入

#### `requireActiveTotp(CredentialRecord record) → CredentialRecord`
- **说明：** 要求凭据已激活（非待确认）。 调用场景：登录 MFA 前断言用户已完成 TotpEnrollmentService。
- **参数：**
  - `record` — 凭据记录，可为 null
- **返回：** 非 null 且已激活的记录

#### `requirePendingTotp(CredentialRecord record) → CredentialRecord`
- **说明：** 要求凭据处于注册待确认状态。 调用场景：DefaultTotpEnrollmentService 仅接受 pending 行。
- **参数：**
  - `record` — 凭据记录，可为 null
- **返回：** 非 null 且待确认的记录
