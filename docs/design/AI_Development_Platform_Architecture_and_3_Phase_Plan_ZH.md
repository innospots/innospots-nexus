# 企业级 AI 开发与生产平台

## 修订版架构设计及三阶段开发计划

**文档版本：** 3.1  
**状态：** 提议 / 开发基线  
**目的：** 在 2.0 合约、运行时与体验层基线上，将平台收束为一套可扩展的智能体框架，并把控制面收成导航 + 主界面 + 设置区。

### 相对 2.0 的核心调整

1. **智能体框架优先于功能清单。** 平台提供可扩展的能力宿主。Agent、Tool、MCP 都以功能扩展形式接入。
2. **体验层保持 2.0 不变。** 仍为 AI Studio、Agent Studio、Artifact Workspace、App Studio。Studio 是产品能力面，本轮不改结构。
3. **控制面改为工作台形态。** 控制面不再平铺「项目 / 注册表 / 版本 / 策略」，而是：左侧导航、主界面、设置区。Studio 通过控制面进入、配置与运行。
4. **左侧导航 YAML 化。** 控制面导航由 YAML 声明，扩展可贡献导航项，调整导航不需要改控制面内核。
5. **主界面双表面。** 控制面主区域既可对话交互，也可打开应用页面。
6. **设置区按宿主与扩展分层。** 基础设置只包含模型与扩展；Agent、Tools、MCP 都是功能扩展，不是与模型并列的一等设置分类。
7. **数据归属双边界。** Workspace 与 Project 都是数据归属边界，但 Workspace 不对客户开发开放，只对管理开放；客户开发者只面对 Project。

---

# 1. 执行摘要

平台战略方向不变：不绑定单一 AI 框架，保留领域模型与运行时合约所有权，将第三方能力视为可安装扩展，并将 Artifact 作为一等生产对象。

3.1 在此之上补上扩展骨架，并重切控制面：

> **可扩展智能体框架 + 体验层 Studio 不变 + 控制面（导航 / 主界面 / 设置区）+ 不可变执行快照 + 以 Artifact 为中心的生产。**

实施约束仍然成立：

1. **不要并行构建所有领域。** 首个发布证明完整闭环：安装扩展 -> 经控制面导航进入 Studio -> 主界面对话或打开页面 -> 执行 -> 生成 Artifact -> 预览/编辑/版本化 -> 复用。
2. **不要将 DSL 作为首要投资。** 先用规范 JSON/TypeScript 执行合约；语法后置。
3. **不要让 Pi 成为平台内核。** Pi 是 Agent Runtime 的一种实现。
4. **不要仅将 Artifact 视为文件存储。** Artifact 需要版本、血缘、预览/编辑合约。
5. **控制面配置与运行时执行分离。** 运行时执行不可变 Run/Execution 快照。
6. **能力隔离从第一天开始。** 可安装扩展即是代码边界，需要清单、权限、密钥、沙箱和信任策略。
7. **暂缓公开市场。** 先私有/内部注册表与安装器。
8. **不要把 Workspace 暴露给客户开发。** 客户开发入口是 Project；Workspace 是管理侧归属与隔离边界。

---

# 2. 修订版架构原则

## 2.1 核心原则

| 原则           | 决策                                                                                         |
| -------------- | -------------------------------------------------------------------------------------------- |
| 框架策略       | 平台是可扩展的智能体宿主，不是预置功能清单。                                                 |
| 扩展策略       | Agent、Tool、MCP、页面、导航、查看器均通过 Capability / Extension 合约进入。                 |
| 体验策略       | 体验层保持四个 Studio：AI Studio、Agent Studio、Artifact Workspace、App Studio。             |
| 控制面策略     | 控制面形态为导航 + 主界面 + 设置区；Studio 经此进入，不在控制面再平铺资源模块。               |
| 导航策略       | 左侧导航由 YAML 声明，扩展可贡献，控制面按权限合并渲染。                                     |
| 设置策略       | 基础设置 = 模型 + 扩展；Agent / Tools / MCP 属于功能扩展配置。                               |
| 数据策略       | Workspace 与 Project 都是归属边界；Workspace 仅管理可见，Project 是客户开发边界。            |
| 领域所有权     | AI 对象模型、Artifact、Agent 清单、运行时合约和策略模型由平台拥有。                          |
| 运行时策略     | Pi 是首个位于适配器之后的 Agent Runtime 实现。                                               |
| 执行策略       | 控制面将配置编译为不可变的 Run/Execution 快照。                                              |
| Artifact 策略  | Artifact 是一等对象，而非聊天响应的副作用。                                                  |
| 安全策略       | 能力、权限、密钥、沙箱和网络控制是运行时的显式关注点。                                       |
| 存储策略       | 元数据在 PostgreSQL；大容量内容在对象存储；遥测与事务数据分离。                              |
| 开发策略       | 三个阶段：控制面闭环 -> 平台化 -> 生态系统与企业级。                                         |

## 2.2 平台拥有 vs. 委托

### 平台拥有

- 智能体框架（能力宿主、扩展生命周期、贡献点）
- 体验层 Studio 边界（AI / Agent / Artifact / App）
- 控制面壳（导航合并、主界面双表面、设置宿主）
- AI 对象模型与 Project 领域
- Artifact 模型及血缘
- Agent / Tool / MCP 清单与安装合约
- Provider / Adapter / Capability 合约
- Runtime Contract
- Run / Execution 模型
- 版本和部署快照模型
- 权限和策略评估合约
- 注册表和安装工作流

### 可插拔 / 委托

- Agent 运行时实现（初期为 Pi）
- 模型提供者
- 向量数据库与 RAG 实现
- Tool 实现与 MCP 服务器
- 工作流引擎
- Artifact 查看器 / 编辑器
- 控制面页面与导航贡献
- 评估引擎
- 遥测后端
- 身份验证 / 身份提供者

