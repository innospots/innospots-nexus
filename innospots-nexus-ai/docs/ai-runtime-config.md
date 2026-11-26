# Node Runtime 配置设计

## 1. 文档定位

- 状态：草案
- 范围：`@innospots/nexus-ai-runtime` 的配置来源、YAML Schema、合并规则、密钥与重载
- 关联：[ai-runtime-design.md](ai-runtime-design.md)、[ai-runtime-modules.md](ai-runtime-modules.md)

配置**只读加载**，不提供维护 API，不写 SQLite。模型、AgentProfile、工具、MCP、文件根
全部来自配置；唯一运行时可变数据是会话。

## 2. 配置来源

| 优先级 | 来源 | 说明 |
|--------|------|------|
| 高 | `ConfigSource` 接口 | 宿主注入，或 `NEXUS_AI_CONFIG_URL` 拉取 |
| 低 | 本地 YAML | `<configDir>/runtime.yaml` + `<configDir>/conf.d/**/*.yaml` |
| 最低 | 内置默认 | 代码默认值，仅用于缺省 |

- 同一资源 `(kind, metadata.name)` 由高优先级来源整资源覆盖低优先级。
- 同一来源内重复定义直接失败，并报出文件与行号。
- `runtime.yaml` 只提供运行参数（日志、并发、文件限制），不覆盖进程绑定参数。

### 2.1 `ConfigSource` 接口

```ts
interface ConfigSource {
  readonly name: string;
  readonly priority: number;
  load(): Promise<LoadedDocument[]>;
}
```

内置实现：

| 实现 | priority | 说明 |
|------|----------|------|
| `FileConfigSource` | 0 | 本地 `configDir` |
| `HttpConfigSource` | 100 | `--config-url` / `NEXUS_AI_CONFIG_URL` |
| `StaticConfigSource` | 200 | 宿主/测试注入 |

**HTTP 接口契约**：

```http
GET <configUrl>
Accept: application/yaml, text/yaml
```

- 2xx 响应体直接是 YAML，可包含多个 `---` 文档。
- 不要求外层 `{data:...}` 包装；`Content-Type` 不强制。
- 当前实现不附加认证头；需要鉴权时由内网网关或后续 `ConfigSource` 扩展处理。
- 失败（非 2xx/解析失败）默认启动失败；不会静默回退，避免配置漂移。

### 2.2 加载顺序

```text
FileConfigSource(0) → HttpConfigSource(100) → StaticConfigSource(200)
```

同一 `(kind, metadata.name)` 由高优先级来源覆盖。

## 3. 目录与多文件

```text
<configDir>/
├── runtime.yaml
└── conf.d/
    ├── 10-providers.yaml
    ├── 20-models.yaml
    ├── 30-agent-profiles.yaml
    ├── 40-tools.yaml
    ├── 50-mcp.yaml
    └── 60-file-roots.yaml
```

- `conf.d` 递归扫描 `*.yaml` / `*.yml`，按相对路径字典序加载。
- 每个文件支持多个 YAML 文档（`---`）。
- 文件之间按资源身份合并，不按文件名合并。
- `configDir` 不存在时以空配置启动；模型调用返回 `model_not_configured`。
- 运行时不自动创建或修改配置文件。

## 4. 通用 Schema

```yaml
apiVersion: nexus.ai/v1
kind: <Runtime|Provider|Model|AgentProfile|PromptTemplate|Skill|Tool|McpServer|FileRoot>
metadata:
  name: <稳定名称>
spec:
  enabled: true
```

约束：

- `apiVersion` 固定 `nexus.ai/v1`。
- `metadata.name` 只允许 `[a-z0-9][a-z0-9._-]*`，在同类资源内唯一。
- `spec.enabled=false` 时资源保留在配置快照中，但不进入运行时注册表。
- 未知字段默认拒绝（strict schema），避免拼写错误静默生效。

## 5. Runtime

```yaml
apiVersion: nexus.ai/v1
kind: Runtime
metadata:
  name: default
spec:
  logLevel: info
  maxConcurrentRuns: 2
  runTimeoutSeconds: 300
  maxTurns: 12
  maxToolCalls: 100
  maxMessageBytes: 1048576
  maxFileReadBytes: 262144
  maxFileWriteBytes: 1048576
  maxFileListEntries: 1000
  maxFileOpsPerRun: 100
  trashRetentionDays: 7
  allowSymlinks: false
  toolHttpAllowlist: []
  fileDenyPatterns:
    - ".git/**"
    - ".env*"
    - ".ssh/**"
    - "id_rsa*"
    - "*.pem"
    - "*.key"
    - "*.p12"
    - ".aws/**"
    - ".kube/**"
```

