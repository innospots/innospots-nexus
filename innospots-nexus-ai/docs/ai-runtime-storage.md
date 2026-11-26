# Node Runtime 持久化设计

## 1. 文档定位

- 状态：草案
- 范围：会话 JSONL、会话元数据、会话索引、用量、审计、run 状态
- 关联：[ai-runtime-design.md](ai-runtime-design.md)、[ai-runtime-api.md](ai-runtime-api.md)

pi 原生不使用 SQLite；本模块也不引入关系型数据库。**唯一运行时可变数据是会话**。

## 2. 存储总览

| 数据 | 位置 | 生命周期 | 真源 |
|------|------|----------|------|
| 会话正文 | `<dataDir>/sessions/*.jsonl` | 持久 | SessionManager |
| 会话元数据 | 同一 JSONL 的 `custom` / `session_info` entry | 持久 | SessionManager |
| 会话索引 | 进程内缓存 | 进程 | 可重建 |
| 文件操作审计 | `<dataDir>/audit/file-ops-YYYYMMDD.jsonl` | 持久 | 审计文件 |
| run 状态 | 进程内存 Map | 进程 | 无持久化 |
| 配置 | ConfigSource / YAML | 只读 | 配置文件 |
| 密钥 | `<dataDir>/secrets/*` | 持久 | 密钥文件 |

**没有 `ai_*` 表，没有 `node:sqlite`。**

## 3. 会话文件

### 3.1 位置与命名

```text
<dataDir>/sessions/<timestamp>_<session-id>.jsonl
```

- `sessionDir` 固定为 `<dataDir>/sessions`，必须显式传给 SessionManager。
- `virtualCwd` 固定为 `<dataDir>/workspace`，避免泄露宿主路径。
- 禁止回退到 `~/.pi/agent/sessions`。

### 3.2 格式

- 首行 `session` header：`{"type":"session","version":3,...}`。
- 后续 entry 通过 `id`/`parentId` 组成 append-only 树。
- 原生 entry 类型：`message`、`model_change`、`thinking_level_change`、`usage`、
  `compaction`、`context_edit`、`branch_summary`、`custom`、`custom_message`、
  `label`、`session_info`。
- 全部读写通过 SessionManager 公开方法，不自行改 entry 结构。

### 3.3 `nexus.session` 元数据

创建会话时追加：

```json
{
  "type": "custom",
  "customType": "nexus.session",
  "id": "8hex",
  "parentId": null,
  "timestamp": "2026-11-26T10:00:00.000Z",
  "data": {
    "agent": "default",
    "owner": "user-123",
    "agentProfileHash": "sha256:...",
    "promptTemplate": "default-assistant",
    "promptHash": "sha256:...",
    "skills": ["pdf-tools"],
    "tools": ["http.echo", "mcp__docs__search"],
    "mcpServers": ["docs"],
    "fileRoot": "workspace",
    "fileRootPath": "/srv/nexus/workspace",
    "fileAccess": "read_write",
    "allowDelete": false
  }
}
```

- `owner` 由 Java 注入的 `X-Nexus-User` 提供。
- `agentProfileHash` 标识创建会话时的 AgentProfile 生效集合；之后 AgentProfile 变更不影响已有会话。
- 工具/MCP 名称集合是快照；具体实现按名称从当前全局配置解析，定义删除则工具不可用。
- FileRoot 路径与权限是**创建时快照**；后续配置变更不影响已有会话。
- 元数据 entry 不参与 LLM 上下文。

### 3.4 标题

- 使用 `appendSessionInfo(name)` 写入 `session_info`。
- 不另建标题字段或索引表。

## 4. 会话索引

### 4.1 构建

```text
启动 / 首次列表
  → SessionManager.list(virtualCwd, sessionDir)
  → 读取每个文件的 header + nexus.session + session_info
  → 构建 Map<sessionId, SessionSummary>
  → 按文件 mtime/size 记录版本
```

`SessionSummary`：

