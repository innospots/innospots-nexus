# Core Host Bootstrap and Startup Initialization Design

## 1. 文档定位

定义 `innospots-nexus-core` 的**框架无关**启动后初始化编排：`NexusStartupTask` 契约、`NexusStartup` 执行入口、宿主适配层。
不包含 console 业务（catalog sync、种子数据等），那些由各模块注册 `NexusStartupTask` 实现。

配套：[2026-09-04-console-catalog-and-navigation-design.md](2026-09-04-console-catalog-and-navigation-design.md)

## 2. 设计原则

1. **core 只编排顺序，不执行业务** — 插件子系统是 core 唯一内置任务；其余由模块注册。
2. **与 Spring/Quarkus 解耦** — core 不依赖 `ApplicationRunner`、`StartupEvent`；适配层在 spring-app / quarkus-app。
3. **宿主只调一个方法** — 框架适配层调用 `NexusStartup.run()`；**不在运行时传入 task 列表**。
4. **配置期组装、运行期执行** — 各模块在 Spring/Quarkus **配置类**中把 Task 注册进 `NexusStartup`；组装完成后一次性执行。
5. **用 `order` 表达先后，不用复杂 phase 机** — V1 不做 7 阶段枚举；用整数排序约定分段。
6. **fail-fast** — 任一步失败则中止；V1 不做 `CONTINUE_ON_ERROR`。
7. **不引入 ServiceLoader** — V1 由宿主配置类显式组装 Task。

## 3. 模块边界

| 模块 | 负责 | 不负责 |
|------|------|--------|
| **core.bootstrap** | `NexusStartupTask` 契约、`NexusStartup`（组装+执行）、内置 `PluginHostStartupTask` | catalog sync、角色种子、JWT 解析 |
| **core.plugin** | `PluginHostBootstrap`、`PluginEventBus`（已有） | 调用 console sync |
| **console** | 注册 `ConsoleCatalogSyncStartupTask`；`PluginManagementEndpoint` 启停后调 sync | 定义启动编排框架 |
| **spring-app / quarkus-app** | 配置类组装 `NexusStartup` Bean；适配层只调 `run()` | 业务初始化逻辑 |
| **kernel / platform** | 按需注册种子 Task（`order` 在 300+） | 插件、权限目录 |

## 4. 核心契约（仅 3 个类型）

### 4.1 `NexusStartupTask`

```java
public interface NexusStartupTask {

    String name();

    /** 越小越先执行。约定分段见 §5。 */
    int order();

    void run(NexusStartupContext context);
}
```

### 4.2 `NexusStartupContext`

轻量上下文，仅传递跨步骤产物；由 `NexusStartup.run()` 内部创建，**不暴露给宿主**：

| attachment | 写入者 | 用途 |
|------------|--------|------|
| `PluginInstallationManager` | `PluginHostStartupTask` | 后续步骤或应用代码查询插件 |

不引入通用 `Map<Class<?>, Object>` 除非 V2 确有需求。

### 4.3 `NexusStartup`（宿主唯一入口）

Task 在 **build 时** 注册；`run()` 无参数：

```java
public final class NexusStartup {

    public static Builder builder() { ... }

    /** 宿主唯一执行入口：排序 → 顺序执行 → fail-fast。 */
    public void run() { ... }

    public static final class Builder {
        public Builder task(NexusStartupTask task) { ... }
        public NexusStartup build() { ... }
    }
}
```

**不单独暴露 `NexusStartupCoordinator`** — 排序与循环逻辑内聚在 `NexusStartup.run()`，避免宿主误用 `coordinator.run(tasks, ctx)` 绕过组装。

### 4.4 内置 Task（core）

| name | order | 行为 |
|------|-------|------|
| `plugin-host` | **100** | 调用现有 `PluginHostBootstrap.enable(...)`，attach `PluginInstallationManager` |

**不内置** `service-ready`、`NexusHostReadyEvent` — 若需要，应用自行注册 `order=900` 的 Task。V1 不新增事件类型。

