# 包 `endpoint.grant`（已迁至 core）

Grant 注解、目录模型与 `EndpointGrantManager` 已归属 **`innospots-nexus-core`**
（`com.innospots.nexus.core.endpoint.grant`）。

完整 API 参考见：

- [`innospots-nexus-core` → `endpoint-grant.md`](../../innospots-nexus-core/references/endpoint-grant.md)
- [`innospots-nexus-core` → `endpoint-grant-support.md`](../../innospots-nexus-core/references/endpoint-grant-support.md)

本模块保留 **JAX-RS 过滤器链**（`console.jaxrs.filter`）、**请求属性**（`console.jaxrs.support`）
与 **catalog 组装**（`CatalogPermissionCatalogAssembler` → `CatalogModuleGrantVo` 等 REST 响应）。