---

# 3. 修订版整体架构

```text
+-------------------------------------------------------------------------+
|                         企业 AI 开发平台                                 |
+-------------------------------------------------------------------------+
| 体验层（保持 2.0）                                                       |
| AI Studio | Agent Studio | Artifact Workspace | App Studio             |
+-----------------------------------+-------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| 控制面                                                                  |
|  +------------+------------------------------+------------------------+ |
|  | 左侧导航   | 主界面                       | 设置区                 | |
|  | YAML 合并  | 对话  |  应用页面            | 基础：模型 / 扩展      | |
|  | 扩展贡献   | Conversation / Page          | 功能扩展：Agent/Tool/MCP| |
|  +------------+------------------------------+------------------------+ |
|  数据上下文：Workspace(管理) | Project(开发)                            |
|  领域操作：注册表 | 版本 | 策略 | 部署 | 扩展安装 | 模型绑定 | Run 定义  |
+-----------------------------------+-------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| 规范合约层                                                              |
| AI 对象模型 | Extension/Capability | Runtime Contract | Artifact API    |
+-----------------------------------+-------------------------------------+
                                    |
                    +---------------+---------------+
                    |                               |
                    v                               v
         +-----------------------+       +--------------------------+
         | 编译器 / 解析器       |       | Provider / Adapter 层    |
         | 验证 / 依赖解析       |       | 模型 / 工具 / MCP        |
         | 能力绑定              |       | 运行时 / 查看器 / 页面   |
         +-----------+-----------+       +--------------------------+
                     |
                     v
         +-------------------------------+
         | 不可变执行快照                  |
         | Agent + 模型 + 工具 + 策略    |
         | 输入 + 输出 + 运行时          |
         +---------------+---------------+
                         |
                         v
+-------------------------------------------------------------------------+
| 运行时面                                                                |
| Run 管理器 | Agent Runtime | Tool Runtime | 状态 | Session            |
| 事件流 | 调度器 | 沙箱 | Artifact 操作                                 |
+----------------------------+--------------------------------------------+
                             |
                             v
+-------------------------------------------------------------------------+
| 存储 / 基础设施                                                         |
| PostgreSQL | 对象存储 | 缓存/队列 | 向量存储 | 遥测                   |
+-------------------------------------------------------------------------+
```

关键变化：

- **体验层不改。** 四个 Studio 仍是产品能力面。
- **控制面改形态。** 从资源模块平铺改为导航 + 主界面 + 设置区；Workspace / Project / 注册表等仍是控制面领域，经此形态操作。
- 规范合约层增加 **Extension / Capability**，与现有 `innospots-nexus-core` 插件能力宿主对齐。

---

# 4. 体验层与控制面

## 4.1 体验层（保持 2.0）

体验层仍是四个产品能力面，本轮不改划分、不合并、不取消：

```text
AI Studio | Agent Studio | Artifact Workspace | App Studio
```

| Studio              | 能力面                         | 进入控制面后的典型落点     |
| ------------------- | ------------------------------ | -------------------------- |
| AI Studio           | 对话、提示、模型试用           | 主界面 · 对话表面          |
| Agent Studio        | Agent 编排、运行、调试         | 主界面 · 对话 / Agent 页   |
| Artifact Workspace  | 产物预览、编辑、版本、血缘     | 主界面 · 应用页面          |
| App Studio          | 生成应用、页面、发布           | 主界面 · 应用页面          |

Studio 回答「用户能做什么」。它们不直接平铺成控制面菜单；由控制面导航指向对应表面。

## 4.2 控制面形态：导航 / 主界面 / 设置区

控制面回答「如何进入、配置、运行」。形态固定为三个区域，领域操作（Project、扩展安装、模型绑定、Run 定义、策略）都挂在这套形态上，而不是再做一套资源后台。

```text
+------------------------------------------------------------------+
| 顶栏   当前 Project                          设置入口 / 用户     |
+----------+-------------------------------------------------------+
|          |                                                       |
| 左侧导航 |                 主界面                                |
|          |                                                       |
| YAML     |   对话表面              应用页面表面                  |
| 配置     |   Conversation          Page                          |
| + 扩展   |   AI / Agent Studio     Artifact / App Studio         |
| 贡献     |                                                       |
|          |                                                       |
| 页脚导航 |                                                       |
| （设置） |                                                       |
+----------+-------------------------------------------------------+
```

三个稳定区域：

| 区域     | 职责                                         | 是否由 YAML/扩展驱动 |
| -------- | -------------------------------------------- | -------------------- |
| 左侧导航 | 进入各 Studio 与扩展功能；分组、排序、权限过滤 | 是                 |
| 主界面   | 对话交互，或打开应用页面；可切换或分栏       | 表面类型由导航声明   |
| 设置区   | 配置模型与扩展；功能扩展的实例配置           | 扩展贡献配置 schema  |

控制面壳稳定。体验层 Studio 保持不变；功能增减通过导航 YAML 和扩展贡献完成，而不是改 Studio 划分，也不是把控制面拆回资源模块列表。

## 4.3 左侧导航：YAML 驱动

平台默认导航与扩展贡献导航都使用同一 YAML 合约。控制面启动时合并、按权限裁剪、按 `order` 排序。默认项应对齐四个 Studio，而不是另造一套产品信息架构。

