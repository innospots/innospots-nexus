# 包 `catalog.domain.enums`

## `CatalogResourceType`

**类型：** enum

Console 插件目录索引暴露的资源类型。

| 常量 | 说明 |
|------|------|
| `MODULE` | 顶层或嵌套管理模块；叶子 MODULE 直接承载 `entryPageKey` |
| `MENU` | MODULE 下一级菜单入口；当前 catalog 只持久化这两类节点 |