进程绑定参数（`host`、`port`、`dataDir`、`configDir`、`readyFile`）由 CLI/环境变量提供，
不在 `Runtime` YAML 中重复定义。

## 6. Provider

```yaml
apiVersion: nexus.ai/v1
kind: Provider
metadata:
  name: openai-main
spec:
  type: openai
  baseUrl: https://api.openai.com/v1
  apiKey:
    fromEnv: OPENAI_API_KEY
  enabled: true
```

字段：

| 字段 | 必填 | 说明 |
|------|------|------|
| `type` | 是 | `openai` / `anthropic` / `google` / `deepseek` / `openrouter` / `openai-compatible` |
| `baseUrl` | 否 | 自定义网关或区域端点 |
| `apiKey.fromEnv` | 二选一 | 从 `<dataDir>/secrets.env` 或 `NEXUS_AI_` 变量读取 |
| `apiKey.secretRef` | 二选一 | 指向 `<dataDir>/secrets/<name>.enc` |
| `headers` | 否 | 附加请求头，不含密钥 |
| `enabled` | 否 | 默认 true |

禁止：

- 明文 `apiKey`。
- 引用宿主 `OPENAI_API_KEY` 等变量；`fromEnv` 只读受控 secrets 文件。

## 7. Model

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

| 字段 | 必填 | 说明 |
|------|------|------|
| `provider` | 是 | 引用 Provider 名称 |
| `modelId` | 是 | provider 侧模型 ID |
| `displayName` | 否 | 对外展示名 |
| `contextWindow` | 否 | 覆盖 provider 默认值 |
| `maxOutputTokens` | 否 | 输出上限 |
| `capabilities` | 否 | `text` / `tool` / `vision` |
| `defaultParams` | 否 | temperature/topP 等 |

## 8. AgentProfile

**不是 pi 官方规范**。它是 Nexus 配置，最终映射为 pi-agent-core 的 Agent 构造参数。

```yaml
apiVersion: nexus.ai/v1
kind: AgentProfile
metadata:
  name: default
spec:
  displayName: Default Assistant
  description: Enterprise assistant with read-only workspace access
  model: gpt-main
  promptTemplate: default-assistant
  promptVariables:
    role: enterprise assistant
  thinkingLevel: medium
  toolExecution: parallel
  maxTurns: 12
  maxToolCalls: 100
  mcpServers:
    - docs
  tools:
    - http.echo
    - mcp__docs__search
  fileRoot: workspace
  fileAccess: read
  allowDelete: false
  enabled: true
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `displayName` | 否 | 对外展示名 |
| `description` | 否 | 说明，不进入模型上下文 |
| `model` | 是 | 引用 Model |
| `systemPrompt` | 三选一 | 内联 prompt |
| `systemPromptFile` | 三选一 | 相对 `configDir` 的文件，读取时做路径校验 |
| `promptTemplate` | 三选一 | 引用 `PromptTemplate` |
| `promptVariables` | 否 | 模板变量；仅配置提供，不接受会话覆盖 |
| `skills` | 否 | 引用 `Skill` 名称列表；按顺序注入或索引 |

`skills` 规则：

- 不填 → 不注入任何 Skill。
- Skill 资源不会自动全局生效，必须被 AgentProfile 引用。
- 引用不存在或 `enabled=false` 的 Skill → 配置校验失败。
- 这里引用的是 Nexus `kind: Skill`，不是 `pi-coding-agent` 的 skill runtime。
| `thinkingLevel` | 否 | `off/minimal/low/medium/high/xhigh/max` |
| `toolExecution` | 否 | `parallel`（默认）/ `sequential` |
| `maxTurns` | 否 | 单 run 最大轮次 |
| `maxToolCalls` | 否 | 单 run 最大工具调用次数 |
| `mcpServers` | 否 | 允许的 MCP Server 名称；不填则不注册 MCP 工具 |
| `tools` | 否 | 最终工具白名单，支持 `http.*` 与 `mcp__<server>__<tool>` |
| `fileRoot` | 否 | 引用 FileRoot |
| `fileAccess` | 否 | `none/read/read_write` |
| `allowDelete` | 否 | 是否注册 `file_delete` |
| `enabled` | 否 | 默认 true |

### 8.1 PromptTemplate

提示词模板是 **Nexus 配置资源**，不是 pi skill。

```yaml
apiVersion: nexus.ai/v1
kind: PromptTemplate
metadata:
  name: default-assistant
