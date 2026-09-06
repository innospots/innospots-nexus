# 表达式模型

## 形式

表达式为**字符串**，通常包裹在 `${...}` 中（具体求值由运行时 expression 引擎实现）。

## 常见引用

| 前缀 | 含义 | 示例 |
|------|------|------|
| `state` | 页面状态 | `${state.keyword}` |
| `data` | 数据源结果 | `${data.customers}` |
| `dataStatus` | 数据源 loading/error | `${dataStatus.list.loading}` |
| `actions` | 命名 action 结果 | `${actions.editResult}` |
| `form` | `state.form` 别名 | `${form.keyword}` |
| `row` / `rowIndex` | 表格行上下文 | `${row.id}` |
| `route` | 路由上下文 | `${route.params.id}`、`${route.query.tab}` |
| `params` | 路径参数别名；overlay 内为注入参数 | `${params.id}` |
| `parent` | overlay 内父页作用域快照（只读） | `${parent.state.x}` |
| `page` | 当前页元信息 | `${page.title}` |
| `event` | 事件回调参数 | `${event}` |

## expressionOrBoolean

以下字段接受**布尔字面量**或**表达式字符串**：

- `ActionConfig.condition`
- `ComponentNode.when` / `ComponentReferenceNode.when`

```yaml
when: true
when: ${state.ready}
condition: ${state.keyword}
```

反序列化：`ExpressionOrBooleanDeserializer`。

## HTTP / service params

数据源与动态 source 的 `params`、`request.url` 等字段可嵌入表达式：

```yaml
params:
  customerId: ${state.selectedId}
request:
  url: /api/customers/${state.selectedId}
```

## Schema 注意

Schema 将 `expression` 定义为非空字符串；复杂三元表达式写法以运行时引擎为准。
