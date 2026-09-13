# 管理域：菜单、角色、字典

控制台拥有工作空间级管理数据的 **REST 契约**、**实体**和 **DAO**。
**业务工作流**（service/operator 实现）归属 **innospots-nexus-kernel**，
除非明确仅限 platform。

## Role（`console.role`）

### 持久化

| 表 | 实体 | 说明 |
|-------|--------|-------|
| `nx_role` | `RoleEntity` | `owner_type` + `owner_id` + `role_code` 唯一 |
| `nx_role_binding` | `RoleBindingEntity` | USER 或 ORG_UNIT → role |

### 端点

- `RoleEndpoint` — CRUD + 状态 + 选项（**interface** → kernel 实现）
- `RoleBindingEndpoint` — 分页/添加/删除绑定（**class stub**）
- `GrantManagementEndpoint` — 权限替换（见 catalog-permission.md）

### 请求/VO record

`RoleCreateRequest`、`RoleUpdateRequest`、`RolePageRequest`、`RoleStatusUpdateRequest`、
`RoleVo`、`RoleOptionVo`、`RoleBindingAddRequest`、`RoleBindingPageRequest`、`RoleBindingVo`。

### 入口插件

`RoleEntryPlugin` — 插件 ID `com.innospots.nexus.console.role`。

## Menu（`console.menu`）

### 持久化

`nx_menu` — 通过 `parent_id` 构成树，`menu_key` 在工作空间内唯一，`security_realm`、
`MenuType`、`MenuOpenMode`。

### 端点

- `MenuEndpoint` — 树 CRUD + 重排序 + 选项（**class stub**）
- `NavigationMenuEndpoint` — 在 console 中**已实现**（权限过滤读取）

### VO 区分

| VO | 用途 |
|----|-----|
| `MenuVo` | 管理树编辑器（完整字段） |
| `NavigationMenuVo` | 运行时侧边栏（精简，仅已授权） |
| `MenuOptionVo` | 选择控件 |

### 入口插件

`MenuEntryPlugin` — 插件 ID `com.innospots.nexus.console.menu`。

## Dictionary（`console.dictionary`）

### 持久化

| 表 | 实体 |
|-------|--------|
| `nx_dictionary_type` | `DictionaryTypeEntity` |
| `nx_dictionary_item` | `DictionaryItemEntity` |

类型按工作空间的 `type_code` 键控；项按 `type_code` + `item_value` 键控。

### 端点

- `DictionaryTypeEndpoint` — 类型 CRUD + 选项（**stub**）
- `DictionaryItemEndpoint` — `{typeCode}` 下的项（**stub**）

### 入口插件

`DictionaryEntryPlugin` — 插件 ID `com.innospots.nexus.console.dictionary`。

## 实现状态（快照 1.0.0）

| 领域 | 契约 | console 中的 service/operator |
|------|----------|----------------------------|
| Role REST | 完整接口 | 无 — kernel |
| Role binding | Stub 类 | Kernel TODO |
| Menu management | Stub 类 | Kernel TODO |
| Menu navigation | 已实现 | `NavigationMenuAssembler` |
| Dictionary | Stub 类 | Kernel TODO |

在 kernel 中实现时：

1. 将实现接口端点作为 JAX-RS Bean，委托给 `*Service`。
2. 遵循 `endpoint → service → operator → dao`，使用 `WorkspaceBaseEntity` TLC 填充。
3. 不要重复 VO — 复用 console records。
