# JAX-RS 横切能力（`core.jaxrs`）

业务中立的 Jakarta REST 横切能力：异常映射与请求元数据支持。

- Spring Boot 装配见 `innospots-nexus-spring-console` 中的 `ConsoleJaxRsWebConfiguration`。
- console 侧过滤器与权限校验见 `com.innospots.nexus.console.jaxrs`。

## 分层

```text
ExceptionMapper<NexusException>            NexusExceptionMapper（@Provider）
ExceptionMapper<WebApplicationException>   WebApplicationExceptionMapper（保留原生 HTTP 状态，如 404）
ExceptionMapper<Throwable>                 ThrowableExceptionMapper（未捕获兜底 → SYSTEM_ERROR）
        ↓
JaxRsExceptionSupport                      统一记录 + R.fail 响应（X-Request-Id 头）
        ↑
RequestScope / RequestProperties           请求元数据（由宿主过滤器 bind/clear）
```

## 错误响应形态

`NexusException` 映射为 legacy `R` 形态的 JSON 响应：

- HTTP 状态取 `NexusStatusCode.findByFullCode(code).httpStatusCode()`，未命中时兜底
  `SYSTEM_ERROR.httpStatusCode()`。
- 响应头 `X-Request-Id`：优先取请求属性 `RequestProperties.REQUEST_ID`，其次 `TLC.TRACE_ID`，
  最后 `unknown`。
- 日志分级：5xx 或存在 cause 输出 ERROR 与堆栈，其余为 WARN。

详细 API 见 [`jaxrs-exception.md`](jaxrs-exception.md)、[`jaxrs-support.md`](jaxrs-support.md)。