# 包 `installation.config`

## PluginInstallationConfig

**类型：** record

插件安装策略配置；默认开启首次发现自动安装。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `autoInstall` | `boolean` | 首次发现时是否自动安装 |

### 方法

#### `from(String value) → PluginInstallationConfig`
- **说明：** 系统配置键 {@value}。 public static final String AUTO_INSTALL_KEY = "nexus.plugin.auto-install"; /** 从系统配置读取布尔值，缺省为空时按 true 处理。
- **参数：**
  - `value` — 配置文本；允许 true 或 false
- **返回：** 解析后的安装策略配置
