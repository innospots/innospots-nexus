# Nexus AI Runtime 设计方案

## 1. 文档定位

- 状态：草案（待评审）
- 日期：2026-11-26
- 归属模块：`innospots-nexus-ai`
- 关联文档：[AGENTS.md](../../AGENTS.md)、[module-layout.md](../../.cursor/skills/java-project/references/module-layout.md)
- 详细拆分：
  - [ai-runtime-modules.md](ai-runtime-modules.md)
  - [ai-runtime-config.md](ai-runtime-config.md)
  - [ai-runtime-api.md](ai-runtime-api.md)
  - [ai-runtime-storage.md](ai-runtime-storage.md)
  - [ai-runtime-operations.md](ai-runtime-operations.md)
  - [java-module-design.md](java-module-design.md)
  - [java-api-design.md](java-api-design.md)
  - [java-runtime-supervisor.md](java-runtime-supervisor.md)
  - [java-configuration.md](java-configuration.md)
- 交付阶段：**只交付工程骨架与契约，不交付实现代码**

本文定义 `innospots-nexus-ai` 的工程结构、运行时边界、对外 API 契约与分发方式。
AI 能力由**独立 Node.js 进程**提供，AI 契约由该进程定义；Java 模块负责运行时宿主、
进程生命周期，并以 `/ai/**` 透明反向代理作为唯一对外入口，统一完成 console 鉴权。

## 2. 背景与目标

### 2.1 背景

- 现有 `innospots-nexus-*` 是 Java 25 / Maven 平台库，运行时由 Spring/Quarkus 装配层组装。
- 选定的 Agent SDK 是 `@earendil-works/pi-agent-core`（由 `@mariozechner/pi-agent-core` 迁移而来），
  配套 `@earendil-works/pi-ai`。二者是 **Node.js ≥ 22.19 的 ESM 库**，无法在 JVM 内加载。
- 会话持久化直接复用 `@earendil-works/pi-coding-agent` 的 **`SessionManager`**：
  v3 JSONL、append-only 树、原生分支/压缩/上下文编辑。
- 因此必须引入进程边界。若把 Java 与 Node 的 REST 契约各写一套，会产生双重维护与语义漂移。

### 2.2 目标

1. 用 pi agent 作为 SDK，提供可被外部服务调用的 AI Agent 能力。
2. **API 只有一份**：由 Node 运行时定义并实现，Java 不再包装 REST 契约。
3. 模型/AgentProfile/工具/文件根配置**只读加载**：`ConfigSource` 接口优先于本地 YAML 多文件；不提供配置维护 API。
4. 与宿主机的 pi 安装、模型配置、凭证、会话完全隔离；不读取 `~/.pi` 或宿主 provider 环境变量。
5. 轻量启动：单 Node 进程、无外部中间件、无运行时联网安装、冷启动目标 < 2s。
6. `innospots-nexus-ai` 作为 Maven 依赖提供宿主/代理；runtime 作为
   `@innospots/nexus-ai-runtime` npm 包，由引用方 pnpm 安装。
7. 文件工具仅允许操作登记并绑定到 AgentProfile 的根目录，禁止 shell、代码执行与任意路径访问。
8. Java 侧提供 health/liveness/readiness 端点；Node 未启动、启动中、健康检查失败或配置无效时，
   业务端点快速返回 503，状态端点返回可诊断信息。

### 2.3 成功标准

- 外部服务只访问 Java `/ai/v1/**`，使用 console Bearer 完成鉴权后即可：
  选择 AgentProfile → 创建会话 → 流式对话 → 取消运行。
- 引用方只依赖 thin jar + npm runtime，不需要解包 jar，也不在运行期安装依赖。
- 一台只有 Node ≥ 22.19 的机器，拿到已安装的 runtime 包即可直接启动。
- 宿主环境中的 `OPENAI_API_KEY`、`~/.pi/auth.json` 等对运行时不可见、不被读取。

## 3. 不在本方案中的能力

| 排除项 | 原因 / 后续归属 |
|--------|----------------|
| 多租户 / workspace / project 作用域 | 本期明确「暂不涉及」；配置/会话元数据预留迁移路径，见 §7.3 |
| 管理控制台页面、菜单、权限目录 | 本模块不提供；如需管理界面，后续另建受控 admin 端点或配置管理流程 |
| OpenAI 兼容 `/v1/chat/completions` 门面 | 会丢失会话、工具、取消语义；未来可作为 Node 侧适配层追加 |
| WebSocket 传输 | SSE 已覆盖服务端流式输出；双向交互后续再评估 |
| 图片生成、分类模型 | pi-ai 支持，但本期只做 chat + tool calling |
| 集群、分布式会话、跨节点调度 | 单进程、单写入者；横向扩展后续单独设计 |
| shell、代码执行、任意路径文件访问 | 明确禁止；文件工具仅限登记根内 |
| SQLite / 关系型数据库 / ORM | pi 原生不使用；本模块不引入，避免额外持久化层 |
| 关系型配置表（`ai_*`） | 不建表；配置只读加载，唯一可变数据是会话 JSONL |
| MCP stdio 传输 | V1 仅 HTTP/SSE；stdio 需要进程白名单，后续单独评审 |
| Spring Boot / Quarkus 自动配置 | 模块不绑定框架；装配由引用方完成 |

## 4. 关键决策（grill-me 结论）

| 决策点 | 结论 |
|--------|------|
| pi 使用层级 | `pi-agent-core` + `pi-ai` + `pi-coding-agent`（仅用 `SessionManager`） |
| 会话存储 | 直接复用 `SessionManager`：v3 JSONL、append-only 树，隔离的 `sessionDir` |
| 进程边界 | Java 管理独立 Node 进程；Node 仅 loopback，Java `/ai/**` 是唯一对外入口 |
| 模块依赖 | 依赖 `innospots-nexus-console`（传递 core/base）；不依赖 portal/platform |
| 配置来源 | `ConfigSource` 接口优先，本地 YAML 多文件兜底；均只读 |
| 配置维护 | 不提供 CRUD API；fs.watch / SIGHUP 触发重载 |
| 配置/持久化 | 配置只读加载；会话正文用 SessionManager JSONL；无 SQLite |
| 作用域 | 本期不引入租户/工作区字段 |
| 工具边界 | HTTP API、MCP、受限文件工具；不开放 shell / 代码执行 / 越界路径 |
| 制品形态 | Java thin jar（supervisor + 代理）+ 独立 npm 包 `@innospots/nexus-ai-runtime` |
| 引用方分发 | 引用方 `pnpm install @innospots/nexus-ai-runtime@<same-version>`，不解包 jar |
| 本阶段交付 | Maven 模块骨架 + 设计文档，不写实现 |

## 5. 总体架构

```text
┌─────────────────────────────────────────────────────────────┐
│ 外部服务（任意语言）                                          │
│   GET/POST/PATCH/DELETE  /ai/v1/**                          │
│   Authorization: Bearer <console-token>                     │
└───────────────────────────┬─────────────────────────────────┘
                            │ Java 统一鉴权 / 审计 / 授权
┌───────────────────────────▼─────────────────────────────────┐
│ Java: innospots-nexus-ai（依赖 console）                      │
│  endpoint/  AiHealthEndpoint + AI 代理端点                   │
│  health/    runtime 健康监控、状态快照、readiness 判定        │
│  proxy/     路径/方法白名单、头过滤、SSE 透传、超时            │
│  runtime/   AiRuntime、AiProcessSupervisor、RuntimeLocator    │
│  status/    AiStatusCode（NAI 前缀）                          │
└───────────────────────────┬─────────────────────────────────┘
                            │ 127.0.0.1:<ephemeral>，无鉴权，仅本机
┌───────────────────────────▼─────────────────────────────────┐
│ Node AI Runtime（内部进程，契约与实现所有者）                  │
│  server/     HTTP 路由、SSE、错误封装（不做鉴权）              │
│  agent/      pi-agent-core：Agent 实例、run、取消、事件桥接     │
│  model/      pi-ai：provider/model 注册、凭证解析、连通性探测   │
│  tool/       HTTP 工具 + MCP 工具 + 受限文件工具（HTTP/SSE）    │
│  session/    SessionManager：v3 JSONL、分支、压缩、上下文      │
│  config/     ConfigSource 接口 + YAML 多文件、校验、重载      │
│  security/   env 清洗、路径/主机白名单                         │
└─────────────────────────────────────────────────────────────┘
```

### 5.1 职责划分

| 关注点 | 归属 | 说明 |
|--------|------|------|
| 对外 API、鉴权、授权、审计 | Java `endpoint` | 唯一对外入口；console Bearer + 粗粒度 Grant |
| 健康检查、readiness、可用性门禁 | Java `health` | 不依赖 Node 存活；业务请求前置 503 |
| 请求/响应/SSE 转发 | Java `proxy` | 透明代理，不复制业务 DTO |
| AI 契约与实现 | Node runtime | 唯一定义处，OpenAPI 由 Node 生成 |
| Agent 循环、工具调用 | Node runtime（pi-agent-core） | Java 不参与推理编排 |
| 模型配置、AgentProfile/工具/文件根配置 | Node runtime + ConfigSource/YAML | 只读，接口优先于本地文件 |
| 会话正文与模型上下文 | Node runtime + `SessionManager` | v3 JSONL，唯一真源 |
| runtime 定位、启动、健康、关闭 | Java `runtime` | 不解析业务语义 |
| 生命周期接线（Spring/Quarkus） | 引用方装配层 | 模块内不绑定框架 |

### 5.2 为什么 Java 作为唯一对外入口

- 外部只访问 Java `/ai/v1/**`，复用 console 的 Bearer 登录、角色授权、审计与异常映射。
- Node 只监听 `127.0.0.1` 的随机端口，不启用 HTTP 鉴权，不对外暴露。
- Java 采用**透明反向代理**：方法、查询串、请求体、状态码与 SSE 事件按原样转发，
  不在 Java 侧重复定义 AI DTO 与业务语义。
- 代理层负责路径/方法白名单、头过滤、请求体上限、超时、SSE 背压与断连处理；
  这是 Java 侧唯一需要维护的“协议逻辑”。


## 6. 工程结构

### 6.1 Maven 归属

| 能力 | Owning artifact | 直接依赖 | 新建模块 |
|------|-----------------|----------|----------|
| AI 运行时宿主 + `/ai/**` 透明代理 | `innospots-nexus-ai` | `innospots-nexus-console` | 是 |
| AI API / Agent / 存储 | `@innospots/nexus-ai-runtime`（npm 包，源码在 `runtime/`） | 无 Maven 依赖 | 否 |
| Spring/Quarkus 生命周期接线 | 引用方装配模块 | `innospots-nexus-ai` | 本期不建 |

