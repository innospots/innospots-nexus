# 包 `sitemap.domain.model`

## `SitemapMenuItem`

**类型：** class（final，`@Getter @Setter`、`@JsonAutoDetect(fieldVisibility = ANY)`）

meta-sitemap 菜单节点，对应前端 `sitemap.yaml` 的 `menus[]` 形状。路由在前端 `pages[]`
声明；节点本身不定义页面 DSL、HTTP 端点或 catalog 资源。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 菜单 id |
| `type` | `String` | `page` 或 `group` |
| `pageId` | `String` | 关联前端页面 id |
| `title` | `String` | 显示标题 |
| `layout` | `String` | 前端 shell 布局键 |
| `icon` | `String` | 图标标识 |
| `order` | `Integer` | 同级排序 |
| `publicAccess` | `Boolean` | YAML `public`，匿名可访问标记 |
| `hidden` | `Boolean` | 不在侧栏展示 |
| `activeMenu` | `String` | 详情页高亮菜单 id |
| `roles` | `List<String>` | 后端预设可见角色；空列表不限制 |
| `children` | `List<SitemapMenuItem>` | `group` 子菜单 |

`roles`、`hidden` 由 `navigation.service.MetaSitemapMenuFilter` 消费；catalog 授权仍由
`ConsolePagePermissionAuthorizer` 独立判定。
