# innospots-nexus-plugin-ui-spec — Pactor Page DSL 规范索引

> **规范与契约索引**（非 Maven 模块、非 Cursor 技能），由 `java:reference` 消费。
> 描述 `com.innospots.nexus.core.plugin.contribution.console.ui.spec` 下的 **Pactor Page DSL 1.0** YAML。
> Java API 快照见 [`innospots-nexus-plugin`](../innospots-nexus-plugin/README.md)。

快照版本：0.1.0-SNAPSHOT

## 模块概览

**Pactor Page DSL 1.0** 用于声明控制台插件页面的结构、状态、数据源、动作与组件树。
与插件清单 **`plugin.yaml`**（`apiVersion: nexus.plugin/v1`）分离：后者描述插件身份与 `console@1` 贡献骨架；
**页面正文** 使用本 DSL 的独立 YAML 文件。

**能力一览：**

| 能力 | 说明 |
|------|------|
| **文档根** | 顶层 `dsl: '1.0'`、`page`、`body` / `children` |
| **数据** | `dataSources`：`static` / `service` / `http` / `computed` / `resource` |
| **交互** | `actions` 注册表、`lifecycle` 钩子、组件 `events` |
| **UI 树** | `type` 内联组件、`component` 引用、`source` 动态片段 |
| **权限** | 页面 / 动作 / 组件级 `permission` |
| **表达式** | `${state.*}`、`${data.*}`、`${dataStatus.*}`、`${actions.*}` |
| **运行时** | `JacksonPageDslParser` + `PageDslValidator`（严格未知字段） |

**不包含：** 前端组件实现、HTTP 端点定义、catalog 表持久化、`plugin.yaml` 的 Capability/Bind 语法。

## 与 plugin / console 的关系

```text
META-INF/nexus/plugin.yaml          → PluginDefinition + console@1 模块/页面键
src/main/resources/ui-pages/        → Page DSL YAML（本规范）
         │
         ▼
JacksonPageDslParser → PageDsl → PageDslValidator
         │
         ▼
Console 目录同步 / 运行时加载（PageDslLoader）
```

| 文档 | 规范 |
|------|------|
| 插件清单、console@1 树 | [`plugin-dsl-spec.md`](../../../../../innospots-nexus-plugin/docs/plugin/design/plugin-dsl-spec.md) |
| Console 贡献设计 | `innospots-nexus-plugin/docs/plugin/design/plugin-console-contribution-design.md` |
| **页面 YAML（本文）** | 本目录 `references/` |

`console@1` 中 `UiSpecPageDeclaration.pagePath` 指向 classpath 上的页面资源；默认布局见
[`workflow-and-layout.md`](references/workflow-and-layout.md)。

## 规范章节（契约速查）

| 章节 | 说明 |
|------|------|
| 文档结构与编码 | 必填字段、版本、严格解析 |
| 数据源 | 五种 `type` 与校验规则 |
| 动作与生命周期 | `actions`、`lifecycle`、`events` |
| 组件与布局 | `body`、`components`、`when`、动态 `source` |
| 表达式与状态 | `state` 初始值与运行时绑定 |
| 权限 | 字符串 / 数组 / 对象三种形态 |
| 使用方式 | 文件路径、解析、校验、与宿主集成 |

## 类参考（Java 绑定模型）

实现包：`com.innospots.nexus.core.plugin.contribution.console.ui.spec`（artifact：`innospots-nexus-plugin`）。

| 类 | 类型 | 说明 |
|------|------|------|
| `PageDsl` | `class` | YAML 根文档；`SPEC_VERSION = "1.0"` |
| `PageMeta` | `class` | `page.id`（kebab-case）等元数据 |
| `RequiresConfig` | `class` | 运行时与组件版本要求 |
| `LifecycleConfig` | `class` | `onInit` … `onDestroy` 钩子 |
| `DataSourceConfig` | `interface` | 数据源 sealed 层次 |
| `ActionConfig` | `class` | 单步动作调用 |
| `ComponentNode` | `class` | 内联 `type` 组件 |
| `ComponentReferenceNode` | `class` | `component: name` 引用 |
| `PageDslValidator` | `class` | 结构校验（不校验宿主是否实现动作/服务） |
| `JacksonPageDslParser` | `class` | YAML → `PageDsl` |
| `PageDslConfig` | `record` | 默认 `ui-pages/**/*.yaml`，未知字段失败 |
| `PageDslLoader` | `interface` | 按 `moduleKey` + `pageKey` 加载 |

更多类型见 [`java-runtime.md`](references/java-runtime.md) 与
[`innospots-nexus-plugin` 包级索引](../innospots-nexus-plugin/README.md) 中 `contribution-console-ui-spec-*`。

## 包参考

| 主题 | 参考 |
|------|------|
| 文档结构、编码、顶层字段 | [`references/yaml-document.md`](references/yaml-document.md) |
| 数据源契约 | [`references/data-sources.md`](references/data-sources.md) |
| 动作、生命周期、事件 | [`references/actions-lifecycle.md`](references/actions-lifecycle.md) |
| 组件树与布局 | [`references/components-layout.md`](references/components-layout.md) |
| 表达式与 `state` | [`references/expressions-state.md`](references/expressions-state.md) |
| 权限声明 | [`references/permissions.md`](references/permissions.md) |
| 文件布局与解析流程 | [`references/workflow-and-layout.md`](references/workflow-and-layout.md) |
| Java 运行时 API | [`references/java-runtime.md`](references/java-runtime.md) |

## 示例

- 完整列表页：[`customer-list.yaml`](../../../../../innospots-nexus-plugin/src/test/resources/ui-pages/demo/customer-list.yaml)
- 最小页：[`order-list.yaml`](../../../../../innospots-nexus-plugin/src/test/resources/ui-pages/sales/order-list.yaml)
- 内置模块页：[`ui-pages/`](../../../../../innospots-nexus-console/src/main/resources/ui-pages/)
