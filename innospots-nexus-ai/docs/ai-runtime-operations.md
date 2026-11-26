# Node Runtime 运行与发布设计

## 1. 文档定位

- 状态：草案
- 范围：CLI、环境变量、启动输出、信号、健康、npm 发布、部署与版本
- 关联：[ai-runtime-design.md](ai-runtime-design.md)、[java-runtime-supervisor.md](java-runtime-supervisor.md)

## 2. 安装形态

| 场景 | 命令 |
|------|------|
| 全局安装 | `pnpm add -g @innospots/nexus-ai-runtime`，然后 `nexus-ai` |
| 项目内安装 | `pnpm add @innospots/nexus-ai-runtime`，然后 `pnpm exec nexus-ai` |
| 临时运行 | `npx @innospots/nexus-ai-runtime` |
| 纯 node（包目录） | `node node_modules/@innospots/nexus-ai-runtime` |
| 多次运行 | `nexus-ai --port 18081` |

`nexus-ai-runtime` 是同一 CLI 的别名。

`.mjs` 是 ESM 文件扩展名；`bin/nexus-ai-runtime.mjs` 是内部入口，npm 的 `bin` 会生成
无扩展命令，用户无需输入 `.mjs`。

## 3. 默认值

| 项 | 默认 |
|----|------|
| host | `127.0.0.1` |
| port | `18080` |
| dataDir | `~/.nexus-ai` |
| configDir | `<dataDir>/config` |
| readyFile | `<dataDir>/runtime.json` |
| logLevel | `info` |
| logFormat | TTY 下 `pretty`，否则 `json` |
| watchConfig | 开启 |

Java supervisor 会显式传 `--port 0` 使用随机端口。

## 4. 参数

| 参数 | 默认 | 说明 |
|------|------|------|
| `--host` | `127.0.0.1` | 仅允许回环；其他值退出 2 |
| `--port` | `18080` | `0` 表示随机端口 |
| `--data-dir` | `~/.nexus-ai` | sessions、日志、secrets、ready/pid |
| `--config-dir` | `<data-dir>/config` | `runtime.yaml` + `conf.d/` |
| `--config-url` | 空 | ConfigSource HTTP 地址，优先于本地 |
| `--ready-file` | `<data-dir>/runtime.json` | 原子写就绪信息 |
| `--pid-file` | 空 | 可选 PID 文件 |
| `--log-level` | `info` | debug/info/warn/error |
| `--log-format` | 自动 | json/pretty |
| `--no-watch` | 关 | 关闭 fs.watch，只用 SIGHUP |
| `--version` | - | 打印版本 |
| `--help` | - | 打印帮助 |

## 5. 环境变量

```text
NEXUS_AI_HOST
NEXUS_AI_PORT
NEXUS_AI_DATA_DIR
NEXUS_AI_CONFIG_DIR
NEXUS_AI_CONFIG_URL
NEXUS_AI_READY_FILE
NEXUS_AI_LOG_LEVEL
NEXUS_AI_MASTER_KEY
```

优先级：

```text
CLI > 环境变量 > 默认值
```

`Runtime` YAML 只提供运行参数，不覆盖 host/port/dataDir/configDir/readyFile。

## 6. 启动流程

```text
1. 解析参数/环境变量
2. 校验 Node >= 22.19
3. 创建 dataDir / configDir
4. 加载 ConfigSource + 本地 YAML
5. 构建模型、工具、AgentProfile、FileRoot 注册表
6. 初始化 SessionManager（sessions 目录）
7. 绑定 127.0.0.1:<port>
8. 打印 readiness JSON
9. 原子写 readyFile
10. 注册 SIGTERM / SIGINT / SIGHUP
11. 启动配置 watch（可关闭）
```

### 6.1 readiness 输出

stdout 一行 JSON：

```json
{"event":"listening","host":"127.0.0.1","port":18080,"pid":1234,
 "version":"0.1.0-SNAPSHOT","configVersion":"sha256:..."}
```

同时写入 `readyFile`（0600），内容相同。Java supervisor 读取实际端口。

### 6.2 启动失败

| 场景 | 退出码 |
|------|--------|
| 参数/配置非法 | 2 |
| Node 版本不满足 | 3 |
| 端口绑定失败 | 4 |
| 其他启动失败 | 5 |
| ConfigSource 拉取/解析失败 | 6 |

