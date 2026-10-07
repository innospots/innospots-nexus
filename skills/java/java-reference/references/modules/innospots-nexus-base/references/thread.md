# 包 `thread`

## AsyncExecutors

**类型：** class

全局异步执行器门面，由单例 NexusThreadPoolExecutor 支撑。 延迟初始化，默认核心线程数 = 可用处理器数，无队列缓冲。

### 方法

#### `initialize() → synchronized void`
- **说明：** 使用默认参数初始化全局异步执行器。 / public static synchronized void initialize()

#### `initialize(int coreSize, int queueCapacity, String poolName) → synchronized void`
- **说明：** 使用指定参数初始化全局异步执行器。
- **参数：**
  - `coreSize` — 核心线程数
  - `queueCapacity` — 队列容量
  - `poolName` — 线程池名称

#### `submit(Runnable runnable) → Future<?>`
- **说明：** 提交异步 Runnable 任务。
- **参数：**
  - `runnable` — 待执行的任务
- **返回：** 任务 Future

#### `submit(Callable<T> callable) → Future<T>`
- **说明：** 提交异步 Callable 任务。
- **参数：**
  - `callable` — 待执行的任务
  - `<T>` — 返回值类型
- **返回：** 任务 Future

#### `close() → synchronized void`
- **说明：** 关闭全局异步执行器并释放资源。 / public static synchronized void close()


## ExecutorShutdown

**类型：** class

ExecutorService 优雅关闭工具。

### 方法

#### `shutdownGracefully(ExecutorService executor) → void`
- **说明：** 使用默认 5 秒等待时间优雅关闭。
- **参数：**
  - `executor` — 待关闭执行器

#### `shutdownGracefully(ExecutorService executor, Duration await) → void`
- **说明：** 发起 ExecutorService 并等待；超时后 ExecutorService。
- **参数：**
  - `executor` — 待关闭执行器
  - `await` — 等待时长


## NexusThreadFactory

**类型：** class

命名 ThreadFactory，按 ThreadExecutionRole 配置守护线程与名称前缀。

### 方法

#### `worker(String namePrefix) → NexusThreadFactory`
- **说明：** 创建非守护（ThreadExecutionRole）线程工厂。
- **参数：**
  - `namePrefix` — 名称前缀
  - `daemon` — 是否为守护线程
  - `role` — 执行角色
- **返回：** 工厂实例

#### `background(String namePrefix) → NexusThreadFactory`
- **说明：** 后台执行线程工厂（守护）。
- **参数：**
  - `namePrefix` — 名称前缀
- **返回：** 工厂实例

#### `executionRole() → ThreadExecutionRole`
- **说明：** 返回配置的执行角色。
- **返回：** 角色

#### `newThread(Runnable runnable) → Thread`
- **说明：** 创建新线程。
- **参数：**
  - `runnable` — 线程任务
- **返回：** 新创建的线程


## NexusThreadPoolExecutor

**类型：** class

自定义 ThreadPoolExecutor，在提交线程与工作线程之间捕获并传播 TLC 上下文。

### 方法

#### `poolName() → String`
- **说明：** 创建线程池执行器。
- **参数：**
  - `poolName` — 线程池名称
  - `corePoolSize` — 核心线程数
  - `maximumPoolSize` — 最大线程数
  - `keepAliveTime` — 空闲线程存活时间
  - `unit` — 存活时间单位
  - `workQueue` — 工作队列
  - `threadFactory` — 线程工厂
  - `handler` — 拒绝处理策略
- **返回：** 线程池名称

#### `hasAvailableThread() → boolean`
- **说明：** 判断是否至少有一个线程可立即处理任务。
- **返回：** 有可用线程时返回 true

#### `availableThreadCount() → int`
- **说明：** 返回当前未在执行任务的线程数。
- **返回：** 可用线程数


## ScheduledThreadPoolBuilder

**类型：** class

命名 ScheduledThreadPoolExecutor 的流式构建器；默认定时类后台线程（守护线程）。

### 方法

#### `builder(String poolName) → ScheduledThreadPoolBuilder`
- **说明：** 创建指定池名称的构建器。
- **参数：**
  - `poolName` — 线程池名称前缀
- **返回：** 构建器实例

#### `poolSize(int poolSize) → ScheduledThreadPoolBuilder`
- **说明：** 设置调度线程数（最小为 1）。
- **参数：**
  - `poolSize` — 线程数
