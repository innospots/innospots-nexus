# innospots-nexus-plugin 文档

本目录为 **`innospots-nexus-plugin`** 模块文档：classpath 插件运行时、`console@1` 贡献处理与 Pactor Page DSL。

## 入口

- 手册索引：[`plugin/manual/README.md`](plugin/manual/README.md)
- 设计文档：[`plugin/design/`](plugin/design/)
- 安装表 DDL：[`plugin-installation-schema.sql`](plugin-installation-schema.sql)

## 模块边界

| 归属 plugin | 归属 console | 归属 core |
|-------------|--------------|-----------|
| 发现、安装、生命周期、Capability | catalog 索引表持久化与同步 REST | 持久化基类、审计填充 |
| `console@1` 声明模型与 Handler | `ConsoleCatalogSyncService`、`ConsoleCatalogResourceEntity` | Quartz、server、watcher |
| Page DSL 解析与校验 | 管理 REST、VO/Converter | `MetaResourceService` |

Java 包名仍为 `com.innospots.nexus.core.plugin.*`（兼容既有 import）；Maven artifact 为
`innospots-nexus-plugin`。

## 验证命令

```bash
mvn -pl innospots-nexus-plugin -am test
```