错误写 stderr，stdout 不输出半截 readiness。

## 7. 信号

| 信号 | 行为 |
|------|------|
| `SIGTERM` | 停止接收新请求；等待进行中的 run 到超时；关闭流；退出 0 |
| `SIGINT` | 同 SIGTERM |
| `SIGHUP` | 重新加载配置；失败保留旧快照 |

- 不自带 daemon 化；由 systemd、容器或 Java supervisor 管理。
- 优雅关闭超时默认 10s；超时由 supervisor SIGKILL。

## 8. 健康

Node 内部：

```text
GET /v1/health
```

返回 `status`、`version`、`node`、`configVersion`、`checks`。

Java 侧：

```text
GET /ai/v1/health
GET /ai/v1/health/live
GET /ai/v1/health/ready
```

- Java 健康端点不依赖 Node 存活。
- `live` 恒 200；`ready` 仅在 Node RUNNING + 配置有效时 200。
- Node 内部健康不对外反代。

## 9. npm 发布

### 9.1 包内容

`package.json` 的 `files`：

```text
bin/
dist/
config/
README.md
```

不发布：`src/`、`test/`、`node_modules/`、`pnpm-lock.yaml`（可由包管理器生成）。

### 9.2 发布命令

```bash
cd runtime
pnpm install --frozen-lockfile
pnpm typecheck
pnpm test
pnpm build
pnpm publish --access restricted --tag latest
```

- `prepublishOnly` 强制执行 `pnpm build`。
- 快照发布：`x.y.z-snapshot.<build>` + `snapshot` dist-tag。
- 私有仓库：`.npmrc` 配置 registry 与 `_authToken`（`NPM_TOKEN`）。

### 9.3 版本一致性

- npm `package.json.version` 必须等于 Java jar manifest
  `Nexus-Ai-Runtime-Version`。
- 发布顺序：先 npm，再 jar，或同一 release gate 校验。
- Java supervisor 启动时校验；不一致返回 `RUNTIME_VERSION_MISMATCH`。

## 10. 离线与隔离

| 场景 | 方案 |
|------|------|
| 私有 npm | 内部 registry / GitHub Packages |
| 离线镜像 | `pnpm pack` 生成 tarball，`file:` 安装 |
| 空气隔离 | 把 tarball 与 `node_modules` 打进镜像 |
| 运行机无网络 | 允许；安装已在构建阶段完成 |

运行期禁止 `pnpm install` / `npm install`。

## 11. 部署形态

### 11.1 随 Java 服务启动

```text
Java supervisor
  → node <runtimeDir>/bin/nexus-ai-runtime.mjs --port 0 ...
  → 读取 readyFile
  → 轮询 /v1/health
```

### 11.2 独立进程

```bash
nexus-ai --port 18080 --data-dir /srv/nexus-ai --config-dir /etc/nexus-ai
```

由 systemd/容器管理；Java 仍作为对外 API 入口。

### 11.3 容器

```dockerfile
FROM node:22-alpine
WORKDIR /app
COPY node_modules ./node_modules
COPY config ./config
CMD ["node", "node_modules/@innospots/nexus-ai-runtime/bin/nexus-ai-runtime.mjs",
     "--host", "127.0.0.1", "--port", "18080",
     "--data-dir", "/data", "--config-dir", "/app/config"]
```

`/data` 需要持久卷。

## 12. 观测

- 日志：stdout JSON（supervisor 捕获），stderr 错误。
- 关键字段：`ts`、`level`、`event`、`requestId`、`sessionId`、`runId`、`state`。
- 不记录密钥、Authorization、文件内容、完整 prompt。
- 健康检查结果、配置版本、退出原因必须可观测。

## 13. 测试

| 场景 | 断言 |
|------|------|
| 零参数启动 | 默认 `~/.nexus-ai`、18080 |
| `--port 0` | readiness 输出实际端口 |
| 参数优先级 | CLI > env > 默认 |
| 非回环 host | 退出 2 |
| Node 版本不足 | 退出 3 |
| 端口占用 | 退出 4 |
| SIGHUP | 配置重载 / 失败保留 |
| SIGTERM | 优雅退出、readyFile 处理 |
| npm pack | 仅含约定文件；bin 可执行 |
| 版本不一致 | Java supervisor 拒绝启动 |
