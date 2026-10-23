# 发现与安装

| 包 / 类型 | 说明 |
|-----------|------|
| `discovery.*` | Java SPI + `plugin.yaml` 发现、Contribution 解码、`PluginCatalog` |
| `installation.bootstrap.PluginHostBootstrap` | 宿主启用插件的统一装配入口 |
| `installation.service.*` | 安装对账、唯一运行时创建与启停 |
| `installation.domain.*` | 安装事实、来源/存在性、安全定义快照 |
| `installation.repository.*` / `dao.*` | `nx_plugin_installation` 单表持久化 |
| `config.*` | 插件配置 schema、来源解析与密文 |

包级参考：[discovery.md](discovery.md)、[installation-bootstrap.md](installation-bootstrap.md)、
[installation-service.md](installation-service.md)、[installation-domain-model.md](installation-domain-model.md)、
[installation-repository.md](installation-repository.md)、[config.md](config.md)。
