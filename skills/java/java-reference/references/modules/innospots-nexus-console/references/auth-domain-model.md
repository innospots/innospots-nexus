# 包 `auth.domain.model`

## AuthUser

**类型：** record

用户目录端口返回的域中立用户身份。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `userId` | `String` | platform_user_id 或 tenant_user_id |
| `loginName` | `String` | 匹配到的登录名、邮箱或手机号 |
| `status` | `String` | 生命周期状态 name |
| `realm` | `SecurityRealm` | 所属安全域 |


## CredentialRecord

**类型：** record

域用户的鉴权凭据快照（opaque verifier，由 algorithm 解释）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `subjectId` | `String` | 用户主体 ID（platform_user_id 或 tenant_user_id） |
| `credentialKind` | `String` | 凭据类型 name（如 PASSWORD） |
| `algorithm` | `String` | 可插拔算法 ID |
| `verifier` | `String` | 不透明验证材料 |
| `verifierParams` | `String` | 算法私有参数（console 不解析） |
| `credentialVersion` | `Integer` | 凭据版本 |
| `failedAttempts` | `Integer` | 连续失败次数 |
| `lockedUntil` | `LocalDateTime` | 锁定截止时间 |
| `forceReset` | `Boolean` | 下次登录是否必须改密 |
| `expiredAt` | `LocalDateTime` | 过期时间 |


## TokenClaims

**类型：** record

加密进访问或刷新令牌的紧凑声明。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `realm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `purpose` | `String` | ACCESS 或 REFRESH |
| `tokenType` | `String` | IDENTITY 或 BUSINESS |
| `userId` | `String` | platform or tenant user 标识符 |
| `tenantId` | `String` | BUSINESS 令牌上的租户 |
| `tenantMemberId` | `String` | BUSINESS 令牌上的租户成员 |
| `workspaceId` | `String` | 作用域 BUSINESS 令牌上的活跃工作区 |
| `projectId` | `String` | 作用域 BUSINESS 令牌上的活跃项目 |
| `expiresAt` | `long` | epoch 秒级过期时间 |
