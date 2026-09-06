# Action 模型

## ActionConfig 字段

| 字段 | 说明 |
|------|------|
| `id` | 可选；结果暴露为 `${actions.<id>}` |
| `action` | **必填**；action registry 名称 |
| `params` | 开放对象，依 action 类型而定 |
| `condition` | 布尔或表达式，执行守卫 |
| `permission` | 动作级权限 |

## ActionOrList（单对象或数组）

以下位置均使用同一 union：

- `actions.<name>`
- `lifecycle.onInit` 等
- `events.onClick` 等

```yaml
# 单步
search:
  action: reload
  params:
    dataSource: customers

# 多步
resetSearch:
  - action: resetState
  - action: reload
    params:
      dataSource: customers
```

## 常用 action（运行时 Action Registry，非 schema 固定枚举）

### 数据与状态

| action | 典型用途 |
|--------|----------|
| `reload` | 刷新 `params.dataSource` |
| `resetState` | 重置 state |
| `setState` | 浅合并 state |
| `form.setValues` | 表单字段联动写入 |

### 编排

| action | 典型用途 |
|--------|----------|
| `call` | 调用 `actions` 中命名序列（`params.name`） |
| `if` | 条件分支（`params.then` / `else`） |
| `sequence` | 子动作串行，失败中断 |
| `parallel` | 子动作并行 |
| `delay` | 等待毫秒 |

### 交互（应用层注册）

| action | 典型用途 | 专篇 |
|--------|----------|------|
| `message` | 全局提示 | — |
| `confirm` | 确认框；结果 `${actions.<id>}` | — |
| `navigate` | 整页路由跳转 `params.to` | [navigation.md](navigation.md) |
| `overlay.open` | 打开 modal / drawer | [overlay.md](overlay.md) |
| `overlay.close` | 关闭弹层并回传 `payload` | [overlay.md](overlay.md) |

### 网络

| action | 典型用途 |
|--------|----------|
| `request` | HTTP 请求；可选 `target` 写入 state |

## 命名 actions 与 call

```yaml
actions:
  search:
    - action: reload
      params:
        dataSource: customers

body:
  type: Button
  events:
    onClick:
      action: call
      params:
        name: search
```

## 校验规则（PageDslValidator）

- 每步必须有 `action`
- 同作用域 `id` 不重复
- `params.dataSource` 须存在于 `dataSources`
- 嵌套 `params.actions` / `then` / `else` 递归校验

## Java 反序列化

- `ActionOrListDeserializer` — 单对象/数组
- `ActionOrListMapDeserializer` — `actions` map
- `EventMapDeserializer` — 组件 `events`
