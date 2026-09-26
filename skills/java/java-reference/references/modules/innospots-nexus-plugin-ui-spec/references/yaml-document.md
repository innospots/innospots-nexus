# 文档结构与编码

## 规范身份

| 项 | 值 |
|----|-----|
| 名称 | Pactor Page DSL |
| 版本 | **1.0**（YAML 字段 `dsl: '1.0'`，与 `PageDsl.SPEC_VERSION` 一致） |
| 媒体类型 | YAML 1.2 文本，UTF-8 |
| 与 plugin.yaml | **独立文档**；不得把 Page DSL 字段写入 `nexus.plugin/v1` 顶层 |

## 编码与解析规则

宿主默认使用 `JacksonPageDslParser` + `PageDslConfig.defaults()`：

| 规则 | 要求 |
|------|------|
| 单文件单文档 | 一个 `.yaml` 文件对应一个 `PageDsl` |
| 顶层 | 必须是 mapping（对象） |
| 未知字段 | 默认 **拒绝**（`failOnUnknownProperties: true`） |
| `dsl` | 必填，且必须为字符串 `'1.0'` |
| `page.id` | 必填，非空；建议 kebab-case |
| `meta` | 可选；仅供工具，运行时不得依赖 |
| `body` vs `children` | 完整页面优先 `body`；片段可用根级 `children` |

校验在解析后由 `PageDslValidator` 执行，失败抛出 `NexusException`（`CONFIG_ERROR`）。

## 顶层字段

| 字段 | 必填 | 说明 |
|------|------|------|
| `dsl` | 是 | 固定 `'1.0'` |
| `page` | 是 | 见 [`permissions.md`](permissions.md) 中 `PageMeta` |
| `requires` | 否 | `runtime` 版本范围、`components` 组件版本表 |
| `meta` | 否 | 作者、标签、文档版本等自由键值 |
| `state` | 否 | 页面初始状态；运行时通过动作 `setState` 等更新 |
| `dataSources` | 否 | 命名数据源 map |
| `actions` | 否 | 命名动作序列 map |
| `components` | 否 | 命名可复用 UI 片段 map |
| `lifecycle` | 否 | 页面级钩子 |
| `body` | 推荐 | 页面 UI 根（`DslNode`） |
| `children` | 否 | 无 `body` 时的替代根 |

## `page` 对象

| 字段 | 必填 | 说明 |
|------|------|------|
| `id` | 是 | 页面唯一键，与 `pageKey` / 资源文件名一致 |
| `name` | 否 | camelCase 程序化名称 |
| `title` | 否 | 展示标题 |
| `description` | 否 | 描述 |
| `type` | 否 | 模式：`list`、`detail`、`form`、`dashboard`、`general` 等 |
| `permission` | 否 | 页面级权限，见 [`permissions.md`](permissions.md) |

## `requires` 对象

```yaml
requires:
  runtime: '>=0.1.0'
  components:
    Page: '>=1.0'
    Table: '>=1.0'
```

- `runtime`：控制台 Pactor 运行时语义版本范围（字符串，由宿主解释）。
- `components`：组件注册表类型名 → 版本范围。

## 最小合法示例

```yaml
dsl: '1.0'

page:
  id: order-list
  title: Orders
  type: list

dataSources:
  orders:
    type: http
    request:
      method: GET
      url: /api/orders
```
