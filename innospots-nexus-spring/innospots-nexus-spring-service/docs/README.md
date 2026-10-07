# Spring 服务标准接口 — 开发接入手册

本目录为 **innospots-nexus-spring-service** 的分主题接入手册。每个主题单独成文，按步骤说明：依赖引入、自动装配、YAML/Bean 配置、Controller 写法、MVC 与 WebFlux 差异、验证方式，以及该主题的二次扩展（继承、Bean 覆盖、配置类）。

## 阅读顺序建议

| 文档 | 适用场景 |
|------|----------|
| [HTTP API 接入](http-api-integration.md) | 普通 REST、统一错误体、权限与 Invocation 桥接 |
| [流式 Stream 接入](stream-integration.md) | SSE / NDJSON、`StreamSession`、客户端断连协作 |
| [WebSocket 接入](websocket-integration.md) | 长连接、JSON envelope、跨连接推送 |
| [文件上传下载接入](file-transfer-integration.md) | `DownloadResource` / `UploadResource`、Range 与校验 |
| [熔断与限流治理](governance-integration.md) | 限流、超时、舱壁、熔断与 `GovernanceConfig` |
| [观测与监控采集](observability-integration.md) | Access Log、MDC、Trace、业务指标 |

## 共性前置条件

1. **Maven**：在 Web 入口模块（assembly / application）引入 `innospots-nexus-spring-service`，版本由 `innospots-nexus-bom` 管理；**不要**在纯 domain 库中依赖本模块。
2. **Starter**：Servlet 应用加 `spring-boot-starter-web`；响应式应用加 `spring-boot-starter-webflux`；WebSocket 另加 `spring-boot-starter-websocket`（MVC）或 WebFlux WS 组件。
3. **最小配置**：

```yaml
service:
  enabled: true
  name: my-app
  response-profile: LEGACY   # 或 PROBLEM_DETAIL
```

4. **显式启用**（在 `@SpringBootApplication` 或配置类上）：

| 能力 | 注解 |
|------|------|
| HTTP API（必选基座） | `@EnableNexusServiceHttp` |
| 流式 SSE / `StreamSession` | `@EnableNexusServiceStream` |
| 文件下载 `DownloadResource` | `@EnableNexusServiceTransfer` |
| WebSocket Registry | `@EnableNexusServiceWebSocket` |
| 全部 | `@EnableNexusService` |

   Servlet 与 WebFlux 由 `@EnableNexusServiceHttp` 内部条件配置二选一（`ServiceServletConfiguration` / `ServiceReactiveConfiguration`）。

5. **边界原则**：业务实现放在 **中立库**（`service-contract` / `service-stream` 等）；Spring 模块只做 Filter、Bridge、ReturnValueHandler。业务代码禁止直接写 `HttpServletResponse` 输出流（下载/流式走标准返回类型）。

## 参考代码与测试

- 模块内夹具：`src/test/java/com/innospots/nexus/spring/service/test/fixture/`
- 黑盒场景库：`innospots-nexus-service-adapter-test`
- 中立能力说明：`innospots-nexus-service` 各子模块 `README.md`
- 设计规格：`innospots-nexus-service/docs/service-adapter-design.md`

## 本地验证

```bash
mvn -pl innospots-nexus-spring/innospots-nexus-spring-service -am test \
  -Dtest=AdapterScenarioMvcTest,AdapterScenarioWebFluxTest
```
