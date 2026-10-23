# 包 `endpoint.contract`

共享 JAX-RS 根路径契约（`com.innospots.nexus.core.endpoint.contract`）。Console 实现与
OpenAPI 扫描消费这些接口；运行时过滤器与 Spring 组装仍在 **console** 模块。

## `ConsoleRestPaths`

**类型：** class（final）

控制台与公共开放 REST API 根路径前缀常量。

| 常量 | 值 | 说明 |
|------|-----|------|
| `API_PREFIX` | `/api/d/nexus` | 管理控制台 API 根 |
| `PUBLIC_API_PREFIX` | `/api/public` | 公共开放 API 根（免 Bearer） |

`com.innospots.nexus.console.config.ConsoleConstant` 委托上述常量，并提供 `apiPath` /
`publicPath` 拼接辅助方法。

## `ConsoleEndpoint`

**类型：** interface

根管理控制台端点契约。类级 `@Path(ConsoleRestPaths.API_PREFIX)`。

## `PublicEndpoint`

**类型：** interface

公共开放 REST API 根契约，`@Path(ConsoleRestPaths.PUBLIC_API_PREFIX)`；挂载子路径默认纳入
console `ConsolePermitAllPaths`（与 `/openapi/**` 同类，免 Bearer 鉴权）。
