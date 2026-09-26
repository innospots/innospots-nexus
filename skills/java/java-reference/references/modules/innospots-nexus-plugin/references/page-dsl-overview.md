# Pactor Page DSL 1.0

位于 `contribution.console.ui.spec` 及子包：

| 子域 | 说明 |
|------|------|
| `parser` / `loader` | `PageDslLoader`、`JacksonPageDslParser` |
| `node` | 组件树、`DslNode`、数据源引用 |
| `datasource` | HTTP / Service 数据源配置 |
| `action` | 动作配置与反序列化 |
| `permission` | 页面权限 DSL |
| `filter` / `validation` | 过滤与校验 |
| `endpoint` | `DefaultPageDslEndpoint`（DSL 绑定辅助） |
| `jackson` | 自定义 Jackson 反序列化 |

**YAML 规范与使用方式（权威）：**
[`innospots-nexus-plugin-ui-spec` 规范索引](../innospots-nexus-plugin-ui-spec/README.md)。

插件清单 DSL（`plugin.yaml`）：`innospots-nexus-plugin/docs/plugin/design/plugin-dsl-spec.md`。

包级 Java API：所有 `contribution-console-ui-spec-*.md` 文件。
