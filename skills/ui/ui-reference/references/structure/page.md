# 页面文档结构（PageDsl 顶层）

## 必填

| 字段 | 类型 | 说明 |
|------|------|------|
| `dsl` | `"1.0"` | 规范版本 |
| `page` | object | 至少含 `id` |
| `page.id` | string | kebab-case，与文件名一致 |

## 可选顶层字段

| 字段 | 说明 |
|------|------|
| `requires` | runtime / components 版本要求 |
| `meta` | 工具元数据；**运行时不得依赖** |
| `state` | 初始状态 → [behavior/state.md](../behavior/state.md) |
| `dataSources` | 命名数据源 → [behavior/datasource.md](../behavior/datasource.md) |
| `actions` | 命名动作 → [behavior/action.md](../behavior/action.md) |
| `components` | 可复用 UI 片段 → [component.md](component.md) |
| `lifecycle` | 生命周期 → [behavior/lifecycle.md](../behavior/lifecycle.md) |
| `body` | 完整页面 UI 根（**推荐**） |
| `children` | 局部 fragment 根 |

## `page` 元数据

| 字段 | 约束 |
|------|------|
| `id` | 必填，kebab-case |
| `name` | 可选，camelCase |
| `title` | 显示标题 |
| `description` | 描述 |
| `type` | `list` / `detail` / `form` / `dashboard` / `general` 等 |
| `permission` | 页面级权限 |

`page.type` 是**语义标签**，用于选型 [patterns/](../patterns/)，不改变解析规则。

## `body` vs `children`

| 模式 | 使用 |
|------|------|
| 完整页面 | `body` + 通常含 dataSources/actions |
| 片段 | 根级 `children`（仍须 `page.id`） |

## UI 树入口

```yaml
body:
  type: Page
  props:
    title: 客户管理
  children:
    - ...
```

- `body` 解决「树从哪开始」
- **布局怎么设计** → [layout.md](layout.md)
- **常见页面怎么搭** → [patterns/](../patterns/)

## 严格解析

`PageDslConfig.failOnUnknownProperties = true`：未知字段导致解析失败。

文件路径 → [naming.md](../naming.md)
