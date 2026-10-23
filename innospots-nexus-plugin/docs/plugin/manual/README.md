# Nexus Plugin 手册索引

本目录以当前 `innospots-nexus-plugin` 实现为准。插件只通过不可变
`PluginDefinition` 声明配置 schema 与 `console@2` Contribution，不再提供
Capability、Provider 路由或跨插件依赖。

| 文档 | 说明 |
|------|------|
| [01-discovery-and-bootstrap.md](01-discovery-and-bootstrap.md) | classpath 发现、安装对账、运行时创建与启动序列 |
| [02-development-workflow.md](02-development-workflow.md) | 插件开发、配置声明、Entry 声明与验收流程 |
| [03-java-plugin.md](03-java-plugin.md) | Java SPI 插件、`ModuleDescriptors` 与 `EntryPluginDescriptor` |
| [04-yaml-plugin.md](04-yaml-plugin.md) | YAML 文件格式、配置 schema 与 `console@2` 声明 |
| [05-runtime-lifecycle.md](05-runtime-lifecycle.md) | 初始化、Contribution 事务、停止与失败隔离 |
| [07-exposure-and-contribution.md](07-exposure-and-contribution.md) | Exposure 边界与 Contribution 契约 |
| [08-configuration.md](08-configuration.md) | 配置 schema、来源、优先级与读取方式 |
| [09-host-assembly.md](09-host-assembly.md) | 宿主启动装配与必需注册表 |
| [10-host-extension-guide.md](10-host-extension-guide.md) | Spring 宿主扩展与自定义组件接入 |
| [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md) | `ConfigSource` 与数据库配置实践 |
| [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md) | Quarkus 宿主扩展与自定义组件接入 |

**当前能力边界**

- 支持：Java SPI、`META-INF/nexus/plugin.yaml`、配置 schema、`console@2`
  Contribution、安装意图与运行诊断。
- 不支持：Capability 路由、Provider 注册、跨插件依赖、远程 bind、自动 Exposure、
  运行期热更新。
- 旧页面运行时已移除；`entryPageKey` 仅作为 console catalog 的稳定入口身份保留。
