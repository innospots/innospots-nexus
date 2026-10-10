# 包 `jaxrs.filter`

均为 `@Provider` 的 JAX-RS 请求过滤器；按 `@Priority` 升序执行（见 [`jaxrs.md`](jaxrs.md) 链路图）。

## ConsoleAuthenticationFilter

**类型：** class，`@Priority(Priorities.AUTHENTICATION)`

解析 Bearer 访问令牌并填充 `SessionContext`。默认配置下公共 API 与 OpenAPI 同属
`ConsolePermitAllPaths`，完全跳过鉴权；若自定义去掉对应 permit 项，则仍可通过
`ConsolePublicApiPaths` 匿名访问，且携带 Bearer 时会校验并绑定会话。

### 构造方法

#### `ConsoleAuthenticationFilter(ConsoleWebSecuritySettings security, TokenIssuer tokenIssuer)`
- **参数：**
  - `security` — Jersey 鉴权路径配置
  - `tokenIssuer` — 访问令牌解析

### 方法

#### `filter(ContainerRequestContext requestContext)`
- **说明：** 安全关闭时直接放行；permitAll 命中时跳过；公共 API 匿名放行（携带 Bearer 则解析绑定）；
  其余路径无 Bearer 时抛 `AUTHENTICATION_FAILED`
- **异常：** `NexusException` — 缺失/格式非法的 Bearer 头时

## ConsoleCorsFilter

**类型：** class，`@Priority(50)`，实现 `ContainerRequestFilter` 与 `ContainerResponseFilter`

可配置的 JAX-RS CORS 支持；默认关闭（`ConsoleWebCorsSettings.enabled = false`）。
OPTIONS 预检直接 `Response.ok()` 并附 CORS 头（abortWith）；普通响应附加
`Access-Control-Allow-*` / `Max-Age` 头。来源解析：允许 `*` 或显式列表；
`allowCredentials` 时回显请求 Origin。

### 构造方法

#### `ConsoleCorsFilter(ConsoleWebCorsSettings cors)`
- **参数：**
  - `cors` — CORS 配置

## ConsoleDevSessionFilter

**类型：** class，`@Priority(Priorities.AUTHENTICATION + 10)`

在 `ConsoleWebSecuritySettings.enabled = false` 时按 `devSession` 配置注入开发用
`SessionContext`。与 `ConsoleAuthenticationFilter` 职责分离：认证 Filter 只处理
Bearer 令牌；本 Filter 仅在关闭请求侧安全时提供可配置的固定身份，便于本地调试
依赖租户/工作区上下文的接口。已有用户绑定时跳过。

### 构造方法

#### `ConsoleDevSessionFilter(ConsoleWebSecuritySettings security)`
- **参数：**
  - `security` — Jersey 鉴权路径配置

## ConsolePagePermissionFilter

**类型：** class，`@Priority(Priorities.AUTHENTICATION + 100)`

对 `consolePathPatterns` 命中的路径执行控制台页面权限校验（在 Bearer 鉴权之后）。
公共 API 不执行页面权限校验。将 HTTP 方法、路径与 `pageKeyHeader` 转为
`AuthorizationRequest`，委托 `ConsolePagePermissionAuthorizer` 判定 catalog
PAGE/DATASOURCE 授权；与「是否已登录」的认证 Filter 语义区分。判定通过后将
`AuthorizationContext` 写入请求属性 `RequestProperties.AUTHORIZATION_CONTEXT`。

### 构造方法

#### `ConsolePagePermissionFilter(ConsoleWebSecuritySettings security, ConsolePagePermissionAuthorizer pagePermissionAuthorizer, AuthorizationSubjectResolver subjectResolver)`
- **参数：**
  - `security` — Jersey 鉴权路径配置
  - `pagePermissionAuthorizer` — 页面/数据源权限判定
  - `subjectResolver` — 当前鉴权主体解析

### 方法

#### `filter(ContainerRequestContext requestContext)`
- **说明：** 缺失页面键头或判定不允许时抛 `NO_PERMISSION`；主体缺失时抛 `AUTHENTICATION_FAILED`
- **异常：** `NexusException`

## ConsoleRequestContextFilter

**类型：** class，`@Priority(Priorities.USER)`，实现 `ContainerRequestFilter` 与 `ContainerResponseFilter`

分配 requestId 并在响应结束后清理线程上下文。

### 方法

#### `filter(ContainerRequestContext requestContext)`（请求阶段）
- **说明：** `RequestScope.bind`；requestId 取 `X-Request-Id` 头，缺失时生成去连字符 UUID；
  写入请求属性 `REQUEST_ID` 与 `TLC.TRACE_ID`

#### `filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)`（响应阶段）
- **说明：** 响应头回填 `X-Request-Id`；`ConsoleTokenSessionBinder.clear()` + `RequestScope.clear()`