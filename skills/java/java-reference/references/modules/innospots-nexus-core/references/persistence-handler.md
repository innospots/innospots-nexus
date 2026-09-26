# 包 `persistence.handler`

## AuditMetaObjectHandler

**类型：** class

MyBatis-Plus 元对象处理器，为继承 BaseEntity 或 portal 租户域基类的实体自动填充审计字段。 从 com.innospots.nexus.base.thread.TLC 读取用户身份、租户 ID、 工作区 ID 与项目 ID，仓储层无需显式赋值。

### 方法

#### `insertFill(MetaObject metaObject) → void`
- **说明：** 插入时填充 createdAt、updatedAt、createdBy、updatedBy； TLC 中存在时同时填充 tenantId、workspaceId、projectId。
- **参数：**
  - `metaObject` — 元对象

#### `updateFill(MetaObject metaObject) → void`
- **说明：** 更新时填充 updatedAt、updatedBy；作用域列在插入后不可变。
- **参数：**
  - `metaObject` — 元对象
