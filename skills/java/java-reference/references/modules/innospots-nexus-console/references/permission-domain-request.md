# 包 `permission.domain.request`

## `PermissionGrantItemRequest`

**类型：** record

角色或组织单元权限全量替换请求中的一条资源授权。

### 组件

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceKey` | `String` | 被授权的 catalog 资源键 |
| `constraintDefinition` | `String` | 旧版 datasource 授权附加查询条件；catalog v2 要求为空 |

`PermissionGrantService.replace(...)` 校验 `resourceKey` 必须指向当前可见的
MODULE 或 MENU，并拒绝非空 `constraintDefinition`。

## `PermissionGrantReplaceRequest`

**类型：** record

一个角色或组织单元最终应拥有的完整资源授权集合。

### 组件

| 名称 | 类型 | 说明 |
|------|------|------|
| `grants` | `List<PermissionGrantItemRequest>` | 前端提交的完整授权集合；空集合表示清空该主体的授权 |

### 规范化

紧凑构造器将 `null` 转换为 `List.of()`，并通过 `List.copyOf(...)` 复制非空集合，
避免调用方后续修改请求内容。
