# innospots-nexus-ai

基于 [`pi-agent-core`](https://github.com/badlogic/pi-mono) 的 AI Agent 运行时模块。

- **AI API 由独立 Node 进程提供**，拥有自己的 REST/SSE 契约；Node 只监听 loopback。
- 会话正文直接使用 pi 原生 **`SessionManager`** 的 v3 JSONL。
- 模型/AgentProfile/工具/文件根配置**只读加载**：`ConfigSource` 接口优先，本地 YAML 多文件兜底；
  无配置维护 API，不使用 SQLite。
- **Java 侧是唯一对外入口**：`/ai/**` 透明反向代理 Node API，统一完成 console Bearer 鉴权、
  `ai.read` / `ai.write` 授权与审计；Node 不做鉴权。
- **Java 发布 thin jar，Node runtime 独立发布 npm 包**：
  - `com.innospots:innospots-nexus-ai`：supervisor + `/ai/**` 代理，不内嵌 runtime。
  - `@innospots/nexus-ai-runtime`：Node runtime，引用方用 pnpm 安装。
- 依赖 `innospots-nexus-console`，不引入 Spring、Quarkus、JDBC、ORM 或 portal/platform。
- 与宿主机的 pi 安装、模型配置、凭证和会话完全隔离。

## 当前状态

**MVP 已实现**：Node runtime（配置、会话、Agent、工具、HTTP/SSE、health、CLI）与 Java 模块
（supervisor、`/ai/**` 代理、health）均包含可编译代码与单元/集成测试。

```bash
# Node
cd runtime && pnpm install && pnpm typecheck && pnpm test && pnpm build

# Java
mvn -pl innospots-nexus-ai test
```

设计文档：[docs/ai-runtime-design.md](docs/ai-runtime-design.md)

## 结构

```text
innospots-nexus-ai/
├── pom.xml                       # thin jar：supervisor + /ai 代理
├── docs/ai-runtime-design.md     # 架构、API、配置、分发、路线图
├── runtime/                      # Node 源码 + npm 包
│   ├── package.json              # name: @innospots/nexus-ai-runtime
│   ├── pnpm-lock.yaml            # Phase 1 生成并提交
│   ├── tsconfig.json
│   ├── bin/nexus-ai-runtime.mjs  # npm bin
│   ├── config/
│   │   ├── runtime.yaml
│   │   └── conf.d/*.yaml         # 多文件配置示例
│   └── src/                      # Phase 1 起
└── src/main/java/                # Java 宿主 + /ai 反向代理（P1+）
```

## 发布

Java jar（不含 runtime）：

```bash
mvn -pl innospots-nexus-ai -am deploy
```

Node npm 包：

```bash
cd runtime
pnpm install --frozen-lockfile
pnpm build
pnpm publish --access restricted --tag latest
```

版本必须与 jar manifest 的 `Nexus-Ai-Runtime-Version` 一致；快照/预发布使用
`x.y.z-snapshot.<build>` 并以 `snapshot` dist-tag 发布。

## 引用方

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-ai</artifactId>
    <version>${innospots.version}</version>
</dependency>
```

```json
{
  "dependencies": {
    "@innospots/nexus-ai-runtime": "0.1.0-SNAPSHOT"
  }
}
```

```bash
pnpm install --frozen-lockfile --prod
```

- 不再解包 jar，也不需要 `runtime.zip`。
- `AiRuntimeConfig.runtimeDir` 指向
  `node_modules/@innospots/nexus-ai-runtime`（包含 `dist/server.mjs`）。
- 容器部署时把 `node_modules` 与 Node ≥ 22.19 一起打进镜像。
- 运行机只需要 Node ≥ 22.19，不需要 pnpm；安装发生在构建/镜像阶段。
- 离线/隔离环境：使用私有 npm 镜像，或 `pnpm pack` 生成 tarball 后以 `file:` 安装。

## 启动 Runtime

```bash
# 安装一次
pnpm add -g @innospots/nexus-ai-runtime

# 零参数启动：~/.nexus-ai + 127.0.0.1:18080
nexus-ai

# 只改端口
nexus-ai --port 18081

# 指定目录
nexus-ai --data-dir /srv/nexus-ai --config-dir /etc/nexus-ai
```

项目内安装：

```bash
pnpm exec nexus-ai
```

临时运行：

```bash
npx @innospots/nexus-ai-runtime
```

> `nexus-ai` 来自 npm `bin`；`bin/nexus-ai-runtime.mjs` 只是内部 ESM 入口，
> 不需要手动输入 `.mjs`。如果坚持用 `node`，可以执行
> `node node_modules/@innospots/nexus-ai-runtime`（`main` 指向 `dist/server.mjs`）。

启动成功后 stdout 输出一行 readiness JSON，并写入 `<data-dir>/runtime.json`：

```json
{"event":"listening","host":"127.0.0.1","port":18080,"pid":1234,"version":"0.1.0-SNAPSHOT","configVersion":"sha256:..."}
```

常用参数：

| 参数 | 默认 | 说明 |
|------|------|------|
| `--host` | `127.0.0.1` | 仅允许回环 |
| `--port` | `18080` | 固定端口；Java supervisor 用 `0` |
| `--data-dir` | `~/.nexus-ai` | 状态/日志/密钥 |
| `--config-dir` | `<data-dir>/config` | YAML 多文件 |
| `--config-url` | 空 | 优先于本地 YAML |
| `--ready-file` | `<data-dir>/runtime.json` | 原子写就绪信息 |
| `--log-level` | `info` | debug/info/warn/error |
| `--no-watch` | 关 | 关闭 fs.watch，只用 SIGHUP |
| `--help` / `--version` | - | 帮助/版本 |

- 信号：`SIGTERM`/`SIGINT` 优雅退出；`SIGHUP` 重载配置。
- 不自带 daemon 化；由 systemd、容器或 Java supervisor 管理。

## 对外调用

外部只访问 Java 端点，且只开放最小调用面：

```text
GET    /ai/v1/health                 # 聚合状态；200/503，匿名
GET    /ai/v1/health/live            # JVM 存活；200，匿名
GET    /ai/v1/health/ready           # Node+配置就绪；200/503，匿名
GET    /ai/v1/openapi.yaml           # 匿名
GET    /ai/v1/agents                 # 只读，脱敏
POST   /ai/v1/sessions
GET    /ai/v1/sessions
GET    /ai/v1/sessions/{id}
DELETE /ai/v1/sessions/{id}
GET    /ai/v1/sessions/{id}/messages
POST   /ai/v1/sessions/{id}/messages  # SSE
GET    /ai/v1/runs/{id}
POST   /ai/v1/runs/{id}/cancel
Authorization: Bearer <console-access-token>
```

- Node 未启动/启动中/健康检查失败/配置无效时，业务端点直接 503，并带 `Retry-After`。
- `/health/live` 不依赖 Node；`/health/ready` 只在 runtime 就绪时 200。

- GET 需要 `ai.read`，写操作需要 `ai.write`。
- 配置、诊断、维护端点不反代；模型/AgentProfile/工具/MCP/文件根只从配置加载。
- Node 的 loopback 端口不对外，也不向调用方下发。

## 边界

- 文件工具仅限登记根内：`file_read`、`file_list`、`file_write`、`file_edit`、`file_delete`；
  默认关闭，删除为软删除到数据目录。
- 不提供 shell、代码执行或任意路径访问。
- MCP 仅支持 HTTP/SSE 传输。
- 本期不做租户/工作区作用域、控制台页面与 OpenAI 兼容门面。
