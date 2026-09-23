# 包 `role.domain.entity`

## RoleBindingEntity

**类型：** class

将 USER 或 ORG_UNIT 主体绑定到角色；有效作用域随角色归属变化。

### 方法

#### `idPrefix() → String`

- **说明：** 返回主键前缀。
- **返回：** 操作结果

## RoleEntity

**类型：** class

控制台作用域内的角色持久化实体。归属层级为 PLATFORM、TENANT 或 WORKSPACE。

### 方法

#### `idPrefix() → String`

- **说明：** 返回主键前缀。
- **返回：** 操作结果
