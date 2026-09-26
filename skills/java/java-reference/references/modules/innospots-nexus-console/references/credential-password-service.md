# 包 `credential.password.service`

## CredentialService

**类型：** class

租户/平台用户密码生命周期：注册、修改、重置；校验强度后委托 UserCredentialOperator。 供 portal / platform 调用，不绑定 HTTP。

### 方法

#### `authenticate(SecurityRealm realm, String subjectId, String rawPassword) → void`
- **说明：** /
- **参数：**
  - `credentialOperator` — 凭据表操作
  - `passwordValidator` — 本地密码强度策略
  - `loginLockPolicy` — 登录失败锁定策略
  - `realm` — 安全域
  - `subjectId` — 用户主体 ID
  - `rawPassword` — 明文密码

#### `enrollPassword(SecurityRealm realm, String subjectId, String rawPassword) → void`
- **说明：** 首次为用户设置密码。 调用场景：portal/platform 用户创建或邀请激活后写入初始密码。
- **参数：**
  - `realm` — 平台或租户域
  - `subjectId` — 用户 ID
  - `rawPassword` — 明文新密码

#### `changePassword(SecurityRealm realm, String subjectId, String oldPassword, String newPassword) → void`
- **说明：** 已登录用户修改密码（需校验旧密码）。 调用场景：控制台或 portal 改密 API，在 OTP/会话已建立的前提下调用。
- **参数：**
  - `realm` — 安全域
  - `subjectId` — 用户 ID
  - `oldPassword` — 当前明文密码
  - `newPassword` — 新明文密码

#### `resetPassword(SecurityRealm realm, String subjectId, String newPassword) → void`
- **说明：** 在身份已验证（如 OTP）后强制设置新密码。 调用场景：com.innospots.nexus.portal.user.operator.PasswordOperator 忘记密码流程， 验证码通过后由 portal 调用。
- **参数：**
  - `realm` — 安全域
  - `subjectId` — 用户 ID
  - `newPassword` — 新明文密码

#### `enrollPassword(ConsoleOwnership ownership, String subjectId, String rawPassword) → void`
- **说明：** 在显式归属下注册密码（租户资源级扩展场景）。 调用场景：未来按租户 ID 隔离的凭据扩展；与 CredentialOwnershipResolver 配合。
- **参数：**
  - `ownership` — 凭据归属
  - `subjectId` — 用户 ID
  - `rawPassword` — 明文密码
