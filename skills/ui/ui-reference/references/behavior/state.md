# State（页面状态）

## 作用

`state` 定义页面**初始状态**，运行时通过 `setState` action 浅合并更新。

```yaml
state:
  keyword: ''
  page: 1
  pageSize: 20
  selectedId: null
  form: {}
```

表达式引用：`${state.keyword}`、`${state.selectedId}`

## 设计原则

| 原则 | 说明 |
|------|------|
| 初始值完整 | 列表页含 keyword、page、pageSize；详情页含 selectedId |
| 类型一致 | 后续 `setState` 不随意改变字段类型 |
| 嵌套对象整体替换 | 与 Pactor `setState` 语义一致，非 deep merge |
| 表单对象 | 复杂表单用 `form: {}` 单对象承载 |

## 与 dataSources 协作

```yaml
dataSources:
  customers:
    type: service
    service: customer.list
    params:
      keyword: ${state.keyword}
      page: ${state.page}
```

`state` 变化 → 触发 reload / 自动加载（依 runtime）。

## 与布局无关

状态字段不描述 UI 结构；布局由 [structure/layout.md](../structure/layout.md) 与 UI 树表达。
