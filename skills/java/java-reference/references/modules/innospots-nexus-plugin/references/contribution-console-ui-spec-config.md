# 包 `contribution.console.ui.spec.config`

## PageDslConfig

**类型：** record

定位与解析 Pactor 页面 DSL 文件的不可变配置。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `basePath` | `String` | classpath 基础目录 |
| `fileSuffix` | `String` | 页面文件后缀，通常为 .yaml |
| `failOnUnknownProperties` | `boolean` | 未知 YAML 字段是否导致解析失败 |

### 方法

#### `defaults() → PageDslConfig`
- **说明：** 创建并校验配置。 public PageDslConfig { basePath = normalizeBasePath(basePath); if (!".yaml".equals(fileSuffix) && !".yml".equals(fileSuffix)) { invalid("PageDsl fileSuffix must be '.yaml' or '.yml'"); } } /** 返回 *.yaml 资源的严格默认配置。
- **返回：** 默认配置

#### `resourcePath(String moduleKey, String pageKey) → String`
- **说明：** 构建一个模块页面的 classpath 资源路径。
- **参数：**
  - `moduleKey` — 所属模块键
  - `pageKey` — 与 page.id 匹配的页面键
- **返回：** classpath 资源路径
