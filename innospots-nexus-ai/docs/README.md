# Nexus AI 开发设计文档索引

## 文档定位

- 状态：草案（与 `ai-runtime-design.md` 同步）
- 日期：2026-11-26
- 归属：`innospots-nexus-ai`
- 上位约束：[AGENTS.md](../../AGENTS.md)

`ai-runtime-design.md` 是总方案；本目录下的其余文档是对各子系统的详细开发设计。
若出现冲突，以 `ai-runtime-design.md` 与 `AGENTS.md` 为准，并回写修正。

## 阅读顺序

```text
1. ai-runtime-design.md          总体方案、边界、关键决策
2. ai-runtime-modules.md         Node runtime 模块与功能
3. ai-runtime-config.md          配置来源、YAML、合并、密钥、重载
4. ai-runtime-api.md             Node 内部 API、SSE、错误
5. ai-runtime-storage.md         会话 JSONL、审计、状态
6. ai-runtime-operations.md      CLI、启动、健康、发布、部署
7. java-module-design.md         Java 模块、类、调用链
8. java-api-design.md            Java 对外接口、鉴权、代理契约
9. java-runtime-supervisor.md    Java 进程管理、健康检查、可用性
10. java-configuration.md        Java 配置项与装配
```

## 文档清单

| 文档 | 范围 | 主要读者 |
|------|------|----------|
| [ai-runtime-design.md](ai-runtime-design.md) | 总方案、边界、关键决策、完整路线图 | 全体 |
| [ai-runtime-modules.md](ai-runtime-modules.md) | Node 运行时目录、模块职责、函数边界、数据流 | Node 开发 |
| [ai-runtime-config.md](ai-runtime-config.md) | ConfigSource、YAML 多文件、优先级、Schema、密钥、重载 | Node 开发 / 运维 |
| [ai-runtime-api.md](ai-runtime-api.md) | Node `/v1` 内部 API、SSE、错误码、调用语义 | Node / Java 开发 |
| [ai-runtime-storage.md](ai-runtime-storage.md) | SessionManager JSONL、会话元数据、审计、运行状态 | Node 开发 |
| [ai-runtime-operations.md](ai-runtime-operations.md) | CLI、环境变量、启动输出、信号、发布与版本 | 开发 / 运维 |
| [java-module-design.md](java-module-design.md) | Java 包结构、核心类、调用链、装配要求 | Java 开发 |
| [java-api-design.md](java-api-design.md) | `/ai/**` 对外接口、鉴权、路由、SSE、错误 | Java / 前端 / 调用方 |
| [java-runtime-supervisor.md](java-runtime-supervisor.md) | 进程生命周期、版本检查、健康监控、503 门禁 | Java 开发 / 运维 |
| [java-configuration.md](java-configuration.md) | `AiRuntimeConfig`、`AiProxyConfig`、环境与示例 | Java 开发 / 运维 |

## 固定术语

| 术语 | 含义 |
|------|------|
| Node runtime | `@innospots/nexus-ai-runtime`，独立 Node 进程，AI 契约与实现所有者 |
| Java module | `com.innospots:innospots-nexus-ai`，thin jar，supervisor + `/ai/**` 代理 |
| AgentProfile | Nexus 的配置资源，映射为 pi-agent-core 的 Agent 构造参数；不是 pi 官方规范 |
| Session | pi `SessionManager` 的 v3 JSONL 会话；唯一运行时可变数据 |
| ConfigSource | Node 侧只读配置来源接口；优先于本地 YAML |
| 可用性门禁 | Java `AiAvailabilityGuard`，在代理前判断 runtime 是否就绪 |

## 关键约束

- Java 是唯一对外入口；Node 只监听 loopback，不做鉴权。
- 对外只暴露最小调用面；配置与诊断端点不反代。
- 配置只读加载，不做配置维护 API。
- 不引入 SQLite 或关系型数据库。
- npm 包与 jar 版本必须一致。
- 任何设计变更先更新 `ai-runtime-design.md`，再同步本目录。