spec:
  content: |
    You are ${role}.
    Follow the workspace policy and never access paths outside the sandbox.
  variables:
    role:
      required: true
      default: a helpful enterprise assistant
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `content` | 二选一 | 内联模板 |
| `file` | 二选一 | 相对 `configDir` 的模板文件 |
| `variables` | 否 | 变量声明：`required`、`default` |

规则：

- 仅支持 `${name}` 标量插值，不做表达式、循环或代码执行。
- 变量只由 AgentProfile 的 `promptVariables` 提供；**不接受会话创建时覆盖**，
  避免调用方注入系统指令。
- PromptTemplate 的 `${name}` 只解析 `promptVariables`，不会回退到 secrets 变量；
  与 headers 的 `${VAR}` 使用不同解析上下文。
- 未声明的变量、缺失的必需变量、未解析的占位符 → 配置校验失败。
- `systemPrompt` / `systemPromptFile` / `promptTemplate` 三者恰好选一个。
- Prompt 在**创建会话时解析并写入 system message**；之后修改模板只影响新会话。

### 8.2 工具、MCP 与 skills 的边界

| 概念 | 是否放在 AgentProfile | 说明 |
|------|----------------------|------|
| 提示词 | 是 | 内联/文件/模板三选一 |
| HTTP 工具 | 是（白名单） | 工具定义在全局 `Tool`，AgentProfile 只允许名称 |
| MCP | 是（作用域 + 白名单） | Server 定义在全局 `McpServer`；`mcpServers` 限定可发现范围，`tools` 再做最终允许 |
| 文件工具 | 是（权限） | 由 `fileRoot` / `fileAccess` / `allowDelete` 控制，不写进 `tools` |
| 运行限制 | 是 | `thinkingLevel`、`toolExecution`、`maxTurns`、`maxToolCalls` |
| skills | 是（引用） | 引用 `Skill` 配置；不是 pi skill runtime |
| 模型参数 | 否 | 放 `Model.defaultParams` |

`tools` 与 `mcpServers` 的交集规则：

1. `mcpServers` 为空 → 不注册任何 MCP 工具。
2. MCP 工具必须同时满足：所属 server 在 `mcpServers` 中，且完整名称在 `tools` 中。
3. `tools` 不填 → 不注册 HTTP/MCP 工具，仅保留文件工具（若 fileAccess 开启）。
4. 文件工具不受 `tools` 控制，避免把 `file_write` 误暴露给只读 Agent。

### 8.3 Skill：复用已有 skills 目录

**为什么不直接引入 pi skills 运行时**

pi 的 skills 属于 `pi-coding-agent`，不是 `pi-agent-core`：

- 启动时扫描 `~/.pi/skills`、项目 `.pi/skills`、`.agents/skills` 等目录；
- 把 name/description 放入 system prompt，由模型用文件工具自行读取 `SKILL.md`；
- 依赖 coding-agent 的项目信任、资源发现、文件工具与渐进式加载；
- 与本模块“配置显式、隔离、无任意文件访问”的约束冲突。

因此本模块**不加载 pi skill runtime**，但可以读取**已有 skills 目录的文件**，转成 Nexus 的
`Skill` 配置资源。

**Skill 配置**

```yaml
apiVersion: nexus.ai/v1
kind: Skill
metadata:
  name: pdf-tools
spec:
  path: /etc/nexus-ai/skills/pdf-tools   # 也可相对 configDir
  mode: inline                          # V1 只支持 inline；index 保留
  include:
    - SKILL.md
    - references/**                     # 只允许文本/JSON/YAML
  maxBytes: 65536
  enabled: true
```

AgentProfile 引用：

```yaml
spec:
  skills:
    - pdf-tools
```

两种模式：

