# 包 `sitemap.domain.vo`

## SitemapResource

**类型：** class（final，`@Getter @Setter`，`@Schema(name = "SitemapResource")`）

对外下发的 sitemap 资源（按会话裁剪后的形态；结构与 `SitemapConfig` 同构，并预留会话
动态字段）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 站点 id（required，如 `site`） |
| `resourceType` | `String` | 资源类型（required，如 `sitemap`） |
| `version` | `Integer` | Sitemap 版本 |
| `dslVersion` | `String` | DSL 版本（如 `1.0`） |
| `updatedAt` | `String` | 最近更新时间（ISO-8601，会话渲染时注入） |
| `app` | `SitemapAppInfo` | 应用元数据 |
| `user` | `Map<String, Object>` | 当前用户快照（会话渲染时注入） |
| `permissions` | `List<String>` | 当前用户权限码（会话渲染时注入） |
| `layout` | `String` | 默认布局键 |
| `auth` | `SitemapAuthConfig` | 认证配置 |
| `pages` | `List<SitemapPageDescriptor>` | 页面声明列表（required） |
| `menus` | `List<SitemapMenuItem>` | 菜单树（required） |
| `layouts` | `Map<String, SitemapLayoutDefinition>` | 命名布局 DSL 表（required） |

### 备注

YAML/JSON 字段 `public` 通过 `@JsonProperty("public")` 绑定到模型 `publicAccess`
（见 `SitemapPageDescriptor` / `SitemapMenuItem`）；当前由 `SitemapMapper` 全量复制，
会话字段留待后续裁剪渲染填充。