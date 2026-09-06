# 规范硬性规则

Pactor Page DSL 1.0 在解析阶段必须满足的**结构规则**（与 schema 及交叉引用一致）。

## 文档级

| 规则 | 说明 |
|------|------|
| `dsl` | 必填，且必须为 `"1.0"` |
| `page` | 必填 |
| `page.id` | 必填，非空，kebab-case |
| 未知顶层字段 | 不允许（严格模式） |

## dataSources

| type | 必填 |
|------|------|
| 键名 | 非空字符串 |
| 定义 | 非 null |
| `static` | `value` |
| `service` | `service` |
| `http` | `request`，且 `request.url` 非空 |
| `http.request.method` | 若存在，须为 `GET POST PUT PATCH DELETE HEAD OPTIONS` 之一 |

## actions

适用于 `actions.*`、`lifecycle.*`、`events.*` 内的动作：

| 规则 | 说明 |
|------|------|
| 键名 | 非空 |
| `action` | 每步必填 |
| `id` | 同文档作用域内唯一 |
| `params.dataSource` | 引用的名称须在 `dataSources` 中 |
| 嵌套 `params.actions` / `then` / `else` | 递归适用上述规则 |

## components

| 规则 | 说明 |
|------|------|
| 键名与定义 | 非空 |
| 递归 | 子树须满足 renderable 规则 |

## 节点树（renderable）

| 形状 | 规则 |
|------|------|
| `ComponentNode` | `type` 必填；`children` 递归 |
| `ComponentReferenceNode` | `component` 必填，且存在于 `components`；`children` 递归 |
| `DslSourceRef` | `source` 必填；`placeholder` 递归 |
| 互斥 | 同一节点不得同时用 `source`、`component`、`type` 表达不同形状 |

## children

| 形态 | 规则 |
|------|------|
| 数组 | 每项为合法 renderable |
| 动态 `source` | `source` 对象合法 |

## 不在规范硬性范围

以下由**运行时 / 产品**验收，规范检查标为遗留项：

- 组件 `type` 是否注册
- 组件 props 是否与 Registry 契约完全一致（软检查对照 [component-catalog.md](../../ui-reference/references/structure/component-catalog.md)）
- `service` / `resource` 是否存在
- 表达式求值
- 权限码是否配置
- `meta` 内容是否有意义
