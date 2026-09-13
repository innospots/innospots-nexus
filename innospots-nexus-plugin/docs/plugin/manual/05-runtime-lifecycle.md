# 运行时生命周期：初始化、调用、销毁

## 1. 两个层次

| 层次 | 负责类 | 持久化 |
|------|--------|--------|
| 安装意图 | `PluginInstallationManager` | `nx_plugin_installation` |
| JVM 运行态 | `DefaultPluginManager` / `ManagedPlugin` | 仅内存 |

`lastRuntimeState` 只做历史诊断，**不参与**下次是否启动。

## 2. 单插件启动顺序（ManagedPlugin）

```text
STARTING
  → 解析配置
  → ContributionHandler.prepare（每个 contribution）
  → 创建 CapabilityProvider 实例
  → plugin.initialize(context)
  → provider.initialize(context)（每个）
  → plugin.start()
  → contribution.stage()
  → registry.registerAll()        # 写入注册表
  → contribution.commit()
  → availability.activate()       # 对外可见
ACTIVE
```

Capability 查询在 `activate()` 之前返回空（经 `PluginAvailabilityIndex` 门控）。

## 3. 宿主如何「调用」插件

Core **不**提供通用「调用插件」RPC。集成方式：

### 3.1 通过 Capability（推荐）

业务代码注入 `CapabilityManager`（来自 `PluginManager.capabilities()`）：

```java
MessageSender sender = capabilities.require(
        MESSAGE_SENDER,
        Tags.of("channel", "wecom"));
sender.send("user-1", "hello");
```

### 3.2 通过 Plugin 级钩子

在 `Plugin.start()` / `stop()` 中启动后台任务或注册资源到 `ResourceScope`。

### 3.3 通过管理 API

Console 暴露 `PluginManagementEndpoint`（依赖注入 `PluginInstallationManager`）：

| 操作 | 方法 |
|------|------|
| 列表 | `plugins()` |
| 安装并启动 | `installAndStart(pluginId)` |
| 启用 | `enable(pluginId)` |
| 停用 | `disable(pluginId)` |
| 失败重试 | `retryStart(pluginId)` |

## 4. 停止与销毁

```text
ACTIVE → STOPPING
  → availability.deactivate()
  → registry.unregisterPlugin(pluginId)
  → contribution.rollback() / close()（逆序）
  → provider.destroy()（逆序）
  → plugin.stop()
  → ResourceScope.close()
→ STOPPED 或 FAILED
```

`DefaultPluginManager.close()` 按启动逆序停止全部插件，**不**做「最后一个 required Provider」保护（与单插件 `stop(id)` 不同）。

## 5. 状态对照

| PluginState | 含义 |
|-------------|------|
| `DESCRIBED` | 已发现、未启动 |
| `WAITING` | 等待依赖 Capability |
| `STARTING` / `STOPPING` | 事务进行中 |
| `ACTIVE` | 可对外提供 Capability |
| `STOPPED` | 已停止 |
| `FAILED` | 启动或停止失败，可 `retryStart`（需安装意图仍启用） |

安装侧的 `REGISTERED` / `DISABLED` / `MISSING` 由 `PluginInstallation` 字段组合派生，不在 `PluginState` 枚举中。

## 6. 插件内访问运行时服务

`PluginContext` / `CapabilityProviderContext` 提供：

| 方法 | 用途 |
|------|------|
| `definition()` | 只读定义 |
| `config()` | 插件级已解析配置 |
| `providerConfig()` | Provider 私有配置 |
| `capabilities()` | 查询其他 ACTIVE Provider |
| `events()` | 插件作用域事件总线 |
| `resources()` | 注册需在停止时关闭的资源 |
| `logger()` | `System.Logger` |

## 7. 失败隔离

- 非 `requiredPluginIds` 的插件启动失败：其他插件继续；状态 `FAILED`。
- `requiredPluginIds` 未全部 `ACTIVE`：`DefaultPluginManager.start()` 回滚本次已启动集合并抛错。
