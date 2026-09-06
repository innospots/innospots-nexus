# 可复用组件（components）

## 作用

顶层 `components` 存放**命名、可复用**的 UI 子树，body 中用 `component` 字段按 key 引用。防止 DSL 嵌套过深：把深层表单、操作列等抽平到顶层定义。

```yaml
components:
  searchForm:
    type: Form
    props:
      layout: inline
    children:
      - type: Input
        props:
          name: keyword
          label: 关键词
      - type: Button
        props:
          text: 查询
          variant: primary
          submit: true

  rowActions:
    type: Flex
    props:
      gap: 8
    children:
      - type: Button
        props:
          text: 编辑
          size: small
        events:
          onClick:
            action: call
            params:
              name: editRow

body:
  type: Page
  children:
    - component: searchForm
    - type: Table
      props:
        dataSource: ${data.list}
        columns:
          - title: 名称
            dataIndex: name
          - title: 操作
            cell:
              component: rowActions
```

## 语义要点

| 要点 | 说明 |
|------|------|
| 作用域继承 | 以**引用处作用域**渲染（与 `call` 一致）：Table 行模板里引用，`${row.id}` 逐行正确求值 |
| 引用处覆盖 | 引用节点的 `props` / `events` **覆盖**子树根节点同名字段；`when` / `permission` 引用处优先 |
| 未定义 key | 渲染 Unsupported 占位（不抛错） |
| 循环引用 | 直接/间接循环（a → b → a）不渲染并告警 |
| 节点形态 | `component` 与 `type` **二选一**；见 [node.md](node.md) |

### 引用处覆盖示例

```yaml
- component: detailPanel
  props:
    style: { flex: 1 }
```

### 与 actions 的分工

| 复用类型 | 机制 |
|----------|------|
| 结构复用 | `components` + `component:` |
| 行为复用 | 顶层 `actions` + `call` |

## 何时抽取

| 抽取到 `components` | 保持内联 |
|---------------------|----------|
| 同片段出现 ≥2 次 | 仅用一次且很短 |
| >10 行且语义独立 | 纯布局容器（Grid/Flex） |
| 需引用并覆盖 `props` | 单次 Card 包裹 |
| Table `cell` 行操作列 | 单行简单文本 |

**布局容器**（`Grid`/`Flex`/`Page`）通常在 `body` 内联，一般不单独抽 `components`，除非跨页复用。

## 与业务组件类型

各 `type` 的 props / events 契约见 **[component-catalog.md](component-catalog.md)**。

| 类别 | 示例 `type` | 说明 |
|------|-------------|------|
| 布局 | `Page`, `Grid`, `Flex`, `Layout`, `Card` | 选用规则 → [layout.md](layout.md) |
| 表单 | `Form`, `Input`, `Select` | 常抽为 `searchForm` 等 |
| 数据展示 | `Table`, `Statistic`, `Text` | 主内容区 |
| 反馈与操作 | `Alert`, `Button`, `Link` | 操作与状态 |

组件 `type` 是否在 Registry 注册，由**运行时**校验，不在 Page DSL schema 硬性范围。

## 动态组件（DslSourceRef）

命名子树也可改为远端动态加载，不必内联：

```yaml
components:
  detailPanel:
    source:
      type: service
      service: dsl.detailSection
      params:
        id: ${row.id}
    placeholder:
      type: Alert
      props:
        message: 加载中…
        variant: info

body:
  type: Page
  children:
    - component: detailPanel
```

四个支持位置、与 `dataSources` 的分工 → [node.md](node.md) DslSourceRef 节。

**注意**：`params` 按首次解析时作用域求值一次；state 变化不会自动重拉结构。需要联动的数据用 `dataSources` + `reload`。