| 模式 | 行为 | 提示词是否变化 | 运行时文件访问 |
|------|------|----------------|----------------|
| `inline`（V1 唯一实现，默认） | 加载时读取 `SKILL.md` 与 `include` 文本，拼入 system prompt | 是，配置加载时注入 | 无 |
| `index`（V1.1+ 保留） | system prompt 只放 name/description；注册 `skill_read` 工具按需读取 | 是，注入索引 | 仅 Skill 根内 |

**V1 约束**：`spec.mode` 缺省为 `inline`；配置 `index` 直接校验失败
（`unsupported_skill_mode`），不做静默降级。这样 V1 的 prompt 组合完全确定。

`inline` 是“提示词没变”的弥补方式：**提示词在会话创建时由 base prompt + skills 组合生成**，
不是等模型自己发现 skill。

规则：

- 只读取显式配置的目录，不扫描 `~/.pi`、`.agents/skills` 或项目目录。
- 目录必须存在，`SKILL.md` 必须可读；越界符号链接、设备文件一律拒绝。
- 只允许 `include` 中的文本文件；脚本、二进制、可执行文件不加载、不执行。
- frontmatter 的 `name`/`description` 用于校验和索引；`allowed-tools` **不会自动授权**，
  仍以 AgentProfile `tools` 为准。
- 同名 Skill 重复定义 → 配置失败。
- `inline` 总大小受 `maxBytes` 限制；超限失败。
- Skill 中的脚本（如 `scripts/extract.sh`）不会执行；需要执行能力时另建受控工具。

**提示词组合顺序**

```text
base system prompt（PromptTemplate / systemPromptFile / systemPrompt）
  → skills（按 AgentProfile.skills 顺序）
  → 工具与沙箱策略（运行时注入的固定约束）
```

- Skill 内容在**创建会话时**解析并写入 system message；后续修改只影响新会话。
- `index` 模式的 `skill_read` 仍走 PathGuard，只允许 Skill 根目录。

#### 8.3.1 引入已有技能目录

```text
1. 确认目录符合 Agent Skills 形式：
   <skill-dir>/SKILL.md
   且 frontmatter 至少有 name、description
2. 决定放置方式：
   a. 复制到 <configDir>/skills/<name>/，使用相对 path；
   b. 保持原位置，使用绝对 path（必须显式登记，不自动扫描 ~/.pi 或 .agents）
3. 新建 Skill 资源：
   kind: Skill
   metadata.name: <name>
   spec.path / spec.include / spec.mode / spec.maxBytes
4. 在 AgentProfile 增加：
   spec.skills: [<name>]
5. 触发重载（fs.watch 或 SIGHUP）
6. 新建会话；已有会话不会重新拼 prompt
```

示例（引入已有 `pdf-tools`）：

```yaml
apiVersion: nexus.ai/v1
kind: Skill
metadata:
  name: pdf-tools
spec:
  path: /opt/agent-skills/pdf-tools
  mode: inline
  include:
    - SKILL.md
    - references/**        # 只加载文本参考文件
  maxBytes: 131072
```

```yaml
# AgentProfile
spec:
  skills:
    - pdf-tools
```

注意：

- 只加载 `include` 中的文本文件；`scripts/**`、二进制、可执行文件不加载、不执行。
- `SKILL.md` 中的“运行 scripts/xxx”指令不会生效；需要执行时另建受控 HTTP/MCP 工具。
- `allowed-tools` 不会自动授权；必须在 AgentProfile `tools` 中显式允许。
- 目录必须存在且可读；越界符号链接、设备文件、同名 Skill 都会导致配置失败。
- 技能内容在**创建会话时**拼入 system prompt；已有会话不受影响。
- `inline` 模式没有渐进式加载；所有 `include` 内容都会进入 prompt，受 `maxBytes` 限制。
- 需要渐进式加载时使用 `mode: index`（V1.1 可选），通过 `skill_read` 按需读取。

#### 8.3.2 `index` 模式为什么不放 V1

不是实现难度高，而是行为复杂：

