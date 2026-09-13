# 统一服务接入与治理

当前为 Maven 工程骨架：已注册模块与依赖，尚未实现接口、运行时和框架适配。
模块划分由开发者于 2026-09-13 确认。

可实施技术设计见 [docs/README.md](docs/README.md)。业务开发用法见 [docs/service-developer-experience-design.md](docs/service-developer-experience-design.md)。

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

两个适配库继承各自框架 parent，组合四个协议/传输模块与观测、治理模块；
不重复声明传递可达的 runtime、contract、base。当前没有添加框架 SDK 或自动配置，
也没有让已有 app / console 应用自动启用服务能力。具体适配实现时按使用情况引入 BOM 管理的依赖。

## 边界约束

- 中立模块禁止依赖 Spring、Quarkus、Servlet、Reactor、Mutiny 或业务模块。
- runtime 通过 contract 扩展点接入观测和治理，不反向依赖其实现模块。
- 四个协议/传输模块互不强制依赖，可独立演进与测试。
- 文件内容复用 base 的 ResourceStore；core 的元数据与存储注册表由装配边界桥接。
- 审计事件和输出扩展属于服务契约，业务审计持久化与查询仍归 kernel / platform。
- 普通 HTTP 响应、WebSocket、流和文件分别保留协议语义，不统一强制 JSON 包装。
- 当前仅保留生产与测试包根，不预建领域层级或占位公共 API。
