# Java 运行时 API

实现位于 **`innospots-nexus-plugin`**，包根：

`com.innospots.nexus.core.plugin.contribution.console.ui.spec`

## 核心类型

| 类型 | 职责 |
|------|------|
| `PageDsl` | 根文档模型 |
| `PageDslParser` / `JacksonPageDslParser` | YAML 解析 |
| `PageDslValidator` | 结构校验 |
| `PageDslConfig` | classpath 路径与严格性 |
| `ClasspathPageDslLoader` | 默认 classpath `PageDslLoader` 实现 |
| `PageDslLoader` | 加载端口（宿主可替换） |
| `PageDslFilter` / `PageDslFilterChain` | 渲染前变换 |

## 子包

| 包 | 内容 |
|----|------|
| `action` | `ActionConfig`、`ActionOrList` |
| `datasource` | `StaticDataSource`、`ServiceDataSource`、`HttpDataSource`… |
| `node` | `ComponentNode`、`ComponentReferenceNode`、`DslSourceRef`、`Children` |
| `permission` | `PermissionConfig`、`PermissionDenied` |
| `parser` | `JacksonPageDslParser` |
| `loader` | `PageDslLoader` |
| `validation` | `PageDslValidator` |
| `config` | `PageDslConfig` |
| `filter` | 渲染过滤器 |
| `jackson` | 自定义反序列化器 |
| `endpoint` | `DefaultPageDslEndpoint`（DSL 相关 HTTP 辅助，非业务 REST） |

## 机器索引

自动生成的包级 API 列表见：

- [`innospots-nexus-plugin` → `contribution-console-ui-spec-*`](../innospots-nexus-plugin/README.md)
- [`page-dsl-overview.md`](../innospots-nexus-plugin/references/page-dsl-overview.md)

## 与 plugin.yaml 的边界

| 层 | 文档 |
|----|------|
| 插件清单、console@1 模块/菜单树 | `plugin.yaml` / `PluginDefinition` |
| 页面 UI 正文 | 本 Page DSL YAML |

二者通过 `pageKey` 与 `pagePath` 关联，内容不得混用 schema。