| 面 | `inline` | `index` |
|----|----------|---------|
| Prompt | 配置加载时一次拼接 | 只放名称/描述索引 |
| 读取时机 | 配置加载 | 模型运行中调用 `skill_read` |
| 工具面 | 无新增工具 | 新增 `skill_read` + PathGuard + 大小/文本限制 |
| 模型行为 | 确定性 | 模型可能不读、读错文件、重复读 |
| 会话语义 | 创建时固定 | 运行中动态进入上下文，需缓存/压缩兼容 |
| 观测 | 简单 | 需要记录 skill 读取、命中率、失败原因 |
| 测试 | 配置/拼接 | 多 skill、frontmatter、越界、超大、重载、并发 |

`inline` 对“少量技能、总大小可控”足够，且行为确定。`index` 的主要收益是**节省 token**，
适合大量技能或长 references；当 skill 目录多、`SKILL.md` 大时才值得引入。

如果已有技能数量较多（例如几十个 Agent Skills），建议把 `index` 提到 V1.1 首批实现：
代码量约一个工具 + 一个索引器，但必须同时补齐 `skill_read` 工具、PathGuard、缓存、
审计和模型漏读的兜底策略。

`index` 的最小实现边界：

```text
配置加载
  → 只解析 SKILL.md frontmatter（name/description）
  → 构建 SkillIndexEntry(skill, root, include, maxBytes)
  → 校验目录/符号链接/重名

system prompt
  → ## Skills
     - pdf-tools: Extract text and tables... (read /skills/pdf-tools/SKILL.md)

工具
  → skill_read(skill, path)
  → path 必须在 skill root 内且在 include 白名单
  → 仅文本，单次 <= maxBytes
  → 读取结果进入 toolResult，不直接拼 system prompt

缓存/观测
  → 按会话缓存已读文件
  → 记录读取次数、拒绝原因、大小
  → 不执行 scripts/assets
```

注意：`index` 模式下模型可能不主动读取技能；需要接受“漏读”风险，或在 AgentProfile 中
提供固定提示（例如“处理 PDF 前必须读取 pdf-tools 技能”）。不做会话级强制选择，避免
调用方注入系统指令。




### 8.4 多 Agent 隔离与生效规则

每个 AgentProfile 都是独立能力集合：

```yaml
# 客服 Agent
kind: AgentProfile
metadata: { name: support }
spec:
  model: gpt-mini
  promptTemplate: support-prompt
  skills: [faq]
  mcpServers: [docs]
  tools: [ticket.create, mcp__docs__search]
  fileAccess: none
---
# 分析 Agent
kind: AgentProfile
metadata: { name: analyst }
spec:
  model: gpt-main
  promptTemplate: analyst-prompt
  skills: [pdf-tools, sql-analytics]
  mcpServers: [warehouse]
  tools: [mcp__warehouse__query]
  fileRoot: reports
  fileAccess: read_write
  allowDelete: false
```

生效集合计算：

```text
effectiveSkills    = AgentProfile.skills ∩ 已加载且 enabled 的 Skill
effectiveMcpTools  = AgentProfile.tools 中 mcp__<server>__<tool>
                     ∩ server ∈ AgentProfile.mcpServers
effectiveHttpTools = AgentProfile.tools 中 http.* ∩ 已加载 Tool
effectiveFileTools = 由 fileAccess / allowDelete 推导
effectivePrompt    = base prompt + effectiveSkills（inline）
```

规则：

- 全局 `Skill` / `Tool` / `McpServer` 只是定义，不会自动对所有 Agent 生效。
- AgentProfile 不写 `tools` → 不注册 HTTP/MCP 工具；不写 `skills` → 不注入技能。
- AgentProfile 不写 `mcpServers` → 不暴露任何 MCP 工具。
- 一个 Agent 的工具/MCP/Skill 不会泄漏给另一个 Agent。
- MCP 连接可以共享，但工具暴露按 Agent 过滤；`beforeToolCall` 再校验一次。
- 调用方不能通过创建会话覆盖 tools/mcp/skills；只能选择 `agent`。
- 会话创建时对 AgentProfile 的生效集合做**快照**（`agentProfileHash`），包含
  skills/tools/mcpServers/fileRoot/prompt 的**名称与配置**；之后 AgentProfile 变更只影响新会话。
- Tool / McpServer 的具体实现按名称从当前全局配置解析；定义被删除时该工具在该会话中不可用。
  这保证能力集合稳定，同时允许实现升级。
- 引用不存在或 disabled 的 Skill/Tool/FileRoot → 配置校验失败。
- MCP 工具在发现后校验；MCP Server 临时不可用 → 健康 DEGRADED，相关工具在该次
  run 中不可用，不阻塞其他 Agent。

