# 包 `bootstrap`

## PluginHostStartupTask

**类型：** class

内置启动任务：启用插件子系统。


## PluginStartupAttributes

**类型：** class

插件子系统的启动属性键与辅助方法。

### 方法

#### `installationManager(NexusStartupContext context) → PluginInstallationManager`
- **说明：** 返回由 PluginHostStartupTask 附加的安装管理器。
- **参数：**
  - `context` — 启动上下文
- **返回：** 插件宿主启动完成后的安装管理器
