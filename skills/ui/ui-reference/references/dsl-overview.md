# Pactor Page DSL 1.0 概览

## 目标

Pactor Page DSL 用声明式 YAML 描述管理台页面：

| 域 | 配置 | 参考 |
|----|------|------|
| 结构 | `body` / `children`、节点树、**布局**、**组件 props** | [structure/](structure/) |
| 数据 | `state`、`dataSources` | [behavior/state.md](behavior/state.md)、[behavior/datasource.md](behavior/datasource.md) |
| 行为 | `actions`、`events`、`lifecycle` | [behavior/](behavior/) |
| 弹层 | `overlay.open` / `overlay.close` | [behavior/overlay.md](behavior/overlay.md) |
| 导航 | `navigate`、菜单 pageId、路由作用域 | [behavior/navigation.md](behavior/navigation.md) |
| 安全 | `permission` | [security/permission.md](security/permission.md) |
| 模式 | 列表/表单/看板等页面组合 | [patterns/](patterns/) |

## 两个概念必须分开

```text
PageDsl 文档结构（顶层字段、节点形状）
        ≠
页面布局设计（Page / Grid / Flex / Layout 如何组合）
```

- **文档结构** → `structure/page.md`、`structure/node.md`
- **组件契约** → `structure/component-catalog.md`（内置 props / events）
- **布局设计** → `structure/layout.md`（布局能力与选择规则）
- **Layout 族** → `structure/layout-components.md`（Header/Sider/Content 职责与组合）
- **页面模式** → `patterns/*.md`（常见页面如何组合布局）

## 规范边界

### 本规范定义

- 顶层文档结构与字段类型
- 节点形状（内联 / 引用 / 动态 source）
- **布局组件职责与选用规则**（Pactor 1.0 已有组件）
- 数据源、action、permission、表达式字段形状

### 本规范不定义（运行时 / 产品）

- 组件 registry 中 `type` 是否合法
- `service` / `resource` 是否已注册
- 表达式求值语义
- 权限码是否在 IAM 存在
- Application Shell（菜单、品牌、页签）——由 Bootstrap `layout.type` 管理

## 设计层次（生成页面时自上而下）

```text
Application Shell          ← 不属于 PageDsl（Bootstrap）
        │
        ▼
┌─────────────────────────────┐
│ Page（body 根，业务页面）    │
│  ┌───────────────────────┐  │
│  │ Page Pattern          │  │  ← patterns/*.md
│  │  Layout / 区域        │  │  ← structure/layout.md
│  │   Grid / Flex         │  │
│  │    Card / Form / Table│  │
│  └───────────────────────┘  │
│  state / dataSources / actions│  ← behavior/*.md
└─────────────────────────────┘
```

## Canonical 源

```text
skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml
```

说明：[schema.md](schema.md)

实现索引：[implementation-map.md](implementation-map.md)