## 9. Tool

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

约束：

- `type` 目前只支持 `http`。
- `parameters` 是 JSON Schema；工具参数在调用前校验。
- 默认拒绝私网/回环/云元数据地址；`allowPrivateNetwork=true` 需显式配置。
- 工具配置只读，不提供创建/修改接口。

## 10. McpServer

```yaml
apiVersion: nexus.ai/v1
kind: McpServer
metadata:
  name: docs
spec:
  transport: http
  url: https://mcp.internal.example/mcp
  headers:
    Authorization: Bearer ${DOCS_TOKEN}
  enabled: true
```

约束：

- `transport` 仅 `http` / `sse`。
- V1 不支持 stdio，避免隐式进程执行。
- 工具名统一 `mcp__<server>__<tool>`。
- 发现失败不阻塞启动，但健康检查标记 DEGRADED。

## 11. FileRoot

```yaml
apiVersion: nexus.ai/v1
kind: FileRoot
metadata:
  name: workspace
spec:
  path: /srv/nexus/workspace
  enabled: true
```

校验：

- 绝对路径，存在且是目录。
- 不能位于 `<dataDir>` 内，也不能包含 `<dataDir>`。
- 拒绝 `/`、用户 home、系统目录（建议）。
- 符号链接在加载时解析为 canonical path；越界拒绝。

## 12. 合并、覆盖与禁用

```text
内置默认 < 本地 YAML < ConfigSource
```

- 资源身份：`(kind, metadata.name)`。
- 同来源重复：启动/重载失败。
- 高优先级整资源覆盖，不做字段深合并。
- 禁用：`spec.enabled: false`；运行时不注册，但配置快照保留。
- 配置只读：运行时不会写回任何 YAML。

## 13. 密钥

### 13.1 `fromEnv`

读取顺序：

1. `<dataDir>/secrets.env`（dotenv，0600）；
2. 进程环境中 `NEXUS_AI_` 前缀变量；
3. 找不到则启动校验失败。

不会读取宿主 `OPENAI_API_KEY`、`ANTHROPIC_API_KEY` 等。

`headers` 中的 `${VAR}` 也按同样规则解析（如 MCP 的 `Authorization: Bearer ${DOCS_TOKEN}`）；
未解析的占位符直接导致配置校验失败。

### 13.2 `secretRef`

- 指向 `<dataDir>/secrets/<name>.enc`。
- AES-256-GCM；主密钥来自 `NEXUS_AI_MASTER_KEY` 或 `<dataDir>/master.key`。
- 由部署工具/密钥管理系统写入，不通过运行时 API。

### 13.3 明文检查

- YAML 中出现明文 `apiKey` 直接拒绝加载。
- 日志中不输出密钥、Authorization、secrets 路径。

## 14. 重载

触发方式：

- `fs.watch` 监听 `configDir`，500ms 防抖；
- `SIGHUP`；
- `--no-watch` 时只接受 SIGHUP。

流程：

```text
load all sources
  → parse + strict validate
  → merge by identity
  → build immutable snapshot
  → success: swap snapshot + update configVersion
  → failure: keep old snapshot + log error
```

- 进行中的 run 固定使用启动时快照。
- 重载失败不影响正在运行的服务，但 `/v1/health` 标记 config 检查失败。

## 15. 错误分类

| 错误 | 处理 |
|------|------|
| 文件不存在 | `configDir` 可空；单文件缺失不报错 |
| YAML 语法错误 | 启动/重载失败，报文件与行号 |
| 未知字段 | 失败（strict） |
| 同来源重复资源 | 失败 |
| Provider/Model 引用不存在 | 失败 |
| AgentProfile 引用未知工具/FileRoot | 失败 |
| 明文密钥 | 失败 |
| ConfigSource HTTP 失败 | 按配置回退本地或失败 |

## 16. 测试要点

- 多文件、多文档、字典序加载。
- ConfigSource 覆盖本地；同来源重复报错。
- `enabled=false` 不注册。
- 密钥 `fromEnv` / `secretRef`，明文拒绝。
- 重载失败保留旧快照。
- AgentProfile/Tool/FileRoot 引用完整性校验。
- 路径逃逸与符号链接拒绝。
