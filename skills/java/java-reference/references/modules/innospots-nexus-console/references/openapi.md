# 包 `openapi`

OpenAPI 注解与构建说明见
[`innospots-nexus-openapi` 索引](../innospots-nexus-openapi/README.md)、
[`openapi-contract.md`](../../openapi-contract.md)。

## NexusConsoleOpenApiDefinition

**类型：** class

控制台 OpenAPI 全局元数据（构建期扫描；非 JAX-RS Application，以便与 portal/platform
共宿主）。 带 Path 以便 SmallRye JAX-RS 扫描器拾取 OpenAPIDefinition，运行时不暴露端点。

当前 Tag 集：`Auth`（认证）、`Catalog`（目录）、`Permission`（权限）、`Role`（角色）、
`Dictionary`（租户级字典）、`Plugin`（插件生命周期）、`UiPage`（Pactor 页面 DSL 加载与渲染）、
`NexusSitemap`（动态页面 Sitemap 加载与渲染）、`OpenApiCatalog`（OpenAPI 规范目录）。