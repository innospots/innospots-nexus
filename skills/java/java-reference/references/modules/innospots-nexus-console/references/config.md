# 包 `config`

## ConsoleConstant

**类型：** class（final，常量容器）

控制台模块共享常量。

### 常量

| 常量 | 说明 |
|------|------|
| `API_PREFIX = "/api/d/nexus"` | 控制台管理 REST API 根路径前缀 |
| `PUBLIC_API_PREFIX = "/api/public"` | 公共开放 REST API 根路径前缀（无需鉴权）；运营域、租户域等业务域路径不在此定义，由各业务模块自行维护 |

### 方法

#### `apiPath(String suffix) → String`（static）
- **说明：** 拼接 `API_PREFIX` 下的子路径
- **参数：**
  - `suffix` — 以 `/` 开头的相对路径；空串表示仅前缀
- **返回：** 完整 JAX-RS 路径

#### `publicPath(String suffix) → String`（static）
- **说明：** 拼接 `PUBLIC_API_PREFIX` 下的子路径
- **参数：**
  - `suffix` — 以 `/` 开头的相对路径；空串表示仅前缀
- **返回：** 完整 JAX-RS 路径

## AuthConfig

**类型：** class

控制台认证的令牌签发设置。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `tokenSecret` | `String` | 紧凑令牌 AES 密钥 |
| `accessTokenTtlSeconds` | `long` | 访问令牌有效期（秒） |
| `refreshTokenTtlSeconds` | `long` | 刷新令牌有效期（秒） |
| `loginCaptchaEnabled` | `boolean` | 登录是否要求图形验证码 |
| `rsa` | `AuthConfig.Rsa` | 登录密码 RSA 公钥/私钥 |

## AuthConfigRsaMapper

**类型：** class（final，静态工具）

将外部公钥/私钥配置映射为 `AuthConfig.Rsa`；缺少公钥时会从私钥推导。

### 方法

#### `map(String publicKeyBase64, String privateKeyBase64) → AuthConfig.Rsa`（static）
- **说明：** 规范化空白并构造 RSA 配置

## RegistrationConfig

**类型：** class

自助注册模式、OTP 校验方式与邮件/短信投递参数配置。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `mode` | `RegistrationMode` | 当前启用的注册模式 |
| `otpVerification` | `RegistrationOtpVerification` | 注册 OTP 校验方式 |
| `otp` | `RegistrationConfig.OtpDelivery` | OTP 联调与投递配置 |

### 方法

#### `allows(RegistrationMode flow) → boolean`
- **说明：** 当前模式是否允许指定自助注册流程

#### `isOtpRequired() → boolean`
- **说明：** 注册是否要求 OTP

## RegistrationMode

**类型：** enum

| 常量 | 说明 |
|------|------|
| `CLOSED` | 关闭所有公开注册 |
| `OPEN` | 开放注册 |
| `INVITE` | 邀请码注册 |
| `APPROVAL` | 待审核注册 |

## RegistrationOtpVerification

**类型：** enum

| 常量 | 说明 |
|------|------|
| `NONE` | 不校验 OTP |
| `EMAIL` | 邮箱 OTP |
| `MOBILE` | 手机 OTP |

## RsaKeyMaterial

**类型：** class（final，静态工具）

RSA 密钥材料解析。

### 方法

#### `derivePublicKeyBase64(String privateKeyBase64) → String`（static）
- **说明：** 从 PKCS#8 私钥推导 X.509 SPKI 公钥 Base64
- **异常：** `NexusException`（`CRYPTO_FAILED`）— 私钥为空或无法解析时
