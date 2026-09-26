# 包 `openapi.scalar`

## OpenApiScalarDocumentation

**类型：** class

Scalar 文档 UI 的框架无关配置与渲染，供 Spring、Quarkus 等运行时挂载 HTTP 路由时复用。

### 方法

#### `renderDocumentationHtml(ScalarProperties scalarProperties,
            OpenApiCatalogOperator operator) → String`
- **说明：** 规范化文档路径、绑定 catalog 源后渲染 HTML。 / public static String renderDocumentationHtml( ScalarProperties scalarProperties, OpenApiCatalogOperator operator)