```yaml
apiVersion: control.nav/v1
kind: NavManifest
metadata:
  name: default
spec:
  items:
    - id: ai-studio
      title:
        zh: AI Studio
        en: AI Studio
      icon: message
      surface: conversation
      studio: ai
      order: 10
    - id: agent-studio
      title:
        zh: Agent Studio
        en: Agent Studio
      icon: bot
      surface: conversation
      studio: agent
      order: 20
    - id: artifacts
      title:
        zh: Artifact
        en: Artifact
      icon: package
      surface: page
      page: artifact.browser
      studio: artifact
      order: 30
    - id: apps
      title:
        zh: App Studio
        en: App Studio
      icon: app
      surface: page
      page: app.studio
      studio: app
      order: 40
    - id: settings
      title:
        zh: 设置
        en: Settings
      icon: settings
      surface: page
      page: settings.basic
      placement: footer
      order: 90
```

扩展贡献导航片段，而不是改控制面源码或拆分 Studio：

```yaml
apiVersion: control.nav/v1
kind: NavContribution
metadata:
  extension: knowledge-pack
spec:
  items:
    - id: knowledge
      title:
        zh: 知识
        en: Knowledge
      icon: book
      surface: page
      page: knowledge.list
      order: 30
      requires:
        capability: knowledge.read
```

合并规则：

1. 平台默认清单作为基底。
2. 已启用扩展的 `NavContribution` 按 `id` 合并；同 `id` 不允许覆盖核心项。
3. `requires.capability` 与当前 Project 已授权能力求交，无权限则隐藏。
4. `placement: footer` 固定在导航底部（设置等）。
5. 阶段 1 不提供客户侧可视化导航编辑器；调整方式就是改 YAML / 扩展贡献。

该合约应对齐 console 现有 `MenuDeclaration` / `UiSpecPageDeclaration` 的语义（目录节点 vs 页面节点），控制面增加 `surface`（`conversation` | `page`）和可选 `studio`（指向体验层四个能力面之一）。

## 4.4 主界面：对话与应用页面

主界面只有两种表面，由导航项的 `surface` 决定：

```text
surface: conversation  ->  ConversationHost
surface: page          ->  PageHost(pageKey)
```

**对话表面**

- 与当前 Project 内已启用的 Agent 扩展对话。
- 流式事件、工具调用卡片、Artifact 卡片。
- 对话可唤起页面表面（例如生成文档后在右侧打开预览）。

**应用页面表面**

- 由 `pageKey` 路由到 Artifact Workspace、App Studio 或扩展贡献的页面。
- 页面由 Studio 或扩展贡献，不是控制面内置路由表。

阶段 1 交互：

- 点击导航：在对话与页面之间切换。
- Artifact 预览允许简单分栏（对话 + 页面）。
- 不在阶段 1 做完整多标签 IDE。

## 4.5 设置区

设置是控制面的配置宿主，不是第五个 Studio。分层如下：

```text
设置
├── 基础设置
│   ├── 模型
│   │   ├── 模型提供者
│   │   ├── 默认模型
│   │   └── 当前 Project 的模型绑定
│   └── 扩展
│       ├── 可安装 / 已安装列表
│       ├── 启用 / 禁用
│       └── 扩展级配置（来自扩展 config schema）
└── 功能扩展（由已启用扩展贡献）
    ├── Agent
    ├── Tools
    └── MCP
```

规则：

- **基础设置只承认两类宿主配置：模型、扩展。**
- **Agent、Tools、MCP 不是基础设置的并列一级分类**，它们是功能扩展。安装后按其类型出现在设置区，配置项来自扩展自己的 schema。
- 模型保留在基础设置中，因为模型绑定是运行时的平台原语，不是某个可选功能包。
- 未安装某类扩展时，设置区不预留空的 Agent / Tools / MCP 菜单。

---

# 5. 可扩展功能体系

智能体框架的产品含义是：平台先提供宿主，再通过扩展生长功能。

## 5.1 宿主与贡献点

```text
智能体框架（Host）
  ├── 扩展生命周期   发现 / 安装 / 启用 / 配置 / 停用
  ├── 能力路由       CapabilityRegistry / CapabilityRouter
  ├── 运行时合约     AgentRuntime / ToolRuntime
  ├── 控制面贡献点   nav / page / conversation.card / settings.section
  └── 安全贡献点     能力声明 / 权限审查 / 密钥
```

这与 `innospots-nexus-core` 已有的 Plugin / Capability 模型对齐：

- `PluginDefinition`：扩展静态声明
- `CapabilityContribution`：扩展提供的能力
- `CapabilityRouter`：按类型与标签选择实现
- `ConfigDefinition`：扩展配置 schema，驱动设置区表单

AI 平台不另起一套插件内核，而是在该宿主上定义 AI 领域的能力类型。

## 5.2 能力类型

阶段 1 先冻结这些能力名：

| Capability            | 谁提供           | 控制面如何消费                         |
| --------------------- | ---------------- | -------------------------------------- |
| `model.provider`      | 模型扩展         | 基础设置-模型；运行时模型调用          |
| `agent.runtime`       | Agent 扩展       | 对话表面选择 Agent；Run 执行           |
| `tool.invoke`         | Tool 扩展        | Agent 调用；设置区 Tools               |
| `mcp.server`          | MCP 扩展         | 作为 Tool 来源接入；设置区 MCP         |
| `artifact.viewer`     | 查看器扩展       | 页面表面预览 Artifact                  |
| `artifact.editor`     | 编辑器扩展       | 页面表面编辑 Artifact                  |
| `control.nav`         | 任意扩展         | 合并到左侧导航                         |
| `control.page`        | 任意扩展         | 主界面页面路由                         |
| `settings.section`    | 任意扩展         | 设置区功能扩展段                       |

Agent、Tools、MCP 的共同身份是 **功能扩展**：同一安装、启用、配置、权限审查流程，不同的 Capability 合同。

## 5.3 扩展清单

