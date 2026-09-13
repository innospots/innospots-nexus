# 包 `domain.tenant`

## TenantSnapshot

**Type:** record

平台租户（`nx_tenant`）的会话/传输快照。与租户业务档案
（`OrganizationSnapshot`）一一对应。

| 组件 | 类型 | 说明 |
|-----------|------|-------------|
| `tenantId` | `String` | 租户标识 |
| `tenantCode` | `String` | 稳定的业务编码 |
| `tenantName` | `String` | 显示名称 |
| `status` | `BasicStatus` | 租户可用状态 |

非内核领域实体。
