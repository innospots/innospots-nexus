# Layout 族组件规范

`Layout` / `Header` / `Sider` / `Content` / `Footer` 是 **页面区域骨架组件**（spec: layout-components），用于在 DSL 内描述「一块屏幕如何分区」。

权威来源：[Pactor DSL 参考 — Layout 族](https://pactor-docs.xp-java.workers.dev/reference/dsl/#layout-header-sider-content-footer)。

Props 速查 → [component-catalog.md](component-catalog.md#layout-族)。选用规则与 Shell 边界 → [layout.md](layout.md)。

---

## 先分清两件事

```text
┌─────────────────────────────────────────────────────────┐
│  Application Shell（Bootstrap layout.type）                │
│  ┌────────┬──────────────────────────────────────────┐ │
│  │ Shell  │  Shell Header / Tabs / Brand               │ │
│  │ Sider  ├──────────────────────────────────────────┤ │
│  │ Menu   │  Page Area ← PageDsl body 渲染在这里        │ │
│  │        │     （普通 admin 页通常只有这一块）           │ │
│  └────────┴──────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  PageDsl Layout 族（body 内的区域组件）                   │
│  只在 blank 外壳、嵌入页、演示页等「页面自控全屏结构」时用   │
└─────────────────────────────────────────────────────────┘
```

| 概念 | 谁管 | 在 YAML 里？ |
|------|------|-------------|
| **Application Shell** | Bootstrap `layout.type`（如 `admin`） | **否** — 菜单、品牌、页签由宿主提供 |
| **Layout 族** | `body` 内的 `type: Layout` 等 | **是** — 页面自己分区 |

**硬规则**：`layout.type: admin`（默认管理台外壳）时，**不要在 `body` 里再套 `Layout + Sider` 模拟应用菜单** — 会出现双侧边栏，属于设计错误。

**何时用 Layout 族**：`layout.type: blank`（或无外壳的嵌入/演示场景），页面需要**完全自控** Header / 侧栏 / 主区 / 底栏。

---

## 六个组件分别是什么

它们不是业务组件，而是 **带默认样式的语义分区容器**（类似 HTML5 的 `<header>` / `<aside>` / `<main>` / `<footer>`）。

| 组件 | 语义 | 默认表现（实现层） | 典型内容 |
|------|------|-------------------|----------|
| `Layout` | **区域编排器** | Flex 容器；根据子节点自动选横/竖主轴 | 只放 `Header` / `Sider` / `Content` / `Footer` 等分区子节点 |
| `Header` | **顶栏区** | 横向 flex、垂直居中、下边框 | Logo、页内标题、顶栏 `Menu`（horizontal）、全局操作按钮 |
| `Sider` | **侧边区** | 固定/可折叠侧栏 | `Menu`（inline）、树、过滤器面板 |
| `Content` | **主内容区** | `flex: 1`，占据剩余空间 | `Page`、Table、Form、业务主 UI |
| `Footer` | **底栏区** | 居中、弱化文本 | 版权、版本号、辅助链接 |
| `Menu` | **导航菜单**（常放在 Header/Sider 内） | 哑渲染列表 | `${data.menus}` 或静态 `items` |

### 容易混淆的概念

| 混淆点 | 正确理解 |
|--------|----------|
| `Sider` vs Shell 侧边栏 | Shell Sider 是应用级菜单；DSL `Sider` 是**本页**侧栏（工作区导航、分类树等） |
| `Header` vs `Page` 标题 | `page.title` / Shell 页签用元信息；`Header` 是**页内顶栏区域**；`Page.props.title` 是内容区标题 — 三者可并存，不要重复堆相同文案 |
| `Layout` vs `Page` | `Page` = 业务内容纵向节奏容器（列表/表单页默认根）；`Layout` = 横竖分区骨架（全屏结构） |
| `Content` vs 普通 `div` | `Content` 语义是「主内容」，且默认 `flex: 1` 吃满剩余高度/宽度 |

---

## Layout：编排规则

`Layout` 本身是 **Flex 容器**，只负责把子分区按主轴排列。

### 主轴自动推导

```text
直接子节点中含 Sider？
    ├─ 是 → 默认 direction: horizontal（Sider | 其余）
    └─ 否 → 默认 direction: vertical（Header → Content → Footer 自上而下）
```

- 可用 `props.direction` **显式覆盖**自动推导。
- 经 `component:` 引用的子树里若含 `Sider`，**同样计入**「含 Sider」判断。

### 合法子节点

`Layout` 的 `children` 应放 **分区组件**，不要直接塞业务表单/表格：

```text
推荐：
  Layout → Sider + Layout(Header + Content + Footer)

不推荐：
  Layout → Table + Form   （应放在 Content 或 Page 内）
```

---

## 标准组合模式

### 模式 A：侧栏 + 主区（最常见）

```text
Layout (horizontal)
├── Sider          ← 左：导航 / 树
└── Layout (vertical，自动推导)
    ├── Header     ← 顶栏（可选）
    ├── Content    ← 主内容（必填语义位）
    └── Footer     ← 底栏（可选）
```

```yaml
body:
  type: Layout
  children:
    - type: Sider
      props:
        width: 240
        collapsible: true
        collapsed: ${state.siderCollapsed}
      events:
        onCollapse:
          - action: setState
            params: { siderCollapsed: ${event} }
      children:
        - type: Menu
          props:
            mode: inline
            items: ${data.menus}
            selectedKey: ${state.currentMenu}
          events:
            onSelect:
              - action: setState
                params: { currentMenu: ${event} }
    - type: Layout
      children:
        - type: Header
          children:
            - type: Flex
              props:
                justify: space-between
                align: center
              children:
                - type: Text
                  props: { text: 工作区, strong: true }
                - type: Button
                  props: { text: 设置, variant: link }
        - type: Content
          children:
            - type: Page
              props:
                title: 主内容
              children:
                - type: Table
                  props:
                    dataSource: ${data.list}
                    columns: [...]
        - type: Footer
          children:
            - type: Text
              props: { text: © 2026 Demo, type: secondary }
```

### 模式 B：仅顶栏 + 内容（无侧栏）

```text
Layout (vertical)
├── Header
└── Content
```

```yaml
body:
  type: Layout
  children:
    - type: Header
      children:
        - type: Menu
          props:
            mode: horizontal
            items: ${data.topNav}
    - type: Content
      children:
        - type: Page
          children: [...]
```

### 模式 C：admin 内页（不用 Layout 族）

绝大多数控制台列表/表单/详情页：

```yaml
body:
  type: Page          # ← 根节点用 Page，不是 Layout
  children:
    - type: Card      # 搜索区
      children: [...]
    - type: Table      # 主表
      props: [...]
```

Shell 已提供外侧 `Sider + Header`，页面只负责 **Page Area 内的业务结构**。

---

## 各组件详细说明

### Header

| 项 | 说明 |
|----|------|
| 作用 | 页面**顶部横条**区域 |
| Props | **无特有 props**（仅用通用 `className` / `style`） |
| 放什么 | 横向 `Flex`、horizontal `Menu`、面包屑（Text/Link）、操作按钮 |
| 不放什么 | 整页主表格、长表单（应放 `Content`） |

### Sider

| prop | 说明 |
|------|------|
| `width` | 展开宽度，默认 `200` |
| `collapsible` | `true` 时渲染底部折叠按钮 |
| `collapsed` | **受控**折叠态，绑 `${state.xxx}` |
| `collapsedWidth` | 折叠后宽度，默认 `64` |
| `onCollapse` | 点击折叠按钮传出 `boolean`，典型接 `setState` |

**受控闭环**（必须成对）：

```yaml
state:
  siderCollapsed: false

- type: Sider
  props:
    collapsible: true
    collapsed: ${state.siderCollapsed}    # state → UI
  events:
    onCollapse:                            # UI → state
      - action: setState
        params: { siderCollapsed: ${event} }
```

缺 `collapsed` / `onCollapse` 时，折叠交互不完整或不可控。

### Content

| 项 | 说明 |
|----|------|
| 作用 | **主内容区**，占据 Layout 剩余空间 |
| Props | **无特有 props** |
| 默认样式 | `flex: 1`（在父 Layout 的 flex 布局中伸展） |
| 放什么 | `Page`、业务 Table/Form、工作区主面板 |
| 常见模式 | `Content` → `Page` → 业务组件（保留 Page 纵向节奏） |

### Footer

| 项 | 说明 |
|----|------|
| 作用 | 页面**底部**弱信息区 |
| Props | **无特有 props** |
| 放什么 | 版权、版本、辅助链接（`Text` / `Link`） |
| 可选性 | 多数 admin 内页**不需要** Footer |

### Menu（常配合 Header / Sider）

| prop | 说明 |
|------|------|
| `items` | `{ key, label, icon?, children? }[]`，可 `${data.menus}` |
| `mode` | `inline`（放 Sider）\| `horizontal`（放 Header） |
| `selectedKey` | 受控选中项 |
| `onSelect` | 传出 `key`；接 `navigate` 或 `setState` |

`Menu` **不读取** Shell 的 MenuRegistry — 数据必须由 DSL（`dataSources` / `state` / 静态 `items`）注入。

---

## 决策表：我该用哪个根节点？

| 场景 | Shell | `body` 根节点 | 是否用 Layout 族 |
|------|-------|--------------|-----------------|
| 普通列表/表单/详情 | `admin` | `Page` | **否** |
| 看板指标卡 | `admin` | `Page` + `Grid` | **否** |
| 主从（列表+详情） | `admin` | `Page` + `Flex`/`Grid` | **否**（侧栏用 Flex 分栏，不是 Sider） |
| 全屏工作区/设计器 | `blank` | `Layout` | **是** |
| 嵌入演示/无外壳页 | `blank` 或无 | `Layout` 或根级 `children` | **是** |
| 登录页 | `blank` | `Page` 或 `Flex` 居中 | **通常否** |

---

## 反模式（ui:check 应标警告）

| 反模式 | 问题 | 改法 |
|--------|------|------|
| admin 外壳 + `body: Layout + Sider + Menu` | 与应用菜单重复，双侧边栏 | 去掉 DSL `Sider`，用 Shell 菜单；页内用 `Page` |
| `Layout` 根下直接放 `Table` | 跳过分区语义，`Content` 未承担主区 | 包一层 `Content` 或改用 `Page` 根 |
| `Sider` 无 `collapsed` 闭环 | 折叠态不可控 | 补 `state` + `onCollapse` → `setState` |
| 用 `Layout` 做行内按钮排列 | 语义错误 | 用 `Flex` |
| `Header` 里放整张业务表 | 顶栏高度爆炸 | 表放 `Content` |
| 所有页面都套 `Layout` | 普通内页多余嵌套 | 默认 `Page` 根 |

---

## 与 Flex / Grid / Page 的配合

```text
Layout 族     → 屏幕级分区（哪一块是侧栏、哪一块是主区）
Page          → 主区内的纵向节奏（搜索区、表格、分页的间距）
Flex / Grid   → 主区内的横向/均分列排列
Card          → 业务语义分组
```

嵌套建议：

```text
Layout
└── Content
    └── Page              ← 保留页面节奏
        ├── Card          ← 筛选
        └── Table         ← 主数据
```

不要用 `Layout` 替代 `Flex` 做工具栏右对齐；不要用 `Grid` 替代 `Layout` 做侧栏+主区（Grid 是均分列，侧栏需要固定宽度 → 用 `Sider`）。

---

## 相关文档

| 文档 | 内容 |
|------|------|
| [layout.md](layout.md) | 三层布局模型、六条选用规则 |
| [component-catalog.md](component-catalog.md) | 各组件 props 表 |
| [patterns/workspace-page.md](../patterns/workspace-page.md) | blank + Layout 工作区模式 |
| [patterns/master-detail.md](../patterns/master-detail.md) | admin 内主从（不用 Sider） |