```yaml
apiVersion: ai.platform/v1
kind: Extension
metadata:
  name: research-agent
  version: 1.2.0
  type: agent          # agent | tool | mcp | model | viewer | page
spec:
  runtime:
    type: pi
  capabilities:
    - agent.runtime
    - control.nav
    - settings.section
  permissions:
    network: restricted
    filesystem: project
    secrets: []
  contributes:
    nav:
      - id: research
        surface: conversation
        agent: research-agent
        order: 15
    settings:
      section: agent.research
```

能力（Capability）与权限（Permission）必须分开声明：

- 能力 = 扩展设计用来做什么。
- 权限 = 本次安装被允许做什么。

请求 `network` 的扩展，不会在每次安装时自动获得无限制网络访问。

## 5.4 为何把 Agent / Tool / MCP 都做成扩展

| 若做成一等内置模块     | 结果                         |
| ---------------------- | ---------------------------- |
| 设置区写死三类菜单     | 未使用的能力仍占用产品结构   |
| 控制面为每类能力做后台 | 控制面裂变成资源清单，离开导航/主界面/设置形态 |
| 安装流程各写一套       | 清单、权限、版本无法复用     |

统一为扩展后：新增一种工具、一个 MCP 服务器、一个专用 Agent，只增加扩展包，不改控制面形态，不改体验层 Studio 划分，不改设置信息架构。

---

# 6. 数据归属边界：Workspace 与 Project

## 6.1 层级

```text
Tenant
  └── Workspace          管理可见；隔离与治理边界
        └── Project      客户开发可见；控制面与资源归属边界
              └── 资源   Agent / Tool / MCP / 模型绑定 / Artifact / Run
```

与现有 Nexus 治理对齐：

- 隔离键仍然是 `tenantId` + `workspaceId`（`WorkspaceBaseEntity` / `TLC`）。
- Project 是 **AI 领域的归属容器**，不是新的 IAM 隔离键，也不替代 kernel 的 Workspace。
- 多租户治理方案中「Workspace 下不引入 Project 业务实体」约束的是 IAM；本方案的 Project 属于 AI 开发平台领域，挂在已有 Workspace 之下。

## 6.2 可见性

| 主体           | 可见 Workspace | 可见 Project | 典型操作                         |
| -------------- | :------------: | :----------: | -------------------------------- |
| 平台运营       |       是       |      是      | 租户/空间治理、审计、配额        |
| 租户管理员     |       是       |      是      | 创建空间、分配成员、查看全部项目 |
| 客户开发者     |       否       |      是      | 打开项目、对话、管理扩展与产物   |

规则：

- **Workspace 不对客户开发开放。** 开发者 API、控制面顶栏、资源路径都不出现 workspace 选择器。
- 开发者进入控制面时，上下文是当前 Project；服务端由 Project 解析出 `workspaceId` 做隔离。
- 管理端（kernel / platform 控制台）管理 Workspace，并可以按空间查看其下 Project。
- 一个 Workspace 可包含多个 Project；Project 不能跨 Workspace 移动（阶段 1）。

## 6.3 API 边界

```text
管理 API（管理员）
  /management/workspaces
  /management/workspaces/:id/projects

开发 API（客户开发者）
  /projects
  /projects/:id/extensions
  /projects/:id/models
  /projects/:id/runs
  /projects/:id/artifacts
```

开发 API 的资源路径以 Project 为根。Workspace 只出现在管理 API。

---

# 7. 核心领域模型

## 7.1 持久化平台对象

```text
Tenant
  Workspace                 # 仅管理
    Project                 # 客户开发入口
      Environment           # 阶段 2；阶段 1 可用默认环境
        资源

资源（均归属 Project，隔离键仍带 workspaceId）
- 扩展安装（Agent / Tool / MCP / 其他）
- 模型绑定
- 提示词
- 知识
- 工作流
- 评估
- 应用
- Artifact
- 部署
```

模型是基础设置中的平台原语；Agent / Tool / MCP 以扩展安装记录存在，不再作为与模型同级的独立资源根类型。

## 7.2 运行时对象

运行时对象是操作性的，不作为可编辑资源：

```text
Run
  Session
  Step
  ToolCall
  ModelCall
  ArtifactWrite
  Event
  Trace
```

`Run` 是历史执行记录；`Extension`（含 Agent）是版本化资源。

## 7.3 引用

```typescript
interface ResourceRef {
  type: string;
  id: string;
  version: string;
}

interface ProjectRef {
  projectId: string;
  workspaceId: string; // 服务端解析，不暴露给客户开发 API
}
```

生产执行必须解析到具体版本。客户开发契约只传 `projectId`。

---

# 8. Agent 模型与第三方集成

Agent 是功能扩展的一种。安装、权限、导航贡献走统一扩展管道；执行走 Runtime Contract。

## 8.1 Agent Provider 合约

```typescript
interface AgentProvider {
  getManifest(): AgentManifest;
  validate(input: unknown): ValidationResult;
  execute(input: AgentInput, context: AgentContext): AsyncIterable<AgentEvent>;
}
```

提供者：

```text
NativeAgentProvider
PiAgentProvider
RemoteAgentProvider
MCPAgentProvider
ExternalAPIProvider
```

应用代码依赖 `AgentProvider`，不直接依赖 Pi API。

## 8.2 Tool 与 MCP

```text
Tool 扩展  --tool.invoke-->  ToolRuntime
MCP 扩展   --mcp.server-->   MCP Adapter --tool.invoke--> ToolRuntime
Agent 扩展 --agent.runtime--> AgentRuntime --调用--> ToolRuntime
```

MCP 不是第三套运行时，而是 Tool 能力的一种提供者。设置区出现 MCP，是因为用户需要配置服务器地址与授权，并不意味着 MCP 拥有独立于扩展体系的生命周期。

---

