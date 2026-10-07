# 动作、生命周期与事件

## 动作条目（`ActionConfig`）

YAML 中单步动作：

```yaml
- action: reload
  params:
    dataSource: customers
```

| 字段 | 说明 |
|------|------|
| `action` | 必填；宿主动作注册表名称 |
| `id` | 可选；同作用域内唯一，供 `${actions.<id>}` 引用 |
| `params` | 动作参数 map |
| `condition` | 布尔或表达式；为 false 时跳过 |
| `permission` | 动作级权限 |

### 命名动作（`actions` map）

```yaml
actions:
  search:
    - action: reload
      params:
        dataSource: customers
  deleteRow:
    - id: confirmDelete
      action: confirm
      permission: customer:delete
      params:
        title: 确认删除
        content: "确定删除客户吗？"
    - action: if
      params:
        condition: ${actions.confirmDelete}
        then:
          - action: message
            params:
              type: success
              text: 删除成功
```

- 值可以是**单个对象**或**数组**（`ActionOrList`）。
- `call` 动作：`params.name` 指向 `actions` 中的另一条目。
- `if` 动作：`params.then` / `params.else` 为嵌套动作列表。

### 常见动作名（来自测试与示例）

| action | 典型 params | 说明 |
|--------|-------------|------|
| `reload` | `dataSource` | 重新加载命名数据源 |
| `setState` | 状态键值 | 合并写入 `state` |
| `resetState` | — | 重置状态（宿主定义语义） |
| `call` | `name` | 调用 `actions` 中命名序列 |
| `confirm` | `title`, `content` | 用户确认，结果写入 `actions.<id>` |
| `if` | `condition`, `then`, `else` | 条件分支 |
| `message` | `type`, `text` | 提示消息 |

**契约边界：** `PageDslValidator` 只校验结构与数据源引用；**不**证明动作名、服务名在宿主中存在。

## 生命周期（`lifecycle`）

| 钩子 | 说明 |
|------|------|
| `onInit` | 首次初始化 |
| `onLoad` | 数据/页面加载 |
| `onReady` | 就绪 |
| `onShow` / `onHide` | 可见性 |
| `onDestroy` | 销毁 |

每个钩子值为 `ActionOrList`（单动作或数组）。

```yaml
lifecycle:
  onInit:
    - action: setState
      params:
        ready: true
  onLoad: []
```

## 组件事件（`events`）

内联组件或 `components` 片段上：

```yaml
events:
  onSubmit:
    - action: call
      params:
        name: search
  onClick:
    - action: call
      params:
        name: deleteRow
```

事件名由组件注册表定义（如 `onClick`、`onChange`、`onSubmit`）。