- **返回：** 当前构建器

#### `background() → ScheduledThreadPoolBuilder`
- **说明：** 使用守护后台线程（默认定时/超时场景）。
- **返回：** 当前构建器

#### `worker() → ScheduledThreadPoolBuilder`
- **说明：** 使用非守护业务工作线程。
- **返回：** 当前构建器

#### `role(ThreadExecutionRole role) → ScheduledThreadPoolBuilder`
- **说明：** 设置执行角色。
- **参数：**
  - `role` — 角色
- **返回：** 当前构建器

#### `removeOnCancelPolicy(boolean removeOnCancelPolicy) → ScheduledThreadPoolBuilder`
- **说明：** 是否对已取消的延迟任务从队列移除（默认 true）。
- **参数：**
  - `removeOnCancelPolicy` — 策略开关
- **返回：** 当前构建器

#### `build() → ScheduledThreadPoolExecutor`
- **说明：** 构建 ScheduledThreadPoolExecutor。
- **返回：** 调度线程池


## SessionContext

**类型：** class

基于 TLC 的用户与作用域快照类型化门面。

### 方法

#### `bindUser(UserSnapshot user) → void`
- **说明：** 绑定当前用户快照并同步 TLC 身份键。
- **参数：**
  - `user` — 用户快照；为 null 时清除用户绑定

#### `bindTenant(TenantSnapshot tenant, OrganizationSnapshot organization) → void`
- **说明：** 绑定租户与企业组织档案快照。
- **参数：**
  - `tenant` — 租户快照
  - `organization` — 组织快照

#### `bindWorkspace(WorkspaceSnapshot workspace) → void`
- **说明：** 绑定当前活动工作区快照。
- **参数：**
  - `workspace` — 工作区快照

#### `bindProject(ProjectSnapshot project) → void`
- **说明：** 绑定当前活动项目快照；为 null 时清除项目作用域。
- **参数：**
  - `project` — 项目快照

#### `user() → Optional<UserSnapshot>`
- **说明：** 返回绑定的用户快照，或在可能时从 TLC 重建。
- **返回：** 用户快照的可选包装

#### `requireUser() → UserSnapshot`
- **说明：** 返回绑定的用户快照，无可用用户时抛出异常。
- **返回：** 用户快照

#### `tenant() → Optional<TenantSnapshot>`
- **说明：** 返回绑定的租户快照。
- **返回：** 租户快照的可选包装

#### `organization() → Optional<OrganizationSnapshot>`
- **说明：** 返回绑定的组织快照。
- **返回：** 组织快照的可选包装

#### `workspace() → Optional<WorkspaceSnapshot>`
- **说明：** 返回绑定的工作区快照。
- **返回：** 工作区快照的可选包装

#### `project() → Optional<ProjectSnapshot>`
- **说明：** 返回绑定的项目快照。
- **返回：** 项目快照的可选包装

#### `tenantId() → String`
- **说明：** 从 TLC 获取当前租户 ID。
- **返回：** 租户 ID

#### `workspaceId() → String`
- **说明：** 从 TLC 获取当前工作区 ID。
- **返回：** 工作区 ID

#### `projectId() → String`
- **说明：** 从 TLC 获取当前项目 ID。
- **返回：** 项目 ID

#### `requireWorkspaceId() → String`
- **说明：** 获取当前工作区 ID，空白时抛出异常。
- **返回：** 工作区 ID

#### `clearUser() → void`
- **说明：** 清除绑定的用户快照及 TLC 身份键。 / public static void clearUser()

#### `clearTenant() → void`
- **说明：** 清除租户与组织快照。 / public static void clearTenant()

#### `clearWorkspace() → void`
- **说明：** 清除工作区快照及 TLC 工作区键。 / public static void clearWorkspace()

#### `clearProject() → void`
- **说明：** 清除项目快照及 TLC 项目键。 / public static void clearProject()


## TLC

**类型：** class

