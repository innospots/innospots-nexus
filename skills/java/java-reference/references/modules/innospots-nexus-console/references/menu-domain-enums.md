# 包 `menu.domain.enums`

## MenuOpenMode

**类型：** enum

打开菜单目标时使用的浏览器目标。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `INTERNAL` | 在应用框架内打开。 |
| `NEW_WINDOW` | 在新浏览器窗口或标签页中打开。 |

## MenuType

**类型：** enum

结构与导航菜单节点类型。 授权动作与 API 资源有意归属 权限领域而非本枚举。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `DIRECTORY` | 分组子菜单节点且无导航目标。 |
| `PAGE` | 渲染内部应用页面。 |
| `EXTERNAL_LINK` | 打开外部 URL。 |
