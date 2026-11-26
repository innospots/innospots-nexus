# Node Runtime 模块与功能设计

## 1. 文档定位

- 状态：草案
- 范围：`@innospots/nexus-ai-runtime` 的源码目录、模块职责、函数边界与数据流
- 关联：[ai-runtime-design.md](ai-runtime-design.md)、[ai-runtime-api.md](ai-runtime-api.md)、[ai-runtime-config.md](ai-runtime-config.md)

本文只描述 **Node runtime**，不包含 Java 代理层。

## 2. 目录结构

```text
runtime/
├── package.json
├── pnpm-lock.yaml
├── tsconfig.json
├── bin/
│   └── nexus-ai-runtime.mjs        # CLI 入口（npm bin）
├── config/
│   ├── runtime.yaml
│   └── conf.d/*.yaml
└── src/
    ├── main.ts                     # 参数解析、启动、信号
    ├── server/                     # HTTP、路由、SSE、错误
    ├── agent/                      # Agent 工厂、run 管理、事件桥接
    ├── model/                      # provider/model 注册、凭证、校验
    ├── tool/                       # HTTP / MCP / 文件工具
    ├── session/                    # SessionManager 适配、索引、上下文桥接
    ├── config/                     # ConfigSource、YAML、合并、校验、重载
    ├── security/                   # env 清洗、PathGuard、主机白名单
    ├── audit/                      # 文件操作 JSONL 审计
    └── health/                     # 内部健康状态
```

## 3. 模块总览

| 模块 | 职责 | 不负责 |
|------|------|--------|
| `main` | CLI、参数、启动顺序、信号、退出码 | 业务逻辑、HTTP 路由 |
| `server` | HTTP 监听、路由、SSE、请求限制、错误封装 | Agent 推理、配置解析 |
| `agent` | 构造 pi Agent、执行 run、事件转发、取消 | provider 凭证、会话落盘 |
| `model` | Provider/Model 配置到 pi-ai 的映射、密钥解析、启动校验 | 模型调用循环 |
| `tool` | 工具注册、HTTP 工具、MCP 客户端、文件工具 | 配置加载、鉴权 |
| `session` | SessionManager、JSONL、会话索引、上下文桥接 | 进程生命周期 |
| `config` | ConfigSource、YAML 多文件、合并、校验、监听 | HTTP 管理接口 |
| `security` | env 白名单、文件路径沙箱、HTTP 主机白名单 | 用户认证 |
| `audit` | 文件操作审计 JSONL | 审计查询 API |
| `health` | 进程内状态、配置版本、检查结果 | Java 对外健康端点 |

## 4. 模块详述

### 4.1 `main`：CLI 与启动

**文件**

```text
src/main.ts
src/runtime/options.ts
src/runtime/startup.ts
src/runtime/shutdown.ts
```

**职责**

1. 解析 CLI 参数与环境变量，生成不可变 `RuntimeOptions`。
2. 校验 Node 版本、目录可写、配置可加载。
3. 初始化 Config、Session、Tool、Agent、HTTP 模块。
4. 打印 readiness JSON，原子写 `--ready-file`。
5. 注册 `SIGTERM` / `SIGINT` / `SIGHUP`。
6. 返回稳定退出码。

**关键契约**

```ts
interface RuntimeOptions {
  host: "127.0.0.1" | "::1";
  port: number;                 // 0 = ephemeral
  dataDir: string;              // default ~/.nexus-ai
  configDir: string;            // default <dataDir>/config
  configUrl?: string;
  readyFile: string;            // default <dataDir>/runtime.json
  pidFile?: string;
  logLevel: "debug" | "info" | "warn" | "error";
  logFormat: "json" | "pretty";
  watchConfig: boolean;
}

async function start(options: RuntimeOptions): Promise<RunningRuntime>;
async function shutdown(runtime: RunningRuntime, reason: string): Promise<void>;
```

**启动顺序**

```text
parse options
  → check Node version
  → ensure dataDir / configDir
  → load ConfigSnapshot
  → init SessionStore / ToolRegistry / ModelRegistry
  → bind HTTP (127.0.0.1)
  → print readiness + write ready file
  → start signal handlers
```

**失败约束**

- 参数/配置错误：退出码 2。
- Node 版本不足：退出码 3。
- 端口绑定失败：退出码 4。
- 其他启动失败：退出码 5。
- ConfigSource 失败：退出码 6。

