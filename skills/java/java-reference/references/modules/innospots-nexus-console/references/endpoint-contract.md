# 包 `endpoint.contract`

## ConsoleEndpoint

**类型：** interface

根管理控制台端点契约。具体 JAX-RS 根路径由实现绑定到
`ConsoleConstant.API_PREFIX`（`/api/d/nexus`）。

## PublicEndpoint

**类型：** interface

公共开放 REST API 根契约，对应 `/api/public`；该前缀及业务域 `public` 路径免 Bearer 鉴权。
