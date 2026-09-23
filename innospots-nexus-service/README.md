# 统一服务接入与治理

当前 M1 contract、M2 runtime、M3 http、M4 stream、M5 websocket、M6 governance/observability 已实现；`service-adapter-test` 十二场景已落地；M7 Spring MVC/WebFlux 与 M8 Quarkus REST/WebSockets Next 自动配置与夹具均已通过共享黑盒测试。M9（二期）核心能力已落地：transfer 模块、审计 REQUIRED/BEST_EFFORT 行为、幂等协调、NDJSON 编码与 WS 消息级权限校验。
模块划分由开发者于 2026-09-13 确认。

可实施技术设计见 [docs/README.md](docs/README.md)。业务开发用法见 [docs/service-developer-experience-design.md](docs/service-developer-experience-design.md)。

## 模块文档（快速接入）

各子模块 README 含**模块介绍、包结构、配置与示例**：

| 模块 | README |
|---|---|
| service-contract | [innospots-nexus-service-contract/README.md](innospots-nexus-service-contract/README.md) |
| service-runtime | [innospots-nexus-service-runtime/README.md](innospots-nexus-service-runtime/README.md) |
| service-http | [innospots-nexus-service-http/README.md](innospots-nexus-service-http/README.md) |
| service-websocket | [innospots-nexus-service-websocket/README.md](innospots-nexus-service-websocket/README.md) |
| service-stream | [innospots-nexus-service-stream/README.md](innospots-nexus-service-stream/README.md) |
| service-transfer | [innospots-nexus-service-transfer/README.md](innospots-nexus-service-transfer/README.md) |
| service-observability | [innospots-nexus-service-observability/README.md](innospots-nexus-service-observability/README.md) |
| service-governance | [innospots-nexus-service-governance/README.md](innospots-nexus-service-governance/README.md) |
| service-adapter-test | [innospots-nexus-service-adapter-test/README.md](innospots-nexus-service-adapter-test/README.md) |
| Spring adapter | [../innospots-nexus-spring/innospots-nexus-spring-service/README.md](../innospots-nexus-spring/innospots-nexus-spring-service/README.md) |
| Quarkus adapter | [../innospots-nexus-quarkus/innospots-nexus-quarkus-service/README.md](../innospots-nexus-quarkus/innospots-nexus-quarkus-service/README.md) |

### 推荐阅读顺序

```text
1. service-contract README        → 注解与 SPI
2. Spring 或 Quarkus adapter README → 应用怎么配
3. stream / websocket / transfer README → 按需 Level 2 能力
4. service-developer-experience-design.md → 分层与禁止项
```

## 模块与依赖

下表名称省略 `innospots-nexus-` 前缀。箭头表示消费方依赖提供方。

| 模块 | 职责 | 直接内部依赖 |
|---|---|---|
| service-contract | 调用上下文、生命周期、扩展与审计输出契约 | base |
| service-runtime | 执行链、异步上下文与资源生命周期 | service-contract |
| service-http | HTTP 请求响应策略、错误映射 | service-runtime |
| service-websocket | 连接与消息生命周期、慢消费者策略 | service-runtime |
| service-stream | 流式数据、背压、取消与完成 | service-runtime |
| service-transfer | 文件上传下载、范围读取与流式传输 | service-runtime |
| service-observability | 跟踪、指标、日志及审计输出集成 | service-contract |
| service-governance | 流控、限流、隔离、超时与熔断 | service-contract |

`service` 只负责聚合构建。八个中立库直接继承 `innospots-nexus-parent`。
版本由根 `${revision}` 统一，所有库坐标登记在 `innospots-nexus-bom`。

## 框架适配

- `../innospots-nexus-spring/innospots-nexus-spring-service`：Spring 适配边界。
- `../innospots-nexus-quarkus/innospots-nexus-quarkus-service`：Quarkus 适配边界。

两个适配库继承各自框架 parent，组合 runtime、四个协议/传输模块与观测、治理模块。

- **Spring MVC / WebFlux**（`innospots-nexus-spring-service`）：`ServiceAutoConfiguration` / `ServiceWebFluxAutoConfiguration`、Servlet/WebFilter、错误映射、`AdapterScenarioMvcTest` / `AdapterScenarioWebFluxTest`。
- **Quarkus**（`innospots-nexus-quarkus-service` + deployment）：REST 过滤器、`ServiceExceptionMapper`、WebSockets Next、`AdapterScenarioQuarkusTest`。

后续增强项统一记录在 [docs/service-future-work.md](docs/service-future-work.md)（含 Quarkus 完整 build-time 处理、上传参数绑定等）。

### M9（二期）已交付

- **transfer**：`BinarySource`、`UploadResource`、`DownloadResource`、`DefaultDownloadPlanner`、`TransferConfig` 与 `UploadDownloadContractsTest`。
- **audit**：`AuditInterceptor` REQUIRED 准入拒绝 + BEST_EFFORT/REQUIRED 终态分发（`AuditEvents`）。
- **idempotency**：`InMemoryIdempotencyStore` + `IdempotencyCoordinator` 单 JVM 去重/冲突检测。
- **NDJSON**：`NdjsonStreamEncoder`（`application/x-ndjson` 单行 JSON）。
- **WS 细权限**：`MessageDescriptor.permissionKeys` + `InvocationEngine` 授权拦截器。

## 验证

```bash
# service 子树（含 adapter-test 契约测试）
mvn clean test -pl innospots-nexus-service -am

# 共享黑盒场景（需 Spring / Quarkus 宿主）
mvn test -pl innospots-nexus-spring/innospots-nexus-spring-service,innospots-nexus-quarkus/innospots-nexus-quarkus-service -am
```

## 边界约束

- 中立模块禁止依赖 Spring、Quarkus、Servlet、Reactor、Mutiny 或业务模块。
- runtime 通过 contract 扩展点接入观测和治理，不反向依赖其实现模块。
- 四个协议/传输模块互不强制依赖，可独立演进与测试。
- 文件内容复用 base 的 ResourceStore；core 的元数据与存储注册表由装配边界桥接。
- 审计事件和输出扩展属于服务契约，业务审计持久化与查询仍归 kernel / platform。
- 普通 HTTP 响应、WebSocket、流和文件分别保留协议语义，不统一强制 JSON 包装。
- 当前仅保留生产与测试包根，不预建领域层级或占位公共 API。