### 4.2 `server`：HTTP 与 SSE

**文件**

```text
src/server/http.ts
src/server/router.ts
src/server/routes/*.ts
src/server/sse.ts
src/server/errors.ts
src/server/limits.ts
```

**职责**

- 监听 `127.0.0.1`，只暴露内部 API `/v1/**`。
- 请求 ID、JSON 解析、请求体大小、超时。
- 路由：health、openapi、agents、sessions、messages、runs。
- SSE：事件编码、心跳、客户端断连清理。
- 错误统一为 `{error:{code,message,requestId,details}}`。

**不负责**

- 鉴权（由 Java 完成）。
- Provider 推理（交给 `agent`）。
- 配置维护（没有写接口）。

**关键契约**

```ts
interface RequestContext {
  requestId: string;
  owner?: string;          // X-Nexus-User，来自 Java loopback
  signal: AbortSignal;
}

type RouteHandler = (ctx: RequestContext, req: Request) => Promise<Response>;
```

**路由清单**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/health` | 内部健康状态，仅 Java 监控使用 |
| GET | `/v1/openapi.yaml` | 生成的 OpenAPI |
| GET | `/v1/agents` | AgentProfile 列表（脱敏） |
| POST | `/v1/sessions` | 创建会话 |
| GET | `/v1/sessions` | 按 owner 列出会话 |
| GET | `/v1/sessions/{id}` | 会话详情 |
| DELETE | `/v1/sessions/{id}` | 删除会话 JSONL |
| GET | `/v1/sessions/{id}/messages` | 历史消息 |
| POST | `/v1/sessions/{id}/messages` | 发送消息，可 SSE |
| GET | `/v1/runs/{id}` | run 状态 |
| POST | `/v1/runs/{id}/cancel` | 取消 run |

### 4.3 `agent`：Agent 与 Run

**文件**

```text
src/agent/agent-factory.ts
src/agent/run-manager.ts
src/agent/event-bridge.ts
src/agent/prompt-builder.ts
```

**职责**

1. 根据 `AgentProfile` 解析模型、工具、system prompt（内联/文件/PromptTemplate/Skill）。
2. 计算该 Agent 的生效集合：skills、HTTP tools、MCP tools、file tools；全局定义不自动继承。
3. 用 `SessionManager.buildSessionContext()` 构造 pi-agent-core `Agent`。
3. 执行 `prompt()`，把事件映射为 SSE。
4. 管理 run 生命周期、并发、取消、超时。
5. 把 finalized 消息追加回 SessionManager。

**关键契约**

```ts
interface AgentRun {
  runId: string;
  sessionId: string;
  owner: string;
  status: "running" | "succeeded" | "aborted" | "failed";
  abort: AbortController;
  startedAt: number;
}

interface RunManager {
  start(sessionId: string, input: string, options: PromptOptions): AsyncIterable<RuntimeEvent>;
  cancel(runId: string): void;
  get(runId: string): AgentRun | undefined;
}
```

**约束**

- 每个会话同一时刻最多一个 run。
- run 状态只存内存；进程重启不恢复未完成 run。
- 取消时保留已产生的 assistant / toolResult，并标记 `aborted`。
- Agent 不使用 `createAgentSession()`，不加载 coding-agent 资源。

### 4.4 `model`：Provider 与 Model

**文件**

```text
src/model/provider-registry.ts
src/model/model-registry.ts
src/model/credential-resolver.ts
src/model/validation.ts
```

**职责**

- 将 YAML 的 `Provider` / `Model` 映射为 pi-ai provider 与 model。
- 解析 `fromEnv` / `secretRef`，不读取宿主 provider 环境变量。
- 提供 AgentProfile 的模型查找。
- 启动时做配置完整性校验；不提供 probe API。

**支持 provider**

```text
openai, anthropic, google, deepseek, openrouter, openai-compatible
```

**关键契约**

```ts
interface ProviderRegistry {
  register(def: ProviderDefinition): void;
  get(name: string): ProviderDefinition | undefined;
  resolveApiKey(name: string): Promise<string>;
}

