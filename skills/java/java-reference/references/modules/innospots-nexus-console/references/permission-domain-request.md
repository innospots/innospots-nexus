# 包 `permission.domain.request`

## PermissionGrantItemRequest

**类型：** record

角色或组织单元权限全量替换请求中的一条资源授权。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `String` | 被授权的资源主键。 |
| `constraintDefinition` | `String` | datasource 授权对应的管理端附加查询条件，可为空。 |

## PermissionGrantReplaceRequest

**类型：** record

一个角色或组织单元最终应拥有的完整资源授权集合。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `grants` | `List<PermissionGrantItemRequest>` | 前端提交的完整授权集合；空集合表示清空该主体的授权。 |

### 构造方法

#### `PermissionGrantReplaceRequest()`

- **说明：** 将空请求规范化为空集合，并复制集合避免调用方后续修改请求内容。