依赖方向：

```text
innospots-nexus-base
        ↑
innospots-nexus-core
        ↑
innospots-nexus-plugin
        ↑
innospots-nexus-console
        ├── innospots-nexus-portal
        ├── innospots-nexus-platform
        └── innospots-nexus-ai
```

`innospots-nexus-ai` **不得**依赖 portal、platform；不得绑定 Spring、Quarkus；
不得用 JDBC/ORM 定义 AI 持久化（AI 数据仍由 Node 持有）。console、portal、platform
不得反向依赖 ai。

### 6.2 模块目录（目标态）

```text
innospots-nexus-ai/
├── pom.xml
├── README.md
├── .gitignore
├── docs/
│   └── ai-runtime-design.md              # 本文
├── src/
│   ├── main/
│   │   └── java/com/innospots/nexus/ai/
│   │       ├── runtime/                  # AiRuntime、AiProcessSupervisor、RuntimeLocator
│   │       ├── proxy/                    # AiProxyService、路径/方法白名单、头过滤、SSE 转发
│   │       ├── health/                   # AiHealthEndpoint、健康监控、readiness、状态快照
│   │       ├── endpoint/                 # AiHealthEndpoint、AiRuntimeProxyEndpoint、AiSessionProxyEndpoint
│   │       ├── status/                   # AiStatusCode
│   │       └── config/                   # AiRuntimeConfig、AiProxyConfig、AiPaths、EnvironmentSanitizer
│   └── test/java/com/innospots/nexus/ai/ # 单测与集成测试
└── runtime/                              # Node 源码（不在此处构建）
    ├── package.json
    ├── pnpm-lock.yaml                    # Phase 1 生成并提交
    ├── tsconfig.json
    ├── bin/nexus-ai-runtime.mjs          # npm bin
    ├── src/
    │   ├── main.ts                       # 启动、参数解析、信号处理
    │   ├── server/                       # http、router、sse、errors（无鉴权）
    │   ├── agent/                        # agent factory、run manager、event bridge
    │   ├── model/                        # provider registry、credential resolver、startup validation
    │   ├── tool/                         # http tool、mcp client、file tools、registry
    │   ├── session/                      # SessionManager 适配、上下文桥接
    │   ├── config/                       # ConfigSource、YAML 多文件、校验、重载
    │   ├── security/                     # env sanitizer、路径/主机白名单
    │   └── runtime/                      # runtime settings、limits
    ├── test/
    ├── config/
    │   ├── runtime.yaml
    │   └── conf.d/*.yaml                 # Provider/Model/AgentProfile/Tool/MCP/FileRoot 示例
    └── scripts/
```

`process-resources` 只处理 Java 资源，**不把 runtime 源码打进 jar**；runtime 通过
`pnpm publish` 独立发布。

说明：

- Java 侧是**技术模块**，按功能分包；新增 `/ai/**` 透明代理端点，但不定义 AI DTO。
- OpenAPI 由 Node 生成并经 `/ai/v1/openapi` 代理暴露；Java 模块**不生成** OpenAPI 索引，
  这是对仓库 OpenAPI 构建约定的显式例外。
- 单包 ≤ 15 个类；`proxy` 超过 15 个类时按 `proxy.header` / `proxy.stream` 拆分。
- 不在本阶段创建空包目录，避免脚手架式占位；实现阶段按上表落地。

### 6.3 制品与分发（npm）

两个独立制品，各自发布：

| 制品 | 内容 | 体积（估算） |
|------|------|--------------|
| `com.innospots:innospots-nexus-ai` | Java supervisor + `/ai/**` 反向代理，**不含 runtime** | 几百 KB |
| `@innospots/nexus-ai-runtime` | `dist/server.mjs`、`bin/`、`config/`、README | 5–15 MB |

```text
本仓库                                     引用方
mvn deploy                                 mvn package / 镜像构建
  └─ thin jar ───────────────▶ 依赖 jar     ├─ pnpm install --prod
pnpm publish                               ├─ node_modules/@innospots/nexus-ai-runtime
  └─ npm package ────────────▶ 依赖包       └─ runtimeDir 指向该包
```

- 不再从 jar 解包，也不生成 `runtime.zip`。
- runtime 安装在引用方构建/镜像阶段完成；运行期不执行 `pnpm install`。
- 版本必须一致：npm `package.json.version` = jar manifest
  `Nexus-Ai-Runtime-Version`；不匹配时 supervisor 拒绝启动。

#### 6.3.1 引用方配置

Maven 依赖：

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-ai</artifactId>
    <version>${innospots.version}</version>
</dependency>
```

Node 依赖（`package.json`）：

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

或者用 Maven 在构建期执行：

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <id>install-ai-runtime</id>
            <phase>prepare-package</phase>
            <goals><goal>exec</goal></goals>
            <configuration>
                <executable>pnpm</executable>
                <workingDirectory>${project.basedir}</workingDirectory>
                <arguments>
                    <argument>install</argument>
                    <argument>--frozen-lockfile</argument>
                    <argument>--prod</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

运行时目录：

```text
<app.dir>/node_modules/@innospots/nexus-ai-runtime/
├── dist/server.mjs
├── bin/nexus-ai-runtime.mjs
└── config/
```

`AiRuntimeConfig.runtimeDir` 指向该包根目录（包含 `dist/server.mjs`）。
容器部署时把 `node_modules` 与 Node ≥ 22.19 一起打进镜像。

#### 6.3.2 环境、缓存与离线

- 发布/安装机：Node ≥ 22.19、pnpm、私有 npm registry 可达。
- 运行机：只需要 Node ≥ 22.19，不需要 pnpm，也不需要 npm registry。
- 私有仓库：`.npmrc` 配置 registry 与 `_authToken`；CI 使用 `NPM_TOKEN`。
- 离线/隔离：`pnpm pack` 生成 tarball，引用方用 `file:./nexus-ai-runtime-<v>.tgz` 安装；
  或使用内部 npm 镜像。
- 使用 `--frozen-lockfile` 固定依赖，避免运行包漂移。
- 可加 `--ignore-scripts`（本包没有安装脚本）。

#### 6.3.3 如果不推 npm 仓库

1. `pnpm pack` 生成 tarball，随引用方制品/镜像分发，`file:` 安装；
2. 发布到私有 npm / GitHub Packages / 内部镜像——推荐；
3. 直接把 `runtime/` 源码纳入外部工程，由外部构建 `pnpm build`；
4. 不做 Maven 依赖时，仍可只部署 npm runtime + HTTP 调用。

**结论**：npm 分发是主路径；不推 npm 时必须提供 tarball 或源码构建，不能让运行期联网安装。

### 6.4 构建流水线

Java（不执行 Node）：

```text
mvn deploy
  └─ maven-jar-plugin: thin jar + Nexus-Ai-Runtime-* manifest
```

Node（独立发布）：

```text
runtime/
  ├─ pnpm install --frozen-lockfile
  ├─ pnpm typecheck && pnpm test
  ├─ pnpm build → dist/server.mjs
  └─ pnpm publish --access restricted --tag latest|snapshot
```

发布约束：

- npm 包 `files` 仅包含 `bin/`、`dist/`、`config/`、`README.md`。
- `prepublishOnly` 强制执行 `pnpm build`，避免发布空 dist。
- 同一版本只发布一次；快照使用 `x.y.z-snapshot.<build>` + `snapshot` dist-tag。
- Java jar 与 npm 包可在同一发布流水线中产出；先发布 npm，再发布 jar，或使用同一
  release gate 校验版本一致。
- `runtime/dist`、`runtime/node_modules` 不进 git。
- esbuild 打成单文件 ESM；若 provider SDK 动态导入无法打包，退化为 `tsc` +
  `pnpm deploy --prod`，运行期仍不安装依赖。

## 7. 运行时设计

### 7.1 启动与关闭

```text
Java start()
  ├─ 解析 dataDir 与 runtimeDir（为空时按 §12 的定位顺序），获取文件锁 <dataDir>/runtime.lock
  ├─ 校验 <runtimeDir>/package.json 的 name/version 与 jar manifest 一致
  ├─ 校验 <runtimeDir>/dist/server.mjs 存在、Node 版本满足 [22.19.0,)
  ├─ 创建/校验 <dataDir>/{sessions,workspace,logs,tmp}，从 ConfigSource + 本地 YAML 加载快照
  ├─ 以清洗后的 env 启动 node <runtimeDir>/bin/nexus-ai-runtime.mjs \
  │     --host 127.0.0.1 --port 0 --data-dir <dataDir> --config-dir <configDir> \
  │     --ready-file <dataDir>/runtime.json
  ├─ 读取 stdout 的 readiness 行与 <dataDir>/runtime.json 的 host/port/pid/version
  ├─ 轮询 /v1/health 直至 200
  └─ 状态 RUNNING，暴露 AiEndpoint(baseUri)

Java stop()
  ├─ 发送 SIGTERM，等待进程退出（默认 10s）
  ├─ 超时则 SIGKILL
  ├─ 删除 runtime.json，释放文件锁
  └─ 状态 STOPPED