interface ModelRegistry {
  register(def: ModelDefinition): void;
  get(name: string): ModelDefinition | undefined;
  resolveForAgent(profile: AgentProfile): ResolvedModel;
}
```

### 4.5 `tool`：工具

**文件**

```text
src/tool/registry.ts
src/tool/http-tool.ts
src/tool/mcp-client.ts
src/tool/file/read.ts
src/tool/file/list.ts
src/tool/file/write.ts
src/tool/file/edit.ts
src/tool/file/delete.ts
src/tool/file/path-guard.ts
```

**职责**

- 按 AgentProfile 的 `tools` / `mcpServers` / `fileAccess` 计算每-Agent 生效集合。
- 全局 Tool/McpServer 只是定义，不自动对所有 Agent 生效。
- HTTP 工具：域名白名单、私网拦截、超时、响应大小。
- MCP：仅 `http` / `sse`，工具名 `mcp__<server>__<tool>`。
- 文件工具：只作用于会话快照的 FileRoot；默认关闭。
- 文件操作写审计。

**工具清单**

| 工具 | 说明 |
|------|------|
| `file_read` | UTF-8 文本、按行读取 |
| `file_list` | 目录列表、不跟随符号链接 |
| `file_write` | 新建/覆盖，临时文件 + rename |
| `file_edit` | 精确替换，默认唯一匹配 |
| `file_delete` | 文件/空目录，软删除到 `<dataDir>/trash` |

**约束**

- 不提供 shell、代码执行、任意路径访问。
- `fileAccess=none` 时不注册任何文件工具。
- `allowDelete=false` 时不注册 `file_delete`。
- 路径校验统一走 `PathGuard`，禁止 `..`、绝对路径、越界符号链接和设备文件。

### 4.6 `session`：会话与上下文

**文件**

```text
src/session/session-manager-adapter.ts
src/session/session-index.ts
src/session/context-bridge.ts
src/session/session-metadata.ts
```

**职责**

1. 封装 `SessionManager`（create/open/list）。
2. 固定 `sessionDir=<dataDir>/sessions`、`virtualCwd=<dataDir>/workspace`。
3. 写入 `nexus.session` custom entry（agent、owner、文件根快照）。
4. 维护进程内会话索引，按文件 mtime/大小失效。
5. 把 SessionManager 上下文桥接给 pi-agent-core。

**关键契约**

```ts
interface SessionStore {
  create(owner: string, agent: string, title?: string): SessionHandle;
  open(sessionId: string, owner: string): SessionHandle;
  list(owner: string, page: Page): SessionSummary[];
  delete(sessionId: string, owner: string): void;
  history(sessionId: string, owner: string, page: Page): MessagePage;
}
```

**约束**

- SessionManager 是会话正文与模型上下文的唯一真源。
- 不写 SQLite，不另建消息表。
- 会话列表按 owner 过滤；owner 由 Java 注入的 `X-Nexus-User` 提供。
- 会话删除只删除对应 JSONL。

### 4.7 `config`：配置加载

**文件**

```text
src/config/config-source.ts
src/config/file-source.ts
src/config/http-source.ts
src/config/static-source.ts
src/config/loader.ts
src/config/merger.ts
src/config/validator.ts
src/config/watcher.ts
src/config/secrets.ts
```

**职责**

- 定义 `ConfigSource` 只读接口。
- 读取本地 YAML 多文件/多文档。
- 可选 HTTP 来源（`NEXUS_AI_CONFIG_URL`），优先于本地。
- 按 `(kind, metadata.name)` 合并与校验。
- 生成不可变 `ConfigSnapshot`（含 PromptTemplate）。
- `fs.watch` / `SIGHUP` 触发热重载。
- 解析 `fromEnv` / `secretRef`，拒绝明文密钥。

**关键契约**

```ts
interface ConfigSource {
  readonly name: string;
  readonly priority: number;
  load(): Promise<ConfigDocument[]>;
  watch?(onChange: () => void): () => void;
}

