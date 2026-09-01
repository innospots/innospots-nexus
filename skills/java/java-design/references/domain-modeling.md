# 领域建模设计决策

本文件回答「这块业务数据应该长什么样」。关注设计决策与取舍，代码模板见
`java:develop` 的 `code-templates.md`。

---

## 实体的持久化范围选择

| 范围 | 基类 | 判定 |
|------|------|------|
| workspace（租户 + 工作区） | `WorkspaceBaseEntity` | **默认选择**，工作区持有的业务记录 |
| tenant（仅租户，不含工作区） | `TenantBaseEntity` | 租户级但不属于某个工作区 |
| 平台级 / realm 全局 | `BaseEntity` | 需求明确标明是平台范围或全局（用户、凭据、服务注册） |

**禁止**新造 `ProjectBaseEntity` 或 `projectId` 隔离列。

范围选择错误的典型代价：唯一索引漏掉租户/工作区维度导致跨租户串数据；
或反过来把真正全局的数据套上工作区隔离导致系统级查询无法进行。

### 字段评审清单

每个拟加入的字段都要能回答：

| 问题 | 说明 |
|------|------|
| 业务含义 | 字段表示什么业务事实 |
| 必填/选填 | 决定 `nullable` |
| Java 类型 | 是否需要枚举而非 String |
| 持久化长度 | 2 的幂，或用 `@Lob` |
| 默认值归属 | 谁负责填默认值 |
| 是否可变 | 决定能否出现在 update 请求 |
| 是否进索引 | 是否用于唯一查找、外键查找、状态过滤、排序 |
| 是否属于本域 | 是否越界到相邻域 |

### 禁止加入实体的内容

- 继承来的审计与租户/工作区字段（`tenantId` `workspaceId` `createdAt` `updatedAt` `createdBy` `updatedBy`）
- 仅响应用的瞬态字段
- 没有明确查询需求的缓存祖先路径
- 与导航无关实体的 HTTP 方法、权限动作
- 框架特定的传输对象
- 「因为遗留实体里有」而保留的字段

### 主键与稳定键

| 键 | 性质 | 规则 |
|----|------|------|
| 技术主键（`roleId`） | String，由 `DbPrimaryGenerator` 经 `IdGenerator.ulid(prefix)` 生成 | `@TableId(ASSIGN_UUID)` + `@Id` + `@Column(length=32)`；**operator 不得手工赋值** |
| 稳定业务键（`roleCode`、`menuKey`） | 归属范围内唯一，创建后不可变 | 唯一索引包含范围列（`workspace_id` / `tenant_id`）；**排除出 update 请求** |

`idPrefix()` 必须短小、小写、稳定、足以识别记录族。不得用临时模块名派生，
也不得在普通重构中更改。

### 索引设计

索引从访问模式反推，覆盖：

1. 稳定唯一查找 → `uk_` 唯一索引
2. 外键/父级查找 → `idx_`
3. 常用状态与分页过滤字段 → `idx_`
4. 关联唯一性 → `uk_`
5. 树查询需要的兄弟排序 → `idx_`

命名：`uk_<表名概念>_<用途>` / `idx_<表名概念>_<用途>`，表名作用域明确。
工作区唯一性索引含 `workspace_id`，租户唯一性索引含 `tenant_id`。

---

## 请求记录（Request）

### 何时拆分请求

**操作修改权不同就必须拆**。不能用一个笼统 `XxxRequest` 承担多种修改权。

| 用途 | 命名 | 是否含稳定键 | 备注 |
|------|------|------------|------|
| 创建 | `XxxCreateRequest` | 可含 | 可携带稳定业务键 |
| 更新可变属性 | `XxxUpdateRequest` | **排除** | 排除不可变稳定键与受保护系统字段 |
| 更新单个生命周期属性 | `XxxStatusUpdateRequest` | — | 状态类单独操作 |
| 分页查询 | `XxxPageRequest` | — | 可组合 `SimpleQueryRequest` |
| 树查询 | `XxxTreeRequest` | — | |
| 排序 | `XxxOrderRequest` | — | 优先于重复的标量参数 |
| 新增关联 | `XxxAddRequest` | — | 增量 |
| 整体替换关联集 | `XxxReplaceRequest` | — | 必须定义「省略是否等于删除」 |
| 变体注册 | `XxxPasswordRegisterRequest` | — | 通过变体注册 |

### 约束

- 必须是 record
- 集合组件在紧凑构造器中 `List.copyOf` / `Set.copyOf` / `Map.copyOf`
- `null` 转不可变空集合（当空与缺失同义时）
- 分页请求归一化非法分页值到共享默认值
- bean 绑定需要时提供无参构造器；查询参数显式 `@QueryParam`，必要时 `@DefaultValue`
- **禁止**把实体当请求使用
- **禁止**加 `Dto` / `Command` / `Payload` 冗余后缀

---

## 视图记录（VO）

| 场景 | 命名 |
|------|------|
| 主管理/详情视图 | `XxxVo` |
| 紧凑选择器选项 | `XxxOptionVo` |
| 资料类投影 | `XxxProfileVo` |
| 运行时/导航视图 | `XxxNavigationVo` |

