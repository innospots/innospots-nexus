# Nexus AI Runtime (Node)

pi agent 驱动的 AI 服务端。仅监听 loopback，供 Java `/ai/**` 代理调用；
配置使用 ConfigSource/YAML 只读加载，会话正文使用 SessionManager JSONL，不引入 SQLite。

## 要求

- Node.js ≥ 22.19.0（与 `@earendil-works/pi-agent-core` 的 engines 对齐）
- pnpm（仅构建期）

## 布局

```text
src/
├── main.ts          # 参数解析、启动、信号处理
├── server/          # http、router、sse、errors（无鉴权）
├── agent/           # Agent 工厂、run 管理、事件桥接
├── model/           # provider/model 注册、凭证解析、probe
├── tool/            # HTTP 工具、MCP 客户端、注册表
├── session/         # SessionManager（v3 JSONL）与上下文桥接
├── config/          # ConfigSource、YAML 多文件加载、校验、重载
├── security/        # env 清洗、路径/主机白名单
└── runtime/         # 运行参数与上限
```

## 发布与安装

```bash
# 维护/发布侧
pnpm install --frozen-lockfile
pnpm typecheck
pnpm test
pnpm build                 # 产出 dist/server.mjs
pnpm publish --access restricted --tag latest    # release
pnpm publish --access restricted --tag snapshot  # prerelease

# 引用方
pnpm add @innospots/nexus-ai-runtime@<same-version-as-jar>
```

- npm 包只包含 `bin/`、`dist/`、`config/`、`README.md`。
- 版本必须与 Java jar 的 `Nexus-Ai-Runtime-Version` 一致。
- 运行机只需要 Node ≥ 22.19；安装发生在构建/镜像阶段。
- 离线环境使用私有镜像或 `pnpm pack` 后的 tarball。

## 启动

```bash
# 安装后直接运行
nexus-ai

# 指定端口
nexus-ai --port 18081

# 指定目录
nexus-ai --data-dir /srv/nexus-ai --config-dir /etc/nexus-ai
```

- 默认 `--host 127.0.0.1 --port 18080 --data-dir ~/.nexus-ai`。
- 启动后打印实际端口并写 `--ready-file`。
- 参数、环境变量、退出码与信号见设计文档 §7.9。
- `bin/nexus-ai-runtime.mjs` 是 ESM 入口；npm 生成 `nexus-ai` 命令，无需输入 `.mjs`。

## 配置

本地配置使用 YAML 多文件，默认布局：

```text
config/
├── runtime.yaml
└── conf.d/
    ├── 10-providers.yaml
    ├── 20-models.yaml
    ├── 30-agent-profiles.yaml
    ├── 35-prompt-templates.yaml
    ├── 40-tools.yaml
    ├── 50-mcp.yaml
    ├── 60-file-roots.yaml
    └── 70-skills.yaml
skills/
└── example/SKILL.md
```

- 支持多文件与单文件多文档（`---`）。
- `ConfigSource` 接口优先于本地文件；两者都只读。
- 同一 `(kind, metadata.name)` 在同一来源重复会报错。
- 重载：`fs.watch` 或 `SIGHUP`；不提供 HTTP reload 端点。

## 约束

- 不读取 `~/.pi`、宿主 provider 环境变量或任何模型配置文件。
- 固定监听 `127.0.0.1`，不做鉴权；`/v1/health` 仅供 Java 健康监控；不提供 HTTP shutdown 端点，由 Java 发送 SIGTERM。
- 外部不可直接访问 Node；鉴权统一在 Java `/ai/**`。
- 会话正文使用 `@earendil-works/pi-coding-agent` 的 `SessionManager`；
  始终显式传 `sessionDir` 与 `cwd`，不调用 `createAgentSession()`。
- 文件工具只允许操作会话快照的沙箱根；相对路径、realpath 校验、默认拒绝符号链接；
  删除为软删除到 `<dataDir>/trash`。
- 不在运行期安装依赖。
- 模型/AgentProfile/工具/文件根配置只读加载：ConfigSource 优先，本地 YAML 兜底；不使用 SQLite。
- 会话正文写入 SessionManager JSONL；文件操作审计写 JSONL；唯一的运行时可变数据是会话。
- 不提供 shell 或代码执行；MCP 只允许 HTTP/SSE。
