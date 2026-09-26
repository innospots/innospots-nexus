# 包 `menu.domain.vo`

## MenuOptionVo

**类型：** record

用于父级选择器与树形控件菜单精简选项。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `menuId` | `String` | 菜单标识符 |
| `parentId` | `String` | 可选 parent 菜单标识符 |
| `menuName` | `String` | 显示名称 |
| `menuType` | `MenuType` | 菜单节点类型 |
| `disabled` | `Boolean` | 选项是否不可选择 |


## MenuVo

**类型：** record

管理控制台菜单详情及嵌套子节点。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `menuId` | `String` | 菜单标识符 |
| `parentId` | `String` | 可选 parent 菜单标识符 |
| `menuKey` | `String` | 项目内唯一的稳定菜单键 |
| `menuName` | `String` | 显示名称 |
| `menuType` | `MenuType` | 菜单节点类型 |
| `routePath` | `String` | 内部导航路径 |
| `componentKey` | `String` | 逻辑前端组件标识符 |
| `redirectPath` | `String` | 可选 redirect path |
| `externalUrl` | `String` | 外部目标地址 |
| `icon` | `String` | 可选 icon 标识符 |
| `openMode` | `MenuOpenMode` | 浏览器打开模式 |
| `visible` | `Boolean` | 节点是否在导航中可见 |
| `status` | `BasicStatus` | 生命周期状态 |
| `sortOrder` | `Integer` | sibling 显示顺序 |
| `builtIn` | `Boolean` | 节点是否由系统管理 |
| `createdAt` | `LocalDateTime` | 创建时间 |
| `updatedAt` | `LocalDateTime` | 最后更新时间 |
| `children` | `List<MenuVo>` | 嵌套子菜单 |


## NavigationMenuVo

**类型：** record

交付给管理前端的只读导航节点。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `menuKey` | `String` | 稳定的菜单键 |
| `menuName` | `String` | 显示名称 |
| `routePath` | `String` | 内部导航路径 |
| `componentKey` | `String` | 逻辑前端组件标识符 |
| `externalUrl` | `String` | 外部目标地址 |
| `icon` | `String` | 可选 icon 标识符 |
| `openMode` | `MenuOpenMode` | 浏览器打开模式 |
| `resourceId` | `String` | 权限资源 ID |
| `ownerPluginId` | `String` | 来源插件 ID |
| `moduleKey` | `String` | 控制台模块键 |
| `pageKey` | `String` | 关联页面标识 |
| `children` | `List<NavigationMenuVo>` | 嵌套可见导航节点 |
