# 组件树与布局

## 根节点

| 方式 | 用途 |
|------|------|
| `body` | 完整页面的首选根 |
| `children` | 无 `body` 时的片段根（数组或单节点） |

## 节点形态（互斥）

### 1. 内联组件（`type`）

```yaml
body:
  type: Page
  props:
    title: 客户管理
  children:
    - type: Table
      props:
        rowKey: id
        dataSource: ${data.customers}
      when: ${state.ready}
```

| 字段 | 说明 |
|------|------|
| `type` | 必填；组件注册表类型名（`Page`、`Table`、`Form`、`Button`…） |
| `id` | 可选 DOM/逻辑 id |
| `props` | 开放 map；由组件 schema 校验（DSL 层不校验 props 形状） |
| `children` | 子节点或子节点数组 |
| `events` | 事件名 → `ActionOrList` |
| `permission` | 组件级权限 |
| `when` | 布尔或表达式；控制可见/挂载 |

### 2. 组件引用（`component`）

```yaml
- component: searchForm
```

- 引用 `components` map 中的命名片段。
- 校验器要求引用名必须存在。
- 引用节点可带 `children` 覆盖/扩展（见 `ComponentReferenceNode`）。

### 3. 动态片段（`source`）

```yaml
extensionPanel:
  source:
    type: service
    service: customer.extensionPanel
    params:
      customerId: ${state.selectedId}
  placeholder:
    type: Alert
    props:
      message: 扩展信息加载中
      variant: info
```

- `source`：远端或服务能力拉取子 DSL（`DslSourceRef`）。
- `placeholder`：加载中的占位 UI。

## `components` 注册表

可复用片段，键名供 `component:` 引用：

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
```

## `children` 反序列化

- 可为 YAML 数组（多子节点）。
- 可为单个节点对象。
- 校验时对数组逐项校验；对 `source` 引用校验 placeholder。

## 与 `requires.components` 的关系

`requires.components` 声明页面依赖的组件类型及最低版本；`type` 字段必须使用已声明的组件名。