# 9. 运行时架构

## 9.1 Runtime Contract

平台拥有此合约：

```typescript
interface AgentRuntime {
  createRun(input: AgentRunInput): Promise<AgentRun>;
  stream(runId: string): AsyncIterable<AgentEvent>;
  resume(runId: string): Promise<AgentRun>;
  cancel(runId: string): Promise<void>;
  inspect(runId: string): Promise<AgentTrace>;
}
```

## 9.2 Pi 集成

```text
平台 AgentRuntime
        |
        v
PiRuntimeAdapter
        |
        v
Pi Agent Runtime
```

适配器映射：平台 Agent Context、工具合约、模型绑定、事件模型、取消/恢复语义。替换运行时时平台合约保持稳定。

## 9.3 执行规则

运行时必须执行不可变 `ExecutionSnapshot`：

```typescript
interface ExecutionSnapshot {
  project: ResourceRef;
  agent: ResourceRef;
  models: ResourceRef[];
  tools: ResourceRef[];
  knowledge: ResourceRef[];
  policies: ResourceRef[];
  artifactOutputs: ArtifactBinding[];
  runtime: RuntimeConfig;
}
```

正在运行的任务不得因为用户在控制面设置区编辑了扩展配置而静默改变行为。

---

# 10. 编译器与 DSL 策略

## 10.1 合约优先，语法其次

```text
Studio / 控制面 / API
   |
   v
规范 Agent/Run 定义
   |
   v
验证 + 解析
   |
   v
ExecutionSnapshot
   |
   v
运行时
```

合约稳定后再加 YAML/DSL -> 解析器 -> AST -> 规范模型。导航 YAML 属于控制面配置，不是执行 DSL，阶段 1 即可使用。

## 10.2 编译器职责

- schema 验证
- 类型验证
- 依赖解析
- 版本解析
- 能力验证
- 权限验证
- 环境验证
- 运行时兼容性验证
- 快照生成

---

# 11. Artifact 平台

Artifact 仍是一等对象，也是该架构中最强的产品差异化因素。

## 11.1 定义

> Artifact 是由 AI 或人类生成或修改的、持久的、可寻址的、版本化的、可预览且可转换的数字输出。

归属：每个 Artifact 属于一个 Project（管理侧可按 Workspace 汇总）。

类型系列：

```text
文档 / Markdown / HTML / 代码
图像 / 视频 / 音频
演示文稿 / 电子表格
数据集 / JSON
应用程序包
```

## 11.2 模型

```typescript
interface Artifact {
  id: string;
  projectId: string;
  type: string;
  version: string;
  status: "draft" | "reviewed" | "published" | "archived";
  contentRef: ContentRef;
  source?: {
    runId?: string;
    agentRef?: ResourceRef;
    parentArtifact?: ArtifactRef;
  };
  metadata: Record<string, unknown>;
}
```

## 11.3 存储与操作

```text
Artifact 元数据  -> PostgreSQL
Artifact 内容    -> 对象存储
预览缓存          -> 对象存储 / 缓存

操作：创建 / 读取 / 更新 / 修补 / 快照 / 差异 / 渲染 / 导出 / 转换
```

大型二进制文件不得嵌入事务行。

## 11.4 血缘

```text
Research Agent
      |
      v
research.md
      |
      v
PPT Agent
      |
      v
presentation.pptx
```

血缘通过 `sourceRunId`、`parentArtifactId` 和转换记录查询。Artifact Workspace（体验层）经控制面主界面的产物页消费该图。

---

# 12. Artifact 查看器 / 编辑器

查看器与编辑器也是扩展，贡献 `artifact.viewer` / `artifact.editor`，并注册到页面表面。

```typescript
interface ArtifactPlugin {
  id: string;
  supports(type: string, version?: string): boolean;
}

interface ArtifactViewerPlugin extends ArtifactPlugin {
  render(input: ViewerInput): ViewerOutput;
}

interface ArtifactEditorPlugin extends ArtifactPlugin {
  open(input: EditorInput): Promise<EditorSession>;
  save(session: EditorSession): Promise<ArtifactVersion>;
}
```

控制面按 Artifact 类型选择插件，在主界面页面表面打开。阶段 1 内置文本/Markdown/JSON/图片预览即可。

---

# 13. 安全与信任边界

## 13.1 信任级别

```text
可信原生
     |
     v
已审核私有包
     |
     v
已验证第三方包
     |
     v
不可信 / 远程
```

信任级别影响安装和运行时限制。

## 13.2 权限模型

```text
network
filesystem          # 作用域为当前 Project，不是 Workspace 根
secrets
model.invoke
tool.invoke
knowledge.read
artifact.read
artifact.write
code.execute
```

客户开发者的文件系统与 Artifact 权限以 Project 为边界。管理员跨项目操作走管理 API 与独立授权。

## 13.3 执行路径

```text
Extension (Agent)
  |
  v
执行策略
  |
  v
能力检查
  |
  v
沙箱 / 工具边界
  |
  v
外部资源
```

注册表条目不能仅因为通过 schema 验证就被视为可信。

---

# 14. 控制面 / 运行时面

## 14.1 控制面

控制面的**形态**是导航 + 主界面 + 设置区（见第 4 节）。经此形态承载的**领域操作**包括：

- Workspace（仅管理）与 Project（开发）
- 扩展注册表、安装、启用、依赖解析
- 模型绑定
- 资源和版本
- 策略
- 评估设置
- 部署
- 执行快照创建
- 导航 YAML 合并与扩展贡献读取

不要把这些领域再做成与导航/主界面/设置并列的第五套控制台。

## 14.2 运行时面

- Run 执行
- 流式事件（供主界面对话表面消费）
- 会话 / 状态
- 工具调用 / 模型调用
- Artifact 操作
- 重试 / 取消 / 恢复
- 沙箱执行

