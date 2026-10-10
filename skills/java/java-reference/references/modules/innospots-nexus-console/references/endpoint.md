# 包 `endpoint`

根路径与公共 API 的 Jakarta REST 资源。运行时绑定、认证、过滤器与具体实现属于
适配器/应用模块（本模块仅定义 JAX-RS 边界）。

## ConsoleEndpoint

**类型：** interface

根管理控制台端点契约（`@Path(ConsoleConstant.API_PREFIX)`，当前 `/api/d/nexus`）。

## MainRootEndpoint

**类型：** class（final），`@Path("/")`

站点根路径 `GET /`：重定向至配置的文档入口（见 `nexus.console.web.jersey.root-path`，
由 `ConsoleWebJerseySettings.rootPath` 提供）。

### 构造方法

#### `MainRootEndpoint(String rootRedirectPath)`
- **参数：**
  - `rootRedirectPath` — 重定向目标；空白时回退 `OpenApiCatalogPaths.UI_DEFAULT`（`/openapi/ui`）

### 端点

#### `GET redirectRoot() → Response`
- **说明：** 307 临时重定向到文档入口
- **返回：** `Response.temporaryRedirect(...)`

## PublicEndpoint

**类型：** interface，`@Path(ConsoleConstant.PUBLIC_API_PREFIX)`

公共开放 REST API 根契约（`/api/public`）。挂载于此前缀下的资源默认纳入
`ConsolePermitAllPaths`（与 `/openapi/**` 相同，免 Bearer 鉴权）。