```ts
interface SessionSummary {
  id: string;
  owner: string;
  agent: string;
  title?: string;
  messageCount: number;
  usage?: UsageSummary;
  createdAt: number;
  updatedAt: number;
  filePath: string;
  fileMtime: number;
  fileSize: number;
}
```

### 4.2 失效

- 文件 mtime 或 size 变化时重建该条索引。
- 会话删除时移除。
- 索引丢失可从 JSONL 全量重建，不丢数据。
- 不写索引文件，不写数据库。

### 4.3 列表语义

- 只返回 `owner` 匹配的会话。
- 按 `updatedAt` 降序。
- 分页使用 entry id 或时间游标。
- 缺少 `nexus.session` 的旧文件：owner 视为空，只有管理员/迁移工具可见。

## 5. 用量

- 以 assistant message 的 `usage` 与 `usage` entry 为准。
- 进程内按会话累计，提供列表/详情展示。
- 不落库；重启后重新扫描 JSONL 计算。
- 成本字段按 provider 返回或 pi-ai 目录价格。

## 6. 删除

- `DELETE /v1/sessions/{id}`：owner 校验后删除对应 JSONL。
- 删除前若存在运行中的 run：先 abort，再删除。
- 物理删除文件；V1 不提供会话回收站。
- 文件操作软删除的 trash 与会话删除无关。

## 7. 审计

### 7.1 位置

```text
<dataDir>/audit/file-ops-YYYYMMDD.jsonl
```

### 7.2 记录

```json
{
  "ts": "2026-11-26T10:00:00.000Z",
  "sessionId": "01J...",
  "runId": "01J...",
  "agent": "default",
  "root": "workspace",
  "op": "file_write",
  "path": "reports/week.md",
  "bytesIn": 1200,
  "bytesOut": 0,
  "result": "ok",
  "errorCode": null
}
```

- 拒绝与失败也记录，`result=denied|error`。
- 不记录文件内容、密钥、绝对路径（只记录相对路径）。
- 查询时按日期流式读取；不建审计表。
- 保留策略由部署方按文件清理。

## 8. Run 状态

```ts
interface RunState {
  runId: string;
  sessionId: string;
  owner: string;
  status: "running" | "succeeded" | "aborted" | "failed";
  startedAt: number;
  finishedAt?: number;
  usage?: UsageSummary;
  error?: { code: string; message: string };
}
```

- 仅内存 Map；进程重启后不恢复未完成 run。
- 已产生的消息仍在 JSONL，重启后可继续会话。
- `GET /v1/runs/{id}` 只对当前进程有效；404 表示不存在或已重启。

## 9. 并发

- 单 Node 进程；SessionManager 单写入者。
- 同一会话最多一个 run；不同会话可并行（受 `maxConcurrentRuns` 限制）。
- 同一 JSONL 的 append 由 SessionManager 串行化。
- 会话删除与 run 并发：删除前 abort，并等待 run 结束。
- 文件操作按 FileRoot 加进程内互斥。

## 10. 故障与恢复

| 场景 | 处理 |
|------|------|
| JSONL 行损坏 | 跳过坏行并记录；会话仍可打开到最后有效 entry |
| 文件被外部删除 | 索引失效，API 返回 404 |
| 磁盘写满 | 返回 507；不破坏已有文件 |
| 进程崩溃 | 未完成 run 丢失；已 append 的消息保留 |
| 配置重载 | 不影响已有 JSONL |
| 会话目录不可写 | 启动失败或健康 DEGRADED |

## 11. 迁移

- 会话版本由 SessionManager 自动迁移（v1/v2 → v3）。
- 不引入自定义数据库迁移。
- `nexus.session` 缺失时按旧会话处理，不阻断读取。
- 未来增加租户/工作区字段时，写入新的 custom entry，不修改 header。

## 12. 测试要点

- 创建 → 发送 → 重开 JSONL，上下文一致。
- owner 过滤；跨用户读取 404。
- 索引缓存失效与重建。
- 删除会话清理索引与 run。
- 审计字段完整，拒绝也记录。
- 进程重启后 run 404、会话可继续。
- 无 SQLite 文件产生。
