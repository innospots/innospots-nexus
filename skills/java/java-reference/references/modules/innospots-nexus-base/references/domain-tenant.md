# 包 `domain.tenant`

## TenantSnapshot

**类型：** record

平台租户（{@code nx_tenant}）的会话/传输快照。 与租户企业档案（{@link com.innospots.nexus.base.domain.organization.OrganizationSnapshot}）一一对应。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | 租户 ID |
| `tenantCode` | `String` | 租户编码 |
| `tenantName` | `String` | 租户名称 |
| `status` | `BasicStatus` | 状态 |
