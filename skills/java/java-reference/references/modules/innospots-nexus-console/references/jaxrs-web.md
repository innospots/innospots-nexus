# 包 `jaxrs.web`

Jersey Web 设置（可由 `nexus.console.web.*` 配置绑定；Spring 装配见
`innospots-nexus-spring-console` 的 `ConsoleJaxRsWebConfiguration`）。

## ConsoleWebSecuritySettings

**类型：** class（`@Getter`）

Jersey 鉴权相关路径与请求头配置。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `enabled` | `boolean` | 是否启用 Jersey 请求侧安全（Bearer 鉴权 + 控制台页面权限 Filter）；默认 `true` |
| `devSession` | `ConsoleWebDevSessionSettings` | 关闭 `enabled` 时的开发用会话绑定；默认关闭 |
| `permitAllPatterns` | `List<String>` | 无需 Bearer 令牌的路径 Ant 模式（如 OpenAPI、健康检查）；初始为 `ConsolePermitAllPaths.defaultPatterns()` |
| `consolePathPatterns` | `List<String>` | 控制台页面权限 Filter 生效的 HTTP 路径 Ant 模式；默认 `/console/datasource/**` |
| `pageKeyHeader` | `String` | 页面权限鉴权用的页面键请求头名；默认 `X-Nexus-Page-Key` |

### 方法

#### `setPermitAllPatterns(List<String> permitAllPatterns)`
- **说明：** 设置免登录路径列表；`null` 时仅清空自定义项并恢复构造期默认

#### `setConsolePathPatterns(List<String> consolePathPatterns)`
- **说明：** 设置页面权限 Filter 生效路径列表；`null` 时清空（不启用页面权限 Filter）

## ConsoleWebCorsSettings

**类型：** class（`@Getter @Setter`）

Jersey `ContainerResponseFilter` 输出的 CORS 头配置（`ConsoleCorsFilter` 使用）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `enabled` | `boolean` | 是否向响应附加 CORS 头；默认 `false` |
| `allowedOrigins` | `List<String>` | `Access-Control-Allow-Origin`；默认 `*` |
| `allowedMethods` | `List<String>` | `Access-Control-Allow-Methods`；默认 GET/POST/PUT/PATCH/DELETE/OPTIONS/HEAD |
| `allowedHeaders` | `List<String>` | `Access-Control-Allow-Headers`；默认 `*` |
| `exposedHeaders` | `List<String>` | `Access-Control-Expose-Headers`；默认空 |
| `allowCredentials` | `boolean` | `Access-Control-Allow-Credentials`；默认 `false` |
| `maxAgeSeconds` | `long` | 预检请求 `Access-Control-Max-Age`（秒）；默认 3600 |

### 方法

#### `setAllowedOrigins(List<String>)` / `setAllowedMethods(List<String>)` / `setAllowedHeaders(List<String>)` / `setExposedHeaders(List<String>)`
- **说明：** 对应列表 setter；`null` 时置为空列表

## ConsoleWebDevSessionSettings

**类型：** class（`@Getter @Setter`）

关闭请求侧安全时的固定会话快照配置（`ConsoleDevSessionBinder` 消费）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `enabled` | `boolean` | 是否注入 dev 会话；默认 `false` |
| `userId` | `String` | 逻辑用户 ID（字符串，与令牌声明一致）；默认 `"1"` |
| `tenantId` | `String` | 租户 ID；默认 `"dev-tenant"` |
| `workspaceId` | `String` | 工作区 ID；默认 `"dev-workspace"` |
| `projectId` | `String` | 可选项目 ID |
| `realm` | `String` | 可选 `TLC.SECURITY_REALM` |
| `tenantMemberId` | `String` | 可选租户成员 ID |

## ConsoleWebJerseySettings

**类型：** class（`@Getter @Setter`）

Jersey Servlet Filter 模式下的路由行为（与 `spring.jersey.type=filter` 配合）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `rootPath` | `String` | `GET /` 重定向目标（`MainRootEndpoint`）；绑定 `nexus.console.web.jersey.root-path`；默认 Scalar 文档页 `/openapi/ui` |
| `forwardOn404` | `boolean` | 未匹配 JAX-RS 资源时是否交给后续 Servlet 链（含 Spring MVC）；默认 `true`（关闭时 Jersey 直接 404，避免落入 Whitelabel HTML） |