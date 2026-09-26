# 包 `domain.organization`

## OrganizationSnapshot

**类型：** record

租户面向业务的档案（语言环境、货币、品牌标识）。 这不是 nx_organization_unit；内部组织树保留在 portal 中。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenantId` | `String` | 租户 ID |
| `organizationCode` | `String` | 组织编码 |
| `organizationName` | `String` | 组织名称 |
| `defaultLocale` | `String` | 默认语言环境 |
| `defaultCurrency` | `String` | 默认货币 |
| `logoKey` | `String` | Logo 资源键 |
| `status` | `BasicStatus` | 状态 |
