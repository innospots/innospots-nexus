# 包 `domain.scope`

## TenantScope

**类型：** record

租户身份及其业务组织档案（console 作用域端口与 portal 实现共享）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenant` | `TenantSnapshot` | 当前租户快照 |
| `organization` | `OrganizationSnapshot` | 租户业务组织档案 |
