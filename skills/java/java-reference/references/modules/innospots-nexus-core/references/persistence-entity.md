# 包 `persistence.entity`

## BaseEntity

**类型：** class

带自动审计字段的 JPA/MyBatis-Plus 基类实体。 无需在仓储层手动赋值。</p>

### 方法

#### `idPrefix() → String`

- **说明：** 返回生成本实体主键时使用的前缀。
- **返回：** 主键前缀；无需前缀时返回空字符串

## ProjectBaseEntity

**类型：** class

工作区内项目范围基类实体，在 {@link WorkspaceBaseEntity} 基础上增加项目 ID， 审计填充时从 {@link com.innospots.nexus.base.thread.TLC#projectId()} 自动写入。

## TenantBaseEntity

**类型：** class

租户范围基类实体，在 {@link BaseEntity} 基础上增加租户 ID， 审计填充时从 {@link com.innospots.nexus.base.thread.TLC#tenantId()} 自动写入。

## WorkspaceBaseEntity

**类型：** class

工作区范围基类实体，在 {@link TenantBaseEntity} 基础上增加工作区 ID， 审计填充时从 {@link com.innospots.nexus.base.thread.TLC#workspaceId()} 自动写入。
