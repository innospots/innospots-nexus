# 角色、字典与菜单域

| 域 | REST | 说明 |
|----|------|------|
| **role** | 类级路径由业务实现绑定 | `RoleEndpoint` / `RoleBindingEndpoint` 契约、`RoleOperator` / `RoleBindingOperator`、实体 |
| **dictionary** | 类级路径由业务实现绑定 | `DictionaryTypeEndpoint` 契约；字典项随类型请求体维护 |
| **menu** | 无管理 REST | `MenuEntity` 供库内能力；运行时侧栏见 **navigation** |

包级参考：`role-*.md`、`dictionary-*.md`、`menu-*.md`。
