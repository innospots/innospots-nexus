# Node Runtime API 设计

## 1. 文档定位

- 状态：草案
- 范围：`@innospots/nexus-ai-runtime` 的内部 HTTP API `/v1/**`
- 关联：[ai-runtime-design.md](ai-runtime-design.md)、[java-api-design.md](java-api-design.md)

该 API **只供 Java `/ai/**` 代理调用**，监听 `127.0.0.1`，不做鉴权。
对外契约由 Java 侧按最小路由表暴露。

## 2. 通用约定

| 项 | 约定 |
|----|------|
| Base path | `/v1` |
| 监听 | `127.0.0.1`，随机或固定端口 |
| 鉴权 | 无；依赖 loopback + Java 代理 |
| 编码 | `application/json; charset=utf-8` |
| 流式 | `text/event-stream` |
| 请求 ID | `X-Request-Id`，缺失时生成 |
| 用户 | `X-Nexus-User`，由 Java 注入；外部同名头在 Java 侧丢弃 |
| 大小限制 | 请求体、消息、工具结果均有上限 |
| 错误体 | `{error:{code,message,requestId,details}}` |

## 3. 路由表

| 方法 | 路径 | 对外 | 说明 |
|------|------|------|------|
| GET | `/v1/health` | 否 | 内部健康；Java 监控使用 |
| GET | `/v1/openapi.yaml` | 是 | 由 Node 生成，Java 透传 |
| GET | `/v1/agents` | 是 | AgentProfile 列表，脱敏 |
| POST | `/v1/sessions` | 是 | 创建会话 |
| GET | `/v1/sessions` | 是 | 按 owner 列出会话 |
| GET | `/v1/sessions/{id}` | 是 | 会话详情 |
| DELETE | `/v1/sessions/{id}` | 是 | 删除会话 JSONL |
| GET | `/v1/sessions/{id}/messages` | 是 | 历史消息分页 |
| POST | `/v1/sessions/{id}/messages` | 是 | 发送消息，SSE |
| GET | `/v1/runs/{id}` | 是 | run 状态 |
| POST | `/v1/runs/{id}/cancel` | 是 | 取消 run |

没有配置维护路由；没有 `/v1/providers`、`/v1/models`、`/v1/tools`、`/v1/mcp`、
`/v1/file-roots`、`/v1/config/*`。

## 4. 健康检查

`GET /v1/health`（内部）：

```json
{
  "status": "UP",
  "version": "0.1.0-SNAPSHOT",
  "node": "22.19.0",
  "configVersion": "sha256:...",
  "checks": {
    "config": "UP",
    "providers": "SKIPPED",
    "sessions": "UP"
  }
}
```

- `status`: `UP` / `DEGRADED`。
- HTTP 200 表示进程可响应；配置错误时返回 200 + `DEGRADED`，由 Java 判定 readiness。
- Java 不向外部透传 Node 端口与内部路径。

## 5. AgentProfile 列表

`GET /v1/agents`

```json
{
  "items": [
    {
      "name": "default",
      "displayName": "Default Agent",
      "model": "gpt-main",
      "capabilities": ["text", "tool"],
      "fileAccess": "read"
    }
  ]
}
```

- 不含 `systemPrompt`、`systemPromptFile`、密钥、FileRoot 绝对路径。
- 只返回 `enabled=true` 的 AgentProfile。
- 用于调用方选择 `agent` 创建会话。

## 6. 会话

### 6.1 创建

`POST /v1/sessions`

```json
{
  "agent": "default",
  "title": "Weekly report"
}
```

响应 `201`：

```json
{
  "id": "01J...",
  "agent": "default",
  "title": "Weekly report",
  "owner": "user-123",
  "createdAt": "2026-11-26T10:00:00Z",
  "messageCount": 0
}
```

规则：

- `owner` 来自 `X-Nexus-User`，缺失返回 401（仅 Java 会调用，正常不会缺失）。
- `agent` 必须是已加载且启用的 AgentProfile。
- 创建时追加 `nexus.session` custom entry，快照 agent、owner、FileRoot、权限、工具/MCP/Skill 生效集合。
- 不传 model；model 由 AgentProfile 决定。
- **不接受**调用方覆盖 tools、mcpServers、skills、fileAccess；只允许选择 `agent`。

### 6.2 列表

`GET /v1/sessions?limit=20&before=<cursor>`

```json
{
  "items": [
    {
      "id": "01J...",
      "title": "Weekly report",
      "agent": "default",
      "updatedAt": "2026-11-26T10:05:00Z",
      "messageCount": 12
    }
  ],
  "next": "01J..."
}
```

- 只返回当前 `owner` 的会话。
- 默认按 `updatedAt` 降序。
- 会话索引来自 JSONL 扫描缓存，不查数据库。

### 6.3 详情与删除

- `GET /v1/sessions/{id}`：owner 校验；不存在或非本人统一 404。
- `DELETE /v1/sessions/{id}`：owner 校验；删除对应 JSONL，清理内存索引与 run 状态。

### 6.4 历史消息

`GET /v1/sessions/{id}/messages?before=<cursor>&limit=50`

