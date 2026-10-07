# 表达式与 `state`

## 初始状态（`state`）

```yaml
state:
  ready: false
  keyword: ''
  page: 1
  pageSize: 20
  selectedId: null
  form: {}
```

- 纯 JSON/YAML 值；无模板求值发生在**写入 YAML 时**。
- 运行时通过动作（如 `setState`）更新；前端绑定读取最新 `state`。

## 表达式字符串

在 **字符串字段** 中使用 `${...}` 占位，由 **Pactor 运行时**（前端或宿主预处理器）求值：

| 前缀 | 示例 | 含义 |
|------|------|------|
| `state` | `${state.keyword}` | 页面状态 |
| `data` | `${data.customers}` | 数据源结果 |
| `dataStatus` | `${dataStatus.customers.loading}` | 加载/错误元数据 |
| `actions` | `${actions.confirmDelete}` | 带 `id` 的动作结果 |
| `event` | `${event.current}` | 事件回调上下文（如分页） |

### 示例

```yaml
props:
  dataSource: ${data.customers}
  loading: ${dataStatus.customers.loading}

params:
  keyword: ${state.keyword}
  page: ${event.current}
```

## 布尔守卫

- 组件 `when`、动作 `condition` 可为 **布尔字面量** 或 **表达式字符串**。
- Jackson 使用 `ExpressionOrBooleanDeserializer` 绑定。

## 契约说明

- DSL **不**定义表达式语言全集；宿主与前端应保持一致。
- `PageDslValidator` **不**解析或类型检查表达式，仅校验结构与引用（如 `dataSource` 键）。
