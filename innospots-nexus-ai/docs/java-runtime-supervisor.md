# Java 运行时宿主与健康检查设计

## 1. 文档定位

- 状态：草案
- 范围：Java supervisor、进程生命周期、版本检查、健康监控、可用性门禁
- 关联：[java-module-design.md](java-module-design.md)、[java-configuration.md](java-configuration.md)

## 2. 设计目标

1. 启动/停止 Node runtime，管理崩溃重启。
2. 启动前完成包、版本、Node、目录、端口检查。
3. 持续健康检查；不可用时快速返回 503。
4. 健康端点不依赖 Node 存活。
5. 与 npm 分发、版本一致性约束配合。

## 3. 状态机

```text
STOPPED
  └─ start() ──▶ STARTING
                   ├─ 成功 ──▶ RUNNING
                   ├─ 失败 ──▶ FAILED
                   └─ stop() ─▶ STOPPING ─▶ STOPPED
RUNNING
  ├─ health fail × N ──▶ DEGRADED
  ├─ process exit ─────▶ STARTING（退避重启）
  └─ stop() ───────────▶ STOPPING ─▶ STOPPED
DEGRADED
  ├─ health ok ────────▶ RUNNING
  ├─ restartOnUnhealthy ▶ STARTING
  └─ stop() ───────────▶ STOPPING
FAILED
  ├─ 冷却后 start() ───▶ STARTING
  └─ stop() ───────────▶ STOPPED
```

状态说明：

| 状态 | 含义 | 业务请求 |
|------|------|----------|
| STOPPED | 未启动 | 503 / LAZY 尝试启动 |
| STARTING | 启动中 | 503 |
| RUNNING | 可用 | 转发 |
| DEGRADED | 进程存活但健康失败 | 503 |
| STOPPING | 停止中 | 503 |
| FAILED | 启动/崩溃失败 | 503 |

## 4. 启动流程

```text
start()
  ├─ 获取 <dataDir>/runtime.lock（FileChannel.tryLock）
  ├─ 若已 RUNNING/STARTING：幂等返回
  ├─ RuntimeLocator.locate()
  ├─ RuntimePackageValidator.validate()
  │    ├─ package.json name == @innospots/nexus-ai-runtime
  │    ├─ package.version == jar manifest Nexus-Ai-Runtime-Version
  │    └─ dist/server.mjs 存在
  ├─ NodeVersionChecker.check() ≥ 22.19
  ├─ 创建 dataDir/{sessions,workspace,logs,tmp}
  ├─ AiProcessLauncher.spawn()
  │    └─ node <runtimeDir>/bin/nexus-ai-runtime.mjs \
  │         --host 127.0.0.1 --port 0 \
  │         --data-dir <dataDir> --config-dir <configDir> \
  │         --ready-file <dataDir>/runtime.json
  ├─ 等待 readyFile / stdout readiness（startupTimeout）
  ├─ 轮询 Node /v1/health 直至 200
  ├─ AiRuntimeHealthMonitor.start()
  └─ RUNNING
```

### 4.1 RuntimeLocator

解析顺序：

1. 显式 `AiRuntimeConfig.runtimeDir`
2. `NEXUS_AI_RUNTIME_DIR`
3. `${app.dir}/node_modules/@innospots/nexus-ai-runtime`
4. `${app.dir}/ai/runtime`

不再从 jar 解包；没有 classpath 回退。

### 4.2 包与版本检查

| 检查 | 失败状态码 |
|------|------------|
| 目录/`package.json` 不存在 | `RUNTIME_PACKAGE_MISSING` |
| `dist/server.mjs` 不存在 | `RUNTIME_PACKAGE_MISSING` |
| 包名不是 `@innospots/nexus-ai-runtime` | `RUNTIME_PACKAGE_MISSING` |
| 版本与 jar manifest 不一致 | `RUNTIME_VERSION_MISMATCH` |
| Node 不存在/版本低 | `NODE_NOT_AVAILABLE` |

### 4.3 进程参数

```text
node <runtimeDir>/bin/nexus-ai-runtime.mjs
  --host 127.0.0.1
  --port 0
  --data-dir <dataDir>
  --config-dir <configDir>
  --ready-file <dataDir>/runtime.json
  [--config-url <url>]
  [--log-level <level>]
```

- 直接 `spawn`，不经过 shell。
- 不传 token。
- env 使用白名单构造。

### 4.4 环境变量

允许：

```text
PATH, LANG, LC_ALL, TZ, NODE_ENV=production,
NEXUS_AI_HOME, NEXUS_AI_LOG_LEVEL, NEXUS_AI_MASTER_KEY,
NEXUS_AI_CONFIG_URL
```