线程本地上下文（Thread-Local Context）—— 类型化的 ThreadLocal 映射， 用于在异步边界间传播横切状态（追踪 ID、租户 ID、用户 ID、工作区 ID 等）。 使用 {@link #scope(Map)} 配合 try-with-resources 进行作用域上下文注入： try (var scope = TLC.scope(Map.of(TLC.TRACE_ID, "abc"))) { // 在注入的上下文中运行的代码 }

### 方法

#### `tenantId(String tenantId) → void`
- **说明：** 在上下文中设置租户 ID。传入 null 时移除该条目。
- **参数：**
  - `tenantId` — 租户 ID

#### `tenantId() → String`
- **说明：** 从上下文中获取租户 ID。
- **返回：** 租户 ID，不存在时返回 null

#### `workspaceId(String workspaceId) → void`
- **说明：** 在上下文中设置工作区 ID。传入 null 时移除该条目。
- **参数：**
  - `workspaceId` — 工作区 ID

#### `workspaceId() → String`
- **说明：** 从上下文中获取工作区 ID。
- **返回：** 工作区 ID，不存在时返回 null

#### `userId(Long userId) → void`
- **说明：** 在上下文中设置用户 ID。传入 null 时移除该条目。
- **参数：**
  - `userId` — 用户 ID

#### `userId() → Long`
- **说明：** 从上下文中获取用户 ID。
- **返回：** 用户 ID，不存在时返回 null

#### `userName(String userName) → void`
- **说明：** 在上下文中设置用户名。传入 null 时移除该条目。
- **参数：**
  - `userName` — 用户名

#### `userName() → String`
- **说明：** 从上下文中获取用户名。
- **返回：** 用户名，不存在时返回 null

#### `securityRealm(String securityRealm) → void`
- **说明：** 在上下文中设置安全域。传入 null 时移除该条目。
- **参数：**
  - `securityRealm` — 安全域

#### `securityRealm() → String`
- **说明：** 从上下文中获取安全域。
- **返回：** 安全域，不存在时返回 null

#### `tenantMemberId(String tenantMemberId) → void`
- **说明：** 在上下文中设置租户成员 ID。传入 null 时移除该条目。
- **参数：**
  - `tenantMemberId` — 租户成员 ID

#### `tenantMemberId() → String`
- **说明：** 从上下文中获取租户成员 ID。
- **返回：** 租户成员 ID，不存在时返回 null

#### `platformUserId(String platformUserId) → void`
- **说明：** 在上下文中设置平台用户 ID。传入 null 时移除该条目。
- **参数：**
  - `platformUserId` — 平台用户 ID

#### `platformUserId() → String`
- **说明：** 从上下文中获取平台用户 ID。
- **返回：** 平台用户 ID，不存在时返回 null

#### `projectId(String projectId) → void`
- **说明：** 在上下文中设置项目 ID。传入 null 时移除该条目。
- **参数：**
  - `projectId` — 项目 ID

#### `projectId() → String`
- **说明：** 从上下文中获取项目 ID。
- **返回：** 项目 ID，不存在时返回 null

#### `put(String key, Object value) → void`
- **说明：** 设置上下文值。null 值会从上下文中移除该键（等效于调用 {@link #remove}）。
- **参数：**
  - `key` — 上下文键
  - `value` — 上下文值

#### `putAll(Map<String, ?> values) → void`
- **说明：** 将给定映射中的所有条目写入上下文。映射中的 null 值会移除对应键。
- **参数：**
  - `values` — 待写入的键值映射

#### `get(String key) → Object`
- **说明：** 按键获取上下文值。
- **参数：**
  - `key` — 上下文键
- **返回：** 上下文值，不存在时返回 null

#### `getString(String key) → String`
- **说明：** 按键获取上下文值并转为字符串。
- **参数：**
  - `key` — 上下文键
- **返回：** 字符串值，不存在时返回 null

#### `getLong(String key) → Long`
- **说明：** 按键获取上下文值并转为 Long，支持类型安全转换： Long 直通、Number → longValue()、String → parseLong()。
- **参数：**
  - `key` — 上下文键
- **返回：** Long 值，无法转换时返回 null

#### `remove(String key) → void`
- **说明：** 从上下文中移除指定键。
- **参数：**
  - `key` — 上下文键

#### `snapshot() → Map<String, Object>`
- **说明：** 捕获当前上下文的快照（防御性拷贝）。
- **返回：** 上下文快照

#### `restore(Map<String, ?> context) → void`
- **说明：** 用给定映射替换整个上下文。
- **参数：**
  - `context` — 新的上下文映射

#### `scope(Map<String, ?> values) → Scope`
- **说明：** 创建作用域上下文：将给定值合并到当前上下文，返回在关闭时恢复先前状态的 Scope。 配合 try-with-resources 使用： try (var s = TLC.scope(Map.of("txId", "abc"))) { ... }
- **参数：**
  - `values` — 待合并的上下文值
- **返回：** 可自动关闭的作用域

#### `context() → Map<String, Object>`
- **说明：** 返回原始上下文映射（共享引用，非拷贝）。
- **返回：** 当前线程的上下文映射

#### `clear() → void`
- **说明：** 清除当前线程的所有上下文值。 / public static void clear()

#### `Scope(Map<String, Object> previous) → record`
- **说明：** 可自动关闭的作用域，在 close 时恢复先前的上下文。 由 TLC 内部使用。
- **参数：**
  - `previous` — 关闭时需恢复的先前上下文


## ThreadExecutionRole

**类型：** enum

线程执行角色：区分需随进程优雅收尾的业务工作线程与不阻塞 JVM 退出的后台线程。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `WORKER` | — |

### 方法

#### `daemon() → boolean`
- **说明：** 非守护线程，适合业务异步与需完整生命周期的任务。 WORKER(false), /** 守护线程，适合定时、超时、清理等后台任务。 BACKGROUND(true); private final boolean daemon; ThreadExecutionRole(boolean daemon) { this.daemon = daemon; } /** 是否以守护线程运行。
- **返回：** 后台角色为 true


## ThreadPoolBuilder

**类型：** class

NexusThreadPoolExecutor 的流式构建器。默认值：核心线程数 = 可用处理器数， 最大线程数 = 核心线程数，队列容量 = 20k，空闲存活时间 = 120s， 默认 ThreadExecutionRole，拒绝策略 = 调用者运行策略。

### 方法

#### `builder(String poolName) → ThreadPoolBuilder`
- **说明：** 创建指定池名称的构建器。
- **参数：**
  - `poolName` — 线程池名称
- **返回：** 构建器实例

#### `build(int coreSize, int maxSize, int queueCapacity, String poolName) → NexusThreadPoolExecutor`
- **说明：** 一步构建线程池的便捷方法。
- **参数：**
  - `coreSize` — 核心线程数
  - `maxSize` — 最大线程数
  - `queueCapacity` — 队列容量
  - `poolName` — 线程池名称
- **返回：** 构建完成的线程池

#### `coreSize(int coreSize) → ThreadPoolBuilder`
- **说明：** 设置核心线程数（最小为 1）。
- **参数：**
  - `coreSize` — 核心线程数
- **返回：** 当前构建器

#### `maxSize(int maxSize) → ThreadPoolBuilder`
- **说明：** 设置最大线程数（最小为 1）。
- **参数：**
  - `maxSize` — 最大线程数
- **返回：** 当前构建器

#### `queueCapacity(int queueCapacity) → ThreadPoolBuilder`
- **说明：** 设置工作队列容量。零或负数时使用 SynchronousQueue。
- **参数：**
  - `queueCapacity` — 队列容量
- **返回：** 当前构建器

#### `keepAliveSeconds(int keepAliveSeconds) → ThreadPoolBuilder`
- **说明：** 设置空闲线程的存活时间（秒）。
- **参数：**
  - `keepAliveSeconds` — 存活时间（秒）
- **返回：** 当前构建器

#### `background() → ThreadPoolBuilder`
- **说明：** 使用守护后台线程（定时、清理等不阻塞 JVM 退出的任务）。
- **返回：** 当前构建器

#### `worker() → ThreadPoolBuilder`
- **说明：** 使用非守护业务工作线程（默认）。
- **返回：** 当前构建器

#### `role(ThreadExecutionRole role) → ThreadPoolBuilder`
- **说明：** 设置线程执行角色。
- **参数：**
  - `role` — 角色
- **返回：** 当前构建器

#### `daemon(boolean daemon) → ThreadPoolBuilder`
- **说明：** 设置工作线程是否为守护线程。
- **参数：**
  - `daemon` — 是否为守护线程
- **返回：** 当前构建器

#### `rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) → ThreadPoolBuilder`
- **说明：** 设置任务拒绝处理策略（默认：ThreadPoolExecutor.CallerRunsPolicy）。
- **参数：**
  - `rejectedExecutionHandler` — 拒绝处理策略
- **返回：** 当前构建器

#### `build() → NexusThreadPoolExecutor`
- **说明：** 构建 NexusThreadPoolExecutor。最大线程数会被规范化为至少等于核心线程数。
- **返回：** 构建完成的线程池
