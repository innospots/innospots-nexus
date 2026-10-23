# 运行时生命周期

## 1. 两个层次

| 层次 | 负责类 | 持久化 |
|------|--------|--------|
| 安装意图 | `PluginInstallationManager` | `nx_plugin_installation` |
| JVM 运行态 | `DefaultPluginManager` / `ManagedPlugin` | 仅内存 |

## 2. 启动顺序

```text
DESCRIBED
  -> STARTING
  -> config-resolve
  -> contribution-prepare
  -> plugin.initialize(context)
  -> plugin.start()
  -> contribution-stage
  -> contribution-commit
  -> availability.activate
ACTIVE
```

`config-resolve` 每个启动周期执行一次，生成不可变 `PluginConfig` 快照。
Contribution 在 `commit` 后才写入活动目录，且仍受插件可用性门控。

## 3. 停止顺序

```text
ACTIVE
  -> STOPPING
  -> availability.deactivate
  -> contribution.rollback / close（逆序）
  -> plugin.stop()
  -> ResourceScope.close()
STOPPED
```

任何清理失败都会继续尝试剩余清理，最终状态为 `FAILED` 并记录 `lastError`。

## 4. 宿主如何调用插件

插件不暴露通用 RPC。宿主通过以下方式集成：

- 在插件 `initialize` / `start` 中注册资源到 `PluginContext.resources()`。
- 订阅或发布 `PluginContext.events()` 中的插件作用域事件。
- 通过 `PluginInstallationManager` 管理启停与读取运行诊断。
- 业务 API 由宿主模块自己的 Jakarta REST Endpoint 实现，不放进插件定义。

管理命令：

| 操作 | 方法 |
|------|------|
| 列表 | `plugins()` |
| 安装并启动 | `installAndStart(pluginId)` |
| 启用 | `enable(pluginId)` |
| 停用 | `disable(pluginId)` |
| 失败重试 | `retryStart(pluginId)` |

## 5. 状态

| PluginState | 含义 |
|-------------|------|
| `DESCRIBED` | 已发现，尚未启动 |
| `STARTING` / `STOPPING` | 生命周期事务进行中 |
| `ACTIVE` | Contribution 已提交且可见 |
| `STOPPED` | 已正常停止 |
| `FAILED` | 启动或停止失败，可按安装意图重试 |

安装侧 `REGISTERED` / `DISABLED` / `MISSING` 由安装记录派生，不属于 `PluginState`。

## 6. PluginContext

| 方法 | 用途 |
|------|------|
| `definition()` | 不可变插件定义 |
| `config()` | 当前启动周期的已校验配置 |
| `events()` | 插件作用域事件总线 |
| `resources()` | 停止时自动关闭的资源作用域 |
| `logger()` | 带插件身份的 `System.Logger` |

## 7. 失败隔离

- 非 required 插件启动失败：其他插件继续，失败插件为 `FAILED`。
- required 插件未 ACTIVE：回滚本次启动集合，`start()` 抛错。
- `close()` 按实际启动顺序逆序停止全部活动插件。
