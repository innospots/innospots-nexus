# Nexus Plugin 手册索引

本目录按「一事一文档」组织，内容以当前 `innospots-nexus-core` 实现为准。设计细节见 [`../design/`](../design/)。

| 文档 | 说明 |
|------|------|
| [01-discovery-and-bootstrap.md](01-discovery-and-bootstrap.md) | 系统启动后如何发现插件、涉及哪些类、初始化序列 |
| [02-development-workflow.md](02-development-workflow.md) | 二次开发总流程：定义 → 实现 → 配置 → 装配 |
| [03-java-plugin.md](03-java-plugin.md) | Java SPI 插件写法与声明 API |
| [04-yaml-plugin.md](04-yaml-plugin.md) | YAML 插件文件格式与字段说明 |
| [05-runtime-lifecycle.md](05-runtime-lifecycle.md) | 注册后的初始化、调用、停止与销毁 |
| [06-capability-usage.md](06-capability-usage.md) | Capability 声明、路由与宿主/插件内调用 |
| [07-exposure-and-contribution.md](07-exposure-and-contribution.md) | Exposure（V1 未实现）与 `console@1` Contribution |
| [08-configuration.md](08-configuration.md) | 插件配置：schema、来源、优先级、文件/Nacos/环境变量如何接入 |
| [09-host-assembly.md](09-host-assembly.md) | 应用宿主如何装配安装管理与运行时 |
| [10-host-extension-guide.md](10-host-extension-guide.md) | 宿主各组件扩展点与实现方式（DAO、注册表、运行时配置） |
| [11-dynamic-plugin-configuration.md](11-dynamic-plugin-configuration.md) | 运行期动态配置：`ConfigSource`、数据库 appKey 实践 |
| [12-quarkus-host-extension-guide.md](12-quarkus-host-extension-guide.md) | Quarkus 宿主扩展：CDI、Config、启动/关闭、REST 装配 |

**V1 能力边界（实现现状）**

- 支持：Java SPI、`META-INF/nexus/plugin.yaml`、`bind.kind=java`、Tags 路由、Capability 依赖、`console@1`
- 不支持：远程 bind（http/process/mcp）、`exposures`、动态 JAR 安装/卸载、独立 ClassLoader
