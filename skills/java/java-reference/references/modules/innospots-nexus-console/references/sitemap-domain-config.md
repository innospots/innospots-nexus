# 包 `sitemap.domain.config`

## SitemapConfig

**类型：** class（final，`@Getter @Setter`）

从 classpath YAML 解析的 sitemap 配置文档（会话无关超集）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 站点 id |
| `resourceType` | `String` | 资源类型（须为 `sitemap`，loader 校验） |
| `version` | `Integer` | sitemap 版本 |
| `dslVersion` | `String` | DSL 版本 |
| `app` | `SitemapAppInfo` | 应用展示元数据 |
| `layout` | `String` | 默认布局键 |
| `auth` | `SitemapAuthConfig` | 认证配置 |
| `pages` | `List<SitemapPageDescriptor>` | 页面声明列表；默认空列表 |
| `menus` | `List<SitemapMenuItem>` | 菜单树；默认空列表 |
| `layouts` | `Map<String, SitemapLayoutDefinition>` | 命名布局 DSL 表（保持插入序） |

### 备注

`@JsonAutoDetect(fieldVisibility = ANY)` 直接按字段绑定 YAML；与
`SitemapResource` 结构同构（后者额外预留会话动态字段）。