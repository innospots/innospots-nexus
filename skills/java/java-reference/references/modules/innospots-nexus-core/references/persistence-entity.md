# 包 `persistence.entity`

## BaseEntity

**类型：** class

带自动审计字段的 JPA/MyBatis-Plus 基类实体。 所有字段由 AuditMetaObjectHandler 通过 MyBatis-Plus 元对象填充自动写入， 无需在仓储层手动赋值。

### 方法

#### `idPrefix() → String`
- **说明：** 返回生成本实体主键时使用的前缀。
- **返回：** 主键前缀；无需前缀时返回空字符串


## OwnershipEntity

**类型：** class

控制台业务归属列：通过 ownerType / ownerId / securityRealm 表达可见性与存储分区；查询与写入见 com.innospots.nexus.core.persistence.scope.OwnershipScope。