```

- **Node 不提供 HTTP 关闭端点，也不做请求鉴权**；只监听 `127.0.0.1`，生命周期由 Java supervisor 控制。
- `startupPolicy`：
  - `EAGER`（服务部署默认）：宿主启动时调用 `AiRuntime.start()`；
  - `LAZY`（嵌入式/工具默认）：首次业务请求时启动。
  readiness 不隐式触发启动；未启动时返回 503，状态快照为 `STOPPED/STARTING`。
- 启动成功后启动 `AiRuntimeHealthMonitor`：默认每 5s 调 Node `/v1/health`，超时 1s，
  连续 3 次失败 → `DEGRADED`；可按配置触发重启。
- `runtimeDir` 定位顺序：显式配置 → `NEXUS_AI_RUNTIME_DIR` →
  `${app.dir}/node_modules/@innospots/nexus-ai-runtime` → `${app.dir}/ai/runtime`。
  不再从 jar 解包，也没有 classpath 回退。
- 版本校验：读取 `package.json.version`，必须等于 jar manifest
  `Nexus-Ai-Runtime-Version`；不一致拒绝启动（状态码 `RUNTIME_VERSION_MISMATCH`）。
- 崩溃重启：指数退避（1s、2s、4s），默认最多 3 次；超过后状态 `FAILED`，保留日志。
- 健康检查失败但进程存活 → `DEGRADED`，不再重启，交由调用方决策。
- 单机多实例：文件锁防止同一 `dataDir` 被两个 supervisor 同时启动。

### 7.2 隔离

| 维度 | 措施 |
|------|------|
| 配置 | YAML 文件与 SessionManager 都使用显式路径；不读取 `~/.pi`、`~/.config/pi`、`auth.json`、`models.json` |
| 会话 | `SessionManager.create/open/list` 始终传 `sessionDir=<dataDir>/sessions`、`virtualCwd=<dataDir>/workspace` |
| 凭证 | 不继承宿主 provider 环境变量；加密后存 `<dataDir>/secrets` 或仅存引用 |
| 文件 | `HOME`、`TMPDIR`、`XDG_*`、日志、secrets、sessions 全部指向 `<dataDir>` |
| 进程 | `cwd` = `<runtimeDir>`（npm 包目录）；env 使用**白名单**构造，而非继承后删除 |
| 网络 | 固定绑定 `127.0.0.1`，不提供远程监听模式 |
| 鉴权 | Node 不做鉴权；仅 Java `/ai/**` 对外并统一鉴权 |
| 工具 | 无 shell/代码执行；HTTP 工具做主机白名单与私网拦截；MCP 仅 HTTP/SSE；文件工具仅登记根内 |
| 运行期安装 | 禁止 `npm install`、`pnpm install`、动态下载 |

env 白名单（初版）：

```text
PATH, LANG, LC_ALL, TZ, NODE_ENV=production,
NEXUS_AI_HOME, NEXUS_AI_LOG_LEVEL, NEXUS_AI_MASTER_KEY, NEXUS_AI_CONFIG_URL
```

显式排除：`PI_*`、`OPENAI_*`、`ANTHROPIC_*`、`GOOGLE_*`、`AZURE_*`、`AWS_*`、
`DEEPSEEK_*`、`MOONSHOT_*`、`OPENROUTER_*`、`HF_TOKEN` 等。

### 7.3 持久化模型（pi 原生文件，无 SQLite）

pi 原生不使用 SQLite：会话是 JSONL，模型/凭证/设置是 JSON 文件。本模块遵循同一思路，
**不引入关系型数据库，也不创建 `ai_*` 表**。

#### 7.3.1 会话正文（SessionManager JSONL）

- 存储：每个会话一个 append-only **v3 JSONL** 文件，位于
  `<dataDir>/sessions/<timestamp>_<session-id>.jsonl`。
- 格式：首行 `{"type":"session","version":3,...}`；后续 entry 通过 `id`/`parentId`
  组成树，类型包括 `message`、`model_change`、`thinking_level_change`、`usage`、
  `compaction`、`context_edit`、`branch_summary`、`custom`、`custom_message`、
  `label`、`session_info`。
- 上下文：`SessionManager.buildSessionContext()` 负责 leaf 回溯、compaction、
  context_edit，产出模型可见消息；它是 finalized context 的**唯一权威**。
- 本模块不自行解析或改写 entry 结构；只通过 `appendMessage`、
  `appendModelChange`、`appendThinkingLevelChange`、`appendUsage` 等公开方法写入。
- **会话元数据不建表**：创建会话时追加一条 `custom` entry：

  ```json
  {"type":"custom","customType":"nexus.session","data":{
    "agent":"default",
    "owner":"user-123",
    "agentProfileHash":"sha256:...",
    "promptTemplate":"default-assistant",
    "promptHash":"sha256:...",
    "skills":["pdf-tools"],
    "tools":["http.echo","mcp__docs__search"],
    "mcpServers":["docs"],
    "fileRoot":"workspace",
    "fileRootPath":"/srv/nexus/workspace",
    "fileAccess":"read_write",
    "allowDelete":false
  }}
  ```

- 标题通过 `appendSessionInfo(name)` 写入；会话列表通过
  `SessionManager.list(cwd, sessionDir)` 加读取 `nexus.session` entry 构建，
  进程内缓存，启动或按需重建。
- 用量与费用从 assistant / usage entry 汇总到内存；不落库。
- run 状态只在内存（`runId → status/usage`）；进程重启不恢复未完成 run，
  历史消息仍可从 JSONL 继续。
- 不写 `ai_message` / `ai_session` / `ai_run` 等表。

#### 7.3.2 配置（只读：ConfigSource + 本地 YAML）

配置**只读加载**，不提供维护 API，也不写入任何配置存储。

- **接口来源**：宿主注册的 `ConfigSource`（或通过 `NEXUS_AI_CONFIG_URL` 拉取），
  优先级最高。
- **本地来源**：`<configDir>/runtime.yaml` + `<configDir>/conf.d/**/*.yaml`，
  支持多文件、多文档（`---`），按相对路径字典序加载。
- 优先级：内置默认 < 本地 YAML < `ConfigSource` 接口。
- 资源身份：`(kind, metadata.name)`；同一来源重复定义 → 启动/重载失败并报出位置；
  高优先级来源按整资源覆盖，不做字段级深合并。
- 每次加载生成**不可变配置快照**；失败则保留上一份可用快照并返回错误。
- 重载：`fs.watch`（本地目录，500ms 防抖）或 `SIGHUP`；不提供 HTTP reload 端点。
- 每次 run 在开始时固定配置快照；重载只影响后续 run。
- 配置不落 SQLite；不建 `ai_provider` / `ai_model` / `ai_agent` / `ai_setting` 等表。

`ConfigSource` 契约（Node 侧端口，只读）：

```ts
interface ConfigSource {
  readonly name: string;
  readonly priority: number;
  load(): Promise<ConfigDocument[]>;
  watch?(onChange: () => void): () => void;
}
```

内置实现：

| 实现 | 说明 |
|------|------|
| `FileConfigSource` | 扫描 `configDir` 的 YAML，进程内缓存 |
| `HttpConfigSource` | 可选；从 `NEXUS_AI_CONFIG_URL` 拉取同一 YAML 文档集合，失败时按配置决定是否回退本地 |
| `StaticConfigSource` | 嵌入宿主直接注入文档，便于测试或上层平台下发 |

> **pi 没有 YAML Agent 规范**。`Provider`/`Model` 映射到 pi-ai 的模型注册；
> `AgentProfile`/`Tool`/`McpServer`/`FileRoot` 都是 Nexus 的配置层，不是 pi 原生 schema。

#### 7.3.3 审计（JSONL）

- 文件操作审计追加到 `<dataDir>/audit/file-ops-YYYYMMDD.jsonl`，一行一条：
  时间、会话、run、agent、根、操作、相对路径、字节数、结果、错误码。
- **不记录文件内容**；拒绝与失败也记录。
- 不建 `ai_file_op` 表；需要查询时按日期文件流式读取。

> 所有状态都由 Node 运行时独占，不属于 Java `BaseEntity` / `core` 持久化体系；
> Java 侧没有 Entity、Dao、JDBC。这是本模块保持轻量的前提，见 §14。

### 7.4 模型配置与凭证（只读 YAML / ConfigSource）

模型配置有两个**只读**来源，接口优先：

1. **`ConfigSource` 接口**：宿主注入或 `NEXUS_AI_CONFIG_URL` 拉取，最高优先级。
2. **本地 YAML**：`<configDir>/conf.d/**/*.yaml`，支持多文件、多文档。

**不提供模型维护 API**；配置变更走文件/接口 + 重载。

#### 7.4.1 目录与加载

```text
<configDir>/
├── runtime.yaml                 # Runtime 设置
└── conf.d/
    ├── 10-providers.yaml        # 可含多个 --- 文档
    ├── 20-models.yaml
    ├── 30-agent-profiles.yaml
    ├── 40-tools.yaml
    ├── 50-mcp.yaml
    └── 60-file-roots.yaml
```

- 本地文件按相对路径字典序加载；每个文件支持多个 YAML 文档（`---`）。
- 资源身份为 `(kind, metadata.name)`；同一来源重复定义直接报错并列出文件路径。
- `ConfigSource` 按整资源覆盖低优先级定义，不做字段级深合并。
- 配置在启动时校验并生成不可变快照；失败保留上一份可用快照。
- 本地文件变更由 `fs.watch` 或 `SIGHUP` 触发重载；没有 HTTP reload 端点。
- 不引入 SQLite；配置文件本身就是持久化。

#### 7.4.2 标准 YAML

所有资源使用同一外层：

```yaml
apiVersion: nexus.ai/v1
kind: <Runtime|Provider|Model|AgentProfile|PromptTemplate|Skill|Tool|McpServer|FileRoot>
metadata:
  name: <稳定名称>
spec:
  enabled: true
```

**Runtime**（`runtime.yaml`）：

```yaml
apiVersion: nexus.ai/v1
kind: Runtime
metadata:
  name: default
spec:
  logLevel: info
  maxConcurrentRuns: 2
  runTimeoutSeconds: 300
  maxMessageBytes: 1048576
  maxFileReadBytes: 262144
  maxFileWriteBytes: 1048576
  maxFileListEntries: 1000
  maxFileOpsPerRun: 100
  trashRetentionDays: 7
  allowSymlinks: false
  toolHttpAllowlist: []
```

**Provider**：

```yaml
apiVersion: nexus.ai/v1
kind: Provider
metadata:
  name: openai-main
spec:
  type: openai                  # openai/anthropic/google/deepseek/openrouter/openai-compatible
  baseUrl: https://api.openai.com/v1
  apiKey:
    fromEnv: OPENAI_API_KEY     # 从 <dataDir>/secrets.env 读取，不读宿主环境
  enabled: true
---
apiVersion: nexus.ai/v1
kind: Provider
metadata:
  name: internal-gateway
spec:
  type: openai-compatible
  baseUrl: https://llm.internal.example/v1
  apiKey:
    secretRef: internal-gateway # 部署工具/密钥管理系统写入的加密秘密引用
  headers:
    X-Tenant: nexus
```

**Model**：

```yaml
apiVersion: nexus.ai/v1
kind: Model
metadata:
  name: gpt-main
spec:
  provider: openai-main
  modelId: gpt-5.1
  displayName: GPT-5.1
  contextWindow: 400000
  maxOutputTokens: 128000
  capabilities: [text, tool, vision]
  defaultParams:
    temperature: 0.2
  enabled: true
```

**AgentProfile**（Nexus 配置，不是 pi 原生规范）：

```yaml
apiVersion: nexus.ai/v1
kind: AgentProfile
metadata:
  name: default
