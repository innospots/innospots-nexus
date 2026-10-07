# 统一服务框架 — 设计文档索引

本目录描述 `innospots-nexus-service` 中立库与 Spring / Quarkus 适配层的**架构、契约与运行语义**。实现状态以仓库根 [README.md](../README.md) 为准；**尚未交付或仅部分落地的能力**见 [service-future-work.md](service-future-work.md)。

## 按角色阅读

| 角色 | 建议路径 |
|------|----------|
| **业务开发** | 根 [README 快速接入](../README.md) → 各子模块 README → [开发体验](service-developer-experience-design.md) |
| **接入 Spring / Quarkus** | [适配与配置](service-adapter-design.md) → 对应 `innospots-nexus-spring-service` / `quarkus-service` README |
| **扩展 SPI / 改运行时** | [总览与边界](service-framework-design.md) → [契约与类型](service-contract-design.md) → [运行时与协议](service-runtime-design.md) |
| **验收与测试** | [模块清单与验收](service-implementation-design.md) |

## 文档地图

```text
service-framework-design.md     目标、模块边界、关键决策（D1–D15）、不在范围内能力
        │
        ├── service-developer-experience-design.md   四级用法、注解落点、禁止项
        ├── service-contract-design.md               类型、SPI、状态码
        ├── service-runtime-design.md                调用链、流/WS/文件、治理与审计语义
        ├── service-adapter-design.md                装配、配置键、宿主差异
        └── service-implementation-design.md         模块源文件索引、测试与需求验收

service-future-work.md          待完善与明确不做的能力（单独维护）
```

## 核心概念（一句话）

- **宿主**负责路由、Web 容器、DI、JSON；**中立库**提供调用上下文、执行链、HTTP/流/WS/文件语义、观测与本地治理。
- 业务公开 API 只用 `T`、`CompletionStage<T>`、`Flow.Publisher<T>`，不暴露 Reactor、Servlet、OTel 实现类型。
- 失败统一 `NexusException` + `StatusCode`；默认 HTTP 响应形态为 legacy `R<T>`，可按 operation 选用 Problem Details。

## 验证

结构或 Java 变更后，在 JDK 支持 release 的前提下：

```bash
mvn clean compile
mvn test
```
