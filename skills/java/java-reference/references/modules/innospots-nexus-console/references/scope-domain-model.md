# 包 `scope.domain.model`

## TenantScope

**类型：** record

租户身份及其业务组织档案。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `tenant` | `TenantSnapshot` | 平台租户快照 |
| `organization` | `OrganizationSnapshot` | 租户面向业务的档案 |
