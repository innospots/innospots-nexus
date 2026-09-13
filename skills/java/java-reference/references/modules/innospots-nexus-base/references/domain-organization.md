# 包 `domain.organization`

## OrganizationSnapshot

**Type:** record

面向业务的租户档案（区域设置、货币、品牌）。**不是**内核
`OrganizationUnit` / `nx_organization_unit`。

| 组件 | 类型 | 说明 |
|-----------|------|-------------|
| `tenantId` | `String` | 所属租户 |
| `organizationCode` | `String` | 稳定的档案编码 |
| `organizationName` | `String` | 显示名称 |
| `defaultLocale` | `String` | 默认区域设置 |
| `defaultCurrency` | `String` | 默认货币 |
| `logoKey` | `String` | 品牌资源键 |
| `status` | `BasicStatus` | 档案可用状态 |

通过 `SessionContext.bindTenant` 与 `TenantSnapshot` 绑定。
