# 包 `sitemap.service`

## SitemapService

**类型：** class（final）

加载并渲染 sitemap（当前按固定 YAML 全量返回，后续按会话与权限裁剪）。

### 构造方法

#### `SitemapService(SitemapConfigLoader configLoader, SitemapMapper mapper)`
- **说明：** 参数缺失时抛 `NexusException`（`CONFIG_ERROR`）
- **参数：**
  - `configLoader` — classpath 配置加载器
  - `mapper` — 配置 → 资源映射

### 方法

#### `render(String domainKey) → SitemapResource`
- **说明：** 渲染指定领域的 sitemap 资源
- **参数：**
  - `domainKey` — `ui-pages` 领域键
- **返回：** 对外 sitemap
- **异常：** `NexusException`（`CONFIG_ERROR`）— domainKey 为空或资源缺失/解析失败时

## SitemapMapper

**类型：** class（final）

将 classpath 配置映射为对外 sitemap 资源。

### 方法

#### `toResource(SitemapConfig config) → SitemapResource`
- **说明：** 将会话无关配置复制为资源形态（暂不裁剪、不注入会话字段：`user`、`permissions`、
  `updatedAt` 留空）
- **参数：**
  - `config` — YAML 配置
- **返回：** 对外资源；`config` 为 `null` 时返回 `null`