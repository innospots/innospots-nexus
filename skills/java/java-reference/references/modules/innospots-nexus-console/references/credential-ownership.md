# 包 `credential.ownership`

## CredentialOwnershipResolver

**类型：** class

将 com.innospots.nexus.console.auth.domain.enums.SecurityRealm 映射为凭据/OTP 行的 com.innospots.nexus.console.scope.ConsoleOwnership（身份级 TENANT/PLATFORM 时 ownerId 可为 null）。

### 方法

#### `ownershipForRealm(SecurityRealm realm) → ConsoleOwnership`
- **说明：** 平台或租户身份域的默认归属（身份级；ownerId 在尚无租户上下文时为 null）。 调用场景：TOTP 注册、租户级密码凭据写入 com.innospots.nexus.console.credential.password.operator.UserCredentialOperator 时。
- **参数：**
  - `realm` — 平台或租户安全域
- **返回：** 对应 ConsoleOwnership，身份级时 ownerId 为 null

#### `tenantResourceOwnership(String tenantId) → ConsoleOwnership`
- **说明：** 租户业务资源下的凭据归属（例如按租户隔离的扩展凭据）。 调用场景：工作区/租户资源级凭据扩展，需显式 tenantId 隔离时。
- **参数：**
  - `tenantId` — 租户主键
- **返回：** 带 ownerId 的租户归属
