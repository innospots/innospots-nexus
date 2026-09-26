# 包 `credential.password`

## CredentialKind

**类型：** enum

nx_user_credential.credential_kind 取值；与 com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithm 解耦。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PASSWORD` | — |
| `TOTP` | — |


## PasswordDecryptor

**类型：** interface

解密前端客户端提交的密码密文；由 portal/platform/auth 注入，实现见 RsaPasswordDecryptor。


## PasswordValidator

**类型：** class

密码强度校验器。 要求：最小长度，且必须包含大写字母、小写字母与数字。

### 方法

#### `isValid(String password) → boolean`
- **说明：** 当密码满足全部强度要求时返回 true。
- **参数：**
  - `password` — 待校验密码
- **返回：** 有效时为 true，否则为 false


## PasswordVerificationOperator

**类型：** interface

忘记密码等场景的验证码端口；OTP 实现见 com.innospots.nexus.console.credential.otp.service.OtpPasswordVerificationOperator。


## RsaPasswordDecryptor

**类型：** record

前端 RSA 加密密码的 PasswordDecryptor 实现。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `Objects.requireNonNull(privateKey` | `String privateKey) implements PasswordDecryptor {

    public RsaPasswordDecryptor {` | — |
| `encryptedPassword` | `"privateKey must not be null");
    }

    @Override
    public String decrypt(String` | — |


## VerificationType

**类型：** enum

忘记密码等流程中验证码的投递通道（与 com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel 在 EMAIL/MOBILE 上互转）。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `EMAIL` | — |
| `MOBILE` | — |
