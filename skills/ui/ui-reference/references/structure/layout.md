# 布局规范（Layout）

定义**布局能力**与**选用规则**。页面具体怎么组合见 [patterns/](../patterns/)。

## 核心原则

**页面布局是 UI 树的一种结构规则，不是 PageDsl 新的顶层配置域。**

不增加顶层 `layout:` 字段；一律通过：

```yaml
body:
  type: Page
  children:
    - ...
```

表达。

---

## 三个层级（必须区分）

### 1. Application Shell Layout（应用外壳）

```text
┌───────────────────────────────┐
│ Header / Tabs / Brand         │
├────────┬──────────────────────┤
│ Sider  │                      │
│ Menu   │    Page Area         │
│        │    （PageDsl body）   │
└────────┴──────────────────────┘
```

| 项 | 说明 |
|----|------|
| 归属 | Bootstrap / App Shell，`layout.type` |
| 与 PageDsl | **不属于**页面 YAML |
| 规则 | Admin 外壳下**不得**在 `body` 内再套完整 `Layout + Sider`（双侧边栏） |
| `blank` 外壳 | 页面完全自控时，在 `body` 内自行组合 `Layout` |

```text
Application Layout  ≠  Page Layout
```

### 2. Page Layout（页面级）

业务页面在 Shell 内的整体结构，**本规范重点**。

推荐逻辑分区（用 `children` 顺序表达，不新增 DSL 字段）：

```text
Page
├── Page Header      title / description / 顶栏 actions（Flex）
├── Filter / Search  Card + Form
├── Summary          Statistic / 指标（可选）
├── Main Content     Table / Form / 主业务区
└── Auxiliary        侧边说明、次要面板（可选）
```

### 3. Content Layout（内容级）

同级元素的排列与对齐。

---

## 布局组件职责

| 组件 | 级别 | 用途 |
|------|------|------|
| `Page` | 页面 | 普通业务页根容器；**纵向节奏**（子块间距） |
| `Layout` | 页面/区域 | `Header` / `Sider` / `Content` / `Footer` 区域骨架 → 详见 [layout-components.md](layout-components.md) |
| `Header` | 区域 | 页内顶栏（非 Shell Header） |
| `Sider` | 区域 | 页内侧栏（非 Shell 菜单）；受控折叠 |
| `Content` | 区域 | 主内容区（`flex: 1`） |
| `Footer` | 区域 | 页脚底栏（可选） |
| `Grid` | 内容 | **均分多列**（指标卡、卡片矩阵） |
| `Flex` | 内容 | 方向、间距、对齐（工具栏、按钮组） |
| `Card` | 内容分组 | 业务区块视觉分区 |

### 选用层级（强约束）

```text
Page          → 页面级根容器（业务页默认）
Layout        → 区域级（Header/Sider/Content/Footer）
Grid / Flex   → 内容级排列
Card          → 业务内容分组
```

---

## 六条规则（AI 生成时必须遵守）

1. **普通业务页面根节点优先使用 `Page`。**
2. **完整 Header/Sider/Content 用 `Layout`，不得与 Application Shell 重复导航。**
3. **同级多列排列用 `Grid`**（当前为均分列，见下方限制）。
4. **工具栏、按钮组、横纵排列用 `Flex`。**
5. **业务信息分区用 `Card`**，不要用大量无语义 Flex 嵌套代替。
6. **优先布局 Props；`style` / `className` 仅作逃生口**（见下文）。

---

## 组件能力（Pactor 1.0）

### Page

- 页面内容根容器
- 提供子节点**纵向间距**（页面节奏）
- `props.title` 等页面级展示（与 `page.title` 可配合）

### Grid

```yaml
type: Grid
props:
  columns: 4    # 均分列数
  gap: 16
children:
  - type: Card
    # ...
```

**适用**：统计卡片、等宽卡片矩阵、双栏均分（`columns: 2`）。

**限制（1.0）**：`columns` 为**均分**列，不支持 `320px | 1fr` 非均分。主从不等宽见 [patterns/master-detail.md](../patterns/master-detail.md)。

### Flex

```yaml
type: Flex
props:
  direction: horizontal   # horizontal | vertical
  gap: middle             # 或数值
  justify: space-between
  align: center
children:
  - ...
```

各布局组件完整 props → [component-catalog.md](component-catalog.md)。

**适用**：工具栏、标题+操作区、按钮组、行内排列。

### Layout 族（Layout / Header / Sider / Content / Footer）

**完整说明** → [layout-components.md](layout-components.md)（职责、组合模式、与 Shell 边界、反模式）。

速记：

| 场景 | 做法 |
|------|------|
| 普通 admin 列表/表单页 | `body` 根用 **`Page`**，**不用** Layout 族 |
| `blank` 外壳全屏工作区 | `body` 根用 **`Layout`** + Sider/Content |
| admin 内再套 `Layout + Sider` | **禁止**（双侧边栏） |

```yaml
# blank 外壳典型骨架（详见 layout-components.md 模式 A）
body:
  type: Layout
  children:
    - type: Sider
      children: [...]
    - type: Layout
      children:
        - type: Header
          children: [...]
        - type: Content
          children:
            - type: Page
              children: [...]
```

---

## 布局 vs 样式优先级

```text
语义布局组件（Page / Grid / Flex / Layout）
        ↓
组件布局 Props（columns / gap / justify / align）
        ↓
Design Token（gap: middle 等）
        ↓
style / className（逃生口）
```

### 推荐

```yaml
type: Flex
props:
  gap: middle
  justify: end
```

### 不推荐（退化为 YAML 版 CSS）

```yaml
type: Flex
props:
  style:
    display: flex
    justifyContent: flex-end
    gap: 16px
```

---

## 快速选型

| 场景 | 选用 |
|------|------|
| 普通上下结构 | `Page` 默认纵向 |
| 统计卡片横排 | `Grid` + `Card` |
| 工具栏 / 按钮组 | `Flex` |
| 搜索区 / 业务块 | `Card` |
| 复杂全页骨架（blank） | `Layout` + Header/Sider/Content |
| 主从不等宽 | `Flex`（临时）或未来栅格扩展 |

---

## 协议扩展建议（非 1.0，勿写入 Schema）

以下需 Runtime 同步实现后再纳入规范：

| 扩展 | 说明 |
|------|------|
| 12 栅格 + `span` | `columns: 12`，子节点 `span: 4` |
| 非均分列 | `columns: [320px, 1fr]` |
| 响应式 | `columns: { xs: 1, md: 3, lg: 4 }` |

当前生成页面时**仅使用 1.0 已支持 Props**。

---

## 与 patterns 的关系

```text
structure/layout.md     → 布局能力与规则（本文）
patterns/list-page.md   → 列表页如何组合 Page + Card + Flex + Table
patterns/dashboard-page.md → 看板如何用 Grid + Statistic
```

生成流程：**先选 Pattern → 再选 Layout Primitive → 再填业务组件**。
