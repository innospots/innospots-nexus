# 包 `openapi.catalog`（及 `catalog.internal`）

构建期 OpenAPI 规范目录：各 API 模块将规范打包为
`META-INF/nexus-openapi/<artifactId>.yaml`，运行时经 HTTP 暴露列表与 JSON 文档。

## OpenApiCatalogPaths

**类型：** class（final，常量 + 工具）

OpenAPI 规范目录与 Scalar 文档页的 HTTP 路径常量。

### 常量

| 常量 | 说明 |
|------|------|
| `SPECS_BASE = "/openapi/specs"` | 规范目录根路径 |
| `SPECS_ITEM_PREFIX` | 单个规范 JSON 文档前缀（`SPECS_BASE + "/"`） |
| `UI_DEFAULT = "/openapi/ui"` | Scalar 文档页默认路径 |
| `SCALAR_JS_SEGMENT = "scalar.js"` | Scalar 文档页挂载的前端脚本文件名（相对 `UI_DEFAULT`） |

### 方法

#### `normalizeSpecsBase(String specsBase) → String`（static）
- **说明：** 规范化规范目录根路径（无前导 `/` 时补齐，并去掉末尾 `/`；空白时回退 `SPECS_BASE`）

#### `specItemUrlPrefix(String specsBase) → String`（static）
- **说明：** 单个规范 JSON 文档的 HTTP 前缀，形如 `/openapi/specs/`

#### `scalarJavascriptPath(String documentationPath, String scriptSegment) → String`（static）
- **说明：** Scalar 文档页对应的 `scalar.js` 绝对路径；`scriptSegment` 空白时用 `SCALAR_JS_SEGMENT`

#### `normalizeDocumentationPath(String path) → String`（static）
- **说明：** 规范化 Scalar HTML 文档页路径（无前导 `/` 时补齐；空白时回退 `UI_DEFAULT`）

## OpenApiCatalogEndpoint

**类型：** class（final），`@Path(OpenApiCatalogPaths.SPECS_BASE)`，`@Tag(name = "OpenApiCatalog")`

构建期 OpenAPI 规范目录端点：列表（`R`）与按 specId 返回 OpenAPI JSON 文档。

### 构造方法

#### `OpenApiCatalogEndpoint(OpenApiCatalogOperator catalogOperator)`
- **参数：**
  - `catalogOperator` — 规范目录操作器

### 端点

#### `GET listSpecs() → R<List<OpenApiSpecItemVo>>`
- **说明：** OpenAPI 规范列表（operationId `openApiSpecList`）
- **返回：** `R.ok(...)` 包装的规范项列表

#### `GET /{specId} getSpec(String specId) → JsonNode`
- **说明：** OpenAPI 规范明细 JSON 文档（operationId `openApiSpecDetail`）
- **参数：**
  - `specId` — 规范标识
- **返回：** 解析后的 OpenAPI 文档 JSON 树

## OpenApiCatalogOperator

**类型：** class（final）

按文件名加载 `OpenApiBundledSpecs.RESOURCE_ROOT` 下的 OpenAPI YAML。

### 构造方法

#### `OpenApiCatalogOperator()`
- **说明：** 使用当前线程上下文 ClassLoader

#### `OpenApiCatalogOperator(ClassLoader classLoader)`
- **参数：**
  - `classLoader` — 扫描 classpath 使用的类加载器

### 方法

#### `listSpecs() → List<OpenApiSpecItemVo>`
- **说明：** 列出全部打包规范（文件名主体即 specId，文件名为 `<specId>.yaml`）

#### `readYaml(String specId) → String`
- **说明：** 读取打包 YAML 正文
- **异常：** specId 非法时 `INVALID_PARAMETER`；未找到时 `RESOURCE_NOT_FOUND`

#### `readOpenApiDocument(String specId) → JsonNode`
- **说明：** 读取打包 YAML 并解析为 OpenAPI 文档 JSON（供 HTTP `application/json` 响应）

## OpenApiSpecItemVo

**类型：** record（`@Schema(name = "OpenApiSpecItemVo")`）

classpath 上的单个 OpenAPI 模块规范。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `specId` | `String` | 规范标识（`<specId>.yaml` 的文件名主体），如 `innospots-nexus-console` |
| `fileName` | `String` | 打包 YAML 文件名，如 `innospots-nexus-console.yaml` |

## 包 `catalog.internal`

### OpenApiBundledSpecs

**类型：** class（final，静态工具）

读取 `META-INF/nexus-openapi/<specId>.yaml` 构建期产物（支持文件目录与 JAR 两种 classpath 形态）。

| 常量 / 方法 | 说明 |
|------|------|
| `RESOURCE_ROOT = "META-INF/nexus-openapi"` | 打包资源根路径 |
| `listSpecIds(ClassLoader)` | 扫描全部规范 ID（去重、排序）；扫描失败映射 `SYSTEM_ERROR` |
| `readYaml(ClassLoader, specId)` | 读取 YAML 正文；specId 含 `/`、`..` 或空白时 `INVALID_PARAMETER`，未找到时 `RESOURCE_NOT_FOUND` |

### OpenApiBundledSpecCodec

**类型：** class（final，静态工具）

将构建期打包的 OpenAPI YAML 正文解析为 JSON 树（HTTP 响应用 `application/json`）。

#### `parseYamlDocument(String yaml) → JsonNode`（static）
- **说明：** 使用 `YAMLMapper` 解析 YAML 为 JSON 树
- **参数：**
  - `yaml` — 打包 YAML 正文
- **异常：** `NexusException`（`SYSTEM_ERROR`）— YAML 解析失败时