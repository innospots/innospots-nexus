# Persistence（`core.persistence`）

共享 JPA + MyBatis-Plus 实体基类、归属列、审计自动填充和主键生成。

## 实体继承（core）

```text
BaseEntity                    审计字段 + idPrefix()
  └─ OwnershipEntity          ownerType / ownerId / securityRealm（console 归属资源）
```

Portal 租户域实体链（`TenantBaseEntity` → `WorkspaceBaseEntity` → `ProjectBaseEntity`）
归属 **innospots-nexus-portal**，不在 core 模块内。

| 类 | 作用 | 备注 |
|------|------|------|
| `BaseEntity` | 平台 / 全局审计 | `createdAt`、`updatedBy` 等由 handler 填充 |
| `OwnershipEntity` | Console 归属列 | 配合 `OwnershipScope` 做查询隔离 |

## 归属作用域（`persistence.scope`）

| 类 | 说明 |
|------|------|
| `OwnerType` | `PLATFORM`、`TENANT`、`WORKSPACE` |
| `PersistenceOwnership` | 不可变归属快照（类型、ID、安全域） |
| `OwnershipScope` | `captureFromSession`、`stamp`、`apply`、`assertOwnership` |

## AuditMetaObjectHandler

**类型：** class（`MetaObjectHandler`）

插入/更新时从 `TLC` 填充审计字段；当实体存在对应属性时填充 `tenantId` / `workspaceId` / `projectId`。

## DbPrimaryGenerator

**类型：** class（`IdentifierGenerator`）

| 方法 | 行为 |
|--------|----------|
| `nextUUID(entity)` | 当实体为 `BaseEntity` 时，使用 `IdGenerator.ulid(entity.idPrefix())` |
| `nextId(entity)` | 通过 `IdGenerator.next()` 生成 Snowflake 数字 ID |

Operator **不得**手动分配主键。

详细 API 见 [`persistence-entity.md`](persistence-entity.md)、[`persistence-scope.md`](persistence-scope.md)、
[`persistence-handler.md`](persistence-handler.md)、[`persistence-id.md`](persistence-id.md)。