interface ConfigSnapshot {
  version: string;
  runtime: RuntimeDefinition;
  providers: Map<string, ProviderDefinition>;
  models: Map<string, ModelDefinition>;
  agents: Map<string, AgentProfile>;
  promptTemplates: Map<string, PromptTemplate>;
  skills: Map<string, SkillDefinition>;
  tools: Map<string, ToolDefinition>;
  mcpServers: Map<string, McpServerDefinition>;
  fileRoots: Map<string, FileRootDefinition>;
  sources: SourceInfo[];
}
```

**不做**

- 不提供配置 CRUD。
- 不写本地配置文件。
- 不使用 SQLite。

### 4.8 `security`：隔离

**文件**

```text
src/security/env-sanitizer.ts
src/security/host-allowlist.ts
src/security/path-guard.ts
```

**职责**

| 能力 | 说明 |
|------|------|
| env 清洗 | 只构造白名单 env，不继承宿主 provider 变量 |
| 主机白名单 | HTTP 工具目标域名/IP 校验，拒绝私网/元数据地址 |
| 路径沙箱 | 相对路径、realpath、符号链接、设备文件校验 |

**约束**

- Node 只监听 loopback。
- 不接受远程绑定参数。
- 文件工具的根不能位于 `<dataDir>` 内。

### 4.9 `audit`：文件操作审计

**文件**

```text
src/audit/file-audit.ts
```

**职责**

- 每次文件操作（含拒绝/失败）追加到
  `<dataDir>/audit/file-ops-YYYYMMDD.jsonl`。
- 记录：时间、session、run、agent、root、op、相对路径、字节数、结果、错误码。
- 不记录文件内容。

### 4.10 `health`：内部健康

**文件**

```text
src/health/health-service.ts
src/health/checks.ts
```

**职责**

- 提供 `/v1/health`：status、version、node、configVersion、checks。
- 报告配置加载、provider 校验、会话目录可写等状态。
- 不对外；由 Java supervisor 轮询。

## 5. 跨模块流程

### 5.1 启动

```text
main
  → config.loader.load()
  → config.merger.merge()
  → config.validator.validate()
  → model.registry.register()
  → tool.registry.register()
  → session.store.init()
  → server.listen()
  → health.ready
```

### 5.2 创建会话

```text
HTTP POST /v1/sessions
  → auth context: owner
  → resolve AgentProfile + capabilities
  → session.store.create(owner, agent, title)
  → SessionManager.create(virtualCwd, sessionDir)
  → appendCustomEntry("nexus.session", { agentProfileHash, skills, tools, mcpServers, fileRoot... })
  → return summary
```

### 5.3 发送消息（SSE）

```text
POST /v1/sessions/{id}/messages
  → session.open + owner check
  → runManager.start()
  → buildSessionContext()
  → Agent.prompt()
  → eventBridge → SSE events
  → appendMessage(user/assistant/toolResult)
  → update in-memory summary
```

### 5.4 工具调用

```text
Agent tool call
  → registry.resolve(name)
  → http / mcp / file executor
  → result or throw
  → toolResult message
```

### 5.5 文件操作

```text
file tool
  → PathGuard.resolve(root, relativePath)
  → stat/lstat/realpath checks
  → read/write/edit/delete
  → audit.append()
```

### 5.6 配置重载

```text
fs.watch / SIGHUP
  → loader.load()
  → merge + validate
  → success: swap ConfigSnapshot
  → failure: keep old snapshot + log error
```

## 6. 并发与线程模型

- Node 单进程、事件循环；DB/配置/会话无跨进程写。
- 每个会话同一时刻最多一个 run。
- run 取消通过 `AbortController`。
- 文件变更操作按 root 加进程内互斥。
- 配置重载与请求并发安全：快照不可变，请求持有旧快照直到结束。
- 健康检查与业务请求共享快照缓存。

## 7. 模块边界

| 禁止 | 原因 |
|------|------|
| `server` 直接操作 SessionManager | 会话逻辑集中在 `session` |
| `agent` 直接读 YAML | 配置由 `config` 提供快照 |
| `tool` 直接改配置 | 工具只消费快照 |
| `session` 调用 HTTP | 会话层不感知网络 |
| 任何模块写 SQLite | 轻量、无关系库 |
| 任何模块读取 `~/.pi` | 完全隔离 |

## 8. 测试边界

| 模块 | 测试重点 |
|------|----------|
| `main` | 参数优先级、退出码、信号 |
| `server` | 路由、限制、错误体、SSE 帧 |
| `agent` | run 冲突、取消、事件顺序 |
| `model` | provider 映射、凭证解析、校验失败 |
| `tool` | HTTP 白名单、MCP 发现、文件沙箱 |
| `session` | JSONL 重开、owner 过滤、索引失效 |
| `config` | 多文件/多文档、优先级、重复报错、重载 |
| `security` | env 清洗、路径越界、私网拦截 |
| `audit` | 记录字段、拒绝也记录、无正文 |
| `health` | configVersion、状态转换 |
