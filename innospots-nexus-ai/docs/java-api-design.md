# Java 对外接口设计

## 1. 文档定位

- 状态：草案
- 范围：Java `/ai/**` 对外接口、鉴权、路由、代理、SSE 与错误
- 关联：[java-module-design.md](java-module-design.md)、[ai-runtime-api.md](ai-runtime-api.md)

Java `/ai/**` 是**唯一对外入口**；Node loopback API 不对外。

## 2. 路径与鉴权

| 项 | 规则 |
|----|------|
| 前缀 | `/ai` |
| 版本 | `/ai/v1/**` |
| 鉴权 | console Bearer |
| 匿名 | `/ai/v1/health`、`/health/live`、`/health/ready`、`/ai/v1/openapi.yaml` |
| Grant | GET → `ai.read`；POST/DELETE → `ai.write` |
| 授权 | `@EndpointGrant` + `consolePathPatterns` 包含 `/ai/**` |
| 审计 | 写操作 `auditLog=true`，`recordArgs=false` |
| 头 | 外部 `X-Nexus-*` 丢弃；注入 `X-Nexus-User` |

## 3. 本地状态端点

不代理 Node，Java 直接实现。

### 3.1 `GET /ai/v1/health/live`

```json
{ "status": "UP", "service": "innospots-nexus-ai" }
```

- 恒 200（JVM 存活）。
- 不检查 Node。

### 3.2 `GET /ai/v1/health/ready`

就绪：

```json
{ "status": "UP", "state": "RUNNING", "configVersion": "sha256:..." }
```

未就绪（503）：

```json
{
  "status": "DOWN",
  "reason": "runtime_starting",
  "state": "STARTING",
  "retryAfterSeconds": 2,
  "checkedAt": "2026-11-26T10:00:00Z"
}
```

条件：

- Node `RUNNING`
- 配置快照有效
- 最近健康检查通过

### 3.3 `GET /ai/v1/health`

```json
{
  "status": "UP",
  "service": "innospots-nexus-ai",
  "version": "0.1.0-SNAPSHOT",
  "checkedAt": "2026-11-26T10:00:00Z",
  "runtime": {
    "state": "RUNNING",
    "version": "0.1.0-SNAPSHOT",
    "nodeVersion": "22.19.0",
    "pid": 1234,
    "startedAt": "2026-11-26T09:59:58Z",
    "lastCheckAt": "2026-11-26T09:59:59Z",
    "latencyMs": 4,
    "error": null
  },
  "checks": {
    "process": "UP",
    "http": "UP",
    "config": "UP"
  }
}
```

- HTTP 200：UP
- HTTP 503：DEGRADED / DOWN
- `Cache-Control: no-store`
- 不返回密钥、配置正文、绝对路径。

## 4. 代理路由表

| 方法 | 路径 | Grant | 说明 |
|------|------|-------|------|
| GET | `/ai/v1/openapi.yaml` | 匿名 | Node OpenAPI |
| GET | `/ai/v1/agents` | `ai.read` | AgentProfile 列表 |
| POST | `/ai/v1/sessions` | `ai.write` | 创建会话 |
| GET | `/ai/v1/sessions` | `ai.read` | 当前用户会话 |
| GET | `/ai/v1/sessions/{id}` | `ai.read` | 详情 |
| DELETE | `/ai/v1/sessions/{id}` | `ai.write` | 删除 |
| GET | `/ai/v1/sessions/{id}/messages` | `ai.read` | 历史 |
| POST | `/ai/v1/sessions/{id}/messages` | `ai.write` | 发送/SSE |
| GET | `/ai/v1/runs/{id}` | `ai.read` | run 状态 |
| POST | `/ai/v1/runs/{id}/cancel` | `ai.write` | 取消 |

**不存在**：providers、models、config、tools、mcp、file-roots、runtime 的对外端点。

## 5. 代理规则

| 项 | 规则 |
|----|------|
| 路径映射 | `/ai/v1/**` → Node `/v1/**` |
| 方法 | 仅 GET/POST/DELETE |
| 路由匹配 | 精确 method+path；不在表内 404，不做前缀放行 |
| 请求头 | Accept、Content-Type、Last-Event-ID、X-Request-Id；丢弃 Authorization/Cookie/Host/hop-by-hop/外部 X-Nexus-* |
| 响应头 | Content-Type、Cache-Control、X-Request-Id、Retry-After；丢弃 Set-Cookie/Transfer-Encoding/Connection |
| 请求体 | 上限 8 MiB，流式计数，超限 413 |
| 普通响应 | 原状态码/体，不包装 |
| SSE | 逐块 flush；不缓冲、不压缩；断连关闭上游 |
| 超时 | 普通 60s；SSE 靠心跳/空闲检测 |
| 代理失败 | Node 风格错误体 |

## 6. SSE 契约

`POST /ai/v1/sessions/{id}/messages`：

```http
Accept: text/event-stream
Content-Type: application/json

{ "input": "Summarize this week" }
```

事件与 Node 一致：

```text
run_started
message_started
text_delta
thinking_delta
tool_started
tool_finished
message_completed
run_completed
error
heartbeat
```

- Java 不解析事件内容，只按帧转发。
- 客户端断开：关闭上游流；Node run 继续。
- 不可用时不会建立 SSE，直接 503。

## 7. 错误契约

### 7.1 代理层失败

```json
{
  "error": {
    "code": "runtime_unavailable",
    "message": "AI runtime is not ready",
    "requestId": "01J...",
    "details": { "state": "STARTING", "retryAfterSeconds": 2 }
  }
}
```

HTTP 503 + `Retry-After`。

### 7.2 console 认证/授权失败

由 console 异常映射返回 `R`，不进入代理。

### 7.3 Node 业务错误

原样透传 Node 错误体，不二次包装。

## 8. 可用性门禁

业务路由在转发前调用 `AiAvailabilityGuard.ensure()`：

| 状态 | 结果 |
|------|------|
| RUNNING + healthy | 转发 |
| STOPPED + LAZY | 尝试启动；成功转发，失败 503 |
| STOPPED + EAGER | 503 |
| STARTING | 503 `runtime_starting` |
| DEGRADED | 503 `runtime_unavailable` |
| FAILED | 503 `runtime_unavailable` |
| 配置无效 | 503 `config_invalid` |

`Retry-After` 与 `details.retryAfterSeconds` 同时返回。

## 9. 示例

```bash
# 健康
curl http://host/ai/v1/health/ready

# 会话列表
curl -H 'Authorization: Bearer <token>' http://host/ai/v1/sessions

# 创建会话
curl -X POST http://host/ai/v1/sessions \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"agent":"default","title":"demo"}'

# 流式对话
curl -N -X POST http://host/ai/v1/sessions/<id>/messages \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -H 'Accept: text/event-stream' \
  -d '{"input":"Hello"}'
```

## 10. OpenAPI

- Java 不生成 AI OpenAPI 索引。
- `/ai/v1/openapi.yaml` 透传 Node 生成内容。
- `/ai/v1/health*` 是 Java 本地端点，需在部署文档或网关文档中单独说明。
- 该例外已记录在 `AGENTS.md` 与总设计。

## 11. 测试

- 未登录 401；缺 Grant 403。
- 非白名单路径 404；非白名单方法 405。
- 头过滤：Authorization 不泄漏、外部 `X-Nexus-User` 被覆盖。
- SSE 帧完整、断连关闭上游。
- 503 + Retry-After 各状态。
- 大请求体 413。
- Node 返回 4xx/5xx 原样透传。
