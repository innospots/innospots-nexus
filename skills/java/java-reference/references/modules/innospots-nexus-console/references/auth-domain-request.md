# 包 `auth.domain.request`

## AuthLoginRequest

**类型：** record

两个域共用的密码登录载荷。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `login` | `String` | user_name、email 或 mobile |
| `encryptedPassword` | `String` | 前端加密密码 |

## PasswordChangeRequest

**类型：** record

已认证用户的密码修改。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `oldEncryptedPassword` | `String` | 当前前端加密密码 |
| `newEncryptedPassword` | `String` | 期望的前端加密密码 |

## PasswordResetRequest

**类型：** record

使用验证码重置密码。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `identity` | `String` | user_name、email 或 mobile |
| `verificationCode` | `String` | 一次性验证码 |
| `type` | `VerificationType` | EMAIL 或 MOBILE |
| `newEncryptedPassword` | `String` | 前端加密的新密码 |

## SelectProjectRequest

**类型：** record

在当前工作区作用域内激活项目。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | owning tenant 标识符 |
| `workspaceId` | `String` | owning workspace 标识符 |
| `projectId` | `String` | 待激活的项目 |

## SelectTenantRequest

**类型：** record

身份认证后选择当前租户。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | 待激活的租户 |

## SelectWorkspaceRequest

**类型：** record

在当前租户业务作用域内激活工作区。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | 用于校验成员关系的租户 |
| `workspaceId` | `String` | 待激活的工作区 |

## TenantRegisterRequest

**类型：** record

租户域身份注册。不创建 TenantMember。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `userName` | `String` | 唯一登录名 |
| `displayName` | `String` | 可选 显示名称 |
| `email` | `String` | 可选 email |
| `mobile` | `String` | 可选 mobile |
| `region` | `String` | 可选 region such as CN |
| `timeZone` | `String` | 可选 IANA 时区 |
| `language` | `String` | 可选 界面语言，例如 zh-CN |
| `encryptedPassword` | `String` | 前端加密密码 |

## TokenRefreshRequest

**类型：** record

刷新令牌交换。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `refreshToken` | `String` | 同域签发的刷新令牌 |
