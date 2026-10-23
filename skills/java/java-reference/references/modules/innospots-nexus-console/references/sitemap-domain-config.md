# 包 `sitemap.domain.config`

## `SitemapConfig`

**类型：** class（final，`@Getter @Setter`、`@JsonAutoDetect(fieldVisibility = ANY)`）

从 classpath `meta-sitemap.yaml` 解析的站点元数据与会话无关默认菜单。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 站点 id |
| `title` | `String` | 站点标题 |
| `description` | `String` | 站点描述 |
| `version` | `Integer` | 文档版本 |
| `updatedAt` | `String` | 更新时间文本 |
| `menus` | `List<SitemapMenuItem>` | 菜单树；默认空列表 |

该类型不是前端 `sitemap.yaml` 的完整模型，不包含 `pages`、`layouts` 或 `auth`。