显式移除：`PI_*`、`OPENAI_*`、`ANTHROPIC_*`、`GOOGLE_*`、`AZURE_*`、`AWS_*`、
`DEEPSEEK_*`、`MOONSHOT_*`、`OPENROUTER_*`、`HF_TOKEN` 等。

## 5. 健康检查

### 5.1 监控

`AiRuntimeHealthMonitor`：

- JDK `ScheduledExecutorService` 单线程。
- 每 `healthCheckInterval`（5s）调用 Node `/v1/health`。
- 单次超时 `healthTimeout`（1s）。
- 结果缓存 `healthCacheTtl`（2s）。
- 连续失败 `unhealthyThreshold`（3）→ DEGRADED。
- 成功一次 → 回到 RUNNING。
- `restartOnUnhealthy=true` 时触发重启。

### 5.2 快照

```java
public record AiRuntimeHealth(
        AiRuntimeStatus state,
        Instant since,
        String runtimeVersion,
        String nodeVersion,
        long pid,
        Instant lastCheckAt,
        long latencyMs,
        String configVersion,
        String error) {
}
```

`volatile` 不可变快照；健康端点与业务门禁读同一对象。

### 5.3 状态端点

- `GET /ai/v1/health/live`：恒 200。
- `GET /ai/v1/health/ready`：RUNNING + config valid + lastCheck ok → 200，否则 503。
- `GET /ai/v1/health`：聚合详情，200/503。

## 6. 可用性门禁

```java
AiRuntimeHealth health = runtime.health();
if (health.state() == RUNNING && health.isFresh()) {
    return; // forward
}
throw unavailable(health);
```

| 状态 | 行为 |
|------|------|
| RUNNING + fresh | 转发 |
| STOPPED + LAZY | 尝试 start（锁 + 冷却） |
| STOPPED + EAGER | 503 |
| STARTING | 503 + Retry-After 2 |
| DEGRADED | 503 + Retry-After 5 |
| FAILED | 503 + Retry-After 10 |
| 配置无效 | 503 + Retry-After 30 |

- 不建立 SSE。
- 错误体 Node 风格，`details.state`、`details.retryAfterSeconds`。
- `Retry-After` 同时设置。

## 7. 重启

- 进程异常退出：指数退避 1s/2s/4s，最多 `maxRestarts`（3）。
- 超过次数 → FAILED，保留最近 stderr 尾部。
- 健康 DEGRADED 不自动重启，除非 `restartOnUnhealthy=true`。
- 重启期间业务 503。
- 启动/重启互斥，避免并发进程。

## 8. 停止

```text
stop()
  ├─ 状态 STOPPING
  ├─ 停止健康监控
  ├─ SIGTERM
  ├─ 等待 shutdownTimeout（10s）
  ├─ 超时 SIGKILL
  ├─ 删除 runtime.json
  ├─ 释放文件锁
  └─ STOPPED
```

- 关闭路径不抛业务异常；记录日志。
- 幂等；多次 stop 安全。

## 9. 文件锁

- `<dataDir>/runtime.lock`，`FileChannel.tryLock()`。
- 已锁：`DATA_DIR_LOCKED`，不启动第二个 Node。
- JVM 退出/异常时释放。

## 10. 端口与就绪

- Java 始终传 `--port 0`，避免端口冲突。
- 从 `runtime.json` 或 stdout readiness 读取实际端口。
- 若 `readyFile` 超时未出现，终止进程并按启动失败处理。
- 健康轮询成功后才进入 RUNNING。

## 11. 日志与诊断

- stdout/stderr 重定向到 `<dataDir>/logs/node-<timestamp>.log`。
- 失败时 `health.error` 保存 stderr 尾部摘要（不含密钥）。
- 关键事件：启动、端口、版本、退出码、重启、健康失败、配置错误。
- 不记录 prompt、文件内容、Authorization。

## 12. 测试

| 场景 | 断言 |
|------|------|
| 包缺失 | 启动失败 `RUNTIME_PACKAGE_MISSING` |
| 版本不一致 | 启动失败 `RUNTIME_VERSION_MISMATCH` |
| Node 版本低 | `NODE_NOT_AVAILABLE` |
| 端口/目录锁 | `DATA_DIR_LOCKED` |
| 启动超时 | `RUNTIME_START_FAILED` |
| 健康连续失败 | DEGRADED，业务 503 |
| 健康恢复 | RUNNING，业务恢复 |
| 进程崩溃 | 退避重启；超限 FAILED |
| stop | SIGTERM 优雅；超时 SIGKILL |
| 并发 start | 只启动一个进程 |
| live/ready | live 恒 200；ready 随状态变化 |
