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

域用户的密码凭证快照。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `userId` | `String` | owner 标识符 |
| `passwordHash` | `String` | 存储的哈希值 |
| `passwordSalt` | `String` | 哈希盐值 |
| `passwordAlgorithm` | `String` | 算法名称 |
| `failedAttempts` | `Integer` | 连续失败次数 |
| `lockedUntil` | `LocalDateTime` | 锁定过期时间 |
| `forceReset` | `Boolean` | 下次登录是否必须修改密码 |

## TenantMembership

**类型：** record

租户域身份认证后使用的活跃租户成员关系。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | tenant 标识符 |
| `tenantMemberId` | `String` | tenant member 标识符 |

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
