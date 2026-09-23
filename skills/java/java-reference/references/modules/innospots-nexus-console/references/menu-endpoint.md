# 包 `menu.endpoint`

## MenuEndpoint

**类型：** class

菜单生命周期与树维护的管理控制台端点。 方法工作流延后至菜单服务与 Operator 边界在此实现。

### 方法

#### `listMenuTree(@BeanParam MenuTreeRequest request) → R<List<MenuVo>>`

- **说明：** 使用可选过滤器列出管理菜单树。
- **参数：**
  - `request` — 树形过滤条件
- **返回：** 匹配的 菜单树

#### `createMenu(MenuCreateRequest request) → R<MenuVo>`

- **说明：** 创建菜单节点。
- **参数：**
  - `request` — 菜单创建数据
- **返回：** created 菜单

#### `reorderMenus(MenuOrderRequest request) → R<Void>`

- **说明：** 重排同级菜单节点。
- **参数：**
  - `request` — ordered sibling 菜单标识符s
- **返回：** 空成功响应

#### `listMenuOptions() → R<List<MenuOptionVo>>`

- **说明：** 列出用于父级选择控件的菜单精简选项。
- **返回：** menu 选项列表
