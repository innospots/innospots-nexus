# 包 `credential.totp.adapter`

## DefaultMfaAuthenticator

**类型：** class

从 nx_user_credential 加载已激活 TOTP 行并校验动态码。

### 方法

#### `verifyTotp(SecurityRealm realm, String subjectId, String code) → boolean`
- **说明：** /
- **参数：**
  - `credentialOperator` — 加载 TOTP 凭据行
  - `secretProtector` — 解密 verifier 中的共享密钥