## 14.3 边界

```text
体验层 Studio
   |
   v
控制面（导航 / 主界面 / 设置区）
   |
   v
ExecutionSnapshot
   |
   v
运行时面
   |
   v
Run / Artifact / Trace
```

运行时面不得依赖瞬时 UI 状态。主界面对话表面只是 Run 事件的消费者。

---

# 15. 数据架构

| 系统         | 职责                                                     |
| ------------ | -------------------------------------------------------- |
| PostgreSQL   | 领域元数据、Workspace/Project、扩展安装、版本、Run、策略 |
| 对象存储     | Artifact 内容、包二进制、预览、导出                      |
| Redis / 队列 | 缓存、短时协调、任务队列                                 |
| 向量存储     | 启用知识能力时的检索索引                                 |
| 遥测存储     | 追踪、指标、日志和高容量事件分析                         |

向量与遥测实现保持可替换。

---

# 16. 事件与 Run 模型

业务事件与运行时流事件分离。

## 16.1 业务事件

```text
WorkspaceCreated          # 仅管理面
ProjectCreated
ExtensionInstalled
ExtensionEnabled
AgentPublished
DeploymentCreated
ArtifactCreated
ArtifactPublished
EvaluationCompleted
```

## 16.2 运行时事件

```text
RunStarted
StepStarted
ModelCalled
ToolCalled
ArtifactWritten
StepCompleted
RunCompleted
RunFailed
RunCancelled
```

对话表面订阅运行时事件；管理审计订阅业务事件。

---

# 17. 三阶段开发策略

## 阶段 1 - 控制面闭环

### 目标

证明可扩展框架、不变的体验层 Studio、以及新的控制面形态端到端可行：

> 管理侧准备 Workspace -> 开发者进入 Project -> 安装扩展（Agent/Tool/MCP）-> 配置模型 -> 经 YAML 导航进入 Studio -> 主界面对话或打开页面 -> 运行 -> 生成 Artifact -> 预览 -> 编辑 -> 版本化 -> 复用

### 范围

#### 体验层

- 四个 Studio 边界保持：AI Studio、Agent Studio、Artifact Workspace、App Studio
- 阶段 1 至少打通 AI/Agent 对话与 Artifact 页面；App Studio 可最小占位

#### 控制面

- 壳：顶栏 Project 上下文、左侧 YAML 导航、主界面、设置区
- 默认导航 YAML 对齐四个 Studio + 设置
- 扩展导航贡献合并
- 主界面：对话表面 + 至少一个页面表面（Artifact 浏览/预览）
- 设置：基础设置（模型、扩展）+ 已启用功能扩展配置

#### 数据边界

- Workspace 管理 API（创建/列表，不进入开发控制面）
- Project 开发 API 与控制面入口
- 资源归属 Project，隔离键 `tenantId` + `workspaceId`

#### 扩展体系

- Extension Manifest v1
- 能力类型：`model.provider` / `agent.runtime` / `tool.invoke` / `mcp.server` / `control.nav` / `control.page`
- 私有/内部 Registry
- 安装器、依赖验证、权限审查
- Agent / Tool / MCP 均走同一安装管道

#### 运行时

- Runtime Contract
- PiRuntimeAdapter
- Agent Run 生命周期、流式事件、取消/恢复（支持时）
- 基础状态/会话

#### 模型与工具

- Model Provider 合约，至少两个适配器
- Native Tool 合约
- MCP Tool 适配器

#### Artifact

- Artifact 模型、对象存储、版本化、血缘
- Markdown / JSON / 图片 / 基础文件预览
- 基础文本编辑
- Artifact Tool：创建/读取/更新/差异对比

#### 可观测性

- Run 追踪、模型/工具计时、代币/成本字段、结构化日志

### 明确不包含

- 向客户开发者暴露 Workspace
- 公开市场
- 客户侧可视化导航编辑器
- 完整可视化 DSL 构建器
- 多引擎工作流编排
- 完整企业级 ABAC
- 高级评估平台
- 完整应用部署引擎
- 所有 Artifact 编辑器
- 把控制面拆回资源模块平铺后台
- 改体验层 Studio 划分（不合并、不取消四个 Studio）

### 阶段 1 退出标准

```text
管理员创建 Workspace
        |
        v
开发者打开 Project（不见 Workspace）
        |
        v
安装 Agent / Tool / MCP 扩展
        |
        v
基础设置绑定模型
        |
        v
YAML 导航出现扩展贡献项
        |
        v
YAML 导航进入对应 Studio
        |
        v
主界面对话 -> Pi 运行时 -> 工具/MCP
        |
        v
Artifact 已创建并在页面表面预览/编辑/版本化
        |
        v
Artifact 被另一个 Agent 复用
```

---

## 阶段 2 - 平台化

### 目标

把已验证的控制面闭环变成可复现的开发者平台：编译、工作流、评估、治理。

### 范围

- 规范 schema、YAML/JSON 执行 DSL v1、编译器、ExecutionSnapshot
- 简单 DAG：Agent -> Artifact -> Agent
- 更丰富的页面表面：查看器/编辑器、血缘图、AI 编辑
- 评估：数据集、评估器合约、发布门禁
- 治理：RBAC、策略适配器、配额、审计；Workspace 级策略对管理员可见
- OpenTelemetry 对齐与成本/延迟仪表板
- 导航按角色的管理端覆盖（仍不是客户可视化编辑器）

### 退出标准

```text
控制面配置 / DSL
 |
 v
编译器 -> ExecutionSnapshot
 |
 +----> 工作流
 +----> Agent Runtime
 +----> 评估 / 治理
 v
Artifact 血缘 + 生产审计
```

