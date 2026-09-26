# 包 `installation.repository`

## PluginInstallationRepository

**类型：** class

安装表领域仓储，维护登记、对账、MISSING 和管理员意图。

### 方法

#### `register(PluginDefinitionSnapshot snapshot, boolean autoInstall) → PluginInstallation`
- **说明：** 创建单表仓储。
- **参数：**
  - `dao` — 安装表 DAO
  - `snapshot` — 插件定义快照
  - `autoInstall` — 首次发现时是否自动安装并启用
- **返回：** 登记后的安装事实

#### `setIntent(String pluginId, boolean installed, boolean desiredEnabled) → PluginInstallation`
- **说明：** 设置安装和启用意图。
- **参数：**
  - `pluginId` — 插件标识
  - `installed` — 是否已安装
  - `desiredEnabled` — 是否期望启用
- **返回：** 更新后的安装事实

#### `updateRuntime(String pluginId, String runtimeState, String error) → PluginInstallation`
- **说明：** 单独写入最近一次运行状态和错误诊断。
- **参数：**
  - `pluginId` — 插件标识
  - `runtimeState` — 运行状态名称；可为 null
  - `error` — 最近一次错误摘要；可为 null
- **返回：** 更新后的安装事实

#### `findAll() → List<PluginInstallation>`
- **说明：** 查询全部安装事实。
- **返回：** 不可变安装事实列表

#### `find(String pluginId) → Optional<PluginInstallation>`
- **说明：** 按 pluginId 查询安装事实。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 安装事实；不存在时为空

#### `require(String pluginId) → PluginInstallation`
- **说明：** 要求安装事实存在。
- **参数：**
  - `pluginId` — 插件标识
- **返回：** 安装事实