- 必须是 record
- 集合元素类型用**单数**（一个元素一个类型）
- 子视图与集合字段在紧凑构造器中防御拷贝
- 可包含：领域枚举（而非持久化的 String）、派生计数、树形子视图、审计时间戳、运行时精简字段
- **禁止**直接暴露持久化实体
- **禁止**大写 `VO`、`Response`、`Result`、`Dto`（`R<T>` 与 `PageResult<T>` 已表达包装语义）

---

## 内部模型（Model）

仅在需要「既不是持久化实体、也不是传输 record」的内部业务模型时创建
`domain.model`。

- 按业务概念命名，不带 `Model` / `Dto` / `Pojo` / `Bean` / `Data` 后缀
- 可封装自身的不变量、校验、计算与状态转换
- **不要**为了把领域对象当纯数据袋而把所有逻辑搬进 service

---

## 枚举（Enums）

| 规则 | 说明 |
|------|------|
| 表示封闭业务概念，而非传输细节 | 错误的例子：把 HTTP 方法列表塞进导航枚举 |
| 放 `domain.enums` | |
| 语义匹配时复用平台枚举 | 如 `BasicStatus` |
| 不合并其他域拥有的分类 | 菜单类型可含目录/页面/外链，但**不该**同时定义按钮或 API 权限 |
| 常量 `UPPER_SNAKE_CASE` | |
| 常量含义不清晰时加 Javadoc | |
| 枚举用 `State` / `Status` / `Mode` / `Type` 区分 | `State` 运行时状态机；`Status` 业务可用性或持久化生命周期；`Mode` 选定运行模式；`Type` 封闭分类 |

---

## 领域事件（Event）

### 归属

- 发布域拥有事件契约，放发布域的 `domain.event`，实现 `DomainEvent`
- `kernel` 与 `platform` **不得互相引用对方事件类型**
- 需要跨这两个模块协调时，用可同时依赖两者的 application/adapter 模块，
  或真正业务中立的低层契约

### 形状

- 不可变 record
- 只包含消费者真正需要的数据
- **禁止**把 DAO、service、可变实体或基础设施对象放进事件

### 命名与标识

- 过去时事实：`RoleCreatedEvent`、`PluginStoppedEvent`
- 事件类型串用稳定小写点分名：`role.created`
- 事件类型串与载荷都是兼容契约：可加字段，不得在内部重构时重命名已发布的类型串

### 发布时机与方式

| 规则 | 说明 |
|------|------|
| 状态变更成功之后才发布 | 写入或事务仍可能失败时不得先发成功事件 |
| `EventBus.publish(event)` | 默认，无需返回值的通知 |
| `EventBus.publishSync(event)` | **仅**当调用方确实需要立即拿到处理器结果 |
| 处理器失败 | 抛 `NexusException` + 对应 `StatusCode` |
| 注册与清理 | 在所属模块的生命周期边界 `subscribe`/`unsubscribe`；注册方负责清理 |

不得用同步事件重建模块间的直接服务调用。

### 处理器

- 消费方在自身 `handler` 包定义 `XxxEventHandler`，实现 `EventHandler<XxxEvent>`
- 处理器可委托本模块的 service 或 operator
- 发布方只依赖事件契约与 `EventBus`，**不得依赖消费方的 handler**

---

## 领域状态码（StatusCode）

仅在现有平台状态码无法准确表达时新增。

| 步骤 | 动作 |
|------|------|
| 1 | 搜索 `NexusStatusCode`、各域状态枚举、技术状态枚举与调用点，确认无同义码 |
| 2 | 判定归属：平台级 → base；领域级 → `<domain>.domain.enums`；技术级 → 技术边界旁 |
| 3 | 使用已批准的三字母模块段（`NEX`、`PLG` 等）；新模块段需登记与评审 |
| 4 | 选择表达失败语义的 `StatusCategory`，**不因 HTTP 映射方便而选** |
| 5 | 分配未使用的四位本地码；**不得重排现有码使列表看起来连续** |
| 6 | 定义稳定元数据：`UPPER_SNAKE_CASE` 常量 + 双语 message/advice + 类别 + 本地码 + HTTP 映射 |
| 7 | 用枚举常量类型化构造；仅 interop 边界才做 raw-code 注册 |
| 8 | **集成前**加契约测试：形状、本地唯一性、元数据、类别、HTTP 映射、cause 保留、端点映射 |
| 9 | 审查兼容消费方：客户端、本地化资源、看板、告警规则、持久化错误载荷、事件消费者、配置键 |

状态文本中禁止出现：密钥、令牌、凭据、含密钥的完整 SQL、堆栈、请求 ID、记录 ID、
文件路径、供应商原文、用户输入。

---

## 领域建模评审门禁

- [ ] 持久化范围与基类正确
- [ ] 主键与稳定键已区分，稳定键不可变性已定义
- [ ] 每个字段都能回答八问，无冗余/越界/遗留残留字段
- [ ] 索引与访问模式匹配，命名表作用域明确
- [ ] 请求按修改权拆分，稳定键已排除出 update
- [ ] VO 按用例拆分，未暴露实体
- [ ] 集合组件已防御拷贝
- [ ] 枚举只表达本域的封闭概念
- [ ] 事件（若有）归属、形状、发布时机、清理责任已明确
- [ ] 状态码（若有）已走完九步扩展流程
- [ ] 未创建投机性的 model / event 包与空分层