---

## 阶段 3 - 生态系统与企业生产

### 目标

可复用的企业级 AI 生产系统与扩展生态。

### 范围

- 公开/私有/企业注册表、市场、签名、升级/回滚
- 多运行时与兼容性矩阵
- 应用平台：生成应用经 App Studio 挂入控制面主界面，并支持独立部署
- 企业治理：细粒度授权、策略即代码、密钥、合规、数据驻留
- Artifact 转换市场与完整编辑器生态
- 高可用、运行时扩展、私有云/本地打包

### 退出标准

```text
发现 -> 安装 -> 配置 -> 评估 -> 发布 -> 部署 -> 观测 -> 治理 -> 升级/回滚
```

系统是企业级 AI 生产平台。体验层 Studio 仍然分工明确，但它们共享同一套控制面形态，而不是各自做成互不相通的后台。

---

# 18. 开发优先级矩阵

| 能力               |     阶段 1     |    阶段 2    |     阶段 3     |
| ------------------ | :------------: | :----------: | :------------: |
| 体验层四 Studio    |    保持边界    |     增强     |    完整能力    |
| 控制面壳           |      必需      |     增强     |    企业级 UX   |
| YAML 导航          |      必需      | 角色覆盖增强 |   可选可视化   |
| 主界面 · 对话      |      必需      |     增强     |     多会话     |
| 主界面 · 页面      | 产物页 + 设置  |    丰富页    |   App 挂载     |
| 设置区（模型）     |      必需      |     增强     |   市场/企业    |
| 设置区（扩展）     |      必需      |     增强     |   多注册表     |
| Agent/Tool/MCP 扩展 |     必需      |     增强     |    生态系统    |
| Workspace（管理）  |      必需      |     治理     |    企业策略    |
| Project（开发）    |      必需      |     增强     |    环境晋升    |
| Runtime Contract   |      必需      |     稳定     |   兼容性矩阵   |
| Pi Runtime Adapter |      必需      |     增强     |    众多之一    |
| Artifact 核心      |      必需      |     增强     |    生态系统    |
| DSL / 编译器       | 规范配置优先   |     必需     |     成熟       |
| 工作流             |     最小化     |     必需     |   持久化编排   |
| 评估               |    冒烟测试    |     必需     |  企业发布门禁  |
| 市场               |       -        |   可选试点   |      必需      |

---

# 19. 推荐的仓库 / 模块结构

继续模块化单体，合约一同演进。控制面壳与体验层 Studio 分模块，但共用一个前端应用。

```text
/apps
  web                 # 控制面壳：导航 / 主界面 / 设置；承载四个 Studio
  api
  worker
  admin               # Workspace 等管理面，可复用 console

/packages
  domain
  contracts
  control-shell       # 控制面壳、表面宿主、设置宿主
  control-nav         # YAML 导航合约
  studio-ai
  studio-agent
  studio-artifact
  studio-app
  extension           # 扩展生命周期，复用 core plugin capability
  manifest
  registry
  installer
  compiler
  runtime-contract
  runtime-pi
  model-providers
  tool-runtime
  mcp
  artifact-core
  artifact-viewers
  artifact-editors
  evaluation
  observability
  policy
  storage

/infrastructure
  docker
  k8s
  migrations
```

规则：包边界先体现架构所有权；部署边界后置。四个 Studio 作为体验模块分包，但必须挂在同一控制面壳上，禁止各自独立成应用。

---

# 20. API 边界建议

首批 API 按领域操作组织，并按管理/开发分流。

```text
# 管理（Workspace 仅此可见）
POST   /management/workspaces
GET    /management/workspaces/:id/projects

# 开发（客户开发者，以 Project 为根）
POST   /projects
GET    /projects/:id
GET    /projects/:id/nav
POST   /projects/:id/extensions/install
POST   /projects/:id/extensions/:extId/enable
GET    /projects/:id/models
PUT    /projects/:id/models/bindings
POST   /projects/:id/runs
GET    /runs/:id
POST   /runs/:id/cancel
GET    /runs/:id/events
GET    /artifacts/:id
POST   /artifacts/:id/patch
GET    /artifacts/:id/versions
POST   /artifacts/:id/transform
POST   /deployments
```

避免把内部持久化模式（含 `workspaceId`）直接暴露为客户开发 API。

---

# 21. 关键架构风险与控制

| 风险                     | 为何重要                     | 控制措施                                      |
| ------------------------ | ---------------------------- | --------------------------------------------- |
| 控制面做成资源平铺后台   | 离开导航/主界面/设置形态     | 控制面固定三区；领域操作全部挂入此形态        |
| 改动体验层 Studio 划分   | 产品能力面不稳定             | 四个 Studio 保持不变，只改进入方式            |
| 把 Agent/Tool/MCP 内置化 | 设置与安装流程分叉           | 统一扩展生命周期，三者都是功能扩展            |
| Workspace 漏到开发面     | 客户心智与 IAM 边界混乱      | 开发 API/控制面只暴露 Project                 |
| Project 变成新隔离键     | 与 kernel 多租户模型冲突     | 隔离仍用 tenantId+workspaceId，Project 只归属 |
| Pi 耦合                  | 运行时迁移成本高             | Runtime Contract + Adapter                    |
| DSL 过度投资             | 语义未定时先做语法           | 规范模型优先；导航 YAML 不等于执行 DSL        |
| 市场过早                 | 包合约未稳                   | 内部注册表优先                                |
| 运行时/UI 耦合           | 执行不可复现                 | ExecutionSnapshot                             |
| Artifact 仅作为文件      | 血缘/编辑困难                | Artifact 领域模型                             |
| 扩展信任                 | 第三方代码可访问资源         | 能力 + 权限 + 沙箱                            |
| 过度微服务               | 验证阶段变慢                 | 先模块化单体                                  |

