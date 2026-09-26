# 包 `permission.domain.request`

## PermissionGrantItemRequest

**类型：** record

角色或组织单元权限全量替换请求中的一条资源授权。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `/**
         * 被授权的资源主键。
         */
        String` | — |
| `constraintDefinition` | `/**
         * datasource 授权对应的管理端附加查询条件，可为空。
         */
        String` | — |


## PermissionGrantReplaceRequest

**类型：** record

一个角色或组织单元最终应拥有的完整资源授权集合。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `grants` | `/**
         * 前端提交的完整授权集合；空集合表示清空该主体的授权。
         */
        List<PermissionGrantItemRequest>` | — |
