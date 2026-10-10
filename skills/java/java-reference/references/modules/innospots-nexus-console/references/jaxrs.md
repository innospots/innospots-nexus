# JAX-RS 横切能力（`console.jaxrs`）

管理控制台 Jakarta REST 横切能力：请求过滤器链、页面权限路径工具、会话绑定与 Jersey Web 设置。

- 业务中立的异常映射与请求元数据支持位于 `com.innospots.nexus.core.jaxrs`（core 模块）。
- 宿主无关的 Jersey 装配与 OpenAPI 文档页位于 `com.innospots.nexus.spring.core.jaxrs`。
- console 专属过滤器装配见 `innospots-nexus-spring-console` 中的 `ConsoleJaxRsWebConfiguration`。

## 请求过滤器链（按 `@Priority` 升序执行）

```text
@Priority(50)    ConsoleCorsFilter              CORS 预检 + 响应头（默认关闭）
AUTHENTICATION   ConsoleAuthenticationFilter    Bearer 令牌解析 → SessionContext 绑定
AUTHENTICATION+10 ConsoleDevSessionFilter       关闭安全时注入固定 dev 会话
AUTHENTICATION+100 ConsolePagePermissionFilter  控制台页面权限校验（catalog PAGE/DATASOURCE）
USER             ConsoleRequestContextFilter    分配 requestId（TLC.TRACE_ID），响应后清理线程上下文
```

要点：

- 免鉴权路径由 `ConsoleWebSecuritySettings.permitAllPatterns`（默认 `ConsolePermitAllPaths.defaultPatterns()`，
  含 `/openapi/**`、公共 API、健康检查与站点根）**完全跳过**。
- 公共开放 API（`ConsolePublicApiPaths`）匿名可访问；携带 Bearer 时仍校验并绑定会话。
- 页面权限 Filter 仅对 `consolePathPatterns`（默认 `/console/datasource/**`）生效，
  使用请求头 `X-Nexus-Page-Key` 定位页面；公共 API 不做页面权限校验。

详细 API 见 [`jaxrs-filter.md`](jaxrs-filter.md)、[`jaxrs-support.md`](jaxrs-support.md)、
[`jaxrs-web.md`](jaxrs-web.md)。