---

# 22. 架构决策摘要

## ADR-001 - 无单一 AI 框架作为平台内核

**决策：** 平台拥有领域合约；引擎作为适配器。  
**理由：** 保持供应商/运行时可替换性。

## ADR-002 - Pi 作为初始 Agent Runtime

**决策：** Pi 是平台 `AgentRuntime` 的第一个实现。  
**理由：** 加速阶段 1，同时不使 Pi 成为领域模型。

## ADR-003 - Artifact 作为一等领域对象

**决策：** Artifact 拥有独立生命周期、版本化、血缘和转换 API。  
**理由：** AI 价值通常通过持久化资产传递。

## ADR-004 - ExecutionSnapshot 作为控制/运行时边界

**决策：** 运行时执行不可变快照。  
**理由：** 可复现性、调试、审计和部署一致性。

## ADR-005 - 合约优先的 DSL 策略

**决策：** 先有规范结构合约，后有面向用户的执行 DSL。导航 YAML 属于控制面配置，可在阶段 1 使用。  
**理由：** 执行语义与控制面导航配置分离。

## ADR-006 - 内部注册表优先于市场

**决策：** 先可安装包和注册表，市场后续。  
**理由：** 市场依赖稳定的清单、依赖、信任和版本化。

## ADR-007 - 先模块化单体

**决策：** 少量运行时服务，同时保持清晰模块边界。  
**理由：** 架构验证阶段降低运维复杂性。

## ADR-008 - 体验层 Studio 不变，控制面改为三区形态

**决策：** 体验层保持 AI Studio / Agent Studio / Artifact Workspace / App Studio。控制面采用 YAML 导航 + 主界面（对话 | 应用页面）+ 设置区。  
**理由：** Studio 是产品能力面，应保持稳定；变化的是进入、配置与运行的控制面形态。

## ADR-009 - Agent / Tool / MCP 均为功能扩展

**决策：** 基础设置只包含模型与扩展；Agent、Tools、MCP 走统一扩展安装与配置。  
**理由：** 三者共享清单、权限、生命周期和控制面贡献点；模型是运行时原语，留在基础设置。

## ADR-010 - Workspace 仅管理可见，Project 为客户开发边界

**决策：** Workspace 与 Project 都是数据归属边界；客户开发者只见 Project。隔离键仍为 `tenantId` + `workspaceId`。  
**理由：** 复用 kernel 已有隔离模型，同时给开发者一个稳定的产品入口，避免把治理空间暴露为开发容器。

## ADR-011 - 导航以 YAML 为调整面

**决策：** 控制面不内置死导航。默认清单对齐四个 Studio，并与扩展 `NavContribution` 合并渲染。  
**理由：** 调整控制面信息架构无需改壳内核，也不改体验层 Studio 划分；与 console 菜单声明语义对齐。

---

# 23. 最终目标架构

```text
                    +--------------------------------------+
                    |            体验层                     |
                    |  AI Studio | Agent Studio             |
                    |  Artifact Workspace | App Studio      |
                    +--------------------+-----------------+
                                         |
                                         v
                    +--------------------------------------+
                    |            控制面                     |
                    |  导航(YAML) | 主界面(对话/页面) | 设置 |
                    |  Workspace(管理) / Project(开发)      |
                    |  扩展安装 / 模型绑定 / 版本 / 策略    |
                    +--------------------+-----------------+
                                         |
                                         v
                    +--------------------------------------+
                    |           规范合约                    |
                    |  Extension/Capability / 对象 / 清单   |
                    |  Runtime / Artifact                   |
                    +--------------------+-----------------+
                                         |
                          +--------------+---------------+
                          |                              |
                          v                              v
                +-------------------+          +-------------------+
                | 编译器/解析器     |          | Adapter/Provider  |
                +---------+---------+          | Agent/Tool/MCP    |
                          |                    | 模型/页面/查看器  |
                          v                    +-------------------+
                +-----------------------+
                | ExecutionSnapshot     |
                +-----------+-----------+
                            |
                            v
                +-----------------------+
                |     运行时面           |
                | Pi / 其他 Runtime      |
                | 工具 / 状态 / 事件     |
                | 沙箱 / 调度器          |
                +-----------+-----------+
                            |
                            v
                +-----------------------+
                |     Artifact          |
                | 预览 / 编辑            |
                | 版本 / 转换 / 血缘     |
                +-----------------------+

  横切：安全 | 策略 | 审计 | 评估 | OTel
  扩展：Agent | Tool | MCP | 页面 | 导航 | 查看器
```

---

# 24. 最终建议

3.1 的实施顺序：

```text
阶段 1
体验层四 Studio 保持 + 控制面三区（YAML 导航 / 主界面 / 设置）
+ 扩展宿主 + Project 开发边界 + Agent Runtime + Artifact
        |
        v
证明：管理空间 -> 开发项目 -> 安装扩展 -> 经导航进入 Studio -> 对话/页面 -> 产物闭环
        |
        v
阶段 2
编译器 + 执行 DSL + 工作流 + 评估 + 治理
        |
        v
平台化并使执行可复现
        |
        v
阶段 3
市场 + 多运行时 + 应用挂载 + 企业运维
```

最重要的架构约束：

> **平台必须拥有合约、控制面形态和扩展生命周期；它不必拥有所有功能实现。**

最重要的产品约束：

> **客户开发者始终在 Project 与控制面中工作；Workspace 只出现在管理面。**

最重要的分层约束：

> **体验层四个 Studio 保持不变；控制面用导航 + 主界面 + 设置区承载它们。新功能通过 YAML 导航和扩展贡献进入控制面，而不是改 Studio 划分。**
