# 包 `menu.domain.request`

## MenuCreateRequest

**类型：** record

创建菜单节点所需的数据。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `parentId` | `String` | 可选 parent 菜单标识符 |
| `menuKey` | `String` | 项目内唯一的稳定菜单键 |
| `menuName` | `String` | 显示名称 |
| `menuType` | `MenuType` | 菜单节点类型 |
| `routePath` | `String` | 内部导航路径 |
| `componentKey` | `String` | 逻辑前端组件标识符 |
| `redirectPath` | `String` | 可选 redirect path |
| `externalUrl` | `String` | 外部目标地址 for link menus |
| `icon` | `String` | 可选 icon 标识符 |
| `openMode` | `MenuOpenMode` | 浏览器打开模式 |
| `visible` | `Boolean` | 节点是否在导航中可见 |
| `sortOrder` | `Integer` | sibling 显示顺序 |

## MenuOrderRequest

**类型：** record

同一父节点下的有序同级菜单标识符。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `parentId` | `String` | 可选 parent 菜单标识符 for root menus |
| `menuIds` | `List<String>` | menu 标识符s in target 显示顺序 |

### 构造方法

#### `MenuOrderRequest()`

## MenuStatusUpdateRequest

**类型：** record

菜单生命周期状态更新。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `status` | `BasicStatus` | target 生命周期状态 |

## MenuTreeRequest

**类型：** record

由查询参数绑定的管理菜单树过滤器。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 菜单名称或键的模糊匹配 |
| `menuType` | `MenuType` | 可选 menu 字典类型 |
| `status` | `BasicStatus` | 可选 生命周期状态 |
| `visible` | `Boolean` | 可选 navigation visibility |

## MenuUpdateRequest

**类型：** record

现有菜单节点的可变档案数据。 稳定的菜单键刻意排除在更新之外。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `parentId` | `String` | 可选 parent 菜单标识符 |
| `menuName` | `String` | 显示名称 |
| `menuType` | `MenuType` | 菜单节点类型 |
| `routePath` | `String` | 内部导航路径 |
| `componentKey` | `String` | 逻辑前端组件标识符 |
| `redirectPath` | `String` | 可选 redirect path |
| `externalUrl` | `String` | 外部目标地址 for link menus |
| `icon` | `String` | 可选 icon 标识符 |
| `openMode` | `MenuOpenMode` | 浏览器打开模式 |
| `visible` | `Boolean` | 节点是否在导航中可见 |
| `sortOrder` | `Integer` | sibling 显示顺序 |
