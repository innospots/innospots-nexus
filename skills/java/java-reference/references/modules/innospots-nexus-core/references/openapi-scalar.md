# 包 `openapi.scalar`

## OpenApiScalarDocumentation

**类型：** class（final，静态工具）

Scalar 文档 UI 的框架无关配置与渲染，供 Spring、Quarkus 等运行时挂载 HTTP 路由时复用。

### 方法

#### `createDefaultProperties() → ScalarProperties`（static）
- **说明：** 创建带 Nexus 默认值的新 `ScalarProperties`

#### `applyNexusDefaults(ScalarProperties properties)`（static）
- **说明：** 应用 Nexus 平台推荐的 Scalar UI 默认值；Spring `scalar.*` 未配置时使用
  （启用、路径 `/openapi/ui`、标题 `Nexus API Reference`、MODERN 布局、显示侧栏、
  关闭 telemetry 与 agent、持久化认证等）

#### `normalizeDocsPath(String path) → String`（static）
- **说明：** 规范化文档路径（委托 `OpenApiCatalogPaths.normalizeDocumentationPath`）

#### `applyCatalogSources(ScalarProperties scalarProperties, OpenApiCatalogOperator operator)`（static）
- **说明：** 以默认规范目录根路径绑定 catalog 源

#### `applyCatalogSources(ScalarProperties scalarProperties, List<OpenApiSpecItemVo> specs)`（static）
- **说明：** 以默认规范目录根路径绑定规范项列表

#### `applyCatalogSources(ScalarProperties scalarProperties, List<OpenApiSpecItemVo> specs, String specsBase)`（static）
- **说明：** 为每个规范生成 `ScalarSource`（URL = `specItemUrlPrefix + specId`，首个为 default）；
  空列表时不修改

#### `renderDocumentationHtml(ScalarProperties scalarProperties, OpenApiCatalogOperator operator) → String`（static）
- **说明：** 规范化路径、绑定 catalog 源后渲染 HTML（默认规范目录根路径）
- **异常：** `IOException` — 渲染失败时

#### `renderDocumentationHtml(ScalarProperties scalarProperties, OpenApiCatalogOperator operator, String specsBase) → String`（static）
- **说明：** 同上，显式指定规范目录根路径
- **异常：** `IOException` — 渲染失败时

#### `scalarJavascriptContent() → byte[]`（static）
- **说明：** 读取 Scalar 前端脚本（`scalar.js`）内容
- **异常：** `IOException` — 读取失败时

#### `prepareForServing(ScalarProperties scalarProperties, OpenApiCatalogOperator operator) → ScalarProperties`（static）
- **说明：** 基于模板配置生成可渲染快照（默认规范目录根路径）

#### `prepareForServing(ScalarProperties scalarProperties, OpenApiCatalogOperator operator, String specsBase) → ScalarProperties`（static）
- **说明：** 复制模板（`BeanUtils.copyProperties`，不修改传入的 `template`，如 Spring `scalar.*`
  Bean）、规范化路径并绑定 catalog 源；复制后清空 `sources` 避免与模板共享列表