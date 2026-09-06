# 节点模型（DslNode / DslRenderable）

## 判别顺序（反序列化）

```text
1. 含 source     → DslSourceRef（动态 DSL）
2. 含 component  → ComponentReferenceNode
3. 否则          → ComponentNode（必须有 type）
```

同一节点不要混用 `source` / `component` / `type`。

## ComponentNode（内联）

```yaml
type: Form
id: searchForm
when: ${state.ready}
permission: customer:edit
props:
  name: search
children:
  - type: Input
    props:
      name: keyword
events:
  onSubmit:
    action: call
    params:
      name: search
```

| 字段 | 说明 |
|------|------|
| `type` | 组件 registry 名，**必填** |
| `id` | 实例 id |
| `props` | 开放对象 |
| `children` | 子节点数组或动态 `source` |
| `events` | 事件 → ActionOrList |
| `permission` | 节点级权限 |
| `when` | 可见条件（布尔或表达式） |

## ComponentReferenceNode（引用）

```yaml
component: searchForm
props:
  compact: true
```

`component` 必须在同文档 `components` 中定义。可覆盖 `props` / `events`；`when` / `permission` 引用处优先。详见 [component.md](component.md)。

各 `type` 的 props 契约 → [component-catalog.md](component-catalog.md)。

## DslSourceRef（动态片段）

```yaml
source:
  type: service
  service: customer.extensionPanel
placeholder:
  type: Spin
```

## Children 三种形态

```yaml
# 数组
children:
  - type: Button
  - component: toolbar

# 单节点（解析为单元素数组）
children:
  type: Alert

# 动态 source
children:
  source:
    type: service
    service: panel.children
```

## 与布局的关系

- 节点树描述**结构**；布局组件（`Page`/`Grid`/`Flex`）是 `type` 的具体取值
- 布局选用规则 → [layout.md](layout.md)