```json
{
  "items": [
    { "id": "a0b1c2d3", "role": "user", "content": "Hello", "timestamp": 1733234401000 },
    { "id": "b2c3d4e5", "role": "assistant", "content": [{"type":"text","text":"Hi"}], "timestamp": 1733234402000 }
  ],
  "next": "a0b1c2d3"
}
```

- 只返回当前分支（active leaf 回溯）的消息。
- 不返回 custom/system/tool 内部细节；工具结果以精简形式返回。
- 分页游标为 entry id。

## 7. 发送消息与 SSE

`POST /v1/sessions/{id}/messages`

```json
{
  "input": "Summarize this week",
  "options": {
    "thinkingLevel": "medium"
  }
}
```

- `Accept: text/event-stream` 或 `"stream": true` 时返回 SSE；否则返回最终消息 JSON。
- 同一会话已有运行中的 run → `409 run_conflict`。
- 发送前检查 owner、会话存在、AgentProfile 仍可用。
- 用户消息先写入 SessionManager，再启动 Agent。

### 7.1 SSE 帧

```text
event: run_started
data: {"runId":"...","sessionId":"...","messageId":"..."}

event: message_started
data: {"messageId":"...","role":"assistant"}

event: text_delta
data: {"delta":"..."}

event: thinking_delta
data: {"delta":"..."}

event: tool_started
data: {"toolCallId":"...","name":"file_read","arguments":{...}}

event: tool_finished
data: {"toolCallId":"...","isError":false,"preview":"..."}

event: message_completed
data: {"messageId":"...","usage":{...}}

event: run_completed
data: {"runId":"...","status":"succeeded","usage":{...}}
```

- 每 15s 发送 `heartbeat`。
- 客户端断开不取消 run；可用 `runId` 查询。
- `error` 事件后关闭流。
- 事件顺序由 `agent/event-bridge` 保证；tool 事件按完成顺序，消息按源顺序。

### 7.2 错误事件

```text
event: error
data: {"code":"upstream_rate_limited","message":"...","requestId":"..."}
```

## 8. Run

### 8.1 查询

`GET /v1/runs/{id}`

```json
{
  "runId": "01J...",
  "sessionId": "01J...",
  "status": "running",
  "startedAt": "2026-11-26T10:00:00Z",
  "finishedAt": null,
  "usage": null,
  "error": null
}
```

- run 状态只在内存；进程重启后不再可查。
- owner 校验通过会话归属间接完成。

### 8.2 取消

`POST /v1/runs/{id}/cancel`

- 透传 `AbortSignal` 给 pi Agent。
- 已产生的 assistant/toolResult 保留，assistant 标记 `stopReason=aborted`。
- 重复取消幂等。

## 9. 错误码

| HTTP | code | 语义 |
|------|------|------|
| 400 | `invalid_request` | 参数/JSON 非法 |
| 401 | `owner_required` | 缺少 `X-Nexus-User` |
| 404 | `session_not_found` / `agent_not_found` / `run_not_found` | 资源不存在或非本人 |
| 409 | `run_conflict` | 会话已有运行中的 run |
| 413 | `payload_too_large` | 请求体/消息超限 |
| 429 | `upstream_rate_limited` | 模型供应商限流 |
| 502 | `upstream_auth_failed` / `tool_failed` | 上游鉴权/工具失败 |
| 507 | `storage_unavailable` | 磁盘/会话写入失败 |

错误体：

```json
{
  "error": {
    "code": "run_conflict",
    "message": "Session already has an active run",
    "requestId": "01J...",
    "details": { "runId": "01J..." }
  }
}
```

## 10. 限制与超时

| 项 | 默认 |
|----|------|
| 请求体 | 1 MiB（会话配置可调） |
| 消息长度 | 1 MiB |
| 单 run 超时 | 300s |
| 最大轮次 | 12 |
| 最大工具调用 | 100 |
| SSE 心跳 | 15s |
| 列表分页 | 默认 20，最大 100 |

## 11. OpenAPI

- Node 根据路由表生成 `/v1/openapi.yaml`。
- Java 透传为 `/ai/v1/openapi.yaml`。
- 配置类路由不存在，因此不会出现在 API 文档中。
- OpenAPI 只描述对外路由；`/v1/health`、`/v1/runtime` 标记 `internal`。

## 12. 调用示例

```bash
# 列 Agent
curl http://127.0.0.1:<port>/v1/agents -H 'X-Nexus-User: user-123'

# 创建会话
curl -X POST http://127.0.0.1:<port>/v1/sessions \
  -H 'Content-Type: application/json' \
  -H 'X-Nexus-User: user-123' \
  -d '{"agent":"default","title":"demo"}'

# 流式发送
curl -N -X POST http://127.0.0.1:<port>/v1/sessions/<id>/messages \
  -H 'Content-Type: application/json' \
  -H 'Accept: text/event-stream' \
  -H 'X-Nexus-User: user-123' \
  -d '{"input":"Hello"}'
```

> 上述直接调用仅用于本机调试；生产环境必须经 Java `/ai/v1/**`。
