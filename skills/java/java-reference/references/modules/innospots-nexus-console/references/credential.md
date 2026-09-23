# 包 `credential`

## NullPasswordVerificationOperator

**类型：** class

拒绝所有验证码操作的空实现。

### 方法

#### `sendVerificationCode(String identity, VerificationType type) → void`

- **说明：** 发送验证码。
- **参数：**
  - `identity` — identity 参数
  - `type` — type 参数

#### `verifyVerificationCode(String identity, VerificationType type, String code) → boolean`

- **说明：** 校验验证码。
- **参数：**
  - `identity` — identity 参数
  - `type` — type 参数
  - `code` — code 参数
- **返回：** 操作结果

#### `expireVerificationCode(String identity, VerificationType type) → void`

- **说明：** 使验证码过期。
- **参数：**
  - `identity` — identity 参数
  - `type` — type 参数

## PasswordValidator

**类型：** class

密码强度校验器。

### 方法

#### `isValid(String password) → boolean`

- **说明：** 当密码满足全部强度要求时返回 true。
- **参数：**
  - `password` — 待校验密码
- **返回：** 有效时为 true，否则为 false

## RsaPasswordDecryptor

**类型：** record

前端加密密码的 RSA 实现。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `privateKey` | `String` | Base64 编码的 PKCS#8 私钥 |

### 构造方法

#### `RsaPasswordDecryptor()`


### 方法

#### `decrypt(String encryptedPassword) → String`

- **说明：** 解密。
- **参数：**
  - `encryptedPassword` — encryptedPassword 参数
- **返回：** 操作结果
