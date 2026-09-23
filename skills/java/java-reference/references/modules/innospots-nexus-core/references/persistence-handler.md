# 包 `persistence.handler`

## AuditMetaObjectHandler

**类型：** class

MyBatis-Plus 元对象处理器，为继承 {@link BaseEntity}、{@link TenantBaseEntity}、 {@link WorkspaceBaseEntity} 或 {@link ProjectBaseEntity} 的实体自动填充审计字段。 工作区 ID 与项目 ID，仓储层无需显式赋值。</p>

### 方法

#### `insertFill(MetaObject metaObject) → void`

- **说明：** 插入时填充 createdAt、updatedAt、createdBy、updatedBy； TLC 中存在时同时填充 tenantId、workspaceId、projectId。
- **参数：**
  - `metaObject` — 元对象

#### `updateFill(MetaObject metaObject) → void`

- **说明：** 更新时填充 updatedAt、updatedBy； TLC 中存在时刷新 tenantId、workspaceId、projectId。
- **参数：**
  - `metaObject` — 元对象
