# OpenAPI（`core.openapi`）

MicroProfile OpenAPI 安全契约、构建期规范目录与 Scalar 文档页的框架无关运行时。

规范条文与端点/Schema 注解要求见
[`standards/openapi.md`](../../../../standards/openapi.md)、
[`openapi-contract.md`](../../openapi-contract.md)。

## 构成

```text
core.openapi（根）
  ├─ NexusAuthenticatedApi        @interface：Bearer 认证标记
  ├─ NexusOpenApiSecurityNames    安全方案名称常量
  ├─ OpenApiCatalogPaths          规范目录与 Scalar 文档页路径常量 + 规范化工具
  ├─ catalog                      运行时规范目录（HTTP 列表 + JSON 文档）
  │    └─ internal                classpath YAML 读取与解析（构建期产物）
  ├─ scalar                       Scalar 文档 UI 配置与 HTML/JS 渲染
  └─ schema                       @Schema 约定（package 文档，无类型）
```

## NexusAuthenticatedApi

**类型：** annotation

标记需要 Bearer 认证的控制台 API 资源。`@Inherited`、`@Target(TYPE)`、RUNTIME retention，
携带 `@SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)`。

## NexusOpenApiSecurityNames

**类型：** class

MicroProfile OpenAPI 安全方案名称常量。常量 `BEARER_AUTH = "bearerAuth"`。

## Schema 约定（`openapi.schema`）

Jakarta REST 请求/响应 DTO 使用 `record`（或少数 `@BeanParam` Bean），在类型与各组件上标注
`@Schema`，由 SmallRye 在 `process-classes` 生成 components。端点返回 `R<T>` /
`R<PageResult<T>>` 时无需为每个接口手写 response schema：JAX-RS 扫描器读取方法
`java.lang.reflect.Type`，将类型实参绑定到 `R.data`、`PageResult.records`。base 中的
`R`、`PageResult`、`I18nObject` 等共享类型须带 `@Schema`，且 base JAR 须包含 Jandex 索引
（`jandex-maven-plugin`），以便 `scanDependenciesDisable=false` 的模块解析依赖中的泛型 record。

详细 API 见 [`openapi-catalog.md`](openapi-catalog.md)、[`openapi-scalar.md`](openapi-scalar.md)。