## 5. order 分段约定（非枚举，文档约定即可）

| order 范围 | 含义 | 示例 |
|------------|------|------|
| 0–99 | 预留（基础设施自检，V1 不用） | — |
| **100** | 插件子系统 | `plugin-host`（core 内置） |
| **200–299** | 依赖 ACTIVE 插件的派生索引 | `console-catalog-sync`（200） |
| **300–499** | 系统/域默认数据种子 | console 300、kernel 310、platform 320 |
| **500+** | 应用扩展 | 客户自定义 Task |
| 900+ | 就绪标记（可选） | 应用自定义 |

console / kernel / platform 各自注册 Task，**不需要**在 core 定义 `SYSTEM_SEED` / `DOMAIN_SEED` 枚举。

## 6. 配置组装与框架适配

### 6.1 配置期：组装 `NexusStartup`（spring-app / quarkus-app）

各模块提供 Task Bean；**一个**配置类用 Builder 组装完整启动管线：

```java
@Bean
NexusStartup nexusStartup(
        PluginHostStartupTask pluginHostTask,
        ConsoleCatalogSyncStartupTask catalogSyncTask) {
    return NexusStartup.builder()
            .task(pluginHostTask)
            .task(catalogSyncTask)
            .build();
}
```

可选模块用 `ObjectProvider` / CDI `Instance` 条件加入，**不在 Runner 里判断**。

> 若宿主希望零 central 列举，可在组装 Bean 中注入 `List<NexusStartupTask>` 并 `forEach(builder::task)` — 这是**配置 Bean 的职责**，不是框架 Runner 的职责。

### 6.2 运行期：框架只调 `run()`

**Spring**

```java
@Bean
ApplicationRunner nexusStartupRunner(NexusStartup nexusStartup) {
    return args -> nexusStartup.run();
}
```

**Quarkus**

```java
void onStart(@Observes StartupEvent event, NexusStartup nexusStartup) {
    nexusStartup.run();
}
```

替代现有 `PluginHostBootstrapRunner`：插件启用逻辑迁入 `PluginHostStartupTask`；Runner 不再接触 `PluginHostBootstrap` 或 Task 列表。

**仅此两处**感知框架；core 零 Spring/Quarkus 依赖。

## 7. 与 catalog sync 的关系

- catalog sync **不是** core 职责。
- console 提供 `ConsoleCatalogSyncStartupTask`（`order=200`），内部调用 `PermissionResourceSyncService.sync()`。
- 运行期插件启停后的 sync **不通过 core 事件链**，见 catalog spec §6.5（`PluginManagementEndpoint` 直接调用 + 启动 Task 兜底）。

core 的 `PluginEventBus` 保留给插件/Capability 场景；**V1 不为 catalog sync 增加监听器**。

## 8. 内置 entry 插件

保证 PLUGIN 后内置插件 ACTIVE 的**唯一推荐方式**：

```text
PluginRuntimeConfig.requiredPluginIds = 6 个内置 entry pluginId
```

**删除** `BuiltinConsolePluginInstallInitializer` 第三种路径，避免重复机制。

## 9. V1 不做的内容

- `NexusStartupPhase` 枚举
- 框架 Runner 注入 `List<NexusStartupTask>` 并传入 `run(tasks)`
- `ServiceLoader` 发现 Task
- `CONTINUE_ON_ERROR` 策略
- `NexusHostReadyEvent` / `service-ready` 内置 Task
- `INFRASTRUCTURE` 阶段
- core 调用 console sync

## 10. 验收标准

1. core 包无 `spring.*`、`jakarta.enterprise.*` import。
2. Spring/Quarkus 适配层代码仅为 `nexusStartup.run()`，无 `List<NexusStartupTask>` 参数。
3. `NexusStartup` 在配置类中组装完毕；`run()` 无入参。
4. `order=100` 插件任务先于 `order=200` catalog sync 执行。
5. 应用新增 Task：实现接口 → 注册 Bean → 在组装 `NexusStartup` 的配置类中加一行 `.task(...)`（或纳入自动收集列表）。
