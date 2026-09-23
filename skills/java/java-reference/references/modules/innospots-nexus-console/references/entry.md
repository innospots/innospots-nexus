# 包 `entry`

## ConsoleModuleDescriptor

**类型：** record

单个内置控制台模块 entry 插件的不可变元数据。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 反向域名插件标识 |
| `moduleKey` | `String` | 控制台模块键与 PageDsl 目录名 |
| `pageKey` | `String` | PageDsl 页面键，通常为 {@code {moduleKey}-main} |
| `pagePath` | `String` | 前端路由路径 |
| `menuKey` | `String` | 模块内菜单 entry 键 |
| `menuIcon` | `String` | 可选 menu icon |
| `orderIndex` | `int` | 同级菜单排序 |
| `displayName` | `I18nObject` | module 显示名称 |
| `description` | `I18nObject` | module 描述 |
| `pageTitle` | `I18nObject` | 菜单与页面标题 |

### 方法

#### `mainPageKey(String moduleKey) → String`

- **说明：** 返回控制台模块的主页面键。
- **参数：**
  - `moduleKey` — 控制台模块键
- **返回：** stable 页面键，例如 {@code menu-main}
