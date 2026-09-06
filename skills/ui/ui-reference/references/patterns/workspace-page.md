# 工作区页模式（Workspace Page）

多面板、可切换工作区（如 IDE 式、多 Tab 内容区）。`page.type` 可用 `dashboard` 或自定义。

## 结构模型

Layout 族语义见 [layout-components.md](../structure/layout-components.md)。

```text
Layout（blank 外壳根）
├── Sider（页内导航/树 — 不是 Shell 菜单）
└── Layout
    ├── Header（可选：工作区标题 + 操作）
    └── Content
        └── Page → 业务主区 / Tab 内容
```

## 适用场景

- 插件管理、流程设计器、多文档编辑
- 需要页内**二级导航**但 Application Shell 已提供一级菜单

## 约束

| 规则 | 说明 |
|------|------|
| Admin 默认外壳 | **不要**再套完整 `Layout + Sider` 模拟应用菜单 |
| `blank` 外壳 | 可用 `Layout` + `Sider` + `Content` 自建工作区 |
| 页内 Tab | 用运行时 Tab 组件（非 DSL 顶层字段） |

## 骨架示例（blank + Layout）

```yaml
body:
  type: Layout
  children:
    - type: Sider
      props:
        width: 240
      children:
        - component: workspaceNav
    - type: Content
      children:
        - type: Page
          props:
            title: 工作区
          children:
            - component: workspaceMain
```

## 与 console `*-main` 页区别

控制台 `menu-main` 等多为 **general** 占位页；复杂工作区按本模式在业务模块单独设计。

## 设计顺序

```text
1. 确认 Shell 类型（admin vs blank）
2. 是否需要页内 Sider（多数情况不需要）
3. 主内容用 Page + components 组合
4. state 管理工作区选中项
```
