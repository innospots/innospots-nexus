# 包 `credential.totp`

## MfaAuthenticator

**类型：** interface

MFA 动态码校验端口；默认实现见 com.innospots.nexus.console.credential.totp.adapter.DefaultMfaAuthenticator。


## TotpCredentials

**类型：** class

TOTP 凭据行 verifierParams 约定与判定辅助。

### 方法

#### `isEnrollmentPending(CredentialRecord record) → boolean`
- **说明：** 写入 verifierParams 表示注册尚未通过首码确认。 / public static final String ENROLLMENT_PENDING = "enrollment_pending"; private TotpCredentials() { } /** 判断 TOTP 行是否仍处于待确认注册状态。 调用场景：区分「可登录 MFA」与「仅完成扫码未确认」。
- **参数：**
  - `record` — 凭据记录，可为 null
- **返回：** verifierParams 等于 {@link #ENROLLMENT_PENDING} 时为 true


## TotpMasterKeyProvider

**类型：** interface

提供 TOTP 共享密钥静态加密用的主密钥（应用或 adapter 注入，须为 16/24/32 字节 AES 材料）。


## TotpProvisioningUriBuilder

**类型：** class

构建 otpauth:// 扫码 URI，参数与 com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithm 一致（SHA1、8 位、30 秒）。

### 方法

#### `build(String accountLabel, String base32Secret) → String`
- **说明：** 使用默认发行方名称「Nexus」构建 URI。 调用场景：com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService 返回给前端展示二维码。
- **参数：**
  - `accountLabel` — 账号展示名
  - `base32Secret` — 共享密钥
- **返回：** otpauth://totp/... URI

#### `build(String issuer, String accountLabel, String base32Secret) → String`
- **说明：** 指定发行方构建 URI。
- **参数：**
  - `issuer` — authenticator 中显示的组织名
  - `accountLabel` — 账号展示名
  - `base32Secret` — 共享密钥
- **返回：** otpauth://totp/... URI


## TotpSecretProtector

**类型：** class

TOTP 共享密钥的 Base32 生成与 AES-GCM 静态加密；主密钥由 TotpMasterKeyProvider 提供。

### 方法

#### `generateBase32Secret() → String`
- **说明：** /
- **参数：**
  - `masterKeyProvider` — 应用级 AES-GCM 主密钥来源
- **返回：** 供 authenticator 与 {@link #encryptForStorage} 使用的 Base32 字符串

#### `encryptForStorage(String base32Secret) → String`
- **说明：** 将明文共享密钥加密后写入 verifier 列。 调用场景：TOTP 注册开始时持久化密钥，库中永不存明文。
- **参数：**
  - `base32Secret` — 注册阶段生成的共享密钥
- **返回：** AES-GCM 密文字符串

#### `decryptFromStorage(String encryptedVerifier) → String`
- **说明：** 从库中 verifier 解密得到 Base32 共享密钥。 调用场景：com.innospots.nexus.console.credential.totp.service.TotpVerificationSupport。
- **参数：**
  - `encryptedVerifier` — 持久化的 AES-GCM 载荷
- **返回：** Base32 共享密钥

#### `decodeBase32Secret(String base32Secret) → byte[]`
- **说明：** 将 Base32 解码为 HMAC 所需的原始密钥字节。
- **参数：**
  - `base32Secret` — RFC 4648 Base32 字符串
- **返回：** 解码后的密钥字节