spec:
  displayName: Default Assistant
  model: gpt-main
  promptTemplate: default-assistant        # 或 systemPrompt / systemPromptFile
  promptVariables:
    role: enterprise assistant
  thinkingLevel: medium
  toolExecution: parallel
  maxTurns: 12
  maxToolCalls: 100
  mcpServers: [docs]
  skills: [pdf-tools]                      # Nexus Skill，加载 SKILL.md 内容
  tools:
    - http.echo
    - mcp__docs__search
  fileRoot: workspace
  fileAccess: read_write                  # none/read/read_write
  allowDelete: false
  enabled: true
```

**PromptTemplate**（Nexus 配置，不是 pi 规范）：

```yaml
apiVersion: nexus.ai/v1
kind: PromptTemplate
metadata:
  name: default-assistant
spec:
  content: |
    You are ${role}. Never access paths outside the sandbox.
  variables:
    role: { required: true, default: a helpful enterprise assistant }
```

**Skill**（引入已有 skills 目录，不加载 pi skill runtime）：

```yaml
apiVersion: nexus.ai/v1
kind: Skill
metadata:
  name: pdf-tools
spec:
  path: /opt/agent-skills/pdf-tools
  mode: inline                            # V1 只支持 inline
  include: [SKILL.md, references/**]
  maxBytes: 131072
```

V1 只实现 `inline`：配置加载时拼接，prompt 组合确定；`index` 保留到 V1.1。

详见 [ai-runtime-config.md](ai-runtime-config.md) §8.3。

**Tool**：

```yaml
apiVersion: nexus.ai/v1
kind: Tool
metadata:
  name: http.echo
spec:
  type: http
  method: POST
  url: https://api.internal.example/echo
  headers:
    Content-Type: application/json
  parameters:
    type: object
    properties:
      text: { type: string }
    required: [text]
  timeoutSeconds: 15
  allowPrivateNetwork: false
  enabled: true
```

**McpServer**：

```yaml
apiVersion: nexus.ai/v1
kind: McpServer
metadata:
  name: docs
spec:
  transport: http                # http/sse
  url: https://mcp.internal.example/mcp
  headers:
    Authorization: Bearer ${DOCS_TOKEN}
  enabled: true
```

**FileRoot**：

```yaml
apiVersion: nexus.ai/v1
kind: FileRoot
metadata:
  name: workspace
spec:
  path: /srv/nexus/workspace     # 绝对路径，不能位于 dataDir 内
  enabled: true
```

#### 7.4.3 合并、来源与禁用

- 同一 `(kind, name)`：高优先级 `ConfigSource` 覆盖本地 YAML；按整资源覆盖。
- 配置只读，**没有 API PATCH/DELETE**；本地文件与接口来源都不被运行时修改。
- 禁用资源：在配置中设置 `spec.enabled: false`，重载后不进入运行快照。
- `/v1/runtime`（内部诊断）可返回当前快照版本与各资源的来源文件，便于排障；
  该端点不对外反代。

#### 7.4.4 凭证

- `fromEnv`：只从 `<dataDir>/secrets.env`（0600）或 `NEXUS_AI_` 前缀变量读取，
  **不读取宿主 `OPENAI_API_KEY` 等变量**。
- `secretRef`：指向 `<dataDir>/secrets/<name>.enc`，AES-256-GCM 加密；由部署工具或
  密钥管理系统写入，不通过运行时 API 管理。
- 本地 YAML 中禁止出现明文密钥；加载时检测到即拒绝。
- `headers` 中的 `${VAR}` 只从 `<dataDir>/secrets.env` 或 `NEXUS_AI_` 变量解析；
  未解析的占位符直接失败。
- 主密钥：`NEXUS_AI_MASTER_KEY`（推荐）或 `<dataDir>/master.key`（0600，首次生成）。

#### 7.4.5 Provider 白名单

- V1 只注册：`openai`、`anthropic`、`google`、`deepseek`、`openrouter`、
  `openai-compatible`，避免把 Bedrock/Vertex 等云 SDK 打进产物。
- 模型连通性由启动校验或内部诊断完成，不对外提供 probe API。

### 7.5 Agent 与会话

- 一个 `AgentProfile` 配置 = 模型 + system prompt + 工具白名单 + MCP 作用域 + 技能 + 文件权限 + 运行限制。
- **每个 AgentProfile 的能力集合独立**：`skills` / `tools` / `mcpServers` / `fileRoot` 不共享、
  不自动继承；全局 `Skill` / `Tool` / `McpServer` 只是可复用定义。
- 一个会话对应一个 `SessionManager` 与一个 v3 JSONL 文件，绑定一个 AgentProfile；
  `sessionId` 由 `SessionManager` 生成，不写入任何数据库。
- 创建：`SessionManager.create(virtualCwd, sessionDir, { id? })`
  - `sessionDir = <dataDir>/sessions`
  - `virtualCwd = <dataDir>/workspace`（固定值，避免泄露宿主路径，也避免按宿主目录分组）
  - 两个参数必须显式传入；禁止回退到 `~/.pi/agent/sessions`
  - 随后 `appendCustomEntry("nexus.session", { agent, owner, promptTemplate, promptHash,
    fileRoot, fileRootPath, fileAccess, allowDelete })`
  - `owner` 来自 Java 代理注入的 `X-Nexus-User`（console 当前用户），Node 只信任
    loopback 调用方。
- 打开：`SessionManager.list(cwd, sessionDir)` 按 id 定位文件后调
  `SessionManager.open(path, sessionDir, virtualCwd)`；列表在进程内缓存。
- `GET /v1/sessions` / 详情 / 删除按 `owner` 过滤，避免跨用户读取。

#### 7.5.1 运行时桥接

```text
run 开始
  ├─ manager.buildSessionContext() → 模型可见消息 / model / thinking
  ├─ 构造 pi-agent-core Agent（initialState 使用该上下文）
  ├─ manager.appendMessage(userMessage)
  ├─ agent.prompt(...) → SSE 事件
  ├─ 每个 assistant / toolResult finalized → manager.appendMessage(...)
  ├─ 模型或推理档位变化 → appendModelChange / appendThinkingLevelChange
  └─ run 结束 → 更新内存中的 session 摘要（消息数/用量）
```

- **`SessionManager` 是会话正文与模型上下文的唯一真源**。不得把 `agent.state.messages`
  另行持久化，也不得用额外消息表回放上下文。
- 取消：透传 `AbortSignal`；把已产生的部分 assistant message 以
  `stopReason: "aborted"` 追加，保留已完成的 toolResult。
- 分支、压缩、context edit 由 `SessionManager` 原生能力承担；V1 API 只暴露线性会话，
  底层数据已是树结构，后续开放 `/branch`、`/compact` 不需要数据迁移。
- 历史查询：`GET /v1/sessions/{id}/messages` 使用 `getEntries()` /
  `buildContextEntries()` 读取当前分支；不查数据库，也不另存消息副本。
- 会话标题变更通过 `appendSessionInfo(name)` 写入 `session_info`；列表缓存按文件
  mtime/大小失效，不做数据库同步。
- 自动压缩：V1 先使用 `buildSessionContext()` 的原生结果；后续通过
  `appendCompaction()` 接入摘要生成，摘要本身仍是一次模型调用。
- 每个会话同一时刻最多一个 run；并发请求返回 `run_conflict`。
- 全局并发 run 数、单 run 超时、最大工具调用次数由 `Runtime` YAML 配置控制。

#### 7.5.2 依赖与体积

`SessionManager` 来自 `@earendil-works/pi-coding-agent`，包根同时导出编码 agent、TUI、
codemode 等能力。本模块：

- 只导入 `SessionManager`，不调用 `createAgentSession()`，不加载项目资源、skills、TUI；
- 在 P1 设置 bundle 体积门禁；若 tree-shaking 后体积不可接受，退回「复用 JSONL 格式、
  自研轻量 Store」的实现，保持同一 `SessionStore` 端口不变。


### 7.6 工具

| 类型 | 能力 | 安全约束 |
|------|------|----------|
| HTTP 工具 | 由配置定义：method、URL 模板、headers、JSON Schema | 域名白名单；默认拒绝私网/回环/元数据地址；超时；响应大小上限 |
| MCP 工具 | 连接远程 MCP Server，发现并调用工具 | 仅 `http` / `sse`；工具名 `mcp__<server>__<tool>`；按 AgentProfile 白名单启用 |
| 文件工具 | 只读 / 读写 / 软删除，见 §7.7 | 仅登记根内；默认关闭；无 shell、无代码执行 |
| 其他内置工具 | 无 | 不提供 shell、代码执行、任意路径访问 |

工具执行结果统一返回 `toolResult` 消息；失败以 `isError=true` 回传模型，不中断 run。
MCP stdio 传输保留字段但 V1 拒绝启动，避免隐式进程执行。

### 7.7 受限文件工具

文件工具默认关闭，P3 交付；以下是契约与安全边界。

#### 7.7.1 根与权限

- 管理端通过 `FileRoot` 配置定义文件根（只读加载）：路径必须存在、是目录、
  规范化后不位于 `<dataDir>` 内，也不得包含 `<dataDir>`；拒绝 `/`，建议同时
  拒绝用户 home 与系统目录。
- AgentProfile 绑定一个文件根，并声明：
  - `fileAccess`: `none` / `read` / `read_write`
  - `allowDelete`: 是否允许软删除
- `read` 只注册 `file_read`、`file_list`；`read_write` 额外注册
  `file_write`、`file_edit`；`allowDelete=false` 时 `file_delete` 不注册。
- 文件工具不受 `AgentProfile.spec.tools` 控制，由 `fileAccess` / `allowDelete` 独立控制。
- 会话创建时**快照**根路径与权限，写入 JSONL custom entry（`nexus.session`）；
  之后修改 AgentProfile 配置不影响已有会话。
- 每次文件操作校验配置快照中的 `FileRoot` 仍 `enabled`，且与快照 path 一致；
  管理端禁用/删除根后，已有会话立即失去访问。撤销单会话权限需删除会话或禁用根。
- `fileAccess=none` 时不注册文件工具，模型看不到工具定义，避免无效调用。
- 文件工具不复用 `pi-coding-agent` 的内置 read/write/edit/bash：它们绑定编码 agent 的
  `cwd` 与权限模型，无法保证仅限登记根。本模块自研工具，统一走 `PathGuard`。
- 根目录不可用（被删除、权限不足、变成符号链接）时，文件工具统一返回
  `file_root_unavailable`，不影响其他会话。

#### 7.7.2 工具清单

| 工具 | 参数 | 行为 |
|------|------|------|
| `file_read` | `path`、`offset`、`limit` | 读取 UTF-8 文本；按行分页；拒绝二进制 |
| `file_list` | `path="."`、`depth=1` | 列出目录项：名称、类型、大小、mtime；不跟随符号链接 |
| `file_write` | `path`、`content`、`overwrite=false` | 新建或覆盖；在根内按需创建父目录；临时文件 + rename |
| `file_edit` | `path`、`oldText`、`newText`、`replaceAll=false` | 精确文本替换；默认要求唯一匹配；原子写回 |
| `file_delete` | `path` | 仅文件或空目录；软删除到 `<dataDir>/trash`；V1 不支持递归删除 |

约束：

- 仅 UTF-8 文本；出现 NUL 或判定为二进制时拒绝。
- 读上限 256 KiB、写/编辑上限 1 MiB、列表上限 1000 项、单 run 文件操作上限 100 次，
  全部由 `Runtime` YAML 调整。
- 工具描述中的根目录统一叫 `workspace`，输入只接受相对路径；不向模型暴露宿主机绝对路径。
- `file_write` 不改变已有文件权限；新建文件默认 `0640`。

#### 7.7.3 路径防护

1. 拒绝绝对路径、Windows 盘符、UNC、NUL 字符；规范化后拒绝任何 `..` 段。
2. 根目录先 `realpath` 一次并缓存；目标已存在时 `realpath` 后使用
   `path.relative(root, target)` 判断：结果不得为空以外的绝对路径，也不得包含
   以 `..` 开头的段。
3. 目标不存在时，先对最近的已存在祖先做 `realpath` 校验，再拼接剩余段；
   创建过程中逐段检查，避免中途替换为符号链接。
4. 默认拒绝符号链接（`allowSymlinks=false`）；即使开启，解析后也不得越出根。
5. 只允许普通文件与目录；拒绝设备、FIFO、socket、块设备。
6. 读/写打开使用 `O_NOFOLLOW`，打开后 `fstat` 再校验一次；变更操作按根加进程内互斥锁。
   多 runtime 共享同一根不受支持，需要外部协调或容器隔离。
7. 默认拒绝敏感路径：`.git/**`、`.env*`、`.ssh/**`、`id_rsa*`、`*.pem`、`*.key`、
   `*.p12`、`.aws/**`、`.kube/**`；可通过 `fileDenyPatterns` 调整。
8. 路径校验是应用层防护，仍存在 TOCTOU 窗口；高安全场景应把 Node 进程放入容器，
   只挂载允许的根目录，`dataDir` 单独挂载。

#### 7.7.4 软删除与审计

- 删除移动到 `<dataDir>/trash/<rootId>/<yyyyMMdd>/<ulid>/<相对路径>`；跨卷时
  退化为 `copyFile + unlink`，同名冲突追加 ULID。
- 默认保留 7 天；启动时与每日任务清理过期 trash。V1 不提供恢复 API，
  管理端可按审计记录手工恢复。
- 每次操作（含拒绝与失败）追加到 `audit/file-ops-YYYYMMDD.jsonl`：会话、run、根、
  操作、相对路径、字节数、结果、错误码；**不记录文件内容**。
- 不建 `ai_file_op` 表；查询时按日期流式读取。
- 一旦允许 `file_read`，文件内容可能随对话发送到配置的模型供应商或 HTTP 工具；
  这是管理端的显式授权，需在启用前确认。


### 7.8 健康检查与可用性

#### 7.8.1 Java 状态端点（不依赖 Node 存活）

| 方法 | 路径 | 鉴权 | 语义 |
|------|------|------|------|
| GET | `/ai/v1/health/live` | 匿名 | JVM/进程存活即 200，不检查 Node |
| GET | `/ai/v1/health/ready` | 匿名 | Node RUNNING、配置已加载、最近健康检查通过才 200；否则 503 |
| GET | `/ai/v1/health` | 匿名 | 聚合状态与诊断信息；200=UP，503=DEGRADED/DOWN |

`/ai/v1/health` 响应示例：

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

- 响应头 `Cache-Control: no-store`；不返回密钥、配置正文、文件路径。
- `live` 永不因 Node 故障返回 503；`ready` 用于负载均衡/k8s readiness。
- `ready` 未就绪示例（HTTP 503）：

```json
{
  "status": "DOWN",
  "reason": "runtime_starting",
  "state": "STARTING",
  "retryAfterSeconds": 2,
  "checkedAt": "2026-11-26T10:00:00Z"
}
```

- 状态端点由 Java 实现，**不反向代理** Node `/v1/health`。

#### 7.8.2 业务端点可用性门禁

所有代理业务端点（agents/sessions/messages/runs）在转发前调用 `AiAvailabilityGuard.ensure()`：

| 状态 | 行为 |
|------|------|
| `RUNNING` 且最近健康检查通过 | 正常转发 |
| `STOPPED` + `LAZY` | 尝试启动（互斥锁 + 冷却窗口）；成功则转发，失败 503 |
| `STOPPED` + `EAGER` | 503 `runtime_unavailable`，`Retry-After: 2` |
| `STARTING` | 503 `runtime_starting`，`Retry-After: 2` |
| `DEGRADED` | 503 `runtime_unavailable`，`Retry-After: 5`；不转发到 Node |
| `FAILED` | 503 `runtime_unavailable`，`Retry-After: 10`；等待退避重启 |
| 配置无效 | 503 `config_invalid`，`Retry-After: 30` |

错误体沿用 Node 风格：

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

- 不可用时**不建立 SSE**，客户端立即收到 503，避免长时间挂起。
- `Retry-After` 与 `details.retryAfterSeconds` 同时给出。
- 启动失败会记录 stderr 尾部；`/ai/v1/health` 的 `runtime.error` 返回可诊断摘要。

#### 7.8.3 状态快照与刷新

- `AiRuntime.health()` 返回不可变快照：`state`、`since`、`nodeVersion`、
  `runtimeVersion`、`pid`、`lastCheckAt`、`latencyMs`、`error`、`configVersion`。
- 健康检查结果缓存 `healthCacheTtl`（默认 2s），避免每个请求都打 Node。
- 后台 `AiRuntimeHealthMonitor` 默认每 5s 检查一次；连续 3 次失败 → `DEGRADED`。
- 启动/重启互斥，避免并发请求触发多次进程。
- `health/ready` 与业务门禁读同一快照，保证判定一致。

### 7.9 CLI 启动命令

npm 包安装后提供统一入口 **`nexus-ai`**（别名 `nexus-ai-runtime`）：

```bash
# 本机安装（推荐，安装一次）
pnpm add -g @innospots/nexus-ai-runtime

# 零参数启动：~/.nexus-ai + 127.0.0.1:18080
nexus-ai

# 只改端口
nexus-ai --port 18081

# 指定数据/配置目录
nexus-ai --data-dir /srv/nexus-ai --config-dir /etc/nexus-ai
```

项目内安装时：

```bash
pnpm exec nexus-ai
```

临时运行：

```bash
npx @innospots/nexus-ai-runtime
```

> `bin/nexus-ai-runtime.mjs` 只是 ESM 入口文件；npm 会根据 `bin` 生成 `nexus-ai`
> 可执行命令，用户不需要输入 `.mjs`。

#### 7.9.1 参数

| 参数 | 默认 | 说明 |
|------|------|------|
| `--host` | `127.0.0.1` | 仅允许回环地址；其他值直接拒绝 |
| `--port` | `18080` | CLI 默认固定端口；Java supervisor 显式传 `0` 使用随机端口 |
| `--data-dir` | `~/.nexus-ai` | sessions、日志、secrets、ready/pid 文件 |
| `--config-dir` | `<data-dir>/config` | `runtime.yaml` + `conf.d/` || `--config-url` | 空 | 可选 `ConfigSource` HTTP 地址，优先于本地 YAML |
| `--ready-file` | `<data-dir>/runtime.json` | 原子写入 `{host,port,pid,version,configVersion}` |
| `--pid-file` | 空 | 可选 PID 文件 |
| `--log-level` | `info` | `debug/info/warn/error` |
| `--log-format` | `json` | `json` / `pretty`（TTY 默认 pretty） |
| `--no-watch` | 关 | 关闭本地 YAML `fs.watch`，只保留 SIGHUP 重载 |
| `--version` | - | 打印 runtime 版本 |
| `--help` | - | 打印参数说明 |

环境变量（优先级低于 CLI）：

```text
NEXUS_AI_HOST, NEXUS_AI_PORT, NEXUS_AI_DATA_DIR, NEXUS_AI_CONFIG_DIR,
NEXUS_AI_CONFIG_URL, NEXUS_AI_READY_FILE, NEXUS_AI_LOG_LEVEL
```

优先级：**CLI > 环境变量 > 默认值**；`runtime.yaml` 只提供运行参数，不覆盖进程绑定参数。

`--config-dir` 不存在时以空配置启动（仅健康检查可用），首次模型调用返回
`model_not_configured`；运行时不自动创建或修改配置。`--data-dir` 会自动创建。

#### 7.9.2 启动输出与退出码

启动成功后在 stdout 打印一行 JSON，并原子写入 `--ready-file`：

```json
{"event":"listening","host":"127.0.0.1","port":18080,"pid":1234,
 "version":"0.1.0-SNAPSHOT","configVersion":"sha256:..."}
```

退出码：

| 码 | 含义 |
|----|------|
| 0 | 正常退出 |
| 2 | 参数或配置非法 |
| 3 | Node 版本不满足 |
| 4 | 端口绑定失败 |
| 5 | 启动失败 |
| 6 | ConfigSource 拉取/解析失败 |

信号：

- `SIGTERM` / `SIGINT`：停止接收新请求，等待进行中的 run 到超时，退出。
- `SIGHUP`：重新加载配置；失败保留旧快照。
- 不自带 daemon 化；由 systemd/容器/Java supervisor 管理。

#### 7.9.3 Java supervisor 使用的命令

```text
node <runtimeDir>/bin/nexus-ai-runtime.mjs \
  --host 127.0.0.1 \
  --port 0 \
  --data-dir <dataDir> \
  --config-dir <configDir> \
  --ready-file <dataDir>/runtime.json
```

- 不经过 shell，直接 `spawn(node, args)`。
- 不传 `--token`（Node 不鉴权）。
- 端口从 readiness 行或 `runtime.json` 读取；再轮询 `/v1/health`。

## 8. Node 内部 API 契约

### 8.1 通用约定

- Base path：`/v1`，仅监听 `127.0.0.1`，**不做鉴权**；唯一调用方是 Java `/ai/**` 代理。
- 对外路径映射：`/ai/v1/**` → Node `/v1/**`，方法、查询串、请求体、状态码与 SSE 原样透传。
- 编码：`application/json; charset=utf-8`；流式：`text/event-stream`
- 请求 ID：响应头 `X-Request-Id`，错误体回带
- 错误体：

```json
{
  "error": {
    "code": "model_not_configured",
    "message": "No enabled model is configured",
    "requestId": "01J9Z8K3Q7W6M2V4T1B0C5N8XA",
    "details": {}
  }
}
```

`code` 为稳定 snake_case 标识；HTTP 状态码表达类别，`details` 不包含密钥与堆栈。

Node `/v1/health`（内部）：

```json
{
  "status": "UP",
  "version": "0.1.0-SNAPSHOT",
  "node": "22.19.0",
  "configVersion": "sha256:...",
  "checks": { "config": "UP", "providers": "SKIPPED" }
}
```

Java 只读取 `status`、`version`、`configVersion` 与 `checks`，不向外部透传 Node 端口。

### 8.2 端点

| 方法 | 路径 | 对外 | 说明 |
|------|------|------|------|
| GET | `/v1/health` | 否 | 供 Java 健康监控；返回 status/version/node/configVersion/checks |
| GET | `/v1/openapi.yaml` | 是 | Node 生成，经 `/ai/v1/openapi.yaml` 暴露 |
| GET | `/v1/agents` | 是（只读） | AgentProfile 列表；脱敏，不含 system prompt/密钥 |
| POST | `/v1/sessions` | 是 | 创建会话；body `{agent, title?}` |
| GET | `/v1/sessions` | 是 | 按 `owner` 过滤 |
| GET | `/v1/sessions/{id}` | 是 | owner 校验 |
| DELETE | `/v1/sessions/{id}` | 是 | owner 校验；删 JSONL |
| GET | `/v1/sessions/{id}/messages` | 是 | 分页历史（`before`、`limit`） |
| POST | `/v1/sessions/{id}/messages` | 是 | 发送消息；`Accept: text/event-stream` 走 SSE |
| GET | `/v1/runs/{id}` | 是 | run 状态、用量、错误 |
| POST | `/v1/runs/{id}/cancel` | 是 | 取消运行 |
| GET | `/v1/runtime` | 否 | 内部诊断：版本、快照版本、配置来源 |

**没有配置维护端点**：`Provider` / `Model` / `AgentProfile` / `Tool` / `McpServer` /
`FileRoot` 只从 `ConfigSource` / 本地 YAML 加载；不提供 POST/PATCH/DELETE。
配置变更由文件监听或 `SIGHUP` 触发，不通过 HTTP。

`AgentProfile.fileRoot` / `fileAccess` / `allowDelete` 写入 JSONL custom entry 快照；
文件内容不通过 REST 读写，只由 Agent 工具访问。

### 8.3 消息与流式事件

请求：

```json
{
  "input": "帮我汇总本周告警",
  "stream": true,
  "options": { "thinkingLevel": "medium" }
}
```

SSE 事件（`event:` + 单行 JSON `data:`）：

| 事件 | 负载要点 |
|------|----------|
| `run_started` | `runId`、`sessionId`、`messageId` |
| `message_started` | 助手消息占位 |
| `text_delta` | `delta` |
| `thinking_delta` | `delta`（模型支持时） |
| `tool_started` | `toolCallId`、`name`、`arguments` |
| `tool_finished` | `toolCallId`、`isError`、`preview` |
| `message_completed` | 完整消息、usage |
| `run_completed` | `status`（succeeded/aborted/failed）、usage |
| `error` | 错误码与消息；随后关闭流 |
| `heartbeat` | 每 15s，防代理超时 |

非流式请求返回 `message_completed` 的完整结构。SSE 断开不取消 run，
客户端可用 `runId` 重连查询状态；重放事件不在 V1 范围。

## 9. Java 契约

### 9.1 公共 API（契约骨架）

```java
public interface AiRuntime extends AutoCloseable {

    void start();

    void stop();

    AiRuntimeStatus status();

    AiRuntimeHealth health();

    Optional<AiEndpoint> endpoint();
}

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

public record AiRuntimeConfig(
        Path dataDir,
        Path configDir,
        Path runtimeDir,
        String nodeExecutable,
        String bindHost,
        int port,
        Duration startupTimeout,
        Duration shutdownTimeout,
        int maxRestarts,
        Map<String, String> environment) {
}

public record AiEndpoint(URI baseUri) {
}

public record AiProxyConfig(
        String pathPrefix,
        int maxRequestBodyBytes,
        Duration connectTimeout,
        Duration requestTimeout,
        Duration sseIdleTimeout) {
}

public enum AiRuntimeStatus {
    STOPPED, STARTING, RUNNING, DEGRADED, STOPPING, FAILED
}
```

`AiHealthEndpoint` 本地实现 health/live/ready；`AiRuntimeProxyEndpoint` 代理 openapi/agents；
`AiSessionProxyEndpoint` 代理 sessions/runs。`AiProxyService` 是包内实现。

### 9.2 `/ai/**` 代理契约

| 项 | 规则 |
|----|------|
| 路径映射 | `/ai/v1/**` → Node `/v1/**`；外部路径不带 `/ai` 转发 |
| 方法 | 仅 `GET`、`POST`、`DELETE`；其他返回 405 |
| 鉴权 | console Bearer；health 三条匿名；GET 需 `ai.read`，写操作需 `ai.write` |
| 授权实现 | 代理方法上声明 `@EndpointGrant`；装配层把 `/ai/**` 纳入 `consolePathPatterns`，`recordArgs=false` |
| 对外路由 | 代理仅下表 10 条 + 本地 health 3 条；不在表内一律 404，不做前缀放行 |
| 请求头 | 透传 `Accept`、`Content-Type`、`Last-Event-ID`、`X-Request-Id`；丢弃 `Authorization`、`Cookie`、`Host`、hop-by-hop 及外部 `X-Nexus-*`；注入 `X-Nexus-User` |
| 响应头 | 透传 `Content-Type`、`Cache-Control`、`X-Request-Id`、`Retry-After`；丢弃 `Set-Cookie`、`Transfer-Encoding`、`Connection` |
| 请求体 | 上限 8 MiB（可配）；超限 413 |
| 普通响应 | 原状态码与响应体；不包装、不二次序列化 |
| SSE | `text/event-stream` 逐块 flush；不缓冲、不压缩；客户端断连即关闭上游 |
| 超时 | 普通请求 60s；SSE 无总超时，靠心跳与空闲检测 |
| 代理自身失败 | Node 风格错误体 `{error:{code,message,requestId}}`，不泄露内部路径与堆栈 |
| OpenAPI | `/ai/v1/openapi.yaml` 透传 Node 生成；Java 不生成 OpenAPI 索引 |

**本地状态端点（不反代、匿名）**：

| 方法 | 路径 | 语义 |
|------|------|------|
| GET | `/ai/v1/health` | 聚合状态；200/503 |
| GET | `/ai/v1/health/live` | JVM 存活；200 |
| GET | `/ai/v1/health/ready` | Node + 配置就绪；200/503 |

**代理路由表（最小调用面）**：

| 方法 | 路径 | Grant |
|------|------|-------|
| GET | `/ai/v1/openapi.yaml` | 匿名 |
| GET | `/ai/v1/agents` | `ai.read` |
| POST | `/ai/v1/sessions` | `ai.write` |
| GET | `/ai/v1/sessions` | `ai.read` |
| GET | `/ai/v1/sessions/{id}` | `ai.read` |
| DELETE | `/ai/v1/sessions/{id}` | `ai.write` |
| GET | `/ai/v1/sessions/{id}/messages` | `ai.read` |
| POST | `/ai/v1/sessions/{id}/messages` | `ai.write` |
| GET | `/ai/v1/runs/{id}` | `ai.read` |
| POST | `/ai/v1/runs/{id}/cancel` | `ai.write` |

不反代：Node `/v1/health`、`/v1/runtime`、`/v1/providers*`、`/v1/models*`、`/v1/config*`、
`/v1/tools*`、`/v1/mcp*`、`/v1/file-roots*`。配置没有维护端点。

`/ai/**` 是唯一对外入口；Node 的 `127.0.0.1` 端口不写入任何外部配置，也不下发内部凭证。

#### 9.2.1 端点骨架

JAX-RS 路径模板按段匹配，不能用单个 `{path:.*}` 可靠覆盖 `/v1/sessions/{id}/messages`
这类多段路径。因此代理端点按资源组显式声明路径，共用 `AiProxyService.forward(...)`；
只复制路由，不复制 DTO 与业务语义。

```java
@Path("/ai/v1")
@EndpointModule("ai")
public final class AiSessionProxyEndpoint {

    @GET
    @Path("/sessions")
    @EndpointGrant(action = READ, name = "ai.read", recordArgs = false)
    public void listSessions(@Context UriInfo uriInfo,
                             @Context HttpHeaders headers,
                             @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/sessions", uriInfo, headers, null, response);
    }

    @POST
    @Path("/sessions")
    @EndpointGrant(action = CREATE, name = "ai.write", recordArgs = false, auditLog = true)
    public void createSession(@Context UriInfo uriInfo,
                              @Context HttpHeaders headers,
                              InputStream body,
                              @Suspended AsyncResponse response) {
        proxy.forward("POST", "/v1/sessions", uriInfo, headers, body, response);
    }

    @GET
    @Path("/sessions/{id}/messages")
    @EndpointGrant(action = READ, name = "ai.read", recordArgs = false)
    public void listMessages(@PathParam("id") String id, ...) { }

    @POST
    @Path("/sessions/{id}/messages")
    @EndpointGrant(action = CREATE, name = "ai.write", recordArgs = false, auditLog = true)
    public void sendMessage(@PathParam("id") String id, ..., InputStream body,
                            @Suspended AsyncResponse response) {
        // Accept: text/event-stream 时使用 StreamingOutput，逐块 flush
    }

    @POST
    @Path("/runs/{id}/cancel")
    @EndpointGrant(action = UPDATE, name = "ai.write", recordArgs = false, auditLog = true)
    public void cancelRun(@PathParam("id") String id, ...) { }
}
```

同样按组声明：

| 资源组 | 端点类 | 覆盖路径 |
|--------|--------|----------|
| 健康检查 | `AiHealthEndpoint` | `/health`、`/health/live`、`/health/ready` |
| 运行时/Agent 列表 | `AiRuntimeProxyEndpoint` | `/openapi.yaml`、`/agents` |
| 会话 | `AiSessionProxyEndpoint` | `/sessions*`、`/runs*` |

实现约定：

- 转发使用 JDK `HttpClient`，以 `BodyHandlers.ofInputStream()` 读取上游；SSE 直接写入
  `StreamingOutput`，不聚合完整响应。
- 请求体按流计数，超过 `maxRequestBodyBytes` 立即终止并返回 413。
- 上游连接失败 → `PROXY_UPSTREAM_UNAVAILABLE`；超时 → `PROXY_UPSTREAM_TIMEOUT`；
  流中断 → `PROXY_STREAM_FAILED`。
- 代理层失败使用 Node 风格错误体，便于调用方统一处理；console 认证/授权失败仍由
  console 异常映射返回 `R`。

#### 9.2.2 装配要求

引用方装配层（Spring/Quarkus）需要：

1. 注册 console 的认证、权限、审计与异常映射 Filter/Provider。
2. 将 `ConsoleWebSecuritySettings.consolePathPatterns` 扩展为包含 `/ai/**`，
   使 `ai.read` / `ai.write` 参与授权判定。
3. 注册 3 个 AI 端点类（health / runtime proxy / session proxy），并提供 `AiRuntime` 与 `AiProxyConfig` 实例。
4. 由宿主生命周期调用 `AiRuntime.start()` / `close()`，或依赖首次请求惰性启动。
5. `/ai/v1/health`、`/health/live`、`/health/ready` 通过方法级
   `ignoreAuth/ignorePermission` 或 permitAll 路径保持匿名。

模块本身不提供 Spring/Quarkus 自动配置，避免把 framework binding 带进中立库。

### 9.3 状态码（`NAI` 前缀）

| 枚举 | localCode | 分类 | HTTP | 语义 |
|------|-----------|------|------|------|
| `RUNTIME_NOT_STARTED` | 0001 | CONFIGURATION | 503 | 未启动即调用 |
| `RUNTIME_START_FAILED` | 0002 | EXTERNAL_FAILURE | 503 | 进程启动失败 |
| `RUNTIME_ALREADY_STARTED` | 0003 | CONFIGURATION | 409 | 重复启动 |
| `RUNTIME_UNHEALTHY` | 0004 | EXTERNAL_FAILURE | 503 | 健康检查失败 |
| `RUNTIME_STOP_TIMEOUT` | 0005 | EXTERNAL_FAILURE | 500 | 关闭超时 |
| `NODE_NOT_AVAILABLE` | 0006 | CONFIGURATION | 500 | 找不到 Node 或版本不满足 |
| `RUNTIME_PACKAGE_MISSING` | 0007 | RESOURCE_DATA | 500 | npm runtime 包或 dist/server.mjs 缺失 |
| `RUNTIME_VERSION_MISMATCH` | 0008 | CONFIGURATION | 500 | npm 包版本与 jar manifest 不一致 |
| `CLIENT_REQUEST_FAILED` | 0009 | EXTERNAL_FAILURE | 502 | 上游请求失败 |
| `PROXY_REQUEST_DENIED` | 0010 | PERMISSION_SECURITY | 403 | 路径/方法不在白名单 |
| `INVALID_RUNTIME_CONFIG` | 0011 | CONFIGURATION | 400 | 配置非法 |
| `DATA_DIR_LOCKED` | 0012 | RESOURCE_LIMIT | 409 | dataDir 已被占用 |
| `PROXY_BODY_TOO_LARGE` | 0013 | INPUT_VALIDATION | 413 | 请求体超限 |
| `PROXY_UPSTREAM_UNAVAILABLE` | 0014 | EXTERNAL_FAILURE | 503 | Node 不可达 |
| `PROXY_UPSTREAM_TIMEOUT` | 0015 | EXTERNAL_FAILURE | 504 | 上游超时 |
| `PROXY_STREAM_FAILED` | 0016 | EXTERNAL_FAILURE | 502 | SSE 转发中断 |
| `PROXY_REQUEST_INVALID` | 0017 | INPUT_VALIDATION | 400 | 代理请求非法 |
| `PROXY_METHOD_NOT_ALLOWED` | 0018 | INPUT_VALIDATION | 405 | HTTP 方法不在白名单 |
| `PROXY_PATH_NOT_FOUND` | 0019 | RESOURCE_DATA | 404 | 路径不在代理路由表 |
| `RUNTIME_STARTING` | 0020 | CONFIGURATION | 503 | 正在启动 |
| `RUNTIME_DEGRADED` | 0021 | EXTERNAL_FAILURE | 503 | 进程存活但健康检查失败 |
| `RUNTIME_CONFIG_INVALID` | 0022 | CONFIGURATION | 503 | 配置快照无效 |

所有应用可见失败统一抛 `NexusException.build(AiStatusCode.*, cause)`；禁止 JDK
运行时异常直接穿透公共 API。Node 透传的业务错误保持 Node 错误体，不二次包装。

## 10. 安全

| 面 | 设计 |
|----|------|
| 传输 | Node 固定 loopback；外部只访问 Java `/ai/**`，跨机部署置于网关/TLS 之后 |
| 鉴权 | 统一在 Java endpoint：console Bearer + `ai.read`/`ai.write` Grant；Node 不做鉴权 |
| 本地进程 | loopback 端口不写入外部配置；同机不可信进程风险由 OS 用户/容器隔离消解 |
| 凭证 | 模型凭证写入不回显；AES-256-GCM；主密钥独立存放 |
| 工具 | 无 shell/代码执行；HTTP 域名白名单；MCP 仅 HTTP/SSE；文件工具仅登记根内 |
| 文件 | 相对路径 + `realpath` 前缀校验；默认拒绝符号链接/设备文件；敏感路径 denylist；软删除到 trash |
| 审计 | 文件操作写 JSONL，不含文件内容；拒绝与失败也记录 |
| SSRF | 解析后校验 IP，拒绝 RFC1918、链路本地、云元数据地址；重定向逐跳校验 |
| 输入 | 请求体大小上限、消息长度上限、工具参数 JSON Schema 校验 |
| 日志 | 结构化 JSON；密钥、Authorization、消息正文默认脱敏；日志目录 0700 |
| 进程 | 白名单 env；`cwd` 隔离；不加 shell（直接 `spawn` 而非 `shell -c`） |

## 11. 失败矩阵

| 场景 | 表现 | 处理 |
|------|------|------|
| Node 不存在 / 版本过低 | 启动失败 | `NODE_NOT_AVAILABLE`，不回退到其他 Node |
| runtime npm 包缺失 / dist 缺失 | 启动失败 | `RUNTIME_PACKAGE_MISSING` |
| npm 包版本与 jar 不一致 | 启动失败 | `RUNTIME_VERSION_MISMATCH`，提示期望/实际版本 |
| 端口被占用（固定端口） | 启动失败 | `RUNTIME_START_FAILED`，日志含 stderr 尾部 |
| 健康检查超时 | 启动失败 | `RUNTIME_START_FAILED`，终止子进程 |
| 进程异常退出 | 重启退避 | 超过次数 → `FAILED`；健康失败 → `DEGRADED` |
| 关闭超时 | 强杀 | `RUNTIME_STOP_TIMEOUT`（关闭路径记录日志，不抛给业务） |
| Node 未启动（LAZY） | 业务 503 | `RUNTIME_NOT_STARTED`，`Retry-After: 2` |
| Node 启动中 | 业务 503 | `RUNTIME_STARTING`，`Retry-After: 2` |
| 连续健康检查失败 | 业务 503 | `RUNTIME_DEGRADED`，`Retry-After: 5` |
| 配置快照无效 | 业务 503 | `RUNTIME_CONFIG_INVALID`，`Retry-After: 30` |
| `/health/live` 在 Node 故障时 | 200 | 只反映 JVM 存活 |
| `/health/ready` 在 Node 未就绪时 | 503 | 返回 state 与原因 |
| YAML 解析失败 / 同一层重复资源 | 启动或重载失败 | 保留上一份快照，返回文件路径与行号 |
| ConfigSource 拉取失败 | 启动/重载失败或回退本地 | 按 `requireRemoteConfig` 决定；错误包含 URL 与状态码 |
| 配置目录不可读 | 启动失败 | `INVALID_RUNTIME_CONFIG`，列出不可读路径 |
| 未登录 / token 失效 | Java 401 | console 认证失败，Node 不会收到请求 |
| 缺少 `ai.read` / `ai.write` | Java 403 | console 授权失败 |
| 路径不在白名单 | Java 403/404 | `PROXY_REQUEST_DENIED` / `PROXY_PATH_NOT_FOUND` |
| HTTP 方法不允许 | Java 405 | `PROXY_METHOD_NOT_ALLOWED` |
| 请求体超过代理上限 | Java 413 | `PROXY_BODY_TOO_LARGE` |
| Node 进程不可达 | Java 503 | `PROXY_UPSTREAM_UNAVAILABLE` |
| 上游响应超时 | Java 504 | `PROXY_UPSTREAM_TIMEOUT` |
| SSE 转发中断 | Java 502 | `PROXY_STREAM_FAILED`，记录 runId |
| 客户端断开 SSE | 关闭上游 | run 继续；可用 `runId` 查询 |
| 未配置模型 | API 400 | `model_not_configured` |
| 模型鉴权失败 | API 502 | `upstream_auth_failed`，不回显密钥 |
| 上游限流 | API 429 | `upstream_rate_limited`，透传 `Retry-After` |
| 会话忙 | API 409 | `run_conflict` |
| 工具超时 / 失败 | run 继续 | `tool_finished(isError=true)`，模型决定后续 |
| 路径越界 / 绝对路径 / `..` | 工具拒绝 | `path_outside_root`，记审计 |
| 符号链接越界或禁用 | 工具拒绝 | `symlink_denied` |
| 根被删除 / 不可读 | 工具拒绝 | `file_root_unavailable` |
| 文件过大 / 二进制 | 工具拒绝 | `file_too_large` / `binary_not_allowed` |
| 删除非空目录 | 工具拒绝 | `directory_not_empty`，V1 不支持递归 |
| 软删除写入 trash 失败 | 保留原文件 | `file_delete_failed`，绝不先 unlink |
| 单 run 文件操作超限 | 工具拒绝 | `file_op_limit_exceeded` |
| 磁盘写满 | API 507 | `storage_unavailable`，停止接收新 run |

## 12. 配置项

Java 侧（`AiRuntimeConfig`，可由引用方绑定到自身配置体系）：

| 键 | 默认 | 说明 |
|----|------|------|
| `dataDir` | 必填 | sessions、workspace、日志、密钥、runtime.json 根目录 |
| `configDir` | `<dataDir>/config` | 本地 YAML 根目录（`runtime.yaml` + `conf.d/`），可由 `NEXUS_AI_CONFIG_DIR` 覆盖 |
| `configUrl` | 空 | 可选 `ConfigSource` HTTP 地址；有值时优先于本地 YAML |
| `runtimeDir` | 空 | 为空时依次查找 `NEXUS_AI_RUNTIME_DIR`、`${app.dir}/node_modules/@innospots/nexus-ai-runtime`、`${app.dir}/ai/runtime` |
| `nodeExecutable` | `node` | Node 可执行文件；不满足 ≥22.19 则拒绝启动 |
| `bindHost` | `127.0.0.1` | 固定回环，不提供远程监听 |
| `port` | `0` | 0 表示随机端口 |
| `startupTimeout` | `30s` | 含配置加载与校验 |
| `startupPolicy` | `EAGER` | 服务部署建议 EAGER；嵌入式可用 LAZY |
| `healthCheckInterval` | `5s` | Node `/v1/health` 检查周期 |
| `healthTimeout` | `1s` | 单次健康检查超时 |
| `healthCacheTtl` | `2s` | 业务门禁复用健康结果的缓存 |
| `unhealthyThreshold` | `3` | 连续失败进入 DEGRADED |
| `restartOnUnhealthy` | `false` | DEGRADED 后是否自动重启 |
| `shutdownTimeout` | `10s` | 优雅关闭窗口 |
| `maxRestarts` | `3` | 崩溃重启上限 |
| `environment` | 空 | 仅允许白名单键覆盖 |

Java 代理（`AiProxyConfig`）：

| 键 | 默认 | 说明 |
|----|------|------|
| `pathPrefix` | `/ai` | 对外路径前缀，转发时剥离 |
| `maxRequestBodyBytes` | `8 MiB` | 超过返回 413 |
| `connectTimeout` | `5s` | 连接 Node 超时 |
| `requestTimeout` | `60s` | 普通请求总超时 |
| `sseIdleTimeout` | `120s` | SSE 空闲上限，靠心跳续期 |
| `allowedRoutes` | 上述 10 条 | 精确 method+path；不在表内 404 |
| `anonymousRoutes` | `/v1/openapi.yaml` | health 三条为本地匿名端点，不走代理 |
| `failFastOnUnavailable` | `true` | 不可用时不转发，直接 503 |
| `retryAfterSeconds` | `5` | 503 的 `Retry-After` 默认值 |

Node 侧运行参数（来自 `Runtime` YAML，非敏感）：`maxConcurrentRuns`、`runTimeout`、
`maxTurns`、`maxToolCalls`、`maxMessageBytes`、`logLevel`、`toolHttpAllowlist`、
`maxFileReadBytes`、`maxFileWriteBytes`、`maxFileListEntries`、`maxFileOpsPerRun`、
`fileDenyPatterns`、`trashRetentionDays`、`allowSymlinks`。

`runtime/config/runtime.yaml` 是**进程级**配置模板；模型/AgentProfile/工具/文件根配置在
`runtime/config/conf.d/*.yaml`。运行时**不修改**任何配置文件。

## 13. 测试策略

| 层 | 内容 | 是否依赖真实 LLM |
|----|------|------------------|
| Node 单测 | YAML 加载/合并/优先级、SessionManager.inMemory、SSE 编码、env 清洗、主机白名单 | 否 |
| Node 集成 | 启动 → 配置 faux provider → 建会话 → 流式完成 → 取消 → JSONL 重开 | 否（pi-ai faux provider） |
| Java 单测 | runtimeDir 校验、状态机、路径/方法白名单、头过滤、SSE 转发、env 构造 | 否 |
| Java 集成 | 真实 npm runtime 包 + 随机端口 + `/ai/**` 代理 + health + 关闭；`@EnabledIf` 检测 Node | 否 |
| 版本校验 | npm `package.json.version` 与 jar manifest 不一致时拒绝启动 | 否 |
| 发布 | `pnpm pack` 仅含 bin/dist/config/README；bin 可执行；版本一致 | 否 |
| 代理安全 | 未登录/缺 Grant/越权路径/超限/SSE 断连/上游不可达/超时 | 否 |
| 健康检查 | live 恒 200；ready 在 STOPPED/STARTING/DEGRADED/FAILED 时 503；RUNNING 恢复 200；Retry-After | 否 |
| 文件沙箱 | `..`/绝对路径/UNC/符号链接越界/设备文件/敏感文件/超限/二进制；原子写；软删除与审计 | 否 |
| 配置加载 | 多文件/多文档、ConfigSource 覆盖本地、重复资源报错、重载失败保留旧快照、secretRef/fromEnv | 否 |
| 隔离测试 | 设置 `OPENAI_API_KEY` 与伪造 `~/.pi`，断言运行时不可见，且会话只写入 `<dataDir>/sessions` | 否 |
| 契约测试 | 代理路径映射、方法/头过滤、SSE 事件透传快照 | 否 |
| 每日任务 | 使用测试密钥跑一次真实供应商冒烟 | 是（非 CI 门禁） |

## 14. AGENTS 对齐

| 约束 | 本方案 |
|------|--------|
| 模块职责 | 新增 `innospots-nexus-ai`：AI 运行时宿主 + `/ai/**` 对外代理；不承载管理业务 |
| 依赖方向 | 依赖 `console`（及传递 core/base）；与 portal/platform 平行；不得反向依赖 |
| base 保持 middleware-free | 未向 base 增加任何 AI 类型 |
| core 不含 AI 业务 | 模型配置与会话在 Node 文件（YAML/JSONL），不进入 core |
| 无 Spring/Quarkus 绑定 | 模块内不引入框架依赖，生命周期由引用方接线 |
| 包结构 | 技术模块按功能分包；Java 只有代理端点，无业务 DTO/持久化 |
| 鉴权 | 复用 console Bearer 与 `@EndpointGrant`；health 匿名；Node 不做鉴权 |
| 健康检查 | Java 本地 health/live/ready；Node 故障时 503，不依赖 Node 存活 |
| 状态码 | 使用 `NAI` + `StatusCategory` + 4 位本地码 |
| 文件工具 | 仅登记根内、默认关闭；无 shell/代码执行；软删除与操作审计 |
| 异常 | 仅 `NexusException`，统一 `AiStatusCode`；Node 业务错误体透传 |
| 持久化 | 配置只读加载（ConfigSource/YAML）；会话正文用 pi `SessionManager` JSONL；审计 JSONL；无 SQLite |
| 制品与分发 | thin jar（supervisor/代理）+ `@innospots/nexus-ai-runtime` npm 包；版本强制一致 |
| OpenAPI | 显式例外：Java 代理不生成索引，透传 Node 生成的 `/ai/v1/openapi.yaml` |
| 设计文档位置 | 模块内 `docs/`，符合 `design-deliverables.md` |

需要在 `AGENTS.md` 增补：模块职责一节、依赖规则一条。本期已随骨架提交。

## 15. 分阶段实施

| 阶段 | 内容 | 出口 |
|------|------|------|
| P0（本次） | Maven 骨架、reactor/BOM 登记、AGENTS、本文档 | `mvn validate` 通过 |
| P1 | Node runtime：启动、ConfigSource + YAML 加载/合并/校验、SessionManager 导入与 bundle 体积基线、health | faux provider 集成测试通过 |
| P2 | AgentProfile 加载、sessions、SessionManager JSONL、上下文桥接、SSE、取消 | 流式对话与 JSONL 重开通过 |
| P3 | 工具：HTTP 工具 + MCP（HTTP/SSE）与白名单；受限文件工具 + 路径防护/软删除/审计；Skill inline | 工具调用、越界、删除与技能拼接测试通过 |
| P4 | Java supervisor + `/ai/**` 透明代理 + health/live/ready + npm 安装/版本校验 | 外部工程经 `/ai/v1/**` 完成鉴权、SSE 与启停验证 |
| P5 | 密钥加密、限流、日志脱敏、契约快照、迁移演练；Skill `index` 可选 | 安全检查与故障演练通过 |

## 16. 待确认事项

1. 运行时的 Node 版本基线锁定为 **≥ 22.19.0**（pi 要求），是否允许更高版本自动放行？
   建议：满足最低版本即放行，超出 Node 26 时记录告警。
2. 主密钥是必须由部署方提供，还是允许首次启动自动生成？建议：允许自动生成，
   但生产环境强制显式提供。
3. HTTP 工具是否允许访问内网地址？建议：默认拒绝，按域名/IP 段白名单放开。
4. 会话历史是否需要保留策略（按天数/条数）？建议：P2 先提供手动删除，
   保留策略在 P5 定。
5. 引用方/CI 是否统一能访问私有 npm registry？若不能，必须提供
   `pnpm pack` tarball 或内部镜像；否则退回 jar 内嵌预构建产物需要单独决策。
6. npm 包的发布权限与 `NPM_TOKEN` 由谁管理？快照版使用
   `x.y.z-snapshot.<build>` + `snapshot` dist-tag，Release 使用同 Maven 版本。
7. `pi-coding-agent` 包根导出较宽，tree-shaking 后的 bundle 体积是否满足轻量门禁？
   建议 P1 设定上限（例如 dist ≤ 40MB）；超出则在同一 `SessionStore` 端口下切换为
   「复用 JSONL 格式、自研轻量 Store」。
8. 会话目录是否按 agent 或日期分层？pi 原生文件名为
   `<timestamp>_<session-id>.jsonl`，建议保持原生命名，列表缓存负责展示排序。
9. 文件工具默认拒绝符号链接与 `.env`/`.git`/密钥类路径；是否需要在某些 AgentProfile 上
   放开，需单独评审并按配置启用。
10. 软删除默认保留 7 天；是否需要恢复 API 与清理策略（按天数/容量）在 P3 定。
11. `/ai/**` 目前只分 `ai.read` / `ai.write` 两个粗粒度 Grant；是否需要按资源路径
    （agents、sessions）细分，需结合控制台权限目录再定。
12. Java 代理不生成 OpenAPI 索引，直接透传 Node 的 `/ai/v1/openapi.yaml`；
    是否接受这一例外，还是后续在 Java 侧生成聚合文档。
13. Node 无鉴权且仅 loopback；同机不可信进程的风险是否由容器/独立 OS 用户承担，
    需要在部署规范中明确。
14. 同一层 YAML 重复资源当前设计为报错；是否需要「后者覆盖前者」的宽松模式，
    建议保持报错，避免隐式覆盖。
15. 本地配置是否默认开启 `fs.watch` 自动重载，还是只接受 `SIGHUP` 显式触发。
16. `fromEnv` 读取 `<dataDir>/secrets.env` 是否满足部署要求，还是统一走 `secretRef`。
17. readiness 是否需要支持「探测触发启动」（`startOnProbe`）；当前设计只报告状态，
    启动由 EAGER 或首个业务请求触发。
