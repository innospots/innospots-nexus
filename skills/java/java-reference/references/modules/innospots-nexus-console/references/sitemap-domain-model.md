# 包 `sitemap.domain.model`

均为 `@Getter @Setter` + `@JsonAutoDetect(fieldVisibility = ANY)` + `@Schema` 的
sitemap YAML 结构模型。

## SitemapAppInfo

**类型：** class

Sitemap 应用展示元数据。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 应用显示名称 |

## SitemapAuthConfig

**类型：** class

Sitemap 认证相关配置。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `loginPath` | `String` | 登录页路径（如 `/login`） |

## SitemapPageDescriptor

**类型：** class

Sitemap 中的页面声明。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 页面 id（required） |
| `path` | `String` | 路由路径（required） |
| `title` | `String` | 页面标题 |
| `version` | `Integer` | 页面版本 |
| `publicAccess` | `Boolean` | 匿名可访问（YAML/JSON 字段 `public`） |
| `lazy` | `Boolean` | 是否懒加载（不下发 pageDsl） |
| `permission` | `String` | 访问所需权限码 |
| `pageDsl` | `PageDsl` | 内联页面 DSL（非懒加载时下发） |

## SitemapMenuItem

**类型：** class

Sitemap 菜单树节点（page / group / link）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 菜单 id（required） |
| `type` | `String` | 节点类型：`page` \| `group` \| `link`（required） |
| `pageId` | `String` | 关联页面 id（type=page） |
| `title` | `String` | 显示标题 |
| `icon` | `String` | 图标标识 |
| `layout` | `String` | 布局键 |
| `order` | `Integer` | 排序 |
| `publicAccess` | `Boolean` | 匿名可访问（YAML/JSON 字段 `public`） |
| `hidden` | `Boolean` | 不在导航中展示 |
| `activeMenu` | `String` | 高亮所属菜单 id |
| `permission` | `String` | 访问所需权限码 |
| `path` | `String` | 外链地址（type=link） |
| `external` | `Boolean` | 是否外部链接 |
| `children` | `List<SitemapMenuItem>` | 子菜单（type=group） |

## SitemapLayoutDefinition

**类型：** class

命名布局的 DSL 定义。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `dsl` | `String` | DSL 版本（如 `1.0`） |
| `body` | `SitemapLayoutNode` | 布局根节点（required） |

## SitemapLayoutNode

**类型：** class

布局 DSL 内联组件节点；`props` 与 `events` 保持开放结构。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `type` | `String` | 组件类型（required） |
| `id` | `String` | 可选节点 id |
| `props` | `Map<String, Object>` | 组件属性（开放 map） |
| `children` | `List<SitemapLayoutNode>` | 子节点 |
| `events` | `Map<String, Object>` | 事件处理器（开放 map） |