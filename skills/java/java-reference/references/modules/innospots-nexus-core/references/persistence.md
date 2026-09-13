# Persistence（`core.persistence`）

共享 JPA + MyBatis-Plus 实体基类、审计自动填充和主键生成。

## 实体继承

```text
BaseEntity
  └─ TenantBaseEntity        tenantId
       └─ WorkspaceBaseEntity  workspaceId   ← 业务实体的默认基类
            └─ ProjectBaseEntity  projectId   ← 仅经设计批准时使用
```

| 类 | 作用域 | 插入/更新时 TLC 填充的键 |
|-------|-------|----------------------------------|
| `BaseEntity` | 平台 / 领域全局 | 仅审计字段 |
| `TenantBaseEntity` | 租户 | `tenantId` |
| `WorkspaceBaseEntity` | 租户 + 工作空间 | `tenantId`、`workspaceId` |
| `ProjectBaseEntity` | + 项目隔离 | + `projectId` |

### BaseEntity

**类型：** `@MappedSuperclass` class

- `idPrefix()` — 覆盖 ULID 前缀（通过 `DbPrimaryGenerator`）
- `createdAt`、`updatedAt`、`createdBy`、`updatedBy` — 不得在 operator 中手动设置

具体实体还必须遵循 `skills/java/java-reference/standards/api-design.md`（表名常量、索引、`@TableId(ASSIGN_UUID)` 等）。

## AuditMetaObjectHandler

**类型：** class（`MetaObjectHandler`）

在插入/更新时从 `TLC` 填充审计字段和作用域列：

- `createdBy` / `updatedBy` ← `TLC.userName()`
- 当实体继承对应基类时，填充 `tenantId` / `workspaceId` / `projectId`

在宿主应用中注册为 MyBatis-Plus 元对象处理器。

## DbPrimaryGenerator

**类型：** class（`IdentifierGenerator`）

| 方法 | 行为 |
|--------|----------|
| `nextUUID(entity)` | 当实体为 `BaseEntity` 时，使用 `IdGenerator.ulid(entity.idPrefix())` |
| `nextId(entity)` | 通过 `IdGenerator.next()` 生成 Snowflake 数字 ID |

Operator **不得**手动分配主键。
