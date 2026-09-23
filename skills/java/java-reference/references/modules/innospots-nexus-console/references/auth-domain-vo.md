# 包 `auth.domain.vo`

## AuthTokenVo

**类型：** record

登录、注册、刷新或租户选择后返回的令牌对。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `realm` | `SecurityRealm` | PLATFORM 或 TENANT |
| `tokenType` | `String` | IDENTITY 或 BUSINESS |
| `accessToken` | `String` | 访问令牌 |
| `refreshToken` | `String` | 刷新令牌 |
| `tenantId` | `String` | 在 TENANT 业务令牌上设置 |
| `tenantMemberId` | `String` | 在 TENANT 业务令牌上设置 |
| `workspaceId` | `String` | 工作区活跃时设置 |
| `projectId` | `String` | 项目活跃时设置 |
